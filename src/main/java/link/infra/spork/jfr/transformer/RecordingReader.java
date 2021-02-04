package link.infra.spork.jfr.transformer;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RecordingReader {
	// TODO: inline?
	public static void read(RandomAccessFile file) throws IOException {
		while (file.getFilePointer() < file.length()) {
			ChunkParser.read(file);
		}
	}
}
