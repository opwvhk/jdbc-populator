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

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * <p>A populating data source. Delegates all calls to the named data source, after populating the database via its named {@link JDBCPopulator}.</p>
 *
 * <p>Configuration properties:</p><dl>
 *
 * <dt>delegate</dt><dd>Required. The JNDI name of the data source to delegate all calls to. Must be a {@link javax.sql.DataSource}.</dd>
 *
 * <dt>populator</dt><dd>Required. The JNDI name of the {@code JDBCPopulator} to use to populate the database.</dd>
 *
 * </dl>
 *
 * @author <a href="mailto:oscar.westra@42.nl">Oscar Westra van Holthe - Kind</a>
 */
public class PopulatingDataSource extends DelegateDataSource {

    /**
     * The populator to use to populate the database.
     */
    private JDBCPopulator populator;
    /**
     * A flag that determines if the database has been populated.
     */
    private boolean populated;


    @Override
    public Connection getConnection() throws SQLException {

        Connection connection = super.getConnection();
        populateOnce(connection);
        return connection;
    }


    @Override
    public Connection getConnection(String username, String password) throws SQLException {

        Connection connection = super.getConnection(username, password);
        populateOnce(connection);
        return connection;
    }


    private void populateOnce(Connection connection) throws SQLException {

        if (populated)
        {
            return;
        }
        populateInTransaction(connection);
        populated = true;
    }


    private void populateInTransaction(Connection connection) throws SQLException {

        boolean autocommit = connection.getAutoCommit();
        try
        {
            if (autocommit)
            {
                connection.setAutoCommit(false);
            }
            populator.populateDatabase(connection);
            connection.commit();
        }
        catch (SQLException e)
        {
            connection.rollback();
            throw e;
        }
        finally
        {
            if (autocommit)
            {
                connection.setAutoCommit(true);
            }
        }
    }


    /**
     * Set the {@link JDBCPopulator} of this data source to a populator loaded from JNDI.
     *
     * @param populator the name of the {@code JDBCPopulator} to delegate to
     * @throws NamingException when the data source cannot be found
     */
    public void setPopulator(String populator) throws NamingException {

        this.populator = (JDBCPopulator) new InitialContext().lookup(populator);
    }
}
