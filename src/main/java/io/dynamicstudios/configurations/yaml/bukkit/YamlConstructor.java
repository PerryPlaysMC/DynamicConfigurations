package io.dynamicstudios.configurations.yaml.bukkit;

import io.dynamicstudios.configurations.utils.ReflectionUtil;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class YamlConstructor {

 public static final Tag ARRAY = new Tag("!array");

 public static void apply(SafeConstructor safeConstructor) throws Exception {
	Field constructorField = ReflectionUtil.findField(safeConstructor.getClass(), new String[]{"yamlConstructors", "yamlConstructor"});
	Object ycf = ReflectionUtil.invokeField(constructorField, safeConstructor);
	Map<Tag, Construct> yamlConstructors = (Map<Tag, Construct>) (ycf);
	yamlConstructors.put(ARRAY, new ConstructArray(safeConstructor));
	yamlConstructors.put(Tag.SEQ, new ConstructArray(safeConstructor));
 }

 private static class ConstructArray implements Construct {
	private SafeConstructor safeConstructor;

	public ConstructArray(SafeConstructor safeConstructor) {
	 this.safeConstructor = safeConstructor;
	}

	private int pointer(Mark mark) {
	 Field f = ReflectionUtil.findField(mark.getClass(), new String[]{"pointer"});
	 Object val = ReflectionUtil.invokeField(f, mark);
	 return val == null ? 0 : (int) val;
	}


	private int[] buffer(Mark mark) {
	 Field f = ReflectionUtil.findField(mark.getClass(), new String[]{"buffer"});
	 Object val = ReflectionUtil.invokeField(f, mark);
	 if(val instanceof String) {
		return ((String) val).codePoints().toArray();
	 }
	 return val == null ? new int[0] : (int[]) val;
	}

	@Override
	public Object construct(Node node) {
	 SequenceNode seqNode = (SequenceNode) node;
	 Mark startMark = seqNode.getStartMark();
	 try {
		Mark endMark = seqNode.getEndMark();

		// Get the buffer and pointers
		int[] buffer = buffer(startMark);
		int startPointer = pointer(startMark);
		int endPointer = pointer(endMark);

		// Extract the text between start and end markers
		StringBuilder currentText = new StringBuilder();
		int start = startPointer;
		while(start < buffer.length) {
		 if(start >= endPointer) break;
		 currentText.appendCodePoint(buffer[Math.max(start, 0)]);
		 ++start;
		}
		if(currentText.length() > 0)
		 if(seqNode.getTag().equals(YamlConstructor.ARRAY) || (currentText.charAt(0) == '[' && currentText.charAt(currentText.length() - 1) == ']')) {
			Method constructSequenceMethod = ReflectionUtil.findMethod(BaseConstructor.class, new String[]{"constructSequence"}, new Class<?>[]{SequenceNode.class});
			List<? extends Object> objs = (List<? extends Object>) ReflectionUtil.invokeMethod(constructSequenceMethod, safeConstructor, seqNode);
			if(objs == null) return new Object[0];

			Class<?> type = objs.stream().filter(Objects::nonNull).findFirst().map(Object::getClass).orElse(null);
			if(type == null) type = Object.class;
			Object array = Array.newInstance(type, objs.size());
			return objs.toArray((Object[]) array);
		 }
	 } catch(Exception e) {
		e.printStackTrace();
	 }
	 return construct2(seqNode);
	}

	public Object construct2(Node node) {
	 SequenceNode seqNode = (SequenceNode) node;

	 // Use reflection to invoke createDefaultList or constructSequence based on construction steps
	 if(node.isTwoStepsConstruction()) {
		Method method = ReflectionUtil.findMethod(BaseConstructor.class, new String[]{
			 "createDefaultList",
			 "newList",
		}, new Class<?>[]{SequenceNode.class}, new Class<?>[]{SequenceNode.class});
		return ReflectionUtil.invokeMethod(method, safeConstructor, seqNode.getValue().size());
	 } else {
		Method method = ReflectionUtil.findMethod(BaseConstructor.class, new String[]{
			 "constructSequence"
		}, new Class<?>[]{SequenceNode.class});
		return ReflectionUtil.invokeMethod(method, safeConstructor, seqNode);
	 }
	}

	public void construct2ndStep(Node node, Object data) {
	 if(node.isTwoStepsConstruction()) {
		Method constructSequenceStep2Method = ReflectionUtil.findMethod(BaseConstructor.class, new String[]{"constructSequenceStep2"}, new Class[]{SequenceNode.class, Collection.class});
		if(constructSequenceStep2Method == null) return;
		ReflectionUtil.invokeMethod(constructSequenceStep2Method, safeConstructor, node, data);
	 } else {
		throw new YAMLException("Unexpected recursive sequence structure. Node: " + node);
	 }
	}
 }
}