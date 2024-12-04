package fcu.iLive.controller.user;

import fcu.iLive.model.cart.CartItems;
import fcu.iLive.service.cart.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

  @Autowired
  private CartService cartService;

  // 新增商品到購物車
  @PostMapping("/{userId}/items")
  public ResponseEntity<?> addToCart(
      @PathVariable int userId,
      @RequestParam int productId,
      @RequestParam int quantity) {
    cartService.addToCart(userId, productId, quantity);
    return ResponseEntity.ok().build();
  }

  // 取得購物車商品列表
  @GetMapping("/{userId}/items")
  public ResponseEntity<List<CartItems>> getCartItems(@PathVariable int userId) {
    List<CartItems> items = cartService.getCartItems(userId);
    return ResponseEntity.ok(items);
  }

  // 更新購物車商品數量
  @PutMapping("/items/{cartItemId}")
  public ResponseEntity<?> updateQuantity(
      @PathVariable int cartItemId,
      @RequestParam int quantity) {
    cartService.updateCartItemQuantity(cartItemId, quantity);
    return ResponseEntity.ok().build();
  }

  // 刪除購物車商品
  @DeleteMapping("/items/{cartItemId}")
  public ResponseEntity<?> removeCartItem(@PathVariable int cartItemId) {
    cartService.removeCartItem(cartItemId);
    return ResponseEntity.ok().build();
  }

  // 清空購物車
  @DeleteMapping("/{cartId}")
  public ResponseEntity<?> clearCart(@PathVariable int cartId) {
    cartService.clearCartItems(cartId);
    return ResponseEntity.ok().build();
  }
}