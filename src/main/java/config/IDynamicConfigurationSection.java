package config;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/

public interface IDynamicConfigurationSection {

   String id();

   Map<String, Object> data();

   /**
    * Save the config
    * @return This
    */
   IDynamicConfigurationSection save();

   /**
    * Reload the config
    * @return This
    */
   IDynamicConfigurationSection reload();


   /**
    * @return All key values
    */
   Set<String> getKeys(boolean deep);

   /**
    * Check if the config has a path set
    * @param path
    * @return Is the path set in the config
    */
   boolean isSet(String path);

   /**
    * Set a value in the config with the provided path
    * @param path Path to get to the config
    * @param value Object to be set in the file
    * @return this
    */
   IDynamicConfigurationSection set(String path, Object value);

   /**
    * Set a value in the config with the provided path with a comment
    * @param path Path to get to the config
    * @param value Object to be set in the file
    * @param comment The comment associated with the data
    * @return this
    */
   IDynamicConfigurationSection set(String path, Object value, String comment);

   /**
    * Set a value in the config with the provided path with an inline-comment
    * @param path Path to get to the config
    * @param value Object to be set in the file
    * @param comment The comment associated with the data
    * @return this
    */
   IDynamicConfigurationSection setInline(String path, Object value, String comment);

   /**
    * Add a comment to the file from the previous key
    * @param comment The comment
    * @return this
    */
   IDynamicConfigurationSection comment(String... comment);

   /**
    * Add an inline-comment to the file from the previous key
    * @param comment The comment
    * @return this
    */
   IDynamicConfigurationSection inlineComment(String... comment);

   /**
    * Get an Object from the config
    * @param path The path where the object is located
    * @return The object from the config, null if not set
    */
   default Object get(String path) {return get(path, null);}
   /**
    * Get an Object from the config
    * @param path The path where the object is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The object from the config, defaultValue if not set
    */
   Object get(String path, Object defaultValue);

   /**
    * Get a ConfigurationSection
    * @param path The path where the configuration section is located
    * @return The Configuration Section
    */
   IDynamicConfigurationSection getSection(String path);

   /**
    * Create a ConfigurationSection
    * @param path The path where the configuration section will be located
    * @return The Configuration Section that was created
    */
   IDynamicConfigurationSection createSection(String path);

   /**
    * Create a ConfigurationSection
    * @param path The path where the configuration section will be located
    * @return The Configuration Section that was created
    */
   IDynamicConfigurationSection createSection(String path, String comment);

   /**
    * Get a String from the config
    * @param path The path where the object is located
    * @return The String from the config, null if not set
    */
   default String getString(String path) {return getString(path,null);}
   /**
    * Get a String from the config
    * @param path The path where the object is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The String from the config, defaultValue if not set
    */
   String getString(String path, String defaultValue);
   /**
    * Get a Double from the config
    * @param path The path where the Double is located
    * @return The String from the config, null if not set
    */
   default Double getDouble(String path) {return getDouble(path,null);}
   /**
    * Get a Double from the config
    * @param path The path where the object is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The Double from the config, defaultValue if not set
    */
   Double getDouble(String path, Double defaultValue);

   /**
    * Get a Integer from the config
    * @param path The path where the Integer is located
    * @return The Integer from the config, null if not set
    */
   default Integer getInteger(String path) {return getInteger(path,null);}
   /**
    * Get a Integer from the config
    * @param path The path where the Integer is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The Integer from the config, defaultValue if not set
    */
   Integer getInteger(String path, Integer defaultValue);

   /**
    * Get a Float from the config
    * @param path The path where the Float is located
    * @return The Float from the config, null if not set
    */
   default Float getFloat(String path) {return getFloat(path,null);}
   /**
    * Get a Float from the config
    * @param path The path where the Float is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The Float from the config, defaultValue if not set
    */
   Float getFloat(String path, Float defaultValue);

   /**
    * Get a Byte from the config
    * @param path The path where the Byte is located
    * @return The Byte from the config, null if not set
    */
   default Byte getByte(String path) {return getByte(path,null);}
   /**
    * Get a Byte from the config
    * @param path The path where the Byte is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The Byte from the config, defaultValue if not set
    */
   Byte getByte(String path, Byte defaultValue);

   /**
    * Get a Boolean from the config
    * @param path The path where the Boolean is located
    * @return The Boolean from the config, null if not set
    */
   default Boolean getBoolean(String path) {return getBoolean(path,null);}
   /**
    * Get a Boolean from the config
    * @param path The path where the Boolean is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The Boolean from the config, defaultValue if not set
    */
   Boolean getBoolean(String path, Boolean defaultValue);


