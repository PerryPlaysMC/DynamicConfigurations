package io.dynamicstudios.configurations;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DefaultDynamicConfigurationSectionImpl implements IDynamicConfigurationSection {


 @Override
 public String getString(String path, String defaultValue) {
	Object value = get(path);
	return !(value instanceof String) ? (value != null ? (value instanceof List<?> ? ((List<?>) value).stream()
		 .map(o -> o + "").collect(Collectors.joining("\n")) : value.toString()) : defaultValue) : value.toString();
 }


 @Override
 public Double getDouble(String path, Double defaultValue) {
	Object value = get(path);
	Double f = defaultValue;
	if(!(value instanceof Double) && value != null)
	 try {
		f = Double.parseDouble(value.toString());
	 } catch(Exception ignored) {
	 }
	return !(value instanceof Double) ? (value != null ? f : defaultValue) : (Double) value;
 }


 @Override
 public Integer getInteger(String path, Integer defaultValue) {
	Object value = get(path);
	Integer f = defaultValue;
	if(!(value instanceof Integer) && value != null)
	 try {
		f = Integer.parseInt(value.toString());
	 } catch(Exception ignored) {
	 }
	return !(value instanceof Integer) ? (value != null ? f : defaultValue) : (Integer) value;
 }

 @Override
 public Long getLong(String path, Long defaultValue) {
	Object value = get(path);
	Long f = defaultValue;
	if(!(value instanceof Integer) && value != null)
	 try {
		f = Long.parseLong(value.toString());
	 } catch(Exception e) {
	 }
	return !(value instanceof Long) ? (value != null ? f : defaultValue) : (Long) value;
 }

 @Override
 public Number getNumber(String path, Number defaultValue) {
	Object value = get(path);
	Number f = defaultValue;
	if(!(value instanceof Integer) && value != null)
	 try {
		f = Long.parseLong(value.toString());
	 } catch(Exception e) {
		try {
		 f = Double.parseDouble(value.toString());
		} catch(Exception e1) {
		}
	 }
	return !(value instanceof Number) ? (value != null ? f : defaultValue) : (Number) value;
 }


 @Override
 public Float getFloat(String path, Float defaultValue) {
	Object value = get(path);
	Float f = defaultValue;
	if(!(value instanceof Float) && value != null)
	 try {
		f = Float.parseFloat(value.toString());
	 } catch(Exception ignored) {
	 }
	return !(value instanceof Float) ? (value != null ? f : defaultValue) : (Float) value;
 }


 @Override
 public Byte getByte(String path, Byte defaultValue) {
	Object value = get(path);
	Byte f = defaultValue;
	if(!(value instanceof Byte) && value != null)
	 try {
		f = Byte.parseByte(value.toString());
	 } catch(Exception ignored) {
	 }
	return !(value instanceof Byte) ? (value != null ? f : defaultValue) : (Byte) value;
 }


 @Override
 public Boolean getBoolean(String path, Boolean defaultValue) {
	Object value = get(path);
	Boolean f = defaultValue;
	if(!(value instanceof Boolean) && value != null)
	 try {
		f = Boolean.parseBoolean(value.toString());
	 } catch(Exception ignored) {
	 }
	return !(value instanceof Boolean) ? (value != null ? f : defaultValue) : (Boolean) value;
 }

 @Override
 public String getMessage(String path, String defaultValue) {
	Object value = get(path);
	if(value instanceof String) return value.toString().replace("\\n", "\n");
	if(value instanceof List) return String.join("\n", getListString(path));
	if(value instanceof String[]) return String.join("\n", (String[]) value);
	if(getString(path) != null) return getString(path);
	return defaultValue;
 }


 @Override
 public List<?> getList(String path, List<?> defaultValue) {
	Object value = get(path);
	if(value instanceof Object[]) return Arrays.asList((Object[]) value);
	return !(value instanceof List) ? defaultValue : (List<?>) value;
 }

 @Override
 public List<String> getListString(String path, List<String> defaultValue) {
	List<?> value = getList(path, null);
	try {
	 return value.stream().map(String::valueOf).collect(Collectors.toList());
	} catch(Exception e) {
	 return defaultValue;
	}
 }

 @Override
 public List<Double> getListDouble(String path, List<Double> defaultValue) {
	List<String> value = getListString(path, null);
	try {
	 return value.stream().map(Double::valueOf).collect(Collectors.toList());
	} catch(Exception e) {
	 return defaultValue;
	}
 }

 @Override
 public List<Integer> getListInteger(String path, List<Integer> defaultValue) {
	List<String> value = getListString(path, null);
	try {
	 return value.stream().map(Integer::valueOf).collect(Collectors.toList());
	} catch(Exception e) {
	 return defaultValue;
	}
 }


 @Override
 public List<Float> getListFloat(String path, List<Float> defaultValue) {
	List<String> value = getListString(path, null);
	try {
	 return value.stream().map(Float::valueOf).collect(Collectors.toList());
	} catch(Exception e) {
	 return defaultValue;
	}
 }


 @Override
 public List<Byte> getListByte(String path, List<Byte> defaultValue) {
	List<String> value = getListString(path, null);
	try {
	 return value.stream().map(Byte::valueOf).collect(Collectors.toList());
	} catch(Exception e) {
	 return defaultValue;
	}
 }


 @Override
 public List<Boolean> getListBoolean(String path, List<Boolean> defaultValue) {
	List<String> value = getListString(path, null);
	try {
	 return value.stream().map(Boolean::valueOf).collect(Collectors.toList());
	} catch(Exception e) {
	 return defaultValue;
	}
 }

 @Override
 public boolean contains(String path) {
	return contains(path, false);
 }

 @Override
 public boolean contains(String path, boolean ignoreDefaults) {
	return ignoreDefaults ? get(path, null) != null : get(path) != null;
 }

 @Override
 public boolean isInteger(String path) {
	try {
	 Integer.parseInt(get(path) + "");
	 return true;
	} catch(Exception e) {
	 return false;
	}
 }

 @Override
 public boolean isDouble(String path) {
	try {
	 Double.parseDouble(get(path) + "");
	 return true;
	} catch(Exception e) {
	 return false;
	}
 }

 @Override
 public boolean isBoolean(String path) {
	String bool = get(path) + "";
	return bool.equalsIgnoreCase("true") || bool.equalsIgnoreCase("false")
		 || bool.equalsIgnoreCase("yes") || bool.equalsIgnoreCase("no");
 }

 @Override
 public boolean isLong(String path) {
	try {
	 Long.parseLong(get(path) + "");
	 return true;
	} catch(Exception e) {
	 return false;
	}
 }

 @Override
 public boolean isShort(String path) {
	try {
	 Short.parseShort(get(path) + "");
	 return true;
	} catch(Exception e) {
	 return false;
	}
 }

 @Override
 public boolean isByte(String path) {
	try {
	 Byte.parseByte(get(path) + "");
	 return true;
	} catch(Exception e) {
	 return false;
	}
 }

 @Override
 public boolean isString(String path) {
	return get(path) instanceof String;
 }
}
