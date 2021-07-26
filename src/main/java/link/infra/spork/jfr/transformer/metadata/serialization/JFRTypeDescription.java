package link.infra.spork.jfr.transformer.metadata.serialization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JFRTypeDescription {
	public Map<String, String> attributes;
	public Map<String, List<JFRTypeDescription>> children;

	public JFRTypeDescription(Map<String, String> attributes, Map<String, List<JFRTypeDescription>> children) {
		this.attributes = attributes;
		this.children = children;
	}

	public JFRTypeDescription() {
		this(new HashMap<>(), new HashMap<>());
	}
}
