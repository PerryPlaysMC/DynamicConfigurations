package io.dynamicstudios.configurations.utils;

import io.dynamicstudios.configurations.DynamicConfigurationManager;
import io.dynamicstudios.configurations.IDynamicConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Creator: PerryPlaysMC
 * Created: 03/2022
 **/
public class DynamicConfigurationDirectory {

  private final JavaPlugin plugin;
  private final File directory;
  private final List<DynamicConfigurationDirectory> subDirectories;
  private final List<IDynamicConfiguration> configurations;
  private final DynamicConfigurationDirectory parent;
  private boolean allowsSubDirectories = true;

  public DynamicConfigurationDirectory(JavaPlugin plugin, File directory) {
    this(plugin, directory, null);
  }

  public DynamicConfigurationDirectory(JavaPlugin plugin, File directory, DynamicConfigurationDirectory parent) {
    this.plugin = plugin;
    this.directory = directory;
    this.parent = parent;
    this.subDirectories = new ArrayList<>();
    this.configurations = new ArrayList<>();
    reload();
    DynamicConfigurationManager.addConfigurationDirectory(this);
  }

  public DynamicConfigurationDirectory reload() {
    for(IDynamicConfiguration configuration : configurations) configuration.reload();
    configurations.clear();
    subDirectories.clear();
    if(!directory.isDirectory()) directory.mkdirs();
    if(!directory.isDirectory()) throw new IllegalStateException(directory.getPath() + " Is not a directory");
    File[] files = directory.listFiles();
    if(files == null) return this;
    for(File file : files) {
      if(file.isDirectory()) {
        if(allowsSubDirectories())
          subDirectories.add(new DynamicConfigurationDirectory(plugin, file, this));
        continue;
      }
	    String[] name = file.getName().split("\\.");
	    if(DynamicConfigurationManager.hasRegister(name[name.length-1]))
		    configurations.add(DynamicConfigurationManager.createConfiguration(plugin, this, file.getName()));
	    else if(DynamicConfigurationManager.hasRegister("." + name[name.length-1]))
		    configurations.add(DynamicConfigurationManager.createConfiguration(plugin, this, file.getName()));
    }
    return this;
  }

  public IDynamicConfiguration createConfiguration(String name) {
    DynamicConfigurationDirectory dir = this;
    String sp = name.contains(File.separator) ? File.separator : "/";
    String[] split = name.split(Pattern.quote(sp));
    if(name.contains(sp) && split.length > 0 && allowsSubDirectories())
      for(int i = 0; i < split.length - 1; i++) {
        String s = split[i];
        if(!s.isEmpty())
          dir = dir.getOrCreateSubDirectory(s);
      }
    IDynamicConfiguration configuration = DynamicConfigurationManager.createConfiguration(plugin, dir, name);
    dir.configurations.add(configuration);
    return configuration;
  }

  public IDynamicConfiguration createConfiguration(String resourceName, String name) {
    DynamicConfigurationDirectory dir = this;
    String sp = name.contains(File.separator) ? File.separator : "/";
    String[] split = name.split(Pattern.quote(sp));
    if(name.contains(sp) && split.length > 0 && allowsSubDirectories())
      for(int i = 0; i < split.length - 1; i++) {
        String s = split[i];
        if(!s.isEmpty())
          dir = dir.getOrCreateSubDirectory(s);
      }
    IDynamicConfiguration configuration = DynamicConfigurationManager.createConfiguration(plugin, dir, resourceName, name);
    dir.configurations.add(configuration);
    return configuration;
  }

  public IDynamicConfiguration getConfiguration(String name) {
    if(name.contains("/")) {
      DynamicConfigurationDirectory dir = this;
      String sp = name.contains(File.separator) ? File.separator : "/";
      String[] split = name.split(Pattern.quote(sp));
      for(int i = 0; i < split.length - 1; i++) {
        String s = split[i];
        if(!s.isEmpty()) dir = (dir.allowsSubDirectories() ? dir : this).getOrCreateSubDirectory(s);
      }
      return dir.getConfiguration(name);
    } else {
      for(IDynamicConfiguration configuration : configurations)
        if(configuration.name().equalsIgnoreCase(name)) return configuration;
    }
    return null;
  }

  public DynamicConfigurationDirectory getSubDirectory(String name) {
    if(!allowsSubDirectories()) throw new IllegalStateException("You can't grab a subdirectory if 'allowsSubDirectories' is false");
    for(DynamicConfigurationDirectory subDirectory : subDirectories) {
      if(subDirectory.name().equalsIgnoreCase(name)) return subDirectory;
    }
    return null;
  }


  public DynamicConfigurationDirectory getOrCreateSubDirectory(String name) {
    if(!allowsSubDirectories()) throw new IllegalStateException("You can't grab a subdirectory if 'allowsSubDirectories' is false");
    for(DynamicConfigurationDirectory subDirectory : subDirectories)
      if(subDirectory.name().equalsIgnoreCase(name)) return subDirectory;
    DynamicConfigurationDirectory dir = new DynamicConfigurationDirectory(plugin, new File(directory, name), this);
    subDirectories.add(dir);
    return dir;
  }

  public String name() {
    return directory.getName();
  }

  public DynamicConfigurationDirectory addConfiguration(IDynamicConfiguration configuration) {
    if(configuration.directory().getPath().equals(directory().getPath()) && !configurations.contains(configuration))
      configurations.add(configuration);
    else if(configuration.directory().getPath().startsWith(directory().getPath()) && allowsSubDirectories()) {
      String name = configuration.file().getPath().substring(directory().getPath().length());
      String sp = name.contains(File.separator) ? File.separator : "/";
      String[] split = name.split(Pattern.quote(sp));
      DynamicConfigurationDirectory dir = this;
      if(name.contains(sp) && split.length > 0 && allowsSubDirectories())
        for(int i = 0; i < split.length - 1; i++) {
          String s = split[i];
          if(!s.isEmpty())
            dir = dir.getOrCreateSubDirectory(s);
        }
      if(dir != this)
        dir.configurations.add(configuration);
    }
    return this;
  }

  public DynamicConfigurationDirectory regenerate() {
    for(IDynamicConfiguration configuration : configurations)
      configuration.regenerate();
    return this;
  }

  public DynamicConfigurationDirectory parent() {
    return parent;
  }

  public File directory() {
    return directory;
  }

  public JavaPlugin plugin() {
    return plugin;
  }

  public List<DynamicConfigurationDirectory> subDirectories() {
    return subDirectories;
  }

  public List<IDynamicConfiguration> configurations() {
    return configurations;
  }

  public boolean allowsSubDirectories() {
    return allowsSubDirectories;
  }

  public DynamicConfigurationDirectory allowsSubDirectories(boolean allowsSubDirectories) {
    this.allowsSubDirectories = allowsSubDirectories;
    return this;
  }
}
