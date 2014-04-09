package com.bio4j.model.properties;

import com.bio4j.model.Property;
import com.bio4j.model.PropertyType;

public interface Sequence extends Property {

  public static enum type implements PropertyType<type, String> {
    sequence;
    public type value() { return sequence; }
    public Class<String> valueClass() { return String.class; }
  }

  public static type TYPE = type.sequence;

  public String sequence();
}