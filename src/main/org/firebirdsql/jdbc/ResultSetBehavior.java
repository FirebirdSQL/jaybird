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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.util.InternalApi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.function.Consumer;

/**
 * Aggregate type for result set behavior and logic for deriving supported combinations of type, concurrency and
 * holdability.
 *
 * @author Mark Rotteveel
 * @since 6
 */
@InternalApi
public final class ResultSetBehavior {

    //@formatter:off
    private static final int NO_BIT_SET     = 0;
    private static final int SCROLLABLE_BIT = 0b0001;
    // NOTE: As neither Jaybird nor Firebird support sensitive result sets, this bit is effectively unused
    private static final int SENSITIVE_BIT  = 0b0010;
    private static final int UPDATABLE_BIT  = 0b0100;
    private static final int HOLDABLE_BIT   = 0b1000;
    private static final int ALL_BITS_SET = SCROLLABLE_BIT | SENSITIVE_BIT | UPDATABLE_BIT | HOLDABLE_BIT;
    private static final Consumer<SQLWarning> NO_WARNINGS = w -> {};
    //@formatter:on

    private static final ResultSetBehavior[] cache = new ResultSetBehavior[ALL_BITS_SET + 1];

    private final int bitset;

    private ResultSetBehavior(int bitset) {
        this.bitset = bitset;
    }

    /**
     * Result set type.
     * <p>
     * <b>NOTE:</b> The current implementation will never return {@link ResultSet#TYPE_SCROLL_SENSITIVE}, because it is
     * always downgraded to {@link ResultSet#TYPE_SCROLL_INSENSITIVE} in {@link #of(int, int, int, Consumer)}.
     * </p>
     *
     * @return result set type; one of {@link ResultSet#TYPE_FORWARD_ONLY}, {@link ResultSet#TYPE_SCROLL_INSENSITIVE} or
     * {@link ResultSet#TYPE_SCROLL_SENSITIVE}
     * @see #isForwardOnly()
     * @see #isScrollable()
     * @see #isInsensitive()
     * @see #isSensitive()
     */
    public int type() {
        if (isForwardOnly()) {
            return ResultSet.TYPE_FORWARD_ONLY;
        } else if (isInsensitive()) {
            return ResultSet.TYPE_SCROLL_INSENSITIVE;
        } else {
            // NOTE: In practice this is not returned, because SENSITIVE_BIT is never set!
            return ResultSet.TYPE_SCROLL_SENSITIVE;
        }
    }

    /**
     * Forward-only result set.
     *
     * @return {@code true} if the result set is forward-only ({@link ResultSet#TYPE_FORWARD_ONLY})
     * @see #isScrollable()
     * @see #type()
     */
    public boolean isForwardOnly() {
        return (bitset & SCROLLABLE_BIT) == 0;
    }

    /**
     * Scrollable result set.
     *
     * @return {@code true} if the result set is scrollable ({@link ResultSet#TYPE_SCROLL_INSENSITIVE} or
     * {@link ResultSet#TYPE_SCROLL_SENSITIVE})
     * @see #isForwardOnly()
     * @see #type()
     */
    public boolean isScrollable() {
        return (bitset & SCROLLABLE_BIT) != 0;
    }

    /**
     * Result set is insensitive to changes in the data source.
     *
     * @return {@code true} if the result set is insensitive to changes ({@link ResultSet#TYPE_FORWARD_ONLY} or
     * {@link ResultSet#TYPE_SCROLL_INSENSITIVE})
     * @see #isSensitive()
     * @see #type()
     */
    public boolean isInsensitive() {
        return (bitset & SENSITIVE_BIT) == 0;
    }

    /**
     * Result set is sensitive to changes in the data source.
     * <p>
     * <b>NOTE:</b> In the current implementation, this always returns {@code false}.
     * </p>
     *
     * @return {@code true} if the result is sensitive to changes ({@link ResultSet#TYPE_SCROLL_SENSITIVE})
     * @see #isInsensitive()
     * @see #type()
     */
    public boolean isSensitive() {
        return (bitset & SENSITIVE_BIT) != 0;
    }

