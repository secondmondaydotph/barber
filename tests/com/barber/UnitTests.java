package com.barber;
import com.barber.util.*;
import com.barber.service.*;
public class UnitTests {
 private static void check(boolean ok){if(!ok)throw new AssertionError();}
 public static void main(String[] args){
  String p="test-password-12345",hash=Passwords.hash(p);check(Passwords.verify(p,hash));check(!Passwords.verify("incorrect",hash));check(!hash.equals(Passwords.hash(p)));
  check(Html.e("<script>\"'&").equals("&lt;script&gt;&quot;&#39;&amp;"));
  check(AdminService.email(" CUSTOMER@EXAMPLE.COM ").equals("customer@example.com"));
  boolean rejected=false;try{Passwords.hash("short");}catch(IllegalArgumentException e){rejected=true;}check(rejected);
  rejected=false;try{BookingService.validDate(BookingService.today().minusDays(1));}catch(IllegalArgumentException e){rejected=true;}check(rejected);
  BookingService.validDate(BookingService.today().plusDays(90));
  System.out.println("PASS: password hashing, verification, escaping, email and date validation.");
 }
}
