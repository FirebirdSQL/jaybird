// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.util.InternalApi;

import static java.util.Objects.requireNonNull;

/**
 * Statement type identification.
 *
 * @author Mark Rotteveel
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
