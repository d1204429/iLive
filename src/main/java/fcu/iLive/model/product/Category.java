//分類實體

package fcu.iLive.model.product;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {
  private int categoryId;             // CategoryID INT
  private String categoryName;        // CategoryName VARCHAR(50)
  private int parentCategoryId;       // ParentCategoryID INT
  private List<Category> subCategories; // 子分類列表（選擇性使用）

  // Constructor, Getters and Setters
}

