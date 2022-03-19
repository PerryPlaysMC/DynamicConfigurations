package config.yaml;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.DynamicConfigurationDirectory;
import config.DynamicConfigurationManager;
import config.IDynamicConfiguration;
import config.IDynamicConfigurationSection;
import config.json.DynamicJsonAdapter;
import config.json.DynamicJsonConfigurationSection;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/

public class DynamicYamlConfiguration implements IDynamicConfiguration {

   private Map<String, Object> data = new LinkedHashMap<>();

   private final File file;
   private File directory;
   private final JavaPlugin plugin;
   private final Supplier<InputStream> stream;
   protected final HashMap<String,String> COMMENTS = new HashMap<>();
   protected final HashMap<String,String> INLINE_COMMENTS = new HashMap<>();
   private boolean autoSave = false;
   private YamlConfiguration yaml;
   private String lastPath = "";

   private DynamicConfigurationDirectory configurationDirectory = null;

   public DynamicYamlConfiguration(JavaPlugin plugin, DynamicConfigurationDirectory directory, String name) {
      this(plugin,directory.directory(),name);
      this.configurationDirectory = directory;
   }
   public DynamicYamlConfiguration(JavaPlugin plugin, File directory, String name) {
      Validate.notNull(plugin);
      this.plugin = plugin;
      if(directory != null)
         this.directory = directory;
      else if(name.contains("/"))
         this.directory = new File(name.substring(0,name.lastIndexOf('/')));
      else this.directory = new File("plugins/" + plugin.getName());
      if(name.contains("/")) name = name.substring(name.lastIndexOf('/')+1);
      this.file = new File(this.directory, name.endsWith(".yml") ? name : name + ".yml");
      if(!this.file.exists()) regenerate();
      this.stream = null;
      reload();
   }
   public DynamicYamlConfiguration(JavaPlugin plugin, String directory, String name) {
      this(plugin, directory == null || directory.isEmpty() ? null : new File(directory), name);
   }

   public DynamicYamlConfiguration(JavaPlugin plugin, Supplier<InputStream> stream) {
      Validate.notNull(plugin);
      this.plugin = plugin;
      this.directory = null;
      this.file = null;
      this.stream = stream;
      reload();
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
   public String name() {
      return file.getName();
   }

   @Override
   public boolean autoSave() {
      return autoSave;
   }

   @Override
   public Map<String, String> comments() {
      return ImmutableMap.copyOf(COMMENTS);
   }

   @Override
   public Map<String, String> inlineComments() {
      return ImmutableMap.copyOf(INLINE_COMMENTS);
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
      String dir = (directory!=null?(directory.getPath().endsWith("/") ?
         directory.getPath() :directory.getPath()+"/"):"").replace(plugin.getDataFolder().getPath(),"");
      if(rsc == null) rsc = plugin.getResource((dir.equals("/")?"":dir)+ name());
      if(directory!=null&&!directory.isDirectory())directory.mkdirs();
      try {
         if(rsc == null) file.createNewFile();
         else DynamicConfigurationManager.writeFile(rsc,file);
      } catch (IOException e) {
         e.printStackTrace();
      }
      reload();
      return this;
   }
   @Override
   public IDynamicConfiguration save() {
      try {
         if(stream != null) return this;
         yaml.set(".",null);
         setToYML(this,yaml);
         String toString = yaml.saveToString();
         BufferedWriter writer = new BufferedWriter(new FileWriter(file));
         writer.write(DynamicConfigurationManager.prepareConfigString(toString, this, COMMENTS, INLINE_COMMENTS));
         writer.flush();
         writer.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return this;
   }

   private void setToYML(IDynamicConfigurationSection input, ConfigurationSection section) {
      for(Map.Entry<?, ?> entry : input.data().entrySet()) {
         String key = entry.getKey().toString();
         Object value = entry.getValue();
         if (value instanceof IDynamicConfigurationSection) {
            ConfigurationSection sec = section.createSection(key);
            setToYML((IDynamicConfigurationSection) value, sec);
            section.set(key, sec);
         } else {
            section.set(key, value instanceof String ? ((String)value).replace("\n","\\n") : value);
         }
      }
   }

   @Override
   public IDynamicConfiguration reload() {
      if(stream != null) {
         COMMENTS.clear();
         COMMENTS.putAll(DynamicConfigurationManager.getComments(stream.get()));
         INLINE_COMMENTS.clear();
         INLINE_COMMENTS.putAll(DynamicConfigurationManager.getInlineComments(stream.get()));
         yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(stream.get()));
         this.data = new LinkedHashMap<>();
         convertMapsToSections(DynamicConfigurationManager.getKeys(stream.get()), yaml,this);
         return this;
      }
      Supplier<InputStream> content = () -> DynamicConfigurationManager.getContent(file);
      COMMENTS.clear();
      COMMENTS.putAll(DynamicConfigurationManager.getComments(content.get()));
      INLINE_COMMENTS.clear();
      INLINE_COMMENTS.putAll(DynamicConfigurationManager.getInlineComments(content.get()));
      yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(content.get()));
      this.data = new LinkedHashMap<>();
      convertMapsToSections(DynamicConfigurationManager.getKeys(content.get()), yaml,this);
      return this;
   }

