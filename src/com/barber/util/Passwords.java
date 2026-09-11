package com.barber.util;
import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
public final class Passwords {
 private static final SecureRandom RANDOM = new SecureRandom();
 private Passwords() {}
 public static String hash(String password) {
  if(password == null || password.length()<12 || password.length()>200) throw new IllegalArgumentException("Use a password of 12 to 200 characters.");
  byte[] salt=new byte[16]; RANDOM.nextBytes(salt);
  return "210000:"+Base64.getEncoder().encodeToString(salt)+":"+Base64.getEncoder().encodeToString(derive(password,salt,210000));
 }
 public static boolean verify(String password,String stored) {
  if(password==null || password.length()>200) return false;
  try {String[] p=stored.split(":"); int rounds=Integer.parseInt(p[0]); if(rounds<100000||rounds>1000000)return false; return MessageDigest.isEqual(Base64.getDecoder().decode(p[2]),derive(password,Base64.getDecoder().decode(p[1]),rounds));}catch(RuntimeException e){return false;}
 }
 private static byte[] derive(String password,byte[] salt,int rounds) {
  PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt,rounds,256);
  try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}catch(GeneralSecurityException e){throw new IllegalStateException(e);}finally{spec.clearPassword();}
 }
 public static String token(){byte[] b=new byte[32];RANDOM.nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
}
