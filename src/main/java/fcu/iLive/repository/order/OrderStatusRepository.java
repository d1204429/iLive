//訂單狀態操作

package fcu.iLive.repository.order;

import fcu.iLive.model.order.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderStatusRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public List<OrderStatus> findAll() {
    String sql = "SELECT * FROM OrderStatus";

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      OrderStatus status = new OrderStatus();
      status.setStatusId(rs.getInt("StatusID"));
      status.setStatusName(rs.getString("StatusName"));
      return status;
    });
  }

  public Optional<OrderStatus> findById(int statusId) {
    String sql = "SELECT * FROM OrderStatus WHERE StatusID = ?";

    List<OrderStatus> statuses = jdbcTemplate.query(sql,
        new Object[]{statusId},
        (rs, rowNum) -> {
          OrderStatus status = new OrderStatus();
          status.setStatusId(rs.getInt("StatusID"));
          status.setStatusName(rs.getString("StatusName"));
          return status;
        });

    return statuses.isEmpty() ? Optional.empty() : Optional.of(statuses.get(0));
  }
}