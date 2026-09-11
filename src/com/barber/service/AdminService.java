package com.barber.service;
import com.barber.dao.Database;
import com.barber.util.Passwords;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.util.*;
public final class AdminService {
 public static String text(String s,int max){s=s==null?"":s.strip();if(s.isEmpty()||s.length()>max)throw new IllegalArgumentException("A required field is missing or too long.");return s;}
 public static String email(String s){s=text(s,200).toLowerCase(Locale.ROOT);if(!s.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))throw new IllegalArgumentException("Enter a valid email address.");return s;}
 public void catalog(String kind,long id,String name,int minutes,BigDecimal price,boolean active)throws SQLException {
  String table=switch(kind){case "service"->"services";case "addon"->"addons";default->throw new IllegalArgumentException("Unknown catalog.");};
  name=text(name,100);if(minutes<(kind.equals("service")?15:0)||minutes>(kind.equals("service")?240:120)||price.signum()<0||price.compareTo(new BigDecimal("99999999.99"))>0||price.scale()>2)throw new IllegalArgumentException("Check duration and price (up to two decimal places).");
  if(id==0)Database.update("INSERT INTO "+table+"(name,minutes,price,active) VALUES(?,?,?,?)",name,minutes,price,active);
  else if(Database.update("UPDATE "+table+" SET name=?,minutes=?,price=?,active=? WHERE id=?",name,minutes,price,active,id)!=1)throw new IllegalArgumentException("Item not found.");
 }
 public void barber(long id,String name,String email,String password,boolean active)throws SQLException {
  name=text(name,100);email=email(email);
  if(id==0){Database.update("INSERT INTO users(name,email,password_hash,role,active) VALUES(?,?,?,'BARBER',?)",name,email,Passwords.hash(password),active);return;}
  try(Connection c=Database.open()){c.setAutoCommit(false);try{
   Database.rows(c,"SELECT pg_advisory_xact_lock(?)",id);
   if(!active&&!Database.rows(c,"SELECT id FROM bookings WHERE barber_id=? AND status IN ('CONFIRMED','IN_PROGRESS') AND ends_at>?",id,LocalDateTime.now(BookingService.ZONE)).isEmpty())throw new IllegalArgumentException("Complete or cancel this barber's upcoming appointments before deactivation.");
   int changed=Database.update(c,"UPDATE users SET name=?,email=?,active=? WHERE id=? AND role='BARBER'",name,email,active,id);if(changed!=1)throw new IllegalArgumentException("Barber not found.");
   if(password!=null&&!password.isBlank())Database.update(c,"UPDATE users SET password_hash=? WHERE id=?",Passwords.hash(password),id);c.commit();
  }catch(SQLException|RuntimeException e){c.rollback();throw e;}}
 }
 public void barberProfile(long id,String specialties,String bio,String photoUrl)throws SQLException{
  specialties=optional(specialties,300);bio=optional(bio,800);photoUrl=optional(photoUrl,500);
  if(!photoUrl.isEmpty()&&!photoUrl.matches("(?i)https?://[^\\s]+|/[^\\s]+"))throw new IllegalArgumentException("Photo must be an HTTPS URL or an application path beginning with /. ");
  final String fs=specialties,fb=bio,fp=photoUrl;withBarber(id,c->Database.update(c,"INSERT INTO barber_profiles(barber_id,specialties,bio,photo_url) VALUES(?,?,?,?) ON CONFLICT(barber_id) DO UPDATE SET specialties=excluded.specialties,bio=excluded.bio,photo_url=excluded.photo_url",id,fs,fb,fp));
 }
 private static String optional(String value,int max){value=value==null?"":value.strip();if(value.length()>max)throw new IllegalArgumentException("Profile text is too long.");return value;}
 public void schedule(long barber,int weekday,LocalTime start,LocalTime end,boolean active)throws SQLException{
  if(weekday<1||weekday>7||!end.isAfter(start))throw new IllegalArgumentException("Select a weekday and a closing time later than opening.");
  withBarber(barber,c->Database.update(c,"INSERT INTO weekly_schedules(barber_id,weekday,start_time,end_time,active) VALUES(?,?,?,?,?) ON CONFLICT(barber_id,weekday) DO UPDATE SET start_time=excluded.start_time,end_time=excluded.end_time,active=excluded.active",barber,weekday,start,end,active));
 }
 public void exception(long barber,LocalDate day,boolean closed,LocalTime start,LocalTime end,boolean remove)throws SQLException{
  BookingService.validDate(day);if(!remove&&!closed&&(start==null||end==null||!end.isAfter(start)))throw new IllegalArgumentException("Enter valid exception hours.");
  withBarber(barber,c->{if(remove)Database.update(c,"DELETE FROM schedule_exceptions WHERE barber_id=? AND day=?",barber,day);else Database.update(c,"INSERT INTO schedule_exceptions(barber_id,day,closed,start_time,end_time) VALUES(?,?,?,?,?) ON CONFLICT(barber_id,day) DO UPDATE SET closed=excluded.closed,start_time=excluded.start_time,end_time=excluded.end_time",barber,day,closed,start,end);});
 }
 private interface Change{void run(Connection c)throws SQLException;}
 private void withBarber(long id,Change change)throws SQLException{try(Connection c=Database.open()){c.setAutoCommit(false);try{Database.rows(c,"SELECT pg_advisory_xact_lock(?)",id);if(Database.rows(c,"SELECT id FROM users WHERE id=? AND role='BARBER'",id).isEmpty())throw new IllegalArgumentException("Barber not found.");change.run(c);c.commit();}catch(SQLException|RuntimeException e){c.rollback();throw e;}}}
}
