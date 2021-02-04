package link.infra.spork.jfr.transformer;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class ConstantPool {
	private final Long2ObjectMap<TypeHandler<?>> handlers = new Long2ObjectOpenHashMap<>();

	// TODO: clean up
	public static class ResolvableData<T> {
		private final T value;
		private final long offset;
		private final TypeHandler<T> typeHandler;
		private final long reference;

		public ResolvableData(T value, long offset) {
			this.value = value;
			this.offset = offset;
			this.typeHandler = null;
			this.reference = -1;
		}

		public ResolvableData(TypeHandler<T> handler, long reference) {
			this.typeHandler = handler;
			this.reference = reference;
			this.value = null;
			this.offset = -1;
		}

		public T get() {
			if (value != null) {
				return value;
			} else {
				if (typeHandler != null) {
					return typeHandler.constants.get(reference);
				}
				return null;
			}
		}
	}

	private static class TypeHandler<T> {
		public final MetadataParser.ClassElement classMetadata;
		public final PoolElementParser<T> parser;
		public final Long2ObjectMap<T> constants = new Long2ObjectOpenHashMap<>();

		private TypeHandler(MetadataParser.ClassElement classMetadata, PoolElementParser<T> parser) {
			this.classMetadata = classMetadata;
			this.parser = parser;
		}
	}

	private interface PoolElementParser<T> {
		T parse(RandomAccessFile file, boolean useCompressedInts, Long2ObjectMap<TypeHandler<?>> handlers) throws IOException;
	}

	public ConstantPool(List<MetadataParser.ClassElement> classList) {
		for (MetadataParser.ClassElement el : classList) {
			handlers.put(el.id, createHandler(el));
		}
	}

	private static TypeHandler<?> createHandler(MetadataParser.ClassElement el) {
		// TODO: handle class/method/symbol/package? specially
		if (el.fields.size() == 1 && el.simple) {
			// Parse only the first field
			if (el.fields.get(0).usesConstantPool) {
				throw new RuntimeException("This wasn't supposed to happen!!");
			}
			return new TypeHandler<>(el, (file, useCompressedInts, handlers) ->
				handlers.get(el.fields.get(0).classId).parser.parse(file, useCompressedInts, handlers));
		} else if (el.fields.isEmpty() && el.superType == null) {
			switch (el.name) {
				case "java.lang.String":
					return new TypeHandler<>(el, (file, useCompressedInts, handlers) -> {
						long offset = file.getFilePointer();
						byte encoding = file.readByte();
						if (encoding == Util.STRING_TYPE_CONSTANT_POOL) {
							return new ResolvableData<>(handlers.get(el.id), Util.readLong(file, useCompressedInts));
						} else {
							return new ResolvableData<>(Util.readString(file, useCompressedInts, encoding), offset);
						}
					});
				case "boolean":
					return new TypeHandler<>(el, (file, useCompressedInts, handlers) -> file.readBoolean());
				case "byte":
					return new TypeHandler<>(el, (file, useCompressedInts, handlers) -> file.readByte());
				case "short":
					return new TypeHandler<>(el, (file, useCompressedInts, handlers) -> Util.readShort(file, useCompressedInts));
				case "char":
					return new TypeHandler<>(el, (file, useCompressedInts, handlers) -> Util.readChar(file, useCompressedInts));
				case "int":
					return new TypeHandler<>(el, (file, useCompressedInts, handlers) -> Util.readInt(file, useCompressedInts));
				case "long":
					return new TypeHandler<>(el, (file, useCompressedInts, handlers) -> Util.readLong(file, useCompressedInts));
				case "float":
					return new TypeHandler<>(el, (file, useCompressedInts, handlers) -> file.readFloat());
				case "double":
					return new TypeHandler<>(el, (file, useCompressedInts, handlers) -> file.readDouble());
				default:
					throw new RuntimeException("Failed to create parser for field " + el.name);
			}
		} else {
			return new TypeHandler<>(el, (file, useCompressedInts, handlers) -> {
				Object[] results = new Object[el.fields.size()];
				// TODO: make finding parsers more efficient?
				for (int i = 0; i < el.fields.size(); i++) {
					MetadataParser.FieldElement field = el.fields.get(i);
					if (field.usesConstantPool) {
						results[i] = new ResolvableData<>(handlers.get(field.classId), Util.readLong(file, useCompressedInts));
					} else {
						results[i] = handlers.get(field.classId).parser.parse(file, useCompressedInts, handlers);
					}
				}
				return results;
			});
		}
	}

	private <T> void readConstants(TypeHandler<T> handler, RandomAccessFile file, boolean useCompressedInts) throws IOException {
		int constantCount = Util.readInt(file, useCompressedInts);
		for (int j = 0; j < constantCount; j++) {
			long constantIndex = Util.readLong(file, useCompressedInts);
			T value = handler.parser.parse(file, useCompressedInts, handlers);
			handler.constants.put(constantIndex, value);
		}
		System.out.println("Stored " + constantCount + " of " + handler.classMetadata.name);
	}

	public long read(RandomAccessFile file, boolean useCompressedInts) throws IOException {
		Util.readInt(file, useCompressedInts); // Event size
		if (Util.readLong(file, useCompressedInts) != 1) {
			throw new IOException("Invalid constant pool event");
		}
		Util.readLong(file, useCompressedInts); // Start time
		Util.readLong(file, useCompressedInts); // Duration
		long delta = Util.readLong(file, useCompressedInts);
		file.readByte(); // Flags
		int typeCount = Util.readInt(file, useCompressedInts);
		for (int i = 0; i < typeCount; i++) {
			long classId = Util.readLong(file, useCompressedInts);
			TypeHandler<?> handler = handlers.get(classId);
			readConstants(handler, file, useCompressedInts);
		}

		return delta;
	}

	// TODO: expose this cleaner
	public Object parseValue(long typeId, RandomAccessFile file, boolean useCompressedInts) throws IOException {
		TypeHandler<?> handler = handlers.get(typeId);
		return handler.parser.parse(file, useCompressedInts, handlers);
	}
}
