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

import java.util.Map;
import java.util.TreeMap;

import static java.util.Collections.unmodifiableMap;
import static org.firebirdsql.jaybird.parser.CharSequenceComparison.caseInsensitiveComparator;

/**
 * Detects the type of statement, and - optionally - whether a DML statement has a {@code RETURNING} clause.
 * <p>
 * If the detected statement type is {@code UPDATE}, {@code DELETE}, {@code INSERT}, {@code UPDATE OR INSERT} and
 * {@code MERGE}, it identifies the affected table and - optionally - whether or not a {@code RETURNING} clause is
 * present (delegated to a {@link ReturningClauseDetector}).
 * </p>
 * <p>
 * The types of statements detected are informed by the needs of Jaybird, and may change between point releases.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
public final class StatementDetector implements TokenVisitor {

    private static final StateAfterStart INITIAL_OTHER =
            new StateAfterStart(ParserState.OTHER, LocalStatementType.OTHER);
    private static final Map<CharSequence, StateAfterStart> NEXT_AFTER_START;

    static {
        var nextAfterStart = new TreeMap<CharSequence, StateAfterStart>(caseInsensitiveComparator());
        var selectState = new StateAfterStart(ParserState.SELECT, LocalStatementType.SELECT);
        nextAfterStart.put("SELECT", selectState);
        // NOTE: This is a shortcut, if WITH is ever allowed as the first token of another statement type,
        // this must be changed to detect the first keyword after the entire WITH clause
        nextAfterStart.put("WITH", selectState);
        nextAfterStart.put("EXECUTE", new StateAfterStart(ParserState.EXECUTE, LocalStatementType.OTHER));
        nextAfterStart.put("UPDATE", new StateAfterStart(ParserState.UPDATE, LocalStatementType.UPDATE));
        nextAfterStart.put("DELETE", new StateAfterStart(ParserState.DELETE, LocalStatementType.DELETE));
        nextAfterStart.put("INSERT", new StateAfterStart(ParserState.INSERT, LocalStatementType.INSERT));
        nextAfterStart.put("MERGE", new StateAfterStart(ParserState.MERGE, LocalStatementType.MERGE));
        nextAfterStart.put("COMMIT", new StateAfterStart(ParserState.COMMIT_ROLLBACK, LocalStatementType.HARD_COMMIT));
        nextAfterStart.put("ROLLBACK",
                new StateAfterStart(ParserState.COMMIT_ROLLBACK, LocalStatementType.HARD_ROLLBACK));
        nextAfterStart.put("SET", new StateAfterStart(ParserState.SET, LocalStatementType.OTHER));
        NEXT_AFTER_START = unmodifiableMap(nextAfterStart);
    }

    private final boolean detectReturning;
    private LocalStatementType statementType = LocalStatementType.UNKNOWN;
    private ParserState parserState = ParserState.START;
    private Token tableNameToken;
    private ReturningClauseDetector returningClauseDetector;

    /**
     * Detect statement type and returning clause.
     *
     * @see #StatementDetector(boolean)
     */
    public StatementDetector() {
        this(true);
    }

    /**
     * Detect statement type and - optionally - returning clause.
     *
     * @param detectReturning
     *         {@code true} detect returning clause, {@code false} do not detect returning clause
     */
    public StatementDetector(boolean detectReturning) {
        this.detectReturning = detectReturning;
    }

    /**
     * Determines the local statement type of {@code sql}.
     * <p>
     * The return values of this method are decided by the needs of Jaybird, and do not necessarily cover all statement
     * types, and they may change between point releases.
     * </p>
     *
     * @param sql
     *         statement text
     * @return local statement type
     * @since 6
     */
    public static LocalStatementType determineLocalStatementType(String sql) {
        var detector = new StatementDetector(false);
        SqlParser.withReservedWords(FirebirdReservedWords.latest())
                .withVisitor(detector)
                .of(sql)
                .parse();
        return detector.getStatementType();
    }

    @Override
    public void visitToken(Token token, VisitorRegistrar visitorRegistrar) {
        if (token.isWhitespaceOrComment()) return;
        parserState = parserState.next(token, this);
        if (parserState.isFinalState()) {
            // We're not interested anymore
            visitorRegistrar.removeVisitor(this);
        } else if (parserState == ParserState.FIND_RETURNING) {
            // We're not interested anymore
            visitorRegistrar.removeVisitor(this);
            if (detectReturning) {
                // Use ReturningClauseDetector to handle detection
                returningClauseDetector = new ReturningClauseDetector();
                visitorRegistrar.addVisitor(returningClauseDetector);
                // Forward current token; if the current token is RETURNING, it is correctly detected
                returningClauseDetector.visitToken(token, visitorRegistrar);
            }
        }
    }

    @Override
    public void complete(VisitorRegistrar visitorRegistrar) {
        // do nothing
    }

    public StatementIdentification toStatementIdentification() {
        return new StatementIdentification(statementType, tableNameToken != null ? tableNameToken.text() : null,
                returningClauseDetected());
    }

    boolean returningClauseDetected() {
        return returningClauseDetector != null && returningClauseDetector.returningClauseDetected();
    }

    /**
     * @return detected statement type, {@code UNKNOWN} when no tokens have been received (nothing was parsed)
     */
    public LocalStatementType getStatementType() {
        return statementType;
    }

    Token getTableNameToken() {
        return tableNameToken;
    }

    private void updateStatementType(LocalStatementType statementType) {
        this.statementType = statementType;
        if (statementType == LocalStatementType.OTHER) {
            // clear any previously set table name
            setTableNameToken(null);
        }
    }

    private void setTableNameToken(Token tableNameToken) {
        this.tableNameToken = tableNameToken;
    }

    private enum ParserState {
        START {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (!(token instanceof ReservedToken)) {
                    return forceOther(detector);
                }
                StateAfterStart stateAfterStart =
                        NEXT_AFTER_START.getOrDefault(token.textAsCharSequence(), INITIAL_OTHER);
                detector.updateStatementType(stateAfterStart.type);
                return stateAfterStart.state;
            }
        },
        SELECT(true),
        EXECUTE {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token instanceof ReservedToken && token.equalsIgnoreCase("PROCEDURE")) {
                    detector.updateStatementType(LocalStatementType.EXECUTE_PROCEDURE);
                    return EXECUTE_PROCEDURE;
                }
                return forceOther(detector);
            }
        },
        EXECUTE_PROCEDURE(true),
        UPDATE {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token instanceof OperatorToken && token.equalsIgnoreCase("OR")) {
                    detector.updateStatementType(LocalStatementType.UNKNOWN);
                    return POSSIBLY_UPDATE_OR_INSERT;
                } else {
                    return DML_TARGET.next(token, detector);
                }
            }
        },
        POSSIBLY_UPDATE_OR_INSERT {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token instanceof ReservedToken && token.equalsIgnoreCase("INSERT")) {
                    detector.updateStatementType(LocalStatementType.UPDATE_OR_INSERT);
                    // Further detection can use the insert path
                    return INSERT;
                }
                return forceOther(detector);
            }
        },
        DELETE {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (!(token instanceof ReservedToken && token.equalsIgnoreCase("FROM"))) {
                    return forceOther(detector);
                }
                return DML_TARGET;
            }
        },
        // Shared by UPDATE, DELETE and MERGE
        DML_TARGET {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token.isValidIdentifier()) {
                    detector.setTableNameToken(token);
                    return DML_POSSIBLE_ALIAS;
                }
                return forceOther(detector);
            }
        },
        // Shared by UPDATE, DELETE and MERGE
        DML_POSSIBLE_ALIAS {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token.isValidIdentifier()) {
                    // either alias or possibly returning clause
                    return FIND_RETURNING;
                } else if (token instanceof ReservedToken) {
                    if (token.equalsIgnoreCase("AS")) {
                        return DML_ALIAS;
                    }
                    return FIND_RETURNING;
                }
                // Unexpected or invalid token at this point
                return forceOther(detector);
            }
        },
        DML_ALIAS {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token.isValidIdentifier()) {
                    return FIND_RETURNING;
                }
                // syntax error
                return forceOther(detector);
            }
        },
        INSERT {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token instanceof ReservedToken && token.equalsIgnoreCase("INTO")) {
                    return INSERT_INTO;
                }
                return forceOther(detector);
            }
        },
        INSERT_INTO {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token.isValidIdentifier()) {
                    detector.setTableNameToken(token);
                    return FIND_RETURNING;
                }
                // Syntax error
                return forceOther(detector);
            }
        },
        MERGE {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token instanceof ReservedToken && token.equalsIgnoreCase("INTO")) {
                    return DML_TARGET;
                }
                // Syntax error
                return forceOther(detector);
            }
        },
        // finding itself is offloaded to ReturningClauseDetector
        FIND_RETURNING,
        COMMIT_ROLLBACK {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token instanceof GenericToken && token.equalsIgnoreCase("WORK")) {
                    return COMMIT_ROLLBACK_WORK;
                }
                // RETAIN or syntax error
                return forceOther(detector);
            }
        },
        COMMIT_ROLLBACK_WORK {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                // RETAIN or syntax error
                return forceOther(detector);
            }
        },
        SET {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token instanceof GenericToken && token.equalsIgnoreCase("TRANSACTION")) {
                    detector.updateStatementType(LocalStatementType.SET_TRANSACTION);
                    return SET_TRANSACTION;
                }
                return forceOther(detector);
            }
        },
        SET_TRANSACTION(true),
        OTHER(true);

        private final boolean finalState;

        ParserState() {
            this(false);
        }

        ParserState(boolean finalState) {
            this.finalState = finalState;
        }

        final boolean isFinalState() {
            return finalState;
        }

        ParserState next(Token token, StatementDetector detector) {
            throw new IllegalStateException(
                    "State " + this + " is a terminal state and next(..) should not be invoked");
        }

        /**
         * Update {@code detector} to {@link LocalStatementType#OTHER}, and return {@link ParserState#OTHER}.
         *
         * @param detector
         *         detector to update
         * @return always {@link ParserState#OTHER}
         */
        private static ParserState forceOther(StatementDetector detector) {
            detector.updateStatementType(LocalStatementType.OTHER);
            return OTHER;
        }

    }

    private record StateAfterStart(ParserState state, LocalStatementType type) {
    }

}
