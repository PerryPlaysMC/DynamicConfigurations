package config;

import config.json.DynamicJsonConfiguration;
import config.json.DynamicJsonConfigurationSection;
import config.yaml.DynamicYamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/

public class DynamicConfigurationManager {

   public interface ConfigCreate {
      IDynamicConfiguration create(JavaPlugin plugin, Object directory, String name);
   }

   private static final Set<IDynamicConfiguration> CONFIGURATIONS = new HashSet<>();
   private static final Set<DynamicConfigurationDirectory> CONFIGURATION_DIRECTORIES = new HashSet<>();
   private static final Map<String, ConfigCreate> CONFIG_EXTENSION_REGISTER = new HashMap<>();

   static {
      registerExtension(".yml", (plugin, directory, name) -> {
         if(directory instanceof File) return new DynamicYamlConfiguration(plugin,(File)directory,name);
         if(directory instanceof DynamicConfigurationDirectory) return new DynamicYamlConfiguration(plugin, (DynamicConfigurationDirectory) directory,name);
         if(directory instanceof Supplier)
            return new DynamicYamlConfiguration(plugin, (Supplier<InputStream>) directory);
         return new DynamicYamlConfiguration(plugin, "", name);
      });
      registerExtension(".json", (plugin, directory, name) -> {
         if(directory instanceof File) return new DynamicJsonConfiguration(plugin, (File) directory, name);
         if(directory instanceof DynamicConfigurationDirectory)
            return new DynamicJsonConfiguration(plugin, (DynamicConfigurationDirectory) directory, name);
         if(directory instanceof Supplier)
            return new DynamicJsonConfiguration(plugin, (Supplier<InputStream>) directory);
         return new DynamicJsonConfiguration(plugin, "", name);
      });
   }

   public static void registerExtension(String extension, ConfigCreate clazz) {
      CONFIG_EXTENSION_REGISTER.put((extension.startsWith(".") ? "" : ".") + extension,clazz);
   }

   public static void addConfiguration(IDynamicConfiguration configuration) {
      CONFIGURATIONS.add(configuration);
   }

   public static void removeConfiguration(IDynamicConfiguration configuration) {
      CONFIGURATIONS.remove(configuration);
   }

   public static IDynamicConfiguration getConfiguration(String name) {
      for(IDynamicConfiguration configuration : getConfigurations())
         if(configuration.name().equals(name) || configuration.file().getName().equals(name)
            ||(configuration.directory().getName() + "/" +configuration.name()).equals(name)
            || (configuration.directory().getName() + "/" +configuration.file().getName()).equals(name)) return configuration;
      return null;
   }


   public static void addConfigurationDirectory(DynamicConfigurationDirectory configuration) {
      CONFIGURATION_DIRECTORIES.add(configuration);
   }

   public static void removeConfigurationDirectory(DynamicConfigurationDirectory configuration) {
      CONFIGURATION_DIRECTORIES.remove(configuration);
   }

   public static DynamicConfigurationDirectory getConfigurationDirectory(String name) {
      for(DynamicConfigurationDirectory configuration : getConfigurationDirectories())
         if(configuration.name().equals(name)||configuration.directory().getPath().equals(name)) return configuration;
      return null;
   }

