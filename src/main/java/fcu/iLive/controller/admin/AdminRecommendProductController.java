package fcu.iLive.controller.admin;

import fcu.iLive.model.product.RecommendedProduct;
import fcu.iLive.service.product.RecommendedProductService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
public class AdminRecommendProductController {

  @Autowired
  private RecommendedProductService RecommendedProductservice;

  @GetMapping("/recommends")
  public List<RecommendedProduct> getAdminRecommends() {
    return RecommendedProductservice.findAll();
  }

  @PostMapping("/recommends")
  public RecommendedProduct createRecommend(@RequestBody RecommendedProduct recommendedProduct) {
    return RecommendedProductservice.create(recommendedProduct.getProductId());
  }

  @PutMapping("/recommends/{recommendId}")
  public void updateProduct(
      @PathVariable int recommendId,
      @RequestBody RecommendedProduct recommendedProduct) {
    RecommendedProductservice.update(recommendId, recommendedProduct.getProductId());
  }

  @DeleteMapping("/recommends/{recommendId}")
  public void deleteRecommend(@PathVariable int recommendId) {
    RecommendedProductservice.delete(recommendId);
  }
}