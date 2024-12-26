package fcu.iLive.controller.user;

import lombok.extern.slf4j.Slf4j;
import fcu.iLive.model.product.Product;
import fcu.iLive.service.product.ProductService;
import fcu.iLive.service.promotion.ProductPromotionService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/v1/products")
public class UserProductController {

  @Autowired
  private ProductPromotionService productPromotionService;
  private ProductService productService;

  /**
   * 取得上架商品列表（含優惠價格）
   */
  @GetMapping
  public ResponseEntity<List<Map<String, Object>>> getPublishedProducts() {
    try {
      List<Map<String, Object>> products = productPromotionService.getAllActiveProductsWithPrices();
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 取得單一商品詳細資訊（含優惠價格）
   */
  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getProductDetails(@PathVariable("id") int productId) {
    try {
      Map<String, Object> product = productPromotionService.getProductWithPrice(productId);
      if (product != null) {
        return new ResponseEntity<>(product, HttpStatus.OK);
      } else {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 取得推薦商品列表
   */
  @GetMapping("/recommendations")
  public ResponseEntity<List<Product>> getRecommendedProducts() {
    try {
      List<Product> recommendations = productService.getRecommendedProducts();
      return new ResponseEntity<>(recommendations, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 更新推薦商品列表
   */
  @PostMapping("/recommendations")
  public ResponseEntity<Void> updateRecommendedProducts(@RequestBody List<Integer> productIds) {
    try {
      productService.updateRecommendedProducts(productIds);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      // 加入錯誤日誌
      log.error("更新推薦商品時發生錯誤: ", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 根據分類查詢商品（含優惠價格）
   */
  @GetMapping("/category/{categoryId}")
  public ResponseEntity<List<Map<String, Object>>> getProductsByCategory(
      @PathVariable("categoryId") int categoryId) {
    try {
      List<Map<String, Object>> products =
          productPromotionService.getProductsByCategoryWithPrices(categoryId);
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 商品搜尋（含優惠價格）
   */
  @GetMapping("/search")
  public ResponseEntity<List<Map<String, Object>>> searchProducts(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice) {
    try {
      List<Map<String, Object>> products =
          productPromotionService.searchProductsWithPrices(keyword, minPrice, maxPrice);
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}