package io.dynamicstudios.configurations.yaml.bukkit;

import io.dynamicstudios.configurations.utils.FileUtils;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of {@link org.bukkit.configuration.Configuration} which saves all files in Yaml.
 * Note that this implementation is not synchronized.
 */
public class YamlConfigurationUtil extends FileConfiguration {
 protected static final String COMMENT_PREFIX = "# ";
 protected static final String BLANK_CONFIG = "{}\n";

 private static Field yamlOptionsField = null;
 private static Field yamlRepresenterField = null;
 private static Field yamlField = null;
 private static Field constructorField = null;

 static {
	try {
	 List<Field> fields = new ArrayList<>();
	 fields.addAll(Arrays.asList(YamlConfiguration.class.getDeclaredFields()));
	 fields.addAll(Arrays.asList(YamlConfiguration.class.getDeclaredFields()));
	 for(Field field : fields) {
		if(field.getType().getName().equalsIgnoreCase("org.yaml.snakeyaml.DumperOptions"))
		 yamlOptionsField = field;
		if(field.getType().getName().equalsIgnoreCase("org.bukkit.configuration.file.YamlRepresenter"))
		 yamlRepresenterField = field;
		if(field.getType().getName().equalsIgnoreCase("org.snake.Yaml") ||
			 field.getType().getName().equalsIgnoreCase("org.yaml.snakeyaml.Yaml"))
		 yamlField = field;
	 }
	 fields.clear();
	 fields.addAll(Arrays.asList(Yaml.class.getDeclaredFields()));
	 fields.addAll(Arrays.asList(Yaml.class.getFields()));
	 for(Field field : fields) {
		if(field.getType().getName().equalsIgnoreCase("org.snake.constructor.BaseConstructor") ||
			 field.getType().getName().equalsIgnoreCase("org.yaml.snakeyaml.constructor.BaseConstructor")) {
		 constructorField = field;
		 break;
		}
	 }
	 constructorField.setAccessible(true);
	 yamlRepresenterField.setAccessible(true);
	 yamlOptionsField.setAccessible(true);
	 yamlField.setAccessible(true);
	} catch(Exception e) {
	 e.printStackTrace();
	}
 }

 /**
	* Creates a new {@link YamlConfigurationUtil}, loading from the given file.
	* <p>
	* Any errors loading the Configuration will be logged and then ignored.
	* If the specified input is not a valid config, a blank config will be
	* returned.
	* <p>
	* The encoding used may follow the system dependent default.
	*
	* @param file Input file
	* @return Resulting configuration
	* @throws IllegalArgumentException Thrown if file is null
	*/
 public static YamlConfigurationUtil loadConfiguration(File file) {

	YamlConfigurationUtil config = new YamlConfigurationUtil();

	try {
	 config.load(file);
	} catch(FileNotFoundException ex) {
	} catch(IOException | InvalidConfigurationException ex) {
	 Logger.getLogger("DynamicConfigurations").log(Level.WARNING, "Cannot load " + file, ex);
	}

	return config;
 }

 public static YamlConfigurationUtil loadConfiguration(String contents) throws InvalidConfigurationException {
	YamlConfigurationUtil config = new YamlConfigurationUtil();
	config.loadFromString(contents);
	return config;
 }

 /**
	* Creates a new {@link YamlConfigurationUtil}, loading from the given reader.
	* <p>
	* Any errors loading the Configuration will be logged and then ignored.
	* If the specified input is not a valid config, a blank config will be
	* returned.
	*
	* @param reader input
	* @return resulting configuration
	* @throws IllegalArgumentException Thrown if stream is null
	*/
 public static YamlConfigurationUtil loadConfiguration(Reader reader) {

	YamlConfigurationUtil config = new YamlConfigurationUtil();

	try {
	 config.load(reader);
	} catch(IOException | InvalidConfigurationException ex) {
	 Logger.getLogger("DynamicStudios").log(Level.SEVERE, "Cannot load configuration from stream", ex);
	}

	return config;
 }

