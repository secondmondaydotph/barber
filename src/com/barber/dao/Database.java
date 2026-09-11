package com.barber.dao;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.io.*;
public final class Database {
 private Database() {}
 public static Properties settings() throws IOException {
  Properties p=new Properties(); String file=System.getProperty("barber.config",System.getenv().getOrDefault("BARBER_CONFIG",""));
  if(file.isBlank()){Path local=Path.of(".local","database.properties");if(Files.isRegularFile(local))file=local.toString();}
  if(!file.isBlank())try(InputStream in=Files.newInputStream(Path.of(file))){p.load(in);}
  p.putIfAbsent("url","jdbc:postgresql://localhost:5432/barber");p.putIfAbsent("user","postgres");p.putIfAbsent("password","");
  for(String key:List.of("url","user","password")){String value=System.getenv("BARBER_DB_"+key.toUpperCase());if(value!=null)p.setProperty(key,value);}
  String platformUrl=System.getenv("BARBER_DATABASE_URL");
  if(platformUrl!=null&&!platformUrl.isBlank())applyPlatformUrl(p,platformUrl);
  return p;
 }
 private static void applyPlatformUrl(Properties p,String value) throws IOException {
  try {
   java.net.URI uri=java.net.URI.create(value.replaceFirst("^postgres(?:ql)?://","postgresql://"));
   String[] credentials=Optional.ofNullable(uri.getUserInfo()).orElse("").split(":",2);
   String database=uri.getPath()==null?"":uri.getPath().replaceFirst("^/","");
   String query=uri.getQuery()==null?"":"?"+uri.getQuery();
   p.setProperty("url","jdbc:postgresql://"+uri.getHost()+":"+(uri.getPort()<0?5432:uri.getPort())+"/"+database+query);
   if(credentials.length>0&&!credentials[0].isBlank())p.setProperty("user",java.net.URLDecoder.decode(credentials[0],java.nio.charset.StandardCharsets.UTF_8));
   if(credentials.length>1)p.setProperty("password",java.net.URLDecoder.decode(credentials[1],java.nio.charset.StandardCharsets.UTF_8));
  } catch(RuntimeException e) { throw new IOException("BARBER_DATABASE_URL is invalid.",e); }
 }
 public static Connection open() throws SQLException {
  try{Class.forName("org.postgresql.Driver");Properties p=settings();Properties auth=new Properties();auth.setProperty("user",p.getProperty("user"));auth.setProperty("password",p.getProperty("password"));auth.setProperty("connectTimeout","5");auth.setProperty("socketTimeout","15");return DriverManager.getConnection(p.getProperty("url"),auth);}catch(IOException|ClassNotFoundException e){throw new SQLException("Database configuration unavailable.",e);}
 }
 public static List<Map<String,Object>> rows(Connection c,String sql,Object...params) throws SQLException {
  try(PreparedStatement p=c.prepareStatement(sql)){bind(p,params);try(ResultSet r=p.executeQuery()){List<Map<String,Object>> result=new ArrayList<>();while(r.next()){Map<String,Object> row=new LinkedHashMap<>();for(int i=1;i<=r.getMetaData().getColumnCount();i++)row.put(r.getMetaData().getColumnLabel(i),r.getObject(i));result.add(row);}return result;}}
 }
 public static List<Map<String,Object>> rows(String sql,Object...params)throws SQLException{try(Connection c=open()){return rows(c,sql,params);}}
 public static int update(Connection c,String sql,Object...params)throws SQLException{try(PreparedStatement p=c.prepareStatement(sql)){bind(p,params);return p.executeUpdate();}}
 public static int update(String sql,Object...params)throws SQLException{try(Connection c=open()){return update(c,sql,params);}}
 private static void bind(PreparedStatement p,Object[] values)throws SQLException{for(int i=0;i<values.length;i++)p.setObject(i+1,values[i]);}
 public static long number(Map<String,Object> r,String key){return ((Number)r.get(key)).longValue();}
}
