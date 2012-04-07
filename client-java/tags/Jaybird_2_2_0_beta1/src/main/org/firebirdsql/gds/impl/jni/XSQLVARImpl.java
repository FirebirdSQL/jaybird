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

/**
 * Base class for big- and little-endian implementations.
 */
public abstract class XSQLVARImpl extends XSQLVAR {

    XSQLVARImpl(int sqltype, int sqlscale, int sqlsubtype, int sqllen,
            byte[] sqldata, String sqlname, String relname, String ownname, 
            String aliasname, String relaliasname) {
        
        this.sqltype = sqltype;
        this.sqlscale = sqlscale;
        this.sqlsubtype = sqlsubtype;
        this.sqllen = sqllen;
        this.sqldata = sqldata;
        this.sqlname = sqlname;
        this.relname = relname;
        this.relaliasname = relaliasname;
        this.ownname = ownname;
        this.aliasname = aliasname;
    }

    // Only for TestNgds
    public XSQLVARImpl() {
        this.sqlname = "";
        this.relname = "";
        this.relaliasname = "";
        this.ownname = "";
        this.aliasname = "";
    }

}
