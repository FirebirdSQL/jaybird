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
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
@InternalApi
public final class StatementDetector implements TokenVisitor {

    private static final Map<String, ParserState> NEXT_AFTER_START;

    static {
        TreeMap<String, ParserState> nextAfterStart = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        nextAfterStart.put("SELECT", ParserState.SELECT);
        // NOTE: This is a shortcut, if WITH is ever allowed as the first token of another statement type,
        // this must be changed to detect the first keyword after the entire WITH clause
        nextAfterStart.put("WITH", ParserState.SELECT);
        nextAfterStart.put("EXECUTE", ParserState.EXECUTE);
        nextAfterStart.put("UPDATE", ParserState.UPDATE);
        nextAfterStart.put("DELETE", ParserState.DELETE);
        nextAfterStart.put("INSERT", ParserState.INSERT);
        nextAfterStart.put("MERGE", ParserState.MERGE);
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

    @Override
    public void visitToken(Token token, VisitorRegistrar visitorRegistrar) {
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
            ParserState next0(Token token, StatementDetector detector) {
                if (!(token instanceof ReservedToken)) {
                    detector.updateStatementType(LocalStatementType.OTHER);
                    return OTHER;
                }
                ParserState nextState = NEXT_AFTER_START.getOrDefault(token.text(), ParserState.OTHER);
                switch (nextState) {
                case SELECT:
                    detector.updateStatementType(LocalStatementType.SELECT);
                    break;
                case UPDATE:
                    // Might be UPDATE OR INSERT
                    detector.updateStatementType(LocalStatementType.UPDATE);
                    break;
                case DELETE:
                    detector.updateStatementType(LocalStatementType.DELETE);
                    break;
                case INSERT:
                    detector.updateStatementType(LocalStatementType.INSERT);
                    break;
                case MERGE:
                    detector.updateStatementType(LocalStatementType.MERGE);
                    break;
                default:
                    detector.updateStatementType(LocalStatementType.OTHER);
                    break;
                }
                return nextState;
            }
        },
        SELECT(true),
        EXECUTE {
            @Override
            ParserState next0(Token token, StatementDetector detector) {
                if (token instanceof ReservedToken && token.equalsIgnoreCase("PROCEDURE")) {
                    detector.updateStatementType(LocalStatementType.EXECUTE_PROCEDURE);
                    return EXECUTE_PROCEDURE;
                }
                return OTHER;
            }
        },
        EXECUTE_PROCEDURE(true),
        UPDATE {
            @Override
            ParserState next0(Token token, StatementDetector detector) {
                if (token instanceof OperatorToken && token.equalsIgnoreCase("OR")) {
                    detector.updateStatementType(LocalStatementType.UNKNOWN);
                    return POSSIBLY_UPDATE_OR_INSERT;
                } else {
                    return DML_TARGET.next0(token, detector);
                }
            }
        },
        POSSIBLY_UPDATE_OR_INSERT {
            @Override
            ParserState next0(Token token, StatementDetector detector) {
                if (token instanceof ReservedToken && token.equalsIgnoreCase("INSERT")) {
                    detector.updateStatementType(LocalStatementType.UPDATE_OR_INSERT);
                    // Further detection can use the insert path
                    return INSERT;
                }
                detector.updateStatementType(LocalStatementType.OTHER);
                return OTHER;
            }
        },
        DELETE {
            @Override
            ParserState next0(Token token, StatementDetector detector) {
                if (!(token instanceof ReservedToken && token.equalsIgnoreCase("FROM"))) {
                    detector.updateStatementType(LocalStatementType.OTHER);
                    return OTHER;
                }
                return DML_TARGET;
            }
        },
        // Shared by UPDATE, DELETE and MERGE
        DML_TARGET {
            @Override
            ParserState next0(Token token, StatementDetector detector) {
                if (token.isValidIdentifier()) {
                    detector.setTableNameToken(token);
                    return DML_POSSIBLE_ALIAS;
                } else {
                    detector.updateStatementType(LocalStatementType.OTHER);
                    return OTHER;
                }
            }
        },
        // Shared by UPDATE, DELETE and MERGE
        DML_POSSIBLE_ALIAS {
            @Override
            ParserState next0(Token token, StatementDetector detector) {
                if (token.isValidIdentifier()) {
                    // either alias or possibly returning clause
                    return FIND_RETURNING;
                } else if (token instanceof ReservedToken) {
                    if (token.equalsIgnoreCase("AS")) {
                        return DML_ALIAS;
                    }
                    return FIND_RETURNING;
                } else {
                    // Unexpected or invalid token at this point
                    detector.updateStatementType(LocalStatementType.OTHER);
                    return OTHER;
                }
            }
        },
        DML_ALIAS {
            @Override
            ParserState next0(Token token, StatementDetector detector) {
                if (token.isValidIdentifier()) {
                    return FIND_RETURNING;
                }
                // syntax error
                detector.updateStatementType(LocalStatementType.OTHER);
                return OTHER;
            }
        },
        INSERT {
            @Override
            ParserState next0(Token token, StatementDetector detector) {
                if (token instanceof ReservedToken && token.equalsIgnoreCase("INTO")) {
                    return INSERT_INTO;
                }
                detector.updateStatementType(LocalStatementType.OTHER);
                return OTHER;
            }
        },
        INSERT_INTO {
            @Override
            ParserState next0(Token token, StatementDetector detector) {
                if (token.isValidIdentifier()) {
                    detector.setTableNameToken(token);
                    return FIND_RETURNING;
                }
                // Syntax error
                detector.updateStatementType(LocalStatementType.OTHER);
                return OTHER;
            }
        },
        MERGE {
            @Override
            ParserState next0(Token token, StatementDetector detector) {
                if (token instanceof ReservedToken && token.equalsIgnoreCase("INTO")) {
                    return DML_TARGET;
                }
                // Syntax error
                detector.updateStatementType(LocalStatementType.OTHER);
                return OTHER;
            }
        },
        // finding itself is offloaded to ReturningClauseDetector
        FIND_RETURNING,
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

        final ParserState next(Token token, StatementDetector detector) {
            if (token.isWhitespaceOrComment()) {
                // Ignore whitespace and comments
                return this;
            }
            return next0(token, detector);
        }

        ParserState next0(Token token, StatementDetector detector) {
            throw new IllegalStateException(
                    "State " + this + " is a terminal state and next(..) should not be invoked");
        }

    }

}
