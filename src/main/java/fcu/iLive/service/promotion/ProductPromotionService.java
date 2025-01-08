package fcu.iLive.service.promotion;

import fcu.iLive.model.product.Product;
import fcu.iLive.model.promotion.ProductPromotion;
import fcu.iLive.model.promotion.Promotion;
import fcu.iLive.repository.product.ProductRepository;
import fcu.iLive.repository.promotion.ProductPromotionRepository;
import fcu.iLive.service.product.ProductService;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class ProductPromotionService {

  @Autowired
  private ProductPromotionRepository productPromotionRepository;

  @Autowired
  private PromotionService promotionService;

  @Autowired
  private ProductRepository productRepository;

  /**
   * 取得特定活動的所有商品
   */
  public List<Map<String, Object>> getPromotionProducts(int promotionId) {
    return productPromotionRepository.findProductsByPromotionId(promotionId);
  }

  /**
   * 取得當前有效的優惠商品
   * 條件：活動狀態為有效(IsActive=1)且在有效期間內的商品
   */
  public List<Map<String, Object>> getActivePromotionalProducts() {
    return productPromotionRepository.findActivePromotionalProducts();
  }



  /**
   * 查詢商品的所有有效優惠
   */
  public List<ProductPromotion> getProductPromotions(int productId) {
    return productPromotionRepository.findByProductId(productId);
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

  /**
   * 新增商品優惠
   */
  @Transactional
  public int createProductPromotion(ProductPromotion productPromotion) {
    validateProductPromotion(productPromotion);

    Product product = productRepository.findById(productPromotion.getProductId());
    if (product == null) {
      throw new IllegalArgumentException("找不到指定商品");
    }
    BigDecimal originalPrice = product.getPrice();

    Promotion promotion = promotionService.getPromotionById(productPromotion.getPromotionId());
    if (promotion == null) {
      throw new IllegalArgumentException("找不到指定的促銷活動");
    }

    BigDecimal promotionalPrice = calculatePromotionalPrice(originalPrice, promotion);
    productPromotion.setPromotionalPrice(promotionalPrice);

    return productPromotionRepository.create(productPromotion);
  }

  /**
   * 刪除商品優惠
   */
  @Transactional
  public void deleteProductPromotion(int productPromotionId) {
    productPromotionRepository.delete(productPromotionId);
  }

  /**
   * 添加商品相關優惠價格
   */
  private Map<String, Object> appendProductPrice(Product product) {
    Map<String, Object> productInfo = new HashMap<>();
    try {
      productInfo.put("productId", product.getProductId());
      productInfo.put("name", product.getName());
      productInfo.put("description", product.getDescription());
      productInfo.put("imageUrl", product.getImageUrl());
      productInfo.put("categoryId", product.getCategoryId());
      productInfo.put("parentCategoryId", product.getParentCategoryId()); // 新增這行
      productInfo.put("brand", product.getBrand());
      productInfo.put("availableStock", product.getAvailableStock());

      BigDecimal originalPrice = product.getPrice();
      if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalStateException("商品" + product.getProductId() + "的價格無效");
      }
      productInfo.put("originalPrice", originalPrice);

      List<ProductPromotion> promotions = getProductPromotions(product.getProductId());

      if (!promotions.isEmpty()) {
        try {
          BigDecimal promotionalPrice = promotions.stream()
              .map(ProductPromotion::getPromotionalPrice)
              .filter(price -> price != null && price.compareTo(BigDecimal.ZERO) > 0)
              .filter(price -> price.compareTo(originalPrice) <= 0)
              .min(BigDecimal::compareTo)
              .orElse(originalPrice);

          if (promotionalPrice.compareTo(originalPrice.multiply(new BigDecimal("0.1"))) < 0) {
            promotionalPrice = originalPrice.multiply(new BigDecimal("0.1"));
          }

          productInfo.put("promotionalPrice", promotionalPrice);
        } catch (Exception e) {
          productInfo.put("promotionalPrice", originalPrice);
        }
      } else {
        productInfo.put("promotionalPrice", originalPrice);
      }

    } catch (Exception e) {
      log.error("計算商品{}的價格時發生錯誤: {}", product.getProductId(), e.getMessage());
      productInfo.put("originalPrice", product.getPrice());
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

  /**
   * 計算優惠價格
   */
  private BigDecimal calculatePromotionalPrice(BigDecimal originalPrice, Promotion promotion) {
    try {
      BigDecimal calculatedPrice;

      if ("PERCENTAGE".equals(promotion.getDiscountType())) {
        BigDecimal discountRate = promotion.getDiscountValue()
            .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        calculatedPrice = originalPrice.multiply(BigDecimal.ONE.subtract(discountRate));
      } else if ("FIXED_AMOUNT".equals(promotion.getDiscountType())) {
        calculatedPrice = originalPrice.subtract(promotion.getDiscountValue());
      } else {
        throw new IllegalArgumentException("不支援的折扣類型: " + promotion.getDiscountType());
      }

      BigDecimal minimumPrice = originalPrice.multiply(new BigDecimal("0.1"));
      calculatedPrice = calculatedPrice.max(minimumPrice);
      calculatedPrice = calculatedPrice.min(originalPrice);

      return calculatedPrice.setScale(0, RoundingMode.HALF_UP);
    } catch (Exception e) {
      log.error("計算優惠價格時發生錯誤", e);
      return originalPrice;
    }
  }

  /**
   * 驗證商品促銷基本資訊
   */
  private void validateProductPromotion(ProductPromotion productPromotion) {
    if (productPromotion == null) {
      throw new IllegalArgumentException("商品促銷資訊不能為空");
    }
    if (productPromotion.getProductId() <= 0) {
      throw new IllegalArgumentException("無效的商品ID");
    }
    if (productPromotion.getPromotionId() <= 0) {
      throw new IllegalArgumentException("無效的促銷活動ID");
    }
  }
}