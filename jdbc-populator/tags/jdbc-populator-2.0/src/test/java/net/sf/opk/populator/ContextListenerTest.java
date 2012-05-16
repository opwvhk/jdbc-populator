/*
 * Copyright (c) 2012 Oscar Westra van Holthe - Kind
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package net.sf.opk.populator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Context;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNull;


public class ContextListenerTest extends DatabaseTestBase
{
	private ContextListener listener;
	private Context mockContext;


	@Before
	public void setUp() throws Exception
	{
		listener = new ContextListener();

		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, DummyInitialContextFactory.class.getName());
		mockContext = createMock(Context.class);
		DummyInitialContextFactory.setMockContext(mockContext);
	}


	@After
	public void tearDown() throws Exception
	{
		System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
	}


	@Test
	public void testFaultyXPath() throws Exception
	{
		assertNull(ContextListener.compileXPathExpression("/*impossible"));
	}


	@Test
	public void testShutdown() throws Exception
	{
		replay(mockContext);

		ServletContext mockServletContext = createMock(ServletContext.class);
		replay(mockServletContext);

		ServletContextEvent event = new ServletContextEvent(mockServletContext);
		listener.contextDestroyed(event);

		verify(mockServletContext);
		checkRecordCount(0L);
	}


	@Test
	public void testNoImport() throws Exception
	{
		DataSource nonJTADataSource = createMock(DataSource.class);
		expect(nonJTADataSource.getConnection()).andStubReturn(connectionForTest);
		expect(mockContext.lookup("jdbc/myDataSourceNonJta")).andStubReturn(nonJTADataSource);
		replay(mockContext, nonJTADataSource);

		fireContextInitializedEvent("persistence_nonjta");

		checkRecordCount(0L);
	}


	private void fireContextInitializedEvent(String... extraClassResources)
			throws URISyntaxException, MalformedURLException
	{
		File classpathsDirectory = findClasspathsDirectory();

		URL[] urls = new URL[extraClassResources.length];
		for (int i = 0; i < extraClassResources.length; i++)
		{
			urls[i] = createURL(classpathsDirectory, extraClassResources[i]);
		}

		fireContextInitializedEvent(urls);
	}


	private File findClasspathsDirectory() throws URISyntaxException
	{
		File srcDirectory = MavenUtil.findSourcesDirectory();
		File testDirectory = new File(srcDirectory, "test");
		File classpathsDirectory = new File(testDirectory, "classpaths");
		return classpathsDirectory.getAbsoluteFile();
	}


	private URL createURL(File classpathsDirectory, String subPath) throws MalformedURLException
	{
		File path = new File(classpathsDirectory, subPath);
		return path.toURI().toURL();
	}


	private void fireContextInitializedEvent(URL[] extraClasspath)
	{
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		ServletContext mockServletContext = createMock(ServletContext.class);
		replay(mockServletContext);
		ServletContextEvent event = new ServletContextEvent(mockServletContext);
		try
		{
			URLClassLoader urlClassLoader = new URLClassLoader(extraClasspath, originalClassLoader);
			// Replace the thread classloader - assumes you have permissions to do so
			Thread.currentThread().setContextClassLoader(urlClassLoader);

			listener.contextInitialized(event);
		}
		finally
		{
			verify(mockServletContext);
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}


	@Test
	public void testNoPersistenceUnits() throws Exception
	{
		replay(mockContext);
		fireContextInitializedEvent("importOk");

		checkRecordCount(0L);
	}


	@Test
	public void testPersistenceUnitJTA() throws Exception
	{
		XADataSource jtaDataSource = createMock(XADataSource.class);
		XAConnection xaConnection = createMock(XAConnection.class);
		expect(jtaDataSource.getXAConnection()).andStubReturn(xaConnection);
		expect(xaConnection.getConnection()).andStubReturn(connectionForTest);
		expect(mockContext.lookup("jdbc/myDataSource")).andStubReturn(jtaDataSource);
		replay(mockContext, jtaDataSource, xaConnection);

		fireContextInitializedEvent("importOk", "persistence_jta");

		checkRecordCount(1L);
	}


	@Test
	public void testPersistenceUnitNonJTA() throws Exception
	{
		DataSource nonJTADataSource = createMock(DataSource.class);
		expect(nonJTADataSource.getConnection()).andStubReturn(connectionForTest);
		expect(mockContext.lookup("jdbc/myDataSourceNonJta")).andStubReturn(nonJTADataSource);
		replay(mockContext, nonJTADataSource);

		fireContextInitializedEvent("importOk", "persistence_nonjta");

		checkRecordCount(1L);
	}


	@Test
	public void testPersistenceUnitBoth() throws Exception
	{
		XADataSource jtaDataSource = createMock(XADataSource.class);
		XAConnection xaConnection = createMock(XAConnection.class);
		expect(jtaDataSource.getXAConnection()).andStubReturn(xaConnection);
		expect(xaConnection.getConnection()).andStubReturn(connectionForTest);
		expect(mockContext.lookup("jdbc/myDataSource")).andStubReturn(jtaDataSource);
		replay(mockContext, jtaDataSource, xaConnection);

		fireContextInitializedEvent("importOk", "persistence_both");

		checkRecordCount(1L);
	}


	@Test
	public void testTooManyPersistenceUnits() throws Exception
	{
		replay(mockContext);

		fireContextInitializedEvent("importOk", "persistence_jta", "persistence_nonjta");

		checkRecordCount(0L);
	}


	@Test
	public void testImportError0() throws Exception
	{
		DataSource nonJTADataSource = createMock(DataSource.class);
		expect(nonJTADataSource.getConnection()).andStubThrow(new SQLException());
		expect(mockContext.lookup("jdbc/myDataSourceNonJta")).andStubReturn(nonJTADataSource);
		replay(mockContext, nonJTADataSource);

		fireContextInitializedEvent("importError", "persistence_nonjta");

		checkRecordCount(0L);
	}


	@Test
	public void testImportError1() throws Exception
	{
		XADataSource jtaDataSource = createMock(XADataSource.class);
		XAConnection xaConnection = createMock(XAConnection.class);
		expect(jtaDataSource.getXAConnection()).andStubThrow(new SQLException());
		expect(mockContext.lookup("jdbc/myDataSource")).andStubReturn(jtaDataSource);
		replay(mockContext, jtaDataSource, xaConnection);

		fireContextInitializedEvent("importError", "persistence_jta");

		checkRecordCount(0L);
	}


	@Test
	public void testImportError2() throws Exception
	{
		XADataSource jtaDataSource = createMock(XADataSource.class);
		XAConnection xaConnection = createMock(XAConnection.class);
		expect(jtaDataSource.getXAConnection()).andStubReturn(xaConnection);
		Connection connection = createMock(Connection.class);
		expect(xaConnection.getConnection()).andStubReturn(connection);
		expect(connection.createStatement()).andStubThrow(new SQLException());
		connection.rollback();
		expectLastCall().asStub();
		connection.close();
		expectLastCall().asStub();
		expect(mockContext.lookup("jdbc/myDataSource")).andStubReturn(jtaDataSource);
		replay(mockContext, jtaDataSource, xaConnection, connection);

		fireContextInitializedEvent("importError", "persistence_jta");

		checkRecordCount(0L);
	}


	@Test
	public void testImportError3() throws Exception
	{
		XADataSource jtaDataSource = createMock(XADataSource.class);
		XAConnection xaConnection = createMock(XAConnection.class);
		expect(jtaDataSource.getXAConnection()).andStubReturn(xaConnection);
		expect(xaConnection.getConnection()).andStubReturn(connectionForTest);
		expect(mockContext.lookup("jdbc/myDataSource")).andStubReturn(jtaDataSource);
		replay(mockContext, jtaDataSource, xaConnection);

		fireContextInitializedEvent("importError", "persistence_jta");

		checkRecordCount(0L);
	}
}
