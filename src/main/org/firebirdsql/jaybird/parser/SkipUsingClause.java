// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import java.util.ArrayDeque;

/**
 * Token visitor that will look for the end of a Firebird 6+ {@code USING ... DO} clause.
 * <p>
 * This visitor does not look for the {@code USING} keyword. In common use, it should be added by a token visitor that
 * read {@code USING} itself, or if it's otherwise known that the token stream starts with a {@code USING ... DO}
 * clause.
 * </p>
 *
 * @since 7
 */
class SkipUsingClause implements TokenVisitor {

    private final ArrayDeque<ParserState> preservedState = new ArrayDeque<>();
    private ParserState parserState = ParserState.FIND_DO;

    /**
     * Creates instance to find the end of {@code USING ... DO}.
     */
    SkipUsingClause() {
    }

    @Override
    public void visitToken(Token token, VisitorRegistrar visitorRegistrar) {
        if (token.isWhitespaceOrComment()) return;
        parserState = parserState.next(token, this);
        if (parserState == ParserState.DO_FOUND) {
            visitorRegistrar.removeVisitor(this);
        }
    }

    private void pushParserState(ParserState parserState) {
        preservedState.addFirst(parserState);
    }

    private ParserState popParserState() {
        return preservedState.removeFirst();
    }

    // Given DO is not a reserved word, we need to be careful scanning for the closing DO
    private enum ParserState {
        FIND_DO {
            @Override
            ParserState next(Token token, SkipUsingClause skipUsingClause) {
                if (token instanceof ParenthesisOpen) {
                    skipUsingClause.pushParserState(this);
                    return FIND_PAREN_CLOSE;
                } else if (token instanceof ReservedToken) {
                    if (token.equalsIgnoreCase("BEGIN") || token.equalsIgnoreCase("CASE")) {
                        skipUsingClause.pushParserState(this);
                        return FIND_END;
                    } else if (token.equalsIgnoreCase("DECLARE")) {
                        return DECLARE;
                    }
                } else if (token instanceof GenericToken && token.equalsIgnoreCase("DO")) {
                    return DO_FOUND;
                }
                return this;
            }
        },
        FIND_PAREN_CLOSE {
            @Override
            ParserState next(Token token, SkipUsingClause skipUsingClause) {
                if (token instanceof ParenthesisClose) {
                    return skipUsingClause.popParserState();
                } else if (token instanceof ParenthesisOpen) {
                    skipUsingClause.pushParserState(this);
                    return FIND_PAREN_CLOSE;
                }
                return this;
            }
        },
        FIND_END {
            @Override
            ParserState next(Token token, SkipUsingClause skipUsingClause) {
                if (token instanceof ReservedToken) {
                    if (token.equalsIgnoreCase("END")) {
                        return skipUsingClause.popParserState();
                    } else if (token.equalsIgnoreCase("BEGIN") || token.equalsIgnoreCase("CASE")) {
                        skipUsingClause.pushParserState(this);
                        return FIND_END;
                    }
                }
                return this;
            }
        },
        DECLARE {
            @Override
            ParserState next(Token token, SkipUsingClause skipUsingClause) {
                if (token instanceof ReservedToken
                        && (token.equalsIgnoreCase("PROCEDURE") || token.equalsIgnoreCase("FUNCTION"))) {
                    return DECLARE_SUB;
                }
                // Anything else is taken as start of DECLARE [VARIABLE]
                return DECLARE_VAR;
            }
        },
        DECLARE_VAR {
            @Override
            ParserState next(Token token, SkipUsingClause skipUsingClause) {
                if (token instanceof SemicolonToken) {
                    // End of DECLARE [VARIABLE]
                    return FIND_DO;
                }
                return this;
            }
        },
        DECLARE_SUB {
            @Override
            ParserState next(Token token, SkipUsingClause skipUsingClause) {
                if (token instanceof SemicolonToken) {
                    // End of forward-declared routine
                    return FIND_DO;
                } else if (token instanceof ReservedToken && token.equalsIgnoreCase("AS")) {
                    return DECLARE_SUB_BODY;
                }
                return this;
            }
        },
        DECLARE_SUB_BODY {
            @Override
            ParserState next(Token token, SkipUsingClause skipUsingClause) {
                // NOTE: Correctness of this depends on not being able to nest subroutine declarations
                if (token instanceof ReservedToken && token.equalsIgnoreCase("BEGIN")) {
                    skipUsingClause.pushParserState(FIND_DO);
                    return FIND_END;
                }
                return this;
            }
        },
        DO_FOUND {
            @Override
            ParserState next(Token token, SkipUsingClause skipUsingClause) {
                throw new IllegalStateException(
                        "State " + this + " is a terminal state and next(..) should not be invoked");
            }
        },
        ;

        abstract ParserState next(Token token, SkipUsingClause skipUsingClause);

    }
}
