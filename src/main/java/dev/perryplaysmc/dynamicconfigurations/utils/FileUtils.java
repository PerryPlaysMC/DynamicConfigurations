package dev.perryplaysmc.dynamicconfigurations.utils;

import dev.perryplaysmc.dynamicconfigurations.IDynamicConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creator: PerryPlaysMC
 * Created: 03/2022
 **/
public class FileUtils {

   private static final HashMap<String, String> INPUTSTREAM_CACHE = new HashMap<>();

   /**
    * Group 1 = Spacing
    * Group 2 = Value
    */
   private static final Pattern LIST_VALUE_DETECTOR = Pattern.compile("(\\s*)-\\s*([\"']?.+[\"']?)?\\s*#?",
      Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
   /**
    * Group 1 = Spacing
    * Group 2 = Key
    */
   private static final Pattern COMMENT_PATH_DETECTOR = Pattern.compile("(\\s*)([^:]+):",
      Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
   /**
    * Group 1 = Spacing
    * Group 2 = Key
    * Group 3 = Comment
    */
   private static final Pattern INLINE_COMMENT_DETECTOR = Pattern.compile("(\\s*)([^:]+):(?:.*)?(#.+)",
      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
   /**
    * Group 1 = Spacing
    * Group 2 = Key
    * Group 3 = Value
    */
   private static final Pattern LINE_PATH_VALUE_DETECTOR = Pattern.compile("(\\s*)([^:]+):\\s*([\"']?.+[\"']?)?",
      Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

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

   public static String pasteConfig(IDynamicConfiguration configuration, DynamicConfigurationOptions options, Map<String, String> comments, Map<String, String> inlineComments) {
      String configString = configuration.saveToString();
      List<String> pathList = new ArrayList<>();
      String[] lines = configString.split("\n");
      StringBuilder builder = new StringBuilder();
      int lastIndent = -1, indents = options.indent();
      int currentIndent, start, end;
      String indentString, configValue, path;
      Character wrapWith = options.stringWrap().wrapWith();
      for(int index = 0; index < lines.length; index++) {
         String currentLine = lines[index];
         currentIndent = 0; start = -1; end = -1;
         indentString = ""; configValue = "";
         if(currentLine.isEmpty()||currentLine.replaceAll("\\s","").startsWith("#")||currentLine.replaceAll("\\s","").isEmpty())continue;
         StringBuilder originalLine = new StringBuilder(currentLine);
         Matcher matcher = LINE_PATH_VALUE_DETECTOR.matcher(currentLine);
         if(matcher.find()) {
            currentIndent = (indentString = matcher.group(1)).length();
            currentLine = matcher.group(2);
            if(matcher.group(3) != null) {
               configValue = matcher.group(3);
               start = matcher.start(3);
               end = matcher.end(3);
            }
         }
         pathList = createPath(pathList,currentLine,currentIndent,lastIndent, indents);
         path = String.join(".", pathList).replace("'", "");
         if(wrapWith != null)
            if(configuration.get(path) instanceof String && !configValue.isEmpty() && end > -1 && start > -1) {
               StringBuilder editedValue = new StringBuilder(configValue);
               if(configValue.startsWith(wrapWith+"") && !configValue.endsWith(wrapWith+""))
                  index = appendTextToLine(lines, index, indentString, editedValue, wrapWith);
               if(editedValue.charAt(0) != wrapWith)editedValue.insert(0, wrapWith);
               if(editedValue.charAt(editedValue.length()-1) != wrapWith) editedValue.append(wrapWith);
               originalLine.replace(start,end,editedValue.toString());
            }
            else if(configuration.getListString(path, null) != null) {
               int nextIndex = index+1;
               String nextLine;
               while(lines.length > nextIndex && (nextLine = lines[nextIndex]).startsWith(indentString + "-")) {
                  Matcher matcher1 = LIST_VALUE_DETECTOR.matcher(nextLine);
                  if(matcher1.find()) {
                     StringBuilder newIndentLength = new StringBuilder(matcher1.group(1));
                     for(int i = 0; i < indents; i++) newIndentLength.append(" ");
                     StringBuilder value = new StringBuilder(matcher1.group(2));
                     if(value.charAt(0) == wrapWith && value.charAt(value.length()-1) != wrapWith)
                        index = appendTextToLine(lines, nextIndex, indentString, value, wrapWith);
                     if(value.charAt(0) != '-')value.insert(0, "- ");
                     if(!value.substring(1).startsWith(wrapWith+""))value.insert(2, wrapWith);
                     if(value.charAt(value.length()-1) !=wrapWith) value.append(wrapWith);
                     newIndentLength = new StringBuilder((currentIndent < newIndentLength.length() ? newIndentLength.toString() : indentString));
                     originalLine.append("\n").append(newIndentLength).append(value);
                  }else
                     originalLine.append("\n").append(nextLine, 0, (indentString + "-").length()).append("- ").append(wrapWith)
                        .append(nextLine.substring((indentString + "-").length())).append(wrapWith);
                  nextIndex++;
               }
               if(nextIndex -1 != index) index = nextIndex-1;
            }
         if(!builder.toString().isEmpty())
            builder.append("\n");
         if(inlineComments.containsKey(path) && inlineComments.get(path).length() > 1) {
            int in = originalLine.indexOf("\n");
            if(in!=-1) {
               String sub = originalLine.substring(0,in);
               builder.append(sub).append(" ").append(inlineComments.get(path));
               builder.append(originalLine.substring(in));
            }else
               builder.append(originalLine).append(" ").append(inlineComments.get(path));
         }else if(comments.containsKey(path) && comments.get(path).length() > 1)
            builder.append(indentString).append(comments.get(path)).append("\n").append(originalLine);
         else builder.append(originalLine);
         lastIndent = currentIndent;
      }
      return builder.toString();
   }


   public static List<String> findKeys(InputStream file) {
      if(file == null) return new LinkedList<>();
      try {
         if(file.markSupported()) file.reset();
         String currentLine;
         BufferedReader reader = new BufferedReader(new InputStreamReader(file));
         List<String> keys = new LinkedList<>();
         String path;
         int lastIndent = -1, indents = 0;
         List<String> pathList = new ArrayList<>();
         while((currentLine = reader.readLine()) != null) {
            Matcher pathMatcher = COMMENT_PATH_DETECTOR.matcher(currentLine);
            int currentIndent;
            if(pathMatcher.find()) {
               currentIndent = pathMatcher.group(1).length();
               currentLine = pathMatcher.group(2);
            }else continue;
            if(indents == 0 && currentIndent > 0) indents = currentIndent;
            if(currentLine.isEmpty()||currentLine.startsWith("#")||currentLine.replaceAll("\\s","").startsWith("#")
               ||currentLine.replaceAll("\\s","").startsWith("-"))continue;
            pathList = createPath(pathList, currentLine, currentIndent, lastIndent, indents);
            path = String.join(".",pathList).replace("'","");
            keys.add(path);
            lastIndent = currentIndent;
         }
         return keys;
      } catch (IOException e) {
         e.printStackTrace();
         return new LinkedList<>();
      }
   }


   public static Map<String, String> findComments(InputStream file) {
      if(file == null) return new HashMap<>();
      try {
         if(file.markSupported()) file.reset();
         String currentLine, path;
         StringBuilder comment = new StringBuilder();
         HashMap<String, String> map = new HashMap<>();
         BufferedReader reader = new BufferedReader(new InputStreamReader(file));
         int lastIndent = -1, indents = 0;
         List<String> pathList = new ArrayList<>();
         while((currentLine = reader.readLine()) != null) {
            if(currentLine.isEmpty()||currentLine.replaceAll("\\s","").isEmpty())continue;
            if(currentLine.replaceAll("\\s","").startsWith("#")) {
               comment.append((comment.length() == 0) ? "" : "\n").append(currentLine.substring(currentLine.indexOf('#')));
               continue;
            }
            int currentIndent = 0;
            Matcher matcher = COMMENT_PATH_DETECTOR.matcher(currentLine);
            if(matcher.find()) {
               currentIndent = matcher.group(1).length();
               currentLine = matcher.group(2);
            }

            if(indents == 0 && currentIndent > 0) indents = currentIndent;
            pathList = createPath(pathList, currentLine, currentIndent, lastIndent, indents);
            path = String.join(".",pathList).replace("'","");
            if(comment.length()>1) {
               map.put(path, comment.toString());
               comment = new StringBuilder();
            }
            lastIndent = currentIndent;
         }
         return map;
      } catch (IOException e) {
         e.printStackTrace();
         return new HashMap<>();
      }
   }

   public static Map<String, String> findInlineComments(InputStream file) {
      if(file == null) return new HashMap<>();
      try {
         if(file.markSupported()) file.reset();
         String currentLine, path;
         BufferedReader reader = new BufferedReader(new InputStreamReader(file));
         HashMap<String, String> map = new HashMap<>();
         String comment = "";
         int lastIndent = -1, indents = 0;

         List<String> pathList = new ArrayList<>();
         while((currentLine = reader.readLine()) != null) {
            Matcher inlineMatcher = INLINE_COMMENT_DETECTOR.matcher(currentLine);
            Matcher defaultPathMatcher = COMMENT_PATH_DETECTOR.matcher(currentLine);
            int currentIndent = 0;
            if(currentLine.replaceAll("\\s","").startsWith("#"))continue;
            if(inlineMatcher.find()) {
               currentIndent = inlineMatcher.group(1) == null ? 0 : inlineMatcher.group(1).length();
               currentLine = inlineMatcher.group(2);
               if(inlineMatcher.group(3) != null)
                  comment = inlineMatcher.group(3);
            }else if(defaultPathMatcher.find()) {
               currentIndent = defaultPathMatcher.group(1) == null ? 0 : defaultPathMatcher.group(1).length();
               currentLine = defaultPathMatcher.group(2);
            }
            if(indents == 0 && currentIndent > 0) indents = currentIndent;
            pathList = createPath(pathList, currentLine, currentIndent, lastIndent, indents);
            path = String.join(".",pathList).replace("'","");
            if(comment.length() > 1) {
               map.put(path, comment);
               comment = "";
            }
            lastIndent = currentIndent;
         }
         return map;
      } catch (IOException e) {
         e.printStackTrace();
         return new HashMap<>();
      }
   }

   public static InputStream findStream(JavaPlugin plugin, File file) {
      String filePath = file.getPath();
      if(INPUTSTREAM_CACHE.containsKey(filePath)) return plugin.getResource(INPUTSTREAM_CACHE.get(filePath));
      if(plugin.getResource(file.getName()) != null) {
         INPUTSTREAM_CACHE.put(filePath,file.getName());
         return plugin.getResource(file.getName());
      }
      if(plugin.getResource(filePath) != null) {
         INPUTSTREAM_CACHE.put(filePath,filePath);
         return plugin.getResource(filePath);
      }
      String[] split = filePath.split(File.separator);
      int subStringIndex = 0;
      for(int i = split.length-1; i > 0; i--) {
         String path = split[i];
         String fullFilePath = filePath.substring(subStringIndex);
         if(plugin.getResource(fullFilePath) != null) {
            INPUTSTREAM_CACHE.put(filePath, fullFilePath);
            return plugin.getResource(fullFilePath);
         }
         subStringIndex+=path.length();
      }
      return null;
   }

   private static int appendTextToLine(String[] split, int i, String indentStr, StringBuilder value, Character wrapWith) {
      int nextIndex = i + 1;
      StringBuilder nextLine;
      while(split.length > nextIndex && (nextLine = new StringBuilder(split[nextIndex])).toString().startsWith(indentStr)) {
         value.append(nextLine.substring(indentStr.length()));
         if(nextLine.toString().endsWith(wrapWith+"")) break;
         nextIndex++;
      }
      if(nextIndex-1 != i) i = nextIndex;
      return i;
   }

   private static List<String> createPath(List<String> followPath, String currentLine, int indent, int lastIndent, int indentSize) {
      if(lastIndent == indent) followPath.remove(followPath.size()-1);
      else if(indent == 0 && lastIndent > 0) followPath.clear();
      else if(indent - lastIndent < 0 && !followPath.isEmpty()) {
         indent = indent/indentSize;
         followPath = followPath.subList(0, indent);
      }
      followPath.add(currentLine);
      return followPath;
   }

}
