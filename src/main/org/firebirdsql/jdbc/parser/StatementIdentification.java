/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.jdbc.parser;

import org.firebirdsql.util.InternalApi;

import static java.util.Objects.requireNonNull;

/**
 * Statement identification (primarily for generated keys support).
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
@InternalApi
public class StatementIdentification {

    private final DmlStatementType dmlStatementType;
    private final String tableName;
    private final boolean returningClauseDetected;

    StatementIdentification(DmlStatementType dmlStatementType, String tableName, boolean returningClauseDetected) {
        this.dmlStatementType = requireNonNull(dmlStatementType, "dmlStatementType");
        this.tableName = tableName;
        this.returningClauseDetected = returningClauseDetected;
    }

    public DmlStatementType getDmlStatementType() {
        return dmlStatementType;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean returningClauseDetected() {
        return returningClauseDetected;
    }
}
