package io.dynamicstudios.configurations;

import io.dynamicstudios.configurations.json.DynamicJsonConfiguration;
import io.dynamicstudios.configurations.utils.DynamicConfigurationDirectory;
import io.dynamicstudios.configurations.utils.FileUtils;
import io.dynamicstudios.configurations.yaml.DynamicYamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/

public class DynamicConfigurationManager {

  private static final HashMap<Class<?>, IDynamicConfigurationSerializer<?>> CONFIGURATION_SERIALIZER = new HashMap<>();
  private static final Set<IDynamicConfiguration> CONFIGURATIONS = new HashSet<>();
  private static final Set<DynamicConfigurationDirectory> CONFIGURATION_DIRECTORIES = new HashSet<>();
  private static final Map<String, ConfigCreate> CONFIG_EXTENSION_REGISTER = new HashMap<>();
  public static boolean DEBUG_ENABLED = false;

  static {
    registerExtension(".yml", (plugin, directory, resourceName, name) -> {
      if(directory instanceof File) return new DynamicYamlConfiguration(plugin, false, (File) directory, resourceName, name);
      if(directory instanceof DynamicConfigurationDirectory)
        return new DynamicYamlConfiguration(plugin, (DynamicConfigurationDirectory) directory, resourceName, name);
      if(directory instanceof InputStreamSupplier)
        return new DynamicYamlConfiguration(plugin, (InputStreamSupplier) directory, name);
      return new DynamicYamlConfiguration(plugin, "", resourceName, name);
    });
    registerExtension(".json", (plugin, directory, resourceName, name) -> {
      if(directory instanceof File) return new DynamicJsonConfiguration(plugin, (File) directory, resourceName, name);
      if(directory instanceof DynamicConfigurationDirectory)
        return new DynamicJsonConfiguration(plugin, (DynamicConfigurationDirectory) directory, resourceName, name);
      if(directory instanceof InputStreamSupplier)
        return new DynamicJsonConfiguration(plugin, (InputStreamSupplier) directory, name);
      return new DynamicJsonConfiguration(plugin, "", resourceName, name);
    });
  }

  public static String getStackTrace() {
    StackTraceElement l = Thread.currentThread().getStackTrace()[2];
    return(l.getClassName()+"#"+l.getMethodName()+"():"+l.getLineNumber());
  }

  public static boolean hasSerializer(Class<?> deserializeType) {
    return CONFIGURATION_SERIALIZER.containsKey(deserializeType);
  }

  public static <T> IDynamicConfigurationSerializer<T> serializer(Class<?> deserializeType) {
    return (IDynamicConfigurationSerializer<T>) CONFIGURATION_SERIALIZER.get(deserializeType);
  }

  public static void registerExtension(String extension, ConfigCreate clazz) {
    if(DEBUG_ENABLED) Logger.getLogger("DynamicStudios").log(Level.INFO, "Registering file extension: '" + extension + "'");
    CONFIG_EXTENSION_REGISTER.put((extension.startsWith(".") ? "" : ".") + extension, clazz);
    CONFIG_EXTENSION_REGISTER.put(extension.substring(extension.startsWith(".") ? 1 : 0), clazz);
  }

  public static void registerSerializer(IDynamicConfigurationSerializer<?> serializer) {
    CONFIGURATION_SERIALIZER.put(serializer.type(), serializer);
  }

  public static void addConfiguration(IDynamicConfiguration configuration) {
    CONFIGURATIONS.add(configuration);
  }

  public static void removeConfiguration(IDynamicConfiguration configuration) {
    CONFIGURATIONS.remove(configuration);
  }

  public static IDynamicConfiguration getConfiguration(String name) {
    for(IDynamicConfiguration configuration : getConfigurations())
      if(configuration.name().equals(name) || configuration.file().getName().equals(name)
        || (configuration.directory().getName() + "/" + configuration.name()).equals(name)
        || (configuration.directory().getName() + "/" + configuration.file().getName()).equals(name)) return configuration;
    return null;
  }

  public static void addConfigurationDirectory(DynamicConfigurationDirectory configuration) {
    CONFIGURATION_DIRECTORIES.add(configuration);
  }

  public static void removeConfigurationDirectory(DynamicConfigurationDirectory configuration) {
    CONFIGURATION_DIRECTORIES.remove(configuration);
  }

  public static DynamicConfigurationDirectory getConfigurationDirectory(String name) {
    for(DynamicConfigurationDirectory configuration : getConfigurationDirectories())
      if(configuration.name().equals(name) || configuration.directory().getPath().equals(name)) return configuration;
    return null;
  }

  public static DynamicConfigurationDirectory getConfigurationDirectory(File path) {
    for(DynamicConfigurationDirectory configuration : getConfigurationDirectories())
      if(configuration.directory().getPath().equals(path.getPath())) return configuration;
    return null;
  }

