//訂單資料操作

package fcu.iLive.repository.order;

import fcu.iLive.model.order.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public Order save(Order order) {
    String sql = "INSERT INTO Orders (UserID, OrderDate, TotalAmount, ShippingAddress, PaymentMethod, StatusID) " +
        "VALUES (?, ?, ?, ?, ?, ?)";

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setInt(1, order.getUserId());
      ps.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
      ps.setBigDecimal(3, order.getTotalAmount());
      ps.setString(4, order.getShippingAddress());
      ps.setString(5, order.getPaymentMethod());
      ps.setInt(6, order.getStatusId());
      return ps;
    }, keyHolder);

    order.setOrderId(keyHolder.getKey().intValue());
    return order;
  }

  public Optional<Order> findById(int orderId) {
    String sql = "SELECT * FROM Orders WHERE OrderID = ?";

    List<Order> orders = jdbcTemplate.query(sql,
        new Object[]{orderId},
        (rs, rowNum) -> {
          Order order = new Order();
          order.setOrderId(rs.getInt("OrderID"));
          order.setUserId(rs.getInt("UserID"));
          order.setOrderDate(rs.getTimestamp("OrderDate").toLocalDateTime());
          order.setTotalAmount(rs.getBigDecimal("TotalAmount"));
          order.setShippingAddress(rs.getString("ShippingAddress"));
          order.setPaymentMethod(rs.getString("PaymentMethod"));
          order.setStatusId(rs.getInt("StatusID"));
          return order;
        });

    return orders.isEmpty() ? Optional.empty() : Optional.of(orders.get(0));
  }

  public List<Order> findByUserId(int userId) {
    String sql = "SELECT * FROM Orders WHERE UserID = ? ORDER BY OrderDate DESC";

    return jdbcTemplate.query(sql,
        new Object[]{userId},
        (rs, rowNum) -> {
          Order order = new Order();
          order.setOrderId(rs.getInt("OrderID"));
          order.setUserId(rs.getInt("UserID"));
          order.setOrderDate(rs.getTimestamp("OrderDate").toLocalDateTime());
          order.setTotalAmount(rs.getBigDecimal("TotalAmount"));
          order.setShippingAddress(rs.getString("ShippingAddress"));
          order.setPaymentMethod(rs.getString("PaymentMethod"));
          order.setStatusId(rs.getInt("StatusID"));
          return order;
        });
  }

  public boolean update(Order order) {
    String sql = "UPDATE Orders SET TotalAmount = ?, ShippingAddress = ?, " +
        "PaymentMethod = ?, StatusID = ? WHERE OrderID = ? AND UserID = ?";

    int rowsAffected = jdbcTemplate.update(sql,
        order.getTotalAmount(),
        order.getShippingAddress(),
        order.getPaymentMethod(),
        order.getStatusId(),
        order.getOrderId(),
        order.getUserId()
    );

    return rowsAffected > 0;
  }

  public boolean updateStatus(int orderId, int statusId, int userId) {
    String sql = "UPDATE Orders SET StatusID = ? WHERE OrderID = ? AND UserID = ?";

    int rowsAffected = jdbcTemplate.update(sql, statusId, orderId, userId);
    return rowsAffected > 0;
  }

  public boolean deleteById(int orderId, int userId) {
    String sql = "DELETE FROM Orders WHERE OrderID = ? AND UserID = ?";

    int rowsAffected = jdbcTemplate.update(sql, orderId, userId);
    return rowsAffected > 0;
  }
}