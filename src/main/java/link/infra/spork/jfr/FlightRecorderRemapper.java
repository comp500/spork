package link.infra.spork.jfr;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import jdk.management.jfr.FlightRecorderMXBean;
import link.infra.spork.mappings.MappingsStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FlightRecorderRemapper {
	private final MappingsStore mappings;
	private final Long2ObjectOpenHashMap<FileChannel> channels = new Long2ObjectOpenHashMap<>();
	private final ThreadLocal<ByteBuffer> buf = ThreadLocal.withInitial(() -> ByteBuffer.allocate(50000));
	private final Logger LOGGER = LogManager.getLogger();

	public FlightRecorderRemapper(MappingsStore mappings) {
		this.mappings = mappings;
	}

	public void closeStream(long streamId) throws IOException {
		if (channels.containsKey(streamId)) {
			channels.get(streamId).close();
		}
	}

	public byte[] readStream(long streamId, FlightRecorderMXBean delegate) throws IOException {
		if (!channels.containsKey(streamId)) {
			try {
				Path tempFile = Files.createTempFile("spork-jfr-remap-", ".jfr");
				LOGGER.info("Created temp file for JFR remapping: " + tempFile);
				FileChannel chan = FileChannel.open(tempFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
				channels.put(streamId, chan);
				byte[] unmappedBuf;
				while ((unmappedBuf = delegate.readStream(streamId)) != null) {
					chan.write(ByteBuffer.wrap(unmappedBuf));
				}
				// TODO: read from a different file
				chan.position(0);
				LOGGER.info("Unmapped file has been fully written!");
			} catch (IOException e) {
				LOGGER.error("Failed to remap file", e);
				throw e;
			}
		}
		ByteBuffer bBuf = buf.get();
		int n = channels.get(streamId).read(bBuf);
		if (n == -1) {
			return null;
		}
		byte[] retBuf = new byte[n];
		bBuf.get(retBuf);
		return retBuf;
	}
}
