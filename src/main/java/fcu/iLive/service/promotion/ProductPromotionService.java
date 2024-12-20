package fcu.iLive.service.promotion;

import fcu.iLive.model.product.Product;
import fcu.iLive.model.promotion.ProductPromotion;
import fcu.iLive.model.promotion.Promotion;
import fcu.iLive.repository.promotion.ProductPromotionRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductPromotionService {

  @Autowired
  private ProductPromotionRepository productPromotionRepository;

  @Autowired
  private PromotionService promotionService;

  /**
   * 查詢商品的所有有效優惠
   *
   * @param productId 商品ID
   * @return List<ProductPromotion> 商品優惠列表
   */
  public List<ProductPromotion> getProductPromotions(Integer productId) {
    return productPromotionRepository.findByProductId(productId);
  }

  /**
   * 新增商品優惠
   *
   * @param productPromotion 商品優惠資訊
   * @return Integer 新商品優惠ID
   */
  @Transactional
  public Integer createProductPromotion(ProductPromotion productPromotion) {
    validatePromotionalPrice(productPromotion.getPromotionalPrice());
    return productPromotionRepository.create(productPromotion);
  }

  /**
   * 更新商品優惠價格
   *
   * @param productPromotionId 商品優惠ID
   * @param newPrice          新優惠價格
   */
  @Transactional
  public void updatePromotionalPrice(Integer productPromotionId, BigDecimal newPrice) {
    validatePromotionalPrice(newPrice);
    productPromotionRepository.updatePrice(productPromotionId, newPrice);
  }

  /**
   * 刪除商品優惠
   *
   * @param productPromotionId 商品優惠ID
   */
  @Transactional
  public void deleteProductPromotion(Integer productPromotionId) {
    productPromotionRepository.delete(productPromotionId);
  }

  /**
   * 計算商品的價格資訊
   *
   * @param product 商品
   * @return Map 價格資訊，包含原價和優惠價等
   */
  public Map<String, Object> calculatePrices(Product product) {
    Map<String, Object> priceInfo = new HashMap<>();
    BigDecimal originalPrice = product.getPrice();

    // 設置基本價格資訊
    priceInfo.put("originalPrice", originalPrice);

    // 取得商品優惠
    List<ProductPromotion> promotions = getProductPromotions(product.getProductId());

    if (promotions.isEmpty()) {
      priceInfo.put("hasPromotion", false);
      priceInfo.put("promotionalPrice", originalPrice);
      priceInfo.put("discountAmount", BigDecimal.ZERO);
      priceInfo.put("promotionTitle", null);
      priceInfo.put("promotionEndDate", null);
      priceInfo.put("promotionId", null);
      return priceInfo;
    }

    // 取得最低優惠價格的促銷
    ProductPromotion bestPromotion = promotions.stream()
        .min((p1, p2) -> p1.getPromotionalPrice().compareTo(p2.getPromotionalPrice()))
        .get();

    // 取得優惠活動資訊
    Promotion promotionInfo = promotionService.getPromotionById(bestPromotion.getPromotionId());

    // 設置優惠資訊
    priceInfo.put("hasPromotion", true);
    priceInfo.put("promotionalPrice", bestPromotion.getPromotionalPrice());
    priceInfo.put("promotionId", promotionInfo.getPromotionId());
    priceInfo.put("promotionTitle", promotionInfo.getTitle());
    priceInfo.put("promotionEndDate", promotionInfo.getEndDate());
    priceInfo.put("discountAmount", originalPrice.subtract(bestPromotion.getPromotionalPrice()));

    return priceInfo;
  }

  /**
   * 批量計算多個商品的價格資訊
   *
   * @param products 商品列表
   * @return Map 商品ID對應的價格資訊
   */
  public Map<Integer, Map<String, Object>> calculateBulkPrices(List<Product> products) {
    Map<Integer, Map<String, Object>> allPrices = new HashMap<>();

    for (Product product : products) {
      allPrices.put(product.getProductId(), calculatePrices(product));
    }

    return allPrices;
  }

  /**
   * 驗證優惠價格
   *
   * @param price 優惠價格
   * @throws IllegalArgumentException 當價格不合理時
   */
  private void validatePromotionalPrice(BigDecimal price) {
    if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("優惠價格必須大於零");
    }
  }
}