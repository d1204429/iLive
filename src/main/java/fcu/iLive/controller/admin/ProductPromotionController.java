package fcu.iLive.controller.admin;

import fcu.iLive.model.promotion.ProductPromotion;
import fcu.iLive.service.promotion.ProductPromotionService;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/product-promotions")
public class ProductPromotionController {

  @Autowired
  private ProductPromotionService productPromotionService;

  /**
   * 取得商品的所有優惠
   */
  @GetMapping("/product/{productId}")
  public ResponseEntity<List<ProductPromotion>> getProductPromotions(
          @PathVariable("productId") int productId) {
    try {
      log.info("獲取商品ID:{}的促銷資訊", productId);
      List<ProductPromotion> promotions = productPromotionService.getProductPromotions(productId);
      return new ResponseEntity<>(promotions, HttpStatus.OK);
    } catch (Exception e) {
      log.error("獲取商品促銷資訊失敗", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 取得所有促銷商品
   */
  @GetMapping("/products/promotional")
  public ResponseEntity<List<Map<String, Object>>> getPromotionalProducts() {
    try {
      log.info("獲取所有促銷商品");
      List<Map<String, Object>> products = productPromotionService.getAllActiveProductsWithPrices();
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      log.error("獲取促銷商品列表失敗", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // ProductPromotionController.java
  @GetMapping("/promotion/{promotionId}/products")
  public ResponseEntity<List<Map<String, Object>>> getPromotionProducts(
      @PathVariable("promotionId") int promotionId) {
    try {
      log.info("獲取活動ID:{}的所有商品", promotionId);
      List<Map<String, Object>> products = productPromotionService.getPromotionProducts(promotionId);
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      log.error("獲取活動商品列表失敗", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


  /**
   * 取得所有商品促銷
   */
  @GetMapping("/products")
  public ResponseEntity<List<Map<String, Object>>> getAllProducts() {
    try {
      log.info("獲取所有商品促銷資訊");
      List<Map<String, Object>> products = productPromotionService.getAllActiveProductsWithPrices();
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      log.error("獲取商品促銷列表失敗", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 新增商品優惠
   */
  @PostMapping
  public ResponseEntity<Integer> createProductPromotion(@RequestBody ProductPromotion productPromotion) {
    try {
      log.info("建立商品促銷: {}", productPromotion);
      int newId = productPromotionService.createProductPromotion(productPromotion);
      return new ResponseEntity<>(newId, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      log.error("建立商品促銷參數錯誤: {}", e.getMessage());
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("建立商品促銷失敗", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 刪除商品優惠
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProductPromotion(@PathVariable("id") int productPromotionId) {
    try {
      log.info("刪除商品促銷, ID: {}", productPromotionId);
      productPromotionService.deleteProductPromotion(productPromotionId);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      log.error("刪除商品促銷失敗", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 取得所有商品促銷資訊
   */
  @GetMapping
  public ResponseEntity<List<Map<String, Object>>> getAllProductPromotions() {
    try {
      log.info("獲取所有商品促銷資訊");
      List<Map<String, Object>> products = productPromotionService.getAllActiveProductsWithPrices();
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      log.error("獲取商品促銷列表失敗", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
