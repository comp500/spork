package link.infra.spork.jfr.transformer.metadata;

public class SettingElement extends Element {
	public String name;
	public String value;
	// TODO: resolve to a stub ClassElement, or the real one if it exists?
	public long classId;
	public String defaultValue;

	@Override
	public void setAttribute(String name, String value) throws MetadataParsingException {
		switch (name) {
			case "name":
				this.name = value;
				break;
			case "value":
				this.value = value;
				break;
			case "class":
				classId = Long.parseLong(value);
				break;
			case "defaultValue":
				this.defaultValue = value;
				break;
			default:
				UnknownMetadataException.throwIfStrict("Attempted to set setting attribute " + name);
		}
	}

	@Override
	public Element constructChild(String childType) throws MetadataParsingException {
		return new UnknownElement(childType);
	}
}
