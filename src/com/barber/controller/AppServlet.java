package com.barber.controller;
import com.barber.dao.*;
import com.barber.model.User;
import com.barber.service.*;
import com.barber.util.Passwords;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
@WebServlet(urlPatterns={"/home","/about","/services","/team","/contact","/testimonials","/login","/register","/logout","/book","/availability","/my-bookings","/profile","/cancel-booking","/rebook","/waitlist","/claim-waitlist","/dashboard","/admin/*","/staff/*","/health","/error-page"})
public final class AppServlet extends HttpServlet {
 private final ShopDao shop=new ShopDao();private final BookingService bookings=new BookingService();private final AdminService admin=new AdminService();
 private record Attempts(int count,Instant expires){}
 private final Map<String,Attempts> attempts=new ConcurrentHashMap<>();
 private String path(HttpServletRequest r){return r.getServletPath()+(r.getPathInfo()==null?"":r.getPathInfo());}
 private User user(HttpServletRequest r){return (User)r.getSession().getAttribute("user");}
 private void token(HttpServletRequest r){if(r.getSession().getAttribute("csrf")==null)r.getSession().setAttribute("csrf",Passwords.token());}
 private boolean permit(HttpServletRequest r,HttpServletResponse s,String p)throws IOException,SQLException {
  if(!Set.of("/book","/availability","/my-bookings","/profile","/cancel-booking","/rebook","/waitlist","/claim-waitlist","/dashboard","/logout").contains(p)&&!p.startsWith("/admin/")&&!p.startsWith("/staff/"))return true;
  User u=user(r);if(u==null){s.sendRedirect(r.getContextPath()+"/login");return false;}
  if(!shop.active(u)){r.getSession().invalidate();s.sendRedirect(r.getContextPath()+"/login");return false;}
  if(p.startsWith("/admin/")&&!u.role().equals("ADMIN")||p.startsWith("/staff/")&&!Set.of("ADMIN","BARBER").contains(u.role())||Set.of("/book","/availability","/my-bookings","/profile","/cancel-booking","/rebook","/waitlist","/claim-waitlist").contains(p)&&!u.role().equals("CUSTOMER")){s.sendError(403);return false;}return true;
 }
 protected void doGet(HttpServletRequest r,HttpServletResponse s)throws IOException,ServletException {
  token(r);String p=path(r);
  try{
   if(!permit(r,s,p))return;
   switch(p){
    case "/error-page" -> {
     Object raw=r.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);int code=raw instanceof Number?((Number)raw).intValue():404;
     String title,message;switch(code){
      case 400 -> {title="Please check your request";message="Some information was missing or invalid. Review your entries and try again.";}
      case 403 -> {title="This area is not available for your account";message="You are signed in, but your account role does not have access to this page.";}
      case 404 -> {title="We could not find that page";message="The address may be incorrect, or the page may have moved.";}
      default -> {code=500;title="Something went wrong";message="We could not complete your request. Please try again, or return to a safe page.";}
     }
     User current=user(r);String target="/home",label="Return home";
     if(current!=null){if(current.role().equals("CUSTOMER")){target="/my-bookings";label="My appointments";}else{target="/dashboard";label="Return to dashboard";}}
     s.setStatus(code);r.setAttribute("errorCode",code);r.setAttribute("errorTitle",title);r.setAttribute("error",message);r.setAttribute("errorAction",target);r.setAttribute("errorActionLabel",label);r.setAttribute("page","error");view(r,s,"public-form");
    }
    case "/health" -> {try(var c=Database.open()){s.setContentType("text/plain");s.getWriter().print(c.isValid(2)?"Barber database connected":"Barber database unavailable");}}
    case "/team" -> {r.setAttribute("barbers",shop.barbers());view(r,s,"team");}
    case "/home","/about","/services","/contact","/testimonials" -> view(r,s,p.substring(1));
    case "/login","/register" -> {r.setAttribute("page",p.substring(1));view(r,s,"public-form");}
    case "/book","/rebook" -> {r.setAttribute("services",shop.services());r.setAttribute("addons",shop.addons());r.setAttribute("barbers",shop.barbers());if(p.equals("/rebook")){var previous=shop.bookingForRebook(user(r),id(r,"id"));r.setAttribute("previous",previous);}r.setAttribute("page","book");view(r,s,"public-form");}
    case "/availability" -> {s.setContentType("application/json");var slots=bookings.availability(id(r,"barber"),LocalDate.parse(r.getParameter("date")),id(r,"service"),addonIds(r));s.getWriter().print("{\"slots\":["+String.join(",",slots.stream().map(t->"\""+t+"\"").toList())+"]}");}
    case "/my-bookings" -> {r.setAttribute("bookings",shop.bookings(user(r)));r.setAttribute("waitlist",shop.waitlist(user(r)));r.setAttribute("notifications",shop.notifications(user(r)));r.setAttribute("loyaltyVisits",shop.loyaltyVisits(user(r)));r.setAttribute("page","my-bookings");view(r,s,"public-form");}
    case "/profile" -> {r.setAttribute("profile",shop.profile(user(r)));r.setAttribute("page","profile");view(r,s,"public-form");}
    case "/dashboard","/admin/bookings","/staff/bookings" -> {r.setAttribute("bookings",shop.bookings(user(r)));r.setAttribute("page",p.equals("/dashboard")?"dashboard":"bookings");if(user(r).role().equals("CUSTOMER")){s.sendRedirect(r.getContextPath()+"/my-bookings");return;}view(r,s,"dashboard");}
    case "/admin/services","/admin/addons","/admin/barbers","/admin/schedules","/admin/customers" -> {
     String section=p.substring(7);r.setAttribute("page",section);
     if(section.equals("services")||section.equals("addons"))r.setAttribute("items",Database.rows("SELECT * FROM "+section+" ORDER BY id"));
     if(section.equals("barbers"))r.setAttribute("items",Database.rows("SELECT u.id,u.name,u.email,u.active,coalesce(p.specialties,'') specialties,coalesce(p.bio,'') bio,coalesce(p.photo_url,'') photo_url FROM users u LEFT JOIN barber_profiles p ON p.barber_id=u.id WHERE u.role='BARBER' ORDER BY u.name"));
     if(section.equals("customers"))r.setAttribute("items",shop.customers());
     if(section.equals("schedules")){r.setAttribute("barbers",shop.barbers());r.setAttribute("schedules",Database.rows("SELECT w.*,u.name FROM weekly_schedules w JOIN users u ON u.id=w.barber_id ORDER BY u.name,w.weekday"));r.setAttribute("exceptions",Database.rows("SELECT e.*,u.name FROM schedule_exceptions e JOIN users u ON u.id=e.barber_id ORDER BY e.day DESC LIMIT 100"));}
     view(r,s,"dashboard");
    }
    default -> s.sendError(404);
   }
  }catch(IllegalArgumentException|DateTimeException e){if(p.equals("/availability")){s.setStatus(400);s.setContentType("application/json");s.getWriter().print("{\"error\":\"Check the barber, date and selected services.\"}");}else error(r,s,400,"Check your inputs and try again.");}
  catch(SQLException e){log("Database operation failed; SQLState="+e.getSQLState());error(r,s,503,"The database is unavailable. Please contact the shop and try again later.");}
 }
 protected void doPost(HttpServletRequest r,HttpServletResponse s)throws IOException,ServletException{
  token(r);String p=path(r);String expected=(String)r.getSession().getAttribute("csrf");
  if(!expected.equals(r.getParameter("csrf"))){error(r,s,403,"Your form expired. Reload the page and try again.");return;}
  try{
   if(!permit(r,s,p))return;
   switch(p){
    case "/register" -> {shop.register(AdminService.text(r.getParameter("name"),100),AdminService.email(r.getParameter("email")),AdminService.text(r.getParameter("phone"),30),r.getParameter("password"));flash(r,"Account created. Please sign in.");redirect(r,s,"/login");}
    case "/login" -> {
     String key=r.getRemoteAddr();Instant now=Instant.now();attempts.entrySet().removeIf(e->e.getValue().expires().isBefore(now));
     if(attempts.size()>5000&&!attempts.containsKey(key)){error(r,s,429,"Please try signing in later.");return;}
     Attempts a=attempts.get(key);if(a!=null&&a.count()>=10){error(r,s,429,"Too many attempts. Try again in 15 minutes.");return;}
     User u=shop.login(AdminService.email(r.getParameter("email")),r.getParameter("password"));
     if(u==null){attempts.compute(key,(k,v)->new Attempts(v==null?1:v.count()+1,v==null?now.plusSeconds(900):v.expires()));throw new IllegalArgumentException("Email or password is incorrect.");}
     attempts.remove(key);r.getSession().invalidate();r.getSession(true).setAttribute("user",u);token(r);redirect(r,s,u.role().equals("CUSTOMER")?"/my-bookings":"/dashboard");
    }
    case "/logout" -> {r.getSession().invalidate();redirect(r,s,"/home");}
    case "/book" -> {int count=Integer.parseInt(Optional.ofNullable(r.getParameter("occurrences")).filter(x->!x.isBlank()).orElse("1"));if(count>1){var ids=bookings.bookRecurring(user(r),id(r,"barber"),LocalDate.parse(r.getParameter("date")),LocalTime.parse(r.getParameter("time")),id(r,"service"),addonIds(r),Integer.parseInt(r.getParameter("intervalWeeks")),count);flash(r,ids.size()+" recurring appointments confirmed. Payment is made at each visit.");}else{long id=bookings.book(user(r),id(r,"barber"),LocalDate.parse(r.getParameter("date")),LocalTime.parse(r.getParameter("time")),id(r,"service"),addonIds(r));flash(r,"Appointment #"+id+" confirmed. Payment is made at the shop.");}redirect(r,s,"/my-bookings");}
    case "/waitlist" -> {long id=bookings.joinWaitlist(user(r),id(r,"barber"),LocalDate.parse(r.getParameter("date")),LocalTime.parse(r.getParameter("time")),id(r,"service"),addonIds(r));flash(r,"Waitlist entry #"+id+" created. You will see an alert here if the slot opens.");redirect(r,s,"/my-bookings");}
    case "/claim-waitlist" -> {long id=bookings.claimWaitlist(user(r),id(r,"id"));flash(r,"Appointment #"+id+" confirmed from your waitlist offer.");redirect(r,s,"/my-bookings");}
    case "/profile" -> {User updated=shop.updateProfile(user(r),AdminService.text(r.getParameter("name"),100),AdminService.email(r.getParameter("email")),AdminService.text(r.getParameter("phone"),30),r.getParameter("password"));r.getSession().setAttribute("user",updated);flash(r,"Your profile has been updated.");redirect(r,s,"/profile");}
    case "/cancel-booking" -> {bookings.status(user(r),id(r,"id"),"CANCELLED");flash(r,"Appointment cancelled.");redirect(r,s,"/my-bookings");}
    case "/staff/status" -> {bookings.status(user(r),id(r,"id"),r.getParameter("status"));flash(r,"Appointment updated.");redirect(r,s,"/dashboard");}
    case "/admin/service","/admin/addon" -> {String kind=p.substring(7);admin.catalog(kind,optionalId(r),AdminService.text(r.getParameter("name"),100),Integer.parseInt(r.getParameter("minutes")),new BigDecimal(r.getParameter("price")),checked(r,"active"));flash(r,"Catalog saved.");redirect(r,s,"/admin/"+kind+"s");}
    case "/admin/barber" -> {admin.barber(optionalId(r),r.getParameter("name"),r.getParameter("email"),r.getParameter("password"),checked(r,"active"));flash(r,"Barber account saved. Configure working hours under Schedules.");redirect(r,s,"/admin/barbers");}
    case "/admin/barber-profile" -> {admin.barberProfile(id(r,"id"),r.getParameter("specialties"),r.getParameter("bio"),r.getParameter("photoUrl"));flash(r,"Public barber profile saved.");redirect(r,s,"/admin/barbers");}
    case "/admin/schedule" -> {admin.schedule(id(r,"barber"),Integer.parseInt(r.getParameter("weekday")),LocalTime.parse(r.getParameter("start")),LocalTime.parse(r.getParameter("end")),checked(r,"active"));flash(r,"Weekly hours saved. Existing bookings remain unchanged.");redirect(r,s,"/admin/schedules");}
    case "/admin/exception" -> {boolean closed=checked(r,"closed"),remove=checked(r,"remove");admin.exception(id(r,"barber"),LocalDate.parse(r.getParameter("date")),closed,closed||remove?null:LocalTime.parse(r.getParameter("start")),closed||remove?null:LocalTime.parse(r.getParameter("end")),remove);flash(r,"Schedule exception saved. Existing bookings remain unchanged.");redirect(r,s,"/admin/schedules");}
    default -> s.sendError(404);
   }
  }catch(SecurityException e){error(r,s,403,"You do not have permission for that appointment.");}
  catch(IllegalArgumentException|DateTimeException e){flash(r,e instanceof NumberFormatException||e instanceof DateTimeException?"Check all required fields and date/time values.":e.getMessage());redirect(r,s,returnPath(p));}
  catch(SQLException e){log("Database operation failed; SQLState="+e.getSQLState());flash(r,"23505".equals(e.getSQLState())?"This email or item name already exists.":"Unable to save. Please try again later or contact the shop.");redirect(r,s,returnPath(p));}
 }
 private String returnPath(String p){return switch(p){case "/admin/service"->"/admin/services";case "/admin/addon"->"/admin/addons";case "/admin/barber","/admin/barber-profile"->"/admin/barbers";case "/admin/schedule","/admin/exception"->"/admin/schedules";case "/staff/status"->"/dashboard";case "/cancel-booking","/waitlist","/claim-waitlist"->"/my-bookings";case "/profile"->"/profile";default->Set.of("/login","/register","/book","/rebook").contains(p)?"/book":"/home";};}
 private boolean checked(HttpServletRequest r,String key){return "on".equals(r.getParameter(key));}
 private long id(HttpServletRequest r,String key){long n=Long.parseLong(r.getParameter(key));if(n<=0)throw new IllegalArgumentException("Invalid selection.");return n;}
 private long optionalId(HttpServletRequest r){String s=r.getParameter("id");return s==null||s.isBlank()?0:id(r,"id");}
 private Set<Long> addonIds(HttpServletRequest r){Set<Long> ids=new HashSet<>();String[] vals=r.getParameterValues("addon");if(vals!=null)for(String s:vals){long id=Long.parseLong(s);if(id<=0)throw new IllegalArgumentException("Invalid add-on.");ids.add(id);}return ids;}
 private void flash(HttpServletRequest r,String message){r.getSession().setAttribute("flash",message);}
 private void redirect(HttpServletRequest r,HttpServletResponse s,String path)throws IOException{s.setStatus(303);s.setHeader("Location",r.getContextPath()+path);}
 private void view(HttpServletRequest r,HttpServletResponse s,String view)throws ServletException,IOException{r.getRequestDispatcher("/WEB-INF/views/"+view+".jsp").forward(r,s);}
 private void error(HttpServletRequest r,HttpServletResponse s,int code,String message)throws ServletException,IOException{s.setStatus(code);r.setAttribute("error",message);r.setAttribute("page","error");view(r,s,"public-form");}
}
