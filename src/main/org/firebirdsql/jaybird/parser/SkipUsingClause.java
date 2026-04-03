// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;

/**
 * Token visitor that will look for the end of a Firebird 6+ {@code USING ... DO} clause.
 *
 * @since 7
 */
class SkipUsingClause implements TokenVisitor {

    private final ArrayDeque<ParserState> preservedState = new ArrayDeque<>();
    private final Collection<TokenVisitor> registerOnFound;
    private ParserState parserState = ParserState.FIND_DO;

    /**
     * Creates instance to find the end of {@code USING ... DO}.
     *
     * @param registerOnFound
     *         token visitors to register after the end of {@code USING ... DO} has been found
     */
    SkipUsingClause(Collection<TokenVisitor> registerOnFound) {
        this.registerOnFound = registerOnFound;
    }

    /**
     * Creates instance to find the end of {@code USING ... DO}.
     *
     * @param registerOnFound
     *         token visitor to register after the end of {@code USING ... DO} has been found
     */
    SkipUsingClause(TokenVisitor registerOnFound) {
        this(List.of(registerOnFound));
    }

    @Override
    public void visitToken(Token token, VisitorRegistrar visitorRegistrar) {
        if (token.isWhitespaceOrComment()) return;
        parserState = parserState.next(token, this);
        if (parserState == ParserState.DO_FOUND) {
            registerOnFound.forEach(visitorRegistrar::addVisitor);
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
