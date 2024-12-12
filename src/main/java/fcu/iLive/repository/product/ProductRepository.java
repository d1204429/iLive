package fcu.iLive.repository.product;

import fcu.iLive.model.product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品資料訪問層
 * 處理商品相關的資料庫操作，包含基本CRUD及庫存管理
 */
@Repository
public class ProductRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * 根據商品ID查詢商品資訊
   * @param productId 商品ID
   * @return 商品實體，若不存在則返回null
   */
  public Product findById(int productId) {
    String sql = "SELECT * FROM Products WHERE ProductID = ?";

    List<Product> products = jdbcTemplate.query(
        sql,
        this::mapRowToProduct,
        productId
    );

    return products.isEmpty() ? null : products.get(0);
  }

  /**
   * 新增商品
   * @param product 商品實體
   * @return 包含生成ID的商品實體
   */
  public Product save(Product product) {
    String sql = "INSERT INTO Products (Name, Description, Price, Stock, CategoryID, Brand, " +
        "ImageURL, LockedStock) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";

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
      return ps;
    }, keyHolder);

    product.setProductId(keyHolder.getKey().intValue());
    return product;
  }

  /**
   * 查詢所有商品
   * @return 商品列表
   */
  public List<Product> findAll() {
    String sql = "SELECT * FROM Products";
    return jdbcTemplate.query(sql, this::mapRowToProduct);
  }

  /**
   * 更新商品資訊
   * @param product 待更新的商品實體
   */
  public void update(Product product) {
    String sql = "UPDATE Products SET Name = ?, Description = ?, Price = ?, Stock = ?, " +
        "CategoryID = ?, Brand = ?, ImageURL = ? WHERE ProductID = ?";

    jdbcTemplate.update(sql,
        product.getName(),
        product.getDescription(),
        product.getPrice(),
        product.getStock(),
        product.getCategoryId(),
        product.getBrand(),
        product.getImageUrl(),
        product.getProductId());
  }

  /**
   * 刪除商品
   * @param productId 商品ID
   */
  public void delete(int productId) {
    String sql = "DELETE FROM Products WHERE ProductID = ?";
    jdbcTemplate.update(sql, productId);
  }

  /**
   * 根據分類查詢商品
   * @param categoryId 分類ID
   * @return 該分類下的商品列表
   */
  public List<Product> findByCategory(int categoryId) {
    String sql = "SELECT * FROM Products WHERE CategoryID = ?";
    return jdbcTemplate.query(sql, this::mapRowToProduct, categoryId);
  }

  /**
   * 搜尋商品
   * @param keyword 關鍵字，用於商品名稱和描述的模糊查詢
   * @param minPrice 最低價格
   * @param maxPrice 最高價格
   * @return 符合條件的商品列表
   */
  public List<Product> search(String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
    StringBuilder sql = new StringBuilder("SELECT * FROM Products WHERE 1=1");
    List<Object> params = new ArrayList<>();

    if (keyword != null && !keyword.trim().isEmpty()) {
      sql.append(" AND (Name LIKE ? OR Description LIKE ?)");
      String searchPattern = "%" + keyword.trim() + "%";
      params.add(searchPattern);
      params.add(searchPattern);
    }

    if (minPrice != null) {
      sql.append(" AND Price >= ?");
      params.add(minPrice);
    }

    if (maxPrice != null) {
      sql.append(" AND Price <= ?");
      params.add(maxPrice);
    }

    return jdbcTemplate.query(sql.toString(), this::mapRowToProduct, params.toArray());
  }

  /**
   * 將資料庫查詢結果映射為商品實體
   * @param rs 資料庫結果集
   * @param rowNum 行號
   * @return 商品實體
   */
  private Product mapRowToProduct(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
    Product product = new Product();
    product.setProductId(rs.getInt("ProductID"));
    product.setName(rs.getString("Name"));
    product.setDescription(rs.getString("Description"));
    product.setPrice(rs.getBigDecimal("Price"));
    product.setStock(rs.getInt("Stock"));
    product.setCategoryId(rs.getInt("CategoryID"));
    product.setBrand(rs.getString("Brand"));
    product.setImageUrl(rs.getString("ImageURL"));
    product.setLockedStock(rs.getInt("LockedStock"));
    product.setCreatedAt(rs.getTimestamp("CreatedAt") != null ?
        rs.getTimestamp("CreatedAt").toLocalDateTime() : null);
    product.setUpdatedAt(rs.getTimestamp("UpdatedAt") != null ?
        rs.getTimestamp("UpdatedAt").toLocalDateTime() : null);
    return product;
  }

  /**
   * 更新商品的庫存鎖定數量
   * @param productId 商品ID
   * @param lockedStock 新的鎖定數量
   */
  public void updateLockedStock(int productId, int lockedStock) {
    String sql = "UPDATE Products SET LockedStock = ?, UpdatedAt = CURRENT_TIMESTAMP " +
        "WHERE ProductID = ?";
    jdbcTemplate.update(sql, lockedStock, productId);
  }

  /**
   * 扣減商品的實際庫存和保留庫存
   * 用於訂單支付完成時確認扣庫存
   * @param productId 商品ID
   * @param quantity 扣減數量
   * @return 是否扣減成功
   */
  public boolean deductStock(int productId, int quantity) {
    String sql = "UPDATE Products SET Stock = Stock - ?, LockedStock = LockedStock - ?, " +
        "UpdatedAt = CURRENT_TIMESTAMP WHERE ProductID = ? AND Stock >= ? AND LockedStock >= ?";
    return jdbcTemplate.update(sql, quantity, quantity, productId, quantity, quantity) > 0;
  }

  /**
   * 釋放商品的保留庫存
   * 用於訂單取消或訂單逾期時釋放庫存
   * @param productId 商品ID
   * @param quantity 釋放數量
   * @return 是否釋放成功
   */
  public boolean releaseLockedStock(int productId, int quantity) {
    String sql = "UPDATE Products SET LockedStock = LockedStock - ?, " +
        "UpdatedAt = CURRENT_TIMESTAMP WHERE ProductID = ? AND LockedStock >= ?";
    return jdbcTemplate.update(sql, quantity, productId, quantity) > 0;
  }

  /**
   * 檢查商品是否有足夠的可用庫存
   * 可用庫存 = 實際庫存 - 已鎖定庫存
   * @param productId 商品ID
   * @param quantity 需要的數量
   * @return 是否有足夠庫存
   */
  public boolean hasEnoughStock(int productId, int quantity) {
    Product product = findById(productId);
    return product != null && product.getAvailableStock() >= quantity;
  }
}