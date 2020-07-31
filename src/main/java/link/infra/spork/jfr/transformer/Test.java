package link.infra.spork.jfr.transformer;

import link.infra.spork.jfr.transformer.binpatch.BinaryPatcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Test {
	public static void test() throws IOException {
		byte[] input = "..very cool text that should not be overwritten\r\n".getBytes();
		ByteArrayInputStream bais = new ByteArrayInputStream(input);
		BinaryPatcher patcher = new BinaryPatcher();
		patcher.addPatch(2, 9, "Wow this is".getBytes());
		patcher.addLengthReference(0, 2, 2, 9,
			(length) -> Integer.toString(length).getBytes());
//		patcher.addOffsetReference(16, 2,5,
//			(referenceOffset, offset) -> Integer.toString(offset).getBytes());
		int outputLength = patcher.processPositions(input.length);
		try (FileChannel chan = FileChannel.open(Paths.get("test.txt"), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			chan.transferFrom(patcher.apply(Channels.newChannel(bais)), 0, outputLength);
		}
	}
}
