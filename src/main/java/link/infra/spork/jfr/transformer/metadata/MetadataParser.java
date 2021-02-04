package link.infra.spork.jfr.transformer.metadata;

import link.infra.spork.jfr.transformer.TransformerParsingException;
import link.infra.spork.jfr.transformer.Util;

import java.io.IOException;
import java.io.RandomAccessFile;

public class MetadataParser {
	public static RootElement read(RandomAccessFile file, boolean useCompressedInts) throws IOException, TransformerParsingException {
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

	private static <T extends Element> T readElement(T element, RandomAccessFile file, String[] stringPool, boolean useCompressedInts) throws IOException, TransformerParsingException {
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
			} else {
				UnknownMetadataException.throwIfStrict();
			}
		}
		return element;
	}
}
