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

/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */

package org.firebirdsql.gds;

import javax.security.auth.Subject;
import java.util.List;

/**
 * The interface <code>isc_db_handle</code> represents a socket connection
 * to the database server.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface isc_db_handle {

    Subject getSubject();

    boolean hasTransactions();
    
    /**
     * Get list of warnings that were returned by the server.
     * 
     * @return instance of {@link List} containing instances of 
     * {@link GDSException} representing server warnings (method 
     * {@link GDSException#isWarning()} returns <code>true</code>).
     */
    List getWarnings();
    
    /**
     * Clear warning list associated with this connection.
     */
    void clearWarnings();

    void setDialect(int value);

    int getDialect();

    void setVersion(String value);

    String getVersion();

    String getDatabaseProductName();

    String getDatabaseProductVersion();

    int getDatabaseProductMajorVersion();

    int getDatabaseProductMinorVersion();

    void setODSMajorVersion(int value);

    int getODSMajorVersion();

    void setODSMinorVersion(int value);

    int getODSMinorVersion();
}

