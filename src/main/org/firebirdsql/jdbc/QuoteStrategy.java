// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.util.InternalApi;

/**
 * Strategy for quoting object names and literals (or no quoting of object names in the case of dialect 1).
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * </p>
 *
 * @since 2.2
 */
@InternalApi
public enum QuoteStrategy {
    /**
     * Dialect 1 doesn't support quoting of object names, and uses double quotes for literals.
     *
     * @since 6
     */
    DIALECT_1 {
        @Override
        public StringBuilder appendQuoted(String objectName, StringBuilder sb) {
            return sb.append(objectName);
        }

        @Override
        public String quoteObjectName(String objectName) {
            return objectName;
        }

        @Override
        public StringBuilder appendLiteral(String value, StringBuilder sb) {
            return appendWithQuoteEscaped('"', value, sb);
        }
    },
    /**
     * Dialect 3 (and 2) supports quoting of object names, and uses single quotes for literals.
     *
     * @since 6
     */
    DIALECT_3 {
        @Override
        public StringBuilder appendQuoted(String objectName, StringBuilder sb) {
            return appendWithQuoteEscaped('"', objectName, sb);
        }

        @Override
        public String quoteObjectName(String objectName) {
            return appendQuoted(objectName, new StringBuilder(objectName.length() + 2)).toString();
        }

        @Override
        public StringBuilder appendLiteral(String value, StringBuilder sb) {
            return appendWithQuoteEscaped('\'', value, sb);
        }
    };

    /**
     * Appends the {@code objectName} with (or in the case of dialect 1: without) quotes to {@code sb}.
     *
     * @param objectName
     *         The object name to append
     * @param sb
     *         StringBuilder for appending
     * @return The StringBuilder for method chaining
     */
    public abstract StringBuilder appendQuoted(String objectName, StringBuilder sb);

    /**
     * Returns the object name appropriately quoted according to this quote strategy.
     *
     * @param objectName
     *         The object name
     * @return The transformed object name
     * @since 3.0.6
     */
    public abstract String quoteObjectName(String objectName);

    /**
     * Appends {@code value} as a CHAR literal with the right quotes and escaping for this quote strategy to {@code sb}.
     *
     * @param value
     *         The value to append as a literal
     * @param sb
     *         StringBuilder for appending
     * @return The StringBuilder for method chaining
     * @since 6
     */
    public abstract StringBuilder appendLiteral(String value, StringBuilder sb);

    /**
     * Returns {@code value} as a CHAR literal with the right quotes and escaping for this quote strategy.
     *
     * @param value
     *         The value to append as a literal
     * @return The transformed value as a literal
     * @since 6
     */
    public String quoteLiteral(String value) {
        return appendLiteral(value, new StringBuilder(value.length() + 2)).toString();
    }

    /**
     * Appends {@code value} to {@code sb}, enclosing it in {@code quoteChar} and escaping occurrences of
     * {@code quoteChar} by doubling it.
     *
     * @param quoteChar quote character
     * @param value value to append quoted and escaped
     * @param sb string builder to append to
     * @return {@code sb} (for method chaining)
     * @since 6
     */
    StringBuilder appendWithQuoteEscaped(char quoteChar, String value, StringBuilder sb) {
        sb.append(quoteChar);
        for (int i = 0; i < value.length(); i++) {
            char currentChar = value.charAt(i);
            // we have to double quote quotes
            if (currentChar == quoteChar) {
                sb.append(quoteChar);
            }

            sb.append(currentChar);
        }
        return sb.append(quoteChar);
    }

    /**
     * Obtain the {@link QuoteStrategy} for the specified dialect.
     *
     * @param dialect
     *         Dialect
     * @return Appropriate {@link QuoteStrategy}
     */
    public static QuoteStrategy forDialect(final int dialect) {
        return dialect == ISCConstants.SQL_DIALECT_V5 ? DIALECT_1 : DIALECT_3;
    }
}
