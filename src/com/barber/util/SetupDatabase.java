package com.barber.util;
import com.barber.dao.Database;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
public final class SetupDatabase {
 public static void main(String[] args)throws Exception{
  if(args.length!=1)throw new IllegalArgumentException("Pass the Barber project directory.");
  Path root=Path.of(args[0]).toAbsolutePath();Properties settings=Database.settings();String url=settings.getProperty("url");
  if(!url.matches("jdbc:postgresql://(localhost|127\\.0\\.0\\.1):[0-9]+/barber"))throw new IllegalArgumentException("Automatic creation is restricted to a local database named barber.");
  String maintenance=url.substring(0,url.lastIndexOf('/')+1)+"postgres";
  try(Connection c=DriverManager.getConnection(maintenance,settings.getProperty("user"),settings.getProperty("password"))){
   if(Database.rows(c,"SELECT datname FROM pg_database WHERE datname='barber'").isEmpty()){try(Statement s=c.createStatement()){s.executeUpdate("CREATE DATABASE barber");}System.out.println("Created database barber.");}
   else System.out.println("Using existing database barber; no data will be dropped.");
  }
  try(Connection c=Database.open()){c.setAutoCommit(false);try{
   try(Statement s=c.createStatement()){s.execute(Files.readString(root.resolve("database/schema.sql")));}
   if(Database.rows(c,"SELECT id FROM users WHERE role='ADMIN'").isEmpty()){
    String password=Passwords.token();Database.update(c,"INSERT INTO users(name,email,password_hash,role) VALUES('Shop Administrator','admin@barber.local',?,'ADMIN')",Passwords.hash(password));
    Path file=root.resolve(".local/admin-login.txt");Files.createDirectories(file.getParent());
    Files.writeString(file,"Barber administrator\nEmail: admin@barber.local\nPassword: "+password+"\nKeep this private; it is not inside the web root.\n",StandardOpenOption.CREATE_NEW);
    System.out.println("Created administrator. Credentials saved privately to .local/admin-login.txt (not printed).");
   }
   c.commit();System.out.println("Barber schema and catalog ready.");
  }catch(Exception e){c.rollback();throw e;}}
 }
}
