/*
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

import junit.framework.TestCase;

/**
 * Base class for test cases which can be run against only a single GDS implementation.
 */
public class SimpleFBTestBase extends TestCase
    {
    /**
     * Default name of database file to use for the test case.
     */
    protected final String DB_NAME = "fbtest.fdb";

    /**
     * Default user name for database connection
     */
    protected final String DB_USER = "sysdba";

    /**
     * Password for the default user for database connection
     */
    protected final String DB_PASSWORD = "masterkey";

    /**
     *
     */
    protected static final String DB_PATH        = System.getProperty("test.db.dir", "");

    /**
     *
     */
    protected static final String DB_SERVER_URL  = System.getProperty("test.db.host", "localhost");

    /**
     *
     */
    protected static final int    DB_SERVER_PORT = Integer.parseInt(System.getProperty("test.db.port", "3050"));

    /**
     * Builds an firebird database connection string for the supplied database file.
     *
     * @param name
     * @return
     */
    protected String getdbpath(String name)
       {
       if ("EMBEDDED".equalsIgnoreCase(System.getProperty("test.gds_type")))
           return DB_PATH + "/" + name;
       else
       if ("LOCAL".equalsIgnoreCase(System.getProperty("test.gds_type")))
           return DB_PATH + "/" + name;
       else
           return DB_SERVER_URL + "/" + DB_SERVER_PORT + ":" + DB_PATH + "/" + name;
       }

    /**
     *
     * @param s
     */
    protected SimpleFBTestBase(String s)
        {
        super(s);
        }
    }
