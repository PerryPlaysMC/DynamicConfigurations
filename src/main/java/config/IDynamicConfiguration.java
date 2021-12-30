package config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
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
