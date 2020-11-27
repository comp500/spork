package link.infra.spork.jfr.transformer.binpatch;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.function.Supplier;

public class BinaryPatcher {
	private final List<Patch> patches = new ArrayList<>();
	private final List<SerializedOffsetReference> offsetReferences = new ArrayList<>();
	private final List<SerializedLengthReference> lengthReferences = new ArrayList<>();
	private final List<Repositionable> references = new ArrayList<>();
	private long sizeDelta = -1;
	
	private interface Repositionable {
		long getCurrentPosition();
		void setNewPosition(long position);
	}

	public static class SerializedOffsetReference {
		private final long sourceReferenceOffset;
		private final int sourceReferenceLength;
		private final long sourceOffset;
		private final Serializer serializer;
		private long destOffset;
		private long destReferenceOffset;

		private SerializedOffsetReference(long sourceReferenceOffset, int sourceReferenceLength, long sourceOffset, Serializer serializer) {
			this.sourceReferenceOffset = sourceReferenceOffset;
			this.sourceReferenceLength = sourceReferenceLength;
			this.sourceOffset = sourceOffset;
			this.serializer = serializer;
		}

		public interface Serializer {
			byte[] getReferenceData(long referenceOffset, long offset);
		}

		Repositionable reposReferenceOffset() {
			return new Repositionable() {
				@Override
				public long getCurrentPosition() {
					return sourceReferenceOffset;
				}

				@Override
				public void setNewPosition(long position) {
					destReferenceOffset = position;
				}
			};
		}

		Repositionable reposOffset() {
			return new Repositionable() {
				@Override
				public long getCurrentPosition() {
					return sourceOffset;
				}

				@Override
				public void setNewPosition(long position) {
					destOffset = position;
				}
			};
		}

		void writePatch(BinaryPatcher patchConsumer) {
			patchConsumer.addPatch(sourceReferenceOffset, sourceReferenceLength, serializer.getReferenceData(destReferenceOffset, destOffset));
		}
	}

	public static class SerializedLengthReference {
		private final long sourceReferenceOffset;
		private final int sourceReferenceLength;
		private final long sourceOffset;
		private final long sourceLength;
		private final Serializer serializer;
		private long destOffset;
		private long destOffsetEnd;

		public SerializedLengthReference(long sourceReferenceOffset, int sourceReferenceLength, long sourceOffset, long sourceLength, Serializer serializer) {
			this.sourceReferenceOffset = sourceReferenceOffset;
			this.sourceReferenceLength = sourceReferenceLength;
			this.sourceOffset = sourceOffset;
			this.sourceLength = sourceLength;
			this.serializer = serializer;
		}

		public interface Serializer {
			byte[] getLengthData(long length);
		}

		Repositionable reposOffset() {
			return new Repositionable() {
				@Override
				public long getCurrentPosition() {
					return sourceOffset;
				}

				@Override
				public void setNewPosition(long position) {
					destOffset = position;
				}
			};
		}

		Repositionable reposOffsetEnd() {
			return new Repositionable() {
				@Override
				public long getCurrentPosition() {
					return sourceOffset + sourceLength;
				}

				@Override
				public void setNewPosition(long position) {
					destOffsetEnd = position;
				}
			};
		}

		void writePatch(BinaryPatcher patchConsumer) {
			patchConsumer.addPatch(sourceReferenceOffset, sourceReferenceLength, serializer.getLengthData(destOffsetEnd - destOffset));
		}
	}

	private static class Patch {
		private final long offset;
		private final int origLength;
		private final byte[] bytes;
		private long newOffset;

		private Patch(long offset, int origLength, byte[] bytes) {
			this.offset = offset;
			this.origLength = origLength;
			this.bytes = bytes;
		}
	}

	public void addPatch(long offset, int origLength, byte[] bytes) {
		patches.add(new Patch(offset, origLength, bytes));
	}

	// Note: Offset and length references aren't updated when offset and length reference patches move stuff around!
	// TODO: relative offsets?!!

