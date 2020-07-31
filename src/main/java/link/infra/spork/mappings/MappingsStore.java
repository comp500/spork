package link.infra.spork.mappings;

import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.MethodDef;
import net.fabricmc.mapping.tree.TinyTree;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MappingsStore {
	public final Map<String, String> classes;
	public final Map<String, String> methods;

	public MappingsStore(TinyTree mappings) {
		Map<String, String> classesTemp = new HashMap<>();
		Map<String, String> methodsTemp = new HashMap<>();

		for (ClassDef cDef : mappings.getClasses()) {
			String cName = cDef.getName("intermediary");
			String cMappedName = cDef.getName("named");
			if (cName != null && cMappedName != null && !cName.equals(cMappedName)) {
				classesTemp.put(cName, cMappedName);
			}
			for (MethodDef mDef : cDef.getMethods()) {
				String mName = mDef.getName("intermediary");
				String mMappedName = mDef.getName("named");
				if (mName != null && mMappedName != null && !mName.equals(mMappedName)) {
					methodsTemp.put(mName, mMappedName);
				}
			}
		}

		classes = Collections.unmodifiableMap(classesTemp);
		methods = Collections.unmodifiableMap(methodsTemp);
	}

	public String getClass(String name) {
		String mappedName = classes.get(name);
		if (mappedName != null) {
			return mappedName;
		}
		return name;
	}

	public String getMethod(String name) {
		String mappedName = methods.get(name);
		if (mappedName != null) {
			return mappedName;
		}
		return name;
	}

}
