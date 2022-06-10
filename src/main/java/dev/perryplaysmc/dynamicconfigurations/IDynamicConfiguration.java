package dev.perryplaysmc.dynamicconfigurations;

import dev.perryplaysmc.dynamicconfigurations.utils.DynamicConfigurationDirectory;
import dev.perryplaysmc.dynamicconfigurations.utils.DynamicConfigurationOptions;
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

  DynamicConfigurationOptions options();

  DynamicConfigurationDirectory configurationDirectory();

  IDynamicConfiguration configurationDirectory(DynamicConfigurationDirectory directory);

  String name();

  Map<String, String> comments();

  Map<String, String> inlineComments();

  IDynamicConfiguration regenerate();

  String saveToString();
}
