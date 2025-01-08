package fcu.iLive.service.product;

import fcu.iLive.model.product.Product;
import fcu.iLive.model.promotion.ProductPromotion;
import fcu.iLive.repository.product.ProductRepository;
import fcu.iLive.service.promotion.ProductPromotionService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProductService {

  @Autowired
  private ProductRepository productRepository;

  @Transactional
  public Product createProduct(Product product) {
    //product.setLockedStock(0);
    product.setStatus(1);
    return productRepository.save(product);
  }

  public Product getProduct(int productId) {
    return productRepository.findById(productId);
  }

  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  public List<Product> getAllActiveProducts() {
    return productRepository.findAllActive();
  }

  @Transactional
  public void updateProduct(Product product) {
    Product existingProduct = productRepository.findById(product.getProductId());
    if (existingProduct == null) {
      throw new RuntimeException("Product not found");
    }
    //product.setLockedStock(existingProduct.getLockedStock());
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

  public int getAvailableStock(int productId) {
    Product product = productRepository.findById(productId);
    return product != null ? product.getAvailableStock() : 0;
  }
}