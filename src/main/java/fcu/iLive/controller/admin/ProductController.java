//商品管理 API (/admin/products/**)

// 後台商品管理控制器
package fcu.iLive.controller.admin;

import fcu.iLive.model.product.Product;
import fcu.iLive.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@RequestMapping("/api/v1/admin/products")  // 後台
@RequestMapping("/products")  // 移除 /api/v1 前綴
public class ProductController {

  @Autowired
  private ProductService productService;

  // 建立新商品
  @PostMapping
  public ResponseEntity<Product> createProduct(@RequestBody Product product) {
    try {
      Product createdProduct = productService.createProduct(product);
      return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // 取得單一商品詳細資訊
  @GetMapping("/{id}")
  public ResponseEntity<Product> getProduct(@PathVariable("id") int productId) {
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

  // 取得所有商品列表（後台管理用）
  @GetMapping
  public ResponseEntity<List<Product>> getAllProducts() {
    try {
      List<Product> products = productService.getAllProducts();
      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // 更新商品資訊
  @PutMapping("/{id}")
  public ResponseEntity<Void> updateProduct(@PathVariable("id") int productId,
      @RequestBody Product product) {
    try {
      product.setProductId(productId);
      productService.updateProduct(product);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // 刪除商品
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable("id") int productId) {
    try {
      productService.deleteProduct(productId);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
