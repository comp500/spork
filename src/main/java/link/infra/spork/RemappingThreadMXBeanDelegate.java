package link.infra.spork;

import link.infra.spork.mappings.MappingsStore;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class RemappingThreadMXBeanDelegate implements ThreadMXBean {
	private final ThreadMXBean delegate;
	private final MappingsStore mappings;

	public RemappingThreadMXBeanDelegate(ThreadMXBean delegate, MappingsStore mappings) {
		this.delegate = delegate;
		this.mappings = mappings;
	}

	@Override
	public int getThreadCount() {
		return delegate.getThreadCount();
	}

	@Override
	public int getPeakThreadCount() {
		return delegate.getPeakThreadCount();
	}

	@Override
	public long getTotalStartedThreadCount() {
		return delegate.getTotalStartedThreadCount();
	}

	@Override
	public int getDaemonThreadCount() {
		return delegate.getDaemonThreadCount();
	}

	@Override
	public long[] getAllThreadIds() {
		return delegate.getAllThreadIds();
	}

	@Override
	public ThreadInfo getThreadInfo(long id) {
		return transformThreadInfo(delegate.getThreadInfo(id));
	}

	@Override
	public ThreadInfo[] getThreadInfo(long[] ids) {
		return transformThreadInfo(delegate.getThreadInfo(ids));
	}

	@Override
	public ThreadInfo getThreadInfo(long id, int maxDepth) {
		return transformThreadInfo(delegate.getThreadInfo(id, maxDepth));
	}

	@Override
	public ThreadInfo[] getThreadInfo(long[] ids, int maxDepth) {
		return transformThreadInfo(delegate.getThreadInfo(ids, maxDepth));
	}

	@Override
	public boolean isThreadContentionMonitoringSupported() {
		return delegate.isThreadContentionMonitoringSupported();
	}

	@Override
	public boolean isThreadContentionMonitoringEnabled() {
		return delegate.isThreadContentionMonitoringEnabled();
	}

	@Override
	public void setThreadContentionMonitoringEnabled(boolean enable) {
		delegate.setThreadContentionMonitoringEnabled(enable);
	}

	@Override
	public long getCurrentThreadCpuTime() {
		return delegate.getCurrentThreadCpuTime();
	}

	@Override
	public long getCurrentThreadUserTime() {
		return delegate.getCurrentThreadUserTime();
	}

	@Override
	public long getThreadCpuTime(long id) {
		return delegate.getThreadCpuTime(id);
	}

	@Override
	public long getThreadUserTime(long id) {
		return delegate.getThreadUserTime(id);
	}

	@Override
	public boolean isThreadCpuTimeSupported() {
		return delegate.isThreadCpuTimeSupported();
	}

	@Override
	public boolean isCurrentThreadCpuTimeSupported() {
		return delegate.isCurrentThreadCpuTimeSupported();
	}

	@Override
	public boolean isThreadCpuTimeEnabled() {
		return delegate.isThreadCpuTimeEnabled();
	}

	@Override
	public void setThreadCpuTimeEnabled(boolean enable) {
		delegate.setThreadCpuTimeEnabled(enable);
	}

	@Override
	public long[] findMonitorDeadlockedThreads() {
		return delegate.findMonitorDeadlockedThreads();
	}

	@Override
	public void resetPeakThreadCount() {
		delegate.resetPeakThreadCount();
	}

	@Override
	public long[] findDeadlockedThreads() {
		return delegate.findDeadlockedThreads();
	}

	@Override
	public boolean isObjectMonitorUsageSupported() {
		return delegate.isObjectMonitorUsageSupported();
	}

	@Override
	public boolean isSynchronizerUsageSupported() {
		return delegate.isSynchronizerUsageSupported();
	}

	private ThreadInfo[] transformThreadInfo(ThreadInfo[] info) {
		for (int i = 0; i < info.length; i++) {
			// Turn this threadinfo into a compositedata
			CompositeData threadData = sun.management.ThreadInfoCompositeData.toCompositeData(info[i]);
			info[i] = ThreadInfo.from(new RemappingThreadCompositeData(threadData, mappings));
		}
		return info;
	}

	private ThreadInfo transformThreadInfo(ThreadInfo info) {
		// Turn this threadinfo into a compositedata
		CompositeData threadData = sun.management.ThreadInfoCompositeData.toCompositeData(info);
		return ThreadInfo.from(new RemappingThreadCompositeData(threadData, mappings));
	}

	@Override
	public ThreadInfo[] getThreadInfo(long[] ids, boolean lockedMonitors, boolean lockedSynchronizers) {
		return transformThreadInfo(delegate.getThreadInfo(ids, lockedMonitors, lockedSynchronizers));
	}

	@Override
	public ThreadInfo[] dumpAllThreads(boolean lockedMonitors, boolean lockedSynchronizers) {
		return transformThreadInfo(delegate.dumpAllThreads(lockedMonitors, lockedSynchronizers));
	}

	@Override
	public ObjectName getObjectName() {
		return delegate.getObjectName();
	}
}
