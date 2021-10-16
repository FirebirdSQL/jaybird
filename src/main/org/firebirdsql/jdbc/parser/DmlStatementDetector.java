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

import java.util.Map;
import java.util.TreeMap;

import static java.util.Collections.unmodifiableMap;

/**
 * Detects if the visited statement is a non-{@code SELECT} DML statement.
 * <p>
 * The detected statement types are {@code UPDATE}, {@code DELETE}, {@code INSERT}, {@code UPDATE OR INSERT} and
 * {@code MERGE}, including the affected table and whether or not a {@code RETURNING} clause is present (delegated to
 * a {@link ReturningClauseDetector}).
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
@InternalApi
public final class DmlStatementDetector implements TokenVisitor {

    private static final Map<String, ParserState> NEXT_AFTER_START;

    static {
        TreeMap<String, ParserState> nextAfterStart = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        nextAfterStart.put("UPDATE", ParserState.UPDATE);
        nextAfterStart.put("DELETE", ParserState.DELETE);
        nextAfterStart.put("INSERT", ParserState.INSERT);
        nextAfterStart.put("MERGE", ParserState.MERGE);
        NEXT_AFTER_START = unmodifiableMap(nextAfterStart);
    }

    private DmlStatementType statementType = DmlStatementType.UNKNOWN;
    private ParserState parserState = ParserState.START;
    private Token tableNameToken;
    private ReturningClauseDetector returningClauseDetector;

    @Override
    public void visitToken(Token token, VisitorRegistrar visitorRegistrar) {
        parserState = parserState.next(token, this);
        if (parserState == ParserState.OTHER) {
            // We're not interested anymore
            visitorRegistrar.removeVisitor(this);
        } else if (parserState == ParserState.FIND_RETURNING) {
            // We're not interested anymore
            visitorRegistrar.removeVisitor(this);
            // Use ReturningClauseDetector to handle detection
            returningClauseDetector = new ReturningClauseDetector();
            visitorRegistrar.addVisitor(returningClauseDetector);
            // Forward current token; if the current token is RETURNING, it is correctly detected
            returningClauseDetector.visitToken(token, visitorRegistrar);
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

    DmlStatementType getStatementType() {
        return statementType;
    }

    Token getTableNameToken() {
        return tableNameToken;
    }

    private void updateStatementType(DmlStatementType statementType) {
        this.statementType = statementType;
        if (statementType == DmlStatementType.OTHER) {
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
            ParserState next0(Token token, DmlStatementDetector detector) {
                if (!(token instanceof ReservedToken)) {
                    detector.updateStatementType(DmlStatementType.OTHER);
                    return OTHER;
                }
                ParserState nextState = NEXT_AFTER_START.getOrDefault(token.text(), ParserState.OTHER);
                switch (nextState) {
                case UPDATE:
                    // Might be UPDATE OR INSERT
                    detector.updateStatementType(DmlStatementType.UPDATE);
                    break;
                case DELETE:
                    detector.updateStatementType(DmlStatementType.DELETE);
                    break;
                case INSERT:
                    detector.updateStatementType(DmlStatementType.INSERT);
                    break;
                case MERGE:
                    detector.updateStatementType(DmlStatementType.MERGE);
                    break;
                default:
                    detector.updateStatementType(DmlStatementType.OTHER);
                    break;
                }
                return nextState;
            }
        },
        UPDATE {
            @Override
            ParserState next0(Token token, DmlStatementDetector detector) {
                if (token instanceof OperatorToken && token.equalsIgnoreCase("OR")) {
                    detector.updateStatementType(DmlStatementType.UNKNOWN);
                    return POSSIBLY_UPDATE_OR_INSERT;
                } else {
                    return DML_TARGET.next0(token, detector);
                }
            }
        },
        POSSIBLY_UPDATE_OR_INSERT {
            @Override
            ParserState next0(Token token, DmlStatementDetector detector) {
                if (token instanceof ReservedToken && token.equalsIgnoreCase("INSERT")) {
                    detector.updateStatementType(DmlStatementType.UPDATE_OR_INSERT);
                    // Further detection can use the insert path
                    return INSERT;
                }
                detector.updateStatementType(DmlStatementType.OTHER);
                return OTHER;
            }
        },
        DELETE {
            @Override
            ParserState next0(Token token, DmlStatementDetector detector) {
                if (!(token instanceof ReservedToken && token.equalsIgnoreCase("FROM"))) {
                    detector.updateStatementType(DmlStatementType.OTHER);
                    return OTHER;
                }
                return DML_TARGET;
            }
        },
        // Shared by UPDATE, DELETE and MERGE
        DML_TARGET {
            @Override
            ParserState next0(Token token, DmlStatementDetector detector) {
                if (token.isValidIdentifier()) {
                    detector.setTableNameToken(token);
                    return DML_POSSIBLE_ALIAS;
                } else {
                    detector.updateStatementType(DmlStatementType.OTHER);
                    return OTHER;
                }
            }
        },
        // Shared by UPDATE, DELETE and MERGE
        DML_POSSIBLE_ALIAS {
            @Override
            ParserState next0(Token token, DmlStatementDetector detector) {
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
                    detector.updateStatementType(DmlStatementType.OTHER);
                    return OTHER;
                }
            }
        },
        DML_ALIAS {
            @Override
            ParserState next0(Token token, DmlStatementDetector detector) {
                if (token.isValidIdentifier()) {
                    return FIND_RETURNING;
                }
                // syntax error
                detector.updateStatementType(DmlStatementType.OTHER);
                return OTHER;
            }
        },
        INSERT {
            @Override
            ParserState next0(Token token, DmlStatementDetector detector) {
                if (token instanceof ReservedToken && token.equalsIgnoreCase("INTO")) {
                    return INSERT_INTO;
                }
                detector.updateStatementType(DmlStatementType.OTHER);
                return OTHER;
            }
        },
        INSERT_INTO {
            @Override
            ParserState next0(Token token, DmlStatementDetector detector) {
                if (token.isValidIdentifier()) {
                    detector.setTableNameToken(token);
                    return FIND_RETURNING;
                }
                // Syntax error
                detector.updateStatementType(DmlStatementType.OTHER);
                return OTHER;
            }
        },
        MERGE {
            @Override
            ParserState next0(Token token, DmlStatementDetector detector) {
                if (token instanceof ReservedToken && token.equalsIgnoreCase("INTO")) {
                    return DML_TARGET;
                }
                // Syntax error
                detector.updateStatementType(DmlStatementType.OTHER);
                return OTHER;
            }
        },
        FIND_RETURNING {
            @Override
            ParserState next0(Token token, DmlStatementDetector detector) {
                // finding itself is offloaded to ReturningClauseDetector
                throw new IllegalStateException(
                        "State " + this + " is a terminal state and next(..) should not be invoked");
            }
        },
        OTHER {
            @Override
            ParserState next0(Token token, DmlStatementDetector detector) {
                throw new IllegalStateException(
                        "State " + this + " is a terminal state and next(..) should not be invoked");
            }
        };

        final ParserState next(Token token, DmlStatementDetector detector) {
            if (token.isWhitespaceOrComment()) {
                // Ignore whitespace and comments
                return this;
            }
            return next0(token, detector);
        }

        abstract ParserState next0(Token token, DmlStatementDetector detector);

    }

}
