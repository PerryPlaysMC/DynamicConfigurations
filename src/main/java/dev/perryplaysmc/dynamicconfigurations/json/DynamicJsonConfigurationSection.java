package dev.perryplaysmc.dynamicconfigurations.json;

import dev.perryplaysmc.dynamicconfigurations.DynamicConfigurationManager;
import dev.perryplaysmc.dynamicconfigurations.IDynamicConfigurationSection;
import dev.perryplaysmc.dynamicconfigurations.IDynamicConfigurationSerializer;
import dev.perryplaysmc.dynamicconfigurations.IDynamicConfigurationStringSerializer;
import dev.perryplaysmc.dynamicconfigurations.utils.DynamicConfigurationOptions;
import dev.perryplaysmc.dynamicconfigurations.yaml.DynamicYamlConfigurationSectionImpl;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/

public class DynamicJsonConfigurationSection implements IDynamicConfigurationSection {

  private final DynamicJsonConfiguration configuration;
  private final Map<String, Object> data;
  private final String id;
  private final String fullPath;
  private IDynamicConfigurationSection parent;

  protected DynamicJsonConfigurationSection(DynamicJsonConfiguration configuration, String id, Map<String, Object> data) {
    this(configuration, null, id, data);
  }

  protected DynamicJsonConfigurationSection(DynamicJsonConfiguration configuration, IDynamicConfigurationSection parent, String id, Map<String, Object> data) {
    this.configuration = configuration;
    this.parent = parent;
    this.id = id;
    this.data = data;
    if(parent != null) {
      StringBuilder fullPath = new StringBuilder(id());
      while(parent != null) {
        fullPath.insert(0, parent.id() + ".");
        parent = parent.parent();
      }
      this.fullPath = fullPath.toString();
    } else this.fullPath = "";
  }

  @Override
  public String fullPath() {
    return fullPath;
  }

  @Override
  public DynamicConfigurationOptions<?> options() {
    return configuration.options();
  }

  @Override
  public IDynamicConfigurationSection parent() {
    return parent;
  }

  public IDynamicConfigurationSection parent(IDynamicConfigurationSection parent) {
    this.parent = parent;
    return this;
  }

  @Override
  public String id() {
    return id;
  }

  public Map<String, Object> data() {
    return data;
  }

  @Override
  public IDynamicConfigurationSection save() {
    configuration.save();
    return this;
  }

  @Override
  public IDynamicConfigurationSection reload() {
    configuration.reload();
    return this;
  }

  private HashSet<String> keys(IDynamicConfigurationSection map, HashSet<String> keys) {
    for(String s : map.data().keySet()) {
      keys.add(s);
      if(map.get(s) instanceof IDynamicConfigurationSection) keys.addAll(((IDynamicConfigurationSection) map.get(s))
        .getKeys(true).stream().map(str -> s + "." + str).collect(Collectors.toList()));
    }
    return keys;
  }

  @Override
  public Set<String> getKeys(boolean deep) {
    if(!deep) return data.keySet();
    return keys(this, new HashSet<>());
  }

  @Override
  public boolean isSet(String path) {
    if(path.contains(".")) {
      String[] split = path.split("\\.");
      IDynamicConfigurationSection deep = this;
      for(String s : split) {
        if(deep.get(s) != null)
          if(deep.get(s) instanceof IDynamicConfigurationSection) deep = (IDynamicConfigurationSection) deep.get(s);
          else if(!(deep.get(s) instanceof IDynamicConfigurationSection)) return true;
      }
      return deep == this ? data.containsKey(split[split.length - 1]) : deep.isSet(split[split.length - 1]);
    }
    return data.containsKey(path);
  }

  @Override
  public IDynamicConfigurationSection set(String path, Object value) {
    String[] paths = path.split("\\.");
    IDynamicConfigurationSection start = this;
    if(paths.length == 1) {
      if(value != null && DynamicConfigurationManager.hasSerializer(value.getClass())) {
        IDynamicConfigurationSerializer<Object> serializer = DynamicConfigurationManager.serializer(value.getClass());
        if(serializer instanceof IDynamicConfigurationStringSerializer)
          data.put(paths[0], ((IDynamicConfigurationStringSerializer<Object>) serializer).serialize(value));
        else {
          IDynamicConfigurationSection section = new DynamicJsonConfigurationSection(configuration, this, path, new HashMap<>());
          data.put(path, section);
          serializer.serialize(section, value);
        }
        return configuration.options().autoSave() ? save() : this;
      }
      if(value == null) data.remove(paths[0]);
      else data.put(paths[0], value);
      return configuration.options().autoSave() ? save() : this;
    }
    for(int i = 0; i < paths.length; i++) {
      String lastKey = paths[i];
      if(i == paths.length - 1) {
        if(value instanceof DynamicJsonConfigurationSection && start != this)
          ((DynamicJsonConfigurationSection) value).parent(start);
        start.set(lastKey, value);
      } else {
        if(!start.isSet(lastKey) || !(start.get(lastKey) instanceof IDynamicConfigurationSection))
          start.set(lastKey, new DynamicJsonConfigurationSection(configuration, start == this ? null : start, lastKey, new LinkedHashMap<>()));
        start = (IDynamicConfigurationSection) start.get(lastKey);
      }
    }
    return configuration.options().autoSave() ? save() : this;
  }

