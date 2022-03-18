package config.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Creator: PerryPlaysMC
 * Created: 10/2021
 **/
public class DynamicJsonAdapter extends TypeAdapter<DynamicJsonConfigurationSection> {

   private Gson GSON;
   private final DynamicJsonConfiguration configuration;

   public void gson(Gson GSON) {
      this.GSON = GSON;
   }

   public DynamicJsonAdapter(DynamicJsonConfiguration configuration) {
      this.configuration = configuration;
   }

   @Override
   public void write(JsonWriter jsonWriter, DynamicJsonConfigurationSection iDynamicConfigurationSection) throws IOException {
      jsonWriter.jsonValue(GSON.toJson(iDynamicConfigurationSection.data()).replace("\n","\n  "));
   }

   @Override
   public DynamicJsonConfigurationSection read(JsonReader jsonReader) throws IOException {
      return new DynamicJsonConfigurationSection(configuration, jsonReader.nextName(),GSON.fromJson(jsonReader, Map.class));
   }
}
