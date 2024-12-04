package fcu.iLive.repository.product;

import fcu.iLive.model.product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProductRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public Product findById(int productId) {
    List<Product> products = jdbcTemplate.query(
        "SELECT * FROM products WHERE product_id = ?",
        new BeanPropertyRowMapper<>(Product.class),
        productId
    );
    return products.isEmpty() ? null : products.get(0);
  }

  public Product save(Product product) {
    if (product.getProductId() > 0) {
      jdbcTemplate.update(
          "UPDATE products SET name=?, description=?, price=?, stock=?, category_id=?, " +
              "brand=?, image_url=?, updated_at=? WHERE product_id=?",
          product.getName(), product.getDescription(), product.getPrice(),
          product.getStock(), product.getCategoryId(), product.getBrand(),
          product.getImageUrl(), product.getUpdatedAt(), product.getProductId()
      );
    } else {
      jdbcTemplate.update(
          "INSERT INTO products (name, description, price, stock, category_id, brand, " +
              "image_url, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?)",
          product.getName(), product.getDescription(), product.getPrice(),
          product.getStock(), product.getCategoryId(), product.getBrand(),
          product.getImageUrl(), product.getCreatedAt(), product.getUpdatedAt()
      );
    }
    return product;
  }
}