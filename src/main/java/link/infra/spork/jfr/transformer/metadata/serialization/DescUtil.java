package link.infra.spork.jfr.transformer.metadata.serialization;

final class DescUtil {
	private DescUtil() {}

	public static long parseLong(String value, String fieldName) throws DescriptionParsingException {
		DescriptionParsingException.ensureNotNull(value, "Could not find value for " + fieldName);
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new DescriptionParsingException("Invalid long value of " + fieldName + ": " + value);
		}
	}

	public static int parseInt(String value, String fieldName) throws DescriptionParsingException {
		DescriptionParsingException.ensureNotNull(value, "Could not find value for " + fieldName);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new DescriptionParsingException("Invalid int value of " + fieldName + ": " + value);
		}
	}

	public static int parseIntOptional(String value, String fieldName, int defaultValue) throws DescriptionParsingException {
		if (value == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new DescriptionParsingException("Invalid int value of " + fieldName + ": " + value);
		}
	}

	public static boolean parseBoolean(String value, String fieldName) throws DescriptionParsingException {
		DescriptionParsingException.ensureNotNull(value, "Could not find value for " + fieldName);
		return Boolean.parseBoolean(value);
	}
}
