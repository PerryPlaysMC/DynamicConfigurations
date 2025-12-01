package io.dynamicstudios.configurations.yaml;

import com.google.common.collect.ImmutableMap;
import io.dynamicstudios.configurations.*;
import io.dynamicstudios.configurations.utils.DynamicConfigurationDirectory;
import io.dynamicstudios.configurations.utils.DynamicConfigurationOptions;
import io.dynamicstudios.configurations.utils.FileUtils;
import io.dynamicstudios.configurations.yaml.bukkit.YamlConfigurationUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/

public class DynamicYamlConfiguration extends DefaultDynamicConfigurationSectionImpl implements IDynamicConfiguration {

 protected List<Runnable> reloadListeners = new ArrayList<>();
 protected ScheduledExecutorService autoSaveService = Executors.newSingleThreadScheduledExecutor();
 protected ScheduledFuture autoSaveTask = null;
 protected final HashMap<String, String> COMMENTS = new HashMap<>();
 protected final HashMap<String, String> INLINE_COMMENTS = new HashMap<>();
 private File file;
 private final JavaPlugin plugin;
 private final DynamicConfigurationOptions<DynamicYamlConfiguration> options;
 private Map<String, Object> data = new LinkedHashMap<>();
 private File directory;
 private DynamicConfigurationManager.InputStreamSupplier stream;
 private final boolean isGhost;
 private YamlConfigurationUtil yaml;
 private String lastPath = "";
 private DynamicConfigurationDirectory configurationDirectory;

