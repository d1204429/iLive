package fcu.iLive.model.order;

public class OrderStatusConstants {
  public static final int ORDERED = 1;        // 下單鎖定
  public static final int PAID = 2;           // 已付款
  public static final int SHIPPED = 3;        // 已出貨
  public static final int CANCELLED = 4;      // 已取消
  public static final int COMPLETED = 5;      // 已完成
  public static final int REFUNDED = 6;       // 已退貨
  public static final int EXPIRED = 7;        // 未付款取消
}