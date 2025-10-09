// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.jaybird.util.Identifier;
import org.firebirdsql.jaybird.util.ObjectReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Token visitor to extract an object reference (identifier chain) from a string.
 * <p>
 * When this token visitor is added to a parser, it is expected that the token stream is immediately before the start of
 * the object reference (ignoring whitespace and comments). It will unregister itself as soon as it receives a token
 * that is not part of an object reference.
 * </p>
 * <p>
 * If you want to reuse an instance of this token visitor, you must call {@link #reset()} before adding it to
 * the parser again.
 * </p>
 *
 * @since 7
 */
public final class ObjectReferenceExtractor implements TokenVisitor {

    // pre-sizing at 3, as for current usages we expect at most 3 identifiers (i.e. <schema>.<package>.<procedure>)
    private final List<Identifier> identifiers = new ArrayList<>(3);
    private Token previousToken;
    private boolean previousTokenWasScopeSpecifierValue;
    private UnexpectedTokenException unexpectedTokenException;

    @Override
    public void visitToken(Token token, VisitorRegistrar visitorRegistrar) {
        if (token.isWhitespaceOrComment()) return;
        final boolean previousTokenWasScopeSpecifierValue = this.previousTokenWasScopeSpecifierValue;
        this.previousTokenWasScopeSpecifierValue = false;
        try {
            if (previousToken == null || previousToken instanceof PeriodToken) {
                if (token instanceof GenericToken identifier && identifier.isValidIdentifier()) {
                    identifiers.add(new Identifier(identifier.text().toUpperCase(Locale.ROOT)));
                } else if (token instanceof QuotedIdentifierToken quotedIdentifier) {
                    identifiers.add(new Identifier(quotedIdentifier.name()));
                } else {
                    throw new UnexpectedTokenException(
                            "Expected a QuotedIdentifierToken or GenericToken with valid identifier, received " + token,
                            token);
                }
            } else if (previousToken instanceof GenericToken || previousToken instanceof QuotedIdentifierToken
                    || previousTokenWasScopeSpecifierValue) {
                if (!(token instanceof PeriodToken || isScopeSpecifier(token))) {
                    // End of identifier chain, we're no longer interested
                    visitorRegistrar.removeVisitor(this);
                    return;
                }
            } else if (isScopeSpecifier(previousToken)) {
                if (isScopeSpecifierValue(token)) {
                    var identifierScope = Identifier.Scope.valueOf(token.text().toUpperCase(Locale.ROOT));
                    // Replace last identifier with one with the specified scope
                    Identifier oldIdentifier = identifiers.remove(identifiers.size() - 1);
                    identifiers.add(new Identifier(oldIdentifier.name(), identifierScope));
                    this.previousTokenWasScopeSpecifierValue = true;
                } else {
                    // End of identifier chain, we're no longer interested
                    // This is unexpected, but we're assuming future implementation of % as remainder or modular division
                    // or some other kind of operator which would end the identifier chain
                    visitorRegistrar.removeVisitor(this);
                    return;
                }
            } else {
                throw new UnexpectedTokenException(
                        "Unexpected token or parser state, likely this is an implementation bug, received "
                                + token, token);
            }
        } catch (UnexpectedTokenException e) {
            unexpectedTokenException = e;
            // Unrecoverable, no longer interested
            visitorRegistrar.removeVisitor(this);
            return;
        }

        previousToken = token;
    }

    /**
     * Resets the state of the detector so it behaves as if it was just created.
     */
    public void reset() {
        identifiers.clear();
        previousToken = null;
        previousTokenWasScopeSpecifierValue = false;
        unexpectedTokenException = null;
    }

    private boolean isScopeSpecifier(Token token) {
        return token instanceof OperatorToken operatorToken && operatorToken.charAt(0) == '%';
    }

    private boolean isScopeSpecifierValue(Token token) {
        // SCHEMA is a reserved word, PACKAGE is not
        return token instanceof GenericToken && token.equalsIgnoreCase("PACKAGE")
                || token instanceof ReservedToken && token.equalsIgnoreCase("SCHEMA");
    }

    @Override
    public void complete(VisitorRegistrar visitorRegistrar) {
        if (previousToken instanceof PeriodToken) {
            unexpectedTokenException = new UnexpectedTokenException(
                    "Last token was PeriodToken, missing a QuotedIdentifierToken or GenericToken with valid identifier",
                    previousToken);
        } else if (isScopeSpecifier(previousToken)) {
            unexpectedTokenException = new UnexpectedTokenException(
                    "Last token was scope specifier (%), missing scope or other token", previousToken);
        }
    }

    /**
     * Obtains the object reference.
     *
     * @return object reference
     * @throws IllegalStateException
     *         if an unexpected token was encountered, or if no identifiers where detected
     */
    public ObjectReference toObjectReference() {
        if (unexpectedTokenException != null) {
            throw new IllegalStateException("Parsing failed", unexpectedTokenException);
        } else if (identifiers.isEmpty()) {
            throw new IllegalStateException("No identifiers were detected");
        }
        return ObjectReference.ofIdentifiers(identifiers);
    }

}
