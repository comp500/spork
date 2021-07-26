package link.infra.spork.jfr.transformer.metadata.serialization;

import link.infra.spork.jfr.transformer.Test;

public class UnknownDescriptionDataException extends DescriptionParsingException {
	public UnknownDescriptionDataException() {
		super();
	}

	public UnknownDescriptionDataException(String desc) {
		super(desc);
	}

	public static void throwIfStrict() throws DescriptionParsingException {
		if (Test.strict) {
			throw new UnknownDescriptionDataException();
		}
	}

	public static void throwIfStrict(String desc) throws DescriptionParsingException {
		if (Test.strict) {
			throw new UnknownDescriptionDataException(desc);
		}
	}
}
