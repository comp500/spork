package link.infra.spork.jfr.transformer.metadata.serialization;

public interface JFRType<V> {
	V deserialize();
	void serialize(V value);

	void readDescription(JFRTypeDescription description, JFRTypeLookup lookup) throws DescriptionParsingException;
	JFRTypeDescription describeType();
}
