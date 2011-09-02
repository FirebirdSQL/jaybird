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

import java.sql.SQLException;
import java.sql.Statement;

import org.firebirdsql.gds.impl.GDSHelper;

/**
 * JDBC 2.0 compliant implementation of {@link Statement} interface.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBStatement extends AbstractStatement {

    public FBStatement(GDSHelper gdsHelper, int rsType, int rsConcurrency, int rsHoldability,
            FBObjectListener.StatementListener statementListenet) throws SQLException {

        super(gdsHelper, rsType, rsConcurrency, rsHoldability, statementListenet);
    }

}
