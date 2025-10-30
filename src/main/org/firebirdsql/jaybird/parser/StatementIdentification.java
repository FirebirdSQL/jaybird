// SPDX-FileCopyrightText: Copyright 2021-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

import static java.util.Objects.requireNonNull;

/**
 * Statement type identification.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
@NullMarked
public final class StatementIdentification {

    private final LocalStatementType statementType;
    private final @Nullable String schema;
    private final @Nullable String tableName;
    private final boolean returningClauseDetected;

    StatementIdentification(LocalStatementType statementType, @Nullable Token schema, @Nullable Token tableName,
            boolean returningClauseDetected) {
        this.statementType = requireNonNull(statementType, "statementType");
        this.schema = normalizeObjectName(schema);
        this.tableName = normalizeObjectName(tableName);
        this.returningClauseDetected = returningClauseDetected;
    }

    public LocalStatementType getStatementType() {
        return statementType;
    }

    /**
     * Schema, if this is a DML statement (other than {@code SELECT}), and if the table is qualified.
     * <p>
     * It reports the name normalized to its metadata storage representation.
     * </p>
     *
     * @return schema, {@code null} if the table was not qualified, or for {@code SELECT} and other non-DML statements
     * @since 7
     */
    public @Nullable String getSchema() {
        return schema;
    }

    /**
     * Table name, if this is a DML statement (other than {@code SELECT}).
     * <p>
     * It reports the name normalized to its metadata storage representation.
     * </p>
     *
     * @return table name, {@code null} for {@code SELECT} and other non-DML statements
     */
    public @Nullable String getTableName() {
        return tableName;
    }

    public boolean returningClauseDetected() {
        return returningClauseDetected;
    }

    /**
     * Normalizes an object name from the parser to its storage representation.
     * <p>
     * Unquoted identifiers are uppercased, and quoted identifiers are returned with the quotes stripped and doubled
     * double quotes replaced by a single double quote.
     * </p>
     *
     * @param objectToken
     *         token with the object name (can be {@code null})
     * @return normalized object name, or {@code null} if {@code objectToken} was {@code null}
     */
    private static @Nullable String normalizeObjectName(@Nullable Token objectToken) {
        if (objectToken == null) return null;
        if (objectToken instanceof QuotedIdentifierToken quotedIdentifier) {
            return quotedIdentifier.name();
        }
        return objectToken.text().toUpperCase(Locale.ROOT);
    }

}
