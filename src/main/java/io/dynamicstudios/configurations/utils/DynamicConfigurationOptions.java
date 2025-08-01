package io.dynamicstudios.configurations.utils;

import io.dynamicstudios.configurations.DynamicConfigurationManager;
import io.dynamicstudios.configurations.IDynamicConfiguration;

import java.util.UUID;

/**
 * Creator: PerryPlaysMC
 * Created: 03/2022
 **/
public class DynamicConfigurationOptions<T extends IDynamicConfiguration> {

 private final StringBuilder indentString = new StringBuilder("  ");
 private final T parentConfiguration;
 private final IDynamicConfiguration defaults;
 private int indent = 2;
 private boolean autoSave = false;
 private boolean appendMissingKeys = false;
 private boolean loadDefaults = false;
 private boolean hasLoaded = false;
 private StringWrap stringWrap = StringWrap.DOUBLE_QUOTED;

 public DynamicConfigurationOptions(T parentConfiguration) {
	this.parentConfiguration = parentConfiguration;
	if(!parentConfiguration.isGhost() && FileUtils.findStream(parentConfiguration.plugin(), parentConfiguration.file()) != null)
	 this.defaults = DynamicConfigurationManager.createGhostConfiguration(parentConfiguration.plugin(), parentConfiguration.name(),
			UUID.randomUUID() + parentConfiguration.name());
	else this.defaults = null;
 }

 /**
	* Will it load the defaults from the file?
	*
	* @return boolean
	*/
 public boolean loadDefaults() {
	return loadDefaults;
 }

 public DynamicConfigurationOptions<T> loadDefaults(boolean enabled) {
	loadDefaults = enabled;
	if(loadDefaults && !hasLoaded) {
	 configuration().reload();
	 hasLoaded = true;
	}
	return this;
 }

 public IDynamicConfiguration defaults() {
	return defaults;
 }

 /**
	* Indent length
	*/
 public int indent() {
	return indent;
 }

 /**
	* How far indented would you like it?
	*
	* @param indent Indention length
	*/
 public DynamicConfigurationOptions<T> indent(int indent) {
	this.indent = indent;
	indentString.setLength(0);
	for(int i = 0; i < indent; i++)
	 indentString.append(" ");
	return this;
 }

 public String indentString() {
	return indentString.toString();
 }

 /**
	* Will it save whenever it's edited?
	*/
 public boolean autoSave() {
	return autoSave;
 }

 /**
	* Should it save whenever it's edited?
	*/
 public DynamicConfigurationOptions<T> autoSave(boolean autoSave) {
	this.autoSave = autoSave;
	return this;
 }

 public boolean appendMissingKeys() {
	return appendMissingKeys;
 }

 public DynamicConfigurationOptions<T> appendMissingKeys(boolean appendMissingKeys) {
	this.appendMissingKeys = appendMissingKeys;
	return this;
 }

 public StringWrap stringWrap() {
	return stringWrap;
 }

 public DynamicConfigurationOptions<T> stringWrap(StringWrap stringWrap) {
	this.stringWrap = stringWrap;
	return this;
 }


 public T configuration() {
	return parentConfiguration;
 }

 @Override
 public String toString() {
	return "DynamicConfigurationOptions{\n" +
		 "indentString=" + indentString +
		 ", \nparentConfiguration=" + parentConfiguration +
		 ", \ndefaults=" + defaults +
		 ", \nindent=" + indent +
		 ", \nautoSave=" + autoSave +
		 ", \nappendMissingKeys=" + appendMissingKeys +
		 ", \nloadDefaults=" + loadDefaults +
		 ", \nhasLoaded=" + hasLoaded +
		 ", \nstringWrap=" + stringWrap +
		 "\n}";
 }
}
