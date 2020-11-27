package link.infra.spork.jfr.transformer;

import link.infra.spork.jfr.transformer.binpatch.BinaryPatcher;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RecordingReader {
	// TODO: inline?
	public static void read(RandomAccessFile file, BinaryPatcher patcher) throws IOException {
		while (file.getFilePointer() < file.length()) {
			ChunkParser.read(file, patcher);
		}
	}
}
