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

 IDynamicConfiguration rename(String name);

 IDynamicConfiguration onReload(Runnable... onReload);

 IDynamicConfiguration addReloadListener(Runnable onReload);

 IDynamicConfiguration clearReloadListeners();

 boolean supportsComments();

 boolean isGhost();

 Map<String, String> comments();

 Map<String, String> inlineComments();

 IDynamicConfiguration regenerate();

 String saveToString();


 public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, String name) {
	return DynamicConfigurationManager.createConfiguration(plugin, name);
 }
 public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, File directory, String name) {
	return DynamicConfigurationManager.createConfiguration(plugin, directory, name);
 }
 public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, File directory, String resourceName, String name) {
	return DynamicConfigurationManager.createConfiguration(plugin, directory, resourceName, name);
 }
 public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, String directory, String name) {
	return DynamicConfigurationManager.createConfiguration(plugin, directory, name);
 }
 public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, String directory, String resourceName, String name) {
	return DynamicConfigurationManager.createConfiguration(plugin, directory, resourceName, name);
 }
 public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, DynamicConfigurationDirectory directory, String name) {
	return DynamicConfigurationManager.createConfiguration(plugin, directory, name);
 }
 public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, DynamicConfigurationDirectory directory, String resourceName, String name) {
	return DynamicConfigurationManager.createConfiguration(plugin, directory, resourceName, name);
 }
 public static IDynamicConfiguration createGhostConfiguration(JavaPlugin plugin, String fileName, String configName) {
	return DynamicConfigurationManager.createGhostConfiguration(plugin, fileName, configName);
 }
 public static IDynamicConfiguration createGhostConfiguration(JavaPlugin plugin, String resourceName, String fileName, String configName) {
	return DynamicConfigurationManager.createGhostConfiguration(plugin, resourceName, fileName, configName);
 }
}
