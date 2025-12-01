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

 /**
	* Gets the unique identifier of this configuration section.
	*
	* @return the ID of this section
	*/
 String id();

 /**
	* Gets the full path of this configuration section within the configuration hierarchy.
	*
	* @return the full path
	*/
 String fullPath();

 /**
	* Gets the options associated with this configuration section.
	*
	* @return the configuration options
	*/
 DynamicConfigurationOptions<?> options();

 /**
	* Gets the parent configuration section, if any.
	*
	* @return the parent section, or {@code null} if this is the root
	*/
 IDynamicConfigurationSection parent();

 /**
	* Gets the underlying data map for this configuration section.
	*
	* @return the data map
	*/
 Map<String, Object> data();

 /**
	* Saves the configuration to its underlying storage.
	*
	* @return this section for chaining
	*/
 IDynamicConfigurationSection save();

 /**
	* Reloads the configuration from its underlying storage.
	*
	* @return this section for chaining
	*/
 IDynamicConfigurationSection reload();

 /**
	* Gets all keys in this configuration section.
	*
	* @param deep whether to include keys from child sections recursively
	* @return a list of all keys
	*/
 List<String> getKeys(boolean deep);

 /**
	* Checks if a path is set in this configuration section.
	*
	* @param path the path to check
	* @return {@code true} if the path is set, {@code false} otherwise
	*/
 boolean isSet(String path);

 /**
	* Sets a value in the configuration at the provided path, but only if the path is not already set.
	*
	* @param path  the path in the configuration
	* @param value the value to set
	* @return this section for chaining
	*/
 default IDynamicConfigurationSection setIfNotSet(String path, Object value) {
	if (isSet(path)) {
	 return this;
	}
	return set(path, value);
 }

 /**
	* Sets a value in the configuration at the provided path with a comment, but only if the path is not already set.
	*
	* @param path    the path in the configuration
	* @param value   the value to set
	* @param comment the comment associated with the data
	* @return this section for chaining
	*/
 default IDynamicConfigurationSection setIfNotSet(String path, Object value, String comment) {
	if (isSet(path)) {
	 return this;
	}
	return set(path, value, comment);
 }

 /**
	* Sets a value in the configuration at the provided path with an inline comment, but only if the path is not already set.
	*
	* @param path    the path in the configuration
	* @param value   the value to set
	* @param comment the inline comment associated with the data
	* @return this section for chaining
	*/
 default IDynamicConfigurationSection setIfNotSetInline(String path, Object value, String comment) {
	if (isSet(path)) {
	 return this;
	}
	return setInline(path, value, comment);
 }

 /**
	* Sets a value in the configuration at the provided path.
	*
	* @param path  the path in the configuration
	* @param value the value to set
	* @return this section for chaining
	*/
 IDynamicConfigurationSection set(String path, Object value);

 /**
	* Sets a value in the configuration at the provided path with a comment.
	*
	* @param path    the path in the configuration
	* @param value   the value to set
	* @param comment the comment associated with the data
	* @return this section for chaining
	*/
 IDynamicConfigurationSection set(String path, Object value, String comment);

 /**
	* Sets a value in the configuration at the provided path with an inline comment.
	*
	* @param path    the path in the configuration
	* @param value   the value to set
	* @param comment the inline comment associated with the data
	* @return this section for chaining
	*/
 IDynamicConfigurationSection setInline(String path, Object value, String comment);

 /**
	* Adds a comment to the configuration file after the previous key.
	*
	* @param comment the lines of comment to add
	* @return this section for chaining
	*/
 IDynamicConfigurationSection comment(String... comment);

 /**
	* Adds an inline comment to the configuration file after the previous key.
	*
	* @param comment the lines of inline comment to add
	* @return this section for chaining
	*/
 IDynamicConfigurationSection inlineComment(String... comment);

 /**
	* Gets an object from the configuration.
	*
	* @param path the path where the object is located
	* @return the object from the configuration, or the default value if not set
	*/
 default Object get(String path) {
	return get(path, options().defaults() == null ? null : options().defaults().get(path));
 }

 /**
	* Gets an object from the configuration.
	*
	* @param path         the path where the object is located
	* @param defaultValue the default value to return if the path is not set
	* @return the object from the configuration, or {@code defaultValue} if not set
	*/
 Object get(String path, Object defaultValue);

 /**
	* Gets a deserialized object from the configuration.
	*
	* @param <T>            the type of the object
	* @param deserializeType the class of the object to deserialize to
	* @param path           the path where the object is located
	* @return the deserialized object from the configuration, or the default value if not set
	*/
 default <T> T get(Class<T> deserializeType, String path) {
	return get(deserializeType, path, options().defaults() == null ? null : options().defaults().get(deserializeType, path));
 }

 /**
	* Gets a deserialized object from the configuration.
	*
	* @param <T>            the type of the object
	* @param deserializeType the class of the object to deserialize to
	* @param path           the path where the object is located
	* @param defaultValue   the default value to return if the path is not set
	* @return the deserialized object from the configuration, or {@code defaultValue} if not set
	*/
 <T> T get(Class<T> deserializeType, String path, T defaultValue);

 /**
	* Checks if the path refers to a configuration section.
	*
	* @param path the path to check
	* @return {@code true} if the path is a configuration section, {@code false} otherwise
	*/
 default boolean isSection(String path) {
	return get(path) instanceof IDynamicConfigurationSection;
 }

 /**
	* Gets a child configuration section.
	*
	* @param path the path to the child section
	* @return the child configuration section
	*/
 IDynamicConfigurationSection getSection(String path);

 /**
	* Creates and gets a child configuration section.
	*
	* @param path the path where the section will be created
	* @return the created configuration section
	*/
 IDynamicConfigurationSection createSection(String path);

 /**
	* Creates and gets a child configuration section with a comment.
	*
	* @param path    the path where the section will be created
	* @param comment the comment for the section
	* @return the created configuration section
	*/
 IDynamicConfigurationSection createSection(String path, String comment);

 /**
	* Gets a string value from the configuration.
	*
	* @param path the path where the string is located
	* @return the string from the configuration, or the default value if not set
	*/
 default String getString(String path) {
	return getString(path, options().defaults() == null ? null : options().defaults().getString(path));
 }

 /**
	* Gets a string value from the configuration.
	*
	* @param path         the path where the string is located
	* @param defaultValue the default value to return if the path is not set
	* @return the string from the configuration, or {@code defaultValue} if not set
	*/
 String getString(String path, String defaultValue);

 /**
	* Gets an enum value from the configuration.
	*
	* @param <T>   the enum type
	* @param tEnum the class of the enum
	* @param path  the path where the enum is located
	* @return the enum value from the configuration, or the default value if not set
	*/
 default <T extends Enum<?>> T getEnum(Class<T> tEnum, String path) {
	return getEnum(tEnum, path, options().defaults() == null ? null : options().defaults().getEnum(tEnum, path));
 }

 /**
	* Gets an enum value from the configuration.
	*
	* @param <T>          the enum type
	* @param tEnum        the class of the enum
	* @param path         the path where the enum is located
	* @param defaultValue the default value to return if the path is not set
	* @return the enum value from the configuration, or {@code defaultValue} if not set
	*/
 <T extends Enum<?>> T getEnum(Class<T> tEnum, String path, T defaultValue);

 /**
	* Gets a double value from the configuration.
	*
	* @param path the path where the double is located
	* @return the double from the configuration, or the default value if not set
	*/
 default Double getDouble(String path) {
	return getDouble(path, options().defaults() == null ? null : options().defaults().getDouble(path));
 }

 /**
	* Gets a double value from the configuration.
	*
	* @param path         the path where the double is located
	* @param defaultValue the default value to return if the path is not set
	* @return the double from the configuration, or {@code defaultValue} if not set
	*/
 Double getDouble(String path, Double defaultValue);

 /**
	* Gets an integer value from the configuration.
	*
	* @param path the path where the integer is located
	* @return the integer from the configuration, or the default value if not set
	*/
 default Integer getInteger(String path) {
	return getInteger(path, options().defaults() == null ? null : options().defaults().getInteger(path));
 }

 /**
	* Gets an integer value from the configuration.
	*
	* @param path         the path where the integer is located
	* @param defaultValue the default value to return if the path is not set
	* @return the integer from the configuration, or {@code defaultValue} if not set
	*/
 Integer getInteger(String path, Integer defaultValue);

 /**
	* Gets a long value from the configuration.
	*
	* @param path the path where the long is located
	* @return the long from the configuration, or the default value if not set
	*/
 default Long getLong(String path) {
	return getLong(path, options().defaults() == null ? null : options().defaults().getLong(path));
 }

 /**
	* Gets a long value from the configuration.
	*
	* @param path         the path where the long is located
	* @param defaultValue the default value to return if the path is not set
	* @return the long from the configuration, or {@code defaultValue} if not set
	*/
 Long getLong(String path, Long defaultValue);

 /**
	* Gets a number value from the configuration.
	*
	* @param path the path where the number is located
	* @return the number from the configuration, or the default value if not set
	*/
 default Number getNumber(String path) {
	return getNumber(path, options().defaults() == null ? null : options().defaults().getNumber(path));
 }

 /**
	* Gets a number value from the configuration.
	*
	* @param path         the path where the number is located
	* @param defaultValue the default value to return if the path is not set
	* @return the number from the configuration, or {@code defaultValue} if not set
	*/
 Number getNumber(String path, Number defaultValue);

 /**
	* Gets a float value from the configuration.
	*
	* @param path the path where the float is located
	* @return the float from the configuration, or the default value if not set
	*/
 default Float getFloat(String path) {
	return getFloat(path, options().defaults() == null ? null : options().defaults().getFloat(path));
 }

 /**
	* Gets a float value from the configuration.
	*
	* @param path         the path where the float is located
	* @param defaultValue the default value to return if the path is not set
	* @return the float from the configuration, or {@code defaultValue} if not set
	*/
 Float getFloat(String path, Float defaultValue);

 /**
	* Gets a byte value from the configuration.
	*
	* @param path the path where the byte is located
	* @return the byte from the configuration, or the default value if not set
	*/
 default Byte getByte(String path) {
	return getByte(path, options().defaults() == null ? null : options().defaults().getByte(path));
 }

 /**
	* Gets a byte value from the configuration.
	*
	* @param path         the path where the byte is located
	* @param defaultValue the default value to return if the path is not set
	* @return the byte from the configuration, or {@code defaultValue} if not set
	*/
 Byte getByte(String path, Byte defaultValue);

 /**
	* Gets a boolean value from the configuration.
	*
	* @param path the path where the boolean is located
	* @return the boolean from the configuration, or the default value if not set
	*/
 default Boolean getBoolean(String path) {
	return getBoolean(path, options().defaults() == null ? null : options().defaults().getBoolean(path));
 }

 /**
	* Gets a boolean value from the configuration.
	*
	* @param path         the path where the boolean is located
	* @param defaultValue the default value to return if the path is not set
	* @return the boolean from the configuration, or {@code defaultValue} if not set
	*/
 Boolean getBoolean(String path, Boolean defaultValue);

 /**
	* Gets a message string from the configuration (aliased to string getter).
	*
	* @param path the path where the message is located
	* @return the message string from the configuration, or the default value if not set
	*/
 default String getMessage(String path) {
	return getMessage(path, options().defaults() == null ? null : options().defaults().getMessage(path));
 }

 /**
	* Gets a message string from the configuration (aliased to string getter).
	*
	* @param path         the path where the message is located
	* @param defaultValue the default value to return if the path is not set
	* @return the message string from the configuration, or {@code defaultValue} if not set
	*/
 String getMessage(String path, String defaultValue);

 /**
	* Gets a list of objects from the configuration.
	*
	* @param path the path where the list is located
	* @return the list from the configuration, or the default value if not set
	*/
 default List<?> getList(String path) {
	return getList(path, options().defaults() == null ? null : options().defaults().getList(path));
 }

 /**
	* Gets a list of objects from the configuration.
	*
	* @param path         the path where the list is located
	* @param defaultValue the default value to return if the path is not set
	* @return the list from the configuration, or {@code defaultValue} if not set
	*/
 List<?> getList(String path, List<?> defaultValue);

 /**
	* Gets a list of strings from the configuration.
	*
	* @param path the path where the list is located
	* @return the string list from the configuration, or the default value if not set
	*/
 default List<String> getListString(String path) {
	return getListString(path, options().defaults() == null ? null : options().defaults().getListString(path));
 }

 /**
	* Gets a list of strings from the configuration.
	*
	* @param path         the path where the list is located
	* @param defaultValue the default value to return if the path is not set
	* @return the string list from the configuration, or {@code defaultValue} if not set
	*/
 List<String> getListString(String path, List<String> defaultValue);

 /**
	* Gets a list of doubles from the configuration.
	*
	* @param path the path where the list is located
	* @return the double list from the configuration, or the default value if not set
	*/
 default List<Double> getListDouble(String path) {
	return getListDouble(path, options().defaults() == null ? null : options().defaults().getListDouble(path));
 }

 /**
	* Gets a list of doubles from the configuration.
	*
	* @param path         the path where the list is located
	* @param defaultValue the default value to return if the path is not set
	* @return the double list from the configuration, or {@code defaultValue} if not set
	*/
 List<Double> getListDouble(String path, List<Double> defaultValue);

 /**
	* Gets a list of integers from the configuration.
	*
	* @param path the path where the list is located
	* @return the integer list from the configuration, or the default value if not set
	*/
 default List<Integer> getListInteger(String path) {
	return getListInteger(path, options().defaults() == null ? null : options().defaults().getListInteger(path));
 }

 /**
	* Gets a list of integers from the configuration.
	*
	* @param path         the path where the list is located
	* @param defaultValue the default value to return if the path is not set
	* @return the integer list from the configuration, or {@code defaultValue} if not set
	*/
 List<Integer> getListInteger(String path, List<Integer> defaultValue);

 /**
	* Gets a list of floats from the configuration.
	*
	* @param path the path where the list is located
	* @return the float list from the configuration, or the default value if not set
	*/
 default List<Float> getListFloat(String path) {
	return getListFloat(path, options().defaults() == null ? null : options().defaults().getListFloat(path));
 }

 /**
	* Gets a list of floats from the configuration.
	*
	* @param path         the path where the list is located
	* @param defaultValue the default value to return if the path is not set
	* @return the float list from the configuration, or {@code defaultValue} if not set
	*/
 List<Float> getListFloat(String path, List<Float> defaultValue);

 /**
	* Gets a list of bytes from the configuration.
	*
	* @param path the path where the list is located
	* @return the byte list from the configuration, or the default value if not set
	*/
 default List<Byte> getListByte(String path) {
	return getListByte(path, options().defaults() == null ? null : options().defaults().getListByte(path));
 }

 /**
	* Gets a list of bytes from the configuration.
	*
	* @param path         the path where the list is located
	* @param defaultValue the default value to return if the path is not set
	* @return the byte list from the configuration, or {@code defaultValue} if not set
	*/
 List<Byte> getListByte(String path, List<Byte> defaultValue);

 /**
	* Gets a list of booleans from the configuration.
	*
	* @param path the path where the list is located
	* @return the boolean list from the configuration, or the default value if not set
	*/
 default List<Boolean> getListBoolean(String path) {
	return getListBoolean(path, options().defaults() == null ? null : options().defaults().getListBoolean(path));
 }

 /**
	* Gets a list of booleans from the configuration.
	*
	* @param path         the path where the list is located
	* @param defaultValue the default value to return if the path is not set
	* @return the boolean list from the configuration, or {@code defaultValue} if not set
	*/
 List<Boolean> getListBoolean(String path, List<Boolean> defaultValue);

 /**
	* Gets a list of enum values from the configuration.
	*
	* @param <T>   the enum type
	* @param tEnum the class of the enum
	* @param path  the path where the list is located
	* @return the enum list from the configuration, or the default value if not set
	*/
 default <T extends Enum<?>> List<T> getListEnum(Class<T> tEnum, String path) {
	return getListEnum(tEnum, path, options().defaults() == null ? null : options().defaults().getListEnum(tEnum, path));
 }

 /**
	* Gets a list of enum values from the configuration.
	*
	* @param <T>          the enum type
	* @param tEnum        the class of the enum
	* @param path         the path where the list is located
	* @param defaultValue the default value to return if the path is not set
	* @return the enum list from the configuration, or {@code defaultValue} if not set
	*/
 <T extends Enum<?>> List<T> getListEnum(Class<T> tEnum, String path, List<T> defaultValue);

 /**
	* Checks if the configuration contains the specified path.
	*
	* @param path the path to check
	* @return {@code true} if the path exists, {@code false} otherwise
	*/
 boolean contains(String path);

 /**
	* Checks if the configuration contains the specified path, optionally ignoring defaults.
	*
	* @param path          the path to check
	* @param ignoreDefaults whether to ignore default values when checking
	* @return {@code true} if the path exists, {@code false} otherwise
	*/
 boolean contains(String path, boolean ignoreDefaults);

 /**
	* Checks if the value at the path is an integer.
	*
	* @param path the path to check
	* @return {@code true} if the value is an integer, {@code false} otherwise
	*/
 boolean isInteger(String path);

 /**
	* Checks if the value at the path is a double.
	*
	* @param path the path to check
	* @return {@code true} if the value is a double, {@code false} otherwise
	*/
 boolean isDouble(String path);

 /**
	* Checks if the value at the path is a boolean.
	*
	* @param path the path to check
	* @return {@code true} if the value is a boolean, {@code false} otherwise
	*/
 boolean isBoolean(String path);

 /**
	* Checks if the value at the path is a long.
	*
	* @param path the path to check
	* @return {@code true} if the value is a long, {@code false} otherwise
	*/
 boolean isLong(String path);

 /**
	* Checks if the value at the path is a short.
	*
	* @param path the path to check
	* @return {@code true} if the value is a short, {@code false} otherwise
	*/
 boolean isShort(String path);

 /**
	* Checks if the value at the path is a byte.
	*
	* @param path the path to check
	* @return {@code true} if the value is a byte, {@code false} otherwise
	*/
 boolean isByte(String path);

 /**
	* Checks if the value at the path is a string.
	*
	* @param path the path to check
	* @return {@code true} if the value is a string, {@code false} otherwise
	*/
 boolean isString(String path);

 /**
	* Converts this configuration section to a human-readable string representation.
	*
	* @return a string representation of the section
	*/
 default String asString() {
	List<String> lines = new ArrayList<>();
	lines.add((id().isEmpty() ? "" : id() + ": ") + "{");
	for (Map.Entry<String, Object> key_val : data().entrySet()) {
	 String key = key_val.getKey();
	 Object val = key_val.getValue();
	 if (lines.size() > 2) {
		lines.set(lines.size() - 1, lines.get(lines.size() - 1) + ",");
	 }
	 String text = indent(1) + key + ": ";
	 if (val instanceof Map) {
		text += fromMap(2, (Map<?, ?>) val);
	 } else if (val instanceof IDynamicConfigurationSection) {
		text += fromMap(2, ((IDynamicConfigurationSection) val).data());
	 } else if (val instanceof Object[]) {
		text += Arrays.toString((Object[]) val);
	 } else {
		text += val;
	 }
	 lines.add(text);
	}
	lines.add("}");
	return String.join("\n", lines);
 }

 /**
	* Converts a map to a human-readable string representation with indentation.
	*
	* @param indent  the indentation level
	* @param objects the map to convert
	* @return a string representation of the map
	*/
 default String fromMap(int indent, Map<?, ?> objects) {
	List<String> lines = new ArrayList<>();
	lines.add("{");
	for (Map.Entry<?, ?> key_val : objects.entrySet()) {
	 Object key = key_val.getKey();
	 Object val = key_val.getValue();
	 if (lines.size() > 2) {
		lines.set(lines.size() - 1, lines.get(lines.size() - 1) + ",");
	 }
	 String text = indent(indent) + key + ": ";
	 if (val instanceof Map) {
		text += fromMap(indent + 1, (Map<?, ?>) val);
	 } else if (val instanceof IDynamicConfigurationSection) {
		text += fromMap(indent + 1, ((IDynamicConfigurationSection) val).data());
	 } else if (val instanceof Object[]) {
		text += Arrays.toString((Object[]) val);
	 } else {
		text += val;
	 }
	 lines.add(String.join("\n" + indent(indent), text.split("\n")));
	}
	lines.add(indent(indent - 1) + "}");
	return String.join("\n", lines);
 }

 /**
	* Generates an indentation string with the specified number of spaces.
	*
	* @param indents the number of indent levels
	* @return the indentation string
	*/
 default String indent(int indents) {
	StringBuilder res = new StringBuilder();
	for (int i = 0; i < indents; i++) {
	 res.append(" ");
	}
	return res.toString();
 }
}