package fcu.iLive.controller.user;


import fcu.iLive.model.product.Product;
import fcu.iLive.service.product.ProductService;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class UserProductController {

  @Autowired
  private ProductService productService;

  // 取得上架商品列表（前台展示用）
  @GetMapping
  public ResponseEntity<List<Product>> getPublishedProducts() {
    try {
      // 這裡只回傳上架商品 states = 1
      List<Product> products = productService.getAllActiveProducts();
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // 取得單一商品資訊（前台展示用）
  @GetMapping("/{id}")
  public ResponseEntity<Product> getProductDetails(@PathVariable("id") int productId) {
    try {
      Product product = productService.getProduct(productId);
      if (product != null) {
        return new ResponseEntity<>(product, HttpStatus.OK);
      } else {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // 根據分類查詢商品
  @GetMapping("/category/{categoryId}")
  public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable("categoryId") int categoryId) {
    try {
      List<Product> products = productService.getProductsByCategory(categoryId);
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // 商品搜尋功能
  // 範例 URL: /api/v1/products/search?keyword=手機&minPrice=1000&maxPrice=5000
  @GetMapping("/search")
  public ResponseEntity<List<Product>> searchProducts(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice) {
    try {
      List<Product> products = productService.searchProducts(keyword, minPrice, maxPrice);
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}