	public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, String name) {
		return createConfiguration(plugin, name.contains("/") ? null : plugin.getDataFolder(), name);
	}

	public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, File directory, String name) {
		return createConfiguration(plugin, directory, name,name);
	}


	public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, File directory, String resourceName, String name) {
		IDynamicConfiguration config = getConfiguration(name);
		if(config == null) config = getConfiguration((directory != null ? directory.getPath() + "/" : "") + name);
		if(config == null) {
			if(name.lastIndexOf('.') != -1) {
				String extension = name.substring(name.lastIndexOf('.'));
				if(CONFIG_EXTENSION_REGISTER.containsKey(extension))
					return CONFIG_EXTENSION_REGISTER.get(extension).create(plugin, directory, resourceName, name);
			}
			return new DynamicYamlConfiguration(plugin, directory, name);
		}
		return config;
	}

	public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, String directory, String name) {
		return createConfiguration(plugin, directory, name, name);
	}

	public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, String directory, String resourceName, String name) {
		return createConfiguration(plugin, directory == null ? null : new File(directory), resourceName, name);
	}

	public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, DynamicConfigurationDirectory directory, String name) {
		return createConfiguration(plugin, directory, name, name);
	}
	public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, DynamicConfigurationDirectory directory, String resourceName, String name) {
		IDynamicConfiguration config = getConfiguration(name);
		if(config == null) config = getConfiguration(name);
		if(config == null) {
			String extension = name.substring(name.lastIndexOf('.'));
			if(CONFIG_EXTENSION_REGISTER.containsKey(extension))
				return CONFIG_EXTENSION_REGISTER.get(extension).create(plugin, directory, resourceName, name);
			else return new DynamicYamlConfiguration(plugin, directory, resourceName, name);
		}
		return config;
	}

	public static IDynamicConfiguration createGhostConfiguration(JavaPlugin plugin, String fileName, String configName) {
		return createGhostConfiguration(plugin, fileName, fileName,configName);
	}
	public static IDynamicConfiguration createGhostConfiguration(JavaPlugin plugin, String resourceName, String fileName, String configName) {
		String extension = fileName.substring(fileName.lastIndexOf('.'));
		if(CONFIG_EXTENSION_REGISTER.containsKey(extension))
			return CONFIG_EXTENSION_REGISTER.get(extension).create(plugin, (InputStreamSupplier) () -> FileUtils.findStream(plugin, new File(resourceName)), resourceName, configName);
		else return new DynamicYamlConfiguration(plugin, () -> FileUtils.findStream(plugin, new File(resourceName)), configName);
	}

	public static IDynamicConfiguration createGhostConfiguration(JavaPlugin plugin, String name, InputStreamSupplier inputStream) {
		String extension = name.lastIndexOf('.') == -1 ? name : name.substring(name.lastIndexOf('.'));
		if(CONFIG_EXTENSION_REGISTER.containsKey(extension))
			return CONFIG_EXTENSION_REGISTER.get(extension).create(plugin, inputStream, null, name);
		else return new DynamicYamlConfiguration(plugin, inputStream, name);
	}

  public static boolean isMissingKeys(IDynamicConfiguration configuration, IDynamicConfiguration ghostConfiguration) {
    List<String> list1 = ghostConfiguration.getKeys(true);
    List<String> list2 = configuration.getKeys(true);
    list1.removeAll(list2);
    return !list1.isEmpty();
  }

  public static boolean isMissingKeys(IDynamicConfiguration configuration, InputStreamSupplier ghostConfigurationStream) {
    IDynamicConfiguration ghostConfiguration = createGhostConfiguration(configuration.plugin(), configuration.name(), ghostConfigurationStream);
    return isMissingKeys(configuration, ghostConfiguration);
  }

  public static boolean appendMissingKeysFromTo(IDynamicConfiguration ghostConfiguration, IDynamicConfiguration configuration) {
    List<String> list1 = ghostConfiguration.getKeys(true);
    List<String> list2 = configuration.getKeys(true);
    list1.removeAll(list2);
    if(!list1.isEmpty()) {
	    if(DEBUG_ENABLED) Logger.getLogger("DynamicStudios").log(Level.INFO, "Found missing keys"+list1+" in file '" + configuration.file() + "'");
      boolean autoSave = configuration.options().autoSave();
      configuration.options().autoSave(false);
      for(String s : list1) {
        if(!configuration.supportsComments()) {
          configuration.set(s,ghostConfiguration.get(s));
          continue;
        }
        if(ghostConfiguration.comments().containsKey(s))
          configuration.set(s, ghostConfiguration.get(s), ghostConfiguration.comments().get(s));
        else configuration.setInline(s, ghostConfiguration.get(s), ghostConfiguration.inlineComments().getOrDefault(s, ""));
      }
      configuration.options().autoSave(autoSave);
      return true;
    }
    return false;
  }

  public static boolean appendMissingKeysFromTo(InputStreamSupplier ghostConfigurationStream, IDynamicConfiguration configuration) {
    IDynamicConfiguration ghostConfiguration = createGhostConfiguration(configuration.plugin(), configuration.name(), ghostConfigurationStream);
    return appendMissingKeysFromTo(ghostConfiguration, configuration);
  }

  public static Set<DynamicConfigurationDirectory> getConfigurationDirectories() {
    return CONFIGURATION_DIRECTORIES;
  }

  public static Set<IDynamicConfiguration> getConfigurations() {
    return CONFIGURATIONS;
  }

	public static boolean hasRegister(String extension) {
		return CONFIG_EXTENSION_REGISTER.containsKey(extension);
	}

	public interface ConfigCreate {
		IDynamicConfiguration create(JavaPlugin plugin, Object directory, String resourceName, String name);
	}

	public interface InputStreamSupplier {
    InputStream get();
  }
}
