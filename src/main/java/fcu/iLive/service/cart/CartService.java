package fcu.iLive.service.cart;

import fcu.iLive.exception.BusinessException;
import fcu.iLive.model.cart.ShoppingCart;
import fcu.iLive.model.cart.CartItems;
import fcu.iLive.model.product.Product;
import fcu.iLive.repository.cart.ShoppingCartRepository;
import fcu.iLive.repository.cart.CartItemsRepository;
import fcu.iLive.repository.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService {

  @Autowired
  private ShoppingCartRepository cartRepository;

  @Autowired
  private CartItemsRepository cartItemsRepository;

  @Autowired
  private ProductRepository productRepository;

  /**
   * 獲取或創建購物車
   */
  @Transactional
  public ShoppingCart getOrCreateCart(int userId) {
    ShoppingCart cart = cartRepository.findByUserId(userId);
    if (cart == null) {
      cart = new ShoppingCart();
      cart.setUserId(userId);
      cart.setCreatedAt(LocalDateTime.now());
      cart = cartRepository.save(cart);
    }
    return cart;
  }

  /**
   * 添加商品到購物車
   */
  @Transactional
  public void addToCart(int userId, int productId, int quantity) {
    // 檢查商品
    Product product = productRepository.findById(productId);
    if (product == null) {
      throw new BusinessException("商品不存在");
    }

    // 檢查數量
    if (quantity < 1) {
      throw new BusinessException("商品數量不能小於1");
    }

    // 檢查庫存
    if (product.getStock() < quantity) {
      throw new BusinessException("商品庫存不足");
    }

    // 獲取購物車
    ShoppingCart cart = getOrCreateCart(userId);
    CartItems cartItem = cartItemsRepository.findByCartIdAndProductId(cart.getCartId(), productId);

    if (cartItem == null) {
      // 新增購物車項目
      cartItem = new CartItems();
      cartItem.setCartId(cart.getCartId());
      cartItem.setProductId(productId);
      cartItem.setQuantity(quantity);
      cartItem.setCreatedAt(LocalDateTime.now());
      cartItem.setUpdatedAt(LocalDateTime.now());
    } else {
      // 更新數量
      int newQuantity = cartItem.getQuantity() + quantity;
      if (product.getStock() < newQuantity) {
        throw new BusinessException("商品庫存不足");
      }
      cartItem.setQuantity(newQuantity);
      cartItem.setUpdatedAt(LocalDateTime.now());
    }

    cartItemsRepository.save(cartItem);
  }

  /**
   * 更新購物車項目數量
   */
  @Transactional
  public void updateCartItemQuantity(int cartItemId, int quantity) {
    // 1. 先檢查購物車項目是否存在
    CartItems cartItem = cartItemsRepository.findById(cartItemId);
    if (cartItem == null) {
      throw new BusinessException("購物車項目不存在");
    }

    // 2. 檢查修改後的數量是否合法（>=1）
    if (quantity < 1) {
      throw new BusinessException("商品數量不能小於1");
    }

    // 3. 送出後檢查庫存是否足夠
    Product product = productRepository.findById(cartItem.getProductId());
    if (product.getStock() < quantity) {
      throw new BusinessException("商品庫存不足");
    }

    // 4. 都通過後才更新數量
    cartItem.setQuantity(quantity);
    cartItem.setUpdatedAt(LocalDateTime.now());
    cartItemsRepository.save(cartItem);
  }

  /**
   * 刪除單個購物車項目
   */
  @Transactional
  public void removeCartItem(int cartItemId) {
    CartItems cartItem = cartItemsRepository.findById(cartItemId);
    if (cartItem == null) {
      throw new BusinessException("購物車項目不存在");
    }
    cartItemsRepository.deleteById(cartItemId);
  }

  /**
   * 獲取購物車所有商品
   */
  public List<CartItems> getCartItems(int userId) {
    ShoppingCart cart = getOrCreateCart(userId);
    return cartItemsRepository.findByCartId(cart.getCartId());
  }

  /**
   * 清空購物車
   */
  @Transactional
  public void clearCartItems(int cartId) {
    cartItemsRepository.deleteAllByCartId(cartId);
  }
}