    /**
     * Result set concurrency.
     *
     * @return result set concurrency; one of {@link ResultSet#CONCUR_READ_ONLY} or {@link ResultSet#CONCUR_UPDATABLE}
     * @see #isReadOnly()
     * @see #isUpdatable()
     */
    public int concurrency() {
        if (isReadOnly()) {
            return ResultSet.CONCUR_READ_ONLY;
        } else {
            return ResultSet.CONCUR_UPDATABLE;
        }
    }

    /**
     * Read-only result set.
     *
     * @return {@code true} if the result set is read-only ({@link ResultSet#CONCUR_READ_ONLY})
     * @see #isUpdatable()
     * @see #concurrency()
     */
    public boolean isReadOnly() {
        return (bitset & UPDATABLE_BIT) == 0;
    }

    /**
     * Updatable result set.
     *
     * @return {@code true} if the result set is updatable ({@link ResultSet#CONCUR_UPDATABLE})
     * @see #isReadOnly()
     * @see #concurrency()
     */
    public boolean isUpdatable() {
        return (bitset & UPDATABLE_BIT) != 0;
    }

    /**
     * Result set holdability.
     *
     * @return result set holdability; one of {@link ResultSet#CLOSE_CURSORS_AT_COMMIT} or
     * {@link ResultSet#HOLD_CURSORS_OVER_COMMIT}
     * @see #isCloseCursorsAtCommit()
     * @see #isHoldCursorsOverCommit()
     */
    public int holdability() {
        if (isCloseCursorsAtCommit()) {
            return ResultSet.CLOSE_CURSORS_AT_COMMIT;
        } else {
            return ResultSet.HOLD_CURSORS_OVER_COMMIT;
        }
    }

    /**
     * Result set is closed at commit.
     *
     * @return {@code true} if result set is closed at commit ({@link ResultSet#CLOSE_CURSORS_AT_COMMIT})
     * @see #isHoldCursorsOverCommit()
     * @see #holdability()
     */
    public boolean isCloseCursorsAtCommit() {
        return (bitset & HOLDABLE_BIT) == 0;
    }

    /**
     * Result set is held (remains open) over commit.
     *
     * @return {@code true} if result is held over commit ({@link ResultSet#HOLD_CURSORS_OVER_COMMIT})
     * @see #isCloseCursorsAtCommit()
     * @see #holdability() 
     */
    public boolean isHoldCursorsOverCommit() {
        return (bitset & HOLDABLE_BIT) != 0;
    }

    /**
     * Return a &mdash; possibly cached &mdash; result set behaviour with concurrency
     * changed to {@link ResultSet#CONCUR_READ_ONLY}.
     * <p>
     * Type and holdability may be upgraded or downgraded if required if the new combination would not be supported. In
     * the current implementation, this doesn't actually happen, and type and concurrency are preserved.
     * </p>
     *
     * @return read-only result set behaviour
     */
    public ResultSetBehavior withReadOnly() {
        if (isReadOnly()) return this;
        return getOrCreateInstance(bitset & ~UPDATABLE_BIT);
    }

    /**
     * Returns a &mdash; possibly cached &mdash; result set behavior for the type, concurrency and holdability.
     * <p>
     * Unsupported, but valid, combinations may result in upgrading or downgrading the type, concurrency and/or
     * holdability. In the current implementation this only happens for type.
     * </p>
     *
     * @param type
     *         result set type; one of {@link ResultSet#TYPE_FORWARD_ONLY}, {@link ResultSet#TYPE_SCROLL_INSENSITIVE} or
     *         {@link ResultSet#TYPE_SCROLL_SENSITIVE}
     * @param concurrency
     *         result set concurrency; one of {@link ResultSet#CONCUR_READ_ONLY} or {@link ResultSet#CONCUR_UPDATABLE}
     * @param holdability
     *         result set holdability; one of {@link ResultSet#CLOSE_CURSORS_AT_COMMIT} or
     *         {@link ResultSet#HOLD_CURSORS_OVER_COMMIT}
     * @return derived result set behavior
     * @throws SQLException
     *         for invalid values of {@code type}, {@code concurrency} or {@code holdability} (i.e. not defined by JDBC)
     * @see #of(int, int, int, Consumer)
     */
    public static ResultSetBehavior of(int type, int concurrency, int holdability) throws SQLException {
        return of(type, concurrency, holdability, NO_WARNINGS);
    }

