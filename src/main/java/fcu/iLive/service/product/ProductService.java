package fcu.iLive.service.product;

import fcu.iLive.model.product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  // 新增商品
  public Product createProduct(Product product) {
    String sql = "INSERT INTO Products (Name, Description, Price, Stock, CategoryID, Brand, " +
        "ImageURL, CreatedAt, UpdatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, product.getName());
      ps.setString(2, product.getDescription());
      ps.setBigDecimal(3, product.getPrice());
      ps.setInt(4, product.getStock());
      ps.setInt(5, product.getCategoryId());
      ps.setString(6, product.getBrand());
      ps.setString(7, product.getImageUrl());
      ps.setObject(8, LocalDateTime.now());
      ps.setObject(9, LocalDateTime.now());
      return ps;
    }, keyHolder);

    product.setProductId(keyHolder.getKey().intValue());
    return product;
  }

  // 查詢單一商品
  public Product getProduct(int productId) {
    String sql = "SELECT * FROM Products WHERE ProductID = ?";
    return jdbcTemplate.queryForObject(sql, this::mapResultSetToProduct, productId);
  }

  // 查詢所有商品
  public List<Product> getAllProducts() {
    String sql = "SELECT * FROM Products";
    return jdbcTemplate.query(sql, this::mapResultSetToProduct);
  }

  // 更新商品資料
  public void updateProduct(Product product) {
    String sql = "UPDATE Products SET Name = ?, Description = ?, Price = ?, Stock = ?, " +
        "CategoryID = ?, Brand = ?, ImageURL = ?, UpdatedAt = ? WHERE ProductID = ?";

    jdbcTemplate.update(sql,
        product.getName(),
        product.getDescription(),
        product.getPrice(),
        product.getStock(),
        product.getCategoryId(),
        product.getBrand(),
        product.getImageUrl(),
        LocalDateTime.now(),
        product.getProductId()
    );
  }

  // 刪除商品
  public void deleteProduct(int productId) {
    String sql = "DELETE FROM Products WHERE ProductID = ?";
    jdbcTemplate.update(sql, productId);
  }

  // 根據分類查詢商品
  public List<Product> getProductsByCategory(int categoryId) {
    String sql = "SELECT * FROM Products WHERE CategoryID = ?";
    return jdbcTemplate.query(sql, this::mapResultSetToProduct, categoryId);
  }

  // 搜尋商品功能
  public List<Product> searchProducts(String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
    StringBuilder sql = new StringBuilder("SELECT * FROM Products WHERE 1=1");
    List<Object> params = new ArrayList<>();

    // 關鍵字搜尋（商品名稱或描述）
    if (keyword != null && !keyword.trim().isEmpty()) {
      sql.append(" AND (Name LIKE ? OR Description LIKE ?)");
      String searchTerm = "%" + keyword + "%";
      params.add(searchTerm);
      params.add(searchTerm);
    }

    // 價格範圍搜尋
    if (minPrice != null) {
      sql.append(" AND Price >= ?");
      params.add(minPrice);
    }

    if (maxPrice != null) {
      sql.append(" AND Price <= ?");
      params.add(maxPrice);
    }

    return jdbcTemplate.query(sql.toString(), this::mapResultSetToProduct, params.toArray());
  }

  // ResultSet 轉換為 Product 物件
  private Product mapResultSetToProduct(ResultSet rs, int rowNum) throws java.sql.SQLException {
    Product product = new Product();
    product.setProductId(rs.getInt("ProductID"));
    product.setName(rs.getString("Name"));
    product.setDescription(rs.getString("Description"));
    product.setPrice(rs.getBigDecimal("Price"));
    product.setStock(rs.getInt("Stock"));
    product.setCategoryId(rs.getInt("CategoryID"));
    product.setBrand(rs.getString("Brand"));
    product.setImageUrl(rs.getString("ImageURL"));
    product.setCreatedAt(rs.getObject("CreatedAt", LocalDateTime.class));
    product.setUpdatedAt(rs.getObject("UpdatedAt", LocalDateTime.class));
    return product;
  }
}