package link.infra.spork.jfr.transformer.metadata;

public abstract class Element {
	public abstract void setAttribute(String name, String value) throws MetadataParsingException;

	public abstract Element constructChild(String childType) throws MetadataParsingException;
}
