package com.clover.remote.protocol;

import com.clover.sdk.JSONifiable;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

public class CloverSdkDeserializer<T extends JSONifiable> implements JsonDeserializer<T> {
  private final Class<T> cls;

  public CloverSdkDeserializer(Class<T> cls) {
    this.cls = cls;
  }

  @Override
  public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    String s = json.getAsJsonPrimitive().getAsString();
    try {
      Constructor<T> ctor = cls.getConstructor(String.class);
      return ctor.newInstance(s);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
