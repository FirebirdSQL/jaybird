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
package org.firebirdsql.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBObjectListener;

/**
 * JDBC 2.0 compliant implementation of {@link PreparedStatement} interface.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBPreparedStatement extends AbstractPreparedStatement {

    /**
     * Create instance of this class.
     * 
     * @param c instance of {@link AbstractConnection}.
     * @param sql SQL statement to prepare.
     * 
     * @throws SQLException if something went wrong.
     */
    public FBPreparedStatement(GDSHelper gdsHelper, String sql, 
                               int rsType, int rsConcurrency, int rsHoldability,
                               FBObjectListener.StatementListener statementListener,
                               FBObjectListener.BlobListener blobListener,
                               boolean metaData, boolean standaloneStatement)
    throws SQLException {
        super(gdsHelper, sql, rsType, rsConcurrency, rsHoldability,
                statementListener, blobListener, metaData, standaloneStatement);
    }

}