 @Override
 public String saveToString() {
	org.bukkit.configuration.file.YamlConfiguration file = new org.bukkit.configuration.file.YamlConfiguration();
	String header = buildHeader();
	try {
	 if(yamlOptionsField == null || yamlRepresenterField == null || yamlField == null) return header;
	 DumperOptions options = (DumperOptions) yamlOptionsField.get(file);
	 YamlRepresenter rep = (YamlRepresenter) yamlRepresenterField.get(file);
	 Yaml yaml = (Yaml) yamlField.get(file);
	 YamlConstructor.apply((SafeConstructor) constructorField.get(yaml));
	 YamlArrayPreserver.apply(rep);
	 options.setIndent(options().indent());
	 options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
	 rep.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
	 String dump = yaml.dump(getValues(false));
	 if(dump.equals(BLANK_CONFIG)) {
		dump = "";
	 }
	 header += dump;
	} catch(IllegalAccessException e) {
	 throw new RuntimeException(e);
	} catch(Exception e) {
	 throw new RuntimeException(e);
	}
	return header;
 }

 public static boolean validate(InputStream reader) {
	return validate(new InputStreamReader(reader));
 }

 public static boolean validate(Reader reader) {
	BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);

	StringBuilder builder = new StringBuilder();

	try {
	 String line;

	 while((line = input.readLine()) != null) {
		builder.append(line);
		builder.append('\n');
	 }
	} catch(Exception e) {
	 e.printStackTrace();
	} finally {
	 try {
		input.close();
	 } catch(IOException e) {
		e.printStackTrace();
	 }
	}
	return validate(builder.toString());
 }

 public static boolean validate(String contents) {
	YamlConfigurationUtil config = new YamlConfigurationUtil();
	try {
//      config.loaderOptions.setMaxAliasesForCollections(Integer.MAX_VALUE);
	 config.load(contents);
	 return true;
	} catch(Exception e) {
	 return false;
	}
 }


 public static YamlConfigurationUtil fixFromString(InputStream reader, Map<String, String> COMMENTS, Map<String, String> INLINE_COMMENTS) {
	return fixFromString(new InputStreamReader(reader), COMMENTS, INLINE_COMMENTS);
 }

 public static YamlConfigurationUtil fixFromString(Reader reader, Map<String, String> COMMENTS, Map<String, String> INLINE_COMMENTS) {
	BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);

	StringBuilder builder = new StringBuilder();

	try {
	 String line;

	 while((line = input.readLine()) != null) {
		builder.append(line);
		builder.append('\n');
	 }
	} catch(Exception e) {
	 e.printStackTrace();
	} finally {
	 try {
		input.close();
	 } catch(IOException e) {
		e.printStackTrace();
	 }
	}
	return fixFromString(builder.toString(), COMMENTS, INLINE_COMMENTS);
 }

 public static YamlConfigurationUtil fixFromString(String contents, Map<String, String> COMMENTS, Map<String, String> INLINE_COMMENTS) {
	try {
	 YamlConfigurationUtil config = loadConfiguration(contents);
	 COMMENTS.putAll(FileUtils.findComments(contents));
	 INLINE_COMMENTS.putAll(FileUtils.findInlineComments(contents));
	 return config;
	} catch(InvalidConfigurationException e) {
	 Pattern pat = Pattern.compile("(.*)\\s*line\\s*(\\d+).*column\\s*(\\d+):(\\n[^\\n]+\\n[^\\n]+\\n)(.*)\\s*line\\s*(\\d+).*column\\s*(\\d+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	 Pattern cmt = Pattern.compile("(\\s*(?:(?<a>['\\\"]).*(?:\\k<a>))|[^#]+)?(\\s*#.+)?", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	 Matcher matcher = pat.matcher(e.getMessage());
	 String[] lines = contents.split("\n");
	 boolean find = matcher.find();
	 if(!find) return null;
	 while(find) {
		String expected = matcher.group(5);
		int line = Integer.parseInt(matcher.group(6)) - 1;
		int col = Integer.parseInt(matcher.group(7));
		if(expected.startsWith("expected")) {
		 String ln = lines[line];
		 Matcher m = cmt.matcher(ln);
		 String comment = ln.contains("#") ? ln.substring(ln.indexOf('#') - 1) : "";
		 String text = ln.contains("#") ? ln.split("#")[0] : ln;
		 if(m.find()) {
			text = m.group(1);
			comment = m.group(3) == null ? "" : m.group(3);
		 }
		 if(expected.contains("block end"))
			text = "#" + lines[line];
		 else if(expected.startsWith("expected") && (expected.contains("node") || expected.contains("content"))) {
			if(text.endsWith(",")) text = text.substring(0, text.length() - 1);
			if(!text.endsWith("]")) text += "]";
			text = text.substring(0, col - 1) + text.substring(col);
		 } else if(expected.startsWith("expected") && (expected.contains("']'") || expected.contains("','"))) {
			if(text.endsWith(",")) text = text.substring(0, text.length() - 1);
			if(!text.endsWith("]")) text += "]";
		 } else {
			Logger.getLogger("DynamicStudios").log(Level.SEVERE, "EXPECTED: '" + expected + "'", e);
			try {
			 return loadConfiguration(contents);
			} catch(InvalidConfigurationException ex) {
			 throw new RuntimeException(ex);
			}
		 }
		 lines[line] = text + comment;
		}
		find = matcher.find();
	 }
	 contents = String.join("\n", lines);
	 COMMENTS.putAll(FileUtils.findComments(contents));
	 INLINE_COMMENTS.putAll(FileUtils.findInlineComments(contents));
	 return fixFromString(contents, COMMENTS, INLINE_COMMENTS);
	}
 }

 @Override
 public void loadFromString(String contents) throws InvalidConfigurationException {

	Map<?, ?> input;
	try {
	 YamlConfiguration conf = new YamlConfiguration();
	 if(yamlRepresenterField == null || yamlField == null) return;
	 YamlRepresenter rep = (YamlRepresenter) yamlRepresenterField.get(conf);
	 Yaml yaml = (Yaml) yamlField.get(conf);
	 YamlConstructor.apply((SafeConstructor) constructorField.get(yaml));
	 YamlArrayPreserver.apply(rep);
	 input = (Map<?, ?>) yaml.load(contents);
	} catch(YAMLException e) {
	 throw new InvalidConfigurationException(e);
	} catch(ClassCastException e) {
	 throw new InvalidConfigurationException("Top level is not a Map.");
	} catch(Exception e) {
	 throw new RuntimeException(e);
	}

	String header = parseHeader(contents);
	if(!header.isEmpty()) options().header(header);

	this.map.clear();

	if(input != null) {
	 convertMapsToSections(input, this);
	}
 }

 protected void convertMapsToSections(Map<?, ?> input, ConfigurationSection section) {
	for(Map.Entry<?, ?> entry : input.entrySet()) {
	 String key = entry.getKey().toString();
	 Object value = entry.getValue();

	 if(value instanceof Map) {
		convertMapsToSections((Map<?, ?>) value, section.createSection(key));
	 } else {
		section.set(key, value);
	 }
	}
 }

 protected String parseHeader(String input) {
	String[] lines = input.split("\r?\n", -1);
	StringBuilder result = new StringBuilder();
	boolean readingHeader = true;
	boolean foundHeader = false;

	for(int i = 0; (i < lines.length) && (readingHeader); i++) {
	 String line = lines[i];

	 if(line.startsWith(COMMENT_PREFIX)) {
		if(i > 0) {
		 result.append("\n");
		}

		if(line.length() > COMMENT_PREFIX.length()) {
		 result.append(line.substring(COMMENT_PREFIX.length()));
		}

		foundHeader = true;
	 } else if((foundHeader) && (line.length() == 0)) {
		result.append("\n");
	 } else if(foundHeader) {
		readingHeader = false;
	 }
	}

	return result.toString();
 }

 @Override
 protected String buildHeader() {
	String header = options().header();

	if(options().copyHeader()) {
	 Configuration def = getDefaults();

	 if((def instanceof FileConfiguration)) {
		FileConfiguration filedefaults = (FileConfiguration) def;
		String defaultsHeader = String.join(",", filedefaults.options().header());
		if(!defaultsHeader.isEmpty()) {
		 return defaultsHeader;
		}
	 }
	}

	StringBuilder builder = new StringBuilder();
	String[] lines = header.split("\r?\n", -1);
	boolean startedHeader = false;

	for(int i = lines.length - 1; i >= 0; i--) {
	 builder.insert(0, "\n");

	 if((startedHeader) || (!lines[i].isEmpty())) {
		builder.insert(0, lines[i]);
		builder.insert(0, COMMENT_PREFIX);
		startedHeader = true;
	 }
	}

	return builder.toString();
 }

 @Override
 public YamlConfigurationOptions options() {
	if(options == null) {
	 options = new YamlConfigurationOptions(this);
	}

	return (YamlConfigurationOptions) options;
 }
}
