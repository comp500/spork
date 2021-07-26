package link.infra.spork.jfr.transformer;

public class JFRTransformer {

	// TODO: refactor everything into something with somewhat cleaner ways of specifying class metadata
	// TODO: implement serialisation of everything, and check it works
	// TODO: implement tree/visitor framework for event modification
	// TODO: implement reflection framework for event modification MAYBE
	    // TODO: probably not necessary though
	// TODO: maybe rewrite some stuff that is similar to JMC
	// TODO: - rename all the element stuff to JFRMeta*
	// TODO: - make the parser read all the string data first, then make the JFRMetaClasses read from it and serialise to it
	// TODO: - most of the other data (locale etc) can be part of some metadata class, rather than all these associated other classes
	// TODO: - although... what is setting used for?
	// TODO: - maybe use reflection or codegen to make it not pain?
	// TODO: - make concrete classes (JFRClass, JFRField etc) which have the metadata as a field, and parse those in the constant pool and events
	// TODO: determine how transformers should be chained... or leave this up to the API consumer?
		// TODO: maybe just have an event consumer and a way to write events
	// TODO: ensure that all metadata is registered when serialising

	// TODO: instead of failing on unparsed data, strict validation checks input == output?

	public void registerJFRType() {

	}

	/**
	 * Register a transformer that reads an event, and can replace the event with 0 or more events
	 */
	public void registerEventTransformer() {

	}

	public void registerEventPrepender() {

	}

	public void registerEventAppender() {

	}
}
