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
package org.firebirdsql.ngds;


/**
 * Implementation of {@link XSQLVAR} class for big-endian platforms 
 * (like Solaris).
 */
public class XSQLVARBigEndianImpl extends XSQLVARImpl {

    /**
     * Create default instance of this class.
     */
    public XSQLVARBigEndianImpl() {
        super();
    }

    /**
     * Create instance of this class for the specified XSQLVAR parameters.
     */
    public XSQLVARBigEndianImpl(int sqltype, int sqlscale, int sqlsubtype,
            int sqllen, byte[] sqldata, String sqlname, String relname,
            String ownname, String aliasname) {
        super(sqltype, sqlscale, sqlsubtype, sqllen, sqldata, sqlname, relname,
                ownname, aliasname);
    }
}