  @Override
  public IDynamicConfigurationSection set(String path, Object value, String comment) {
    if(DynamicConfigurationManager.DEBUG_ENABLED)
      Bukkit.getLogger().log(Level.WARNING,
        "Comments are not supported in json files: ("+configuration.file()+")" + DynamicConfigurationManager.getStackTrace() + " '" + path + "'");
    return set(path, value);
  }

  @Override
  public IDynamicConfigurationSection setInline(String path, Object value, String comment) {
    if(DynamicConfigurationManager.DEBUG_ENABLED)
      Bukkit.getLogger().log(Level.WARNING,
        "Comments are not supported in json files: ("+configuration.file()+")" + DynamicConfigurationManager.getStackTrace() + " '" + path + "'");
    return set(path, value);
  }

  @Override
  public IDynamicConfigurationSection comment(String... comment) {
    if(DynamicConfigurationManager.DEBUG_ENABLED)
      Bukkit.getLogger().log(Level.WARNING,
        "Comments are not supported in json files("+configuration.file()+"): " + DynamicConfigurationManager.getStackTrace());
    return this;
  }

  @Override
  public IDynamicConfigurationSection inlineComment(String... comment) {
    if(DynamicConfigurationManager.DEBUG_ENABLED)
      Bukkit.getLogger().log(Level.WARNING, "Comments are not supported in json files("+configuration.file()+"): " + DynamicConfigurationManager.getStackTrace());
    return this;
  }

  @Override
  public Object get(String path, Object defaultValue) {
    if(path.contains(".")) {
      String[] split = path.split("\\.");
      IDynamicConfigurationSection deep = this;
      int indexes = 0;
      for(int i = 0, splitLength = split.length; i < splitLength; i++) {
        String key = split[i];
        Object val = (deep == this) ? deep.data().get(key) : deep.get(key);
        if(val != null) {
          if(val instanceof IDynamicConfigurationSection) deep = (IDynamicConfigurationSection) val;
          if(i == splitLength - 1) return val;
        } else break;
        indexes = i;
      }
      return data.getOrDefault(path, deep != this && indexes == split.length - 1 ? deep : defaultValue);
    }
    return data.getOrDefault(path, defaultValue);
  }

  @Override
  public <T> T get(Class<T> deserializeType, String path, T defaultValue) {
    if(!DynamicConfigurationManager.hasSerializer(deserializeType)) return null;
    IDynamicConfigurationSerializer<T> serializer = DynamicConfigurationManager.serializer(deserializeType);
    T deserialized = null;
    if(serializer instanceof IDynamicConfigurationStringSerializer)
      deserialized = ((IDynamicConfigurationStringSerializer<T>) serializer).deserialize(getString(path));
    else deserialized = (T) serializer.deserialize(getSection(path) == null ? this : getSection(path));
    return deserialized == null ? defaultValue : deserialized;
  }

  @Override
  public IDynamicConfigurationSection getSection(String path) {
    return (IDynamicConfigurationSection) get(path, null);
  }

  @Override
  public IDynamicConfigurationSection createSection(String path) {
    IDynamicConfigurationSection sec = getSection(path);
    if(sec == null)
      set(path, sec = new DynamicJsonConfigurationSection(configuration, path.contains(".") ? path.substring(path.lastIndexOf('.')) : path, new LinkedHashMap<>()));
    return sec;
  }

  @Override
  public IDynamicConfigurationSection createSection(String path, String comment) {
    if(DynamicConfigurationManager.DEBUG_ENABLED)
      Bukkit.getLogger().log(Level.WARNING,
        "Comments are not supported in json files: ("+configuration.file()+")" + DynamicConfigurationManager.getStackTrace() + " '" + path + "'");
    return createSection(path);
  }