   public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, String name) {
      return createConfiguration(plugin, name.contains("/") ? null : plugin.getDataFolder(), name);
   }
   public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, File directory, String name) {
      IDynamicConfiguration config = getConfiguration(name);
      if(config == null) config = getConfiguration((directory != null ? directory.getPath() + "/":"") + name);
      if(config == null) {
         if(name.lastIndexOf('.') != -1) {
            String extension = name.substring(name.lastIndexOf('.'));
            if(CONFIG_EXTENSION_REGISTER.containsKey(extension))
               return CONFIG_EXTENSION_REGISTER.get(extension).create(plugin, directory, name);
         }
         return new DynamicYamlConfiguration(plugin, directory, name);
      }
      return config;
   }

   public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, String directory, String name) {
      return createConfiguration(plugin, directory == null ? null : new File(directory), name);
   }
   public static IDynamicConfiguration createConfiguration(JavaPlugin plugin, DynamicConfigurationDirectory directory, String name) {
      IDynamicConfiguration config = getConfiguration(name);
      if(config == null) config = getConfiguration(name);
      if(config == null) {
         String extension = name.substring(name.lastIndexOf('.'));
         if(CONFIG_EXTENSION_REGISTER.containsKey(extension))
            return CONFIG_EXTENSION_REGISTER.get(extension).create(plugin,directory,name);
         else return new DynamicYamlConfiguration(plugin, directory, name);
      }
      return config;
   }

   public static IDynamicConfiguration createGhostConfiguration(JavaPlugin plugin, String name) {
         String extension = name.substring(name.lastIndexOf('.'));
         if(CONFIG_EXTENSION_REGISTER.containsKey(extension))
            return CONFIG_EXTENSION_REGISTER.get(extension).create(plugin, (Supplier)() -> plugin.getResource(name), null);
         else return new DynamicYamlConfiguration(plugin, () -> plugin.getResource(name));
   }

   public static boolean matches(IDynamicConfiguration configuration, IDynamicConfiguration ghostConfiguration) {
      Set<String> list1 = ghostConfiguration.getKeys(true);
      Set<String> list2 = configuration.getKeys(true);
      list1.removeAll(list2);
      return list1.isEmpty();
   }

   public static boolean pasteDifferences(IDynamicConfiguration ghostConfiguration, IDynamicConfiguration configuration) {
      Set<String> list1 = ghostConfiguration.getKeys(true);
      Set<String> list2 = configuration.getKeys(true);
      list1.removeAll(list2);
      if(!list1.isEmpty()) {
         for(String s : list1)
            if(ghostConfiguration.comments().containsKey(s))
               configuration.set(s,ghostConfiguration.get(s), ghostConfiguration.comments().get(s));
            else
               configuration.setInline(s,ghostConfiguration.get(s), ghostConfiguration.inlineComments().getOrDefault(s, ""));
         configuration.save();
         return true;
      }
      return false;
   }

   public static List<String> readInputStream(InputStream inputStream) {
      try {
         if(inputStream == null) return new ArrayList<>();
         Scanner scanner = new Scanner(inputStream);
         StringBuilder lines = new StringBuilder();
         while(scanner.hasNext()) lines.append("\n").append(scanner.nextLine());
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

   public static String prepareConfigString(String configString, IDynamicConfiguration configuration, HashMap<String, String> comments, HashMap<String, String> inlineComments) {
      String path;
      int lastIndent = -1;
      List<String> followPath = new ArrayList<>();
      Pattern pattern = Pattern.compile("(\\s*)([^:]+):\\s*('?.+'?)?", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
      Pattern pattern2 = Pattern.compile("(\\s*)-\\s*('?.+'?)?", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
      StringBuilder config = new StringBuilder();
      String[] split = configString.split("\n");
      for(int i = 0; i < split.length; i++) {
         String currentLine = split[i];
         StringBuilder og = new StringBuilder(currentLine);
         Matcher matcher = pattern.matcher(currentLine);
         int indent = 0;
         String indentStr = "", val = "";
         int s = -1, e = -1;
         if(matcher.find()) {
            indent = (indentStr = matcher.group(1)).length();
            currentLine = matcher.group(2);
            if(matcher.group(3) != null) {
               val = matcher.group(3);
               s = matcher.start(3);
               e = matcher.end(3);
            }
         }
         if(currentLine.isEmpty() || currentLine.replaceAll("\\s", "").startsWith("#")) {
            config.append("\n ");
            continue;
         }
         followPath = createPath(followPath, currentLine, indent, lastIndent);
         path = String.join(".", followPath).replace("'", "");
         if(configuration.get(path) instanceof String && !val.isEmpty() && e > -1 && s > -1) {
            StringBuilder value = new StringBuilder(val);
            if(val.startsWith("'") && !val.endsWith("'")) i = appendMessage(split, i,indentStr,value);
            if(!value.substring(1).startsWith("'"))value.insert(0,"'");
            if(value.charAt(value.length()-1) != '\'') value.append('\'');
            og.replace(s,e,value.toString());
         }else if(configuration.getListString(path, null) != null) {
            int nx = i+1;
            String nl;
            while(split.length > nx && (nl = split[nx]).startsWith(indentStr + "-")) {
               Matcher matcher1 = pattern2.matcher(nl);
               if(matcher1.find()) {
                  StringBuilder value = new StringBuilder(matcher1.group(2));
                  if(value.charAt(0) == '\'' && value.charAt(value.length()-1) != '\'')
                     i = appendMessage(split, nx, indentStr, value);
                  if(value.charAt(0) != '-')value.insert(0, "- ");
                  if(!value.substring(1).startsWith("'"))value.insert(2,"'");
                  if(value.charAt(value.length()-1) != '\'') value.append('\'');
                  og.append("\n").append(indentStr).append(value);
               }else og.append("\n").append(nl, 0, (indentStr + "-").length()).append("- '").append(nl.substring((indentStr + "-").length())).append("'");
               nx++;
            }
            if(nx -1 != i) i = nx-1;
         }
         if(!config.toString().isEmpty())
            config.append("\n");
         if(inlineComments.containsKey(path) && inlineComments.get(path).length() > 1)
            config.append(og).append(" ").append(inlineComments.get(path));
         else if(comments.containsKey(path) && comments.get(path).length() > 1)
            config.append(indentStr).append(comments.get(path)).append("\n").append(og);
         else config.append(og);
         lastIndent = indent;
      }
      return config.toString();
   }

   private static int appendMessage(String[] split, int i, String indentStr, StringBuilder value) {
      int nextIndex = i + 1;
      StringBuilder nextLine;
      boolean didAppend = false;
      while(split.length > nextIndex && (nextLine = new StringBuilder(split[nextIndex])).toString().startsWith(indentStr)) {
         if(nextLine.toString().endsWith("'")) {
            value.append(nextLine.substring(indentStr.length()));
            didAppend = true;
            break;
         }
         nextIndex++;
      }
      if(didAppend) i = nextIndex;
      return i;
   }

   public static List<String> getKeys(InputStream file) {
      if(file == null) return new LinkedList<>();
      try {
         String currentLine;
         if(file.markSupported())
            file.reset();
         BufferedReader reader = new BufferedReader(new InputStreamReader(file));
         List<String> keys = new LinkedList<>();
         String path;
         int lastIndent = -1;
         Pattern pattern = Pattern.compile("(\\s*)([^:]+)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

         List<String> followPath = new ArrayList<>();
         while((currentLine = reader.readLine()) != null) {
            Matcher matcher = pattern.matcher(currentLine);
            int indent = 0;
            if(matcher.find()) {
               indent = matcher.group(1).length();
               currentLine = matcher.group(2);
            }
            if(currentLine.isEmpty()||currentLine.startsWith("#"))continue;
            followPath = createPath(followPath, currentLine, indent, lastIndent);
            path = String.join(".",followPath).replace("'","");
            keys.add(path);
            lastIndent = indent;
         }
         return keys;
      } catch (IOException e) {
         e.printStackTrace();
         return new LinkedList<>();
      }
   }

   public static Map<String, String> getComments(InputStream file) {
      if(file == null) return new HashMap<>();
      try {
         String currentLine;
         if(file.markSupported())
            file.reset();
         BufferedReader reader = new BufferedReader(new InputStreamReader(file));
         HashMap<String, String> map = new HashMap<>();
         String path, comment = "";
         int lastIndent = -1;
         Pattern pattern = Pattern.compile("(\\s*)([^:]+)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

         List<String> followPath = new ArrayList<>();
         while((currentLine = reader.readLine()) != null) {
            Matcher matcher = pattern.matcher(currentLine);
            int indent = 0;
            if(matcher.find()) {
               indent = matcher.group(1).length();
               currentLine = matcher.group(2);
            }
            if(currentLine.isEmpty())continue;
            if(currentLine.startsWith("#")) {
               comment += currentLine + (comment.isEmpty() ? "" : "\n");
               continue;
            }
            followPath = createPath(followPath, currentLine, indent, lastIndent);
            path = String.join(".",followPath).replace("'","");
            if(comment.length()>1) {
               map.put(path, comment);
               comment = "";
            }
            lastIndent = indent;
         }
         return map;
      } catch (IOException e) {
         e.printStackTrace();
         return new HashMap<>();
      }
   }

   private static List<String> createPath(List<String> followPath, String currentLine, int indent, int lastIndent) {
      followPath.add(currentLine);
      if(indent == 0) {
         followPath.clear();
         followPath.add(currentLine);
      }else if(indent == lastIndent) followPath.remove(followPath.size()-2);
      else if(indent == lastIndent -2) {
         followPath.remove(followPath.size()-2);
         followPath.remove(followPath.size()-2);
      } else if(lastIndent - indent >0) followPath = followPath.subList(0, followPath.size()-2);
      return followPath;
   }

   public static Map<String, String> getInlineComments(InputStream file) {
      if(file == null) return new HashMap<>();
      try {
         String currentLine;
         if(file.markSupported())
            file.reset();
         BufferedReader reader = new BufferedReader(new InputStreamReader(file));
         HashMap<String, String> map = new HashMap<>();
         String path;
         String comment = "";
         int lastIndent = -1;
         Pattern pattern = Pattern.compile("(\\s*)([^:]+):(?:.*)?(#.+)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
         Pattern pattern2 = Pattern.compile("(\\s*)([^:]+)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

         List<String> followPath = new ArrayList<>();
         while((currentLine = reader.readLine()) != null) {
            Matcher matcher = pattern.matcher(currentLine);
            Matcher m2 = pattern2.matcher(currentLine);
            int indent = 0;
            if(currentLine.replaceAll("\\s","").startsWith("#"))continue;
            if(matcher.find()) {
               indent = matcher.group(1) == null ? 0 : matcher.group(1).length();
               currentLine = matcher.group(2);
               if(matcher.group(3) != null)
                  comment = matcher.group(3);
            }else if(m2.find()) {
               indent = m2.group(1) == null ? 0 : m2.group(1).length();
               currentLine = m2.group(2);
            }
            followPath = createPath(followPath, currentLine, indent, lastIndent);
            path = String.join(".",followPath).replace("'","");
            if(comment.length() > 1) {
               map.put(path, comment);
               comment = "";
            }
            lastIndent = indent;
         }
         return map;
      } catch (IOException e) {
         e.printStackTrace();
         return new HashMap<>();
      }
   }

   public static InputStream getContent(File file) {
      if(file == null || !file.exists()) return null;
      try {
         String currentLine;

         StringBuilder whole = new StringBuilder();
         BufferedReader reader = new BufferedReader(new FileReader(file));

         while((currentLine = reader.readLine()) != null) {
            whole.append(currentLine).append("\n");
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

   public static Set<DynamicConfigurationDirectory> getConfigurationDirectories() {
      return CONFIGURATION_DIRECTORIES;
   }

   public static Set<IDynamicConfiguration> getConfigurations() {
      return CONFIGURATIONS;
   }
}
