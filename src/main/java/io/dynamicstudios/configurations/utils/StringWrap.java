package io.dynamicstudios.configurations.utils;

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