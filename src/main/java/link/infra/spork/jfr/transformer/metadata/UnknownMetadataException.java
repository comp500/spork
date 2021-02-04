package link.infra.spork.jfr.transformer.metadata;

import link.infra.spork.jfr.transformer.Test;

public class UnknownMetadataException extends MetadataParsingException {
	public UnknownMetadataException() {
		super();
	}

	public UnknownMetadataException(String desc) {
		super(desc);
	}

	public static void throwIfStrict() throws MetadataParsingException {
		if (Test.strict) {
			throw new UnknownMetadataException();
		}
	}

	public static void throwIfStrict(String desc) throws MetadataParsingException {
		if (Test.strict) {
			throw new UnknownMetadataException(desc);
		}
	}
}
