package org.sagebionetworks.app;

import java.util.Objects;
import java.util.Properties;

public class Configuration {

	private final Properties props;

	public Configuration(Properties props) {
		super();
		this.props = props;
	}

	/**
	 * The agent ID is a required property.
	 * 
	 * @return
	 */
	String getAgentId() {
		return getRequiredProperty("org.sagebionetworks.agent.id");
	}

	String getRequiredProperty(String key) {
		Objects.requireNonNull(key, "key");
		String value = props.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException(String.format("missing property: '%s' ", key));
		}
		return value;
	}
}
