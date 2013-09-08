package com.quantimodo.etl;

import org.apache.commons.lang3.StringEscapeUtils;

public final class HTMLUtil {
  // Disable default constructor.
  private HTMLUtil() { throw new UnsupportedOperationException("Utility classes cannot be constructed."); }
  
  public static final String escapeHTML(final String text) {
    return StringEscapeUtils.escapeHtml4(text).replace("\r\n", "<br />").replace("\r", "<br />").replace("\n", "<br />");
  }
  
  public static final String escapeHTMLOnly(final String text) {
    return StringEscapeUtils.escapeHtml4(text);
  }
}
