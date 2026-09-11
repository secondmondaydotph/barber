package com.barber.util;
public final class Html {
 private Html() {}
 public static String e(Object value) { return value == null ? "" : value.toString().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;"); }
}
