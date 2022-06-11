package dev.perryplaysmc.dynamicconfigurations;

import dev.perryplaysmc.dynamicconfigurations.json.DynamicJsonConfiguration;
import dev.perryplaysmc.dynamicconfigurations.utils.DynamicConfigurationDirectory;
import dev.perryplaysmc.dynamicconfigurations.utils.FileUtils;
import dev.perryplaysmc.dynamicconfigurations.yaml.DynamicYamlConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.function.Supplier;


/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/

public class DynamicConfigurationManager {

  private static final HashMap<Class<?>, IDynamicConfigurationSerializer<?>> CONFIGURATION_SERIALIZER = new HashMap<>();
  private static final Set<IDynamicConfiguration> CONFIGURATIONS = new HashSet<>();
  private static final Set<DynamicConfigurationDirectory> CONFIGURATION_DIRECTORIES = new HashSet<>();
  private static final Map<String, ConfigCreate> CONFIG_EXTENSION_REGISTER = new HashMap<>();

  static {
    registerExtension(".yml", (plugin, directory, name) -> {
      if(directory instanceof File) return new DynamicYamlConfiguration(plugin, (File) directory, name);
      if(directory instanceof DynamicConfigurationDirectory)
        return new DynamicYamlConfiguration(plugin, (DynamicConfigurationDirectory) directory, name);
      if(directory instanceof Supplier)
        return new DynamicYamlConfiguration(plugin, (Supplier<InputStream>) directory, name);
      return new DynamicYamlConfiguration(plugin, "", name);
    });
    registerExtension(".json", (plugin, directory, name) -> {
      if(directory instanceof File) return new DynamicJsonConfiguration(plugin, (File) directory, name);
      if(directory instanceof DynamicConfigurationDirectory)
        return new DynamicJsonConfiguration(plugin, (DynamicConfigurationDirectory) directory, name);
      if(directory instanceof Supplier)
        return new DynamicJsonConfiguration(plugin, (Supplier<InputStream>) directory, name);
      return new DynamicJsonConfiguration(plugin, "", name);
    });
  }

  public static boolean hasSerializer(Class<?> deserializeType) {
    return CONFIGURATION_SERIALIZER.containsKey(deserializeType);
  }

  public static <T> IDynamicConfigurationSerializer<T> serializer(Class<?> deserializeType) {
    return (IDynamicConfigurationSerializer<T>) CONFIGURATION_SERIALIZER.get(deserializeType);
  }

  public static void registerExtension(String extension, ConfigCreate clazz) {
    CONFIG_EXTENSION_REGISTER.put((extension.startsWith(".") ? "" : ".") + extension, clazz);
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
    IDynamicConfiguration config = getConfiguration(name);
    if(config == null) config = getConfiguration((directory != null ? directory.getPath() + "/" : "") + name);
    if(config == null) {
      if(name.lastIndexOf('.') != -1) {
        String extension = name.substring(name.lastIndexOf('.'));
        if(CONFIG_EXTENSION_REGISTER.containsKey(extension))
          return CONFIG_EXTENSION_REGISTER.get(extension).create(plugin, directory, name);
      }
      return new DynamicYamlConfiguration(plugin, directory, name);
    }
    return config;
  }

  public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, String directory, String name) {
    return createConfiguration(plugin, directory == null ? null : new File(directory), name);
  }

  public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, DynamicConfigurationDirectory directory, String name) {
    IDynamicConfiguration config = getConfiguration(name);
    if(config == null) config = getConfiguration(name);
    if(config == null) {
      String extension = name.substring(name.lastIndexOf('.'));
      if(CONFIG_EXTENSION_REGISTER.containsKey(extension))
        return CONFIG_EXTENSION_REGISTER.get(extension).create(plugin, directory, name);
      else return new DynamicYamlConfiguration(plugin, directory, name);
    }
    return config;
  }

  public static IDynamicConfiguration createGhostConfiguration(JavaPlugin plugin, String name) {
    String extension = name.substring(name.lastIndexOf('.'));
    if(CONFIG_EXTENSION_REGISTER.containsKey(extension))
      return CONFIG_EXTENSION_REGISTER.get(extension).create(plugin, (Supplier) () -> FileUtils.findStream(plugin, new File(name)), name);
    else return new DynamicYamlConfiguration(plugin, () -> FileUtils.findStream(plugin, new File(name)), name);
  }

  public static IDynamicConfiguration createGhostConfiguration(JavaPlugin plugin, String name, Supplier<InputStream> inputStream) {
    String extension = name.substring(name.lastIndexOf('.'));
    if(CONFIG_EXTENSION_REGISTER.containsKey(extension))
      return CONFIG_EXTENSION_REGISTER.get(extension).create(plugin, inputStream, name);
    else return new DynamicYamlConfiguration(plugin, () -> FileUtils.findStream(plugin, new File(name)), name);
  }

  public static boolean isMissingKeys(IDynamicConfiguration configuration, IDynamicConfiguration ghostConfiguration) {
    Set<String> list1 = ghostConfiguration.getKeys(true);
    Set<String> list2 = configuration.getKeys(true);
    list1.removeAll(list2);
    return !list1.isEmpty();
  }

  public static boolean isMissingKeys(IDynamicConfiguration configuration, Supplier<InputStream> ghostConfigurationStream) {
    IDynamicConfiguration ghostConfiguration = createGhostConfiguration(configuration.plugin(), configuration.name(), ghostConfigurationStream);
    return isMissingKeys(configuration, ghostConfiguration);
  }

  public static boolean appendMissingKeysFrom(IDynamicConfiguration ghostConfiguration, IDynamicConfiguration configuration) {
    Set<String> list1 = ghostConfiguration.getKeys(true);
    Set<String> list2 = configuration.getKeys(true);
    list1.removeAll(list2);
    if(!list1.isEmpty()) {
      boolean autoSave = configuration.options().autoSave();
      configuration.options().autoSave(false);
      for(String s : list1)
        if(ghostConfiguration.comments().containsKey(s))
          configuration.set(s, ghostConfiguration.get(s), ghostConfiguration.comments().get(s));
        else configuration.setInline(s, ghostConfiguration.get(s), ghostConfiguration.inlineComments().getOrDefault(s, ""));
      configuration.options().autoSave(autoSave);
      return true;
    }
    return false;
  }

  public static boolean appendMissingKeysFrom(Supplier<InputStream> ghostConfigurationStream, IDynamicConfiguration configuration) {
    IDynamicConfiguration ghostConfiguration = createGhostConfiguration(configuration.plugin(), configuration.name(), ghostConfigurationStream);
    return appendMissingKeysFrom(ghostConfiguration, configuration);
  }

  public static Set<DynamicConfigurationDirectory> getConfigurationDirectories() {
    return CONFIGURATION_DIRECTORIES;
  }

  public static Set<IDynamicConfiguration> getConfigurations() {
    return CONFIGURATIONS;
  }

  public interface ConfigCreate {
    IDynamicConfiguration create(JavaPlugin plugin, Object directory, String name);
  }
}
