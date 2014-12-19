/*
 * $Id$
 *
 * Firebird Open Source JavaEE connector - JDBC driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.common;

import java.sql.*;

import junit.framework.TestCase;

import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.management.FBManager;

import static org.firebirdsql.common.FBTestProperties.*;

/**
 * Base class for JUnit 3 test cases which could be run against more then one
 * GDS implementation.
 */
public abstract class FBTestBase extends TestCase {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected FBManager fbManager = null;

    protected FBTestBase(String name) {
        super(name);
    }

    protected final FirebirdConnection getConnectionViaDriverManager() throws SQLException {
        return FBTestProperties.getConnectionViaDriverManager();
    }

    protected void setUp() throws Exception {
        fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
    }

    protected void tearDown() throws Exception {
        defaultDatabaseTearDown(fbManager);
        fbManager = null;
    }

}
