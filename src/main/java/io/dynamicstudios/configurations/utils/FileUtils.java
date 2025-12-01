package io.dynamicstudios.configurations.utils;

import io.dynamicstudios.configurations.IDynamicConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
 // private static final Pattern FULL_CONFIG_PARSER = Pattern.compile("^(?<topComment>(?:(?:\\s*)(?:#[^\\n]+))+\\n)?^(?<indent>\\s*)(?<path>(?:(?=\\\")(?:\\\"(?:(?:(?=\\\\\\\")..|(?!\\\").)+)\\\"))|(?:(?=')(?:'(?:(?:(?='')..|(?!').)+)'))|(?:(?:(?<!['\\\"])[^:\\n])+)):(?:[\\t ]+(?:(?<=[^#])(?<value>(?:(?:(?=\\\")(?:\\\"(?:.(?:(?=\\\\\\\")..|(?!\\\").)+)?\\\"))|(?:(?=')(?:'(?:(?:(?=''|\\\\')..|(?=''\\n)..\\n|(?=\\n)\\n|(?!').)+)?'))|(?:(?=\\[)\\[(?:(?:(?:(?=\\\")(?:\\\"(?:(?:(?=\\\\\\\")..|(?!\\\").)+)\\\"))|(?:(?=')(?:'(?:(?:(?=''|\\\\')..|(?!').)+)'))|[^],]+),?)+\\])|(?=[^#'\\\"])[^\\n#]+)))|(?:(?<list>(?:(?:\\n?(?:^\\s*-(?:((?=\\\")(?:\\\"(?:.(?:(?=\\\\\\\")..|(?!\\\").)+)?\\\"))|((?=')(?:'(?:(?:(?='')..|(?!').)+)'))|(?:(?=\\[)\\[(?:(?:(?:(?=\\\")(?:\\\"(?:(?:(?=\\\\\\\")..|(?!\\\").)+)?\\\"))|(?:(?=')(?:'(?:(?:(?='')..|(?!').)+)'))|[^],]+),?)+\\])|[^\\n]+)$))+))))?(?<inlineComment>([\\t ]+)?(?:#[^\\n]*))?[\\t ]*", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
 private static final Pattern FULL_CONFIG_PARSER = Pattern.compile("(?<emptyLine>(?:^\\s*$\\n)+)|^(?<topComment>(?:(?:\\s*)(?:#[^\\n]+))+\\n)?(?<indent>(?:(?!\\n)\\s)*)(?!#)(?<path>(?:(?=\\\")(?:\\\"(?:(?:(?=\\\\\\\")..|(?!\\\").)+)\\\"))|(?:(?=')(?:'(?:(?:(?='')..|(?!').)+)'))|(?:(?:(?<!['\\\"])[^:\\n])+)):[\\t ]*(?:(?:(?<=[^#])(?<value>(?:(?:(?=\\\")(?:\\\"(?:.(?:(?=\\\\\\\")..|(?!\\\").)+)?\\\"))|(?:(?=')(?:'(?:(?:(?=''|\\\\')..|(?=''\\n)..\\n|(?=\\n)\\n|(?!').)+)?'))|(?:(?=\\[)\\[(?:(?:(?:(?=\\\")(?:\\\"(?:(?:(?=\\\\\\\")..|(?!\\\").)+)\\\"))|(?:(?=')(?:'(?:(?:(?=''|\\\\')..|(?!').)+)'))|[^],]+),?)+\\])|(?=[^#'\\\"])[^\\n#]+)))|(?:[ \\t]*\\n?(?<list>(?:(?=\\[)\\[(?:(?:(?:(?=\\\")(?:\\\"(?:(?:(?=\\\\\\\")..|(?!\\\").)+)?\\\"))|(?:(?=')(?:'(?:(?:(?='')..|(?!').)+)'))|[^],]+),?)+\\])|(?:(?:\\n?(?:^\\s*-(?:((?=\\\")(?:\\\"(?:.(?:(?=\\\\\\\")..|(?!\\\").)+)?\\\"))|((?=')(?:'(?:(?:(?='')..|(?!').)+)'))|[^\\n]+)$))+))))?(?<inlineComment>([\\t ]+)?(?:#[^\\n]*))?[\\t ]*|(?<comment>(?:^\\s*#\\s*[^\\n]+\\n?)+)", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
 private static final Pattern LINE_VALUE_INLINE_COMMENT_DETECTOR = Pattern.compile("^((?<indent>[\\t ]*)(?:(?<path>(?:\\\"(?:(?:(?=\\\\\\\")..|(?!\\\\\\\").)+)\\\")|(?:'(?:(?:(?='')..|(?!').)+)')|[^:\\n]+):))(?:(?<=[^#])(?<value>(?:(?:(?=\\\")(?:\\\"(?:.(?:(?=\\\\\\\")..|(?!\\\").)+)\\\"))|(?:(?=')(?:'(?:(?:(?='')..|(?!').)+)'))|(?:(?=\\[)\\[(?:(?:(?:(?=\\\")(?:\\\"(?:(?:(?=\\\\\\\")..|(?!\\\").)+)\\\"))|(?:(?=')(?:'(?:(?:(?='')..|(?!').)+)'))|[^],]+),?)+\\])|(?=[^#'\\\"])[^\\n#]+)))?(?<comment>([\\t ]+)?(?:#[^\\n]*))?\\s*$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

 /**
	* @group 1 = Spacing
	* @group 2 = Value
	*/
 private static final Pattern LIST_LINE_COMMENT_DETECTOR = Pattern.compile("",
		Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
 /**
	* @group 1 = Spacing
	* @group 2 = Key
	*/
 private static final Pattern LINE_VALUE_COMMENT_DETECTOR = Pattern.compile("^(?<comment>(?:(?:\\s*)(?:#[^\\n]+))+\\n)^(?<indent>\\s*)(?<path>\\S(?:(?<=\\\")(?:(?:(?=\\\\\\\")..|(?!\\\").)+)\\\")|(?:(?<=')(?:(?:(?='')..|(?!').)+)')|(?<!['\\\"])[^:\\n]+):",//"(\\s*)((?:(?!\\n)[^#:])+):",
		Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
 /**
	* @group 1 = Spacing
	* @group 2 = Key
	* @group 4 = Comment
	*/
 private static final Pattern INLINE_COMMENT_DETECTOR = Pattern.compile("(\\s+)?([^:]+):\\s*(?:(?:(?<a>['\\\"]).*(?:\\k<a>))|[^#]+)?(\\s*#.+)?",
		Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
 /**
	* @group 1 = Spacing
	* @group 2 = Key
	* @group 3 = Spacing
	* @group 4 = Value
	*/
 private static final Pattern LINE_PATH_VALUE_DETECTOR = Pattern.compile("^(\\s*)([^:]+):(\\s*)([\"']?.+[\"']?)?",
		Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

 public static List<String> readInputStream(InputStream inputStream) {
	try {
	 if(inputStream == null) return new ArrayList<>();
	 Scanner scanner = new Scanner(inputStream);
	 StringBuilder lines = new StringBuilder();
	 while(scanner.hasNextLine()) lines.append("\n").append(scanner.nextLine());
	 if(lines.toString().replaceAll("\\s", "").isEmpty()) return new ArrayList<>();
	 return Arrays.asList(lines.substring(1).split("\n"));
	} catch(Exception e) {
	 e.printStackTrace();
	}
	return new ArrayList<>();
 }

 public static void writeFile(InputStream is, File toPrint) {
	try {
	 FileOutputStream outputStream = new FileOutputStream(toPrint);
	 String join = String.join("\n", readInputStream(is));
	 outputStream.write(join.getBytes());
	 outputStream.close();

	} catch(IOException e) {
	 e.printStackTrace();
	}

 }

 public static void writeFile(List<String> out, File toPrint) {
	try {
	 if(out == null) return;
	 FileOutputStream outputStream = new FileOutputStream(toPrint);
	 outputStream.write(String.join("\n", out).getBytes());
	 outputStream.close();
	} catch(IOException e) {
	 e.printStackTrace();
	}
 }

 public static String generateNewConfigString(IDynamicConfiguration configuration, DynamicConfigurationOptions<?> options, Map<String, String> comments, Map<String, String> inlineComments) {
	String configString = configuration.saveToString();
	Matcher matcher = FULL_CONFIG_PARSER.matcher(configString);
	List<String> pathList = new ArrayList<>();
	StringBuilder builder = new StringBuilder();
	int lastIndent = -1, indents = options.indent();
	int currentIndent;
	String indentString, path;
	StringBuilder configValue = new StringBuilder();
	Character wrapWith = options.stringWrap().wrapWith();
	String defaultIndentLength = options.indentString();
	while(matcher.find()) {
	 String indent = (matcher.group("indent") == null ? "" : matcher.group("indent")).replace("\n", "");
	 String confValue = (matcher.group("value") == null ?
			(matcher.group("list") == null ? "" : matcher.group("list"))
			: matcher.group("value")
			.replace("\n" + indent + " ", "")
			.replace("\n" + indent, ""));
	 String rPath = matcher.group("path");
	 if(rPath == null) continue;
	 try {
		String currentLine = indent + rPath + ": " + confValue;
		currentIndent = (indentString = indent).length();
		pathList = createPath(pathList, rPath, currentIndent, lastIndent, indents);
		path = String.join(".", pathList).replaceFirst("'(.+)'", "$1");
		StringBuilder originalLine = new StringBuilder(currentLine);
		configValue.append(confValue);
		append(configuration, comments, inlineComments, wrapWith, path, configValue, indentString, originalLine, indent, rPath, defaultIndentLength, builder);
		configValue.setLength(0);
		if(comments.containsKey("\0" + path)) {
		 builder.append(comments.get("\0" + path));
		}
		if(comments.containsKey("\n" + path)) {
		 builder.append("\n" + comments.get("\n" + path));
		}
		if(comments.containsKey("\1" + path)) {
		 builder.append(comments.get("\1" + path));
		}
	 lastIndent = currentIndent;
	 }catch(Exception e){
		e.printStackTrace();
	 }
	}
	return builder.toString();
 }

 private static StringBuilder append(IDynamicConfiguration configuration, Map<String, String> comments, Map<String, String> inlineComments, Character wrapWith, String path, StringBuilder configValue, String indentString, StringBuilder originalLine, String indent, String rPath, String defaultIndentLength, StringBuilder builder) {
	if(wrapWith != null) {
	 if(configuration.get(path) instanceof String && configValue.length() > 0) {
		StringBuilder editedValue = new StringBuilder(configValue);
		wrapText(wrapWith, indentString, editedValue);
		originalLine.setLength(0);
		originalLine.append(indent).append(rPath).append(": ").append(editedValue);
	 }
	 else if(configuration.getList(path, configuration.options().defaults() == null ? null : configuration.options().defaults().getList(path, null)) != null
			&& configuration.get(path, configuration.options().defaults() == null ? null : configuration.options().defaults().get(path, null)) instanceof List) {
		Class<?> listType = configuration.getList(path, configuration.options().defaults() == null ? null : configuration.options().defaults().getList(path, null)).stream().filter(Objects::nonNull).findFirst()
			 .map(Object::getClass).orElse(null);
		if(listType == null) listType = String.class;
		List<String> listLines = getLines(configValue.toString(), (string) -> string.replaceAll("\\s+", "").startsWith("-"), (string) -> !string.replaceAll(
			 "\\s", "").startsWith("#"));
		configValue.setLength(0);
		boolean found = false;
		for(String lineValue : listLines) {
		 if(lineValue.replaceAll("\\s", "").startsWith("#")) continue;
		 Matcher match = LIST_LINE_COMMENT_DETECTOR.matcher(lineValue);
		 if(!match.find()) continue;
		 StringBuilder value = new StringBuilder(match.group("value"));
		 if(listType == String.class) {
			wrapText(wrapWith, indentString, value);
		 }
		 if(value.charAt(0) != '-') value.insert(0, "- ");
		 configValue.append(defaultIndentLength).append(indent).append(value).append("\n");
		 found = true;
		}
		if(configValue.length() - 1 > -1)
		 configValue.setLength(configValue.length() - 1);
		originalLine.setLength(0);
		originalLine.append(indent).append(rPath).append(": ").append(configValue.length() == 0 ? "" : "\n").append(configValue);
		if(listLines.isEmpty() || (listLines.size() == 1 && listLines.get(0).matches("\\s*\\[]")))
		 originalLine.append("[]");
		if(configValue.length() == 0 || !found) {
		 if(listType == String.class) {
			originalLine.append("- ").append(wrapWith).append(wrapWith);
		 } else originalLine.append("[]");
		}
	 } else if(configuration.get(path, null) instanceof String[]) {
		String[] str = (String[]) configuration.get(path, null);
		configValue.setLength(0);
		for(String lineValue : str) {
		 if(lineValue.replaceAll("\\s", "").startsWith("#")) continue;
		 StringBuilder value = new StringBuilder(lineValue);
		 wrapText(wrapWith, indentString, value);
		 configValue.append(value).append(", ");
		}
		if(configValue.length() - 2 > -1) configValue.setLength(configValue.length() - 2);
		configValue.insert(0, " [").append(']');
		if(str.length == 0 || (str.length == 1 && str[0].matches("\\s*\\[]"))) originalLine.append("[]");
		originalLine.setLength(0);
		originalLine.append(indent).append(rPath).append(": ").append(configValue);
	 }
	}
	String inlineC = inlineComments.getOrDefault(path, "");

	if(!builder.toString().isEmpty())
	 builder.append("\n");
	if(comments.containsKey(path) && comments.get(path).length() > 0) {
	 String[] split = comments.get(path).split("\n");
	 int stat = builder.length();
	 for(String comment : split) {
		builder.append(indentString).append(comment.matches("#.*") ? "" : "# ").append(comment);
		builder.append("\n");
	 }
	}

	if(inlineComments.containsKey(path) && inlineC.length() > 1) {
	 int in = originalLine.indexOf("\n");
	 if(in != -1) {
		String sub = originalLine.substring(0, in);
		builder.append(sub).append(sub.endsWith(" ") ? "" : " ").append(inlineC.matches("#.*") ? "" : "# ")
			 .append(inlineC).append(originalLine.substring(in));
	 } else
		builder.append(originalLine).append(originalLine.lastIndexOf(" ") == originalLine.length() - 1 ? "" : " ").append(inlineC.matches("#.*") ? "" : "# ").append(inlineC);
	 return configValue;
	}
	builder.append(originalLine);
	return configValue;
 }

 private static void wrapText(Character wrapWith, String indent, StringBuilder value) {
	if(wrapWith == null) return;
	if(value.charAt(0) != wrapWith && StringWrap.isValid(value.charAt(0))) value.replace(0, 1, wrapWith.toString());
	if(value.charAt(0) != wrapWith) value.insert(0, wrapWith);
	if(value.charAt(value.length() - 1) != wrapWith && StringWrap.isValid(value.charAt(value.length() - 1)))
	 value.setLength(value.length() - 1);
	if(value.charAt(value.length() - 1) != wrapWith) value.append(wrapWith);
	if(value.length() == 1 && value.charAt(0) == wrapWith) value.append(wrapWith);
	if(wrapWith == StringWrap.SINGLE_QUOTED.wrapWith())
	 value.replace(1, value.length() - 1, value.substring(1, value.length() - 1).replaceAll(("'(')"), ("$1")).replace(("'"), ("''")).replace("\n " + indent, ""));
	if(wrapWith == StringWrap.DOUBLE_QUOTED.wrapWith())
	 value.replace(1, value.length() - 1, value.substring(1, value.length() - 1).replaceAll(("'(')"), ("$1")).replace("\n " + indent, ""));
 }

 private static List<String> getLines(String text, Predicate<String> resetCondition, Predicate<String> concatCondition) {
	List<String> lines = new ArrayList<>();
	StringBuilder toAdd = new StringBuilder();
	for(String s : text.split("\n")) {
	 if(resetCondition != null && resetCondition.test(s)) {
		if(!toAdd.toString().equals("")) lines.add(toAdd.toString());
		toAdd = new StringBuilder(s);
	 } else {
		if(concatCondition != null && concatCondition.test(s)) toAdd.append("\n").append(s);
		else {
		 lines.add(toAdd.toString());
		 lines.add(s);
		 toAdd = new StringBuilder();
		}
	 }
	}
	if(!toAdd.toString().equals("")) lines.add(toAdd.toString());
	return lines;
 }


 public static List<String> findKeys(String content) {
	Matcher matcher = FULL_CONFIG_PARSER.matcher(content);
	List<String> keys = new ArrayList<>();
	List<String> pathList = new ArrayList<>();
	int currentIndent = 0;
	int lastIndent = 0;
	int indentSize = 0;

	while(matcher.find()) {
	 if(matcher.group("comment") != null) {
		if(!matcher.group("comment").isEmpty()) {
		 continue;
		}
	 }
	 if(matcher.group("emptyLine") != null) {
		if(!matcher.group("emptyLine").isEmpty()) {
		 continue;
		}
	 }

	 String path = matcher.group("path");
	 String indent = matcher.group("indent").replace("\n", "");
	 currentIndent = indent.length();
	 if(indentSize == 0) indentSize = currentIndent;
	 if(lastIndent - currentIndent > 0) {
		pathList = pathList.subList(0, currentIndent / indentSize);
	 } else if(lastIndent == currentIndent && !pathList.isEmpty()) pathList.remove(pathList.size() - 1);
	 pathList.add(path);
	 keys.add(String.join(".", pathList));
	 lastIndent = currentIndent;
	}
	return keys;
 }

 public static List<String> findKeys(InputStream inputStream) {
	return findKeys(String.join("\n", readInputStream(inputStream)));
 }

 public static Map<String, String>[] findAllComments(String content) {
	Map<String, String> inline = new HashMap<>();
	Map<String, String> top = new HashMap<>();
	Matcher matcher = FULL_CONFIG_PARSER.matcher(content);
	List<String> pathList = new ArrayList<>();
	int currentIndent = 0;
	int lastIndent = 0;
	int indentSize = 0;
	String previousKey = "";
	boolean before = true;
	while(matcher.find()) {
	 if(matcher.group("comment") != null) {
		if(!matcher.group("comment").isEmpty()) {
		 top.put("\n" + previousKey, matcher.group("comment"));
		 before = false;
		 continue;
		}
	 }
	 if(matcher.group("emptyLine") != null) {
		if(!matcher.group("emptyLine").isEmpty()) {
		 top.put((before ? "\0" : "\1") + previousKey, matcher.group("emptyLine"));
		 continue;
		}
	 }
	 before = true;
	 String topComment = matcher.group("topComment");
	 String inlineComment = matcher.group("inlineComment");
	 String path = matcher.group("path");
	 if((path.startsWith("'") && path.endsWith("'")) || (path.startsWith("\"") && path.endsWith("\"")))
		path = path.substring(1, path.length() - 1);
	 String indent = matcher.group("indent");
	 currentIndent = indent.length();
	 if(indentSize == 0) indentSize = currentIndent;
	 if(lastIndent - currentIndent > 0) {
		pathList = pathList.subList(0, currentIndent / indentSize);
	 } else if(lastIndent == currentIndent && !pathList.isEmpty()) pathList.remove(pathList.size() - 1);
	 pathList.add(path);
	 if(topComment != null && !topComment.isEmpty()) {
		top.put(String.join(".", pathList), topComment.replaceAll("[ \\t]*#\\s*", ""));
	 }
	 if(inlineComment != null && !inlineComment.isEmpty())
		inline.put(String.join(".", pathList), inlineComment.replaceAll("[ \\t]*#\\s*", ""));
	 lastIndent = currentIndent;
	 previousKey = String.join(".", pathList);
	}
	return new Map[]{top, inline};
 }

 public static Map<String, String>[] findAllComments(InputStream inputStream) {
	return findAllComments(String.join("\n", readInputStream(inputStream)));
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

 public static InputStream findStream(JavaPlugin plugin, File file) {
	if(plugin == null) return null;
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
	String sp = filePath.contains(File.separator) ? File.separator : "/";
	String[] split = filePath.split(Pattern.quote(sp));
	StringBuilder filePathLocator = new StringBuilder();
	for(int i = split.length - 1; i > 0; i--) {
	 filePathLocator.insert(0, split[i] + sp);
	 String locator = filePathLocator.toString();
	 if(plugin.getResource(locator) != null) {
		INPUTSTREAM_CACHE.put(filePath, locator);
		return plugin.getResource(locator);
	 }
	}
	return null;
 }
}