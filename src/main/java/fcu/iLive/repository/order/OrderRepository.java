package fcu.iLive.repository.order;

import fcu.iLive.model.order.Order;
import fcu.iLive.model.order.OrderStatusConstants;
import fcu.iLive.model.order.OrderItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
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

  /**
   * 查詢訂單列表（基本資訊）
   */
  public List<Order> findAll() {
    String sql = """
            SELECT o.*, u.Username, u.PhoneNumber, s.StatusName,
                   oi.OrderItemID, oi.ProductID, oi.Quantity, oi.Price,
                   p.Name as ProductName
            FROM Orders o
            LEFT JOIN Users u ON o.UserID = u.UserID
            LEFT JOIN OrderStatus s ON o.StatusID = s.StatusID
            LEFT JOIN OrderItems oi ON o.OrderID = oi.OrderID
            LEFT JOIN Products p ON oi.ProductID = p.ProductID
            ORDER BY o.OrderID DESC, o.CreatedAt DESC
            """;

    return jdbcTemplate.query(sql, new OrderListResultSetExtractor());
  }

  /**
   * 管理員查詢訂單詳情（完整資訊）
   * @param orderId 訂單ID
   * @return 訂單詳細資訊
   */
  public Order findByIdForAdmin(int orderId) {
    String sql = """
            SELECT o.*, u.Username, u.PhoneNumber, u.Email, s.StatusName,
                   oi.OrderItemID, oi.ProductID, oi.Quantity, oi.Price,
                   p.Name as ProductName, p.Description, p.Brand
            FROM Orders o
            LEFT JOIN Users u ON o.UserID = u.UserID
            LEFT JOIN OrderStatus s ON o.StatusID = s.StatusID
            LEFT JOIN OrderItems oi ON o.OrderID = oi.OrderID
            LEFT JOIN Products p ON oi.ProductID = p.ProductID
            WHERE o.OrderID = ?
            """;

    return jdbcTemplate.query(sql, new OrderDetailResultSetExtractor(), orderId)
        .stream()
        .findFirst()
        .orElse(null);
  }

  /**
   * 管理員查詢訂單列表（基本資訊）
   * @return 訂單列表
   */
  public List<Order> findAllForAdmin() {
    String sql = """
            SELECT o.*, u.Username, u.PhoneNumber, s.StatusName,
                   oi.OrderItemID, oi.ProductID, oi.Quantity, oi.Price,
                   p.Name as ProductName
            FROM Orders o
            LEFT JOIN Users u ON o.UserID = u.UserID
            LEFT JOIN OrderStatus s ON o.StatusID = s.StatusID
            LEFT JOIN OrderItems oi ON o.OrderID = oi.OrderID
            LEFT JOIN Products p ON oi.ProductID = p.ProductID
            ORDER BY o.OrderID DESC, o.CreatedAt DESC
            """;

    return jdbcTemplate.query(sql, new OrderListResultSetExtractor());
  }


  /**
   * 訂單列表資料處理器（基本資訊）
   */
  private class OrderListResultSetExtractor implements ResultSetExtractor<List<Order>> {
    @Override
    public List<Order> extractData(ResultSet rs) throws SQLException, DataAccessException {
      Map<Integer, Order> orderMap = new HashMap<>();

      while (rs.next()) {
        int orderId = rs.getInt("OrderID");

        Order order = orderMap.computeIfAbsent(orderId, k -> {
          try {
            Order newOrder = new Order();
            newOrder.setOrderId(orderId);
            newOrder.setUserId(rs.getInt("UserID"));
            newOrder.setStatusId(rs.getInt("StatusID"));
            newOrder.setTotalAmount(rs.getBigDecimal("TotalAmount"));
            newOrder.setShippingAddress(rs.getString("ShippingAddress"));
            newOrder.setPaymentMethod(rs.getString("PaymentMethod"));

            // 設置基本關聯資料
            newOrder.setUserName(rs.getString("Username"));
            newOrder.setUserPhone(rs.getString("PhoneNumber"));
            newOrder.setStatusName(rs.getString("StatusName"));

            newOrder.setItems(new ArrayList<>());

            // 處理日期
            Timestamp orderDate = rs.getTimestamp("OrderDate");
            if (orderDate != null) {
              newOrder.setOrderDate(orderDate.toLocalDateTime());
            }

            Timestamp createdAt = rs.getTimestamp("CreatedAt");
            if (createdAt != null) {
              newOrder.setCreatedAt(createdAt.toLocalDateTime());
            }

            return newOrder;
          } catch (SQLException e) {
            throw new RuntimeException("Error creating order object", e);
          }
        });

        // 處理訂單項目（基本資訊）
        int orderItemId = rs.getInt("OrderItemID");
        if (!rs.wasNull() && orderItemId > 0) {
          OrderItem item = new OrderItem();
          item.setOrderItemId(orderItemId);
          item.setOrderId(orderId);
          item.setProductId(rs.getInt("ProductID"));
          item.setQuantity(rs.getInt("Quantity"));
          item.setPrice(rs.getBigDecimal("Price"));

          // 商品名稱截斷處理
          String productName = rs.getString("ProductName");
          item.setProductName(truncateProductName(productName));

          order.getItems().add(item);
        }
      }

      return new ArrayList<>(orderMap.values());
    }
  }

  /**
   * 訂單詳情資料處理器（完整資訊）
   */
  private class OrderDetailResultSetExtractor implements ResultSetExtractor<List<Order>> {
    @Override
    public List<Order> extractData(ResultSet rs) throws SQLException, DataAccessException {
      Map<Integer, Order> orderMap = new HashMap<>();

      while (rs.next()) {
        int orderId = rs.getInt("OrderID");

        Order order = orderMap.computeIfAbsent(orderId, k -> {
          try {
            Order newOrder = new Order();
            newOrder.setOrderId(orderId);
            newOrder.setUserId(rs.getInt("UserID"));
            newOrder.setStatusId(rs.getInt("StatusID"));
            newOrder.setTotalAmount(rs.getBigDecimal("TotalAmount"));
            newOrder.setShippingAddress(rs.getString("ShippingAddress"));
            newOrder.setPaymentMethod(rs.getString("PaymentMethod"));

            // 設置完整關聯資料
            newOrder.setUserName(rs.getString("Username"));
            newOrder.setUserPhone(rs.getString("PhoneNumber"));
            newOrder.setUserEmail(rs.getString("Email"));
            newOrder.setStatusName(rs.getString("StatusName"));

            newOrder.setItems(new ArrayList<>());

            // 處理日期
            Timestamp orderDate = rs.getTimestamp("OrderDate");
            if (orderDate != null) {
              newOrder.setOrderDate(orderDate.toLocalDateTime());
            }

            Timestamp createdAt = rs.getTimestamp("CreatedAt");
            if (createdAt != null) {
              newOrder.setCreatedAt(createdAt.toLocalDateTime());
            }

            return newOrder;
          } catch (SQLException e) {
            throw new RuntimeException("Error creating order object", e);
          }
        });

        // 處理訂單項目（完整資訊）
        int orderItemId = rs.getInt("OrderItemID");
        if (!rs.wasNull() && orderItemId > 0) {
          OrderItem item = new OrderItem();
          item.setOrderItemId(orderItemId);
          item.setOrderId(orderId);
          item.setProductId(rs.getInt("ProductID"));
          item.setQuantity(rs.getInt("Quantity"));
          item.setPrice(rs.getBigDecimal("Price"));
          item.setProductName(rs.getString("ProductName"));
          item.setProductSpec(rs.getString("Description"));  // 使用商品描述作為規格
          order.getItems().add(item);
        }
      }

      return new ArrayList<>(orderMap.values());
    }
  }

  private String truncateProductName(String name) {
    if (name == null) return "";

    int len = 0;
    for (int i = 0; i < name.length(); i++) {
      len += name.charAt(i) > 255 ? 2 : 1;
      if (len > 20) {
        return name.substring(0, i) + "...";
      }
    }
    return name;
  }

  /**
   * 管理員依狀態查詢訂單（基本資訊）
   */
  public List<Order> findByStatusForAdmin(int status) {
    String sql = """
            SELECT o.*, u.Username, u.PhoneNumber, s.StatusName,
                   oi.OrderItemID, oi.ProductID, oi.Quantity, oi.Price,
                   p.Name as ProductName
            FROM Orders o
            LEFT JOIN Users u ON o.UserID = u.UserID
            LEFT JOIN OrderStatus s ON o.StatusID = s.StatusID
            LEFT JOIN OrderItems oi ON o.OrderID = oi.OrderID
            LEFT JOIN Products p ON oi.ProductID = p.ProductID
            WHERE o.StatusID = ?
            ORDER BY o.OrderID DESC, o.CreatedAt DESC
            """;

    return jdbcTemplate.query(sql, new OrderListResultSetExtractor(), status);
  }
}

