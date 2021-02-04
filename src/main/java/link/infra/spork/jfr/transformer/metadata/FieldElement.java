package link.infra.spork.jfr.transformer.metadata;

import java.util.ArrayList;
import java.util.List;

public class FieldElement extends Element {
	public String name;
	// TODO: resolve to a stub ClassElement, or the real one if it exists?
	public long classId;
	public boolean usesConstantPool;
	public int dimension;
	public final List<AnnotationElement> annotations = new ArrayList<>();

	@Override
	public void setAttribute(String name, String value) throws MetadataParsingException {
		switch (name) {
			case "name":
				this.name = value;
				break;
			case "class":
				classId = Long.parseLong(value);
				break;
			case "constantPool":
				usesConstantPool = Boolean.parseBoolean(value);
				break;
			case "dimension":
				dimension = Integer.parseInt(value);
				break;
			default:
				UnknownMetadataException.throwIfStrict("Attempted to set field attribute " + name);
		}
	}

	@Override
	public Element constructChild(String childType) throws MetadataParsingException {
		if ("annotation".equals(childType)) {
			AnnotationElement el = new AnnotationElement();
			annotations.add(el);
			return el;
		}
		return new UnknownElement(childType);
	}
}
