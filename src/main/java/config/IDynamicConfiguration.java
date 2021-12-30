package config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Copy Right ©
 * This code is private
 * Owner: PerryPlaysMC *
 * Any attempts to use these program(s) may result in a penalty of up to $5,000 USD
 **/

public interface IDynamicConfiguration extends IDynamicConfigurationSection {

   File getFile();

   File getDirectory();

   String getName();

   boolean isAutoSave();

   /**
    * Should it save whenever it is edited
    */
   IDynamicConfiguration autoSave(boolean autoSave);

   IDynamicConfiguration regenerate();

}
