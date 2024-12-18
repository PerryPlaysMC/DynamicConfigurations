# DynamicConfigurations

[Join the discord to be notified of updates!](https://discord.gg/QuG8R6c3ry)
<br>NEW [Wiki](https://github.com/PerryPlaysMC/DynamicConfigurations/wiki)


# Usage
```java
DynamicConfigurationDirectory directory = new DynamicConfigurationDirectory(this, new File("plugins/Example", "thisisatest"));
IDynamicConfiguration cf1 = directory.createConfiguration("example1.yml");
IDynamicConfiguration cf2 = directory.createConfiguration("example2.yml");
IDynamicConfiguration cf3 = DynamicConfigurationManager.createConfiguration(this, "plugins/Example/thisisatest", "example3.yml");
IDynamicConfiguration cf4 = DynamicConfigurationManager.createConfiguration(this, new File("plugins/Example/thisisatest"), "example4.yml");
directory.addConfiguration(cf3);
directory.reload();
{
   IDynamicConfiguration json =
      DynamicConfigurationManager.createConfiguration(this, "plugins/Example/example.json")
         .options().autoSave(true).configuration();
   json.set("array", new String[]{"a","b","c"})
      .set("list", Arrays.asList("d","e","f"))
      .set("string", "h\ni\nj");
}
{
   IDynamicConfiguration ghost = DynamicConfigurationManager.createGhostConfiguration(this, "example.yml");
   IDynamicConfiguration yml = DynamicConfigurationManager.createConfiguration(this, "plugins/Example/example.yml")
         .options().autoSave(true).loadDefaults(true).appendMissingKeys(true).configuration();
   yml.set("array", new String[]{"a","b","c"})
      .set("list", Arrays.asList("d","e","f"))
      .setInline("list2.list", Arrays.asList("g","h","i"), "Inline comment")
      .set("string", "h\ni\nj", "test comment")
      .set("string2.test", "h\ni\nj", "test comment :D");
   
   yml.options().defaults().set("defaultValue", "This is a default string");

  String message = yml.getString("defaultValue");
  //returns "This is a default string"
  String text = yml.getString("defaultString");
  //returns "This is a default String"
}
```
## Example.yml (in project)
```yml
defaultString: "This is a default String"

```

# Maven
```xml
<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.PerryPlaysMC</groupId>
    <artifactId>DynamicConfigurations</artifactId>
    <version>1.5.0</version>
</dependency>
```
[![](https://jitpack.io/v/PerryPlaysMC/DynamicConfigurations.svg)](https://jitpack.io/#PerryPlaysMC/DynamicConfigurations)
