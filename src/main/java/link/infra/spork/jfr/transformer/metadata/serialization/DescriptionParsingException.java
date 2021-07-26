package link.infra.spork.jfr.transformer.metadata.serialization;

import link.infra.spork.jfr.transformer.TransformerParsingException;

public class DescriptionParsingException extends TransformerParsingException {
	public DescriptionParsingException() {
		super();
	}

	public DescriptionParsingException(String desc) {
		super(desc);
	}

	public static <T> T ensureNotNull(T o, String desc) throws DescriptionParsingException {
		if (o == null) {
			throw new DescriptionParsingException();
		}
		return o;
	}
}
