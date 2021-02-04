package link.infra.spork.jfr.transformer.metadata;

import java.util.ArrayList;
import java.util.List;

public class AnnotationElement extends Element {
	// TODO: resolve to a stub ClassElement, or the real one if it exists?
	public long classId;
	public List<String> values = new ArrayList<>();

	@Override
	public void setAttribute(String name, String value) throws MetadataParsingException {
		if (name.startsWith("value-")) {
			int i;
			try {
				i = Integer.parseInt(name.substring(6));
			} catch (NumberFormatException e) {
				throw new MetadataParsingException("Failed to parse annotation attribute: " + name);
			}
			while (values.size() <= i) {
				values.add(null);
			}
			if (values.get(i) != null) {
				throw new MetadataParsingException("Attempted to reassign a numbered annotation value");
			}
			values.set(i, value);
		}
		switch (name) {
			case "class":
				classId = Long.parseLong(value);
				break;
			case "value":
				if (this.values.size() != 0) {
					throw new MetadataParsingException("Attempted to assign a value when there are already numbered values");
				}
				this.values.add(value);
				break;
			default:
				System.out.println("Annotation attr " + name + " : " + value);
				//UnknownMetadataException.throwIfStrict("Attempted to set annotation attribute " + name);
		}
	}

	@Override
	public Element constructChild(String childType) throws MetadataParsingException {
		return new UnknownElement(childType);
	}
}
