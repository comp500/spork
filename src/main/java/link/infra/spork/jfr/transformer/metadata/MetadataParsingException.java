package link.infra.spork.jfr.transformer.metadata;

import link.infra.spork.jfr.transformer.TransformerParsingException;

public class MetadataParsingException extends TransformerParsingException {
	public MetadataParsingException() {
		super();
	}

	public MetadataParsingException(String desc) {
		super(desc);
	}
}
