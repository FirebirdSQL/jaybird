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
 * @author Mark Rotteveel
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

    private static final Set<CharSequence> NOT_IN_RETURNING_TOKEN_TEXT;
    private static final Set<CharSequence> NOT_IMMEDIATELY_BEFORE_RETURNING_TOKEN_TEXT;
    private static final Set<CharSequence> NOT_IMMEDIATELY_AFTER_RETURNING_TOKEN_TEXT;

    static {
        // Token text that cannot occur in RETURNING at a top-level (some of them may occur nested within parentheses)
        // See also notImmediatelyAfterReturningTokenText for tokens that can occur, but not as first token
        TreeSet<CharSequence> notInReturningTokenText =
                new TreeSet<>(CharSequenceComparison.caseInsensitiveComparator());
        notInReturningTokenText.addAll(Arrays.asList(
                "VALUES", "SELECT", "INSERT", "UPDATE", "DELETE", "MERGE", "WHERE", "PLAN", "ORDER", "ROWS", "SET"));
        NOT_IN_RETURNING_TOKEN_TEXT = unmodifiableSet(notInReturningTokenText);

        // Token text that cannot occur immediately before RETURNING
        TreeSet<CharSequence> notImmediatelyBeforeReturningTokenText = new TreeSet<>(notInReturningTokenText);
        // VALUES can occur in INSERT INTO tbl DEFAULT VALUES RETURNING ...
        notImmediatelyBeforeReturningTokenText.remove("VALUES");
        notImmediatelyBeforeReturningTokenText.addAll(Arrays.asList("WITH", "CONTAINING", "TO", "FROM", "BETWEEN"));
        NOT_IMMEDIATELY_BEFORE_RETURNING_TOKEN_TEXT = unmodifiableSet(notImmediatelyBeforeReturningTokenText);

        // Token text that cannot occur immediately after RETURNING
        TreeSet<CharSequence> notImmediatelyAfterReturningTokenText =
                new TreeSet<>(CharSequenceComparison.caseInsensitiveComparator());
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
                || token instanceof ReservedToken && NOT_IN_RETURNING_TOKEN_TEXT.contains(token.textAsCharSequence());
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
        return token instanceof OperatorToken operatorToken
                && !(isPossibleUnaryOperator(operatorToken) || operatorToken.equalsIgnoreCase("*"))
                || NOT_IMMEDIATELY_AFTER_RETURNING_TOKEN_TEXT.contains(token.textAsCharSequence());
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
        return !NOT_IMMEDIATELY_BEFORE_RETURNING_TOKEN_TEXT.contains(previousToken.textAsCharSequence());
    }

    private static boolean isPossibleUnaryOperator(OperatorToken token) {
        return token.equalsIgnoreCase("+") || token.equalsIgnoreCase("-") || token.equalsIgnoreCase("NOT");
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
                } else if (token instanceof OpenToken openToken) {
                    return nextForOpenToken(openToken, detector);
                }
                // returning not yet found
                return this;
            }
        },
        IN_RETURNING {
            @Override
            ParserState next0(Token token, ReturningClauseDetector detector) {
                if (token instanceof OpenToken openToken) {
                    return nextForOpenToken(openToken, detector);
                } else if (detector.cannotOccurInReturning(token)) {
                    detector.resetReturningClauseTokens();
                    return FIND_RETURNING;
                }
                detector.incrementReturningClauseTokenCount();
                return this;
            }
        },
        NESTED_PARENTHESIS {
            @Override
            ParserState next0(Token token, ReturningClauseDetector detector) {
                if (token instanceof OpenToken openToken) {
                    return nextForOpenToken(openToken, detector);
                } else if (token instanceof ParenthesisClose) {
                    return nextForCloseToken(detector);
                } else {
                    return this;
                }
            }
        },
        NESTED_CURLY_BRACE {
            @Override
            ParserState next0(Token token, ReturningClauseDetector detector) {
                if (token instanceof OpenToken openToken) {
                    return nextForOpenToken(openToken, detector);
                } else if (token instanceof CurlyBraceClose) {
                    return nextForCloseToken(detector);
                } else {
                    return this;
                }
            }
        },
        NESTED_SQUARE_BRACKET {
            @Override
            ParserState next0(Token token, ReturningClauseDetector detector) {
                if (token instanceof OpenToken openToken) {
                    return nextForOpenToken(openToken, detector);
                } else if (token instanceof SquareBracketClose) {
                    return nextForCloseToken(detector);
                } else {
                    return this;
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

        final ParserState nextForOpenToken(OpenToken token, ReturningClauseDetector detector) {
            // Assume we find a known open token and push the state
            detector.pushParserState(this);
            if (token instanceof ParenthesisOpen) {
                return NESTED_PARENTHESIS;
            } else if (token instanceof CurlyBraceOpen) {
                return NESTED_CURLY_BRACE;
            } else if (token instanceof SquareBracketOpen) {
                return NESTED_SQUARE_BRACKET;
            } else {
                // Unrecognized open token, restore state and treat as "normal" token
                if (detector.popParserState() == IN_RETURNING) {
                    detector.incrementReturningClauseTokenCount();
                }
                return this;
            }
        }

        // NOTE: Caller is responsible for checking if it is appropriate to close (i.e. is it balanced with
        // a previous open of the same type)
        final ParserState nextForCloseToken(ReturningClauseDetector detector) {
            ParserState restoredState = detector.popParserState();
            if (restoredState == IN_RETURNING) {
                detector.incrementReturningClauseTokenCount();
            }
            return restoredState;
        }
    }

}
