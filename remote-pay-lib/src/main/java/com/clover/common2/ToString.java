package com.clover.common2;

import android.text.TextUtils;

import java.util.LinkedList;
import java.util.List;

public class ToString {
  private final List<ValueHolder> valueHolders = new LinkedList<ValueHolder>();
  private boolean omitNullValues = false;

  protected final Object obj;

  public ToString(Object obj) {
    this.obj = obj;
  }

  public ToString add(String name, Object value) {
    addHolder(value).builder.append(name).append('=').append(value);
    return this;
  }

  public ToString add(String name, boolean value) {
    checkNameAndAppend(name).append(value);
    return this;
  }

  public ToString add(String name, char value) {
    checkNameAndAppend(name).append(value);
    return this;
  }

  public ToString add(String name, double value) {
    checkNameAndAppend(name).append(value);
    return this;
  }

  public ToString add(String name, float value) {
    checkNameAndAppend(name).append(value);
    return this;
  }

  public ToString add(String name, int value) {
    checkNameAndAppend(name).append(value);
    return this;
  }

  public ToString add(String name, long value) {
    checkNameAndAppend(name).append(value);
    return this;
  }

  private StringBuilder checkNameAndAppend(String name) {
    return addHolder().builder.append(name).append('=');
  }

  @Override
  public String toString() {
    // create a copy to keep it consistent in case value changes
    boolean omitNullValuesSnapshot = omitNullValues;
    boolean needsSeparator = false;

    String cls = obj.getClass().getSimpleName();
    if (TextUtils.isEmpty(cls)) {
      cls = obj.getClass().getSuperclass().getSimpleName();
    }

    StringBuilder builder = new StringBuilder(32).append(cls).append('{');
    for (ValueHolder valueHolder : valueHolders) {
      if (!omitNullValuesSnapshot || !valueHolder.isNull) {
        if (needsSeparator) {
          builder.append(", ");
        } else {
          needsSeparator = true;
        }
        CharSequence sequence = valueHolder.builder;
        builder.append(sequence);
      }
    }
    return builder.append('}').toString();
  }

  private ValueHolder addHolder() {
    ValueHolder valueHolder = new ValueHolder();
    valueHolders.add(valueHolder);
    return valueHolder;
  }

  private ValueHolder addHolder(Object value) {
    ValueHolder valueHolder = addHolder();
    valueHolder.isNull = (value == null);
    return valueHolder;
  }

  private static final class ValueHolder {
    final StringBuilder builder = new StringBuilder();
    boolean isNull;
  }
}
