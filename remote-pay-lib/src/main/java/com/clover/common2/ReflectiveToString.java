package com.clover.common2;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReflectiveToString extends ToString {
  private static final Pattern[] IGNORE_FIELD_PATTERNS = new Pattern[]{
      Pattern.compile("^this\\$\\d+$"),
      Pattern.compile("^serialVersionUID$")};

  public ReflectiveToString(Object obj) {
    super(obj);
  }

  @Override
  public String toString() {
    Set<Field> fields = getFields();
    fields:
    for (Field f : fields) {
      String fn = f.getName();
      for (Pattern p : IGNORE_FIELD_PATTERNS) {
        Matcher m = p.matcher(fn);
        if (m.matches()) {
          continue fields;
        }
      }

      f.setAccessible(true);
      try {
        add(fn, f.get(obj));
      } catch (IllegalArgumentException e) {
        // silently skip, should never happen
      } catch (IllegalAccessException e) {
        // silently skip, should never happen
      }
    }

    return super.toString();
  }

  private Set<Field> getFields() {
    Set<Field> fields = new HashSet<Field>();
    getFields(obj.getClass(), fields);

    return fields;
  }

  private static void getFields(Class<? extends Object> cls, Set<Field> fields) {
    Class<? extends Object> superclass = cls.getSuperclass();
    if (superclass != null) {
      getFields(superclass, fields);
    }
    fields.addAll(Arrays.asList(cls.getDeclaredFields()));
  }
}
