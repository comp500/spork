package link.infra.spork.jfr.transformer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataParser {
	public static abstract class Element {
		public abstract void setAttribute(String name, String value);

		public abstract Element constructChild(String childType);
	}

	// TODO: don't ignore unparsed data
	public static class UnknownElement extends Element {
		protected Map<String, String> attributes = new HashMap<>();
		protected List<Element> children = new ArrayList<>();
		public final String childType;

		public UnknownElement(String childType) {
			this.childType = childType;
		}

		@Override
		public void setAttribute(String name, String value) {
			attributes.put(name, value);
		}

		@Override
		public Element constructChild(String childType) {
			Element child = new UnknownElement(childType);
			children.add(child);
			return child;
		}
	}

	public static class RootElement extends Element {
		public MetadataElement metadata;

		@Override
		public void setAttribute(String name, String value) {}

		@Override
		public Element constructChild(String childType) {
			if ("metadata".equals(childType)) {
				metadata = new MetadataElement();
				return metadata;
			}
			// TODO: region?
			return new UnknownElement(childType);
		}
	}

	public static class MetadataElement extends Element {
		public List<ClassElement> classes = new ArrayList<>();

		@Override
		public void setAttribute(String name, String value) {}

		@Override
		public Element constructChild(String childType) {
			if ("class".equals(childType)) {
				ClassElement el = new ClassElement();
				classes.add(el);
				return el;
			}
			return new UnknownElement(childType);
		}
	}

	public static class ClassElement extends Element {
		public long id;
		// TODO: store offset for remap?
		public String name;
		public String superType;
		public boolean simple;
		public final List<FieldElement> fields = new ArrayList<>();

		@Override
		public void setAttribute(String name, String value) {
			switch (name) {
				case "id":
					id = Long.parseLong(value);
					break;
				case "name":
					this.name = value;
					break;
				case "superType":
					superType = value;
					break;
				case "simpleType":
					simple = Boolean.parseBoolean(value);
					break;
			}
		}

		@Override
		public Element constructChild(String childType) {
			if ("field".equals(childType)) {
				FieldElement el = new FieldElement();
				fields.add(el);
				return el;
			}
			// TODO: annotations?
			return new UnknownElement(childType);
		}
	}

	public static class FieldElement extends Element {
		public String name;
		public long classId;
		public boolean usesConstantPool;
		public int dimension;

		@Override
		public void setAttribute(String name, String value) {
			switch (name) {
				case "name":
					this.name = value;
					break;
				case "class":
					classId = Long.parseLong(value);
					break;
				case "constantPool":
					usesConstantPool = Boolean.parseBoolean(value);
					break;
				case "dimension":
					dimension = Integer.parseInt(value);
					break;
			}
		}

		@Override
		public Element constructChild(String childType) {
			return new UnknownElement(childType);
		}
	}

	public static RootElement read(RandomAccessFile file, boolean useCompressedInts) throws IOException {
		Util.readInt(file, useCompressedInts); // Event size
		if (Util.readLong(file, useCompressedInts) != 0) {
			throw new IOException("Invalid metadata event");
		}
		Util.readLong(file, useCompressedInts); // Start time
		Util.readLong(file, useCompressedInts); // Duration
		Util.readLong(file, useCompressedInts); // Metadata ID
		String[] stringPool = new String[Util.readInt(file, useCompressedInts)];
		for (int i = 0; i < stringPool.length; i++) {
			stringPool[i] = Util.readString(file, useCompressedInts, file.readByte());
		}
		String elName = stringPool[Util.readInt(file, useCompressedInts)];
		if (!elName.equals("root")) {
			throw new IOException("Root element is called " + elName + ", something has gone wrong!");
		}
		return readElement(new RootElement(), file, stringPool, useCompressedInts);
	}

	private static <T extends Element> T readElement(T element, RandomAccessFile file, String[] stringPool, boolean useCompressedInts) throws IOException {
		int attributeCount = Util.readInt(file, useCompressedInts);
		for (int i = 0; i < attributeCount; i++) {
			element.setAttribute(stringPool[Util.readInt(file, useCompressedInts)], stringPool[Util.readInt(file, useCompressedInts)]);
		}
		int childCount = Util.readInt(file, useCompressedInts);
		for (int i = 0; i < childCount; i++) {
			int childName = Util.readInt(file, useCompressedInts);
			Element child = element.constructChild(stringPool[childName]);
			if (child != null) {
				readElement(child, file, stringPool, useCompressedInts);
			}
		}
		return element;
	}
}
