package org.sagebionetworks.agent.action.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HandlerProviderImplTest {

	@Mock
	private EventHandler mockHandler1;

	@Mock
	private EventHandler mockHandler2;

	@Test
	public void testGetHandler() {
		when(mockHandler1.getActionGroup()).thenReturn("group1");
		when(mockHandler1.getFunction()).thenReturn("function1");
		when(mockHandler2.getActionGroup()).thenReturn("group2");
		when(mockHandler2.getFunction()).thenReturn("function1");

		HandlerProviderImpl provider = new HandlerProviderImpl(List.of(mockHandler1, mockHandler2));
		// call under test
		assertEquals(Optional.empty(), provider.getHandlerForFunction("group1", "function2"));
		assertEquals(Optional.of(mockHandler1), provider.getHandlerForFunction("group1", "function1"));
		assertEquals(Optional.of(mockHandler2), provider.getHandlerForFunction("group2", "function1"));

		verify(mockHandler1).getActionGroup();
		verify(mockHandler2).getActionGroup();
		verify(mockHandler1).getFunction();
		verify(mockHandler2).getFunction();
		verifyNoMoreInteractions(mockHandler1, mockHandler2);
	}

	@Test
	public void testGetHandlerWithNullGroup() {
		when(mockHandler1.getActionGroup()).thenReturn("group1");
		when(mockHandler1.getFunction()).thenReturn("function1");

		HandlerProviderImpl provider = new HandlerProviderImpl(List.of(mockHandler1));
		String message = assertThrows(NullPointerException.class, () -> {
			// call under test
			provider.getHandlerForFunction(null, "function");
		}).getMessage();
		assertEquals("actionGroup", message);
	}

	@Test
	public void testGetHandlerWithNullFunction() {
		when(mockHandler1.getActionGroup()).thenReturn("group1");
		when(mockHandler1.getFunction()).thenReturn("function1");

		HandlerProviderImpl provider = new HandlerProviderImpl(List.of(mockHandler1));
		String message = assertThrows(NullPointerException.class, () -> {
			// call under test
			provider.getHandlerForFunction("group", null);
		}).getMessage();
		assertEquals("function", message);
	}
}
