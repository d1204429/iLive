package fcu.iLive.repository.order;

import fcu.iLive.model.order.Order;
import fcu.iLive.model.order.OrderStatusConstants;
import fcu.iLive.model.order.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

/**
 * 訂單資料訪問層
 * 處理訂單相關的資料庫操作，包含訂單狀態管理與庫存互動
 */
@Repository
public class OrderRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private final RowMapper<Order> orderRowMapper = new RowMapper<Order>() {
    @Override
    public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
      Order order = new Order();
      order.setOrderId(rs.getInt("OrderId"));
      order.setUserId(rs.getInt("UserId"));
      order.setStatusId(rs.getInt("StatusId"));
      order.setTotalAmount(rs.getBigDecimal("TotalAmount"));
      order.setShippingAddress(rs.getString("ShippingAddress"));
      order.setPaymentMethod(rs.getString("PaymentMethod"));

      Timestamp orderDate = rs.getTimestamp("OrderDate");
      if (orderDate != null) {
        order.setOrderDate(orderDate.toLocalDateTime());
      }

      Timestamp createdAt = rs.getTimestamp("CreatedAt");
      if (createdAt != null) {
        order.setCreatedAt(createdAt.toLocalDateTime());
      }

      return order;
    }
  };

  /**
   * 根據訂單ID查詢訂單
   * @param orderId 訂單ID
   * @return 訂單實體
   */
  public Order findById(int orderId) {
    String sql = "SELECT * FROM Orders WHERE OrderId = ?";
    List<Order> orders = jdbcTemplate.query(sql, orderRowMapper, orderId);
    return orders.isEmpty() ? null : orders.get(0);
  }

  /**
   * 查詢用戶的所有訂單
   * @param userId 用戶ID
   * @return 訂單列表
   */
  public List<Order> findByUserId(int userId) {
    String sql = "SELECT * FROM Orders WHERE UserId = ? ORDER BY CreatedAt DESC";
    return jdbcTemplate.query(sql, orderRowMapper, userId);
  }

  /**
   * 創建訂單
   * @param order 訂單實體
   * @return 訂單ID
   */
  public int create(Order order) {
    String sql = """
        INSERT INTO Orders 
        (UserId, StatusId, TotalAmount, ShippingAddress, PaymentMethod, CreatedAt) 
        VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;
    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setInt(1, order.getUserId());
      ps.setInt(2, OrderStatusConstants.ORDERED); // 初始狀態為已下單
      ps.setBigDecimal(3, order.getTotalAmount());
      ps.setString(4, order.getShippingAddress());
      ps.setString(5, order.getPaymentMethod());
      return ps;
    }, keyHolder);

    return keyHolder.getKey().intValue();
  }

  /**
   * 更新訂單狀態
   * @param orderId 訂單ID
   * @param status 狀態值
   */
  public void updateOrderStatus(int orderId, int status) {
    String sql = "UPDATE Orders SET StatusId = ? WHERE OrderId = ?";
    jdbcTemplate.update(sql, status, orderId);
  }

  /**
   * 查詢過期未付款訂單
   * @return 過期訂單列表
   */
  public List<Order> findExpiredUnpaidOrders() {
    String sql = """
      SELECT * FROM Orders 
      WHERE StatusId = ? 
      AND CreatedAt < DATE_SUB(NOW(), INTERVAL 30 MINUTE)
      """;
    return jdbcTemplate.query(sql, orderRowMapper, OrderStatusConstants.ORDERED);
  }

  /**
   * 批量更新過期訂單狀態
   * @param orderIds 訂單ID列表
   * @return 更新的記錄數
   */
  public int updateExpiredOrderStatus(List<Integer> orderIds) {
    if (orderIds == null || orderIds.isEmpty()) {
      return 0;
    }

    String orderIdStr = String.join(",", orderIds.stream().map(String::valueOf).toList());
    String sql = "UPDATE Orders SET StatusId = ?, UpdatedAt = CURRENT_TIMESTAMP WHERE OrderId IN (" + orderIdStr + ")";
    return jdbcTemplate.update(sql, OrderStatusConstants.EXPIRED);
  }

  /**
   * 檢查用戶是否購買過指定商品
   * @param userId 用戶ID
   * @param productId 商品ID
   * @return 是否購買過
   */
  public boolean hasUserPurchasedProduct(int userId, int productId) {
    String sql = """
      SELECT COUNT(*) > 0 FROM Orders o
      JOIN OrderItems oi ON o.OrderId = oi.OrderId 
      WHERE o.UserId = ? AND oi.ProductId = ?
      AND o.StatusId IN (?, ?)
      """;
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql,
        Boolean.class,
        userId, productId,
        OrderStatusConstants.COMPLETED,
        OrderStatusConstants.SHIPPED));
  }

  /**
   * 查詢指定狀態的訂單
   * @param status 訂單狀態
   * @return 訂單列表
   */
  public List<Order> findByStatus(int status) {
    String sql = "SELECT * FROM Orders WHERE StatusId = ? ORDER BY CreatedAt DESC";
    return jdbcTemplate.query(sql, orderRowMapper, status);
  }
}