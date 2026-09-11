package com.barber.model;
public record User(long id, String name, String email, String role) implements java.io.Serializable {}
