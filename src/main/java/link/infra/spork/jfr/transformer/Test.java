package link.infra.spork.jfr.transformer;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Test {
	public static void test() throws IOException {
		try (RandomAccessFile file = new RandomAccessFile("C:\\Users\\comp500\\Documents\\Gaming\\Minecraft\\Modding\\Misc\\jfrksy\\spork-jfr-remap-10278554402008797328.jfr", "r")) {
			while (file.getFilePointer() < file.length()) {
				ChunkParser.read(file);
			}
		}
	}
}
