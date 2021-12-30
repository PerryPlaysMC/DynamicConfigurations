package config.yaml;

import config.DynamicConfigManager;
import config.IDynamicConfiguration;
import config.IDynamicConfigurationSection;
import config.json.DynamicJsonConfigurationSection;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Copy Right ©
 * This code is private
 * Owner: PerryPlaysMC *
 * Any attempts to use these program(s) may result in a penalty of up to $5,000 USD
 **/

public class DynamicYamlConfiguration implements IDynamicConfiguration {

   private Map<String, Object> data = new HashMap<>();

   private final File file;
   private final File directory;
   private final JavaPlugin plugin;
   private YamlConfiguration yaml;
   protected final UUID id = UUID.randomUUID();
   private boolean autoSave = false;
   int comments = 0;

   public DynamicYamlConfiguration(JavaPlugin plugin, String directory, String name) {
      Validate.notNull(plugin);
      this.plugin = plugin;
      if(directory == null || directory.isEmpty()) this.directory = new File("plugins/" + plugin.getName());
      else this.directory = new File(directory);
      file = new File(directory, name.endsWith(".yml") ? name : name + ".yml");
      if(!this.file.exists()) regenerate();
      reload();
   }



   @Override
   public String getId() {
      return "";
   }

   @Override
   public Map<String, Object> getData() {
      return data;
   }

   @Override
   public File getFile() {
      return file;
   }

   @Override
   public File getDirectory() {
      return directory;
   }

   @Override
   public String getName() {
      return file.getName();
   }

   @Override
   public boolean isAutoSave() {
      return autoSave;
   }

   @Override
   public IDynamicConfiguration autoSave(boolean autoSave) {
      this.autoSave = autoSave;
      return this;
   }

   @Override
   public IDynamicConfiguration regenerate() {
      if(this.file.exists()) this.file.delete();
      InputStream rsc = plugin.getResource(getName()+".yml");
      if(rsc == null) rsc = plugin.getResource((directory!=null?(directory.getPath().endsWith("/")||getName().startsWith("/") ?
         directory.getPath() :directory.getPath()+"/"):"")+getName()+".yml");
      if(directory!=null&&!directory.isDirectory())directory.mkdirs();
      try {
         if(rsc == null) file.createNewFile();
         else DynamicConfigManager.writeFile(id, rsc,file);
      } catch (IOException e) {
         e.printStackTrace();
      }
      reload();
      return this;
   }

   @Override
   public IDynamicConfiguration save() {
      try {
         yaml.set(".",null);
         setToYML(this,yaml);
         String toString = yaml.saveToString();
         BufferedWriter writer = new BufferedWriter(new FileWriter(file));
         writer.write(DynamicConfigManager.prepareConfigString(id,toString));
         writer.flush();
         writer.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return this;
   }

   private void setToYML(IDynamicConfigurationSection input, ConfigurationSection section) {
      for(Map.Entry<?, ?> entry : input.getData().entrySet()) {
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
      comments = DynamicConfigManager.getComments(file);
      yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(DynamicConfigManager.getContent(id,file)));
      this.data = new HashMap<>();
      convertMapsToSections(yaml,this);
      return this;
   }

   private void convertMapsToSections(ConfigurationSection input, IDynamicConfigurationSection section) {
      for(Map.Entry<?, ?> entry : input.getValues(true).entrySet()) {
         String key = entry.getKey().toString();
         Object value = entry.getValue();
         if (value instanceof ConfigurationSection) {
            IDynamicConfigurationSection sec = new DynamicYamlConfigurationSectionImpl(this,key,new HashMap<>());
            this.convertMapsToSections((ConfigurationSection) value, sec);
            section.getData().put(key, sec);
         } else {
            section.getData().put(key, value instanceof String ? ((String)value).replace("\\n","\n") : value);
         }
      }
   }



   private HashSet<String> keys(IDynamicConfigurationSection map, HashSet<String> keys) {
      for(String s : map.getData().keySet()) {
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
               start.set(lastKey,new DynamicYamlConfigurationSectionImpl(this,lastKey,new HashMap<>()));
            start = (IDynamicConfigurationSection) start.get(lastKey);
         }
      }
      return autoSave ? save() : this;
   }

