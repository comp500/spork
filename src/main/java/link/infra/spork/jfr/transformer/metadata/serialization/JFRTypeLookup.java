package link.infra.spork.jfr.transformer.metadata.serialization;

public interface JFRTypeLookup {
	/**
	 * Looks up the class with the given class ID.
	 * If it does not exist during metadata parsing time, a dummy JFRClass will be created, with only a class ID and no other metadata.
	 */
	JFRClass lookupClass(long classID);
}
