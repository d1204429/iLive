package fcu.iLive.service.cart;

import fcu.iLive.model.cart.CartItems;
import fcu.iLive.model.cart.ShoppingCart;
import fcu.iLive.model.product.Product;
import fcu.iLive.repository.cart.CartItemsRepository;
import fcu.iLive.repository.cart.ShoppingCartRepository;
import fcu.iLive.repository.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService {

  @Autowired
  private ShoppingCartRepository shoppingCartRepository;

  @Autowired
  private CartItemsRepository cartItemsRepository;

  @Autowired
  private ProductRepository productRepository;  // 加入這行


  // 確保用戶有購物車並返回購物車ID
  private int ensureUserHasCart(int userId) {
    ShoppingCart cart = shoppingCartRepository.findByUserId(userId);
    if (cart == null) {
      // 創建新購物車
      cart = new ShoppingCart();
      cart.setUserId(userId);
      cart.setCreatedAt(LocalDateTime.now());
      cart = shoppingCartRepository.save(cart);
    }
    return cart.getCartId();
  }

  // 添加商品到購物車
  @Transactional
  public void addToCart(int userId, int productId, int quantity) {
    // 檢查商品庫存
    Product product = productRepository.findById(productId);
    if (product == null) {
      throw new RuntimeException("商品不存在");
    }

    // 檢查可用庫存
    if (product.getAvailableStock() < quantity) {
      throw new RuntimeException(
          String.format("商品「%s」庫存不足，剩餘%d件",
              product.getName(),
              product.getAvailableStock())
      );
    }

    // 現有購物車項目檢查
    int cartId = ensureUserHasCart(userId);
    CartItems existingItem = cartItemsRepository.findByCartIdAndProductId(cartId, productId);

    if (existingItem != null) {
      // 檢查增加數量後是否超過可用庫存
      int newQuantity = existingItem.getQuantity() + quantity;
      if (product.getAvailableStock() < newQuantity) {
        throw new RuntimeException(
            String.format("商品「%s」庫存不足，剩餘%d件",
                product.getName(),
                product.getAvailableStock())
        );
      }
      existingItem.setQuantity(newQuantity);
      cartItemsRepository.update(existingItem);
    } else {
      CartItems newItem = new CartItems();
      newItem.setCartId(cartId);
      newItem.setProductId(productId);
      newItem.setQuantity(quantity);
      newItem.setCreatedAt(LocalDateTime.now());
      cartItemsRepository.save(newItem);
    }
  }

  // 獲取購物車中的所有商品
  public List<CartItems> getCartItems(int userId) {
    int cartId = ensureUserHasCart(userId);
    return cartItemsRepository.findByCartId(cartId);
  }

  // 更新購物車商品數量
  @Transactional
  public void updateCartItemQuantity(int userId, int cartItemId, int quantity) {
    CartItems item = cartItemsRepository.findById(cartItemId);
    if (item == null) {
      throw new RuntimeException("購物車項目不存在");
    }

    // 檢查該商品是否屬於用戶的購物車
    ShoppingCart cart = shoppingCartRepository.findByUserId(userId);
    if (cart == null || item.getCartId() != cart.getCartId()) {
      throw new RuntimeException("無權操作此購物車項目");
    }

    item.setQuantity(quantity);
    cartItemsRepository.update(item);
  }

  // 從購物車中移除商品
  @Transactional
  public void removeCartItem(int userId, int cartItemId) {
    CartItems item = cartItemsRepository.findById(cartItemId);
    if (item == null) {
      throw new RuntimeException("購物車項目不存在");
    }

    // 檢查該商品是否屬於用戶的購物車
    ShoppingCart cart = shoppingCartRepository.findByUserId(userId);
    if (cart == null || item.getCartId() != cart.getCartId()) {
      throw new RuntimeException("無權操作此購物車項目");
    }

    cartItemsRepository.delete(cartItemId);
  }

  // 清空購物車
  @Transactional
  public void clearCartItems(int userId, int cartId) {
    ShoppingCart cart = shoppingCartRepository.findByUserId(userId);
    if (cart == null || cart.getCartId() != cartId) {
      throw new RuntimeException("無權操作此購物車");
    }

    cartItemsRepository.deleteAllByCartId(cartId);
  }
}