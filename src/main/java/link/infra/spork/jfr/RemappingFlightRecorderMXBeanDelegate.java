package link.infra.spork.jfr;

import jdk.management.jfr.ConfigurationInfo;
import jdk.management.jfr.EventTypeInfo;
import jdk.management.jfr.FlightRecorderMXBean;
import jdk.management.jfr.RecordingInfo;
import link.infra.spork.mappings.MappingsStore;

import javax.management.ObjectName;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RemappingFlightRecorderMXBeanDelegate implements FlightRecorderMXBean {
	private final FlightRecorderMXBean delegate;
	private final FlightRecorderRemapper remapper;

	public RemappingFlightRecorderMXBeanDelegate(FlightRecorderMXBean delegate, MappingsStore mappings) {
		this.delegate = delegate;
		remapper = new FlightRecorderRemapper(mappings);
	}

	@Override
	public long newRecording() throws IllegalStateException, SecurityException {
		return delegate.newRecording();
	}

	@Override
	public long takeSnapshot() {
		return delegate.takeSnapshot();
	}

	@Override
	public long cloneRecording(long recordingId, boolean stop) throws IllegalArgumentException, SecurityException {
		return delegate.cloneRecording(recordingId, stop);
	}

	@Override
	public void startRecording(long recordingId) throws IllegalStateException, SecurityException {
		delegate.startRecording(recordingId);
	}

	@Override
	public boolean stopRecording(long recordingId) throws IllegalArgumentException, IllegalStateException, SecurityException {
		return delegate.stopRecording(recordingId);
	}

	@Override
	public void closeRecording(long recordingId) throws IOException {
		delegate.closeRecording(recordingId);
	}

	@Override
	public long openStream(long recordingId, Map<String, String> streamOptions) throws IOException {
		return delegate.openStream(recordingId, streamOptions);
	}

	@Override
	public void closeStream(long streamId) throws IOException {
		remapper.closeStream(streamId);
		delegate.closeStream(streamId);
	}

	@Override
	public byte[] readStream(long streamId) throws IOException {
		return remapper.readStream(streamId, delegate);
	}

	@Override
	public Map<String, String> getRecordingOptions(long recordingId) throws IllegalArgumentException {
		return delegate.getRecordingOptions(recordingId);
	}

	@Override
	public Map<String, String> getRecordingSettings(long recordingId) throws IllegalArgumentException {
		return delegate.getRecordingSettings(recordingId);
	}

	@Override
	public void setConfiguration(long recordingId, String contents) throws IllegalArgumentException {
		delegate.setConfiguration(recordingId, contents);
	}

	@Override
	public void setPredefinedConfiguration(long recordingId, String configurationName) throws IllegalArgumentException {
		delegate.setPredefinedConfiguration(recordingId, configurationName);
	}

	@Override
	public void setRecordingSettings(long recordingId, Map<String, String> settings) throws IllegalArgumentException {
		delegate.setRecordingSettings(recordingId, settings);
	}

	@Override
	public void setRecordingOptions(long recordingId, Map<String, String> options) throws IllegalArgumentException {
		delegate.setRecordingOptions(recordingId, options);
	}

	@Override
	public List<RecordingInfo> getRecordings() {
		return delegate.getRecordings();
	}

	@Override
	public List<ConfigurationInfo> getConfigurations() {
		return delegate.getConfigurations();
	}

	@Override
	public List<EventTypeInfo> getEventTypes() {
		return delegate.getEventTypes();
	}

	@Override
	public void copyTo(long recordingId, String outputFile) throws IOException, SecurityException {
		delegate.copyTo(recordingId, outputFile);
	}

	@Override
	public ObjectName getObjectName() {
		return delegate.getObjectName();
	}
}
