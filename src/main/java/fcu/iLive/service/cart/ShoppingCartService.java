package fcu.iLive.service.cart;

import fcu.iLive.model.cart.ShoppingCart;
import fcu.iLive.repository.cart.ShoppingCartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ShoppingCartService {

  @Autowired
  private ShoppingCartRepository shoppingCartRepository;

  // 獲取或創建購物車
  public ShoppingCart getOrCreateCart(int userId) {
    // 先查找是否已有購物車
    ShoppingCart cart = shoppingCartRepository.findByUserId(userId);

    // 如果沒有購物車，創建新的
    if (cart == null) {
      cart = new ShoppingCart();
      cart.setUserId(userId);
      cart.setCreatedAt(LocalDateTime.now());
      cart = shoppingCartRepository.save(cart);
    }

    return cart;
  }

  // 根據用戶ID查找購物車
  public ShoppingCart findByUserId(int userId) {
    return shoppingCartRepository.findByUserId(userId);
  }

  // 保存購物車
  public ShoppingCart saveCart(ShoppingCart cart) {
    return shoppingCartRepository.save(cart);
  }
}