   private void convertMapsToSections(List<String> keys, ConfigurationSection input, IDynamicConfigurationSection section) {
      for(int i = 0; i < keys.size(); i++) {
         String key = keys.get(i);
         Object value = input.get(key);
         if (value instanceof ConfigurationSection) {
            IDynamicConfigurationSection sec = new DynamicYamlConfigurationSectionImpl(this,key,new LinkedHashMap<>());
            List<String> newKeys = keys.stream().filter(s->s.startsWith(key)&&!s.equals(key))
               .map(s->s.substring(s.split("\\.")[0].length()+1)).collect(Collectors.toList());
            this.convertMapsToSections(newKeys, (ConfigurationSection) value, sec);
            section.data().put(key, sec);
            i += newKeys.size();
         } else {
            section.data().put(key, value instanceof String ? ((String)value).replace("\\n","\n") : value);
         }
      }
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
         return autoSave ? save() : this;
      }
      for(int i = 0; i < paths.length; i++) {
         String lastKey = paths[i];
         if(i == paths.length-1) {
            start.set(lastKey, value);
         }else {
            if(!start.isSet(lastKey) || !(start.get(lastKey) instanceof IDynamicConfigurationSection))
               start.set(lastKey,new DynamicYamlConfigurationSectionImpl(this,lastKey,new LinkedHashMap<>()));
            start = (IDynamicConfigurationSection) start.get(lastKey);
         }
      }
      lastPath=path;
      return autoSave ? save() : this;
   }

   @Override
   public IDynamicConfigurationSection set(String path, Object value, String comment) {
      String cmt = Arrays.stream(comment.split("\n")).map(s->s.startsWith("#")?s:"#"+s).collect(Collectors.joining("\n"));
      if(!cmt.isEmpty())
         COMMENTS.put(path,cmt);
      return set(path,value);
   }

   @Override
   public IDynamicConfigurationSection setInline(String path, Object value, String comment) {
      String cmt = Arrays.stream(comment.split("\n")).map(s->s.startsWith("#")?s:"#"+s).collect(Collectors.joining("\n"));
      if(!cmt.isEmpty())
         INLINE_COMMENTS.put(path,cmt);
      return set(path,value);
   }

   @Override
   public IDynamicConfigurationSection comment(String... comment) {
      String cmt = Arrays.stream(comment).map(s->s.startsWith("#")?s:"#"+s).collect(Collectors.joining("\n"));
      if(!cmt.isEmpty())
         if(get(lastPath) instanceof IDynamicConfigurationSection)
            INLINE_COMMENTS.put(lastPath,cmt);
         else COMMENTS.put(lastPath,cmt);
      return this;
   }

   @Override
   public IDynamicConfigurationSection inlineComment(String... comment) {
      String cmt = Arrays.stream(comment).map(s->s.startsWith("#")?s:"#"+s).collect(Collectors.joining("\n"));
      if(!cmt.isEmpty())
         INLINE_COMMENTS.put(lastPath,cmt);
      return this;
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
      if(sec == null)
         set(path, sec = new DynamicYamlConfigurationSectionImpl(this,path.contains(".") ? path.substring(path.lastIndexOf('.')) : path, new LinkedHashMap<>()));
      return sec;
   }

   @Override
   public IDynamicConfigurationSection createSection(String path, String comment) {
      if(!comment.isEmpty())
         INLINE_COMMENTS.put(path, (comment.startsWith("#") ? "" : "#")+comment);
      return createSection(path);
   }

   @Override
   public String getString(String path) {
      return getString(path,null);
   }

   @Override
   public String getString(String path, String defaultValue) {
      Object value = get(path);
      return !(value instanceof String) ? (value != null ? (value instanceof List<?> ? ((List<?>)value).stream()
         .map(o->o+"").collect(Collectors.joining("\n")) : value.toString()) : defaultValue) : value.toString();
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
         try {f = Double.parseDouble(value.toString());}catch (Exception ignored) {}
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
         try {f = Integer.parseInt(value.toString());}catch (Exception ignored) {}
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
         try {f = Float.parseFloat(value.toString());}catch (Exception ignored) {}
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
         try {f = Byte.parseByte(value.toString());}catch (Exception ignored) {}
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
         try {f = Boolean.parseBoolean(value.toString());}catch (Exception ignored) {}
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
      if(value instanceof List) return String.join("\n", getListString(path));
      if(value instanceof String[]) return String.join("\n", (String[])value);
      if(getString(path) != null) return getString(path);
      return defaultValue;
   }

   @Override
   public List<?> getList(String path) {
      return getList(path,new ArrayList<>());
   }

   @Override
   public List<?> getList(String path, List<?> defaultValue) {
      Object value = get(path);
      return !(value instanceof List) ? (value != null ? new ArrayList<>(Collections.singletonList(value)) : defaultValue) : (List<?>) value;
   }

   @Override
   public List<String> getListString(String path) {
      return getListString(path, new ArrayList<>());
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