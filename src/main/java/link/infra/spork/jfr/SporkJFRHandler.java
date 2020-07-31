package link.infra.spork.jfr;

import jdk.management.jfr.FlightRecorderMXBean;
import link.infra.spork.mappings.MappingsStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.*;
import java.lang.management.ManagementFactory;

public class SporkJFRHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void handleJFR(MappingsStore mappings) {
		MBeanServer srv = ManagementFactory.getPlatformMBeanServer();
		try {
			FlightRecorderMXBean existingBean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
			srv.unregisterMBean(existingBean.getObjectName());
			srv.registerMBean(new RemappingFlightRecorderMXBeanDelegate(existingBean, mappings), existingBean.getObjectName());
			LOGGER.info("FlightRecorderMXBean replaced, your stacktraces are now mapped");
		} catch (InstanceNotFoundException | MBeanRegistrationException | InstanceAlreadyExistsException | NotCompliantMBeanException e) {
			LOGGER.warn("Failed to replace FlightRecorderMXBean", e);
		}
	}
}
