package fcu.iLive.controller.admin;

import fcu.iLive.model.product.Category;
import fcu.iLive.service.product.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 獲取所有分類
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        try {
            log.info("獲取所有商品分類");
            List<Category> categories = categoryService.getAllCategories();
            return new ResponseEntity<>(categories, HttpStatus.OK);
        } catch (Exception e) {
            log.error("獲取商品分類失敗", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 獲取指定分類
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable("id") int categoryId) {
        try {
            log.info("獲取分類ID: {}", categoryId);
            Category category = categoryService.getCategoryById(categoryId);
            if (category != null) {
                return new ResponseEntity<>(category, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("獲取分類詳情失敗", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }    /**
     * 獲取子分類
     */
    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<Category>> getSubCategories(@PathVariable("id") int categoryId) {
        try {
            log.info("獲取父分類ID: {}的子分類", categoryId);
            List<Category> subCategories = categoryService.getSubCategories(categoryId);
            return new ResponseEntity<>(subCategories, HttpStatus.OK);
        } catch (Exception e) {
            log.error("獲取子分類失敗", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 更新分類
     */
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable("id") int categoryId,
            @RequestBody Category category) {
        try {
            log.info("更新分類ID: {}, 資料: {}", categoryId, category);
            category.setCategoryId(categoryId);
            Category updatedCategory = categoryService.updateCategory(category);
            if (updatedCategory != null) {
                return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            log.error("更新分類參數錯誤", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("更新分類失敗", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 刪除分類
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") int categoryId) {
        try {
            log.info("刪除分類ID: {}", categoryId);
            categoryService.deleteCategory(categoryId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            log.error("刪除分類參數錯誤", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("刪除分類失敗", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

