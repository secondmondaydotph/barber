package com.barber.dao;
import com.barber.model.User;
import com.barber.util.Passwords;
import java.sql.*;
import java.util.*;
public final class ShopDao {
 public User login(String email,String password)throws SQLException{
  var rows=Database.rows("SELECT * FROM users WHERE email=lower(?) AND active",email);
  if(rows.isEmpty()||!Passwords.verify(password,(String)rows.get(0).get("password_hash")))return null;
  var r=rows.get(0);return new User(Database.number(r,"id"),(String)r.get("name"),(String)r.get("email"),(String)r.get("role"));
 }
 public boolean active(User u)throws SQLException{return !Database.rows("SELECT id FROM users WHERE id=? AND active AND role=?",u.id(),u.role()).isEmpty();}
 public void register(String name,String email,String phone,String password)throws SQLException{Database.update("INSERT INTO users(name,email,phone,password_hash,role) VALUES(?,lower(?),?,?,'CUSTOMER')",name,email,phone,Passwords.hash(password));}
 public Map<String,Object> profile(User user)throws SQLException{
  var rows=Database.rows("SELECT id,name,email,phone,role,created_at FROM users WHERE id=? AND active",user.id());
  if(rows.isEmpty())throw new IllegalArgumentException("Profile not found.");return rows.get(0);
 }
 public User updateProfile(User user,String name,String email,String phone,String password)throws SQLException{
  if(!user.role().equals("CUSTOMER"))throw new SecurityException("Only customers can edit this profile.");
  try(Connection c=Database.open()){c.setAutoCommit(false);try{
   if(password==null||password.isBlank())Database.update(c,"UPDATE users SET name=?,email=lower(?),phone=? WHERE id=? AND role='CUSTOMER'",name,email,phone,user.id());
   else Database.update(c,"UPDATE users SET name=?,email=lower(?),phone=?,password_hash=? WHERE id=? AND role='CUSTOMER'",name,email,phone,Passwords.hash(password),user.id());
   c.commit();return new User(user.id(),name,email.toLowerCase(Locale.ROOT),user.role());
  }catch(SQLException|RuntimeException e){c.rollback();throw e;}}
 }
 public List<Map<String,Object>> customers()throws SQLException{return Database.rows("SELECT u.id,u.name,u.email,u.phone,u.active,u.created_at,count(b.id) booking_count,count(b.id) FILTER (WHERE b.status='COMPLETED') completed_count,max(b.starts_at) last_booking FROM users u LEFT JOIN bookings b ON b.customer_id=u.id WHERE u.role='CUSTOMER' GROUP BY u.id ORDER BY u.name");}
 public List<Map<String,Object>> services()throws SQLException{return Database.rows("SELECT * FROM services WHERE active ORDER BY id");}
 public List<Map<String,Object>> addons()throws SQLException{return Database.rows("SELECT * FROM addons WHERE active ORDER BY id");}
 public List<Map<String,Object>> barbers()throws SQLException{return Database.rows("SELECT u.id,u.name,u.email,coalesce(p.bio,'') bio,coalesce(p.specialties,'') specialties,coalesce(p.photo_url,'') photo_url FROM users u LEFT JOIN barber_profiles p ON p.barber_id=u.id WHERE u.role='BARBER' AND u.active ORDER BY u.name");}
 public List<Map<String,Object>> waitlist(User u)throws SQLException{return Database.rows("SELECT w.*,s.name service,b.name barber FROM waitlist_entries w JOIN services s ON s.id=w.service_id JOIN users b ON b.id=w.barber_id WHERE w.customer_id=? AND w.status IN ('WAITING','OFFERED') ORDER BY w.requested_date,w.requested_time,w.created_at",u.id());}
 public List<Map<String,Object>> notifications(User u)throws SQLException{return Database.rows("SELECT * FROM notifications WHERE user_id=? ORDER BY created_at DESC LIMIT 25",u.id());}
 public long loyaltyVisits(User u)throws SQLException{var r=Database.rows("SELECT count(*) visits FROM bookings WHERE customer_id=? AND status='COMPLETED'",u.id());return Database.number(r.get(0),"visits");}
 public Map<String,Object> bookingForRebook(User u,long id)throws SQLException{var r=Database.rows("SELECT * FROM bookings WHERE id=? AND customer_id=?",id,u.id());if(r.isEmpty())throw new IllegalArgumentException("Appointment not found.");Map<String,Object> result=r.get(0);List<Long> addons=new ArrayList<>();for(var a:Database.rows("SELECT addon_id FROM booking_addons WHERE booking_id=?",id))addons.add(Database.number(a,"addon_id"));result.put("addons",addons);return result;}
 public List<Map<String,Object>> bookings(User u)throws SQLException {
  String filter=u.role().equals("CUSTOMER")?" WHERE b.customer_id=?":u.role().equals("BARBER")?" WHERE b.barber_id=?":"";
  String sql="SELECT b.*,c.name customer,c.email customer_email,s.name barber FROM bookings b JOIN users c ON c.id=b.customer_id JOIN users s ON s.id=b.barber_id"+filter+" ORDER BY b.starts_at DESC LIMIT 250";
  return u.role().equals("ADMIN")?Database.rows(sql):Database.rows(sql,u.id());
 }
}
