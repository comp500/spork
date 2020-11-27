package link.infra.spork.jfr.transformer;

import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Util {
	private static long readLEB128Long(DataInput file) throws IOException {
		long value = 0;
		for (int i = 0; i < 8; i++) {
			byte currentByte = file.readByte();
			value += (currentByte & 0x7FL) << (7 * i);
			if (currentByte >= 0) {
				return value;
			}
		}
		return value + ((file.readByte() & 0xFFL) << 56);
	}

	private static byte[] getLEB128Long(long value) {
		byte[] out = new byte[8];
		for (int i = 0; i < 8; i++) {
			out[i] = (byte) (value & 0x7FL);
			value >>= 7;
			if ((value == 0 && ((out[i] & 0x40) == 0)) || (value == -1 && ((out[i] & 0x40) != 0))) {
				return Arrays.copyOfRange(out, 0, i + 1);
			} else {
				out[i] |= 0x80;
			}
		}
		return out;
	}

	public static long readLong(DataInput file, boolean compressed) throws IOException {
		return compressed ? readLEB128Long(file) : file.readLong();
	}

	public static int readInt(DataInput file, boolean compressed) throws IOException {
		return compressed ? (int) readLEB128Long(file) : file.readInt();
	}

	public static char readChar(DataInput file, boolean compressed) throws IOException {
		return compressed ? (char) readLEB128Long(file) : file.readChar();
	}

	public static short readShort(DataInput file, boolean compressed) throws IOException {
		return compressed ? (short) readLEB128Long(file) : file.readShort();
	}

	public static byte[] getLongBytes(long value, boolean compressed) {
		if (compressed) {
			return getLEB128Long(value);
		}
		return new byte[] {
			(byte) (value >> 56),
			(byte) (value >> 48),
			(byte) (value >> 40),
			(byte) (value >> 32),
			(byte) (value >> 24),
			(byte) (value >> 16),
			(byte) (value >> 8),
			(byte) value
		};
	}

	public static byte[] getIntBytes(int value, boolean compressed) {
		if (compressed) {
			return getLEB128Long(value);
		}
		return new byte[] {
			(byte) (value >> 24),
			(byte) (value >> 16),
			(byte) (value >> 8),
			(byte) value
		};
	}

	public static byte[] getCharBytes(char value, boolean compressed) {
		if (compressed) {
			return getLEB128Long(value);
		}
		return new byte[] {
			(byte) (value >> 8),
			(byte) value
		};
	}

	public static byte[] getShortBytes(short value, boolean compressed) {
		if (compressed) {
			return getLEB128Long(value);
		}
		return new byte[] {
			(byte) (value >> 8),
			(byte) value
		};
	}

	static final byte STRING_TYPE_NULL = 0;
	static final byte STRING_TYPE_EMPTY = 1;
	static final byte STRING_TYPE_CONSTANT_POOL = 2;
	static final byte STRING_TYPE_UTF8_BYTE_ARRAY = 3;
	static final byte STRING_TYPE_CHAR_ARRAY = 4;
	static final byte STRING_TYPE_LATIN1_BYTE_ARRAY = 5;

	public static String readString(DataInput file, boolean compressed, byte encoding) throws IOException {
		byte[] byteArr;
		char[] charArr;
		switch (encoding) {
			case STRING_TYPE_NULL:
				return null;
			case STRING_TYPE_EMPTY:
				return "";
			case STRING_TYPE_UTF8_BYTE_ARRAY:
				byteArr = new byte[readInt(file, compressed)];
				file.readFully(byteArr);
				return new String(byteArr, StandardCharsets.UTF_8);
			case STRING_TYPE_CHAR_ARRAY:
				charArr = new char[readInt(file, compressed)];
				for (int i = 0; i < charArr.length; i++) {
					charArr[i] = readChar(file, compressed);
				}
				return new String(charArr);
			case STRING_TYPE_LATIN1_BYTE_ARRAY:
				byteArr = new byte[readInt(file, compressed)];
				file.readFully(byteArr);
				return new String(byteArr, StandardCharsets.ISO_8859_1);
			default:
				throw new IOException("String type " + encoding + " not supported");
		}
	}
}
