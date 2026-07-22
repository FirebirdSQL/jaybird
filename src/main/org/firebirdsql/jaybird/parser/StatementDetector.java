// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.jaybird.util.ObjectReference;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static java.lang.System.Logger.Level.TRACE;
import static java.util.Collections.unmodifiableMap;
import static org.firebirdsql.jaybird.parser.CharSequenceComparison.caseInsensitiveComparator;

/**
 * Detects the type of statement, and statement specific information like target object and {@code RETURNING} clause.
 * <p>
 * If the detected statement type is {@code UPDATE}, {@code DELETE}, {@code INSERT}, {@code UPDATE OR INSERT} and
 * {@code MERGE}, it identifies the affected table and - optionally - if a {@code RETURNING} clause is present
 * (delegated to a {@link ReturningClauseDetector}).
 * </p>
 * <p>
 * If the detected statement is {@code EXECUTE PROCEDURE}, {@code CALL}, or - Callable Statement V2 only - a JDBC call
 * escape, it identifies the stored procedure.
 * </p>
 * <p>
 * The types of statements detected and other information are informed by the needs of Jaybird, and may change between
 * point releases.
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
        // Firebird 5.0+ parenthesized query expression
        // NOTE: This is a shortcut, if parenthesis at the top-level are ever allowed for anything other than SELECT (or
        // SELECT-like statements), this needs to be reworked
        nextAfterStart.put("(", new StateAfterStart(ParserState.SELECT, LocalStatementType.SELECT));
        // Firebird 6.0+ USING ... DO; need to find end of the USING clause to detect actual statement type
        nextAfterStart.put("USING", new StateAfterStart(ParserState.FIND_USING_END, LocalStatementType.OTHER));
        // Firebird 6.0+ CALL
        nextAfterStart.put("CALL", new StateAfterStart(ParserState.CALL, LocalStatementType.CALL));
        // JDBC escape (search for call escape)
        // NOTE: JDBC call escape detection only applies to CallableStatement V2 handling
        nextAfterStart.put("{", new StateAfterStart(ParserState.JDBC_ESCAPE_START, LocalStatementType.OTHER));
        NEXT_AFTER_START = unmodifiableMap(nextAfterStart);
    }

    private final boolean detectReturning;
    private LocalStatementType statementType = LocalStatementType.UNKNOWN;
    private ParserState parserState = ParserState.START;
    private @Nullable ObjectReference targetObject;
    private @Nullable ReturningClauseDetector returningClauseDetector;

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
            // We're not interested any more
            visitorRegistrar.removeVisitor(this);
        } else {
            switch (parserState) {
            case EXECUTE_PROCEDURE:
            case CALL:
            case JDBC_ESCAPE_CALL:
            case INSERT_INTO:
            case DML_TARGET:
            case DML_TARGET_FORWARD_TOKEN: {
                var objectExtractorWithFuture = ObjectReferenceExtractor.withFuture();
                objectExtractorWithFuture.future().handle((@Nullable ObjectReference ref, @Nullable Throwable t) -> {
                    if (ref != null) {
                        setTargetObject(ref);
                    } else {
                        if (t != null) {
                            System.getLogger(getClass().getName()).log(TRACE, "Exception getting object reference", t);
                        }
                        updateStatementType(LocalStatementType.OTHER);
                    }
                    return null;
                });
                TokenVisitor newVisitor = objectExtractorWithFuture.extractor().onRemoveRegister(this);
                visitorRegistrar.addVisitor(newVisitor);
                visitorRegistrar.removeVisitor(this);
                if (parserState == ParserState.DML_TARGET_FORWARD_TOKEN) {
                    parserState = ParserState.DML_TARGET;
                    newVisitor.visitToken(token, visitorRegistrar);
                }
                break;
            }
            case FIND_RETURNING:
                // We're not interested any more
                visitorRegistrar.removeVisitor(this);
                if (detectReturning && returningClauseDetector == null) {
                    // Use ReturningClauseDetector to handle detection
                    returningClauseDetector = new ReturningClauseDetector();
                    visitorRegistrar.addVisitor(returningClauseDetector);
                    // Forward current token; if the current token is RETURNING, it is correctly detected
                    returningClauseDetector.visitToken(token, visitorRegistrar);
                }
                break;
            case FIND_USING_END:
                // Find end of USING ... DO
                visitorRegistrar.addVisitor(new SkipUsingClause().onRemoveRegister(this));
                // When it has been found, the SkipUsingClause will register us again
                visitorRegistrar.removeVisitor(this);
                break;
            }
        }
    }

    @Override
    public void complete(VisitorRegistrar visitorRegistrar) {
        switch (parserState) {
        // if parsing completes in these states, the statement is incomplete
        case JDBC_ESCAPE_CALL, INSERT_INTO -> updateStatementType(LocalStatementType.OTHER);
        // Handle DELETE FROM ... without WHERE, and EXECUTE PROCEDURE ... without arguments
        case EXECUTE_PROCEDURE, DML_TARGET -> {
            // TODO Maybe remove complete(..) and instead have the parser post an EOF token?
            if (targetObject == null) {
                updateStatementType(LocalStatementType.OTHER);
            }
        }
        }
    }

    public StatementIdentification toStatementIdentification() {
        return new StatementIdentification(statementType, targetObject, returningClauseDetected());
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

    Optional<ObjectReference> getTargetObject() {
        return Optional.ofNullable(targetObject);
    }

    void setTargetObject(@Nullable ObjectReference targetObject) {
        this.targetObject = targetObject;
    }

    private boolean hasTargetObject() {
        return targetObject != null;
    }

    private void updateStatementType(LocalStatementType statementType) {
        this.statementType = statementType;
        if (statementType == LocalStatementType.OTHER) {
            // clear any previously set target object
            setTargetObject(null);
        }
    }

    private enum ParserState {
        START {
            @Override
            ParserState next(Token token, StatementDetector detector) {
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
        EXECUTE_PROCEDURE {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (detector.hasTargetObject()) {
                    return EXEC_PROC_ARGS;
                }
                return forceOther(detector);
            }
        },
        // TODO Final state for now; maybe merge with CALL_PROC_ARGS and/or JDBC_CALL_PROC_ARGS
        // TODO Do we need to parse and extract the arguments?
        EXEC_PROC_ARGS(true),
        CALL {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (detector.hasTargetObject()) {
                    return CALL_PROC_ARGS;
                }
                return forceOther(detector);
            }
        },
        // TODO Final state for now; maybe merge with EXEC_PROC_ARGS and/or JDBC_CALL_PROC_ARGS
        // TODO Do we need to parse and extract the arguments? Maybe for named support, or can we get that from prepare?
        CALL_PROC_ARGS(true),
        // NOTE: JDBC call escape detection only applies to CallableStatement V2 handling
        JDBC_ESCAPE_START {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token instanceof PositionalParameterToken) {
                    return JDBC_ESCAPE_POSSIBLY_CALL_QM;
                } else if (token.equalsIgnoreCase("CALL")) {
                    // Call is not a reserved token in Firebird 5.0 and older
                    detector.updateStatementType(LocalStatementType.JDBC_CALL_ESCAPE);
                    return JDBC_ESCAPE_CALL;
                }
                return forceOther(detector);
            }
        },
        // NOTE: JDBC call escape detection only applies to CallableStatement V2 handling
        JDBC_ESCAPE_POSSIBLY_CALL_QM {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token instanceof OperatorToken && token.equalsIgnoreCase("=")) {
                    return JDBC_ESCAPE_POSSIBLY_CALL_EQ;
                }
                return forceOther(detector);
            }
        },
        // NOTE: JDBC call escape detection only applies to CallableStatement V2 handling
        JDBC_ESCAPE_POSSIBLY_CALL_EQ {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token.equalsIgnoreCase("CALL")) {
                    // Call is not a reserved token in Firebird 5.0 and older
                    detector.updateStatementType(LocalStatementType.JDBC_CALL_RETURN_ESCAPE);
                    return JDBC_ESCAPE_CALL;
                }
                return forceOther(detector);
            }
        },
        // NOTE: JDBC call escape detection only applies to CallableStatement V2 handling
        JDBC_ESCAPE_CALL {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (detector.hasTargetObject()) {
                    return JDBC_CALL_PROC_ARGS;
                }
                return forceOther(detector);
            }
        },
        // TODO Final state for now; maybe merge with CALL_PROC_ARGS and/or EXEC_PROC_ARGS
        // TODO We need to parse and extract the arguments
        // NOTE: JDBC call escape detection only applies to CallableStatement V2 handling
        JDBC_CALL_PROC_ARGS(true),
        UPDATE {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (token instanceof OperatorToken && token.equalsIgnoreCase("OR")) {
                    detector.updateStatementType(LocalStatementType.UNKNOWN);
                    return POSSIBLY_UPDATE_OR_INSERT;
                } else {
                    return DML_TARGET_FORWARD_TOKEN;
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
        // Finding the DML target itself is offloaded to ObjectReferenceExtractor
        DML_TARGET {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                if (detector.hasTargetObject()) {
                    return DML_POSSIBLE_ALIAS.next(token, detector);
                }
                return forceOther(detector);
            }
        },
        // For UPDATE to signal the current token must be forwarded to the object reference extractor
        DML_TARGET_FORWARD_TOKEN,
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
                if (detector.hasTargetObject()) {
                    return FIND_RETURNING;
                }
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
        FIND_USING_END {
            @Override
            ParserState next(Token token, StatementDetector detector) {
                // This is invoked after the end of USING ... DO has been found
                if (token instanceof CurlyBraceOpen) {
                    // This is probably a CALL escape, but the normal handling cannot apply as USING can change how
                    // parameters are defined and handled
                    detector.updateStatementType(LocalStatementType.JDBC_ESCAPE_AFTER_USING);
                    return OTHER;
                }
                return START.next(token, detector);
            }
        },
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
