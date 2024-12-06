package fcu.iLive.controller.user;

import fcu.iLive.exception.BusinessException;
import fcu.iLive.model.cart.CartItems;
import fcu.iLive.model.cart.ShoppingCart;
import fcu.iLive.service.cart.CartService;
import fcu.iLive.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 購物車控制器
 * 處理與購物車相關的所有HTTP請求
 */
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

  @Autowired
  private CartService cartService; // 購物車服務

  @Autowired
  private JwtUtil jwtUtil; // JWT工具類

  /**
   * 從請求中取得用戶ID
   * @param request HTTP請求對象
   * @return 用戶ID
   * @throws BusinessException 當驗證令牌無效或缺失時拋出異常
   */
  private int getUserIdFromRequest(HttpServletRequest request) {
    // 檢查Authorization標頭是否存在
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null) {
      throw new BusinessException("未提供驗證令牌");
    }

    // 從標頭中解析Token
    String token = jwtUtil.getTokenFromHeader(authHeader);
    if (token == null) {
      throw new BusinessException("無效的驗證令牌格式");
    }

    // 直接從Token中取得用戶ID
    return jwtUtil.getUserIdFromToken(token);
  }

  /**
   * 獲取購物車中的所有商品
   * @param request HTTP請求對象
   * @return 購物車中的商品列表
   */
  @GetMapping("/items")
  public ResponseEntity<?> getCartItems(HttpServletRequest request) {
    try {
      int userId = getUserIdFromRequest(request);
      List<CartItems> items = cartService.getCartItems(userId);
      return ResponseEntity.ok(items);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  /**
   * 添加商品到購物車
   * @param request 包含商品ID和數量的請求體
   * @param httpRequest HTTP請求對象
   * @return 添加成功返回200 OK，失敗返回400
   */
  @PostMapping("/items/add")
  public ResponseEntity<?> addToCart(
      @RequestBody CartItemRequest request,
      HttpServletRequest httpRequest) {
    try {
      int userId = getUserIdFromRequest(httpRequest);
      cartService.addToCart(userId, request.getProductId(), request.getQuantity());
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  /**
   * 更新購物車中商品的數量
   * @param cartItemId 購物車項目ID
   * @param request 包含新數量的請求體
   * @param httpRequest HTTP請求對象
   * @return 更新成功返回200 OK，失敗返回400
   */
  @PutMapping("/items/{cartItemId}")
  public ResponseEntity<?> updateQuantity(
      @PathVariable int cartItemId,
      @RequestBody CartItemRequest request,
      HttpServletRequest httpRequest) {
    try {
      int userId = getUserIdFromRequest(httpRequest);
      cartService.updateCartItemQuantity(userId, cartItemId, request.getQuantity());
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  /**
   * 從購物車中移除指定商品
   * @param cartItemId 購物車項目ID
   * @param request HTTP請求對象
   * @return 移除成功返回200 OK，失敗返回400
   */
  @DeleteMapping("/items/{cartItemId}")
  public ResponseEntity<?> removeCartItem(
      @PathVariable int cartItemId,
      HttpServletRequest request) {
    try {
      int userId = getUserIdFromRequest(request);
      cartService.removeCartItem(userId, cartItemId);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  /**
   * 清空指定購物車的所有商品
   * @param cartId 購物車ID
   * @param request HTTP請求對象
   * @return 清空成功返回200 OK，失敗返回400
   */
  @DeleteMapping("/{cartId}")
  public ResponseEntity<?> clearCart(
      @PathVariable int cartId,
      HttpServletRequest request) {
    try {
      int userId = getUserIdFromRequest(request);
      cartService.clearCartItems(userId, cartId);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}

/**
 * 購物車項目請求物件
 * 用於處理購物車相關的請求，包含商品新增和數量更新
 */
class CartItemRequest {
  private int productId;  // 商品ID
  private int quantity;   // 商品數量

  /**
   * 獲取商品ID
   * @return 商品ID
   */
  public int getProductId() {
    return productId;
  }

  /**
   * 設置商品ID
   * @param productId 商品ID
   */
  public void setProductId(int productId) {
    this.productId = productId;
  }

  /**
   * 獲取商品數量
   * @return 商品數量
   */
  public int getQuantity() {
    return quantity;
  }

  /**
   * 設置商品數量
   * @param quantity 商品數量
   */
  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
}