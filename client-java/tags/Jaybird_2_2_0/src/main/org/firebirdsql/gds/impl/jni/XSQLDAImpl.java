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
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.XSQLDA;

public class XSQLDAImpl extends XSQLDA {

    public XSQLDAImpl() {
        // empty
    }
    
    XSQLDAImpl(int sqln, int sqld, XSQLVAR[] sqlvar) {
        super();

        this.sqln = sqln;
        this.sqld = sqld;
        this.sqlvar = sqlvar;
    }

    // For TestNgds
    public XSQLDAImpl(int n) {
        version = ISCConstants.SQLDA_VERSION1;
        sqln = n;
        sqld = n;
        sqlvar = new XSQLVARImpl[n];
    }
}
