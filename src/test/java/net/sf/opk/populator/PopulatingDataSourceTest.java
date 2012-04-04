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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PopulatingDataSourceTest {

    private PopulatingDataSource datasource;
    private JDBCPopulator populator;
    private DataSource delegate;


    @Before
    public void setUp() throws NamingException {

        datasource = new PopulatingDataSource();

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, DummyInitialContextFactory.class.getName());
        Context mockContext = createMock(Context.class);
        DummyInitialContextFactory.setMockContext(mockContext);

        final String populatorJNDIName = "populatorJNDIName";
        populator = createMock(JDBCPopulator.class);
        expect(mockContext.lookup(populatorJNDIName)).andReturn(populator);

        final String delegateJNDIName = "delegateJNDIName";
        delegate = createMock(DataSource.class);
        expect(mockContext.lookup(delegateJNDIName)).andReturn(delegate);

        replay(mockContext);
        datasource.setPopulator(populatorJNDIName);
        datasource.setDelegate(delegateJNDIName);
        verify(mockContext);
        reset(mockContext);
    }


    @Test(expected = NamingException.class)
    public void testSetUpFailure1() throws NamingException {

        Context mockContext = createMock(Context.class);
        try
        {
            DummyInitialContextFactory.setMockContext(mockContext);

            final String delegateJNDIName = "delegateJNDIName";
            expect(mockContext.lookup(delegateJNDIName)).andReturn(new Object());

            replay(mockContext);
            datasource.setDelegate(delegateJNDIName);
        }
        finally
        {
            verify(mockContext);
        }
    }


    @Test(expected = ClassCastException.class)
    public void testSetUpFailure2() throws NamingException {

        Context mockContext = createMock(Context.class);
        try
        {
            DummyInitialContextFactory.setMockContext(mockContext);

            final String populatorJNDIName = "populatorJNDIName";
            expect(mockContext.lookup(populatorJNDIName)).andReturn(new Object());

            replay(mockContext);
            datasource.setPopulator(populatorJNDIName);
        }
        finally
        {
            verify(mockContext);
        }
    }


    @Test(expected = SQLException.class)
    public void testGetConnection1a() throws SQLException {

        Connection connection = createMock(Connection.class);
        try
        {
            expect(delegate.getConnection()).andReturn(connection);
            expect(connection.getAutoCommit()).andThrow(new SQLException());

            replay(connection, populator, delegate);
            datasource.getConnection();
        }
        finally
        {
            verify(connection, populator, delegate);
        }
    }


    @Test(expected = SQLException.class)
    public void testGetConnection2a() throws SQLException {

        Connection connection = createMock(Connection.class);
        try
        {
            expect(delegate.getConnection()).andReturn(connection);
            expect(connection.getAutoCommit()).andReturn(false);
            connection.rollback();
            expectLastCall();

            populator.populateDatabase(connection);
            expectLastCall().andThrow(new SQLException());

            replay(connection, populator, delegate);
            assertSame(connection, datasource.getConnection());
        }
        finally
        {
            verify(connection, populator, delegate);
        }
    }


    @Test
    public void testGetConnection3a() throws SQLException {

        Connection connection = createMock(Connection.class);
        expect(delegate.getConnection()).andReturn(connection);
        expect(connection.getAutoCommit()).andReturn(false);
        connection.commit();
        expectLastCall();

        populator.populateDatabase(connection);
        expectLastCall();

        replay(connection, populator, delegate);
        assertSame(connection, datasource.getConnection());
        verify(connection, populator, delegate);
    }


    @Test
    public void testGetConnection4a() throws SQLException {

        Connection connection = createMock(Connection.class);
        expect(delegate.getConnection()).andReturn(connection);
        expect(connection.getAutoCommit()).andReturn(true);
        connection.setAutoCommit(false);
        expectLastCall();
        connection.commit();
        expectLastCall();
        connection.setAutoCommit(true);
        expectLastCall();

        populator.populateDatabase(connection);
        expectLastCall();

        replay(connection, populator, delegate);
        assertSame(connection, datasource.getConnection());
        verify(connection, populator, delegate);
    }


    @Test(expected = SQLException.class)
    public void testGetConnection1b() throws SQLException {

        final String user = "username";
        final String pass = "password";

        Connection connection = createMock(Connection.class);
        try
        {
            expect(delegate.getConnection(user, pass)).andReturn(connection);
            expect(connection.getAutoCommit()).andThrow(new SQLException());

            replay(connection, populator, delegate);
            datasource.getConnection(user, pass);
        }
        finally
        {
            verify(connection, populator, delegate);
        }
    }


    @Test(expected = SQLException.class)
    public void testGetConnection2b() throws SQLException {

        final String user = "username";
        final String pass = "password";

        Connection connection = createMock(Connection.class);
        try
        {
            expect(delegate.getConnection(user, pass)).andReturn(connection);
            expect(connection.getAutoCommit()).andReturn(false);
            connection.rollback();
            expectLastCall();

            populator.populateDatabase(connection);
            expectLastCall().andThrow(new SQLException());

            replay(connection, populator, delegate);
            assertSame(connection, datasource.getConnection(user, pass));
        }
        finally
        {
            verify(connection, populator, delegate);
        }
    }


    @Test
    public void testGetConnection3b() throws SQLException {

        final String user = "username";
        final String pass = "password";

        Connection connection = createMock(Connection.class);
        expect(delegate.getConnection(user, pass)).andReturn(connection);
        expect(connection.getAutoCommit()).andReturn(false);
        connection.commit();
        expectLastCall();

        populator.populateDatabase(connection);
        expectLastCall();

        replay(connection, populator, delegate);
        assertSame(connection, datasource.getConnection(user, pass));
        verify(connection, populator, delegate);
    }


    @Test
    public void testGetConnection4b() throws SQLException {

        final String user = "username";
        final String pass = "password";

        Connection connection = createMock(Connection.class);
        expect(delegate.getConnection(user, pass)).andReturn(connection);
        expect(connection.getAutoCommit()).andReturn(true);
        connection.setAutoCommit(false);
        expectLastCall();
        connection.commit();
        expectLastCall();
        connection.setAutoCommit(true);
        expectLastCall();

        populator.populateDatabase(connection);
        expectLastCall();

        replay(connection, populator, delegate);
        assertSame(connection, datasource.getConnection(user, pass));
        verify(connection, populator, delegate);
    }


    @Test
    public void testGetLoginTimeout() throws SQLException {

        int number = 42;
        expect(delegate.getLoginTimeout()).andReturn(number);

        replay(populator, delegate);
        assertEquals(number, datasource.getLoginTimeout());
        verify(populator, delegate);
    }


    @Test
    public void testSetLoginTimeout() throws SQLException {

        int number = 42;
        delegate.setLoginTimeout(number);
        expectLastCall();

        replay(populator, delegate);
        datasource.setLoginTimeout(number);
        verify(populator, delegate);
    }


    @Test
    public void testGetLogWriter() throws SQLException {

        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        PrintWriter writer = new PrintWriter(System.out);
        expect(delegate.getLogWriter()).andReturn(writer);

        replay(populator, delegate);
        assertSame(writer, datasource.getLogWriter());
        verify(populator, delegate);
    }


    @Test
    public void testSetLogWriter() throws SQLException {

        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        PrintWriter writer = new PrintWriter(System.out);
        delegate.setLogWriter(writer);
        expectLastCall();

        replay(populator, delegate);
        datasource.setLogWriter(writer);
        verify(populator, delegate);
    }


    @Test
    public void testGetParentLogger() throws SQLException {

        Logger logger = Logger.getAnonymousLogger();
        expect(delegate.getParentLogger()).andReturn(logger);

        replay(populator, delegate);
        assertSame(logger, datasource.getParentLogger());
        verify(populator, delegate);
    }


    @Test
    public void testIsWrapperFor() throws SQLException {

        expect(delegate.isWrapperFor(Object.class)).andReturn(false);
        expect(delegate.isWrapperFor(Object.class)).andReturn(true);

        replay(populator, delegate);
        assertFalse(datasource.isWrapperFor(Object.class));
        assertTrue(datasource.isWrapperFor(Object.class));
        verify(populator, delegate);
    }


    @Test
    public void testUnwrap() throws SQLException {

        Object object = new Object();
        expect(delegate.unwrap(Object.class)).andReturn(object);

        replay(populator, delegate);
        assertSame(object, datasource.unwrap(Object.class));
        verify(populator, delegate);
    }
}
