package config;

import java.io.File;
import java.util.Map;


/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/

public interface IDynamicConfiguration extends IDynamicConfigurationSection {

   File file();

   File directory();

   DynamicConfigurationDirectory configurationDirectory();

   IDynamicConfiguration configurationDirectory(DynamicConfigurationDirectory directory);

   String name();

   boolean autoSave();

   Map<String,String> comments();

   Map<String,String> inlineComments();

   /**
    * Should it save whenever it is edited
    */
   IDynamicConfiguration autoSave(boolean autoSave);

   IDynamicConfiguration regenerate();

}
