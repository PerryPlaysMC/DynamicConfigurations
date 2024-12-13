package io.dynamicstudios.configurations.yaml.bukkit;

import io.dynamicstudios.configurations.utils.ReflectionUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Ref;
import java.util.Arrays;
import java.util.Map;

public class YamlArrayPreserver {


  public static void apply(Representer rep) {
    Method addClassTag = ReflectionUtil.findMethod(rep.getClass(),new String[]{"addClassTag"}, new Class[]{Class.class, Tag.class});
    Field representersField = ReflectionUtil.findField(rep.getClass(), new String[]{"representers"});
    Field multiRepresentersField = ReflectionUtil.findField(rep.getClass(), new String[]{"multiRepresenters"});
    RepresentArray representArray = new RepresentArray(rep);
    try {
      ReflectionUtil.invokeMethod(addClassTag, rep, (new Object[0]).getClass(), YamlConstructor.ARRAY);
      ReflectionUtil.invokeMethod(addClassTag, rep, short[].class, YamlConstructor.ARRAY);
      ReflectionUtil.invokeMethod(addClassTag, rep, int[].class, YamlConstructor.ARRAY);
      ReflectionUtil.invokeMethod(addClassTag, rep, long[].class, YamlConstructor.ARRAY);
      ReflectionUtil.invokeMethod(addClassTag, rep, float[].class, YamlConstructor.ARRAY);
      ReflectionUtil.invokeMethod(addClassTag, rep, double[].class, YamlConstructor.ARRAY);
      ReflectionUtil.invokeMethod(addClassTag, rep, char[].class, YamlConstructor.ARRAY);
      ReflectionUtil.invokeMethod(addClassTag, rep, boolean[].class, YamlConstructor.ARRAY);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Map<Class<?>, Represent> representers = (Map<Class<?>, Represent>) ReflectionUtil.invokeField(representersField, rep);
    Map<Class<?>, Represent> multiRepresenters = (Map<Class<?>, Represent>) ReflectionUtil.invokeField(multiRepresentersField, rep);
    representers.put((new Object[0]).getClass(), representArray);
    representers.put(short[].class, representArray);
    representers.put(int[].class, representArray);
    representers.put(long[].class, representArray);
    representers.put(float[].class, representArray);
    representers.put(double[].class, representArray);
    representers.put(char[].class, representArray);
    representers.put(boolean[].class, representArray);
    multiRepresenters.put((new Object[0]).getClass(), representArray);
    multiRepresenters.put(short[].class, representArray);
    multiRepresenters.put(int[].class, representArray);
    multiRepresenters.put(long[].class, representArray);
    multiRepresenters.put(float[].class, representArray);
    multiRepresenters.put(double[].class, representArray);
    multiRepresenters.put(char[].class, representArray);
    multiRepresenters.put(boolean[].class, representArray);
  }

  private static class RepresentArray implements Represent {
    Representer representer;
    public RepresentArray(Representer representer) {
      this.representer = representer;
    }

    @Override
    public Node representData(Object data) {
      Object[] array = (Object[]) data;
      Method method = ReflectionUtil.findMethod(Representer.class, new String[]{"representSequence"}, new Class[]{Tag.class, Iterable.class, boolean.class}
         , new Class[]{Tag.class, Iterable.class, DumperOptions.FlowStyle.class});
      return (Node) ReflectionUtil.invokeMethod(method, representer,Tag.SEQ, Arrays.asList(array), (method == null) ? false : (method.getParameterTypes()[2].getName().contains("boolean") ? false : DumperOptions.FlowStyle.FLOW));
    }
  }

}
