package fcu.iLive.model.product;

public class StockLockStatus {
  public static final int ORDERED = 1;    // 下單鎖定
  public static final int PAID = 2;       // 已付款
  public static final int CANCELLED = 3;  // 已取消
  public static final int OVERDUED = 4;   // 已過期
}