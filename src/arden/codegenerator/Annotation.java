package arden.codegenerator;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public final class Annotation {

	private Class<?> clazz;
	private Map<String, Object> elementValuePairs = new TreeMap<>();

	public Annotation(Class<?> clazz) {
		this.clazz = clazz;
	}

	public void addValue(String key, Object value) {
		elementValuePairs.put(key, value);
	}

	public void save(DataOutput output, ConstantPool pool) throws IOException {
		int type_index = pool.getUtf8(ConstantPool.createFieldDescriptor(clazz));
		output.writeShort(type_index);
		output.writeShort(elementValuePairs.size()); // num_element_value_pairs
		for (Entry<String, Object> entry : elementValuePairs.entrySet()) {
			// element_name_index
			String elementName = entry.getKey();
			output.writeShort(pool.getUtf8(elementName));

			// element_value
			Object elementValue = entry.getValue();
			Class<?> type = elementValue.getClass();
			if (type.equals(String.class)) {
				output.writeByte('s');
				int index = pool.getUtf8((String) elementValue);
				output.writeShort(index);
			} else if (type.equals(Class.class)) {
				output.writeByte('c');
				int index = pool.getClass((Class<?>) elementValue);
				output.writeShort(index);
			} else {
				throw new RuntimeException("This annotation value type is not supported yet.");
			}
		}
	}

}
