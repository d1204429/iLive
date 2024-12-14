package fcu.iLive.config;

import fcu.iLive.service.order.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class SchedulingConfig implements SchedulingConfigurer {

  private static final Logger logger = LoggerFactory.getLogger(SchedulingConfig.class);

  @Autowired
  private OrderService orderService;

  /**
   * 配置排程任務的執行器
   */
  @Override
  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    taskRegistrar.setScheduler(taskExecutor());
  }

  /**
   * 創建固定大小的執行緒池
   */
  public Executor taskExecutor() {
    return Executors.newScheduledThreadPool(5);
  }

  /**
   * 處理過期訂單的排程任務
   * cron表達式：每分鐘執行一次
   */
  //@Scheduled(cron = "0 * * * * ?")  // 每分鐘的第0秒執行
  @Scheduled(cron = "0 */30 * * * ?")  // 每30分鐘執行
  //@Scheduled(fixedRate = 60000)    // 固定速率執行
  //@Scheduled(fixedDelay = 60000)   // 固定延遲執行
  public void handleExpiredOrders() {
    try {
      logger.info("開始處理過期訂單...");
      orderService.handleExpiredOrders();
      logger.info("過期訂單處理完成");
    } catch (Exception e) {
      logger.error("處理過期訂單時發生錯誤：", e);
    }
  }
}