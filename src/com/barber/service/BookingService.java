package com.barber.service;
import com.barber.dao.Database;
import com.barber.model.User;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.util.*;
public final class BookingService {
 public static final ZoneId ZONE=ZoneId.of("Asia/Manila");
 public record Quote(int minutes,BigDecimal price,String description){}
 public static LocalDate today(){return LocalDate.now(ZONE);}
 public static void validDate(LocalDate date){if(date.isBefore(today())||date.isAfter(today().plusDays(90)))throw new IllegalArgumentException("Choose a date within the next 90 days.");}
 public Quote quote(Connection c,long service,Set<Long> addons)throws SQLException{
  var ss=Database.rows(c,"SELECT * FROM services WHERE id=? AND active",service);if(ss.isEmpty())throw new IllegalArgumentException("Select an active service.");
  var s=ss.get(0);int minutes=((Number)s.get("minutes")).intValue();BigDecimal price=(BigDecimal)s.get("price");String description=(String)s.get("name");
  if(addons.size()>10)throw new IllegalArgumentException("Select at most ten add-ons.");
  for(long id:new TreeSet<>(addons)){var aa=Database.rows(c,"SELECT * FROM addons WHERE id=? AND active",id);if(aa.isEmpty())throw new IllegalArgumentException("An add-on is unavailable.");var a=aa.get(0);minutes+=((Number)a.get("minutes")).intValue();price=price.add((BigDecimal)a.get("price"));description+=" + "+a.get("name");}
  if(minutes>480)throw new IllegalArgumentException("The appointment is too long.");return new Quote(minutes,price,description);
 }
 private List<LocalTime> slots(Connection c,long barber,LocalDate date,int minutes)throws SQLException{
  validDate(date);if(Database.rows(c,"SELECT id FROM users WHERE id=? AND role='BARBER' AND active",barber).isEmpty())throw new IllegalArgumentException("Select an active barber.");
  var exceptions=Database.rows(c,"SELECT * FROM schedule_exceptions WHERE barber_id=? AND day=?",barber,date);
  LocalTime start,end;
  if(!exceptions.isEmpty()){var ex=exceptions.get(0);if(Boolean.TRUE.equals(ex.get("closed")))return List.of();start=((Time)ex.get("start_time")).toLocalTime();end=((Time)ex.get("end_time")).toLocalTime();}
  else {var schedule=Database.rows(c,"SELECT * FROM weekly_schedules WHERE barber_id=? AND weekday=? AND active",barber,date.getDayOfWeek().getValue());if(schedule.isEmpty())return List.of();start=((Time)schedule.get(0).get("start_time")).toLocalTime();end=((Time)schedule.get(0).get("end_time")).toLocalTime();}
  var booked=Database.rows(c,"SELECT starts_at,ends_at FROM bookings WHERE barber_id=? AND starts_at>=? AND starts_at<? AND status<>'CANCELLED'",barber,date.atStartOfDay(),date.plusDays(1).atStartOfDay());
  List<LocalTime> available=new ArrayList<>();
  for(LocalDateTime cursor=date.atTime(start);!cursor.plusMinutes(minutes).isAfter(date.atTime(end));cursor=cursor.plusMinutes(30)){
   if(!cursor.isAfter(LocalDateTime.now(ZONE)))continue;LocalDateTime slot=cursor,finish=cursor.plusMinutes(minutes);
   if(booked.stream().noneMatch(b->slot.isBefore(((Timestamp)b.get("ends_at")).toLocalDateTime())&&finish.isAfter(((Timestamp)b.get("starts_at")).toLocalDateTime())))available.add(slot.toLocalTime());
  }return available;
 }
 public List<LocalTime> availability(long barber,LocalDate date,long service,Set<Long> addons)throws SQLException{try(Connection c=Database.open()){return slots(c,barber,date,quote(c,service,addons).minutes());}}
 public long book(User customer,long barber,LocalDate date,LocalTime time,long service,Set<Long> addons)throws SQLException {
  if(!customer.role().equals("CUSTOMER"))throw new IllegalArgumentException("Only customers can book an appointment.");
  try(Connection c=Database.open()){c.setAutoCommit(false);try{
   Database.rows(c,"SELECT pg_advisory_xact_lock(?)",-customer.id());
   Database.rows(c,"SELECT pg_advisory_xact_lock(?)",barber);
   Quote q=quote(c,service,addons);if(!slots(c,barber,date,q.minutes()).contains(time))throw new IllegalArgumentException("This time is no longer available. Please choose another slot.");
   var own=Database.rows(c,"SELECT id FROM bookings WHERE customer_id=? AND status<>'CANCELLED' AND starts_at<? AND ends_at>?",customer.id(),date.atTime(time).plusMinutes(q.minutes()),date.atTime(time));
   if(!own.isEmpty())throw new IllegalArgumentException("You already have an overlapping appointment.");
   var inserted=Database.rows(c,"INSERT INTO bookings(customer_id,barber_id,service_id,starts_at,ends_at,total,description,status) VALUES(?,?,?,?,?,?,?,'CONFIRMED') RETURNING id",customer.id(),barber,service,date.atTime(time),date.atTime(time).plusMinutes(q.minutes()),q.price(),q.description());
   long id=Database.number(inserted.get(0),"id");for(long addon:addons)Database.update(c,"INSERT INTO booking_addons(booking_id,addon_id) VALUES(?,?)",id,addon);c.commit();return id;
  }catch(SQLException|RuntimeException e){c.rollback();throw e;}}
 }
 public List<Long> bookRecurring(User customer,long barber,LocalDate firstDate,LocalTime time,long service,Set<Long> addons,int intervalWeeks,int occurrences)throws SQLException{
  if(intervalWeeks<1||intervalWeeks>12||occurrences<2||occurrences>12)throw new IllegalArgumentException("Recurring appointments must repeat 2 to 12 times, every 1 to 12 weeks.");
  validDate(firstDate.plusWeeks((long)intervalWeeks*(occurrences-1)));
  try(Connection c=Database.open()){c.setAutoCommit(false);try{
   Database.rows(c,"SELECT pg_advisory_xact_lock(?)",-customer.id());Database.rows(c,"SELECT pg_advisory_xact_lock(?)",barber);
   Quote q=quote(c,service,addons);List<LocalDate> dates=new ArrayList<>();
   for(int i=0;i<occurrences;i++){LocalDate d=firstDate.plusWeeks((long)i*intervalWeeks);if(!slots(c,barber,d,q.minutes()).contains(time))throw new IllegalArgumentException("Recurring slot is unavailable on "+d+". No appointments were created.");dates.add(d);}
   long plan=Database.number(Database.rows(c,"INSERT INTO recurring_plans(customer_id,barber_id,service_id,appointment_time,interval_weeks,occurrences) VALUES(?,?,?,?,?,?) RETURNING id",customer.id(),barber,service,time,intervalWeeks,occurrences).get(0),"id");
   List<Long> ids=new ArrayList<>();for(LocalDate d:dates){long id=insert(c,customer.id(),barber,d,time,service,addons,q);Database.update(c,"INSERT INTO booking_recurring(booking_id,plan_id) VALUES(?,?)",id,plan);ids.add(id);}c.commit();return ids;
  }catch(SQLException|RuntimeException e){c.rollback();throw e;}}
 }
 private long insert(Connection c,long customer,long barber,LocalDate date,LocalTime time,long service,Set<Long> addons,Quote q)throws SQLException{
  var own=Database.rows(c,"SELECT id FROM bookings WHERE customer_id=? AND status<>'CANCELLED' AND starts_at<? AND ends_at>?",customer,date.atTime(time).plusMinutes(q.minutes()),date.atTime(time));
  if(!own.isEmpty())throw new IllegalArgumentException("You already have an overlapping appointment.");
  var row=Database.rows(c,"INSERT INTO bookings(customer_id,barber_id,service_id,starts_at,ends_at,total,description,status) VALUES(?,?,?,?,?,?,?,'CONFIRMED') RETURNING id",customer,barber,service,date.atTime(time),date.atTime(time).plusMinutes(q.minutes()),q.price(),q.description());
  long id=Database.number(row.get(0),"id");for(long addon:addons)Database.update(c,"INSERT INTO booking_addons(booking_id,addon_id) VALUES(?,?)",id,addon);return id;
 }
 public long joinWaitlist(User customer,long barber,LocalDate date,LocalTime time,long service,Set<Long> addons)throws SQLException{
  if(!customer.role().equals("CUSTOMER"))throw new IllegalArgumentException("Only customers can join the waitlist.");validDate(date);
  try(Connection c=Database.open()){c.setAutoCommit(false);try{Quote q=quote(c,service,addons);if(slots(c,barber,date,q.minutes()).contains(time))throw new IllegalArgumentException("That slot is currently available. Book it directly instead.");LocalDateTime requested=date.atTime(time);var conflicts=Database.rows(c,"SELECT id FROM bookings WHERE barber_id=? AND status<>'CANCELLED' AND starts_at<? AND ends_at>?",barber,requested.plusMinutes(q.minutes()),requested);if(conflicts.isEmpty())throw new IllegalArgumentException("That time is outside the barber's bookable schedule.");
   var row=Database.rows(c,"INSERT INTO waitlist_entries(customer_id,barber_id,service_id,requested_date,requested_time) VALUES(?,?,?,?,?) ON CONFLICT(customer_id,barber_id,service_id,requested_date,requested_time) DO UPDATE SET status='WAITING',offered_at=NULL RETURNING id",customer.id(),barber,service,date,time);long id=Database.number(row.get(0),"id");
   Database.update(c,"DELETE FROM waitlist_addons WHERE waitlist_id=?",id);for(long addon:addons)Database.update(c,"INSERT INTO waitlist_addons(waitlist_id,addon_id) VALUES(?,?)",id,addon);c.commit();return id;
  }catch(SQLException|RuntimeException e){c.rollback();throw e;}}
 }
 public long claimWaitlist(User customer,long waitlistId)throws SQLException{
  try(Connection c=Database.open()){c.setAutoCommit(false);try{var rows=Database.rows(c,"SELECT * FROM waitlist_entries WHERE id=? AND customer_id=? AND status='OFFERED' FOR UPDATE",waitlistId,customer.id());if(rows.isEmpty())throw new IllegalArgumentException("This waitlist offer is no longer available.");var w=rows.get(0);long barber=Database.number(w,"barber_id"),service=Database.number(w,"service_id");LocalDate date=((java.sql.Date)w.get("requested_date")).toLocalDate();LocalTime time=((Time)w.get("requested_time")).toLocalTime();Set<Long> addons=new HashSet<>();for(var a:Database.rows(c,"SELECT addon_id FROM waitlist_addons WHERE waitlist_id=?",waitlistId))addons.add(Database.number(a,"addon_id"));Quote q=quote(c,service,addons);if(!slots(c,barber,date,q.minutes()).contains(time))throw new IllegalArgumentException("That slot has already been filled.");long id=insert(c,customer.id(),barber,date,time,service,addons,q);Database.update(c,"UPDATE waitlist_entries SET status='BOOKED' WHERE id=?",waitlistId);c.commit();return id;}catch(SQLException|RuntimeException e){c.rollback();throw e;}}
 }
 public void status(User user,long id,String status)throws SQLException{
  try(Connection c=Database.open()){c.setAutoCommit(false);try{
   var r=Database.rows(c,"SELECT * FROM bookings WHERE id=? FOR UPDATE",id);if(r.isEmpty())throw new IllegalArgumentException("Booking not found.");var b=r.get(0);String old=(String)b.get("status");
   boolean owner=user.role().equals("CUSTOMER")&&Database.number(b,"customer_id")==user.id();
   boolean staff=user.role().equals("ADMIN")||(user.role().equals("BARBER")&&Database.number(b,"barber_id")==user.id());
   if(!owner&&!staff)throw new SecurityException("Not permitted.");
   if(owner&&(!status.equals("CANCELLED")||!old.equals("CONFIRMED")||!((Timestamp)b.get("starts_at")).toLocalDateTime().isAfter(LocalDateTime.now(ZONE))))throw new IllegalArgumentException("Only upcoming confirmed appointments can be cancelled.");
   boolean valid=(old.equals("CONFIRMED")&&Set.of("IN_PROGRESS","CANCELLED").contains(status))||(old.equals("IN_PROGRESS")&&status.equals("COMPLETED"));
   if(!valid)throw new IllegalArgumentException("That status change is not allowed.");
   Database.update(c,"UPDATE bookings SET status=? WHERE id=?",status,id);Database.update(c,"INSERT INTO booking_history(booking_id,actor_id,status) VALUES(?,?,?)",id,user.id(),status);
   if(status.equals("CANCELLED")){LocalDateTime start=((Timestamp)b.get("starts_at")).toLocalDateTime();var next=Database.rows(c,"SELECT id,customer_id FROM waitlist_entries WHERE barber_id=? AND requested_date=? AND requested_time=? AND status='WAITING' ORDER BY created_at,id LIMIT 1 FOR UPDATE SKIP LOCKED",Database.number(b,"barber_id"),start.toLocalDate(),start.toLocalTime());if(!next.isEmpty()){long wid=Database.number(next.get(0),"id"),customer=Database.number(next.get(0),"customer_id");Database.update(c,"UPDATE waitlist_entries SET status='OFFERED',offered_at=now() WHERE id=?",wid);Database.update(c,"INSERT INTO notifications(user_id,message,link) VALUES(?,?,?)",customer,"A waitlisted appointment is available on "+start.toLocalDate()+" at "+start.toLocalTime()+". Claim it before another booking takes the slot.","/my-bookings");}}
   c.commit();
  }catch(SQLException|RuntimeException e){c.rollback();throw e;}}
 }
}
