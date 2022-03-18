package config.json;

import com.google.gson.*;
import config.DynamicConfigurationDirectory;
import config.DynamicConfigurationManager;
import config.IDynamicConfiguration;
import config.IDynamicConfigurationSection;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.YamlConfiguration;
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
   private Map<String, Object> data = new LinkedHashMap<>();

   private final File file;
   private File directory;
   private final JavaPlugin plugin;
   private boolean autoSave = false;
   private final Supplier<InputStream> stream;
   private DynamicConfigurationDirectory configurationDirectory = null;

   private final DynamicJsonAdapter adapter = new DynamicJsonAdapter(this);
   private final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(DynamicJsonConfigurationSection.class, adapter).create();

   public DynamicJsonConfiguration(JavaPlugin plugin, File directory, String name) {
      Validate.notNull(plugin);
      this.plugin = plugin;
      if(directory != null)
         this.directory = directory;
      else if(name.contains("/"))
         this.directory = new File(name.substring(0,name.lastIndexOf('/')));
      else this.directory = new File("plugins/" + plugin.getName());
      if(name.contains("/")) name = name.substring(name.lastIndexOf('/')+1);
      this.file = new File(this.directory, (name.endsWith(".json") ? name : name + ".json"));
      if(!this.file.exists()) regenerate();
      this.stream = null;
      reload();
      adapter.gson(GSON);
   }
   public DynamicJsonConfiguration(JavaPlugin plugin, String directory, String name) {
      this(plugin, directory == null || directory.isEmpty() ? null : new File(directory), name);
   }
   public DynamicJsonConfiguration(JavaPlugin plugin, Supplier<InputStream> stream) {
      Validate.notNull(plugin);
      this.plugin = plugin;
      this.directory = null;
      this.file = null;
      this.stream = stream;
      reload();
   }
   public DynamicJsonConfiguration(JavaPlugin plugin, DynamicConfigurationDirectory directory, String name) {
      this(plugin,directory.directory().getPath(),name);
      this.configurationDirectory = directory;
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
   public String name() {
      return file.getName();
   }

   @Override
   public boolean autoSave() {
      return autoSave;
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
   public IDynamicConfiguration autoSave(boolean autoSave) {
      this.autoSave = autoSave;
      return this;
   }

   @Override
   public IDynamicConfiguration regenerate() {
      if(stream != null) {
         return reload();
      }
      if(this.file.exists()) this.file.delete();
      InputStream rsc = plugin.getResource(name());
      if(rsc == null) rsc = plugin.getResource((directory!=null?(directory.getPath().endsWith("/")|| name().startsWith("/") ? directory.getPath() :directory.getPath()+"/"):"")+ name());
      if(directory!=null&&!directory.isDirectory())directory.mkdirs();
      try {
         if(rsc == null) file.createNewFile();
         else DynamicConfigurationManager.writeFile(rsc,file);
      } catch (IOException e) {
         e.printStackTrace();
      }
      return this;
   }

   @Override
   public IDynamicConfiguration save() {
      if(stream != null) return this;
      DynamicConfigurationManager.writeFile(Collections.singletonList(GSON.toJson(data)), file);
      return this;
   }
   @Override
   public IDynamicConfiguration reload() {
      try {
         if(stream != null)
            data = (Map<String, Object>) GSON.fromJson(new InputStreamReader(stream.get()), Map.class);
         else data = (Map<String, Object>) GSON.fromJson(new FileReader(file), Map.class);
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
            .getKeys(true).stream().map(str->s+"."+str).collect(Collectors.toList()));
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
            if(deep.get(s)!=null)
               if(deep.get(s) instanceof IDynamicConfigurationSection) deep = (IDynamicConfigurationSection) deep.get(s);
               else if(!(deep.get(s) instanceof IDynamicConfigurationSection)) return true;
         }
         return deep == this ? data.containsKey(split[split.length-1]) : deep.isSet(split[split.length-1]);
      }
      return data.containsKey(path);
   }

   @Override
   public IDynamicConfigurationSection set(String path, Object value) {
      String[] paths = path.split("\\.");
      IDynamicConfigurationSection start = this;
      if(paths.length == 1) {
         if(value == null) data.remove(paths[0]);
         else data.put(paths[0],value);
         return autoSave() ? save() : this;
      }
      for(int i = 0; i < paths.length; i++) {
         String lastKey = paths[i];
         if(i == paths.length-1) {
            start.set(lastKey, value);
         }else {
            if(!start.isSet(lastKey) || !(start.get(lastKey) instanceof IDynamicConfigurationSection))
               start.set(lastKey,new DynamicJsonConfigurationSection(this,lastKey,new LinkedHashMap<>()));
            start = (IDynamicConfigurationSection) start.get(lastKey);
         }
      }
      return autoSave() ? save() : this;
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
         for(String s : split) {
            if(deep.get(s) != null)
               if(deep.get(s) instanceof IDynamicConfigurationSection) deep = (IDynamicConfigurationSection) deep.get(s);
               else if(!(deep.get(s) instanceof IDynamicConfigurationSection)) return deep.get(s);
         }
         return data.getOrDefault(path, deep != this ? deep : null);
      }
      return data.getOrDefault(path, null);
   }

   @Override
   public Object get(String path, Object defaultValue) {
      return get(path) == null ? defaultValue : get(path);
   }

   @Override
   public IDynamicConfigurationSection getSection(String path) {
      return (IDynamicConfigurationSection) get(path, null);
   }

   @Override
   public IDynamicConfigurationSection createSection(String path) {
      IDynamicConfigurationSection sec = getSection(path);
      if(sec == null) set(path,sec = new DynamicJsonConfigurationSection(this,path.contains(".") ? path.substring(path.lastIndexOf('.')) : path, new LinkedHashMap<>()));
      return sec;
   }

   @Override
   public IDynamicConfigurationSection createSection(String path, String comment) {
      throw new UnsupportedOperationException("Comments are not supported in json files");
   }

   @Override
   public String getString(String path) {
      return getString(path,null);
   }

   @Override
   public String getString(String path, String defaultValue) {
      Object value = get(path);
      return !(value instanceof String) ? (value != null ? value.toString() : defaultValue) : value.toString();
   }

   @Override
   public Double getDouble(String path) {
      return getDouble(path, null);
   }

   @Override
   public Double getDouble(String path, Double defaultValue) {
      Object value = get(path);
      Double f = defaultValue;
      if(!(value instanceof Double) && value!=null)
         try {f = Double.parseDouble(value.toString());
         }catch (Exception e) {}
      return !(value instanceof Double) ? (value != null ? f : defaultValue) : (Double) value;
   }

   @Override
   public Integer getInteger(String path) {
      return null;
   }

   @Override
   public Integer getInteger(String path, Integer defaultValue) {
      Object value = get(path);
      Integer f = defaultValue;
      if(!(value instanceof Integer) && value!=null)
         try {f = Integer.parseInt(value.toString());
         }catch (Exception e) {}
      return !(value instanceof Integer) ? (value != null ? f : defaultValue) : (Integer) value;
   }

   @Override
   public Float getFloat(String path) {
      return getFloat(path,null);
   }

   @Override
   public Float getFloat(String path, Float defaultValue) {
      Object value = get(path);
      Float f = defaultValue;
      if(!(value instanceof Float) && value!=null)
         try {f = Float.parseFloat(value.toString());
         }catch (Exception e) {}
      return !(value instanceof Float) ? (value != null ? f : defaultValue) : (Float) value;
   }

   @Override
   public Byte getByte(String path) {
      return getByte(path,null);
   }

   @Override
   public Byte getByte(String path, Byte defaultValue) {
      Object value = get(path);
      Byte f = defaultValue;
      if(!(value instanceof Byte) && value!=null)
         try {f = Byte.parseByte(value.toString());
         }catch (Exception e) {}
      return !(value instanceof Byte) ? (value != null ? f : defaultValue) : (Byte) value;
   }

   @Override
   public Boolean getBoolean(String path) {
      return getBoolean(path,false);
   }

   @Override
   public Boolean getBoolean(String path, Boolean defaultValue) {
      Object value = get(path);
      Boolean f = defaultValue;
      if(!(value instanceof Boolean) && value!=null)
         try {f = Boolean.parseBoolean(value.toString());
         }catch (Exception e) {}
      return !(value instanceof Boolean) ? (value != null ? f : defaultValue) : (Boolean) value;
   }

   @Override
   public String getMessage(String path) {
      return getMessage(path,null);
   }

   @Override
   public String getMessage(String path, String defaultValue) {
      Object value = get(path);
      if(value instanceof String) return value.toString().replace("\\n","\n");
      if(value instanceof List) return String.join("\n", (Iterable<? extends CharSequence>) value);
      if(value instanceof String[]) return String.join("\n", (String[])value);
      return defaultValue;
   }

   @Override
   public List<?> getList(String path) {
      return getList(path,new ArrayList<>());
   }

   @Override
   public List<?> getList(String path, List<?> defaultValue) {
      Object value = get(path);
      return !(value instanceof List) ? (value != null ? Collections.singletonList(value) : defaultValue) : (List<?>) value;
   }

   @Override
   public List<String> getListString(String path) {
      return getListString(path,new ArrayList<>());
   }

   @Override
   public List<String> getListString(String path, List<String> defaultValue) {
      List<?> value = getList(path, null);
      return value == null ? defaultValue : (List<String>) value;
   }

   @Override
   public List<Double> getListDouble(String path) {
      return getListDouble(path,new ArrayList<>());
   }

   @Override
   public List<Double> getListDouble(String path, List<Double> defaultValue) {
      List<?> value = getList(path, null);
      return value == null ? defaultValue : (List<Double>) value;
   }

   @Override
   public List<Integer> getListInteger(String path) {
      return getListInteger(path,new ArrayList<>());
   }

   @Override
   public List<Integer> getListInteger(String path, List<Integer> defaultValue) {
      List<?> value = getList(path, null);
      return value == null ? defaultValue : (List<Integer>) value;
   }

   @Override
   public List<Float> getListFloat(String path) {
      return getListFloat(path,new ArrayList<>());
   }

   @Override
   public List<Float> getListFloat(String path, List<Float> defaultValue) {
      List<?> value = getList(path, null);
      return value == null ? defaultValue : (List<Float>) value;
   }

   @Override
   public List<Byte> getListByte(String path) {
      return getListByte(path,new ArrayList<>());
   }

   @Override
   public List<Byte> getListByte(String path, List<Byte> defaultValue) {
      List<?> value = getList(path, null);
      return value == null ? defaultValue : (List<Byte>) value;
   }

   @Override
   public List<Boolean> getListBoolean(String path) {
      return getListBoolean(path,new ArrayList<>());
   }

   @Override
   public List<Boolean> getListBoolean(String path, List<Boolean> defaultValue) {
      List<?> value = getList(path, null);
      return value == null ? defaultValue : (List<Boolean>) value;
   }

   @Override
   public String toString() {
      return asString();
   }
}
