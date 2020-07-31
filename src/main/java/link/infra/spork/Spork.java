package link.infra.spork;

import link.infra.spork.jfr.SporkJFRHandler;
import link.infra.spork.jfr.transformer.Test;
import link.infra.spork.mappings.MappingsStore;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class Spork implements PreLaunchEntrypoint {
	private final Logger LOGGER = LogManager.getLogger();
	private static final double JAVA_VERSION = Double.parseDouble(System.getProperty("java.specification.version", "0"));

	@Override
	public void onPreLaunch() {
		try {
			Test.test();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// TODO: prefix logger messages
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			LOGGER.warn("Development environment detected, disabling...");
			// TODO: return
		}

		MappingsStore mappings;
		// TODO: download mappings
		try (BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\comp500\\.gradle\\caches\\fabric-loom\\mappings\\yarn-1.16.1+build.20-v2.tiny"))) {
			mappings = new MappingsStore(TinyMappingFactory.loadWithDetection(reader));
		} catch (IOException e) {
			LOGGER.error("Failed to load mappings for Spork", e);
			return;
		}

		MBeanServer srv = ManagementFactory.getPlatformMBeanServer();
		try {
			ThreadMXBean existingBean = ManagementFactory.getThreadMXBean();
			srv.unregisterMBean(existingBean.getObjectName());
			srv.registerMBean(new RemappingThreadMXBeanDelegate(existingBean, mappings), existingBean.getObjectName());
			LOGGER.info("ThreadMXBean replaced, your stacktraces are now mapped");
		} catch (InstanceNotFoundException | MBeanRegistrationException | InstanceAlreadyExistsException | NotCompliantMBeanException e) {
			LOGGER.warn("Failed to replace ThreadMXBean", e);
		}

		if (JAVA_VERSION > 8) {
			// TODO: check this doesn't funky classload on java 8
			SporkJFRHandler.handleJFR(mappings);
		}
	}
}
