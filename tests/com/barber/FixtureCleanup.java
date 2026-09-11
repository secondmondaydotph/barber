package com.barber;
import com.barber.dao.Database;
public class FixtureCleanup {
 public static void main(String[] args)throws Exception{
  if(args.length!=1||!args[0].matches("[a-f0-9]{12}"))throw new IllegalArgumentException("Expected a generated QA run token.");
  String email="qa-"+args[0]+"-%@example.test",name="QA "+args[0]+" %";
  try(var c=Database.open()){c.setAutoCommit(false);try{
   Database.update(c,"DELETE FROM booking_history WHERE booking_id IN (SELECT id FROM bookings WHERE customer_id IN (SELECT id FROM users WHERE email LIKE ?))",email);
   Database.update(c,"DELETE FROM booking_addons WHERE booking_id IN (SELECT id FROM bookings WHERE customer_id IN (SELECT id FROM users WHERE email LIKE ?))",email);
   Database.update(c,"DELETE FROM bookings WHERE customer_id IN (SELECT id FROM users WHERE email LIKE ?)",email);
   Database.update(c,"DELETE FROM weekly_schedules WHERE barber_id IN (SELECT id FROM users WHERE email LIKE ?)",email);
   Database.update(c,"DELETE FROM schedule_exceptions WHERE barber_id IN (SELECT id FROM users WHERE email LIKE ?)",email);
   Database.update(c,"DELETE FROM users WHERE email LIKE ?",email);
   Database.update(c,"DELETE FROM services WHERE name LIKE ?",name);
   Database.update(c,"DELETE FROM addons WHERE name LIKE ?",name);c.commit();
  }catch(Exception e){c.rollback();throw e;}}
  System.out.println("Removed only the fixtures for this QA run.");
 }
}
