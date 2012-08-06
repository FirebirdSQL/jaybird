/*
 * $Id$
 * Firebird Open Source J2ee connector - jdbc driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.common;

import java.io.File;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Helper class for test properties (database user, password, paths etc)
 */
public final class SimpleFBTestBase {
	private static ResourceBundle testDefaults = ResourceBundle.getBundle("unit_test_defaults");

	public static String getProperty(String property) {
		return getProperty(property, null);
	}
	
	public static String getProperty(String property, String defaultValue) {
		try {
			return System.getProperty(property, testDefaults.getString(property));
		} catch (MissingResourceException ex) {
			return System.getProperty(property, defaultValue);
		}
	}

	/**
	 * Default name of database file to use for the test case.
	 */
	public static final String DB_NAME = "fbtest.fdb";

	public static final String DB_USER = getProperty("test.user", "sysdba");
	public static final String DB_PASSWORD = getProperty("test.password", "masterkey");

	public static final String DB_PATH = getProperty("test.db.dir", "");
	public static final String DB_SERVER_URL = getProperty("test.db.host", "localhost");
	public static final int DB_SERVER_PORT = Integer.parseInt(getProperty("test.db.port", "3050"));

    public static String getDatabasePath() {
        return getDatabasePath(DB_NAME);
    }
    
    public static String getDatabasePath(String name) {
        if (!"127.0.0.1".equals(DB_SERVER_URL) && !"localhost".equals(DB_SERVER_URL))
            return DB_PATH + "/" + name;
        else
            return new File(DB_PATH, name).getAbsolutePath();
    }


	/**
	 * Builds an firebird database connection string for the supplied database
	 * file.
	 * 
	 * @param name
	 * @return
	 */
    public static String getdbpath(String name) {
        final String gdsType = getProperty("test.gds_type", null);
        if ("EMBEDDED".equalsIgnoreCase(gdsType) || "LOCAL".equalsIgnoreCase(gdsType)) {
            return new File(DB_PATH, name).getAbsolutePath();
        } else {
            return DB_SERVER_URL + "/" + DB_SERVER_PORT + ":" + getDatabasePath(name);
        }
    }

	private SimpleFBTestBase() {
	}
}
