package dev.perryplaysmc.dynamicconfigurations.utils;

import dev.perryplaysmc.dynamicconfigurations.IDynamicConfiguration;

/**
 * Creator: PerryPlaysMC
 * Created: 03/2022
 **/
public class DynamicConfigurationOptions {

  private final StringBuilder indentString = new StringBuilder("  ");
  private final IDynamicConfiguration parentConfiguration;
  private int indent = 2;
  private boolean autoSave = false;
  private boolean appendMissingKeys = false;
  private StringWrap stringWrap = StringWrap.DOUBLE_QUOTED;

  public DynamicConfigurationOptions(IDynamicConfiguration parentConfiguration) {
    this.parentConfiguration = parentConfiguration;
  }

  /**
   * Indent length
   */
  public int indent() {
    return indent;
  }

  /**
   * How far indented would you like it?
   *
   * @param indent Indention length
   */
  public DynamicConfigurationOptions indent(int indent) {
    this.indent = indent;
    indentString.setLength(0);
    for(int i = 0; i < indent; i++)
      indentString.append(" ");
    return this;
  }

  public String indentString() {
    return indentString.toString();
  }

  /**
   * Will it save whenever it is edited
   */
  public boolean autoSave() {
    return autoSave;
  }

  /**
   * Should it save whenever it is edited
   */
  public DynamicConfigurationOptions autoSave(boolean autoSave) {
    this.autoSave = autoSave;
    return this;
  }

  public boolean appendMissingKeys() {
    return appendMissingKeys;
  }

  public DynamicConfigurationOptions appendMissingKeys(boolean appendMissingKeys) {
    this.appendMissingKeys = appendMissingKeys;
    return this;
  }

  public StringWrap stringWrap() {
    return stringWrap;
  }

  public DynamicConfigurationOptions stringWrap(StringWrap stringWrap) {
    this.stringWrap = stringWrap;
    return this;
  }


  public IDynamicConfiguration configuration() {
    return parentConfiguration;
  }

}
