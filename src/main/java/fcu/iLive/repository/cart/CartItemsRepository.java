package fcu.iLive.repository.cart;

import fcu.iLive.model.cart.CartItems;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CartItemsRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public List<CartItems> findByCartId(int cartId) {
    return jdbcTemplate.query(
        "SELECT * FROM cart_items WHERE cart_id = ?",
        new BeanPropertyRowMapper<>(CartItems.class),
        cartId
    );
  }

  public CartItems findByCartIdAndProductId(int cartId, int productId) {
    List<CartItems> items = jdbcTemplate.query(
        "SELECT * FROM cart_items WHERE cart_id = ? AND product_id = ?",
        new BeanPropertyRowMapper<>(CartItems.class),
        cartId, productId
    );
    return items.isEmpty() ? null : items.get(0);
  }

  public CartItems findById(int cartItemId) {
    List<CartItems> items = jdbcTemplate.query(
        "SELECT * FROM cart_items WHERE cart_item_id = ?",
        new BeanPropertyRowMapper<>(CartItems.class),
        cartItemId
    );
    return items.isEmpty() ? null : items.get(0);
  }

  public CartItems save(CartItems item) {
    if (item.getCartItemId() > 0) {
      jdbcTemplate.update(
          "UPDATE cart_items SET cart_id=?, product_id=?, quantity=?, created_at=?, updated_at=? WHERE cart_item_id=?",
          item.getCartId(), item.getProductId(), item.getQuantity(),
          item.getCreatedAt(), item.getUpdatedAt(), item.getCartItemId()
      );
    } else {
      jdbcTemplate.update(
          "INSERT INTO cart_items (cart_id, product_id, quantity, created_at, updated_at) VALUES (?,?,?,?,?)",
          item.getCartId(), item.getProductId(), item.getQuantity(),
          item.getCreatedAt(), item.getUpdatedAt()
      );
    }
    return item;
  }

  public void deleteAllByCartId(int cartId) {
    jdbcTemplate.update("DELETE FROM cart_items WHERE cart_id = ?", cartId);
  }

  public void deleteById(int cartItemId) {
    jdbcTemplate.update("DELETE FROM cart_items WHERE cart_item_id = ?", cartItemId);
  }
}