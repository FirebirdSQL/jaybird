/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * Tests for "use Firebird autocommit" mode.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class TestUseFirebirdAutocommit extends FBJUnit4TestBase {

    /**
     * Tests if the connection property {@code useFirebirdAutocommit} defaults to {@code false} if not set.
     */
    @Test
    public void connectionPropertyUseFirebirdAutocommit_notSpecified() throws Exception {
        String url = FBTestProperties.getUrl();
        checkFirebirdAutocommitValue(url, false);
    }

    /**
     * Tests if the connection property {@code useFirebirdAutocommit} is set to {@code false} if URL value is false.
     */
    @Test
    public void connectionPropertyUseFirebirdAutocommit_fromUrl_valueFalse() throws Exception {
        String url = FBTestProperties.getUrl() + "?useFirebirdAutocommit=false";
        checkFirebirdAutocommitValue(url, false);
    }

    /**
     * Tests if the connection property {@code useFirebirdAutocommit} is set to {@code true} from the URL without a value.
     */
    @Test
    public void connectionPropertyUseFirebirdAutocommit_fromUrl_noValue() throws Exception {
        String url = FBTestProperties.getUrl() + "?useFirebirdAutocommit";
        checkFirebirdAutocommitValue(url, true);
    }

    /**
     * Tests if the connection property {@code useFirebirdAutocommit} is set to {@code true} from the URL with
     * value {@code true}.
     */
    @Test
    public void connectionPropertyUseFirebirdAutocommit_fromUrl_valueTrue() throws Exception {
        String url = FBTestProperties.getUrl() + "?useFirebirdAutocommit=true";
        checkFirebirdAutocommitValue(url, true);
    }

    private void checkFirebirdAutocommitValue(String url, boolean expectedUseFirebirdAutocommit) throws SQLException {
        FBConnection connection = (FBConnection) DriverManager.getConnection(url, FBTestProperties.DB_USER,
                FBTestProperties.DB_PASSWORD);

        try {
            FBManagedConnectionFactory managedConnectionFactory = (FBManagedConnectionFactory) connection
                    .getManagedConnection().getManagedConnectionFactory();
            assertEquals("useFirebirdAutocommit",
                    expectedUseFirebirdAutocommit, managedConnectionFactory.isUseFirebirdAutocommit());
        } finally {
            connection.close();
        }
    }

}