    /**
     * Returns a &mdash; possibly cached &mdash; result set behavior for the type, concurrency and holdability.
     * <p>
     * Unsupported, but valid, combinations may result in upgrading or downgrading the type, concurrency and/or
     * holdability. In the current implementation this only happens for type.
     * </p>
     *
     * @param type
     *         result set type; one of {@link ResultSet#TYPE_FORWARD_ONLY}, {@link ResultSet#TYPE_SCROLL_INSENSITIVE} or
     *         {@link ResultSet#TYPE_SCROLL_SENSITIVE}
     * @param concurrency
     *         result set concurrency; one of {@link ResultSet#CONCUR_READ_ONLY} or {@link ResultSet#CONCUR_UPDATABLE}
     * @param holdability
     *         result set holdability; one of {@link ResultSet#CLOSE_CURSORS_AT_COMMIT} or
     *         {@link ResultSet#HOLD_CURSORS_OVER_COMMIT}
     * @param warningConsumer
     *         consumer for reporting a warning when the type, concurrency or holdability is upgraded or downgraded
     * @return derived result set behavior
     * @throws SQLException
     *         for invalid values of {@code type}, {@code concurrency} or {@code holdability} (i.e. not defined by JDBC)
     * @see #of(int, int, int, Consumer)
     */
    public static ResultSetBehavior of(int type, int concurrency, int holdability, Consumer<SQLWarning> warningConsumer)
            throws SQLException {
        int bitset = switch (type) {
            case ResultSet.TYPE_FORWARD_ONLY -> {
                if (holdability == ResultSet.HOLD_CURSORS_OVER_COMMIT) {
                    warningConsumer.accept(
                            FbExceptionBuilder.forWarning(JaybirdErrorCodes.jb_resultSetTypeUpgradeReasonHoldability)
                                    .toSQLException(SQLWarning.class));
                    // Upgrade to scrollable so we cache the result set to provide the holdable semantics
                    yield SCROLLABLE_BIT;
                }
                yield NO_BIT_SET;
            }
            case ResultSet.TYPE_SCROLL_INSENSITIVE -> SCROLLABLE_BIT;
            case ResultSet.TYPE_SCROLL_SENSITIVE -> {
                warningConsumer.accept(
                        FbExceptionBuilder.forWarning(JaybirdErrorCodes.jb_resultSetTypeDowngradeReasonScrollSensitive)
                                .toSQLException(SQLWarning.class));
                // Given we downgrade to TYPE_SCROLL_INSENSITIVE, the SENSITIVE_BIT is not set
                yield SCROLLABLE_BIT;
            }
            default -> throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_invalidResultSetType)
                    .toSQLException();
        };
        bitset |= switch (concurrency) {
            case ResultSet.CONCUR_READ_ONLY -> NO_BIT_SET;
            case ResultSet.CONCUR_UPDATABLE -> UPDATABLE_BIT;
            default ->
                    throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_invalidResultSetConcurrency)
                            .toSQLException();
        };
        bitset |= switch (holdability) {
            case ResultSet.CLOSE_CURSORS_AT_COMMIT -> NO_BIT_SET;
            case ResultSet.HOLD_CURSORS_OVER_COMMIT -> HOLDABLE_BIT;
            default ->
                    throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_invalidResultSetHoldability)
                            .toSQLException();
        };
        return getOrCreateInstance(bitset);
    }

    private static ResultSetBehavior getOrCreateInstance(int bitset) {
        // We accept that under concurrent load this might result in spurious cache misses and multiple instantiations
        ResultSetBehavior resultSetBehavior = cache[bitset];
        if (resultSetBehavior != null) return resultSetBehavior;
        return cache[bitset] = new ResultSetBehavior(bitset);
    }

}
