package link.infra.spork.jfr.transformer.metadata;

public class RootElement extends Element {
	public MetadataElement metadata;
	public RegionElement region;

	@Override
	public void setAttribute(String name, String value) throws MetadataParsingException {
		UnknownMetadataException.throwIfStrict("Attempted to set root attribute " + name);
	}

	@Override
	public Element constructChild(String childType) throws MetadataParsingException {
		if ("metadata".equals(childType)) {
			metadata = new MetadataElement();
			return metadata;
		}
		if ("region".equals(childType)) {
			region = new RegionElement();
			return region;
		}
		return new UnknownElement(childType);
	}
}
