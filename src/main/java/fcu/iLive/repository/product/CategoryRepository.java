package fcu.iLive.repository.product;

import fcu.iLive.model.product.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Slf4j
@Repository
public class CategoryRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Category> rowMapper = (rs, rowNum) -> {
        Category category = new Category();
        category.setCategoryId(rs.getInt("CategoryID"));
        category.setCategoryName(rs.getString("CategoryName"));
        category.setParentCategoryId(rs.getInt("ParentCategoryID"));
        return category;
    };

    /**
     * 獲取所有分類
     */
    public List<Category> findAll() {
        String sql = "SELECT * FROM Categories ORDER BY CategoryID";
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * 根據ID獲取分類
     */
    public Category findById(int categoryId) {
        String sql = "SELECT * FROM Categories WHERE CategoryID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, categoryId);
        } catch (Exception e) {
            log.error("查詢分類失敗, ID: {}", categoryId, e);
            return null;
        }
    }

    /**
     * 獲取子分類
     */
    public List<Category> findByParentId(int parentId) {
        String sql = "SELECT * FROM Categories WHERE ParentCategoryID = ?";
        return jdbcTemplate.query(sql, rowMapper, parentId);
    }
    /**
     * 創建新分類
     */
    public Category create(Category category) {
        String sql = "INSERT INTO Categories (CategoryName, ParentCategoryID) VALUES (?, ?)";
        try {
            int newId = jdbcTemplate.update(sql,
                    category.getCategoryName(),
                    category.getParentCategoryId());
            category.setCategoryId(newId);
            return category;
        } catch (Exception e) {
            log.error("建立分類失敗", e);
            throw new RuntimeException("建立分類失敗", e);
        }
    }

    /**
     * 更新分類
     */
    public Category update(Category category) {
        String sql = "UPDATE Categories SET CategoryName = ?, ParentCategoryID = ? WHERE CategoryID = ?";
        try {
            int result = jdbcTemplate.update(sql,
                    category.getCategoryName(),
                    category.getParentCategoryId(),
                    category.getCategoryId());
            if (result == 0) {
                throw new RuntimeException("找不到要更新的分類");
            }
            return category;
        } catch (Exception e) {
            log.error("更新分類失敗", e);
            throw new RuntimeException("更新分類失敗", e);
        }
    }

    /**
     * 刪除分類
     */
    public void delete(int categoryId) {
        String sql = "DELETE FROM Categories WHERE CategoryID = ?";
        try {
            int result = jdbcTemplate.update(sql, categoryId);
            if (result == 0) {
                throw new RuntimeException("找不到要刪除的分類");
            }
        } catch (Exception e) {
            log.error("刪除分類失敗", e);
            throw new RuntimeException("刪除分類失敗", e);
        }
    }
}
