package link.infra.spork.jfr.transformer.metadata;

public class RegionElement extends Element {
	public String locale;
	public int gmtOffset;

	@Override
	public void setAttribute(String name, String value) throws MetadataParsingException {
		switch (name) {
			case "locale":
				this.locale = value;
				break;
			case "gmtOffset":
				this.gmtOffset = Integer.parseInt(value);
				break;
			default:
				UnknownMetadataException.throwIfStrict("Attempted to set region attribute " + name);
		}
	}

	@Override
	public Element constructChild(String childType) throws MetadataParsingException {
		return new UnknownElement(childType);
	}
}