 public DynamicYamlConfiguration(JavaPlugin plugin, boolean isGhost, File directory, String resourceName, String name) {
	this.plugin = plugin;
	String split = name.contains("/") ? "/" : File.pathSeparator;
	String substring = name.contains(split) ? name.substring(0, name.lastIndexOf(split)) : name;
	if(directory == null) {
	 if(name.contains(split)) directory = new File(substring);
	 else directory = new File(plugin != null ? plugin.getDataFolder() + "" : "");
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

 public DynamicYamlConfiguration(JavaPlugin plugin, File directory, String name) {
	this(plugin, false, directory, name, name);
 }

 public DynamicYamlConfiguration(JavaPlugin plugin, boolean isGhost, File directory, String name) {
	this(plugin, isGhost, directory, name, name);
 }

 public DynamicYamlConfiguration(JavaPlugin plugin, boolean isGhost, String directory, String name) {
	this(plugin, isGhost, directory == null || directory.isEmpty() ? null : new File(directory), name, name);
 }


 public DynamicYamlConfiguration(JavaPlugin plugin, String directory, String name) {
	this(plugin, false, directory, name, name);
 }

 public DynamicYamlConfiguration(boolean isGhost, String name) {
	this(null, isGhost, (File) null, name, name);
 }

 public DynamicYamlConfiguration(JavaPlugin plugin, boolean isGhost, DynamicConfigurationDirectory directory, String name) {
	this(plugin, isGhost, directory == null ? null : directory.directory(), name, name);
	if(directory != null) configurationDirectory(directory);
 }

 public DynamicYamlConfiguration(JavaPlugin plugin, DynamicConfigurationDirectory directory, String name) {
	this(plugin, false, directory == null ? null : directory.directory(), name, name);
	if(directory != null) configurationDirectory(directory);
 }

 public DynamicYamlConfiguration(JavaPlugin plugin, DynamicConfigurationManager.InputStreamSupplier inputStream, String name) {
	this(plugin, true, "", name);
	this.stream = inputStream;
	reload();
 }

 public DynamicYamlConfiguration(JavaPlugin plugin, File directory, String resourceName, String name) {
	this(plugin, false, directory, resourceName, name);
 }

 public DynamicYamlConfiguration(JavaPlugin plugin, boolean isGhost, String directory, String resourceName, String name) {
	this(plugin, isGhost, directory == null || directory.isEmpty() ? null : new File(directory), resourceName, name);
 }


 public DynamicYamlConfiguration(JavaPlugin plugin, String directory, String resourceName, String name) {
	this(plugin, false, directory, resourceName, name);
 }

 public DynamicYamlConfiguration(boolean isGhost, String resourceName, String name) {
	this(null, isGhost, (File) null, resourceName, name);
 }

 public DynamicYamlConfiguration(JavaPlugin plugin, boolean isGhost, DynamicConfigurationDirectory directory, String resourceName, String name) {
	this(plugin, isGhost, directory == null ? null : directory.directory(), resourceName, name);
	if(directory != null) configurationDirectory(directory);
 }

 public DynamicYamlConfiguration(JavaPlugin plugin, DynamicConfigurationDirectory directory, String resourceName, String name) {
	this(plugin, false, directory == null ? null : directory.directory(), resourceName, name);
	if(directory != null) configurationDirectory(directory);
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
 public DynamicConfigurationOptions<DynamicYamlConfiguration> options() {
	return options;
 }

 @Override
 public DynamicConfigurationDirectory configurationDirectory() {
	return configurationDirectory;
 }

 @Override
 public IDynamicConfiguration configurationDirectory(DynamicConfigurationDirectory directory) {
	if(directory != null)
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
 public IDynamicConfiguration rename(String name) {
	File file = this.file;
	if(file == null) {
	 throw new IllegalStateException("File cannot be null");
	}
	if(name == null || name.trim().isEmpty()) {
	 throw new IllegalArgumentException("Name cannot be null or empty");
	}

	// Create the new file path with the updated name
	String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
	File newFile = new File(file.getParent(), name + (name.endsWith("." + extension) ? "" : "." + extension));

	// Rename the file
	try {
	 Path source = file.toPath();
	 Path destination = newFile.toPath();
	 Files.move(source, destination);
	 this.file = newFile; // Update the internal file reference
	} catch(IOException e) {
	 throw new RuntimeException("Failed to rename file to " + name, e);
	}

	return this;
 }

 @Override
 public boolean supportsComments() {
	return true;
 }

 @Override
 public boolean isGhost() {
	return isGhost;
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
 public IDynamicConfiguration onReload(Runnable... onReload) {
	reloadListeners = new ArrayList<>(Arrays.asList(onReload));
	return this;
 }

 @Override
 public IDynamicConfiguration addReloadListener(Runnable onReload) {
	reloadListeners.add(onReload);
	return this;
 }

 @Override
 public IDynamicConfiguration clearReloadListeners() {
	reloadListeners.clear();
	return this;
 }

 @Override
 public IDynamicConfiguration regenerate() {
	if(isGhost) return reload();

	if(this.file.exists()) this.file.delete();
	InputStream rsc = plugin == null ? null : plugin.getResource(name());
	String dir = (directory != null ? (directory.getPath().endsWith("/") ?
		 directory.getPath() : directory.getPath() + "/") : "").replace(plugin == null ? "" : plugin.getDataFolder().getPath(), "");
	if(rsc == null && plugin != null) rsc = plugin.getResource((dir.equals("/") ? "" : dir) + name());
	if(directory != null && !directory.isDirectory()) directory.mkdirs();
	try {
	 if(rsc == null) file.createNewFile();
	 else FileUtils.writeFile(rsc, file);
	} catch(IOException e) {
	 e.printStackTrace();
	}
	reload();
	return this;
 }

 @Override
 public String saveToString() {
	return yaml.saveToString();
 }

 @Override
 public IDynamicConfiguration save() {
	try {
	 if(isGhost) return this;
	 if(stream != null && stream.get() != null)
		if(options().appendMissingKeys() && DynamicConfigurationManager.isMissingKeys(this, stream))
		 DynamicConfigurationManager.appendMissingKeysFromTo(stream, this);
	 yaml.options().indent(options.indent());
	 try {
		yaml.loadFromString("");
	 } catch(InvalidConfigurationException ignored) {
	 }
	 toBukkit(this, yaml);

	 BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	 writer.write(FileUtils.generateNewConfigString(this, options(), COMMENTS, INLINE_COMMENTS));
	 writer.flush();
	 writer.close();
	} catch(IOException e) {
	 e.printStackTrace();
	}
	return this;
 }

 @Override
 public IDynamicConfiguration reload() {
	if(isGhost) {
	 updateComments();
	 if(stream == null || stream.get() == null) yaml = new YamlConfigurationUtil();
	 else yaml = YamlConfigurationUtil.loadConfiguration(new InputStreamReader(stream.get()));
	 Map<String, Object> data = new LinkedHashMap<>();
	 if(stream != null && stream.get() != null)
		fromBukkit(FileUtils.findKeys(stream.get()), yaml, this, data);
	 this.data = data;
	 reloadListeners.forEach(Runnable::run);
	 return this;
	}
	if(file != null) {
	 updateComments();
	 if(stream.get() == null) stream = () -> {
		try {
		 return new FileInputStream(file);
		} catch(FileNotFoundException e) {
		 Logger.getLogger("DynamicStudios").warning("Failed to get inputstream for file " + file.getPath());
		}
		return null;
	 };
	}
	try {
	 Supplier<InputStream> inputStream = () -> {
		try {
		 return file == null ? stream.get() : java.nio.file.Files.newInputStream(file.toPath());
		} catch(IOException e) {
		 throw new RuntimeException(e);
		}
	 };
	 try {
		if(!YamlConfigurationUtil.validate(inputStream.get())) {
		 yaml = YamlConfigurationUtil.fixFromString(inputStream.get(), COMMENTS, INLINE_COMMENTS);
		} else {
		 yaml = YamlConfigurationUtil.loadConfiguration(new InputStreamReader(inputStream.get()));
		}
	 } catch(Exception e) {
		e.printStackTrace();
	 }
	 Map<String, Object> data = new LinkedHashMap<>();
	 fromBukkit(FileUtils.findKeys(inputStream.get()), yaml, this, data);
	 this.data = data;
	 if(options.loadDefaults() && stream != null && stream.get() != null) {
		if(DynamicConfigurationManager.appendMissingKeysFromTo(DynamicConfigurationManager.createGhostConfiguration(plugin, UUID.randomUUID() + name(), stream), this))
		 save();
	 }
	} catch(Exception e) {
	 e.printStackTrace();
	}
	reloadListeners.forEach(Runnable::run);
	return this;
 }

 private void updateComments() {
	Map<String, String>[] totalComments = FileUtils.findAllComments(stream.get());
	COMMENTS.clear();
	COMMENTS.putAll(totalComments[0]);
	INLINE_COMMENTS.clear();
	INLINE_COMMENTS.putAll(totalComments[1]);
 }

 private void toBukkit(IDynamicConfigurationSection input, ConfigurationSection section) {
	for(Map.Entry<?, ?> entry : input.data().entrySet()) {
	 String key = entry.getKey() + "";
	 Object value = entry.getValue();
	 if(value instanceof IDynamicConfigurationSection) {
		ConfigurationSection sec = section.createSection(key);
		toBukkit((IDynamicConfigurationSection) value, sec);
		section.set(key, sec);
	 } else section.set(key, value instanceof String ? ((String) value).replace("\n", "\\n") : value);
	}
 }

 private void fromBukkit(List<String> arrangedKeys, ConfigurationSection input, IDynamicConfigurationSection section, Map<String, Object> data) {
	for(int i = 0; i < arrangedKeys.size(); i++) {
		String key = arrangedKeys.get(i);
		String strKey = arrangedKeys.get(i).replaceAll("['\"](.+)[\"']", "$1");
	 try {
		Object value = input.get(strKey);
		if(value instanceof ConfigurationSection) {
		 IDynamicConfigurationSection sec = new DynamicYamlConfigurationSection(this, section, key, new LinkedHashMap<>());
		 List<String> newKeys = arrangedKeys.stream().filter(s -> s.startsWith(key + ".") && !s.equals(key))
				.map(s -> s.substring(s.indexOf(".") + 1)).collect(Collectors.toList());
		 Map<String, Object> newData = new LinkedHashMap<>();
		 this.fromBukkit(newKeys, (ConfigurationSection) value, sec, newData);
		 sec.data().putAll(newData);
		 data.put(strKey, sec);
		 i += newKeys.size();
		} else {
		 data.put(strKey, value instanceof String ? ((String) value).replace("\\n", "\n") : value);
		}
	 } catch(Exception e) {

		Logger.getLogger("DynamicStudios").warning(name() + ": Error while reading from bukkit Key: '" + key + "' Localized: '" + strKey + "'");
		e.printStackTrace();
	 }
	}
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
	if(!deep) return data.keySet().stream().sorted().collect(Collectors.toList());
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
		lastPath = path;
		IDynamicConfigurationSerializer<Object> serializer = DynamicConfigurationManager.serializer(value.getClass());
		if(serializer instanceof IDynamicConfigurationStringSerializer)
		 data.put(paths[0], ((IDynamicConfigurationStringSerializer<Object>) serializer).serialize(value));
		else {
		 IDynamicConfigurationSection section = new DynamicYamlConfigurationSection(this, this, path, new HashMap<>());
		 data.put(path, section);
		 serializer.serialize(section, value);
		}
		return autoSave();
	 }
	 if(value == null) data.remove(paths[0]);
	 else data.put(paths[0], value instanceof Enum<?> ? ((Enum<?>) value).name() : value);
	 return autoSave();
	}
	for(int i = 0; i < paths.length; i++) {
	 String lastKey = paths[i];
	 if(i == paths.length - 1) {
		if(value instanceof DynamicYamlConfigurationSection && start != this)
		 ((DynamicYamlConfigurationSection) value).parent(start);
		start.set(lastKey, value);
	 } else {
		if(!start.isSet(lastKey) || !(start.get(lastKey) instanceof IDynamicConfigurationSection))
		 start.set(lastKey, new DynamicYamlConfigurationSection(this, start == this ? null : start, lastKey, new LinkedHashMap<>()));
		start = (IDynamicConfigurationSection) start.get(lastKey);
	 }
	}
	lastPath = path;
	return autoSave();
 }

 public IDynamicConfigurationSection autoSave() {
	if(options.autoSave()) {
	 if(autoSaveTask != null && !autoSaveTask.isDone() && !autoSaveTask.isCancelled())
		autoSaveTask.cancel(true);
	 autoSaveTask = autoSaveService.schedule(() -> {
		synchronized(DynamicYamlConfiguration.this) {
		 save();
		}
	 }, 250, TimeUnit.MILLISECONDS);
	}
	return this;
 }

 @Override
 public IDynamicConfigurationSection set(String path, Object value, String comment) {
	String cmt = Arrays.stream(comment.split("\n")).map(s -> s.startsWith("#") ? s : "#" + s).collect(Collectors.joining("\n"));
	if(!cmt.isEmpty())
	 COMMENTS.put(path, cmt);
	return set(path, value);
 }

 @Override
 public IDynamicConfigurationSection setInline(String path, Object value, String comment) {
	String cmt = Arrays.stream(comment.split("\n")).map(s -> s.startsWith("#") ? s : "#" + s).collect(Collectors.joining("\n"));
	if(!cmt.isEmpty()) INLINE_COMMENTS.put(path, cmt);
	return set(path, value);
 }

 @Override
 public IDynamicConfigurationSection comment(String... comment) {
	String cmt = Arrays.stream(comment).map(s -> s.startsWith("#") ? s : "#" + s).collect(Collectors.joining("\n"));
	if(!cmt.isEmpty())
	 if(get(lastPath) instanceof IDynamicConfigurationSection)
		INLINE_COMMENTS.put(lastPath, cmt);
	 else COMMENTS.put(lastPath, cmt);
	return this;
 }

 @Override
 public IDynamicConfigurationSection inlineComment(String... comment) {
	String cmt = Arrays.stream(comment).map(s -> s.startsWith("#") ? s : "#" + s).collect(Collectors.joining("\n"));
	if(!cmt.isEmpty())
	 INLINE_COMMENTS.put(lastPath, cmt);
	return this;
 }

 @Override
 public Object get(String path, Object defaultValue) {
	Map<String, Object> data = new LinkedHashMap<>(this.data);
	if(path.contains(".")) {
	 String[] split = path.split("\\.");
	 IDynamicConfigurationSection sec = this;
	 for(int i = 0; i < split.length; i++) {
		String key = split[i];
//		if(!data.containsKey(key)) return i == split.length - 1 ? sec : defaultValue;
		Object value = data.getOrDefault(key, defaultValue);
		if(value instanceof IDynamicConfigurationSection) {
		 data = ((IDynamicConfigurationSection) value).data();
		 sec = (IDynamicConfigurationSection) value;
		}
		if(i == split.length - 1 || value == null) return value;
	 }
	 return data.getOrDefault(split[0], defaultValue);
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
	} else {
	 IDynamicConfigurationSection section = getSection(path);
	 IDynamicConfigurationSection configuration = section == null ? this : section;
	 deserializedValue = serializer.deserialize(configuration);
	}
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
	 set(path, sec = new DynamicYamlConfigurationSection(this, path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path, new LinkedHashMap<>()));
	return sec;
 }

 @Override
 public IDynamicConfigurationSection createSection(String path, String comment) {
	if(!comment.isEmpty())
	 INLINE_COMMENTS.put(path, (comment.startsWith("#") ? "" : "#") + comment);
	return createSection(path);
 }

 @Override
 public String toString() {
	return asString();
 }
}