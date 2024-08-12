package org.sagebionetworks.agent.action.parameter;

public class StringParser implements ParameterParser<String> {

	@Override
	public String parse(String value) {
		return value;
	}

}
