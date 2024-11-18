//商品實體

package fcu.iLive.model.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
  private int productId;              // ProductID INT
  private String name;                // Name VARCHAR(100)
  private String description;         // Description TEXT
  private BigDecimal price;           // Price DECIMAL(10,2)
  private int stock;                  // Stock INT
  private int categoryId;             // CategoryID INT
  private String brand;               // Brand VARCHAR(50)
  private String imageUrl;            // ImageURL VARCHAR(255)
  private LocalDateTime createdAt;    // CreatedAt DATETIME
  private LocalDateTime updatedAt;    // UpdatedAt DATETIME
  private Category category;          // 關聯對象

  // Constructor, Getters and Setters
}