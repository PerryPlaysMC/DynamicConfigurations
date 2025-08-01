package io.dynamicstudios.configurations.yaml.bukkit;

import org.bukkit.configuration.file.FileConfigurationOptions;

/**
 * Various settings for controlling the input and output of a {@link
 * YamlConfigurationUtil}
 */
public class YamlConfigurationOptions extends FileConfigurationOptions {
 private int indent = 2;

 protected YamlConfigurationOptions(YamlConfigurationUtil configuration) {
	super(configuration);
 }

 @Override
 public YamlConfigurationUtil configuration() {
	return (YamlConfigurationUtil) super.configuration();
 }

 @Override
 public YamlConfigurationOptions copyDefaults(boolean value) {
	super.copyDefaults(value);
	return this;
 }

 @Override
 public YamlConfigurationOptions pathSeparator(char value) {
	super.pathSeparator(value);
	return this;
 }

 @Override
 public YamlConfigurationOptions header(String value) {
	super.header(value);
	return this;
 }

 @Override
 public YamlConfigurationOptions copyHeader(boolean value) {
	super.copyHeader(value);
	return this;
 }

 /**
	* Gets how much spaces should be used to indent each line.
	* <p>
	* The minimum value this may be is 2, and the maximum is 9.
	*
	* @return How much to indent by
	*/
 public int indent() {
	return indent;
 }

 /**
	* Sets how much spaces should be used to indent each line.
	* <p>
	* The minimum value this may be is 2, and the maximum is 9.
	*
	* @param value New indent
	* @return This object, for chaining
	*/
 public YamlConfigurationOptions indent(int value) {
	if(value < 2 || value > 9) return this;

	this.indent = value;
	return this;
 }
}
