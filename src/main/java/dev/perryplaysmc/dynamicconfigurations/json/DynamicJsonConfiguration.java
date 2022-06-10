package dev.perryplaysmc.dynamicconfigurations.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import dev.perryplaysmc.dynamicconfigurations.*;
import dev.perryplaysmc.dynamicconfigurations.utils.DynamicConfigurationDirectory;
import dev.perryplaysmc.dynamicconfigurations.utils.DynamicConfigurationOptions;
import dev.perryplaysmc.dynamicconfigurations.utils.FileUtils;
import dev.perryplaysmc.dynamicconfigurations.yaml.DynamicYamlConfigurationSectionImpl;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/

public class DynamicJsonConfiguration implements IDynamicConfiguration {
  private final File file;
  private final JavaPlugin plugin;
  private final DynamicConfigurationOptions options;
  private final DynamicJsonAdapter adapter = new DynamicJsonAdapter(this);
  private final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(DynamicJsonConfigurationSection.class, adapter).create();
  private Map<String, Object> data = new LinkedHashMap<>();
  private File directory;
  private boolean isGhost = false;
  private Supplier<InputStream> stream;
  private DynamicConfigurationDirectory configurationDirectory = null;

  public DynamicJsonConfiguration(JavaPlugin plugin, File directory, String name) {
    this.plugin = plugin;
    if(directory == null) {
      if(name.contains("/")) directory = new File(name.substring(0, name.lastIndexOf('/')));
      else directory = new File(plugin==null?"plugins/" + plugin.getName():"");
    }
    if(name.contains("/")) {
      String dir = name.substring(0, name.lastIndexOf('/'));
      if(!directory.getPath().endsWith(dir))
        directory = new File(directory, dir);
      name = name.substring(name.lastIndexOf('/') + 1);
    }
    this.directory = directory;
    this.file = new File(directory, name + (!name.endsWith(".json") ? ".json" : ""));
    if(!this.file.exists()) regenerate();
    this.stream = () -> FileUtils.findStream(plugin, file);
    this.options = new DynamicConfigurationOptions(this);
    this.configurationDirectory = DynamicConfigurationManager.getConfigurationDirectory(directory);
    reload();
    adapter.gson(GSON);
  }
  public DynamicJsonConfiguration(String name) {
    this(null, (File)null, name);
  }


  public DynamicJsonConfiguration(JavaPlugin plugin, String directory, String name) {
    this(plugin, directory == null || directory.isEmpty() ? null : new File(directory), name);
  }

  public DynamicJsonConfiguration(JavaPlugin plugin, DynamicConfigurationDirectory directory, String name) {
    this(plugin, directory.directory().getPath(), name);
    this.configurationDirectory = directory;
  }

  public DynamicJsonConfiguration(JavaPlugin plugin, Supplier<InputStream> inputStream, String name) {
    this(plugin, "", name);
    this.isGhost = true;
    this.stream = inputStream;
  }

  @Override
  public DynamicConfigurationDirectory configurationDirectory() {
    return configurationDirectory;
  }

  @Override
  public IDynamicConfiguration configurationDirectory(DynamicConfigurationDirectory directory) {
    if(!directory.directory().getPath().equals(directory().getPath())) {
      this.directory = directory.directory();
      file.renameTo(new File(this.directory, file.getName()));
      reload();
    }
    this.configurationDirectory = directory;
    return this;
  }

  @Override
  public String id() {
    return "";
  }

  @Override
  public String fullPath() {
    return "";
  }

  @Override
  public IDynamicConfigurationSection parent() {
    return null;
  }

  @Override
  public Map<String, Object> data() {
    return data;
  }

  @Override
  public File file() {
    return file;
  }

  @Override
  public File directory() {
    return directory;
  }

  @Override
  public JavaPlugin plugin() {
    return plugin;
  }

  @Override
  public DynamicConfigurationOptions options() {
    return options;
  }

  @Override
  public String name() {
    return file.getName();
  }

  @Override
  public Map<String, String> comments() {
    throw new UnsupportedOperationException("Comments are not supported in json files");
  }

  @Override
  public Map<String, String> inlineComments() {
    throw new UnsupportedOperationException("Comments are not supported in json files");
  }

  @Override
  public IDynamicConfiguration regenerate() {
    if(isGhost) return reload();

    if(this.file.exists()) this.file.delete();
    InputStream rsc = plugin==null?null:plugin.getResource(name());
    String dir = (directory != null ? (directory.getPath().endsWith("/") ?
      directory.getPath() : directory.getPath() + "/") : "").replace(plugin==null?"":plugin.getDataFolder().getPath(), "");
    if(rsc == null&&plugin!=null) rsc = plugin.getResource((dir.equals("/") ? "" : dir) + name());
    if(directory != null && !directory.isDirectory()) directory.mkdirs();
    try {
      if(rsc == null) file.createNewFile();
      else FileUtils.writeFile(rsc, file);
    } catch (IOException e) {
      e.printStackTrace();
    }
    reload();
    return this;
  }

