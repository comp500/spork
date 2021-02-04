package link.infra.spork.jfr.transformer;

import link.infra.spork.jfr.transformer.metadata.ClassElement;
import link.infra.spork.jfr.transformer.metadata.FieldElement;
import link.infra.spork.jfr.transformer.metadata.MetadataParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

public class ChunkParser {
	private static final byte[] MAGIC = "FLR\0".getBytes();
	private static final short MAJOR_VERSION = 2;
	private static final short MINOR_VERSION = 1;

	private static final Logger LOGGER = LogManager.getLogger();

	public static class ChunkHeader {
		public long startPosition;

		// Magic bytes == FLR and null byte
		// Major version == 2
		// Minor version ~= 1
		public long chunkSize;
		public long constantPoolOffset;
		public long metadataOffset;
		public long startNanos;
		public long durationNanos;
		public long startTicks;
		public long ticksPerSecond;
		public int features;

		public boolean useCompressedInts;
	}

	public static void read(RandomAccessFile file) throws IOException, TransformerParsingException {
		ChunkHeader header = new ChunkHeader();
		header.startPosition = file.getFilePointer();
		byte[] magicRead = new byte[4];
		file.readFully(magicRead);
		if (!Arrays.equals(magicRead, MAGIC)) {
			LOGGER.fatal("Failed to read JFR file, magic bytes not found in " + Arrays.toString(magicRead));
			return;
		}
		short ver = file.readShort();
		if (ver != MAJOR_VERSION) {
			LOGGER.fatal("Failed to read JFR file, major version " + ver + " not supported!");
			return;
		}
		ver = file.readShort();
		if (ver != MINOR_VERSION) {
			LOGGER.warn("Minor version " + ver + " may not work correctly!");
		}
		header.chunkSize = file.readLong();
		header.constantPoolOffset = file.readLong();
		header.metadataOffset = file.readLong();
		header.startNanos = file.readLong();
		header.durationNanos = file.readLong();
		header.startTicks = file.readLong();
		header.ticksPerSecond = file.readLong();
		header.features = file.readInt();
		header.useCompressedInts = (header.features & 1) != 0;

		file.seek(header.startPosition + header.metadataOffset);
		List<ClassElement> classes = MetadataParser.read(file, header.useCompressedInts).metadata.classes;

		ConstantPool pool = new ConstantPool(classes);
		long currentDelta = header.constantPoolOffset;
		long currentConstantPoolPosition = header.startPosition;
		// TODO: parse first constant pool, then all the events between 0 and constant pool, then second constant pool, etc.?
		while (currentDelta != 0) {
			currentConstantPoolPosition += currentDelta;
			file.seek(currentConstantPoolPosition);
			currentDelta = pool.read(file, header.useCompressedInts);
		}

		// Seek to the start of the chunk
		file.seek(header.startPosition + 68);

		while (file.getFilePointer() < header.startPosition + header.chunkSize) {
			long startPos = file.getFilePointer();
			int size = Util.readInt(file, header.useCompressedInts);
			long typeId = Util.readLong(file, header.useCompressedInts);

			if (typeId == 0) {
				System.out.println("Read metadata event");
				file.seek(startPos + size);
			} else if (typeId == 1) {
				System.out.println("Read constant pool event");
				file.seek(startPos + size);
			} else {
				ClassElement classElement = null;
				for (ClassElement classEl : classes) {
					if (classEl.id == typeId) {
						classElement = classEl;
					}
				}
				if (classElement == null) {
					System.out.println("Read unknown event " + typeId);
					file.seek(startPos + size);
				}
				System.out.println("Read event " + typeId + " " + classElement.name);

				Object value = pool.parseValue(typeId, file, header.useCompressedInts);

				if (value instanceof Object[]) {
					Object[] arr = (Object[]) value;
					for (int i = 0; i < classElement.fields.size(); i++) {
						FieldElement fieldEl = classElement.fields.get(i);
						Object value2 = arr[i];
						if (value2 instanceof ConstantPool.ResolvableData<?>) {
							System.out.println(fieldEl.name + ": " + ((ConstantPool.ResolvableData<?>) value2).get());
						} else {
							System.out.println(fieldEl.name + ": " + value2);
						}
					}
				} else {
					if (value instanceof ConstantPool.ResolvableData<?>) {
						System.out.println(((ConstantPool.ResolvableData<?>) value).get());
					} else {
						System.out.println(value);
					}
				}
			}
		}

		// Seek to the end of the chunk
		file.seek(header.startPosition + header.chunkSize);
	}
}
