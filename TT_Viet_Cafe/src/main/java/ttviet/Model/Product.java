package ttviet.Model;
import java.math.BigDecimal;
public class Product {
 private int id; private String name,category; private BigDecimal price;
 public Product(int id,String name,String category,BigDecimal price){this.id=id;this.name=name;this.category=category;this.price=price;}
 public int getId(){return id;} public String getName(){return name;} public String getCategory(){return category;} public BigDecimal getPrice(){return price;}
 public String toString(){return name+" - "+price.toPlainString()+"đ";}
}