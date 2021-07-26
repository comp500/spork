package link.infra.spork.jfr.transformer.metadata.serialization;

import link.infra.spork.jfr.transformer.Util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class MetadataParser {
	public static JFRMetadata read(RandomAccessFile file, boolean useCompressedInts) throws DescriptionParsingException, IOException {
		JFRMetadata meta = new JFRMetadata();

		Util.readInt(file, useCompressedInts); // Event size
		if (Util.readLong(file, useCompressedInts) != 0) {
			throw new DescriptionParsingException("Invalid metadata event");
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
			throw new DescriptionParsingException("Root element is called " + elName + ", invalid metadata event");
		}

		JFRTypeDescription desc = readTypeDesc(file, stringPool, useCompressedInts);
		meta.read(desc);

		return meta;
	}

	private static JFRTypeDescription readTypeDesc(RandomAccessFile file, String[] stringPool, boolean useCompressedInts) throws IOException {
		JFRTypeDescription desc = new JFRTypeDescription();
		int attributeCount = Util.readInt(file, useCompressedInts);
		for (int i = 0; i < attributeCount; i++) {
			desc.attributes.put(stringPool[Util.readInt(file, useCompressedInts)], stringPool[Util.readInt(file, useCompressedInts)]);
		}
		int childCount = Util.readInt(file, useCompressedInts);
		for (int i = 0; i < childCount; i++) {
			int childName = Util.readInt(file, useCompressedInts);
			List<JFRTypeDescription> childList = desc.children.computeIfAbsent(stringPool[childName], k -> new ArrayList<>());
			JFRTypeDescription childDesc = readTypeDesc(file, stringPool, useCompressedInts);
			childList.add(childDesc);
		}
		return desc;
	}
}
