package org.sagebionetworks.agent.action.parameter;

public class IntegerParser implements ParameterParser<Integer> {

	@Override
	public Integer parse(String value) {
		return Integer.parseInt(value);
	}

}
