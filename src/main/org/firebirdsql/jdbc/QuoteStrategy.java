/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ISCConstants;

/**
 * Strategy for quoting objects (or no quoting in the case of dialect 1).
 *
 * @since 2.2
 */
public enum QuoteStrategy {
    /**
     * Dialect 1 doesn't support quoting of object names.
     */
    NO_QUOTES {
        @Override
        public StringBuilder appendQuoted(final String objectName, final StringBuilder sb) {
            return sb.append(objectName);
        }

        @Override
        public String quoteObjectName(String objectName) {
            return objectName;
        }
    },
    /**
     * Dialect 3 (and 2) supports quoting of object names.
     */
    QUOTES {
        @Override
        public StringBuilder appendQuoted(final String objectName, final StringBuilder sb) {
            sb.append('"');
            for (int i = 0; i < objectName.length(); i++) {
                char currentChar = objectName.charAt(i);
                // we have to double quote quotes
                if (currentChar == '"') {
                    sb.append('"');
                }

                sb.append(currentChar);
            }
            sb.append('"');
            return sb;
        }

        @Override
        public String quoteObjectName(String objectName) {
            return appendQuoted(objectName, new StringBuilder(objectName.length() + 2)).toString();
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
     * @return The transformed object name.
     * @since 3.0.6
     */
    public abstract String quoteObjectName(String objectName);

    /**
     * Obtain the {@link QuoteStrategy} for the specified dialect.
     *
     * @param dialect
     *         Dialect
     * @return Appropriate {@link QuoteStrategy}
     */
    public static QuoteStrategy forDialect(final int dialect) {
        if (dialect == ISCConstants.SQL_DIALECT_V5) {
            return NO_QUOTES;
        }
        return QUOTES;
    }
}
