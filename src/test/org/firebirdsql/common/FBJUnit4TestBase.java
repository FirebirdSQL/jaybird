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

import static org.firebirdsql.common.FBTestProperties.defaultDatabaseSetUp;
import static org.firebirdsql.common.FBTestProperties.defaultDatabaseTearDown;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.management.FBManager;
import org.junit.After;
import org.junit.Before;

/**
 * Base class for JUnit 4 test cases which could be run against more then a
 * single GDS implementation.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public abstract class FBJUnit4TestBase {

    protected final Logger log = LoggerFactory.getLogger(getClass(), true);

    protected FBManager fbManager = null;

    /**
     * Basic setup of the test database.
     * 
     * @throws Exception
     */
    @Before
    public void basicSetUp() throws Exception {
        fbManager = defaultDatabaseSetUp();
    }

    /**
     * Basic teardown of the test database
     * 
     * @throws Exception
     */
    @After
    protected void basicTearDown() throws Exception {
        defaultDatabaseTearDown(fbManager);
        fbManager = null;
    }

}
