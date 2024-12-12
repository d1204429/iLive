package fcu.iLive.service.product;

import fcu.iLive.model.product.Product;
import fcu.iLive.repository.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

  @Autowired
  private ProductRepository productRepository;

  @Transactional
  public Product createProduct(Product product) {
    // 確保新商品的 lockedStock 為 0
    product.setLockedStock(0);
    return productRepository.save(product);
  }

  public Product getProduct(int productId) {
    return productRepository.findById(productId);
  }

  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  @Transactional
  public void updateProduct(Product product) {
    // 檢查商品是否存在
    Product existingProduct = productRepository.findById(product.getProductId());
    if (existingProduct == null) {
      throw new RuntimeException("Product not found");
    }

    // 保持現有的 lockedStock 值不變
    product.setLockedStock(existingProduct.getLockedStock());
    productRepository.update(product);
  }

  @Transactional
  public void deleteProduct(int productId) {
    productRepository.delete(productId);
  }

  public List<Product> getProductsByCategory(int categoryId) {
    return productRepository.findByCategory(categoryId);
  }

  public List<Product> searchProducts(String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
    return productRepository.search(keyword, minPrice, maxPrice);
  }

  // 獲取可用庫存（總庫存 - 鎖定庫存）
  public int getAvailableStock(int productId) {
    Product product = productRepository.findById(productId);
    return product != null ? product.getAvailableStock() : 0;
  }
}