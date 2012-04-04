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
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

/**
 * A data source that delegates all method calls to a data source obtained via JNDI. It can delegate to both XA and non-XA data sources. Calling methods
 * exclusive to the type not currently being delegated to will yield an {@link IllegalStateException}.
 *
 * @author <a href="mailto:oscar.westra@42.nl">Oscar Westra van Holthe - Kind</a>
 */
public class DelegateXADataSource implements XADataSource {

    /**
     * The data source to delegate to.
     */
    private XADataSource xaDelegate;


    /**
     * Set the delegate of this data source to a data source loaded from JNDI.
     *
     * @param jndiName the name of the data source to delegate to
     * @throws NamingException when the data source cannot be found
     */
    public void setDelegate(String jndiName) throws NamingException {

        Object datasource = new InitialContext().lookup(jndiName);
        if (datasource instanceof XADataSource)
        {
            setDelegate((XADataSource)datasource);
        }
        else
        {
            throw new NamingException(jndiName + " is not a " + XADataSource.class.getName());
        }
    }


    public void setDelegate(XADataSource delegate) {

        xaDelegate = delegate;
    }


    @Override
    public XAConnection getXAConnection() throws SQLException {

        return xaDelegate.getXAConnection();
    }


    @Override
    public XAConnection getXAConnection(String user, String password) throws SQLException {

        return xaDelegate.getXAConnection(user, password);
    }


    @Override
    public PrintWriter getLogWriter() throws SQLException {

        return xaDelegate.getLogWriter();
    }


    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

        xaDelegate.setLogWriter(out);
    }


    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

        xaDelegate.setLoginTimeout(seconds);
    }


    @Override
    public int getLoginTimeout() throws SQLException {

        return xaDelegate.getLoginTimeout();
    }


    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {

        return xaDelegate.getParentLogger();
    }
}
