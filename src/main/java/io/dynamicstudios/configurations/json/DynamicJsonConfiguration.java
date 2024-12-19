package io.dynamicstudios.configurations.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import io.dynamicstudios.configurations.*;
import io.dynamicstudios.configurations.utils.DynamicConfigurationDirectory;
import io.dynamicstudios.configurations.utils.DynamicConfigurationOptions;
import io.dynamicstudios.configurations.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/

public class DynamicJsonConfiguration extends DefaultDynamicConfigurationSectionImpl implements IDynamicConfiguration {
  private final File file;
  private final JavaPlugin plugin;
  private final DynamicConfigurationOptions<DynamicJsonConfiguration> options;
  private final DynamicJsonAdapter adapter = new DynamicJsonAdapter(this);
  private final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(DynamicJsonConfigurationSection.class, adapter).create();
  private Map<String, Object> data = new LinkedHashMap<>();
  private Map<String, String> EMPTY_COMMENTS = new HashMap<>();
  private File directory;
  private boolean isGhost = false;
  private DynamicConfigurationManager.InputStreamSupplier stream;
  private DynamicConfigurationDirectory configurationDirectory;
	public DynamicJsonConfiguration(JavaPlugin plugin, boolean isGhost, File directory, String resourceName, String name) {
		this.plugin = plugin;
		String split = name.contains("/") ? "/" : File.pathSeparator;
		String substring = name.contains(split) ? name.substring(0, name.lastIndexOf(split)) : name;
		if(directory == null) {
			if(name.contains(split)) directory = new File(substring);
			else directory = new File(plugin!=null? plugin.getDataFolder()+"":"");
		}
		if(name.contains("/")) {
			if(!directory.getPath().endsWith(substring))
				directory = new File(directory, substring);
			name = name.substring(name.lastIndexOf(split) + 1);
		}
		this.isGhost = isGhost;
		this.directory = directory;
		this.file = new File(directory, name + (name.endsWith(".yml") ? "" : ".yml"));
		this.stream = () -> FileUtils.findStream(plugin, new File(resourceName));
		this.options = new DynamicConfigurationOptions<>(this);
		this.configurationDirectory = DynamicConfigurationManager.getConfigurationDirectory(directory);
		if(!file.exists()) regenerate();
		reload();
		DynamicConfigurationManager.addConfiguration(this);
	}

	public DynamicJsonConfiguration(JavaPlugin plugin, File directory, String name) {
		this(plugin, false, directory, name, name);
	}

	public DynamicJsonConfiguration(JavaPlugin plugin, boolean isGhost, File directory, String name) {
		this(plugin, isGhost, directory, name, name);
	}

	public DynamicJsonConfiguration(JavaPlugin plugin, boolean isGhost, String directory, String name) {
		this(plugin, isGhost, directory == null || directory.isEmpty() ? null : new File(directory), name, name);
	}


	public DynamicJsonConfiguration(JavaPlugin plugin, String directory, String name) {
		this(plugin, false, directory, name, name);
	}

	public DynamicJsonConfiguration(boolean isGhost, String name) {
		this(null, isGhost, (File)null, name, name);
	}

	public DynamicJsonConfiguration(JavaPlugin plugin, boolean isGhost, DynamicConfigurationDirectory directory, String name) {
		this(plugin, isGhost, directory == null ? null : directory.directory(), name, name);
		if(directory != null) configurationDirectory(directory);
	}
	public DynamicJsonConfiguration(JavaPlugin plugin, DynamicConfigurationDirectory directory, String name) {
		this(plugin, false, directory == null ? null : directory.directory(), name, name);
		if(directory != null) configurationDirectory(directory);
	}

	public DynamicJsonConfiguration(JavaPlugin plugin, DynamicConfigurationManager.InputStreamSupplier inputStream, String name) {
		this(plugin, true, "", name);
		this.stream = inputStream;
		reload();
	}
	public DynamicJsonConfiguration(JavaPlugin plugin, File directory, String resourceName, String name) {
		this(plugin, false, directory, resourceName, name);
	}

	public DynamicJsonConfiguration(JavaPlugin plugin, boolean isGhost, String directory, String resourceName, String name) {
		this(plugin, isGhost, directory == null || directory.isEmpty() ? null : new File(directory), resourceName, name);
	}


	public DynamicJsonConfiguration(JavaPlugin plugin, String directory, String resourceName, String name) {
		this(plugin, false, directory, resourceName, name);
	}

	public DynamicJsonConfiguration(boolean isGhost, String resourceName, String name) {
		this(null, isGhost, (File)null, resourceName, name);
	}

