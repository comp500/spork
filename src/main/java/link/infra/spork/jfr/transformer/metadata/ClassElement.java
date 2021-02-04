package link.infra.spork.jfr.transformer.metadata;

import java.util.ArrayList;
import java.util.List;

public class ClassElement extends Element {
	public long id;
	public String name;
	public String superType;
	public boolean simple;
	public final List<FieldElement> fields = new ArrayList<>();
	public final List<AnnotationElement> annotations = new ArrayList<>();
	public final List<SettingElement> settings = new ArrayList<>();

	@Override
	public void setAttribute(String name, String value) throws MetadataParsingException {
		switch (name) {
			case "id":
				id = Long.parseLong(value);
				break;
			case "name":
				this.name = value;
				break;
			case "superType":
				superType = value;
				break;
			case "simpleType":
				simple = Boolean.parseBoolean(value);
				break;
			default:
				UnknownMetadataException.throwIfStrict("Attempted to set class attribute " + name);
		}
	}

	@Override
	public Element constructChild(String childType) throws MetadataParsingException {
		if ("field".equals(childType)) {
			FieldElement el = new FieldElement();
			fields.add(el);
			return el;
		}
		if ("annotation".equals(childType)) {
			AnnotationElement el = new AnnotationElement();
			annotations.add(el);
			return el;
		}
		if ("setting".equals(childType)) {
			SettingElement el = new SettingElement();
			settings.add(el);
			return el;
		}
		return new UnknownElement(childType);
	}
}
