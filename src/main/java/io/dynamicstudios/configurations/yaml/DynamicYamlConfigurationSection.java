package io.dynamicstudios.configurations.yaml;

import io.dynamicstudios.configurations.*;
import io.dynamicstudios.configurations.utils.DynamicConfigurationOptions;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/

public class DynamicYamlConfigurationSection extends DefaultDynamicConfigurationSectionImpl {

 private final DynamicYamlConfiguration configuration;
 private final String id;
 private final Map<String, Object> data;
 private final String fullPath;
 private IDynamicConfigurationSection parent;
 private String lastPath = "";

 protected DynamicYamlConfigurationSection(DynamicYamlConfiguration configuration, String id, Map<String, Object> data) {
	this(configuration, null, id, data);
 }

 protected DynamicYamlConfigurationSection(DynamicYamlConfiguration configuration, IDynamicConfigurationSection parent, String id, Map<String, Object> data) {
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

 @Override
 public Map<String, Object> data() {
	return data;
 }

 @Override
 public IDynamicConfigurationSection save() {
	configuration.save();
	return this;
 }


 public IDynamicConfigurationSection autoSave() {
	configuration.autoSave();
	return this;
 }

 @Override
 public IDynamicConfigurationSection reload() {
	configuration.reload();
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
	if(DynamicConfigurationManager.DEEP_DEBUG_ENABLED)
	 Logger.getLogger("DynamicStudios").log(Level.INFO, "ConfigSection Saving: '" + (value == null ? "null" : value.getClass().getName()) + "' to: " + path + " has serializer: " + (value != null && DynamicConfigurationManager.hasSerializer(value.getClass())) + ", paths: " + paths.length);
	if(paths.length == 1) {
	 if(value != null && DynamicConfigurationManager.hasSerializer(value.getClass())) {
		lastPath = path;
		IDynamicConfigurationSerializer<Object> serializer = DynamicConfigurationManager.serializer(value.getClass());
		if(serializer instanceof IDynamicConfigurationStringSerializer)
		 data.put(paths[0], ((IDynamicConfigurationStringSerializer<Object>) serializer).serialize(value));
		else {
		 IDynamicConfigurationSection section = new DynamicYamlConfigurationSection(configuration, this, path, new HashMap<>());
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
		 start.set(lastKey, new DynamicYamlConfigurationSection(configuration, start == this ? null : start, lastKey, new LinkedHashMap<>()));

		start = (IDynamicConfigurationSection) start.get(lastKey);
	 }
	}
	lastPath = path;
	return autoSave();
 }

 @Override
 public IDynamicConfigurationSection set(String path, Object value, String comment) {
	if(!comment.isEmpty())
	 configuration.COMMENTS.put(fullPath() + "." + path, "#" + comment);
	return set(path, value);
 }

 @Override
 public IDynamicConfigurationSection setInline(String path, Object value, String comment) {
	if(!comment.isEmpty())
	 configuration.INLINE_COMMENTS.put(fullPath() + "." + path, "#" + comment);
	return set(path, value);
 }

 @Override
 public IDynamicConfigurationSection comment(String... comment) {
	if(!String.join("\n#", comment).isEmpty())
	 configuration.COMMENTS.put(fullPath() + "." + lastPath, "#" + String.join("\n#", comment));
	return this;
 }

 @Override
 public IDynamicConfigurationSection inlineComment(String... comment) {
	if(!String.join("\n#", comment).isEmpty())
	 configuration.INLINE_COMMENTS.put(fullPath() + "." + lastPath, "#" + String.join("\n#", comment));
	return this;
 }

 @Override
 public Object get(String path, Object defaultValue) {
	Map<String, Object> data = new LinkedHashMap<>(this.data);
	if(path.contains(".")) {
	 String[] split = path.split("\\.");
	 for(int i = 0; i < split.length; i++) {
		String key = split[i];
		if(!data.containsKey(key)) return i == split.length - 1 ? data : defaultValue;
		Object value = data.getOrDefault(key, defaultValue);
		if(value instanceof IDynamicConfigurationSection) data = new LinkedHashMap<>(((IDynamicConfigurationSection) value).data());
		if(i == split.length - 1) return value;
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
	 set(path, sec = new DynamicYamlConfigurationSection(configuration, path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path, new LinkedHashMap<>()));
	return sec;
 }

 @Override
 public IDynamicConfigurationSection createSection(String path, String comment) {
	if(!comment.isEmpty())
	 configuration.INLINE_COMMENTS.put(fullPath() + "." + path, "#" + comment);
	return createSection(path);
 }

 @Override
 public String toString() {
	return asString();
 }

 

}
