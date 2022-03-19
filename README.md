# DynamicConfigurations

[Join the discord to be notified of updates!](https://discord.gg/QuG8R6c3ry)


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
         .autoSave(true);
   json.set("array", new String[]{"a","b","c"})
      .set("list", Arrays.asList("d","e","f"))
      .set("string", "h\ni\nj");
}
{
   IDynamicConfiguration yml =
      DynamicConfigurationManager.createConfiguration(this, "plugins/Example/example.yml")
         .autoSave(true);
   yml.set("array", new String[]{"a","b","c"})
      .set("list", Arrays.asList("d","e","f"))
      .set("list2.list", Arrays.asList("g","h","i"))
      .set("string", "h\ni\nj", "test comment")
      .set("string2.test", "h\ni\nj", "test comment :D");
}
```

# Maven
```xml
<repositories>
  <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
   <groupId>com.github.PerryPlaysMC</groupId>
   <artifactId>DynamicConfigurations</artifactId>
   <version>v1.2.2</version>
</dependency>
```
[![](https://jitpack.io/v/PerryPlaysMC/DynamicConfigurations.svg)](https://jitpack.io/#PerryPlaysMC/DynamicConfigurations)
