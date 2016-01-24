package com.clover.remote.protocol;


import com.clover.common2.ReflectiveToString;

public class RemoteMessage {
  public enum Type {COMMAND, QUERY, EVENT, PING, PONG}

  public final String id;
  public final Type type;
  public final String packageName;
  public final String method;
  public final String payload;

  public RemoteMessage(String id, Type type, String packageName, String method, String payload) {
    this.id = id;
    this.type = type;
    this.packageName = packageName;
    this.method = method;
    this.payload = payload;
  }

  @Override
  public String toString() {
    return new ReflectiveToString(this).toString();
  }
}
