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

/**
 * <p>Interface for JDBC populators. These actually fill the database.</p>
 *
 * <p>When the populator is called depends on the caller:</p><ul>
 *
 * <li>{@link ContextListener} only uses {@link net.sf.opk.populator.sql.ImportSqlPopulator}, and calls it right after the application is
 * initialized. This means that the persistence unit of the application has been set up (there must be exactly one),
 * and thus that the database structure is already in place.</li>
 *
 * <li>{@link PopulatingXADataSource} calls its JDBC populator just before the first connection is returned. This
 * happens before any persistence unit is called. If the database is an in-memory database, it'll be empty.</li>
 *
 * </ul>
 *
 * @author <a href="mailto:oscar.westra@42.nl">Oscar Westra van Holthe - Kind</a>
 */
public interface JDBCPopulator {

    /**
     * Populate the database.
     *
     * @param connection the connection to populate the database with
     * @throws SQLException when the database cannot be populated
     */
    void populateDatabase(Connection connection) throws SQLException;
}
