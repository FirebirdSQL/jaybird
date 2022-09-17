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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableSet;

/**
 * Detects if the visited statement has a {@code RETURNING} clause.
 * <p>
 * Detection is very basic, and unfortunately {@code RETURNING} is not a reserved word, while we try to avoid
 * a too complicated parser. As a result, this detector may yield false-positives, for example a statement like
 * {@code update returning set returning = not returning where returning and x = 1} could be incorrectly identified as
 * having a {@code RETURNING} clause (actually, this specific example is correctly detected by the current
 * implementation).
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
class ReturningClauseDetector extends AbstractTokenVisitor {

    private static final Token UNDEFINED_TOKEN = new Token() {
        @Override
        public String text() {
            return "";
        }

        @Override
        public int position() {
            return -1;
        }

        @Override
        public int length() {
            return 0;
        }
    };

    private static final Set<String> NOT_IN_RETURNING_TOKEN_TEXT;
    private static final Set<String> NOT_IMMEDIATELY_BEFORE_RETURNING_TOKEN_TEXT;
    private static final Set<String> NOT_IMMEDIATELY_AFTER_RETURNING_TOKEN_TEXT;

    static {
        // Token text that cannot occur in RETURNING at a top-level (some of them may occur nested within parentheses)
        // See also notImmediatelyAfterReturningTokenText for tokens that can occur, but not as first token
        TreeSet<String> notInReturningTokenText = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        notInReturningTokenText.addAll(Arrays.asList(
                "VALUES", "SELECT", "INSERT", "UPDATE", "DELETE", "MERGE", "WHERE", "PLAN", "ORDER", "ROWS", "SET"));
        NOT_IN_RETURNING_TOKEN_TEXT = unmodifiableSet(notInReturningTokenText);

        // Token text that cannot occur immediately before RETURNING
        TreeSet<String> notImmediatelyBeforeReturningTokenText = new TreeSet<>(notInReturningTokenText);
        // VALUES can occur in INSERT INTO tbl DEFAULT VALUES RETURNING ...
        notImmediatelyBeforeReturningTokenText.remove("VALUES");
        notImmediatelyBeforeReturningTokenText.addAll(Arrays.asList("WITH", "CONTAINING", "TO", "FROM", "BETWEEN"));
        NOT_IMMEDIATELY_BEFORE_RETURNING_TOKEN_TEXT = unmodifiableSet(notImmediatelyBeforeReturningTokenText);

        // Token text that cannot occur immediately after RETURNING
        TreeSet<String> notImmediatelyAfterReturningTokenText = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        notImmediatelyAfterReturningTokenText.addAll(Arrays.asList("FROM", "AS", "TO"));
        NOT_IMMEDIATELY_AFTER_RETURNING_TOKEN_TEXT = unmodifiableSet(notImmediatelyAfterReturningTokenText);
    }

    private final ArrayDeque<ParserState> preservedState = new ArrayDeque<>();
    private ParserState parserState = ParserState.FIND_RETURNING;
    private Token previousToken = UNDEFINED_TOKEN;
    // Count of tokens excluding whitespace and comments after first occurrence of RETURNING
    // An alternative would be to collect all tokens after RETURNING and see if they match the syntax of
    // a RETURNING clause; considered too much effort for little gain for now
    private int returningClauseTokenCount;
    private Boolean returningClauseFound;

    @Override
    public void visitToken(Token token) {
        parserState = parserState.next(token, this);
    }

    @Override
    public void complete() {
        returningClauseFound = hasReturningClauseTokens();
    }

    public boolean returningClauseDetected() {
        return returningClauseFound == Boolean.TRUE;
    }

    private void pushParserState(ParserState parserState) {
        preservedState.addFirst(parserState);
    }

    private ParserState popParserState() {
        return preservedState.removeFirst();
    }

    /**
     * Checks if token cannot occur in a {@code RETURNING} clause.
     * <p>
     * Be aware, these tokens could occur in a nested context, but given the implementation, we don't see those.
     * </p>
     *
     * @param token
     *         Token
     * @return {@code true} if the provided token cannot occur in a {@code RETURNING} clause
     */
    private boolean cannotOccurInReturning(Token token) {
        return !hasReturningClauseTokens() && cannotOccurAsFirstReturningToken(token)
                || token instanceof ReservedToken && NOT_IN_RETURNING_TOKEN_TEXT.contains(token.text());
    }

    /**
     * Checks if a token cannot occur as the first token in the {@code RETURNING} clause.
     *
     * @param token
     *         Token
     * @return {@code true} if the provided token cannot occur as the first (non-whitespace/comment) token in a {@code
     * RETURNING} clause
     */
    private boolean cannotOccurAsFirstReturningToken(Token token) {
        return token instanceof OperatorToken
                && !(isPossibleUnaryOperator((OperatorToken) token) || token.equalsIgnoreCase("*"))
                || NOT_IMMEDIATELY_AFTER_RETURNING_TOKEN_TEXT.contains(token.text());
    }

    /**
     * Checks if - given the previous token - the {@code RETURNING} clause is syntactically possible.
     *
     * @return {@code true} if the {@code RETURNING} clause can occur
     */
    private boolean isReturningClausePossible() {
        if (previousToken instanceof OperatorToken
                || previousToken instanceof ColonToken
                || previousToken instanceof PeriodToken
                || previousToken instanceof CommaToken
                || previousToken instanceof OpenToken) {
            return false;
        }
        return !NOT_IMMEDIATELY_BEFORE_RETURNING_TOKEN_TEXT.contains(previousToken.text());
    }

    private static boolean isPossibleUnaryOperator(OperatorToken token) {
        String tokenText = token.text();
        return tokenText.equals("+") || tokenText.equals("-") || tokenText.equalsIgnoreCase("not");
    }

    /**
     * Counts number of tokens seen after {@code RETURNING} clause.
     */
    private void incrementReturningClauseTokenCount() {
        returningClauseTokenCount++;
    }

    private boolean hasReturningClauseTokens() {
        return returningClauseTokenCount > 0;
    }

    private void resetReturningClauseTokens() {
        returningClauseTokenCount = 0;
    }

    private enum ParserState {
        FIND_RETURNING {
            @Override
            ParserState next0(Token token, ReturningClauseDetector detector) {
                if (token instanceof GenericToken && token.equalsIgnoreCase("returning")
                        && detector.isReturningClausePossible()) {
                    return IN_RETURNING;
                } else if (token instanceof ParenthesisOpen) {
                    detector.pushParserState(this);
                    return NESTED;
                }
                // TODO Also handle {} (JDBC escapes) nesting?
                // returning not yet found
                return FIND_RETURNING;
            }
        },
        IN_RETURNING {
            @Override
            ParserState next0(Token token, ReturningClauseDetector detector) {
                if (token instanceof ParenthesisOpen) {
                    detector.pushParserState(this);
                    return NESTED;
                } else if (detector.cannotOccurInReturning(token)) {
                    detector.resetReturningClauseTokens();
                    return FIND_RETURNING;
                }
                detector.incrementReturningClauseTokenCount();
                return IN_RETURNING;
            }
        },
        NESTED {
            @Override
            ParserState next0(Token token, ReturningClauseDetector detector) {
                if (token instanceof ParenthesisOpen) {
                    detector.pushParserState(this);
                    return NESTED;
                } else if (token instanceof ParenthesisClose) {
                    ParserState restoredState = detector.popParserState();
                    if (restoredState == IN_RETURNING) {
                        detector.incrementReturningClauseTokenCount();
                    }
                    return restoredState;
                } else {
                    return NESTED;
                }
            }
        };

        final ParserState next(Token token, ReturningClauseDetector detector) {
            if (token.isWhitespaceOrComment()) {
                return this;
            }
            try {
                return next0(token, detector);
            } finally {
                detector.previousToken = token;
            }
        }

        abstract ParserState next0(Token token, ReturningClauseDetector detector);

    }

}