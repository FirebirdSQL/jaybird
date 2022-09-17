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
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.util.InternalApi;

import static java.util.Objects.requireNonNull;

/**
 * Statement type identification.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
@InternalApi
public final class StatementIdentification {

    private final LocalStatementType statementType;
    private final String tableName;
    private final boolean returningClauseDetected;

    StatementIdentification(LocalStatementType statementType, String tableName, boolean returningClauseDetected) {
        this.statementType = requireNonNull(statementType, "statementType");
        this.tableName = tableName;
        this.returningClauseDetected = returningClauseDetected;
    }

    public LocalStatementType getStatementType() {
        return statementType;
    }

    /**
     * Table name, if this is a DML statement (other than {@code SELECT}).
     *
     * @return Table name, {@code null} for {@code SELECT} and other non-DML statements
     */
    public String getTableName() {
        return tableName;
    }

    public boolean returningClauseDetected() {
        return returningClauseDetected;
    }
}
