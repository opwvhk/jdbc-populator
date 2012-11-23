/*
 * Copyright 2012 Oscar Westra van Holthe - Kind
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package net.sf.opk.populator;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;


/**
 * <p>Simple {@code InitialContextFactory} that returns only the JNDI context it has been given. Not thread-safe, not
 * even remotely useful as a (real) JNDI provider, but very useful to get a mocked {@link Context} into the code you're
 * testing.</p> <p>Example usage:</p>
 * <pre>
 * System.setProperty(Context.INITIAL_CONTEXT_FACTORY, DummyInitialContextFactory.class.getName());
 * Context mockContext = createMock(Context.class);
 * UserTransaction mockTrans = createMock(UserTransaction.class);
 * expect(mockContext.lookup("UserTransaction")).andReturn(mockTrans);
 * replay(mockContext);
 * DummyInitialContextFactory.setMockContext(mockContext);
 * </pre>
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public class DummyInitialContextFactory implements InitialContextFactory
{
	private static Context mockContext = null;


	public static void setMockContext(Context mockContext)
	{
		DummyInitialContextFactory.mockContext = mockContext;
	}


	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException
	{
		if (mockContext == null)
		{
			throw new IllegalStateException("mock context was not set.");
		}
		return mockContext;
	}
}