	public void addOffsetReference(long referenceOffset, int referenceLength, long targetOffset, SerializedOffsetReference.Serializer serializer) {
		SerializedOffsetReference ref = new SerializedOffsetReference(referenceOffset, referenceLength, targetOffset, serializer);
		offsetReferences.add(ref);
		references.add(ref.reposReferenceOffset());
		references.add(ref.reposOffset());
	}

	public void addLengthReference(long referenceOffset, int referenceLength, long targetOffset, long targetCurrentLength, SerializedLengthReference.Serializer serializer) {
		SerializedLengthReference ref = new SerializedLengthReference(referenceOffset, referenceLength, targetOffset, targetCurrentLength, serializer);
		lengthReferences.add(ref);
		references.add(ref.reposOffset());
		references.add(ref.reposOffsetEnd());
	}

	public Supplier<Long> trackPosition(long offset) {
		class TrackedPosition implements Repositionable {
			private final long currentPosition;
			private long newPosition;

			TrackedPosition(long position) {
				currentPosition = position;
				newPosition = position;
			}

			@Override
			public long getCurrentPosition() { return currentPosition; }

			@Override
			public void setNewPosition(long position) { newPosition = position; }
		}

		TrackedPosition pos = new TrackedPosition(offset);
		references.add(pos);
		return () -> pos.newPosition;
	}

	void processPatches() {
		// Sort patches/references by offset
		patches.sort(Comparator.comparingLong(a -> a.offset));
		references.sort(Comparator.comparingLong(Repositionable::getCurrentPosition));

		long currentPositionDelta = 0;
		Iterator<Patch> patchIter = patches.iterator();
		Iterator<Repositionable> referenceIterator = references.iterator();
		while (patchIter.hasNext() || referenceIterator.hasNext()) {
			if (patchIter.hasNext()) {
				Patch patch = patchIter.next();

				if (referenceIterator.hasNext()) {
					Repositionable ref = referenceIterator.next();
					// Check which one is first
					if (patch.offset < ref.getCurrentPosition()) {
						// Shift patch by delta
						patch.newOffset = patch.offset + currentPositionDelta;
						// Update position delta
						currentPositionDelta += (patch.bytes.length - patch.origLength);
						// Shift ref by delta
						ref.setNewPosition(ref.getCurrentPosition() + currentPositionDelta);
					} else {
						// Shift ref by delta
						ref.setNewPosition(ref.getCurrentPosition() + currentPositionDelta);
						// Shift patch by delta
						patch.newOffset = patch.offset + currentPositionDelta;
						// Update position delta
						currentPositionDelta += (patch.bytes.length - patch.origLength);
					}
				} else {
					// Shift patch by delta
					patch.newOffset = patch.offset + currentPositionDelta;
					// Update position delta
					currentPositionDelta += (patch.bytes.length - patch.origLength);
				}
			} else {
				Repositionable ref = referenceIterator.next();
				// Shift ref by delta
				ref.setNewPosition(ref.getCurrentPosition() + currentPositionDelta);
			}
		}
		sizeDelta = currentPositionDelta;
	}

	void processReferenceSerialisation() {
		for (SerializedOffsetReference ref : offsetReferences) {
			ref.writePatch(this);
		}
		for (SerializedLengthReference ref : lengthReferences) {
			ref.writePatch(this);
		}
		// Update patch/reference deltas
		processPatches();
	}

	private static class PatcherByteChannel implements ReadableByteChannel {
		private final Deque<Patch> patchQueue;
		private Patch currentPatch;
		private long currentPosition = 0;
		private final ReadableByteChannel delegate;

		protected PatcherByteChannel(ReadableByteChannel delegate, Deque<Patch> patchQueue) {
			this.patchQueue = patchQueue;
			this.delegate = delegate;
			currentPatch = patchQueue.removeFirst();
		}