   @Override
   public IDynamicConfigurationSection set(String path, Object value, String comment) {
      if(!comment.isEmpty()) {
         int ind = path.lastIndexOf('.');
         String newPath = (ind == -1 ? "" : path.substring(0,ind)+".") + id;
         if(!isSet(path))
            if(comment.contains("\n")) {
               for(String s : comment.split("\n"))
                  if(!isSet(newPath + "_COMMENT_" + comments)) {
                     set(newPath + "_COMMENT_" + comments, " " + s);
                     comments++;
                  }
            } else if(!isSet(newPath + "_COMMENT_" + comments)){
               set(newPath + "_COMMENT_" + comments, " " + comment);
               comments++;
            }
      }
      return set(path,value);
   }

   @Override
   public IDynamicConfigurationSection comment(String... comment) {
      for(String comm : comment)
         if(comm.contains("\n")) {
            for(String s : comm.split("\n"))
               if(!isSet(id + "_COMMENT_" + (comments))){
                  set(id + "_COMMENT_" + (comments), " " + s);
                  comments++;
               }
         }
         else if(!isSet(id + "_COMMENT_" + (comments))){
            set(id + "_COMMENT_" + (comments), " " + comm);
            comments++;
         }
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
      if(sec == null) sec = new DynamicYamlConfigurationSectionImpl(this,path.contains(".") ? path.substring(path.lastIndexOf('.')) : path, new HashMap<>());
      return sec;
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
      if(value.getClass().getTypeParameters().length != 1 || !value.getClass().getTypeParameters()[0].getTypeName().contains("String"))return defaultValue;
      return (List<String>)value;
   }

   @Override
   public List<Double> getListDouble(String path) {
      return getListDouble(path,new ArrayList<>());
   }

   @Override
   public List<Double> getListDouble(String path, List<Double> defaultValue) {
      List<?> value = getList(path, null);
      if(value.getClass().getTypeParameters().length != 1 || !value.getClass().getTypeParameters()[0].getTypeName().contains("Double"))return defaultValue;
      return (List<Double>)value;
   }

   @Override
   public List<Integer> getListInteger(String path) {
      return getListInteger(path,new ArrayList<>());
   }

   @Override
   public List<Integer> getListInteger(String path, List<Integer> defaultValue) {
      List<?> value = getList(path, null);
      if(value.getClass().getTypeParameters().length != 1 || !value.getClass().getTypeParameters()[0].getTypeName().contains("Integer"))return defaultValue;
      return (List<Integer>)value;
   }

   @Override
   public List<Float> getListFloat(String path) {
      return getListFloat(path,new ArrayList<>());
   }

   @Override
   public List<Float> getListFloat(String path, List<Float> defaultValue) {
      List<?> value = getList(path, null);
      if(value.getClass().getTypeParameters().length != 1 || !value.getClass().getTypeParameters()[0].getTypeName().contains("Float"))return defaultValue;
      return (List<Float>)value;
   }

   @Override
   public List<Byte> getListByte(String path) {
      return getListByte(path,new ArrayList<>());
   }

   @Override
   public List<Byte> getListByte(String path, List<Byte> defaultValue) {
      List<?> value = getList(path, null);
      if(value.getClass().getTypeParameters().length != 1 || !value.getClass().getTypeParameters()[0].getTypeName().contains("Byte"))return defaultValue;
      return (List<Byte>)value;
   }

   @Override
   public List<Boolean> getListBoolean(String path) {
      return getListBoolean(path,new ArrayList<>());
   }

   @Override
   public List<Boolean> getListBoolean(String path, List<Boolean> defaultValue) {
      List<?> value = getList(path, null);
      if(value.getClass().getTypeParameters().length != 1 || !value.getClass().getTypeParameters()[0].getTypeName().contains("Boolean"))return defaultValue;
      return (List<Boolean>)value;
   }

   @Override
   public String toString() {
      return asString();
   }
}