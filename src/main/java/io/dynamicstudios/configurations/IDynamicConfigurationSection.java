package io.dynamicstudios.configurations;

import io.dynamicstudios.configurations.utils.DynamicConfigurationOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/

public interface IDynamicConfigurationSection {

  String id();

  String fullPath();
  
  DynamicConfigurationOptions<?> options();

  IDynamicConfigurationSection parent();

  Map<String, Object> data();

  /**
   * Save the config
   *
   * @return This
   */
  IDynamicConfigurationSection save();

  /**
   * Reload the config
   *
   * @return This
   */
  IDynamicConfigurationSection reload();


  /**
   * @return All key values
   */
  List<String> getKeys(boolean deep);

  /**
   * Check if the config has a path set
   *
   * @param path
   * @return Is the path set in the config
   */
  boolean isSet(String path);

  /**
   * Set a value in the config with the provided path, if it's not already set
   *
   * @param path  Path to set to the config
   * @param value Object to be set in the file
   * @return this
   */
  default IDynamicConfigurationSection setIfNotSet(String path, Object value) {
    if(isSet(path))
      return this;
    return set(path,value);
  }

  /**
   * Set a value in the config with the provided path with a comment, if it's not already set
   *
   * @param path    Path to set to the config
   * @param value   Object to be set in the file
   * @param comment The comment associated with the data
   * @return this
   */
  default IDynamicConfigurationSection setIfNotSet(String path, Object value, String comment) {
    if(isSet(path))
      return this;
    return set(path,value,comment);
  }

  /**
   * Set a value in the config with the provided path with an inline-comment, if it's not already set
   *
   * @param path    Path to set to the config
   * @param value   Object to be set in the file
   * @param comment The comment associated with the data
   * @return this
   */
  default IDynamicConfigurationSection setIfNotSetInline(String path, Object value, String comment) {
    if(isSet(path))
      return this;
    return setInline(path,value,comment);
  }

  /**
   * Set a value in the config with the provided path
   *
   * @param path  Path to set to the config
   * @param value Object to be set in the file
   * @return this
   */
  IDynamicConfigurationSection set(String path, Object value);

  /**
   * Set a value in the config with the provided path with a comment
   *
   * @param path    Path to set to the config
   * @param value   Object to be set in the file
   * @param comment The comment associated with the data
   * @return this
   */
  IDynamicConfigurationSection set(String path, Object value, String comment);

  /**
   * Set a value in the config with the provided path with an inline-comment
   *
   * @param path    Path to set to the config
   * @param value   Object to be set in the file
   * @param comment The comment associated with the data
   * @return this
   */
  IDynamicConfigurationSection setInline(String path, Object value, String comment);

  /**
   * Add a comment to the file from the previous key
   *
   * @param comment The comment
   * @return this
   */
  IDynamicConfigurationSection comment(String... comment);

  /**
   * Add an inline-comment to the file from the previous key
   *
   * @param comment The comment
   * @return this
   */
  IDynamicConfigurationSection inlineComment(String... comment);

  /**
   * Get an Object from the config
   *
   * @param path The path where the object is located
   * @return The object from the config, null if not set
   */
  default Object get(String path) {
    return get(path, options().defaults() == null ? null : options().defaults().get(path));
  }

  /**
   * Get an Object from the config
   *
   * @param path         The path where the object is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The object from the config, defaultValue if not set
   */
  Object get(String path, Object defaultValue);

  /**
   * Get an Object from the config
   *
   * @param path The path where the object is located
   * @return The object from the config, null if not set
   */
  default <T> T get(Class<T> deserializeType, String path) {
    return get(deserializeType, path, options().defaults() == null ? null : options().defaults().get(deserializeType, path));
  }

  /**
   * Get an Object from the config
   *
   * @param path         The path where the object is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The object from the config, defaultValue if not set
   */
  <T> T get(Class<T>  deserializeType, String path, T defaultValue);

  /**
   * Get a ConfigurationSection
   *
   * @param path The path where the configuration section is located
   * @return The Configuration Section
   */
  IDynamicConfigurationSection getSection(String path);

  /**
   * Create a ConfigurationSection
   *
   * @param path The path where the configuration section will be located
   * @return The Configuration Section that was created
   */
  IDynamicConfigurationSection createSection(String path);

  /**
   * Create a ConfigurationSection
   *
   * @param path The path where the configuration section will be located
   * @return The Configuration Section that was created
   */
  IDynamicConfigurationSection createSection(String path, String comment);

  /**
   * Get a String from the config
   *
   * @param path The path where the object is located
   * @return The String from the config, null if not set
   */
  default String getString(String path){
    return getString(path, options().defaults() == null ? null : options().defaults().getString(path));
  }

