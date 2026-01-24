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
import org.jspecify.annotations.NullMarked;

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
@NullMarked
@InternalApi
public enum FirebirdReservedWords implements ReservedWords {

    // Order is intentionally from higher versions to lower versions, see of(AbstractVersion) and latest()
    FIREBIRD_5_0(5, 0, "ADD", "ADMIN", "ALL", "ALTER", "AND", "ANY", "AS", "AT", "AVG", "BEGIN", "BETWEEN", "BIGINT",
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
            "ROWS", "ROW_COUNT", "SAVEPOINT", "SCHEMA", "SCROLL", "SECOND", "SELECT", "SENSITIVE", "SET", "SIMILAR",
            "SMALLINT", "SOME", "SQLCODE", "SQLSTATE", "START", "STDDEV_POP", "STDDEV_SAMP", "SUM", "TABLE", "THEN",
            "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRIGGER", "TRIM", "TRUE",
            "UNBOUNDED", "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "UPDATING", "UPPER", "USER", "USING", "VALUE",
            "VALUES", "VARBINARY", "VARCHAR", "VARIABLE", "VARYING", "VAR_POP", "VAR_SAMP", "VIEW", "WHEN", "WHERE",
            "WHILE", "WINDOW", "WITH", "WITHOUT", "YEAR"),
    FIREBIRD_4_0(4, 0, "ADD", "ADMIN", "ALL", "ALTER", "AND", "ANY", "AS", "AT", "AVG", "BEGIN", "BETWEEN", "BIGINT",
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
    FIREBIRD_3_0(3, 0, "ADD", "ADMIN", "ALL", "ALTER", "AND", "ANY", "AS", "AT", "AVG", "BEGIN", "BETWEEN", "BIGINT",
            "BIT_LENGTH", "BLOB", "BOOLEAN", "BOTH", "BY", "CASE", "CAST", "CHAR", "CHARACTER", "CHARACTER_LENGTH",
            "CHAR_LENGTH", "CHECK", "CLOSE", "COLLATE", "COLUMN", "COMMIT", "CONNECT", "CONSTRAINT", "CORR", "COUNT",
            "COVAR_POP", "COVAR_SAMP", "CREATE", "CROSS", "CURRENT", "CURRENT_CONNECTION", "CURRENT_DATE",
            "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_TRANSACTION", "CURRENT_USER", "CURSOR",
            "DATE", "DAY", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELETE", "DELETING", "DETERMINISTIC", "DISCONNECT",
            "DISTINCT", "DOUBLE", "DROP", "ELSE", "END", "ESCAPE", "EXECUTE", "EXISTS", "EXTERNAL", "EXTRACT", "FALSE",
            "FETCH", "FILTER", "FLOAT", "FOR", "FOREIGN", "FROM", "FULL", "FUNCTION", "GDSCODE", "GLOBAL", "GRANT",
            "GROUP", "HAVING", "HOUR", "IN", "INDEX", "INNER", "INSENSITIVE", "INSERT", "INSERTING", "INT", "INTEGER",
            "INTO", "IS", "JOIN", "LEADING", "LEFT", "LIKE", "LONG", "LOWER", "MAX", "MERGE", "MIN", "MINUTE", "MONTH",
            "NATIONAL", "NATURAL", "NCHAR", "NO", "NOT", "NULL", "NUMERIC", "OCTET_LENGTH", "OF", "OFFSET", "ON",
            "ONLY", "OPEN", "OR", "ORDER", "OUTER", "OVER", "PARAMETER", "PLAN", "POSITION", "POST_EVENT", "PRECISION",
            "PRIMARY", "PROCEDURE", "RDB$DB_KEY", "RDB$RECORD_VERSION", "REAL", "RECORD_VERSION", "RECREATE",
            "RECURSIVE", "REFERENCES", "REGR_AVGX", "REGR_AVGY", "REGR_COUNT", "REGR_INTERCEPT", "REGR_R2",
            "REGR_SLOPE", "REGR_SXX", "REGR_SXY", "REGR_SYY", "RELEASE", "RETURN", "RETURNING_VALUES", "RETURNS",
            "REVOKE", "RIGHT", "ROLLBACK", "ROW", "ROWS", "ROW_COUNT", "SAVEPOINT", "SCROLL", "SECOND", "SELECT",
            "SENSITIVE", "SET", "SIMILAR", "SMALLINT", "SOME", "SQLCODE", "SQLSTATE", "START", "STDDEV_POP",
            "STDDEV_SAMP", "SUM", "TABLE", "THEN", "TIME", "TIMESTAMP", "TO", "TRAILING", "TRIGGER", "TRIM", "TRUE",
            "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "UPDATING", "UPPER", "USER", "USING", "VALUE", "VALUES", "VARCHAR",
            "VARIABLE", "VARYING", "VAR_POP", "VAR_SAMP", "VIEW", "WHEN", "WHERE", "WHILE", "WITH", "YEAR"),
    FIREBIRD_2_5(2, 5, "ADD", "ADMIN", "ALL", "ALTER", "AND", "ANY", "AS", "AT", "AVG", "BEGIN", "BETWEEN", "BIGINT",
            "BIT_LENGTH", "BLOB", "BOTH", "BY", "CASE", "CAST", "CHAR", "CHARACTER", "CHARACTER_LENGTH", "CHAR_LENGTH",
            "CHECK", "CLOSE", "COLLATE", "COLUMN", "COMMIT", "CONNECT", "CONSTRAINT", "COUNT", "CREATE", "CROSS",
            "CURRENT", "CURRENT_CONNECTION", "CURRENT_DATE", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
            "CURRENT_TRANSACTION", "CURRENT_USER", "CURSOR", "DATE", "DAY", "DEC", "DECIMAL", "DECLARE", "DEFAULT",
            "DELETE", "DISCONNECT", "DISTINCT", "DOUBLE", "DROP", "ELSE", "END", "ESCAPE", "EXECUTE", "EXISTS",
            "EXTERNAL", "EXTRACT", "FETCH", "FILTER", "FLOAT", "FOR", "FOREIGN", "FROM", "FULL", "FUNCTION", "GDSCODE",
            "GLOBAL", "GRANT", "GROUP", "HAVING", "HOUR", "IN", "INDEX", "INNER", "INSENSITIVE", "INSERT", "INT",
            "INTEGER", "INTO", "IS", "JOIN", "LEADING", "LEFT", "LIKE", "LOCALTIME", "LOCALTIMESTAMP", "LONG", "LOWER",
            "MAX", "MAXIMUM_SEGMENT", "MERGE", "MIN", "MINUTE", "MONTH", "NATIONAL", "NATURAL", "NCHAR", "NO", "NOT",
            "NULL", "NUMERIC", "OCTET_LENGTH", "OF", "ON", "ONLY", "OPEN", "OR", "ORDER", "OUTER", "PARAMETER", "PLAN",
            "POSITION", "POST_EVENT", "PRECISION", "PRIMARY", "PROCEDURE", "RDB$DB_KEY", "REAL", "RECORD_VERSION",
            "RECREATE", "RECURSIVE", "REFERENCES", "RELEASE", "RETURNING_VALUES", "RETURNS", "REVOKE", "RIGHT",
            "ROLLBACK", "ROWS", "ROW_COUNT", "SAVEPOINT", "SECOND", "SELECT", "SENSITIVE", "SET", "SIMILAR", "SMALLINT",
            "SOME", "SQLCODE", "SQLSTATE", "START", "SUM", "TABLE", "THEN", "TIME", "TIMESTAMP", "TO", "TRAILING",
            "TRIGGER", "TRIM", "UNION", "UNIQUE", "UPDATE", "UPPER", "USER", "USING", "VALUE", "VALUES", "VARCHAR",
            "VARIABLE", "VARYING", "VIEW", "WHEN", "WHERE", "WHILE", "WITH", "YEAR"),
    FIREBIRD_2_1(2, 1, "ACTIVE", "ADD", "ADMIN", "AFTER", "ALL", "ALTER", "AND", "ANY", "AS", "ASC", "ASCENDING", "AT",
            "AUTO", "AVG", "BEFORE", "BEGIN", "BETWEEN", "BIGINT", "BIT_LENGTH", "BLOB", "BOTH", "BY", "CASE", "CAST",
            "CHAR", "CHARACTER", "CHARACTER_LENGTH", "CHAR_LENGTH", "CHECK", "CLOSE", "COLLATE", "COLUMN", "COMMIT",
            "COMMITTED", "COMPUTED", "CONDITIONAL", "CONNECT", "CONSTRAINT", "CONTAINING", "COUNT", "CREATE", "CROSS",
            "CSTRING", "CURRENT", "CURRENT_CONNECTION", "CURRENT_DATE", "CURRENT_ROLE", "CURRENT_TIME",
            "CURRENT_TIMESTAMP", "CURRENT_TRANSACTION", "CURRENT_USER", "CURSOR", "DATABASE", "DATE", "DAY", "DEBUG",
            "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELETE", "DESC", "DESCENDING", "DISCONNECT", "DISTINCT", "DO",
            "DOMAIN", "DOUBLE", "DROP", "ELSE", "END", "ENTRY_POINT", "ESCAPE", "EXCEPTION", "EXECUTE", "EXISTS",
            "EXIT", "EXTERNAL", "EXTRACT", "FETCH", "FILE", "FILTER", "FLOAT", "FOR", "FOREIGN", "FROM", "FULL",
            "FUNCTION", "GDSCODE", "GENERATOR", "GEN_ID", "GLOBAL", "GRANT", "GROUP", "HAVING", "HOUR", "IF", "IN",
            "INACTIVE", "INDEX", "INNER", "INPUT_TYPE", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTO", "IS",
            "ISOLATION", "JOIN", "KEY", "LEADING", "LEFT", "LENGTH", "LEVEL", "LIKE", "LONG", "LOWER", "MANUAL", "MAX",
            "MAXIMUM_SEGMENT", "MERGE", "MIN", "MINUTE", "MODULE_NAME", "MONTH", "NAMES", "NATIONAL", "NATURAL",
            "NCHAR", "NO", "NOT", "NULL", "NUMERIC", "OCTET_LENGTH", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR",
            "ORDER", "OUTER", "OUTPUT_TYPE", "OVERFLOW", "PAGE", "PAGES", "PAGE_SIZE", "PARAMETER", "PASSWORD", "PLAN",
            "POSITION", "POST_EVENT", "PRECISION", "PRIMARY", "PRIVILEGES", "PROCEDURE", "PROTECTED", "RDB$DB_KEY",
            "READ", "REAL", "RECORD_VERSION", "RECREATE", "RECURSIVE", "REFERENCES", "RELEASE", "RESERV", "RESERVING",
            "RETAIN", "RETURNING_VALUES", "RETURNS", "REVOKE", "RIGHT", "ROLLBACK", "ROWS", "ROW_COUNT", "SAVEPOINT",
            "SCHEMA", "SECOND", "SEGMENT", "SELECT", "SENSITIVE", "SET", "SHADOW", "SHARED", "SINGULAR", "SIZE",
            "SMALLINT", "SNAPSHOT", "SOME", "SORT", "SQLCODE", "STABILITY", "START", "STARTING", "STARTS", "STATISTICS",
            "SUB_TYPE", "SUM", "SUSPEND", "TABLE", "THEN", "TIME", "TIMESTAMP", "TO", "TRAILING", "TRANSACTION",
            "TRIGGER", "TRIM", "UNCOMMITTED", "UNION", "UNIQUE", "UPDATE", "UPPER", "USER", "USING", "VALUE", "VALUES",
            "VARCHAR", "VARIABLE", "VARYING", "VIEW", "WAIT", "WHEN", "WHERE", "WHILE", "WITH", "WORK", "WRITE",
            "YEAR"),
    ;

    private final int major;
    private final int minor;
    private final Set<CharSequence> reservedWords;

    FirebirdReservedWords(int major, int minor, CharSequence... reservedWords) {
        this.major = major;
        this.minor = minor;
        this.reservedWords = toUnmodifiableCaseInsensitiveSet(Arrays.asList(reservedWords));
    }

    /**
     * @return Firebird major version
     */
    int major() {
        return major;
    }

    /**
     * @return Firebird minor version
     */
    int minor() {
        return minor;
    }

    /**
     * Reserved words for latest known Firebird version.
     *
     * @return reserved words for latest known Firebird version
     */
    public static FirebirdReservedWords latest() {
        return FIREBIRD_5_0;
    }

    /**
     * Reserved words for the specified Firebird version.
     *
     * @param major
     *         Firebird major version
     * @param minor Firebird minor version
     * @return reserved words for the specified version, or the closest known version
     * @since 6.0.5
     */
    public static FirebirdReservedWords of(int major, int minor) {
        FirebirdReservedWords[] values = values();
        for (FirebirdReservedWords reservedWords : values) {
            if (major > reservedWords.major ||
                    major == reservedWords.major && minor >= reservedWords.minor) {
                return reservedWords;
            }
        }
        // fall back to the lowest known version
        return values[values.length - 1];
    }

    private static Set<CharSequence> toUnmodifiableCaseInsensitiveSet(Collection<CharSequence> values) {
        Set<CharSequence> set = new TreeSet<>(CharSequenceComparison.caseInsensitiveComparator());
        set.addAll(values);
        return unmodifiableSet(set);
    }

    @Override
    public final boolean isReservedWord(CharSequence tokenText) {
        return reservedWords.contains(tokenText);
    }

}
