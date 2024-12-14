//假的付款驗證

package fcu.iLive.service.order;

import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;

/**
 * 支付服務
 * 處理不同支付方式的驗證邏輯
 */
@Service
public class PaymentService {

  /**
   * 信用卡付款驗證
   * 驗證信用卡號是否符合格式規範
   *
   * @param cardNumber 信用卡號
   * @return 驗證結果
   */
  public boolean validateCreditCardPayment(String cardNumber) {
    // 移除所有空格後檢查是否為16碼數字
    return cardNumber != null &&
        cardNumber.replaceAll("\\s+", "").matches("\\d{16}");
  }

  /**
   * APPLE PAY付款驗證
   * 驗證APPLE PAY token是否有效
   *
   * @param token APPLE PAY token
   * @return 驗證結果
   */
  public boolean validateApplePayPayment(String token) {
    // 簡單驗證token是否存在
    return token != null && !token.trim().isEmpty();
  }
}
