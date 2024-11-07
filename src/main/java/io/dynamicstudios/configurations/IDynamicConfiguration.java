package io.dynamicstudios.configurations;

import io.dynamicstudios.configurations.utils.DynamicConfigurationDirectory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;


/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/

public interface IDynamicConfiguration extends IDynamicConfigurationSection {

  File file();

  File directory();

  JavaPlugin plugin();

  DynamicConfigurationDirectory configurationDirectory();

  IDynamicConfiguration configurationDirectory(DynamicConfigurationDirectory directory);

  String name();

  boolean supportsComments();

  boolean isGhost();

  Map<String, String> comments();

  Map<String, String> inlineComments();

  IDynamicConfiguration regenerate();

  String saveToString();
}
