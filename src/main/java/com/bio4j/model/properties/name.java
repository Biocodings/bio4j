package com.bio4j.model.properties;

import com.bio4j.model.Property;
import com.bio4j.model.PropertyType;

public interface Name extends Property {

  public static enum type implements PropertyType<type, String> {
    name;
    public type value() { return name; }
    public Class<String> valueClass() { return String.class; }
  }

  public static type TYPE = type.name;

  public String name();
}