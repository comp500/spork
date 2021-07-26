package link.infra.spork.jfr.transformer.metadata.serialization;

import java.util.*;

public final class JFRMetadata implements JFRTypeLookup {
	public Region region = null;

	public static class Region {
		public int gmtOffset;
		public String locale;

		private void read(JFRTypeDescription desc) throws DescriptionParsingException {
			gmtOffset = DescUtil.parseIntOptional(desc.attributes.get("gmtOffset"), "gmtOffset", 0);
			locale = desc.attributes.get("locale");
		}

		private void write(JFRTypeDescription desc) {
			desc.attributes.put("gmtOffset", Integer.toString(gmtOffset));
			if (locale != null) {
				desc.attributes.put("locale", locale);
			}
		}
	}


	public void read(JFRTypeDescription desc) throws DescriptionParsingException {
		List<JFRTypeDescription> regionChildren = desc.children.get("region");
		if (regionChildren != null) {
			JFRTypeDescription regionDesc = regionChildren.get(0);
			if (regionDesc != null) {
				region = new Region();
				region.read(regionDesc);
			}
		}
		JFRTypeDescription metadataDesc = DescriptionParsingException.ensureNotNull(
			DescriptionParsingException.ensureNotNull(desc.children.get("metadata"), "No metadata children found").get(0), "No metadata children found");

		List<JFRTypeDescription> classes = DescriptionParsingException.ensureNotNull(metadataDesc.children.get("class"), "No class data found");
		for (JFRTypeDescription classDesc : classes) {
			long classId = DescUtil.parseLong(classDesc.attributes.get("id"), "Class ID");
			JFRClass jfrClass = classIdMap.computeIfAbsent(classId, k -> new JFRClass());
			jfrClass.readDescription(classDesc, this);
		}

		markParseComplete();
	}

	public void write(JFRTypeDescription desc) {
		if (region != null) {
			JFRTypeDescription regionDesc = new JFRTypeDescription();
			region.write(regionDesc);
			desc.children.put("region", Collections.singletonList(regionDesc));
		}

		JFRTypeDescription metadataDesc = new JFRTypeDescription();
		List<JFRTypeDescription> classesList = new ArrayList<>();
		for (Map.Entry<Long, JFRClass> classEntry : classIdMap.entrySet()) {
			JFRTypeDescription classDesc = classEntry.getValue().describeType();
			classDesc.attributes.put("id", Long.toString(classEntry.getKey()));
			classesList.add(classDesc);
		}
		metadataDesc.children.put("class", classesList);

		desc.children.put("metadata", Collections.singletonList(metadataDesc));
	}

	private final Map<Long, JFRClass> classIdMap = new HashMap<>();
	private boolean parseComplete = false;

	private static final String DUMMY_NAME_POISON = "invalid.*JFRDummyClass*";

	private void markParseComplete() throws DescriptionParsingException {
		// Check for dummy classes in the completed parsing output
		for (Map.Entry<Long, JFRClass> entry : classIdMap.entrySet()) {
			if (entry.getValue().name.equals(DUMMY_NAME_POISON)) {
				throw new DescriptionParsingException("Class ID " + entry.getKey() + " referenced, but not found in metadata event");
			}
		}
		parseComplete = true;
	}

	@Override
	public JFRClass lookupClass(long classID) {
		JFRClass jfrClass = classIdMap.get(classID);
		if (jfrClass != null) {
			return jfrClass;
		}
		if (parseComplete) {
			// TODO: something propagatable?
			throw new RuntimeException("Failed to find class from ID");
		} else {
			// Write a dummy JFRClass, to handle forward references
			jfrClass = new JFRClass();
			jfrClass.name = DUMMY_NAME_POISON;
			classIdMap.put(classID, jfrClass);
			return jfrClass;
		}
	}
}
