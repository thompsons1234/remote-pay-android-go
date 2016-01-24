package com.clover.remote.protocol;

import com.clover.sdk.JSONifiable;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class CloverSdkSerializer<T extends JSONifiable> implements JsonSerializer<T> {
  @Override
  public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.getJSONObject().toString());
  }
}
