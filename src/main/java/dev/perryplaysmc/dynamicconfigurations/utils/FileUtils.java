package dev.perryplaysmc.dynamicconfigurations.utils;

import dev.perryplaysmc.dynamicconfigurations.IDynamicConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creator: PerryPlaysMC
 * Created: 03/2022
 **/
public class FileUtils {

  private static final HashMap<String, String> INPUTSTREAM_CACHE = new HashMap<>();

  /**
   * @group 1 = Spacing
   * @group 2 = Value
   */
  private static final Pattern LIST_VALUE_DETECTOR = Pattern.compile("(\\s*)-\\s*([\\\"']?((?:(?=\\\\\\\")..|(?!\\\").)*)[\\\"']?)? *#?",
    Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  /**
   * @group 1 = Spacing
   * @group 2 = Key
   */
  private static final Pattern COMMENT_PATH_DETECTOR = Pattern.compile("(\\s*)([^:]+):",
    Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  /**
   * @group 1 = Spacing
   * @group 2 = Key
   * @group 3 = Comment
   */
  private static final Pattern INLINE_COMMENT_DETECTOR = Pattern.compile("(\\s*)([^:]+):(?:.*)?(#.+)",
    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
  /**
   * @group 1 = Spacing
   * @group 2 = Key
   * @group 3 = Spacing
   * @group 4 = Value
   */
  private static final Pattern LINE_PATH_VALUE_DETECTOR = Pattern.compile("(\\s*)([^:]+):(\\s*)([\"']?.+[\"']?)?",
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
      if(out == null) return;
      FileOutputStream outputStream = new FileOutputStream(toPrint);
      outputStream.write(String.join("\n", out).getBytes());
      outputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String generateNewConfigString(IDynamicConfiguration configuration, DynamicConfigurationOptions options, Map<String, String> comments, Map<String, String> inlineComments) {
    String configString = configuration.saveToString();
    List<String> pathList = new ArrayList<>();
    Pattern pattern = Pattern.compile("([^\\s:]+:)", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    List<String> lines = getLines(configString,(string)-> pattern.matcher(string).find(),(string)->!string.replaceAll("\\s","").startsWith("#"));
    StringBuilder builder = new StringBuilder();
    int lastIndent = -1, indents = options.indent();
    int currentIndent, start, end, initialStart;
    String indentString, path;
    StringBuilder configValue;
    Character wrapWith = options.stringWrap().wrapWith();
    String defaultIndentLength = options.indentString();
    for(String currentLine : lines) {
      currentIndent = 0;
      initialStart = -1;
      start = -1;
      end = -1;
      indentString = "";
      configValue = new StringBuilder();
      if(currentLine.isEmpty() || currentLine.replaceAll("\\s", "").startsWith("#") || currentLine.replaceAll("\\s", "").isEmpty()) continue;
      StringBuilder originalLine = new StringBuilder(currentLine);
      Matcher matcher = LINE_PATH_VALUE_DETECTOR.matcher(currentLine);
      if(matcher.find()) {
        currentIndent = (indentString = matcher.group(1)).length();
        currentLine = matcher.group(2);
        if(matcher.group(3)!=null)
          initialStart = matcher.start(3);
        if(matcher.group(4) != null) {
          configValue = new StringBuilder(matcher.group(4));
          start = matcher.start(4);
          end = matcher.end(4);
        }
      }
      pathList = createPath(pathList, currentLine, currentIndent, lastIndent, indents);
      path = String.join(".", pathList).replace("'", "");
      if(wrapWith != null)
        if(configuration.get(path) instanceof String && configValue.length()>0 && end > -1 && start > -1) {
          StringBuilder editedValue = new StringBuilder(configValue);
          wrapText(wrapWith, editedValue);
          originalLine.replace(start, end, editedValue.toString());
        }
        else if(configuration.getList(path, null) != null) {
          List<String> listLines = getLines(configValue.toString(),(string)->string.replaceAll("\\s","").startsWith("-"),(string)->!string.replaceAll(
            "\\s","").startsWith("#"));
          configValue = new StringBuilder();
          for(String lineValue : listLines) {
            if(lineValue.replaceAll("\\s","").startsWith("#")) continue;
            Matcher match = LIST_VALUE_DETECTOR.matcher(lineValue);
            if(!match.find())continue;
            StringBuilder value = new StringBuilder(match.group(2));
            wrapText(wrapWith, value);
            if(value.charAt(0) != '-') value.insert(0, "- ");
            configValue.append(defaultIndentLength).append(match.group(1)).append(value).append("\n");
          }
          configValue.setLength(configValue.length()-1);
          originalLine.replace(start,end,""+configValue);
        }
        else if(configuration.get(path, null) instanceof String[]) {
          String[] str = (String[])configuration.get(path, null);
          configValue = new StringBuilder();
          for(String lineValue : str) {
            if(lineValue.replaceAll("\\s","").startsWith("#")) continue;
            StringBuilder value = new StringBuilder(lineValue);
            wrapText(wrapWith, value);
            configValue.append(value).append(", ");
          }
          if(configValue.length()>0) configValue.setLength(configValue.length()-2);
          configValue.insert(0," [").append(']');
          if(initialStart!=-1)
            originalLine.replace(initialStart,end,""+configValue);
          else originalLine.replace(start,end, ""+configValue);
        }
      if(!builder.toString().isEmpty())
        builder.append("\n");
      if(inlineComments.containsKey(path) && inlineComments.get(path).length() > 1) {
        int in = originalLine.indexOf("\n");
        if(in != -1) {
          String sub = originalLine.substring(0, in);
          builder.append(sub).append(" ").append(inlineComments.get(path)).append(originalLine.substring(in));
        } else
          builder.append(originalLine).append(" ").append(inlineComments.get(path));
      } else if(comments.containsKey(path) && comments.get(path).length() > 1)
        builder.append(indentString).append(comments.get(path)).append("\n").append(originalLine);
      else builder.append(originalLine);
      lastIndent = currentIndent;
    }
    return builder.toString();
  }

  private static void wrapText(Character wrapWith, StringBuilder value) {
    if(wrapWith==null)return;
    if(value.charAt(0) != wrapWith && StringWrap.isValid(value.charAt(0))) value.replace(0, 1, "");
    if(value.charAt(0) != wrapWith) value.insert(0, wrapWith);
    if(value.charAt(value.length() - 1) != wrapWith && StringWrap.isValid(value.charAt(value.length() - 1)))
      value.setLength(value.length()-1);
    if(value.charAt(value.length() - 1) != wrapWith) value.append(wrapWith);
    if(wrapWith == StringWrap.SINGLE_QUOTED.wrapWith())
      value.replace(1,value.length()-1, value.substring(1,value.length()-1).replaceAll(("'(')"),("$1")).replace(("'"), ("''")));
  }

  private static List<String> getLines(String text, Predicate<String> resetCondition, Predicate<String> concatCondition) {
    List<String> lines = new ArrayList<>();
    StringBuilder toAdd = new StringBuilder();
    for(String s : text.split("\n")) {
      if(resetCondition != null && resetCondition.test(s)) {
        if(!toAdd.toString().equals("")) lines.add(toAdd.toString());
        toAdd = new StringBuilder(s);
      }else {
        if(concatCondition!=null&&concatCondition.test(s)) toAdd.append("\n").append(s);
        else {
          lines.add(toAdd.toString());
          lines.add(s);
          toAdd = new StringBuilder();
        }
      }
    }
    if(!toAdd.toString().equals(""))lines.add(toAdd.toString());
    return lines;
  }


  public static List<String> findKeys(InputStream file) {
    if(file == null) return new LinkedList<>();
    try {
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
        } else continue;
        if(indents == 0 && currentIndent > 0) indents = currentIndent;
        if(currentLine.isEmpty() || currentLine.startsWith("#") || currentLine.replaceAll("\\s", "").startsWith("#")
          || currentLine.replaceAll("\\s", "").startsWith("-")) continue;
        pathList = createPath(pathList, currentLine, currentIndent, lastIndent, indents);
        path = String.join(".", pathList).replace("'", "");
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
      String currentLine, path;
      StringBuilder comment = new StringBuilder();
      HashMap<String, String> map = new HashMap<>();
      BufferedReader reader = new BufferedReader(new InputStreamReader(file));
      int lastIndent = -1, indents = 0;
      List<String> pathList = new ArrayList<>();
      while((currentLine = reader.readLine()) != null) {
        if(currentLine.isEmpty() || currentLine.replaceAll("\\s", "").isEmpty()) continue;
        if(currentLine.replaceAll("\\s", "").startsWith("#")) {
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
        path = String.join(".", pathList).replace("'", "");
        if(comment.length() > 1) {
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
        if(currentLine.replaceAll("\\s", "").startsWith("#")) continue;
        if(inlineMatcher.find()) {
          currentIndent = inlineMatcher.group(1) == null ? 0 : inlineMatcher.group(1).length();
          currentLine = inlineMatcher.group(2);
          if(inlineMatcher.group(3) != null)
            comment = inlineMatcher.group(3);
        } else if(defaultPathMatcher.find()) {
          currentIndent = defaultPathMatcher.group(1) == null ? 0 : defaultPathMatcher.group(1).length();
          currentLine = defaultPathMatcher.group(2);
        }
        if(indents == 0 && currentIndent > 0) indents = currentIndent;
        pathList = createPath(pathList, currentLine, currentIndent, lastIndent, indents);
        path = String.join(".", pathList).replace("'", "");
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
    if(plugin==null)return null;
    String filePath = file.getPath();
    if(INPUTSTREAM_CACHE.containsKey(filePath)) return plugin.getResource(INPUTSTREAM_CACHE.get(filePath));
    if(plugin.getResource(file.getName()) != null) {
      INPUTSTREAM_CACHE.put(filePath, file.getName());
      return plugin.getResource(file.getName());
    }
    if(plugin.getResource(filePath) != null) {
      INPUTSTREAM_CACHE.put(filePath, filePath);
      return plugin.getResource(filePath);
    }
    String[] split = filePath.split(File.separator);
    int subStringIndex = 0;
    for(int i = split.length - 1; i > 0; i--) {
      String path = split[i];
      String fullFilePath = filePath.substring(subStringIndex);
      if(plugin.getResource(fullFilePath) != null) {
        INPUTSTREAM_CACHE.put(filePath, fullFilePath);
        return plugin.getResource(fullFilePath);
      }
      subStringIndex += path.length();
    }
    return null;
  }

  private static List<String> createPath(List<String> followPath, String currentLine, int indent, int lastIndent, int indentSize) {
    if(lastIndent == indent) followPath.remove(followPath.size() - 1);
    else if(indent == 0 && lastIndent > 0) followPath.clear();
    else if(indent - lastIndent < 0 && !followPath.isEmpty()) {
      indent = indent / indentSize;
      followPath = followPath.subList(0, indent);
    }
    followPath.add(currentLine);
    return followPath;
  }

}
