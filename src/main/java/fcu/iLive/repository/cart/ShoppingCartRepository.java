package fcu.iLive.repository.cart;

import fcu.iLive.model.cart.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ShoppingCartRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  // 根據用戶ID查找購物車
  public ShoppingCart findByUserId(int userId) {
    List<ShoppingCart> carts = jdbcTemplate.query(
        "SELECT * FROM ShoppingCart WHERE UserId = ?",  // 修改表名和欄位名
        new BeanPropertyRowMapper<>(ShoppingCart.class),
        userId
    );
    return carts.isEmpty() ? null : carts.get(0);
  }

  // 根據購物車ID查找購物車
  public ShoppingCart findById(int cartId) {
    List<ShoppingCart> carts = jdbcTemplate.query(
        "SELECT * FROM ShoppingCart WHERE CartId = ?",  // 修改表名和欄位名
        new BeanPropertyRowMapper<>(ShoppingCart.class),
        cartId
    );
    return carts.isEmpty() ? null : carts.get(0);
  }

  // 保存購物車
  public ShoppingCart save(ShoppingCart cart) {
    if (cart.getCartId() > 0) {
      // 更新現有購物車
      jdbcTemplate.update(
          "UPDATE ShoppingCart SET UserId=?, CreatedAt=? WHERE CartId=?",  // 修改表名和欄位名
          cart.getUserId(),
          cart.getCreatedAt(),
          cart.getCartId()
      );
    } else {
      // 創建新購物車
      jdbcTemplate.update(
          "INSERT INTO ShoppingCart (UserId, CreatedAt) VALUES (?,?)",  // 修改表名和欄位名
          cart.getUserId(),
          cart.getCreatedAt()
      );

      // 獲取新創建的購物車ID
      List<ShoppingCart> newCart = jdbcTemplate.query(
          "SELECT * FROM ShoppingCart WHERE UserId = ? ORDER BY CartId DESC LIMIT 1",  // 修改表名和欄位名
          new BeanPropertyRowMapper<>(ShoppingCart.class),
          cart.getUserId()
      );
      if (!newCart.isEmpty()) {
        cart.setCartId(newCart.get(0).getCartId());
      }
    }
    return cart;
  }
}