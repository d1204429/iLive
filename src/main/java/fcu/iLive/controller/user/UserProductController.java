package fcu.iLive.controller.user;


import fcu.iLive.service.product.RecommendedProductService;
import fcu.iLive.service.promotion.ProductPromotionService;
import fcu.iLive.model.product.RecommendedProduct;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class UserProductController {

  @Autowired
  private ProductPromotionService productPromotionService;

  @Autowired
  private RecommendedProductService RecommendedProductservice;

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

  /**
   * 熱門商品序列
   */
  @GetMapping("/recommends")
  public ResponseEntity<List<RecommendedProduct>> getRecommends() {
    try {
      List<RecommendedProduct> recommends = RecommendedProductservice.findAll();
      return new ResponseEntity<>(recommends, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 取得當前有效的優惠商品
   */
  @GetMapping("/promotions/active")
  public ResponseEntity<List<Map<String, Object>>> getActivePromotionalProducts() {
    try {
      List<Map<String, Object>> products = productPromotionService.getActivePromotionalProducts();
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}