package link.infra.spork.jfr.transformer.metadata.serialization;

public class JFRClass implements JFRType<JFRValue<?, ?>[]> {
	public String name;
	public String superType;
	public boolean simple;
	// TODO: fields, annotations, settings?

	@Override
	public JFRValue<?, ?>[] deserialize() {
		return new JFRValue[0];
	}

	@Override
	public void serialize(JFRValue<?, ?>[] value) {

	}

	@Override
	public void readDescription(JFRTypeDescription description, JFRTypeLookup lookup) throws DescriptionParsingException {
		name = description.attributes.get("name");
		superType = description.attributes.get("superType");
		simple = Boolean.parseBoolean(description.attributes.get("simpleType"));
		// TODO: more fields
	}

	@Override
	public JFRTypeDescription describeType() {
		// TODO: impl
		return null;
	}
}
