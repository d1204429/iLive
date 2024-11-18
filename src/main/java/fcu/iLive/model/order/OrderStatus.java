//訂單狀態實體

package fcu.iLive.model.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatus {
  private int statusId;               // StatusID INT
  private String statusName;          // StatusName VARCHAR(50)

  // Constructor, Getters and Setters
}