  @Override
  public String saveToString() {
    final Gson gsonPrettyPrinting = new GsonBuilder().setPrettyPrinting().create();
    try(final StringWriter pWriter = new StringWriter()) {
      final JsonWriter jWriter = gsonPrettyPrinting.newJsonWriter(pWriter);
      StringBuilder indent = new StringBuilder();
      for(int i = 0; i < options.indent(); i++) indent.append(" ");
      jWriter.setIndent(indent.toString());
      gsonPrettyPrinting.toJson(GSON.toJsonTree(data), jWriter);
      return pWriter.toString();
    } catch (final IOException ignored) {
    }
    return GSON.toJson(data);
  }

  @Override
  public IDynamicConfiguration save() {
    if(isGhost) return this;
    if(stream!=null&&stream.get()!=null)
      if(options().appendMissingKeys() && DynamicConfigurationManager.isMissingKeys(this, stream)) {
        DynamicConfigurationManager.appendMissingKeysFrom(stream, this);
      }
    FileUtils.writeFile(Collections.singletonList(saveToString()), file);
    return this;
  }

  @Override
  public IDynamicConfiguration reload() {
    try {
      if(isGhost)
        data = (Map<String, Object>) GSON.fromJson(new InputStreamReader(stream.get()), Map.class);
      else {
        if(!this.file.exists()) regenerate();
        data = (Map<String, Object>) GSON.fromJson(new FileReader(file), Map.class);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    if(data == null) data = new LinkedHashMap<>();
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
        else serializer.serialize(start,value);
        return options.autoSave() ? save() : this;
      }
      if(value == null) data.remove(paths[0]);
      else data.put(paths[0], value);
      return options.autoSave() ? save() : this;
    }
    for(int i = 0; i < paths.length; i++) {
      String lastKey = paths[i];
      if(i == paths.length - 1) {
        if(value instanceof DynamicYamlConfigurationSectionImpl && start != this)
          ((DynamicYamlConfigurationSectionImpl) value).parent(start);
        start.set(lastKey, value);
      } else {
        if(!start.isSet(lastKey) || !(start.get(lastKey) instanceof IDynamicConfigurationSection))
          start.set(lastKey, new DynamicJsonConfigurationSection(this, start == this ? null : start, lastKey, new LinkedHashMap<>()));
        start = (IDynamicConfigurationSection) start.get(lastKey);
      }
    }
    return options.autoSave() ? save() : this;
  }

  @Override
  public IDynamicConfigurationSection set(String path, Object value, String comment) {
    throw new UnsupportedOperationException("Comments are not supported in json files");
  }

  @Override
  public IDynamicConfigurationSection setInline(String path, Object value, String comment) {
    throw new UnsupportedOperationException("Comments are not supported in json files");
  }

  @Override
  public IDynamicConfigurationSection comment(String... comment) {
    throw new UnsupportedOperationException("Comments are not supported in json files");
  }

  @Override
  public IDynamicConfigurationSection inlineComment(String... comment) {
    throw new UnsupportedOperationException("Comments are not supported in json files");
  }

  @Override
  public Object get(String path) {
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
      return data.getOrDefault(path, deep != this && indexes == split.length - 1 ? deep : null);
    }
    return data.getOrDefault(path, null);
  }


  @Override
  public Object get(Class<?> deserializeType, String path) {
    if(!DynamicConfigurationManager.hasSerializer(deserializeType)) return get(path);
    IDynamicConfigurationSerializer<Object> serializer = DynamicConfigurationManager.serializer(deserializeType);
    if(serializer instanceof IDynamicConfigurationStringSerializer)
      return ((IDynamicConfigurationStringSerializer<Object>)serializer).deserialize(getString(path));
    return serializer.deserialize(getSection(path) == null ? this : getSection(path));
  }

  @Override
  public IDynamicConfigurationSection getSection(String path) {
    return (IDynamicConfigurationSection) get(path, null);
  }

  @Override
  public IDynamicConfigurationSection createSection(String path) {
    IDynamicConfigurationSection sec = getSection(path);
    if(sec == null)
      set(path, sec = new DynamicJsonConfigurationSection(this, path.contains(".") ? path.substring(path.lastIndexOf('.')) : path, new LinkedHashMap<>()));
    return sec;
  }

  @Override
  public IDynamicConfigurationSection createSection(String path, String comment) {
    throw new UnsupportedOperationException("Comments are not supported in json files");
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
    return !(value instanceof Double) ? f : (Double) value;
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
    return !(value instanceof Integer) ? f : (Integer) value;
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
    return !(value instanceof Float) ? f : (Float) value;
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
    return !(value instanceof Byte) ? f : (Byte) value;
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
    return !(value instanceof Boolean) ? f : (Boolean) value;
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
  public String toString() {
    return asString();
  }
}
