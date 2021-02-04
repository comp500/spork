package link.infra.spork.jfr.transformer.metadata;

import java.util.ArrayList;
import java.util.List;

public class MetadataElement extends Element {
	public List<ClassElement> classes = new ArrayList<>();

	@Override
	public void setAttribute(String name, String value) throws MetadataParsingException {
		UnknownMetadataException.throwIfStrict("Attempted to set metadata attribute " + name);
	}

	@Override
	public Element constructChild(String childType) throws MetadataParsingException {
		if ("class".equals(childType)) {
			ClassElement el = new ClassElement();
			classes.add(el);
			return el;
		}
		return new UnknownElement(childType);
	}
}
