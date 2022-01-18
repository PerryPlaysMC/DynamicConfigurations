package config;

import config.json.DynamicJsonConfiguration;
import config.yaml.DynamicYamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/

public class DynamicConfigManager {

   private static final Set<IDynamicConfiguration> CONFIGURATIONS = new HashSet<>();

   public static void addConfiguration(IDynamicConfiguration configuration) {
      CONFIGURATIONS.add(configuration);
   }

   public static void removeConfiguration(IDynamicConfiguration configuration) {
      CONFIGURATIONS.remove(configuration);
   }

   public static IDynamicConfiguration getConfiguration(String name) {
      for(IDynamicConfiguration configuration : getConfigurations())
         if(configuration.getName().equals(name) || configuration.getFile().getName().equals(name)
            ||(configuration.getDirectory().getName() + "/" +configuration.getName()).equals(name)
            || (configuration.getDirectory().getName() + "/" +configuration.getFile().getName()).equals(name)) return configuration;
      return null;
   }

   public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, String name) {
      return createConfiguration(plugin, null, name);
   }

   public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, String directory, String name) {
      IDynamicConfiguration config = getConfiguration(name);
      if(config == null) config = getConfiguration(directory + "/" + name);
      if(config == null)
         if(name.endsWith(".json")) config = new DynamicJsonConfiguration(plugin, directory, name);
         else config = new DynamicYamlConfiguration(plugin, directory, name);
      return config;
   }

   public static List<String> readInputStream(InputStream inputStream) {
      try {
         if(inputStream == null) return new ArrayList<>();
         Scanner scanner = new Scanner(inputStream);
         String lines = "";
         while(scanner.hasNext()) lines+="\n"+scanner.nextLine();
         return Arrays.asList(lines.substring(1).split("\n"));
      } catch (Exception e) {
         e.printStackTrace();
      }
      return new ArrayList<>();
   }
   public static List<String> readFile(File file) {
      if(file == null || !file.exists()) return new ArrayList<>();
      FileInputStream inputStream = null;
      try {
         inputStream = new FileInputStream(file);
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }
      return readInputStream(inputStream);
   }

   public static String prepareConfigString(UUID id, String configString) {
      Pattern pat = Pattern.compile("(\\s*" + id + "_COMMENT_[0-9]+: '*(.+))");
      Matcher match = pat.matcher(configString);
      while(match.find()) {
         String text = match.group(2);
         configString = configString.replace(match.group(1),("\n#" +
            (!text.startsWith(" ") ? " " : "") + (text.endsWith("'") ? text.substring(0,text.length()-1):text)));
         match = pat.matcher(configString);
      }
      return configString;
   }

   public static int getComments(File file) {
      if(file == null || !file.exists()) return 0;
      try {
         int comments = 0;
         String currentLine;

         BufferedReader reader = new BufferedReader(new FileReader(file));

         while((currentLine = reader.readLine()) != null)
            if(currentLine.startsWith("#"))
               comments++;

         reader.close();
         return comments;
      } catch (IOException e) {
         e.printStackTrace();
         return 0;
      }
   }

   public static InputStream getContent(UUID id, File file) {
      if(file == null || !file.exists()) return null;
      try {
         int commentNum = 0;

         String addLine;
         String currentLine;

         StringBuilder whole = new StringBuilder();
         BufferedReader reader = new BufferedReader(new FileReader(file));

         while((currentLine = reader.readLine()) != null) {
            if(currentLine.startsWith("#")) {
               addLine = currentLine.replaceFirst("#", id + "_COMMENT_" + commentNum + ":");
               whole.append(addLine).append("\n");
               commentNum++;
            } else whole.append(currentLine).append("\n");
         }

         String config = whole.toString();
         InputStream configStream = new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8));
         reader.close();
         return configStream;
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }

   public static InputStream getContent(UUID id, InputStream file) {
      if(file == null) return null;
      try {
         int commentNum = 0;

         String addLine;
         String currentLine;

         StringBuilder whole = new StringBuilder();
         BufferedReader reader = new BufferedReader(new InputStreamReader(file));

         while((currentLine = reader.readLine()) != null) {
            if(currentLine.startsWith("#")) {
               addLine = currentLine.replaceFirst("#", id + "_COMMENT_" + commentNum + ":");
               whole.append(addLine).append("\n");
               commentNum++;
            } else whole.append(currentLine).append("\n");
         }

         String config = whole.toString();
         InputStream configStream = new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8));

         reader.close();
         return configStream;
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }

   public static void writeFile(InputStream is, File toPrint) {
      try {
         FileOutputStream outputStream = new FileOutputStream(toPrint);
         outputStream.write(String.join("\n", readInputStream(is)).getBytes());
         outputStream.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static void writeFile(UUID id, InputStream is, File toPrint) {
      try {
         FileOutputStream outputStream = new FileOutputStream(toPrint);
         outputStream.write(String.join("\n",readInputStream(getContent(id,is))).getBytes());
         outputStream.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
   public static void writeFile(List<String> out, File toPrint) {
      try {
         if(out==null)return;
         FileOutputStream outputStream = new FileOutputStream(toPrint);
         outputStream.write(String.join("\n",out).getBytes());
         outputStream.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static Set<IDynamicConfiguration> getConfigurations() {
      return CONFIGURATIONS;
   }
}
