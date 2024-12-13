package io.dynamicstudios.configurations.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ReflectionUtil {


	private static final Map<Class<?>, Map<String, Method>> METHOD_CACHE = new HashMap<>();
	private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new HashMap<>();
	private static final Map<String, Class<?>> CLASS_CACHE = new HashMap<>();

	public static Class<?> findClass(String... classNames) {
		for(String className : classNames) {
			if(CLASS_CACHE.containsKey(className)) return CLASS_CACHE.get(className);
		}
		for(String className : classNames) {
			try {
				CLASS_CACHE.put(className, Class.forName(className));
				return CLASS_CACHE.get(className);
			}catch (Exception e) {}
		}
		return null;
	}

	public static Object invokeMethod(Method method, Object target, Object... args) {
		try {
			if(method == null)return null;
			method.setAccessible(true);
			return method.invoke(target, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			return null;
		}
	}

	public static Object invokeField(Field field, Object target) {
		try {
			if(field == null)return null;
			field.setAccessible(true);
			return field.get(target);
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	public static Method findMethod(Class<?> clazz, String[] name, Class<?>[]... params) {
		if(clazz == null) return null;
		if(METHOD_CACHE.containsKey(clazz)) {
			for(String s : name) {
				for(Class<?>[] param : params) {
					String key = getKey(s, param);
					if(METHOD_CACHE.get(clazz).containsKey(key)) {
						return METHOD_CACHE.get(clazz).get(key);
					}
				}
			}
			return null;
		}
		Set<Method> methods = new HashSet<>();
		methods.addAll(Arrays.asList(clazz.getMethods()));
		methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
		Class<?> sup = clazz.getSuperclass();
		while(sup != null) {
			methods.addAll(Arrays.asList(sup.getMethods()));
			methods.addAll(Arrays.asList(sup.getDeclaredMethods()));
			sup = sup.getSuperclass();
		}
		for(Method method : methods) {
			for(String s : name) {
				if(method.getName().equals(s)) {
					A:for(Class<?>[] param : params) {
						if(method.getParameterTypes().length == param.length) {
							for(int i = 0; i < method.getParameterTypes().length; i++) {
								if(method.getParameterTypes()[i] != param[i]) continue A;
							}
							String key = getKey(s, param);
							METHOD_CACHE.computeIfAbsent(clazz, k -> new HashMap<>()).put(key, method);
							return method;
						}
					}
				}
			}
		}
		return null;
	}

	public static Field findField(Class<?> clazz, String[] name, Class<?>... returnTypes) {
		if(clazz == null) return null;
		if(FIELD_CACHE.containsKey(clazz)) {
			for(String s : name) {
				for(Class<?> param : returnTypes) {
					String key = getKey(s, param);
					if(FIELD_CACHE.get(clazz).containsKey(key)) {
						return FIELD_CACHE.get(clazz).get(key);
					}
				}
			}
			return null;
		}
		Set<Field> fields = new HashSet<>();
		fields.addAll(Arrays.asList(clazz.getFields()));
		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		Class<?> sup = clazz.getSuperclass();
		while(sup != null) {
			fields.addAll(Arrays.asList(sup.getFields()));
			fields.addAll(Arrays.asList(sup.getDeclaredFields()));
			sup = sup.getSuperclass();
		}
		for(Field field : fields) {
			for(String s : name) {
				if(field.getName().equalsIgnoreCase(s)) {
					if(returnTypes.length == 0) return field;
					for(Class<?> returnType : returnTypes) {
						if(field.getType() == returnType) {
							String key = getKey(s, returnType);
							FIELD_CACHE.computeIfAbsent(clazz, k -> new HashMap<>()).put(key, field);
							return field;
						}
					}
				}
			}
		}
		return null;
	}

	private static String getKey(String s, Class<?>... param) {
		String key = s + "(";
		for(Class<?> aClass : param) {
			key += aClass.getName() + ",";
		}
		if(key.endsWith(",")) key = key.substring(0, key.length() - 1);
		key += ")";
		return key;
	}


}
