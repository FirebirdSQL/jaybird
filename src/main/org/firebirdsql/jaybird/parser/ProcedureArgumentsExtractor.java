// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Extracts a procedure argument list.
 * <p>
 * This implementation only works for parenthesized argument lists or no argument list at all, and in its current usage
 * is only used and tested in combination with JDBC call escapes.
 * </p>
 */
final class ProcedureArgumentsExtractor implements TokenVisitor {

    private final Consumer<List<ProcedureArgument>> argumentListConsumer;
    private final Consumer<RuntimeException> exceptionConsumer;
    private final List<Token> currentArgumentTokens = new ArrayList<>();
    private final List<ProcedureArgument> arguments = new ArrayList<>();
    private ParserState state = ParserState.INITIAL;
    private int openParens = 0;
    private boolean notifyLastToken;

    ProcedureArgumentsExtractor(Consumer<List<ProcedureArgument>> argumentListConsumer,
            Consumer<RuntimeException> exceptionConsumer) {
        this.argumentListConsumer = argumentListConsumer;
        this.exceptionConsumer = exceptionConsumer;
    }

    @Override
    public void visitToken(Token token, VisitorRegistrar visitorRegistrar) {
        try {
            state = state.next(token, this);
            if (state == ParserState.END_ARGUMENT_LIST) {
                argumentListConsumer.accept(arguments());
                visitorRegistrar.removeVisitor(this);
            }
        } catch (RuntimeException e) {
            exceptionConsumer.accept(e);
            visitorRegistrar.removeVisitor(this);
        }
    }

    @Override
    public void complete(VisitorRegistrar visitorRegistrar) {
        exceptionConsumer.accept(
                new IllegalStateException("Visitor completion was signalled before end of argument list"));
    }

    /**
     * @return immutable list of procedure arguments (can be empty)
     */
    List<ProcedureArgument> arguments() {
        return List.copyOf(arguments);
    }

    private void pushArgument() {
        arguments.add(ProcedureArgument.of(currentArgumentTokens));
        clearArgumentTokens();
    }

    private void collectArgumentToken(Token token) {
        currentArgumentTokens.add(token);
    }

    private void clearArgumentTokens() {
        currentArgumentTokens.clear();
    }

    private int openParens() {
        return openParens;
    }

    private void pushOpenParens() {
        openParens++;
    }

    private void popOpenParens() {
        assert openParens > 0 : "popOpenParens should not be called when 0 or less";
        openParens--;
    }

    @Override
    public TokenVisitor onRemoveRegister(Collection<TokenVisitor> visitors) {
        return new RegisterOnRemoveTokenVisitor<>(this, visitors, this::isOnRemoveNotifyLastToken);
    }

    private void onRemoveNotifyLastToken() {
        notifyLastToken = true;
    }

    /**
     * @return {@code true} if the last token was not part of procedure arguments, and would need to be notified for
     * visitors registered with {@code onRemoveRegister(...)}
     */
    boolean isOnRemoveNotifyLastToken() {
        return notifyLastToken;
    }

    private enum ParserState {

        INITIAL {
            @Override
            ParserState next(Token token, ProcedureArgumentsExtractor extractor) {
                // Skip whitespace and comments before argument list
                if (token.isWhitespaceOrComment()) return this;
                if (token instanceof ParenthesisOpen) return START_ARGUMENT_LIST;
                if (token instanceof CurlyBraceClose) {
                    // NOTE: assumption: this is a JDBC call escape of the form {[?=]call procname} (no argument list)
                    extractor.onRemoveNotifyLastToken();
                    return END_ARGUMENT_LIST;
                }
                throw new UnexpectedTokenException("Unexpected token at start of argument list", token);
            }
        },
        START_ARGUMENT_LIST {
            @Override
            ParserState next(Token token, ProcedureArgumentsExtractor extractor) {
                if (token.isWhitespaceOrComment()) {
                    extractor.collectArgumentToken(token);
                    return this;
                } else if (token instanceof ParenthesisClose) {
                    // empty argument list
                    extractor.clearArgumentTokens();
                    return END_ARGUMENT_LIST;
                }
                return NEXT_ARGUMENT.next(token, extractor);
            }
        },
        NEXT_ARGUMENT {
            @Override
            ParserState next(Token token, ProcedureArgumentsExtractor extractor) {
                if (token.isWhitespaceOrComment()) {
                    extractor.collectArgumentToken(token);
                    return this;
                } else if (token instanceof CommaToken) {
                    throw new UnexpectedTokenException("First token of argument cannot be a comma", token);
                } else if (token instanceof ParenthesisClose) {
                    throw new UnexpectedTokenException(
                            "First token of argument cannot be a closing parenthesis", token);
                }
                return CONTINUE_ARGUMENT.next(token, extractor);
            }
        },
        CONTINUE_ARGUMENT {
            @Override
            ParserState next(Token token, ProcedureArgumentsExtractor extractor) {
                if (token instanceof ParenthesisOpen) {
                    extractor.pushOpenParens();
                } else if (token instanceof ParenthesisClose) {
                    if (extractor.openParens() == 0) {
                        extractor.pushArgument();
                        return END_ARGUMENT_LIST;
                    } else {
                        extractor.popOpenParens();
                    }
                } else if (token instanceof CommaToken && extractor.openParens() == 0) {
                    extractor.pushArgument();
                    return NEXT_ARGUMENT;
                }
                extractor.collectArgumentToken(token);
                return this;
            }
        },
        END_ARGUMENT_LIST;

        ParserState next(Token token, ProcedureArgumentsExtractor extractor) {
            throw new IllegalStateException(
                    "State " + this + " is a terminal state and next(..) should not be invoked");
        }

    }

}
