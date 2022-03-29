package dev.perryplaysmc.dynamicconfigurations.utils;

import dev.perryplaysmc.dynamicconfigurations.IDynamicConfiguration;
import org.yaml.snakeyaml.DumperOptions;

/**
 * Creator: PerryPlaysMC
 * Created: 03/2022
 **/
public class DynamicConfigurationOptions {

   private int indent = 2;
   private boolean autoSave = false;
   private boolean appendMissingKeys = false;
   private StringWrap stringWrap = StringWrap.DOUBLE_QUOTED;
   private final IDynamicConfiguration parentConfiguration;

   public DynamicConfigurationOptions(IDynamicConfiguration parentConfiguration) {
      this.parentConfiguration = parentConfiguration;
   }

   /**
    * Indent length
    */
   public int indent() {
      return indent;
   }

   /**
    * How far indented would you like it?
    * @param indent Indention length
    */
   public DynamicConfigurationOptions indent(int indent) {
      this.indent = indent;
      return this;
   }

   /**
    * Will it save whenever it is edited
    */
   public boolean autoSave() {
      return autoSave;
   }

   /**
    * Should it save whenever it is edited
    */
   public DynamicConfigurationOptions autoSave(boolean autoSave) {
      this.autoSave = autoSave;
      return this;
   }

   public boolean appendMissingKeys() {
      return appendMissingKeys;
   }

   public DynamicConfigurationOptions appendMissingKeys(boolean appendMissingKeys) {
      this.appendMissingKeys = appendMissingKeys;
      return this;
   }

   public StringWrap stringWrap() {
      return stringWrap;
   }

   public void stringWrap(StringWrap stringWrap) {
      this.stringWrap = stringWrap;
   }


   public IDynamicConfiguration finish() {
      return parentConfiguration;
   }

   enum StringWrap {
      NONE,
      DOUBLE_QUOTED('"'),
      SINGLE_QUOTED('\'');
      private final Character wrapWith;
      StringWrap(Character c) {
         this.wrapWith = c;
      }
      StringWrap() {
         this(null);
      }

      public Character wrapWith() {
         return wrapWith;
      }
   }

}
