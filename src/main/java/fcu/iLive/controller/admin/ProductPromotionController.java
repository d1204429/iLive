package fcu.iLive.controller.admin;

import fcu.iLive.model.promotion.ProductPromotion;
import fcu.iLive.service.promotion.ProductPromotionService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
      List<ProductPromotion> promotions = productPromotionService.getProductPromotions(productId);
      return new ResponseEntity<>(promotions, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 取得所有促銷商品
   */
  @GetMapping("/products/promotional")
  public ResponseEntity<List<Map<String, Object>>> getPromotionalProducts() {
    try {
      List<Map<String, Object>> products = productPromotionService.getAllActiveProductsWithPrices();
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 取得所有商品促銷
   */
  @GetMapping("/products")
  public ResponseEntity<List<Map<String, Object>>> getAllProducts() {
    try {
      List<Map<String, Object>> products = productPromotionService.getAllActiveProductsWithPrices();
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 新增商品優惠
   */
  @PostMapping
  public ResponseEntity<Integer> createProductPromotion(@RequestBody ProductPromotion productPromotion) {
    try {
      int newId = productPromotionService.createProductPromotion(productPromotion);
      return new ResponseEntity<>(newId, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 刪除商品優惠
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProductPromotion(@PathVariable("id") int productPromotionId) {
    try {
      productPromotionService.deleteProductPromotion(productPromotionId);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
