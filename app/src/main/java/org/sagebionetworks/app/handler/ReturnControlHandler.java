package org.sagebionetworks.app.handler;

import org.openapitools.client.ApiException;

public interface ReturnControlHandler {

	String getActionGroup();

	String getFunction();

	String handleEvent(ReturnControlEvent event) throws ApiException;
}
