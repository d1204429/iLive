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

  public ShoppingCart findByUserId(int userId) {
    List<ShoppingCart> carts = jdbcTemplate.query(
        "SELECT * FROM shopping_cart WHERE user_id = ?",
        new BeanPropertyRowMapper<>(ShoppingCart.class),
        userId
    );
    return carts.isEmpty() ? null : carts.get(0);
  }

  public ShoppingCart save(ShoppingCart cart) {
    if (cart.getCartId() > 0) {
      jdbcTemplate.update(
          "UPDATE shopping_cart SET user_id=?, created_at=? WHERE cart_id=?",
          cart.getUserId(), cart.getCreatedAt(), cart.getCartId()
      );
    } else {
      jdbcTemplate.update(
          "INSERT INTO shopping_cart (user_id, created_at) VALUES (?,?)",
          cart.getUserId(), cart.getCreatedAt()
      );
      // 獲取新插入的購物車ID
      List<ShoppingCart> newCart = jdbcTemplate.query(
          "SELECT * FROM shopping_cart WHERE user_id = ? ORDER BY cart_id DESC LIMIT 1",
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