# DynamicConfigurations

# Usage
```java
{
   IDynamicConfiguration json =
      DynamicConfigManager.createConfiguration(this, "plugins/Test/test.json")
      .autoSave(true);
   json.set("array", new String[]{"a","b","c"})
      .set("list", Arrays.asList("d","e","f"))
      .set("string", "h\ni\nj");
}
{
   IDynamicConfiguration yml =
      DynamicConfigManager.createConfiguration(this, "plugins/Test/test.yml")
         .autoSave(true);
   yml.set("array", new String[]{"a","b","c"})
      .set("list", Arrays.asList("d","e","f"))
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
    <version>1.0-SNAPSHOT</version>
</dependency>
```
