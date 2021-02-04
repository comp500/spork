package link.infra.spork.jfr.transformer;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Test {
	public static void test() throws IOException {

		long fileSize;
		try (RandomAccessFile file = new RandomAccessFile("C:\\Users\\comp500\\Documents\\Gaming\\Minecraft\\Modding\\Misc\\jfrksy\\spork-jfr-remap-10278554402008797328.jfr", "r")) {
			RecordingReader.read(file);
			fileSize = file.length();
		}
//		patcher.processPositions(fileSize);
//		try (ReadableByteChannel chan = Files.newByteChannel(Paths.get("C:\\Users\\comp500\\Documents\\Gaming\\Minecraft\\Modding\\Misc\\jfrksy\\spork-jfr-remap-10278554402008797328.jfr"))) {
//			Files.copy(Channels.newInputStream(patcher.apply(chan)), Paths.get("C:\\Users\\comp500\\Documents\\Gaming\\Minecraft\\Modding\\Misc\\jfrksy\\sporkyes.jfr"));
//		}
	}
}
