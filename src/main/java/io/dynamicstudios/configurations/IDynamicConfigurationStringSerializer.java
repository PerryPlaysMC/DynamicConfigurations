package io.dynamicstudios.configurations;

/**
 * Creator: PerryPlaysMC
 * Created: 05/2022
 * @param <T>   The class to serialize/deserialize (i.e. MyClass)
 **/
public interface IDynamicConfigurationStringSerializer<T> extends IDynamicConfigurationSerializer<T> {

 default void serialize(IDynamicConfigurationSection configuration, T t) {
 }

 default T deserialize(IDynamicConfigurationSection configuration) {
	return null;
 }

 String serialize(T t);

 T deserialize(String configuration);


}