  /**
   * Get a String from the config
   *
   * @param path         The path where the object is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The String from the config, defaultValue if not set
   */
  String getString(String path, String defaultValue);

  /**
   * Get a Double from the config
   *
   * @param path The path where the Double is located
   * @return The String from the config, null if not set
   */
  default Double getDouble(String path){
    return getDouble(path, options().defaults() == null ? null : options().defaults().getDouble(path));
  }

  /**
   * Get a Double from the config
   *
   * @param path         The path where the object is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The Double from the config, defaultValue if not set
   */
  Double getDouble(String path, Double defaultValue);

  /**
   * Get a Integer from the config
   *
   * @param path The path where the Integer is located
   * @return The Integer from the config, null if not set
   */
  default Integer getInteger(String path){
    return getInteger(path, options().defaults() == null ? null : options().defaults().getInteger(path));
  }

  /**
   * Get a Integer from the config
   *
   * @param path         The path where the Integer is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The Integer from the config, defaultValue if not set
   */
  Integer getInteger(String path, Integer defaultValue);


  /**
   * Get a Integer from the config
   *
   * @param path The path where the Integer is located
   * @return The Integer from the config, null if not set
   */
  default Long getLong(String path){
    return getLong(path, options().defaults() == null ? null : options().defaults().getLong(path));
  }

  /**
   * Get a Integer from the config
   *
   * @param path         The path where the Integer is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The Integer from the config, defaultValue if not set
   */
  Long getLong(String path, Long defaultValue);


  /**
   * Get a Integer from the config
   *
   * @param path The path where the Integer is located
   * @return The Integer from the config, null if not set
   */
  default Number getNumber(String path){
    return getNumber(path, options().defaults() == null ? null : options().defaults().getNumber(path));
  }

  /**
   * Get a Integer from the config
   *
   * @param path         The path where the Integer is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The Integer from the config, defaultValue if not set
   */
  Number getNumber(String path, Number defaultValue);

  /**
   * Get a Float from the config
   *
   * @param path The path where the Float is located
   * @return The Float from the config, null if not set
   */
  default Float getFloat(String path){
    return getFloat(path, options().defaults() == null ? null : options().defaults().getFloat(path));
  }

  /**
   * Get a Float from the config
   *
   * @param path         The path where the Float is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The Float from the config, defaultValue if not set
   */
  Float getFloat(String path, Float defaultValue);

  /**
   * Get a Byte from the config
   *
   * @param path The path where the Byte is located
   * @return The Byte from the config, null if not set
   */
  default Byte getByte(String path){
    return getByte(path, options().defaults() == null ? null : options().defaults().getByte(path));
  }

  /**
   * Get a Byte from the config
   *
   * @param path         The path where the Byte is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The Byte from the config, defaultValue if not set
   */
  Byte getByte(String path, Byte defaultValue);

  /**
   * Get a Boolean from the config
   *
   * @param path The path where the Boolean is located
   * @return The Boolean from the config, null if not set
   */
  default Boolean getBoolean(String path){
    return getBoolean(path, options().defaults() == null ? null : options().defaults().getBoolean(path));
  }

  /**
   * Get a Boolean from the config
   *
   * @param path         The path where the Boolean is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The Boolean from the config, defaultValue if not set
   */
  Boolean getBoolean(String path, Boolean defaultValue);


  /**
   * Get a String from the config
   *
   * @param path The path where the String is located
   * @return The String from the config, null if not set
   */
  default String getMessage(String path){
    return getMessage(path, options().defaults() == null ? null : options().defaults().getMessage(path));
  }

  /**
   * Get a String from the config
   *
   * @param path         The path where the String is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The String from the config, defaultValue if not set
   */
  String getMessage(String path, String defaultValue);


  /**
   * Get a List<Object> from the config
   *
   * @param path The path where the object is located
   * @return The List<Object> from the config, null if not set
   */
  default List<?> getList(String path){
    return getList(path, options().defaults() == null ? null : options().defaults().getList(path));
  }

  /**
   * Get a List<Object> from the config
   *
   * @param path         The path where the object is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The List<Object> from the config, defaultValue if not set
   */
  List<?> getList(String path, List<?> defaultValue);


  /**
   * Get a List<String> from the config
   *
   * @param path The path where the object is located
   * @return The List<String> from the config, null if not set
   */
  default List<String> getListString(String path){
    return getListString(path, options().defaults() == null ? null : options().defaults().getListString(path));
  }

  /**
   * Get a List<String> from the config
   *
   * @param path         The path where the object is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The List<String> from the config, defaultValue if not set
   */
  List<String> getListString(String path, List<String> defaultValue);


