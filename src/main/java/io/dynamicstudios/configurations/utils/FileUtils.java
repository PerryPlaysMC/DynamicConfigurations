package io.dynamicstudios.configurations.utils;

import io.dynamicstudios.configurations.IDynamicConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
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
	private static final Pattern COMMENT_PATH_DETECTOR = Pattern.compile("(\\s*)((?:(?!\\n)[^#:])+):",
		 Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
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
	private static final Pattern LINE_PATH_VALUE_DETECTOR = Pattern.compile("(\\s*)([^:]+):(\\s*)([\"']?.+[\"']?)?",
		 Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	public static List<String> readInputStream(InputStream inputStream) {
		try {
			if(inputStream == null) return new ArrayList<>();
			Scanner scanner = new Scanner(inputStream);
			StringBuilder lines = new StringBuilder();
			while(scanner.hasNextLine()) lines.append("\n").append(scanner.nextLine());
			if(lines.length() < 0) return new ArrayList<>();
			return Arrays.asList(lines.substring(1).split("\n"));
		} catch (Exception e) {
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

	public static String generateNewConfigString(IDynamicConfiguration configuration, DynamicConfigurationOptions<?> options, Map<String, String> comments, Map<String, String> inlineComments) {
		String configString = configuration.saveToString();
		Logger.getLogger("DynamicStudios").log(Level.INFO, "Saving: {}", configString);
		List<String> pathList = new ArrayList<>();
		Pattern pattern = Pattern.compile("^\\s*-?\\s*([^\\s:]+:)", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		List<String> lines = getLines(configString, (string) -> pattern.matcher(string).find(), (string) -> !string.replaceAll("\\s", "").startsWith("#"));
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
			if(currentLine.isEmpty() || currentLine.replaceAll("\\s", "").startsWith("#") || currentLine.replaceAll("\\s", "").isEmpty())
				continue;
			StringBuilder originalLine = new StringBuilder(currentLine);
			Matcher matcher = LINE_PATH_VALUE_DETECTOR.matcher(currentLine);
			if(matcher.find()) {
				currentIndent = (indentString = matcher.group(1)).length();
				currentLine = matcher.group(2);
				if(matcher.group(3) != null)
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
				if(configuration.get(path) instanceof String && configValue.length() > 0 && end > -1 && start > -1) {
					StringBuilder editedValue = new StringBuilder(configValue);
					wrapText(wrapWith, editedValue);
					originalLine.replace(start, end, editedValue.toString());
				} else if(configuration.getList(path, null) != null && configuration.get(path, null) instanceof List) {
					Class<?> listType = configuration.getList(path, null).stream().filter(Objects::nonNull).findFirst()
						 .map(Object::getClass).orElse(null);
					if(listType == null) listType = String.class;
					List<String> listLines = getLines(configValue.toString(), (string) -> string.replaceAll("\\s", "").startsWith("-"), (string) -> !string.replaceAll(
						 "\\s", "").startsWith("#"));
					configValue = new StringBuilder();
					for(String lineValue : listLines) {
						if(lineValue.replaceAll("\\s", "").startsWith("#")) continue;
						Matcher match = LIST_VALUE_DETECTOR.matcher(lineValue);
						if(!match.find()) continue;
						StringBuilder value = new StringBuilder(match.group(2));
						if(listType == String.class) {
							wrapText(wrapWith, value);
						}
						if(value.charAt(0) != '-') value.insert(0, "- ");
						configValue.append(defaultIndentLength).append(match.group(1)).append(value).append("\n");
					}
					if(configValue.length() - 1 > -1)
						configValue.setLength(configValue.length() - 1);
					if(end > -1 && start > -1)
						originalLine.replace(start, end, configValue.toString());
				} else if(configuration.get(path, null) instanceof String[]) {
					String[] str = (String[]) configuration.get(path, null);
					configValue = new StringBuilder();
					for(String lineValue : str) {
						if(lineValue.replaceAll("\\s", "").startsWith("#")) continue;
						StringBuilder value = new StringBuilder(lineValue);
						wrapText(wrapWith, value);
						configValue.append(value).append(", ");
					}
					if(configValue.length() - 2 > -1) configValue.setLength(configValue.length() - 2);
					configValue.insert(0, " [").append(']');
					if(initialStart != -1)
						originalLine.replace(initialStart, end, "" + configValue);
					else originalLine.replace(start, end, "" + configValue);
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
		if(wrapWith == null) return;
		if(value.charAt(0) != wrapWith && StringWrap.isValid(value.charAt(0))) value.replace(0, 1, "");
		if(value.charAt(0) != wrapWith) value.insert(0, wrapWith);
		if(value.charAt(value.length() - 1) != wrapWith && StringWrap.isValid(value.charAt(value.length() - 1)))
			value.setLength(value.length() - 1);
		if(value.charAt(value.length() - 1) != wrapWith) value.append(wrapWith);
		if(value.length() == 1 && value.charAt(0) == wrapWith) value.append(wrapWith);
		if(wrapWith == StringWrap.SINGLE_QUOTED.wrapWith())
			value.replace(1, value.length() - 1, value.substring(1, value.length() - 1).replaceAll(("'(')"), ("$1")).replace(("'"), ("''")));
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


	public static List<String> findKeys(InputStream file) {
		if(file == null) return new LinkedList<>();
		try {
			String currentLine;
			BufferedReader reader = new BufferedReader(new InputStreamReader(file));
			List<String> keys = new LinkedList<>();
			String path;
			StringBuilder listSection = new StringBuilder();
			int lastIndent = -1, indents = 0;
			List<String> pathList = new ArrayList<>();
			boolean isListSection = false;
			while((currentLine = reader.readLine()) != null) {
				String originalLine = currentLine;
				Matcher pathMatcher = COMMENT_PATH_DETECTOR.matcher(currentLine);
				int currentIndent;
				if(pathMatcher.find()) {
					currentIndent = pathMatcher.group(1).length();
					currentLine = pathMatcher.group(2);
				} else continue;
				if(indents == 0 && currentIndent > 0) indents = currentIndent;
				if(currentLine.isEmpty() || currentLine.startsWith("#") || currentLine.replaceAll("\\s", "").startsWith("#")
					 || currentLine.replaceAll("\\s", "").startsWith("-")) {
					isListSection = originalLine.matches("\\s*-\\s*[^:]+: .*");
					if(listSection.length() != 0) listSection.setLength(0);
					if(isListSection) listSection.append(originalLine);
					continue;
				}
				if(isListSection) {
					listSection.append('\n').append(originalLine);
					if(!listSection.toString().matches("(\\s*)-(\\s*)[^:]+:\\s*[^\\n]+\\n((\\1\\2) [^:]+:\\s*[^\\n]+\\n?)*")) {
						isListSection = false;
						listSection.setLength(0);
					}
				}
				if(isListSection) continue;
				pathList = createPath(pathList, currentLine, currentIndent, lastIndent, indents);
				path = String.join(".", pathList).replace("'", "");
				keys.add(path);
				lastIndent = currentIndent;
			}
			return keys;
		} catch (IOException e) {
			Logger.getLogger("DynamicStudios").log(Level.SEVERE, "Error while finding keys", e);
			e.printStackTrace();
			return new LinkedList<>();
		}
	}

	public static Map<String, String> findComments(String content) {
		int lastIndent = -1, indents = 0;
		String path;
		StringBuilder comment = new StringBuilder();
		HashMap<String, String> map = new HashMap<>();
		List<String> pathList = new ArrayList<>();
		for(String currentLine : content.split("\n")) {
			if(currentLine.isEmpty() || currentLine.replaceAll("\\s", "").isEmpty()) continue;
			if(currentLine.replaceAll("\\s", "").startsWith("#")) {
				comment.append((comment.length() > 0) ? "" : "\n").append(currentLine.substring(currentLine.indexOf('#')));
				continue;
			}
			int currentIndent = 0;
			Matcher matcher = COMMENT_PATH_DETECTOR.matcher(currentLine);
			if(matcher.find()) {
				currentIndent = matcher.group(1).length();
				currentLine = matcher.group(2);
			}else continue;

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
	}

	public static Map<String, String> findComments(InputStream file) {
		if(file == null) return new HashMap<>();
		return findComments(String.join("\n", FileUtils.readInputStream(file)));
	}

	public static Map<String, String> findInlineComments(String content) {
		String path;
		HashMap<String, String> map = new HashMap<>();
		String comment = "";
		int lastIndent = -1, indents = 0;

		List<String> pathList = new ArrayList<>();
		for(String currentLine : content.split("\n")) {
			Matcher inlineMatcher = INLINE_COMMENT_DETECTOR.matcher(currentLine);
			Matcher defaultPathMatcher = COMMENT_PATH_DETECTOR.matcher(currentLine);
			int currentIndent = 0;
			if(currentLine.replaceAll("\\s", "").startsWith("#")) continue;
			if(inlineMatcher.find()) {
				currentIndent = inlineMatcher.group(1) == null ? 0 : inlineMatcher.group(1).length();
				currentLine = inlineMatcher.group(2);
				if(inlineMatcher.group(4) != null) comment = inlineMatcher.group(4);
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
	}
	public static Map<String, String> findInlineComments(InputStream file) {
		if(file == null) return new HashMap<>();
		return findInlineComments(String.join("\n", FileUtils.readInputStream(file)));
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