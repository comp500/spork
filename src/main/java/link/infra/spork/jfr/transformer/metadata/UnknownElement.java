package link.infra.spork.jfr.transformer.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnknownElement extends Element {
	protected Map<String, String> attributes = new HashMap<>();
	protected List<Element> children = new ArrayList<>();
	public final String childType;

	public UnknownElement(String childType) throws MetadataParsingException {
		UnknownMetadataException.throwIfStrict("Attempted to create element child type " + childType);
		this.childType = childType;
	}

	@Override
	public void setAttribute(String name, String value) {
		attributes.put(name, value);
	}

	@Override
	public Element constructChild(String childType) throws MetadataParsingException {
		Element child = new UnknownElement(childType);
		children.add(child);
		return child;
	}
}
