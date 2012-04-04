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
package net.sf.opk.populator.sql;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import net.sf.opk.populator.JDBCPopulator;
import net.sf.opk.populator.util.OnceIterable;
import net.sf.opk.populator.util.SkipCommentsReader;

/**
 * {@code JDBCPopulator} that reads a fixed SQL file to import data with.
 *
 * @author <a href="mailto:oscar.westra@42.nl">Oscar Westra van Holthe - Kind</a>
 */
public class ImportSqlPopulator implements JDBCPopulator {

    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ImportSqlPopulator.class.getName());


    @Override
    public void populateDatabase(Connection connection) throws SQLException {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream sqlStream = classLoader.getResourceAsStream("import.sql");

        if (sqlStream == null)
        {
            return;
        }

        Statement statement = null;
        try
        {
            statement = connection.createStatement();

            InputStreamReader sqlReader = new InputStreamReader(sqlStream, Charset.forName("UTF-8"));
            SqlStatementIterator statementIterator = new SqlStatementIterator(new SkipCommentsReader(sqlReader, "--"));
            for (String sqlStatement : new OnceIterable<String>(statementIterator))
            {
                LOGGER.info(String.format("Executing SQL: %s", sqlStatement));
                statement.execute(sqlStatement);
            }
        }
        finally
        {
            if (statement != null)
            {
                statement.close();
            }
        }
    }
}
