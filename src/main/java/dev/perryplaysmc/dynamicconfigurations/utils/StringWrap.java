package dev.perryplaysmc.dynamicconfigurations.utils;

import dev.perryplaysmc.dynamicconfigurations.DynamicConfigurationManager;
import dev.perryplaysmc.dynamicconfigurations.IDynamicConfiguration;

public enum StringWrap {
  NONE,
  DOUBLE_QUOTED('"'),
  SINGLE_QUOTED('\'');

  private final Character wrapWith;

  StringWrap(Character wrapWith) {
    this.wrapWith = wrapWith;
  }

  StringWrap() {
    this(null);
  }

  public Character wrapWith() {
    return wrapWith;
  }

  public static boolean isValid(char c) {
    return c == '"' || c == '\'';
  }
}