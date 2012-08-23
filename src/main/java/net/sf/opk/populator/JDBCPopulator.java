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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * <p>Interface for JDBC populators. These actually fill the database.</p>
 *
 * <p>The populator is called by its data source just before the first connection is returned. This usually happens
 * before any persistence unit is called. If the database is an in-memory database, it'll be empty.</p>
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public interface JDBCPopulator
{
	/**
	 * Populate the database.
	 *
	 * @param connection the connection to populate the database with
	 * @throws SQLException when the database cannot be populated
	 * @throws IOException when the data to populate with cannot be read
	 */
	void populateDatabase(Connection connection) throws SQLException, IOException;
}
