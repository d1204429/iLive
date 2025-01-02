package fcu.iLive.filter;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import fcu.iLive.util.XssUtils;

public class XssRequestWrapper extends HttpServletRequestWrapper {

  public XssRequestWrapper(HttpServletRequest request) {
    super(request);
  }

  @Override
  public String[] getParameterValues(String parameter) {
    String[] values = super.getParameterValues(parameter);
    if (values == null) {
      return null;
    }

    int count = values.length;
    String[] encodedValues = new String[count];
    for (int i = 0; i < count; i++) {
      encodedValues[i] = XssUtils.sanitize(values[i]);
    }
    return encodedValues;
  }

  @Override
  public String getParameter(String parameter) {
    String value = super.getParameter(parameter);
    return XssUtils.sanitize(value);
  }

  @Override
  public String getHeader(String name) {
    String value = super.getHeader(name);
    if ("Authorization".equals(name)) {
      return value; // 不處理 Authorization header
    }
    return XssUtils.sanitize(value);
  }
}