		@Override
		public int read(ByteBuffer dst) throws IOException {
			if (patchQueue.isEmpty() && currentPatch == null) {
				// No more patches to apply
				return delegate.read(dst);
			}
			int lengthWritten = 0;
			// While there are more patches to write...
			while (!patchQueue.isEmpty() || currentPatch != null) {
				// Get the next patch
				if (currentPatch == null) {
					currentPatch = patchQueue.removeFirst();
				}

				// If the next bytes are in a patch, write them
				if (currentPosition >= currentPatch.newOffset) {
					int len = Math.min(currentPatch.bytes.length - (int)(currentPosition - currentPatch.newOffset), dst.remaining());
					dst.put(currentPatch.bytes, (int) (currentPosition - currentPatch.newOffset), len);
					// Skip the original length from the delegate stream
					if (currentPosition == currentPatch.newOffset) {
						// Note that this doesn't support overlapping patches
						skip(currentPatch.origLength);
					}
					currentPosition += len;
					lengthWritten += len;
					// If we've reached the end of this patch, set it to null
					if (currentPosition >= (currentPatch.newOffset + currentPatch.bytes.length)) {
						currentPatch = null;
					}
				} else {
					// If the next bytes are not in a patch, write them
					int len = Math.min((int) (currentPatch.newOffset - currentPosition), dst.remaining());
					// Create a bytebuffer with the section we want to write into
					ByteBuffer section = dst.duplicate();
					section.limit(section.position() + len);
					int written = delegate.read(section);
					// If we reach this point, the patch queue isn't empty, so there should be more bytes to read
					// This means that something has gone wrong - the channel is shorter than we expected
					// We just return -1 to indicate end of stream
					if (written == -1) {
						// TODO: log this somewhere?
						return -1;
					}
					// Note that there's no need to copy again - the duplicated byte buffer is a view, but we do need to move the position
					dst.position(dst.position() + written);
					currentPosition += written;
					lengthWritten += written;
					// If the read call returned less than full, return to ensure buffer health (no point repeatedly calling read() if it has no more data yet)
					if (written < len) {
						return lengthWritten;
					}
				}

				// If we've reached the end of the buffer, return
				if (!dst.hasRemaining()) {
					return lengthWritten;
				}
			}
			// Read anything after the last patch
			int finalBytesWritten = delegate.read(dst);
			if (finalBytesWritten == -1) {
				return lengthWritten;
			}
			return lengthWritten + finalBytesWritten;
		}

		private final ByteBuffer skipBuffer = ByteBuffer.allocate(1024);

		private void skip(int bytesToSkip) throws IOException {
			long startPosition = currentPosition;
			while (bytesToSkip > 0) {
				skipBuffer.clear();
				int attempt = Math.min(bytesToSkip, skipBuffer.capacity());
				skipBuffer.limit(attempt);
				int bytesSkipped = delegate.read(skipBuffer);
				if (bytesSkipped < 0) {
					throw new SkipFailureException(currentPosition, startPosition, attempt);
				}
				bytesToSkip -= bytesSkipped;
			}
			skipBuffer.clear();
		}

		private static class SkipFailureException extends IOException {
			public SkipFailureException(long currentPosition, long startPosition, int attempt) {
				super("Failed to skip " + attempt + " bytes, start pos " + startPosition + " curr pos " + currentPosition + ", end of stream reached!");
			}
		}

		@Override
		public boolean isOpen() {
			return delegate.isOpen();
		}

		@Override
		public void close() throws IOException {
			delegate.close();
		}
	}

	public long processPositions(long originalSize) {
		// Phase 1: apply patches
		processPatches();
		// Phase 2: make patches from reference serialisation, apply them again
		processReferenceSerialisation();
		return originalSize + sizeDelta;
	}

	public ReadableByteChannel apply(ReadableByteChannel in) {
		if (sizeDelta == -1) {
			throw new RuntimeException("Run processPositions first!");
		}
		ArrayDeque<Patch> patchQueue = new ArrayDeque<>(patches);
		if (patchQueue.isEmpty()) {
			return in;
		}
		return new PatcherByteChannel(in, patchQueue);
	}
}
