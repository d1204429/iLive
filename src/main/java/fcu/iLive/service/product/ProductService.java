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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private ProductPromotionService productPromotionService;

  @Transactional
  public Product createProduct(Product product) {
    product.setLockedStock(0);
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

  /**
   * 取得上架商品列表(含優惠價)
   */
  public List<Map<String, Object>> getAllActiveProductsWithPrices() {
    List<Product> products = productRepository.findAllActive();
    return convertToProductsWithPrices(products);
  }

  /**
   * 取得單一商品(含優惠價)
   */
  public Map<String, Object> getProductWithPrice(int productId) {
    Product product = productRepository.findById(productId);
    if (product == null) {
      return null;
    }
    return appendProductPrice(product);
  }

  /**
   * 取得分類商品列表(含優惠價)
   */
  public List<Map<String, Object>> getProductsByCategoryWithPrices(int categoryId) {
    List<Product> products = productRepository.findByCategory(categoryId);
    return convertToProductsWithPrices(products);
  }

  /**
   * 商品搜尋(含優惠價)
   */
  public List<Map<String, Object>> searchProductsWithPrices(String keyword, BigDecimal minPrice,
      BigDecimal maxPrice) {
    List<Product> products = productRepository.search(keyword, minPrice, maxPrice);
    return convertToProductsWithPrices(products);
  }

  @Transactional
  public void updateProduct(Product product) {
    Product existingProduct = productRepository.findById(product.getProductId());
    if (existingProduct == null) {
      throw new RuntimeException("Product not found");
    }
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

  public int getAvailableStock(int productId) {
    Product product = productRepository.findById(productId);
    return product != null ? product.getAvailableStock() : 0;
  }

  /**
   * 添加商品相關優惠價格
   */
  private Map<String, Object> appendProductPrice(Product product) {
    Map<String, Object> productInfo = new HashMap<>();
    productInfo.put("productId", product.getProductId());
    productInfo.put("name", product.getName());
    productInfo.put("description", product.getDescription());
    productInfo.put("imageUrl", product.getImageUrl());
    productInfo.put("categoryId", product.getCategoryId());
    productInfo.put("brand", product.getBrand());
    productInfo.put("availableStock", product.getAvailableStock());

    // 價格資訊
    productInfo.put("originalPrice", product.getPrice());

    // 優惠價格
    List<ProductPromotion> promotions =
        productPromotionService.getProductPromotions(product.getProductId());

    if (!promotions.isEmpty()) {
      BigDecimal promotionalPrice = promotions.stream()
          .map(ProductPromotion::getPromotionalPrice)
          .min(BigDecimal::compareTo)
          .orElse(product.getPrice());
      productInfo.put("promotionalPrice", promotionalPrice);
    } else {
      productInfo.put("promotionalPrice", product.getPrice());
    }

    return productInfo;
  }

  /**
   * 轉換商品列表價格資訊
   */
  private List<Map<String, Object>> convertToProductsWithPrices(List<Product> products) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (Product product : products) {
      result.add(appendProductPrice(product));
    }
    return result;
  }
}