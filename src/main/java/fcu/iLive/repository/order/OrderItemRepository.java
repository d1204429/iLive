//訂單項目資料操作

package fcu.iLive.repository.order;

import fcu.iLive.model.order.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderItemRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public OrderItem save(OrderItem orderItem) {
    String sql = "INSERT INTO OrderItems (OrderID, ProductID, Quantity, Price) VALUES (?, ?, ?, ?)";

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setInt(1, orderItem.getOrderId());
      ps.setInt(2, orderItem.getProductId());
      ps.setInt(3, orderItem.getQuantity());
      ps.setBigDecimal(4, orderItem.getPrice());
      return ps;
    }, keyHolder);

    orderItem.setOrderItemId(keyHolder.getKey().intValue());
    return orderItem;
  }

  public List<OrderItem> findByOrderId(int orderId) {
    String sql = "SELECT oi.*, p.ProductName FROM OrderItems oi " +
        "JOIN Products p ON oi.ProductID = p.ProductID " +
        "WHERE oi.OrderID = ?";

    return jdbcTemplate.query(sql,
        new Object[]{orderId},
        (rs, rowNum) -> {
          OrderItem orderItem = new OrderItem();
          orderItem.setOrderItemId(rs.getInt("OrderItemID"));
          orderItem.setOrderId(rs.getInt("OrderID"));
          orderItem.setProductId(rs.getInt("ProductID"));
          orderItem.setQuantity(rs.getInt("Quantity"));
          orderItem.setPrice(rs.getBigDecimal("Price"));
          return orderItem;
        });
  }

  public Optional<OrderItem> findById(int orderItemId) {
    String sql = "SELECT oi.*, p.ProductName FROM OrderItems oi " +
        "JOIN Products p ON oi.ProductID = p.ProductID " +
        "WHERE oi.OrderItemID = ?";

    List<OrderItem> items = jdbcTemplate.query(sql,
        new Object[]{orderItemId},
        (rs, rowNum) -> {
          OrderItem orderItem = new OrderItem();
          orderItem.setOrderItemId(rs.getInt("OrderItemID"));
          orderItem.setOrderId(rs.getInt("OrderID"));
          orderItem.setProductId(rs.getInt("ProductID"));
          orderItem.setQuantity(rs.getInt("Quantity"));
          orderItem.setPrice(rs.getBigDecimal("Price"));
          return orderItem;
        });

    return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
  }

  public boolean update(OrderItem orderItem) {
    String sql = "UPDATE OrderItems SET Quantity = ?, Price = ? " +
        "WHERE OrderItemID = ? AND OrderID = ?";

    int rowsAffected = jdbcTemplate.update(sql,
        orderItem.getQuantity(),
        orderItem.getPrice(),
        orderItem.getOrderItemId(),
        orderItem.getOrderId()
    );

    return rowsAffected > 0;
  }

  public boolean deleteById(int orderItemId, int orderId) {
    String sql = "DELETE FROM OrderItems WHERE OrderItemID = ? AND OrderID = ?";

    int rowsAffected = jdbcTemplate.update(sql, orderItemId, orderId);
    return rowsAffected > 0;
  }
}