	public DynamicJsonConfiguration(JavaPlugin plugin, boolean isGhost, DynamicConfigurationDirectory directory, String resourceName, String name) {
		this(plugin, isGhost, directory == null ? null : directory.directory(), resourceName, name);
		if(directory != null) configurationDirectory(directory);
	}
	public DynamicJsonConfiguration(JavaPlugin plugin, DynamicConfigurationDirectory directory, String resourceName, String name) {
		this(plugin, false, directory == null ? null : directory.directory(), resourceName, name);
		if(directory != null) configurationDirectory(directory);
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
  public DynamicConfigurationOptions<DynamicJsonConfiguration> options() {
    return options;
  }

  @Override
  public String name() {
    return file.getName();
  }

  @Override
  public boolean supportsComments() {
    return false;
  }

  @Override
  public boolean isGhost() {
    return isGhost;
  }

  @Override
  public Map<String, String> comments() {
    if(DynamicConfigurationManager.DEBUG_ENABLED)
     Logger.getLogger("DynamicStudios").log(Level.WARNING,
        "Comments are not supported in json files("+file()+"): " + DynamicConfigurationManager.getStackTrace());
    return EMPTY_COMMENTS;
  }

  @Override
  public Map<String, String> inlineComments() {
    if(DynamicConfigurationManager.DEBUG_ENABLED)
     Logger.getLogger("DynamicStudios").log(Level.WARNING,
        "Comments are not supported in json files("+file()+"): " + DynamicConfigurationManager.getStackTrace());
    return EMPTY_COMMENTS;
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
        DynamicConfigurationManager.appendMissingKeysFromTo(stream, this);
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
        if(options.loadDefaults() && stream != null && stream.get() != null)
          options.defaults().data().putAll(DynamicConfigurationManager.createGhostConfiguration(plugin(), UUID.randomUUID()+name(), stream).data());
        data = (Map<String, Object>) GSON.fromJson(new FileReader(file), Map.class);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    if(data == null) data = new LinkedHashMap<>();
    return this;
  }

  private List<String> keys(IDynamicConfigurationSection map, List<String> keys) {
    for(String s : map.data().keySet()) {
      keys.add(s);
      if(map.get(s) instanceof IDynamicConfigurationSection) keys.addAll(((IDynamicConfigurationSection) map.get(s))
        .getKeys(true).stream().map(str -> s + "." + str).collect(Collectors.toList()));
    }
    return keys;
  }

  @Override
  public List<String> getKeys(boolean deep) {
    if(!deep) return data.keySet().stream().sorted().collect(Collectors.toList());;
    return keys(this, new ArrayList<>());
  }

  @Override
  public boolean isSet(String path) {
    if(path.contains(".")) {
      String[] split = path.split("\\.");
      IDynamicConfigurationSection deep = this;
      String[] range = Arrays.copyOfRange(split, 0, split.length - 1);
      for(String s : range) {
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
          IDynamicConfigurationSection section = new DynamicJsonConfigurationSection(this, this, path, new HashMap<>());
          data.put(path, section);
          serializer.serialize(section, value);
        }
        return options.autoSave() ? save() : this;
      }
      if(value == null) data.remove(paths[0]);
      else data.put(paths[0], value);
      return options.autoSave() ? save() : this;
    }
    for(int i = 0; i < paths.length; i++) {
      String lastKey = paths[i];
      if(i == paths.length - 1) {
        if(value instanceof DynamicJsonConfigurationSection && start != this)
          ((DynamicJsonConfigurationSection) value).parent(start);
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
    if(DynamicConfigurationManager.DEBUG_ENABLED)
     Logger.getLogger("DynamicStudios").log(Level.WARNING,
        "Comments are not supported in json files: ("+file()+")" + DynamicConfigurationManager.getStackTrace() + " '" + path + "'");
    return set(path, value);
  }

  @Override
  public IDynamicConfigurationSection setInline(String path, Object value, String comment) {
    if(DynamicConfigurationManager.DEBUG_ENABLED)
     Logger.getLogger("DynamicStudios").log(Level.WARNING,
        "Comments are not supported in json files: ("+file()+")" + DynamicConfigurationManager.getStackTrace() + " '" + path + "'");
    return set(path, value);
  }

  @Override
  public IDynamicConfigurationSection comment(String... comment) {
    if(DynamicConfigurationManager.DEBUG_ENABLED)
     Logger.getLogger("DynamicStudios").log(Level.WARNING,
        "Comments are not supported in json files("+file()+"): " + DynamicConfigurationManager.getStackTrace());
    return this;
  }

  @Override
  public IDynamicConfigurationSection inlineComment(String... comment) {
    if(DynamicConfigurationManager.DEBUG_ENABLED)
     Logger.getLogger("DynamicStudios").log(Level.WARNING,
        "Comments are not supported in json files("+file()+"): " + DynamicConfigurationManager.getStackTrace());
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
	  T deserializedValue = null;
	  if(serializer instanceof IDynamicConfigurationStringSerializer) {
		  if(getString(path) != null)
			  deserializedValue = ((IDynamicConfigurationStringSerializer<T>) serializer).deserialize(getString(path));
	  } else deserializedValue = serializer.deserialize(getSection(path) == null ? this : getSection(path));
	  return deserializedValue == null ? defaultValue : deserializedValue;
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
    if(DynamicConfigurationManager.DEBUG_ENABLED)
     Logger.getLogger("DynamicStudios").log(Level.WARNING,
        "Comments are not supported in json files: ("+file()+")" + DynamicConfigurationManager.getStackTrace() + " '" + path + "'");
    return createSection(path);
  }

  @Override
  public String toString() {
    return asString();
  }
}
