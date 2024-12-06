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

  // 根據ID查找購物車項目
  public CartItems findById(int cartItemId) {
    List<CartItems> items = jdbcTemplate.query(
        "SELECT * FROM CartItems WHERE CartItemId = ?",
        new BeanPropertyRowMapper<>(CartItems.class),
        cartItemId
    );
    return items.isEmpty() ? null : items.get(0);
  }

  // 根據購物車ID和商品ID查找項目
  public CartItems findByCartIdAndProductId(int cartId, int productId) {
    List<CartItems> items = jdbcTemplate.query(
        "SELECT * FROM CartItems WHERE CartId = ? AND ProductId = ?",
        new BeanPropertyRowMapper<>(CartItems.class),
        cartId, productId
    );
    return items.isEmpty() ? null : items.get(0);
  }

  // 獲取購物車的所有項目
  public List<CartItems> findByCartId(int cartId) {
    return jdbcTemplate.query(
        "SELECT ci.*, p.Name AS ProductName, p.Price " +
            "FROM CartItems ci " +
            "JOIN Products p ON ci.ProductId = p.ProductId " +
            "WHERE ci.CartId = ?",
        new BeanPropertyRowMapper<>(CartItems.class),
        cartId
    );
  }

  // 新增購物車項目
  public void save(CartItems item) {
    jdbcTemplate.update(
        "INSERT INTO CartItems (CartId, ProductId, Quantity, CreatedAt) VALUES (?, ?, ?, ?)",
        item.getCartId(),
        item.getProductId(),
        item.getQuantity(),
        item.getCreatedAt()
    );
  }

  // 更新購物車項目數量
  public void update(CartItems item) {
    jdbcTemplate.update(
        "UPDATE CartItems SET Quantity = ?, UpdatedAt = CURRENT_TIMESTAMP WHERE CartItemId = ?",
        item.getQuantity(),
        item.getCartItemId()
    );
  }

  // 刪除購物車項目
  public void delete(int cartItemId) {
    jdbcTemplate.update(
        "DELETE FROM CartItems WHERE CartItemId = ?",
        cartItemId
    );
  }

  // 清空購物車所有項目
  public void deleteAllByCartId(int cartId) {
    jdbcTemplate.update(
        "DELETE FROM CartItems WHERE CartId = ?",
        cartId
    );
  }
}