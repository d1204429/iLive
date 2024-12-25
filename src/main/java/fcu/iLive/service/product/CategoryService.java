package fcu.iLive.service.product;

import fcu.iLive.model.product.Category;
import fcu.iLive.repository.product.CategoryRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 獲取所有分類
     */
    public List<Category> getAllCategories() {
        try {
            log.info("獲取所有商品分類");
            return categoryRepository.findAll();
        } catch (Exception e) {
            log.error("獲取商品分類失敗", e);
            throw new RuntimeException("獲取商品分類失敗", e);
        }
    }

    /**
     * 獲取指定分類
     */
    public Category getCategoryById(int categoryId) {
        try {
            log.info("獲取分類ID: {}", categoryId);
            return categoryRepository.findById(categoryId);
        } catch (Exception e) {
            log.error("獲取分類詳情失敗, ID: {}", categoryId, e);
            throw new RuntimeException("獲取分類詳情失敗", e);
        }
    }

    /**
     * 獲取子分類
     */
    public List<Category> getSubCategories(int parentId) {
        try {
            log.info("獲取父分類ID: {}的子分類", parentId);
            return categoryRepository.findByParentId(parentId);
        } catch (Exception e) {
            log.error("獲取子分類失敗, 父分類ID: {}", parentId, e);
            throw new RuntimeException("獲取子分類失敗", e);
        }
    }
    /**
     * 創建新分類
     */
    @Transactional
    public Category createCategory(Category category) {
        try {
            validateCategory(category);
            log.info("建立新分類: {}", category);
            return categoryRepository.create(category);
        } catch (Exception e) {
            log.error("建立分類失敗", e);
            throw new RuntimeException("建立分類失敗", e);
        }
    }

    /**
     * 更新分類
     */
    @Transactional
    public Category updateCategory(Category category) {
        try {
            validateCategory(category);
            log.info("更新分類: {}", category);
            return categoryRepository.update(category);
        } catch (Exception e) {
            log.error("更新分類失敗", e);
            throw new RuntimeException("更新分類失敗", e);
        }
    }

    /**
     * 刪除分類
     */
    @Transactional
    public void deleteCategory(int categoryId) {
        try {
            log.info("刪除分類ID: {}", categoryId);
            // 檢查是否有子分類
            List<Category> subCategories = categoryRepository.findByParentId(categoryId);
            if (!subCategories.isEmpty()) {
                throw new IllegalArgumentException("無法刪除含有子分類的分類");
            }
            categoryRepository.delete(categoryId);
        } catch (Exception e) {
            log.error("刪除分類失敗", e);
            throw new RuntimeException("刪除分類失敗", e);
        }
    }

    /**
     * 驗證分類資料
     */
    private void validateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("分類資料不能為空");
        }
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("分類名稱不能為空");
        }
        if (category.getCategoryName().length() > 50) {
            throw new IllegalArgumentException("分類名稱不能超過50個字符");
        }
    }
}
