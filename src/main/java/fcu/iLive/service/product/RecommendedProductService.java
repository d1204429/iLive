package fcu.iLive.service.product;

import fcu.iLive.model.product.RecommendedProduct;
import fcu.iLive.repository.product.RecommendedProductRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecommendedProductService {

  @Autowired
  private RecommendedProductRepository recommendedProductRepository;

  /**
   * 查詢所有推薦商品.
   */
  public List<RecommendedProduct> findAll() {
    try {
      return recommendedProductRepository.findAll();
    } catch (Exception e) {
      throw new RuntimeException("Failed to get recommended products", e);
    }
  }

  /**
   * 新增推薦商品.
   */
  public RecommendedProduct create(int productId) {
    try {
      RecommendedProduct recommend = new RecommendedProduct();
      recommend.setProductId(productId);
      recommend.setRecommendId(recommendedProductRepository.getMaxRecommendId() + 1);
      recommend.setCreatedAt(LocalDateTime.now());
      recommend.setUpdatedAt(LocalDateTime.now());

      recommendedProductRepository.insert(recommend);
      return recommend;
    } catch (Exception e) {
      throw new RuntimeException("Failed to create recommended product", e);
    }
  }

  /**
   * 更新推薦商品.
   */
  public void update(int recommendId, int newProductId) {
    try {
      RecommendedProduct recommend = recommendedProductRepository.findById(recommendId);
      recommend.setProductId(newProductId);
      recommend.setUpdatedAt(LocalDateTime.now());
      recommendedProductRepository.update(recommend);
    } catch (Exception e) {
      throw new RuntimeException("Failed to update recommended product", e);
    }
  }

  /**
   * 刪除推薦商品.
   */
  public void delete(int recommendId) {
    try {
      recommendedProductRepository.delete(recommendId);
    } catch (Exception e) {
      throw new RuntimeException("Failed to delete recommended product", e);
    }
  }
}