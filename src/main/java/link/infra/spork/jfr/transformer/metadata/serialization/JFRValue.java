package link.infra.spork.jfr.transformer.metadata.serialization;

public final class JFRValue<V, T extends JFRType<V>> {
	public final V value;
	public final T type;

	public JFRValue(V value, T type) {
		this.value = value;
		this.type = type;
	}

	public static <V, T extends JFRType<V>> JFRValue<V, T> deserialize(T type) {
		return new JFRValue<>(type.deserialize(), type);
	}

	public void serialize() {
		type.serialize(value);
	}
}
