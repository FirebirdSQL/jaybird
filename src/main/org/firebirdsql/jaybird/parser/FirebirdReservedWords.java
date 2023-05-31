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

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableSet;

/**
 * All reserved words per Firebird version.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
public enum FirebirdReservedWords implements ReservedWords {

    // TODO Add other versions if it turns out we do need reserved words per version

    FIREBIRD_5_0("ADD", "ADMIN", "ALL", "ALTER", "AND", "ANY", "AS", "AT", "AVG", "BEGIN", "BETWEEN", "BIGINT",
            "BINARY", "BIT_LENGTH", "BLOB", "BOOLEAN", "BOTH", "BY", "CASE", "CAST", "CHAR", "CHARACTER",
            "CHARACTER_LENGTH", "CHAR_LENGTH", "CHECK", "CLOSE", "COLLATE", "COLUMN", "COMMENT", "COMMIT", "CONNECT",
            "CONSTRAINT", "CORR", "COUNT", "COVAR_POP", "COVAR_SAMP", "CREATE", "CROSS", "CURRENT",
            "CURRENT_CONNECTION", "CURRENT_DATE", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
            "CURRENT_TRANSACTION", "CURRENT_USER", "CURSOR", "DATE", "DAY", "DEC", "DECFLOAT", "DECIMAL", "DECLARE",
            "DEFAULT", "DELETE", "DELETING", "DETERMINISTIC", "DISCONNECT", "DISTINCT", "DOUBLE", "DROP", "ELSE", "END",
            "ESCAPE", "EXECUTE", "EXISTS", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FILTER", "FLOAT", "FOR", "FOREIGN",
            "FROM", "FULL", "FUNCTION", "GDSCODE", "GLOBAL", "GRANT", "GROUP", "HAVING", "HOUR", "IN", "INDEX", "INNER",
            "INSENSITIVE", "INSERT", "INSERTING", "INT", "INT128", "INTEGER", "INTO", "IS", "JOIN", "LATERAL",
            "LEADING", "LEFT", "LIKE", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LONG", "LOWER", "MAX", "MERGE", "MIN",
            "MINUTE", "MONTH", "NATIONAL", "NATURAL", "NCHAR", "NO", "NOT", "NULL", "NUMERIC", "OCTET_LENGTH", "OF",
            "OFFSET", "ON", "ONLY", "OPEN", "OR", "ORDER", "OUTER", "OVER", "PARAMETER", "PLAN", "POSITION",
            "POST_EVENT", "PRECISION", "PRIMARY", "PROCEDURE", "PUBLICATION", "RDB$DB_KEY", "RDB$ERROR",
            "RDB$GET_CONTEXT", "RDB$GET_TRANSACTION_CN", "RDB$RECORD_VERSION", "RDB$ROLE_IN_USE", "RDB$SET_CONTEXT",
            "RDB$SYSTEM_PRIVILEGE", "REAL", "RECORD_VERSION", "RECREATE", "RECURSIVE", "REFERENCES", "REGR_AVGX",
            "REGR_AVGY", "REGR_COUNT", "REGR_INTERCEPT", "REGR_R2", "REGR_SLOPE", "REGR_SXX", "REGR_SXY", "REGR_SYY",
            "RELEASE", "RESETTING", "RETURN", "RETURNING_VALUES", "RETURNS", "REVOKE", "RIGHT", "ROLLBACK", "ROW",
            "ROWS", "ROW_COUNT", "SAVEPOINT", "SCROLL", "SECOND", "SELECT", "SENSITIVE", "SET", "SIMILAR", "SMALLINT",
            "SOME", "SQLCODE", "SQLSTATE", "START", "STDDEV_POP", "STDDEV_SAMP", "SUM", "TABLE", "THEN", "TIME",
            "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRIGGER", "TRIM", "TRUE", "UNBOUNDED",
            "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "UPDATING", "UPPER", "USER", "USING", "VALUE", "VALUES",
            "VARBINARY", "VARCHAR", "VARIABLE", "VARYING", "VAR_POP", "VAR_SAMP", "VIEW", "WHEN", "WHERE", "WHILE",
            "WINDOW", "WITH", "WITHOUT", "YEAR"),
    ;

    private final Set<CharSequence> reservedWords;

    FirebirdReservedWords(CharSequence... reservedWords) {
        this.reservedWords = toUnmodifiableCaseInsensitiveSet(Arrays.asList(reservedWords));
    }

    /**
     * Reserved words for latest known Firebird version.
     *
     * @return reserved words for latest known Firebird version
     */
    public static FirebirdReservedWords latest() {
        return FIREBIRD_5_0;
    }

    private static Set<CharSequence> toUnmodifiableCaseInsensitiveSet(Collection<CharSequence> values) {
        Set<CharSequence> set = new TreeSet<>(CharSequenceComparison.caseInsensitiveComparator());
        set.addAll(values);
        return unmodifiableSet(set);
    }

    @Override
    public boolean isReservedWord(CharSequence tokenText) {
        return reservedWords.contains(tokenText);
    }
}
