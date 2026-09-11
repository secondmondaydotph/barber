package com.barber.controller;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebFilter("/*")
public final class SecurityFilter implements Filter {
 public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{
  request.setCharacterEncoding("UTF-8");response.setCharacterEncoding("UTF-8");HttpServletResponse r=(HttpServletResponse)response;
  r.setHeader("X-Content-Type-Options","nosniff");r.setHeader("X-Frame-Options","DENY");r.setHeader("Referrer-Policy","strict-origin-when-cross-origin");
  if(!((HttpServletRequest)request).getRequestURI().contains("/assets/"))r.setHeader("Cache-Control","no-store");
  chain.doFilter(request,response);
 }
}
