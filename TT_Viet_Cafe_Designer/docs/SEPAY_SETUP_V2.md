# T&T Viet - Huong dan chuan bi SePay (v2)

## 1. Sao luu va cap nhat database

1. Mo phpMyAdmin, chon database `tt_viet_cafe`.
2. Export mot ban sao truoc khi thay doi.
3. Mo tab Import hoac SQL va chay file `database/migration_v2_thanh_toan_sepay.sql`.
4. Migration chi them cot/bang; khong DROP, TRUNCATE hay xoa don hang cu.
5. Don hang cu duoc gan trang thai thanh toan `Chưa đối soát`. Khong tu dong coi chung la da thu tien.

Ung dung phien ban nay can migration truoc khi mo man hinh hoa don/goi mon.

## 2. Cau hinh webhook

Webhook duoc chay o may chu co ket noi den MySQL. Khong chay webhook trong ung dung Swing cua thu ngan va khong dua API key vao Git.

Bien moi truong can dat tren may chu:
- `SEPAY_WEBHOOK_API_KEY`: API Key webhook tao trong SePay.
- `WEBHOOK_PORT`: cong noi bo, mac dinh 8088.

Endpoint noi bo:
`POST http://127.0.0.1:8088/webhooks/sepay`

Can dat reverse proxy HTTPS (vi du Nginx) de cong khai endpoint cho SePay. Chi cau hinh URL HTTPS cong khai trong SePay sau khi endpoint da duoc deploy va kiem tra.

Chay class `ttviet.payment.SePayWebhookServer` tren may chu. Maven can tai cac dependency trong `pom.xml`.

## 3. Quy tac ghi nhan thanh toan

- Tao don, in phieu, hoac hien QR khong lam don thanh da thanh toan.
- Webhook chi xu ly giao dich chuyen tien vao.
- Noi dung chuyen khoan phai chua ma don; so tien phai khop chinh xac tong don.
- Ma giao dich SePay la duy nhat; webhook gui lap khong duoc ghi nhan hai lan.
- Giao dich sai so tien/khong tim thay ma don khong duoc tu dong gan vao don.
- Don cu `Chưa đối soát` can duoc doi chieu rieng, khong tu dong cap nhat.

## 4. Thong tin chua the tu dien

De tao QR VietQR dung tai khoan cua quan, can thong tin ngan hang/so tai khoan cua chu quan va URL webhook HTTPS da deploy. Khong commit API key, mat khau ngan hang hay token vao repository.

## 5. Gioi han ban hien tai

Webhook backend va luu don cho trang thai chua thanh toan da duoc them vao ma nguon. Chua the xac nhan thanh toan thuc te cho den khi co tai khoan SePay, tai khoan ngan hang nhan tien, API key va may chu HTTPS. QR tren giao dien thu ngan, dong bo don dang mo va bo giao dien rieng tung vai tro van la cac hang muc tiep theo.