  @Override
  public String getString(String path, String defaultValue) {
    Object value = get(path);
    return !(value instanceof String) ? (value != null ? value.toString() : defaultValue) : value.toString();
  }

  @Override
  public Double getDouble(String path, Double defaultValue) {
    Object value = get(path);
    Double f = defaultValue;
    if(!(value instanceof Double) && value != null)
      try {
        f = Double.parseDouble(value.toString());
      } catch (Exception e) {
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
      } catch (Exception e) {
      }
    return !(value instanceof Integer) ? (value != null ? f : defaultValue) : (Integer) value;
  }

  @Override
  public Float getFloat(String path, Float defaultValue) {
    Object value = get(path);
    Float f = defaultValue;
    if(!(value instanceof Float) && value != null)
      try {
        f = Float.parseFloat(value.toString());
      } catch (Exception e) {
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
      } catch (Exception e) {
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
      } catch (Exception e) {
      }
    return !(value instanceof Boolean) ? (value != null ? f : defaultValue) : (Boolean) value;
  }

  @Override
  public String getMessage(String path, String defaultValue) {
    Object value = get(path);
    if(value instanceof String) return value.toString().replace("\\n", "\n");
    if(value instanceof List) return String.join("\n", (Iterable<? extends CharSequence>) value);
    if(value instanceof String[]) return String.join("\n", (String[]) value);
    return defaultValue;
  }

  @Override
  public List<?> getList(String path, List<?> defaultValue) {
    Object value = get(path);
    return !(value instanceof List) ? defaultValue : (List<?>) value;
  }

  @Override
  public List<String> getListString(String path, List<String> defaultValue) {
    List<?> value = getList(path, null);
    try {
      return value.stream().map(String::valueOf).collect(Collectors.toList());
    } catch (Exception e) {
      return defaultValue;
    }
  }

  @Override
  public List<Double> getListDouble(String path, List<Double> defaultValue) {
    List<String> value = getListString(path, null);
    try {
      return value.stream().map(Double::valueOf).collect(Collectors.toList());
    } catch (Exception e) {
      return defaultValue;
    }
  }

  @Override
  public List<Integer> getListInteger(String path, List<Integer> defaultValue) {
    List<String> value = getListString(path, null);
    try {
      return value.stream().map(Integer::valueOf).collect(Collectors.toList());
    } catch (Exception e) {
      return defaultValue;
    }
  }


  @Override
  public List<Float> getListFloat(String path, List<Float> defaultValue) {
    List<String> value = getListString(path, null);
    try {
      return value.stream().map(Float::valueOf).collect(Collectors.toList());
    } catch (Exception e) {
      return defaultValue;
    }
  }


  @Override
  public List<Byte> getListByte(String path, List<Byte> defaultValue) {
    List<String> value = getListString(path, null);
    try {
      return value.stream().map(Byte::valueOf).collect(Collectors.toList());
    } catch (Exception e) {
      return defaultValue;
    }
  }


  @Override
  public List<Boolean> getListBoolean(String path, List<Boolean> defaultValue) {
    List<String> value = getListString(path, null);
    try {
      return value.stream().map(Boolean::valueOf).collect(Collectors.toList());
    } catch (Exception e) {
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
      Integer.parseInt(get(path)+"");
      return true;
    }catch (Exception e) {
      return false;
    }
  }

  @Override
  public boolean isDouble(String path) {
    try {
      Double.parseDouble(get(path)+"");
      return true;
    }catch (Exception e) {
      return false;
    }
  }

  @Override
  public boolean isBoolean(String path) {
    String bool = get(path)+"";
    return bool.equalsIgnoreCase("true")||bool.equalsIgnoreCase("false")
      ||bool.equalsIgnoreCase("yes")||bool.equalsIgnoreCase("no");
  }

  @Override
  public boolean isLong(String path) {
    try {
      Long.parseLong(get(path) + "");
      return true;
    }catch (Exception e) {
      return false;
    }
  }

  @Override
  public boolean isShort(String path) {
    try {
      Short.parseShort(get(path)+"");
      return true;
    }catch (Exception e) {
      return false;
    }
  }

  @Override
  public boolean isByte(String path) {
    try {
      Byte.parseByte(get(path)+"");
      return true;
    }catch (Exception e) {
      return false;
    }
  }

  @Override
  public boolean isString(String path) {
    return get(path) instanceof String;
  }

  @Override
  public String toString() {
    return asString();
  }
}
