// arden2bytecode
// Copyright (c) 2010, Daniel Grunwald
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this list
//   of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice, this list
//   of conditions and the following disclaimer in the documentation and/or other materials
//   provided with the distribution.
//
// - Neither the name of the owner nor the names of its contributors may be used to
//   endorse or promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &AS IS& AND ANY EXPRESS
// OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package arden.codegenerator;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for writing .class files
 * 
 * @author Daniel Grunwald
 */
public final class ClassFileWriter {
	private ConstantPool pool = new ConstantPool();

	private int this_class;
	private int super_class;
	private String sourceFileName;
	private List<Annotation> annotations = new ArrayList<>();

	/** Creates a new ClassFileWriter for writing the specified class */
	public ClassFileWriter(String className, Class<?> superClass) {
		if (className == null)
			throw new IllegalArgumentException();
		this_class = pool.getClassByJavaName(className.replace('.', '/'));
		super_class = pool.getClass(superClass);
	}

	public void addAnnotation(Annotation annotation) {
		annotations.add(annotation);
		if (annotations.size() > 65535) {
			throw new ClassFileLimitExceededException("Too many annotations.");
		}
	}

	private byte[] getAnnotationData(List<Annotation> annotations) throws IOException {
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(byteOutputStream);
		data.writeShort(annotations.size()); // num_annotations
		for (Annotation annotation : annotations) {
			annotation.save(data, pool);
		}
		data.flush();
		return byteOutputStream.toByteArray();
	}

	/** Sets the source file name used for the debugger */
	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	/** Gets the source file name used for the debugger */
	public String getSourceFileName() {
		return sourceFileName;
	}

	private class AttributeInfo {
		int nameIndex;
		byte[] data;

		public AttributeInfo(String name) {
			nameIndex = pool.getUtf8(name);
		}

		void save(DataOutput output) throws IOException {
			output.writeShort(nameIndex); // attribute_name_index
			output.writeInt(data.length); // attribute_length
			output.write(data);
		}
	}

	private class FieldInfo {
		short access_flags;
		int name_index;
		int descriptor_index;

		FieldInfo(String name, Class<?> type, int modifiers) {
			access_flags = (short) modifiers;
			name_index = pool.getUtf8(name);
			descriptor_index = pool.getUtf8(ConstantPool.createFieldDescriptor(type));
		}

		void save(DataOutput output) throws IOException {
			output.writeShort(access_flags);
			output.writeShort(name_index);
			output.writeShort(descriptor_index);
			output.writeShort(0); // attributes_count
		}
	}

	private List<FieldInfo> fields = new ArrayList<FieldInfo>();

	/** Declares a new field in the class */
	public FieldReference declareField(String name, Class<?> type, int modifiers) {
		if (fields.size() >= 65534)
			throw new ClassFileLimitExceededException("Too many fields.");
		fields.add(new FieldInfo(name, type, modifiers));
		return pool.createFieldref(this_class, name, type);
	}

	private class MethodInfo {
		short access_flags;
		int name_index;
		int descriptor_index;
		MethodWriter writer;
		AttributeInfo codeAttribute;
		AttributeInfo annotationAttribute;

		MethodInfo(String name, int modifiers, Class<?>[] parameters, Class<?> returnType, Annotation[] annotations) {
			writer = new MethodWriter(pool, (modifiers & Modifier.STATIC) != Modifier.STATIC, parameters.length);
			access_flags = (short) modifiers;
			name_index = pool.getUtf8(name);
			descriptor_index = pool.getUtf8(ConstantPool.createMethodDescriptor(parameters, returnType));

			codeAttribute = new AttributeInfo("Code");

			if (annotations.length > 65535) {
				throw new ClassFileLimitExceededException("Too many annotations.");
			}

			if (annotations.length != 0) {
				try {
					annotationAttribute = new AttributeInfo("RuntimeVisibleAnnotations");
					annotationAttribute.data = getAnnotationData(Arrays.asList(annotations));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		void save(DataOutput output) throws IOException {
			output.writeShort(access_flags);
			output.writeShort(name_index);
			output.writeShort(descriptor_index);

			codeAttribute.data = writer.getCodeAttributeData();

			List<AttributeInfo> attributes = new ArrayList<>();
			attributes.add(codeAttribute);
			if (annotationAttribute != null) {
				attributes.add(annotationAttribute);
			}
			output.writeShort(attributes.size());
			for (AttributeInfo attr : attributes) {
				attr.save(output);
			}
		}
	}

	private static final String JAVA_CONSTRUCTOR_NAME = "<init>";
	private static final String JAVA_STATIC_INITIALIZER_NAME = "<clinit>";

	List<MethodInfo> methods = new ArrayList<MethodInfo>();

	/** Creates a new annotated method in the class */
	public MethodWriter createMethod(String name, int modifiers, Class<?>[] parameters, Class<?> returnType,
			Annotation[] annotations) {
		if (methods.size() >= 65534)
			throw new ClassFileLimitExceededException("Too many methods.");
		MethodInfo info = new MethodInfo(name, modifiers, parameters, returnType, annotations);
		methods.add(info);
		return info.writer;
	}

	/** Creates a new method in the class */
	public MethodWriter createMethod(String name, int modifiers, Class<?>[] parameters, Class<?> returnType) {
		return createMethod(name, modifiers, parameters, returnType, new Annotation[] {});
	}

	public MethodWriter createConstructor(int modifiers, Class<?>[] parameters) {
		return createMethod(JAVA_CONSTRUCTOR_NAME, modifiers, parameters, Void.TYPE);
	}

	public MethodWriter createStaticInitializer() {
		return createMethod(JAVA_STATIC_INITIALIZER_NAME, Modifier.PUBLIC | Modifier.STATIC, new Class<?>[0],
				Void.TYPE);
	}

	/** Saves the class file to disk */
	public void save(String filename) throws IOException {
		DataOutputStream s = new DataOutputStream(new FileOutputStream(filename));
		try {
			save(s);
		} finally {
			s.close();
		}
	}

	/** Saves the class file */
	public void save(DataOutput output) throws IOException {
		ArrayList<AttributeInfo> attributes = new ArrayList<AttributeInfo>();

		// attributes must be created before constant pool is saved
		if (sourceFileName != null) {
			AttributeInfo sourceFile = new AttributeInfo("SourceFile");
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(byteOutputStream);
			data.writeShort(pool.getUtf8(sourceFileName));
			data.flush();
			sourceFile.data = byteOutputStream.toByteArray();
			attributes.add(sourceFile);
		}
		if (!annotations.isEmpty()) {
			AttributeInfo annotationAttribute = new AttributeInfo("RuntimeVisibleAnnotations");
			annotationAttribute.data = getAnnotationData(annotations);
			attributes.add(annotationAttribute);
		}

		// Write the class file
		output.writeInt(0xCAFEBABE); // magic
		output.writeShort(0x00); // minor_version
		output.writeShort(0x32); // major_version
		pool.save(output);
		output.writeShort(0x0021); // ACC_SUPER | ACC_PUBLIC
		output.writeShort(this_class);
		output.writeShort(super_class);
		output.writeShort(0); // interfaces_count
		output.writeShort(fields.size()); // fields_count
		for (FieldInfo info : fields)
			info.save(output);
		output.writeShort(methods.size()); // methods_count
		for (MethodInfo info : methods)
			info.save(output);

		output.writeShort(attributes.size()); // attributes_count
		for (AttributeInfo attr : attributes)
			attr.save(output);
	}
}
