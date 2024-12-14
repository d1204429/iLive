package fcu.iLive.repository.order;

import fcu.iLive.model.order.Order;
import fcu.iLive.model.order.OrderItem;
import fcu.iLive.model.order.OrderStatus;
import fcu.iLive.model.product.Product;
import fcu.iLive.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/**
 * 訂單資料訪問層
 * 處理訂單相關的資料庫操作，包括基本CRUD及關聯查詢
 */
@Repository
public class OrderRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * 創建訂單
   * @param order 訂單實體
   * @return 新創建的訂單ID
   */
  public int create(Order order) {
    String sql = "INSERT INTO Orders (UserID, TotalAmount, ShippingAddress, StatusID, " +
        "CreatedAt) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setInt(1, order.getUserId());
      ps.setBigDecimal(2, order.getTotalAmount());
      ps.setString(3, order.getShippingAddress());
      ps.setInt(4, order.getStatusId());
      return ps;
    }, keyHolder);

    return keyHolder.getKey().intValue();
  }

  /**
   * 更新訂單付款時間
   * @param orderId 訂單ID
   */
  public void updateOrderDate(int orderId) {
    String sql = "UPDATE Orders SET OrderDate = CURRENT_TIMESTAMP WHERE OrderID = ?";
    jdbcTemplate.update(sql, orderId);
  }

  /**
   * 創建訂單項目
   * @param items 訂單項目列表
   */
  public void createOrderItems(List<OrderItem> items) {
    String sql = "INSERT INTO OrderItems (OrderID, ProductID, Quantity, Price, CreatedAt) " +
        "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

    jdbcTemplate.batchUpdate(sql,
        items,
        items.size(),
        (PreparedStatement ps, OrderItem item) -> {
          ps.setInt(1, item.getOrderId());
          ps.setInt(2, item.getProductId());
          ps.setInt(3, item.getQuantity());
          ps.setBigDecimal(4, item.getPrice());
        });
  }

  /**
   * 更新訂單付款方式
   * @param orderId 訂單ID
   * @param paymentMethod 付款方式
   */
  public void updatePaymentMethod(int orderId, String paymentMethod) {
    String sql = "UPDATE Orders SET PaymentMethod = ? WHERE OrderID = ?";
    jdbcTemplate.update(sql, paymentMethod, orderId);
  }

  /**
   * 更新訂單狀態
   * @param orderId 訂單ID
   * @param statusId 狀態ID
   */
  public void updateStatus(int orderId, int statusId) {
    String sql = "UPDATE Orders SET StatusID = ? WHERE OrderID = ?";
    jdbcTemplate.update(sql, statusId, orderId);
  }

  /**
   * 根據訂單ID查詢訂單
   * 包含用戶信息、訂單狀態和訂單項目
   * @param orderId 訂單ID
   * @return 訂單實體
   */
  public Order findById(int orderId) {
    String sql = "SELECT o.*, u.Username, u.Email, u.FullName, u.PhoneNumber, u.Address, " +
        "os.StatusName " +
        "FROM Orders o " +
        "LEFT JOIN Users u ON o.UserID = u.UserID " +
        "LEFT JOIN OrderStatus os ON o.StatusID = os.StatusID " +
        "WHERE o.OrderID = ?";

    List<Order> orders = jdbcTemplate.query(sql, (rs, rowNum) -> {
      Order order = mapRowToOrder(rs, rowNum);

      // 設置用戶信息
      User user = new User();
      user.setUserId(rs.getInt("UserID"));
      user.setUsername(rs.getString("Username"));
      user.setEmail(rs.getString("Email"));
      user.setFullName(rs.getString("FullName"));
      user.setPhoneNumber(rs.getString("PhoneNumber"));
      user.setAddress(rs.getString("Address"));
      order.setUser(user);

      // 設置訂單狀態
      OrderStatus status = new OrderStatus();
      status.setStatusId(rs.getInt("StatusID"));
      status.setStatusName(rs.getString("StatusName"));
      order.setStatus(status);

      return order;
    }, orderId);

    if (orders.isEmpty()) {
      return null;
    }

    Order order = orders.get(0);
    order.setItems(findOrderItems(orderId));
    return order;
  }

  /**
   * 查詢用戶的所有訂單
   * 包含用戶信息、訂單狀態和訂單項目
   * @param userId 用戶ID
   * @return 訂單列表
   */
  public List<Order> findByUserId(int userId) {
    String sql = "SELECT o.*, u.Username, u.Email, u.FullName, u.PhoneNumber, u.Address, " +
        "os.StatusName " +
        "FROM Orders o " +
        "LEFT JOIN Users u ON o.UserID = u.UserID " +
        "LEFT JOIN OrderStatus os ON o.StatusID = os.StatusID " +
        "WHERE o.UserID = ? " +
        "ORDER BY o.CreatedAt DESC";

    List<Order> orders = jdbcTemplate.query(sql, (rs, rowNum) -> {
      Order order = mapRowToOrder(rs, rowNum);

      // 設置用戶信息
      User user = new User();
      user.setUserId(rs.getInt("UserID"));
      user.setUsername(rs.getString("Username"));
      user.setEmail(rs.getString("Email"));
      user.setFullName(rs.getString("FullName"));
      user.setPhoneNumber(rs.getString("PhoneNumber"));
      user.setAddress(rs.getString("Address"));
      order.setUser(user);

      // 設置訂單狀態
      OrderStatus status = new OrderStatus();
      status.setStatusId(rs.getInt("StatusID"));
      status.setStatusName(rs.getString("StatusName"));
      order.setStatus(status);

      return order;
    }, userId);

    // 為每個訂單加載訂單項目
    orders.forEach(order -> order.setItems(findOrderItems(order.getOrderId())));
    return orders;
  }

  /**
   * 查詢訂單的所有項目
   * 包含商品信息
   * @param orderId 訂單ID
   * @return 訂單項目列表
   */
  private List<OrderItem> findOrderItems(int orderId) {
    String sql = "SELECT oi.*, p.Name as ProductName, p.Price as ProductPrice " +
        "FROM OrderItems oi " +
        "LEFT JOIN Products p ON oi.ProductID = p.ProductID " +
        "WHERE oi.OrderID = ?";

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      OrderItem item = new OrderItem();
      item.setOrderItemId(rs.getInt("OrderItemID"));
      item.setOrderId(rs.getInt("OrderID"));
      item.setProductId(rs.getInt("ProductID"));
      item.setQuantity(rs.getInt("Quantity"));
      item.setPrice(rs.getBigDecimal("Price"));
      item.setCreatedAt(rs.getTimestamp("CreatedAt") != null ?
          rs.getTimestamp("CreatedAt").toLocalDateTime() : null);

      // 設置商品信息
      Product product = new Product();
      product.setProductId(rs.getInt("ProductID"));
      product.setName(rs.getString("ProductName"));
      product.setPrice(rs.getBigDecimal("ProductPrice"));
      item.setProduct(product);

      return item;
    }, orderId);
  }

  /**
   * 將資料庫查詢結果映射為訂單實體
   * @param rs 資料庫結果集
   * @param rowNum 行號
   * @return 訂單實體
   */
  private Order mapRowToOrder(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
    Order order = new Order();
    order.setOrderId(rs.getInt("OrderID"));
    order.setUserId(rs.getInt("UserID"));
    order.setOrderDate(rs.getTimestamp("OrderDate") != null ?
        rs.getTimestamp("OrderDate").toLocalDateTime() : null);
    order.setTotalAmount(rs.getBigDecimal("TotalAmount"));
    order.setShippingAddress(rs.getString("ShippingAddress"));
    order.setPaymentMethod(rs.getString("PaymentMethod"));
    order.setStatusId(rs.getInt("StatusID"));
    order.setCreatedAt(rs.getTimestamp("CreatedAt") != null ?
        rs.getTimestamp("CreatedAt").toLocalDateTime() : null);
    return order;
  }
}