   /**
    * Get a String from the config
    * @param path The path where the String is located
    * @return The String from the config, null if not set
    */
   default String getMessage(String path) {return getMessage(path,null);}
   /**
    * Get a String from the config
    * @param path The path where the String is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The String from the config, defaultValue if not set
    */
   String getMessage(String path, String defaultValue);


   /**
    * Get a List<Object> from the config
    * @param path The path where the object is located
    * @return The List<Object> from the config, null if not set
    */
   default List<?> getList(String path) {return getList(path,null);}
   /**
    * Get a List<Object> from the config
    * @param path The path where the object is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The List<Object> from the config, defaultValue if not set
    */
   List<?> getList(String path, List<?> defaultValue);


   /**
    * Get a List<String> from the config
    * @param path The path where the object is located
    * @return The List<String> from the config, null if not set
    */
   default List<String> getListString(String path) {return getListString(path,null);}
   /**
    * Get a List<String> from the config
    * @param path The path where the object is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The List<String> from the config, defaultValue if not set
    */
   List<String> getListString(String path, List<String> defaultValue);


   /**
    * Get a List<Double> from the config
    * @param path The path where the object is located
    * @return The List<Double> from the config, null if not set
    */
   default List<Double> getListDouble(String path) {return getListDouble(path,null);}
   /**
    * Get a List<Double> from the config
    * @param path The path where the object is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The List<Double> from the config, defaultValue if not set
    */
   List<Double> getListDouble(String path, List<Double> defaultValue);


   /**
    * Get a List<Integer> from the config
    * @param path The path where the object is located
    * @return The List<Integer> from the config, null if not set
    */
   default List<Integer> getListInteger(String path) {return getListInteger(path,null);}
   /**
    * Get a List<Integer> from the config
    * @param path The path where the object is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The List<Integer> from the config, defaultValue if not set
    */
   List<Integer> getListInteger(String path, List<Integer> defaultValue);


   /**
    * Get a List<Float> from the config
    * @param path The path where the object is located
    * @return The List<Float> from the config, null if not set
    */
   default List<Float> getListFloat(String path) {return getListFloat(path,null);}
   /**
    * Get a List<Float> from the config
    * @param path The path where the object is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The List<Float> from the config, defaultValue if not set
    */
   List<Float> getListFloat(String path, List<Float> defaultValue);


   /**
    * Get a List<Byte> from the config
    * @param path The path where the object is located
    * @return The List<Byte> from the config, null if not set
    */
   default List<Byte> getListByte(String path) {return getListByte(path,null);}
   /**
    * Get a List<Byte> from the config
    * @param path The path where the object is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The List<Byte> from the config, defaultValue if not set
    */
   List<Byte> getListByte(String path, List<Byte> defaultValue);


   /**
    * Get a List<Boolean> from the config
    * @param path The path where the object is located
    * @return The List<Boolean> from the config, null if not set
    */
   default List<Boolean> getListBoolean(String path) {return getListBoolean(path,null);}
   /**
    * Get a List<Boolean> from the config
    * @param path The path where the object is located
    * @param defaultValue If the path is not set, return defaultValue
    * @return The List<Boolean> from the config, defaultValue if not set
    */
   List<Boolean> getListBoolean(String path, List<Boolean> defaultValue);


   default String asString() {
      String res = (id().isEmpty() ? "" : id() + ": ")+"{\n";
      for(Map.Entry<String, Object> key_val : data().entrySet()) {
         String key = key_val.getKey();
         Object val = key_val.getValue();
         res+=(res.length()>2?",\n":"")+(indent(1))+key+": ";
         if(val instanceof Map)
            res+=fromMap(2,(Map<?, ?>) val);
         if(val instanceof IDynamicConfigurationSection)
            res+=fromMap(2,((IDynamicConfigurationSection) val).data());
         else res+=val;
      }
      return res + (res.length() > 2 ? "\n}" : "}");
   }

   default String fromMap(int indent, Map<?,?> objects) {
      String res = "{\n";
      for(Map.Entry<?, ?> key_val : objects.entrySet()) {
         Object key = key_val.getKey();
         Object val = key_val.getValue();
         res+=(res.length()>2?",\n":"")+indent(indent)+key+": ";
         if(val instanceof Map) res+=fromMap(indent+1,(Map<?, ?>) val);
         if(val instanceof IDynamicConfigurationSection) res+=fromMap(indent+1,((IDynamicConfigurationSection) val).data());
         else res+=val;
      }
      return res + (res.length() > 2 ? "\n"+indent(indent-1) +  "}" : "}");
   }

   default String indent(int indents) {
      String res = "";
      for(int i = 0; i < indents; i++)res+=" ";
      return res;
   }


}
