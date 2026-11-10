package ttviet.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ttviet.dao.DB;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * Webhook SePay doc lap. Chay tren may chu co HTTPS reverse proxy.
 * Cau hinh bang bien moi truong SEPAY_WEBHOOK_API_KEY va WEBHOOK_PORT.
 * Khong dat API key trong ung dung Swing.
 */
public final class SePayWebhookServer {
    private static final ObjectMapper JSON = new ObjectMapper();
    private SePayWebhookServer() {}

    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("SEPAY_WEBHOOK_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Chua cau hinh bien moi truong SEPAY_WEBHOOK_API_KEY");
        }
        int port = Integer.parseInt(System.getenv().getOrDefault("WEBHOOK_PORT", "8088"));
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/webhooks/sepay", exchange -> handle(exchange, apiKey));
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("SePay webhook dang nghe cong " + port + " tai /webhooks/sepay");
    }

    private static void handle(HttpExchange ex, String apiKey) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            respond(ex, 405, "{\"success\":false,\"message\":\"POST required\"}");
            return;
        }
        String authorization = ex.getRequestHeaders().getFirst("Authorization");
        if (authorization == null || !constantTimeEquals(authorization.trim(), "Apikey " + apiKey)) {
            respond(ex, 401, "{\"success\":false,\"message\":\"Unauthorized\"}");
            return;
        }
        try {
            JsonNode body = JSON.readTree(ex.getRequestBody());
            String transactionCode = text(body, "id");
            String content = firstNonBlank(text(body, "content"), text(body, "description"));
            String transferType = text(body, "transferType");
            BigDecimal amount = decimal(body, "transferAmount");
            String transactionDate = text(body, "transactionDate");

            if (transactionCode.isBlank() || amount == null || content.isBlank()
                    || !"in".equalsIgnoreCase(transferType)) {
                respond(ex, 200, "{\"success\":true,\"message\":\"Ignored: not a valid incoming payment\"}");
                return;
            }
            processPayment(transactionCode, content, amount, transactionDate, body);
            respond(ex, 200, "{\"success\":true}");
        } catch (Exception e) {
            e.printStackTrace();
            respond(ex, 500, "{\"success\":false,\"message\":\"Processing failed\"}");
        }
    }

    private static void processPayment(String txCode, String content, BigDecimal amount,
                                       String txDate, JsonNode payload) throws Exception {
        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                long orderId;
                String orderCode;
                BigDecimal orderTotal;
                try (PreparedStatement q = c.prepareStatement(
                    "SELECT id,order_code,total FROM orders " +
                    "WHERE payment_status='Chờ thanh toán' AND LOCATE(order_code,?)>0 " +
                    "ORDER BY id DESC LIMIT 1 FOR UPDATE")) {
                    q.setString(1, content);
                    try (ResultSet r = q.executeQuery()) {
                        if (!r.next()) {
                            c.rollback();
                            return; // khong tu dong gan tien khong ro don
                        }
                        orderId = r.getLong(1);
                        orderCode = r.getString(2);
                        orderTotal = r.getBigDecimal(3);
                    }
                }
                if (orderTotal == null || orderTotal.compareTo(amount) != 0) {
                    c.rollback(); // so tien khong khop: khong danh dau da thanh toan
                    return;
                }

                String insert = "INSERT INTO payment_transactions(provider,transaction_code,order_code,amount," +
                    "transfer_content,transaction_time,raw_payload,verification_status) " +
                    "VALUES('SEPAY',?,?,?,?,?,?,'Đã xác minh')";
                try (PreparedStatement p = c.prepareStatement(insert)) {
                    p.setString(1, txCode);
                    p.setString(2, orderCode);
                    p.setBigDecimal(3, amount);
                    p.setString(4, content);
                    Timestamp ts = parseTimestamp(txDate);
                    if (ts == null) p.setNull(5, Types.TIMESTAMP); else p.setTimestamp(5, ts);
                    p.setString(6, JSON.writeValueAsString(payload));
                    p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement(
                    "UPDATE orders SET payment_status='Đã thanh toán',payment_method='SEPAY'," +
                    "paid_at=NOW(),status='Đã thanh toán' WHERE id=? AND payment_status='Chờ thanh toán'")) {
                    p.setLong(1, orderId);
                    if (p.executeUpdate() != 1) throw new SQLException("Don hang da duoc cap nhat boi giao dich khac");
                }
                if (hasColumn(c, "cafe_tables", "status")) {
                    try (PreparedStatement p = c.prepareStatement(
                        "UPDATE cafe_tables t SET status='Trống' WHERE t.id=(SELECT table_id FROM orders WHERE id=?) " +
                    "AND NOT EXISTS(SELECT 1 FROM orders o WHERE o.table_id=t.id AND o.payment_status='Chờ thanh toán')")) {
                        p.setLong(1, orderId);
                        p.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLIntegrityConstraintViolationException duplicate) {
                c.rollback(); // SePay gui lai webhook: giao dich da xu ly
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    private static boolean hasColumn(Connection c, String table, String column) throws SQLException {
        try (ResultSet r = c.getMetaData().getColumns(c.getCatalog(), null, table, column)) {
            return r.next();
        }
    }
    private static Timestamp parseTimestamp(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Timestamp.valueOf(value.replace('T', ' ').substring(0, 19)); }
        catch (Exception ignored) { return null; }
    }
    private static String text(JsonNode n, String key) {
        JsonNode v = n == null ? null : n.get(key);
        return v == null || v.isNull() ? "" : v.asText("");
    }
    private static BigDecimal decimal(JsonNode n, String key) {
        try { String v = text(n, key); return v.isBlank() ? null : new BigDecimal(v); }
        catch (Exception ignored) { return null; }
    }
    private static String firstNonBlank(String a, String b) { return a == null || a.isBlank() ? b : a; }
    private static boolean constantTimeEquals(String a, String b) {
        return java.security.MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),
                                                    b.getBytes(StandardCharsets.UTF_8));
    }
    private static void respond(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (var out = ex.getResponseBody()) { out.write(bytes); }
    }
}
