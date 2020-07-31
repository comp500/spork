package link.infra.spork;

import link.infra.spork.mappings.MappingsStore;
import sun.management.StackTraceElementCompositeData;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.util.Collection;

public class RemappingThreadCompositeData implements CompositeData {
	private final CompositeData delegate;
	private final MappingsStore mappings;

	public RemappingThreadCompositeData(CompositeData delegate, MappingsStore mappings) {
		this.delegate = delegate;
		this.mappings = mappings;
	}

	@Override
	public CompositeType getCompositeType() {
		return delegate.getCompositeType();
	}

	@Override
	public Object get(String key) {
		if (key.equals("stackTrace")) {
			CompositeData[] existing = (CompositeData[]) delegate.get("stackTrace");
			for (int i = 0; i < existing.length; i++) {
				StackTraceElement el = StackTraceElementCompositeData.from(existing[i]);

				String mappedClass = mappings.getClass(el.getClassName().replace('.', '/')).replace('/', '.');
				String mappedFileName = el.getFileName();
				// If Class.java == some.package.Class, remap Class
				if (mappedFileName != null && mappedFileName.substring(0, mappedFileName.length() - 5).equals(el.getClassName().substring(el.getClassName().lastIndexOf('.') + 1))) {
					mappedFileName = mappedClass.substring(mappedClass.lastIndexOf('.') + 1) + ".java";
				}

				existing[i] = StackTraceElementCompositeData.toCompositeData(new StackTraceElement(
					mappedClass,
					mappings.getMethod(el.getMethodName()),
					mappedFileName,
					el.getLineNumber()
				));
			}
			return existing;
		}
		return delegate.get(key);
	}

	@Override
	public Object[] getAll(String[] keys) {
		return delegate.getAll(keys);
	}

	@Override
	public boolean containsKey(String key) {
		return delegate.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	@Override
	public Collection<?> values() {
		return delegate.values();
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
