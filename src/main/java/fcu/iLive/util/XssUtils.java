package fcu.iLive.util;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class XssUtils {

  public static String sanitize(String input) {
    if (input == null) {
      return null;
    }

    // 使用 JSoup 清理 HTML
    String cleanHtml = Jsoup.clean(input, Safelist.none());

    // 轉義特殊字符
    return StringEscapeUtils.escapeHtml4(cleanHtml);
  }
}