  /**
   * Get a List<Double> from the config
   *
   * @param path The path where the object is located
   * @return The List<Double> from the config, null if not set
   */
  default List<Double> getListDouble(String path){
    return getListDouble(path, options().defaults() == null ? null : options().defaults().getListDouble(path));
  }

  /**
   * Get a List<Double> from the config
   *
   * @param path         The path where the object is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The List<Double> from the config, defaultValue if not set
   */
  List<Double> getListDouble(String path, List<Double> defaultValue);


  /**
   * Get a List<Integer> from the config
   *
   * @param path The path where the object is located
   * @return The List<Integer> from the config, null if not set
   */
  default List<Integer> getListInteger(String path){
    return getListInteger(path, options().defaults() == null ? null : options().defaults().getListInteger(path));
  }

  /**
   * Get a List<Integer> from the config
   *
   * @param path         The path where the object is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The List<Integer> from the config, defaultValue if not set
   */
  List<Integer> getListInteger(String path, List<Integer> defaultValue);


  /**
   * Get a List<Float> from the config
   *
   * @param path The path where the object is located
   * @return The List<Float> from the config, null if not set
   */
  default List<Float> getListFloat(String path){
    return getListFloat(path, options().defaults() == null ? null : options().defaults().getListFloat(path));
  }

  /**
   * Get a List<Float> from the config
   *
   * @param path         The path where the object is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The List<Float> from the config, defaultValue if not set
   */
  List<Float> getListFloat(String path, List<Float> defaultValue);


  /**
   * Get a List<Byte> from the config
   *
   * @param path The path where the object is located
   * @return The List<Byte> from the config, null if not set
   */
  default List<Byte> getListByte(String path){
    return getListByte(path, options().defaults() == null ? null : options().defaults().getListByte(path));
  }

  /**
   * Get a List<Byte> from the config
   *
   * @param path         The path where the object is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The List<Byte> from the config, defaultValue if not set
   */
  List<Byte> getListByte(String path, List<Byte> defaultValue);


  /**
   * Get a List<Boolean> from the config
   *
   * @param path The path where the object is located
   * @return The List<Boolean> from the config, null if not set
   */
  default List<Boolean> getListBoolean(String path){
    return getListBoolean(path, options().defaults() == null ? null : options().defaults().getListBoolean(path));
  }

  /**
   * Get a List<Boolean> from the config
   *
   * @param path         The path where the object is located
   * @param defaultValue If the path is not set, return defaultValue
   * @return The List<Boolean> from the config, defaultValue if not set
   */
  List<Boolean> getListBoolean(String path, List<Boolean> defaultValue);

  boolean contains(String path);

  boolean contains(String path, boolean ignoreDefaults);

  boolean isInteger(String path);

  boolean isDouble(String path);

  boolean isBoolean(String path);

  boolean isLong(String path);

  boolean isShort(String path);

  boolean isByte(String path);

  boolean isString(String path);

  default String asString() {
    List<String> lines = new ArrayList<>();
    lines.add((id().isEmpty() ? "" : id() + ": ") + "{");
    for(Map.Entry<String, Object> key_val : data().entrySet()) {
      String key = key_val.getKey();
      Object val = key_val.getValue();
      if(lines.size() > 2)
        lines.set(lines.size() - 1, lines.get(lines.size() - 1) + ",");
      String text = indent(1) + key + ": ";
      if(val instanceof Map) text += fromMap(2, (Map<?, ?>) val);
      else if(val instanceof IDynamicConfigurationSection) text += fromMap(2, ((IDynamicConfigurationSection) val).data());
      else if(val instanceof Object[]) text += Arrays.toString((Object[]) val);
      else text += val;
      lines.add(text);
    }
    lines.add("}");
    return String.join("\n", lines);
  }

  default String fromMap(int indent, Map<?, ?> objects) {
    List<String> lines = new ArrayList<>();
    lines.add("{");
    for(Map.Entry<?, ?> key_val : objects.entrySet()) {
      Object key = key_val.getKey();
      Object val = key_val.getValue();
      if(lines.size() > 2)
        lines.set(lines.size() - 1, lines.get(lines.size() - 1) + ",");
      String text = indent(indent) + key + ": ";
      if(val instanceof Map) text += fromMap(indent + 1, (Map<?, ?>) val);
      else if(val instanceof IDynamicConfigurationSection) text += fromMap(indent + 1, ((IDynamicConfigurationSection) val).data());
      else if(val instanceof Object[]) text += Arrays.toString((Object[]) val);
      else text += val;
      lines.add(String.join("\n" + indent(indent), text.split("\n")));
    }
    lines.add(indent(indent - 1) + "}");
    return String.join("\n", lines);
  }

  default String indent(int indents) {
    StringBuilder res = new StringBuilder();
    for(int i = 0; i < indents; i++) res.append(" ");
    return res.toString();
  }


}
