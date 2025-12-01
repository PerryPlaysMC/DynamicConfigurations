package io.dynamicstudios.configurations;

/**
 * Creator: PerryPlaysMC
 * Created: 05/2022
 * @param <T>   The class to serialize/deserialize (i.e. MyClass)
 **/
public interface IDynamicConfigurationSerializer<T> {

 Class<? extends T> type();

 void serialize(IDynamicConfigurationSection configuration, T t);

 T deserialize(IDynamicConfigurationSection configuration);


}
