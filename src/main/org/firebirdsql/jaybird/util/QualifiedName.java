// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jdbc.QuoteStrategy;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

/**
 * An object name qualified by a schema.
 * <p>
 * NOTE: This class is not usable for packaged objects, as it has only one level of qualification.
 * </p>
 *
 * @param schema
 *         unquoted, case-sensitive schema name, {@code null} is returned as empty string ({@code ""})
 * @param object
 *         unquoted, case-sensitive object name (e.g. a table name)
 * @since 7
 */
public record QualifiedName(String schema, String object) {

    /**
     * Canonical constructor.
     *
     * @param schema
     *         unquoted, case-sensitive schema name, {@code null} is returned as empty string ({@code ""})
     * @param object
     *         unquoted, case-sensitive object name (e.g. a table name)
     * @throws IllegalArgumentException if {@code object} is blank
     */
    public QualifiedName(@Nullable String schema, String object) {
        // defined explicitly to annotate @Nullable on schema for constructor only
        this.schema = requireNonNullElse(schema, "");
        if (object.isBlank()) {
            throw new IllegalArgumentException("object cannot be blank");
        }
        this.object = requireNonNull(object, "object");
    }

    @Override
    public String schema() {
        return schema;
    }

    /**
     * Estimated length of the quoted identifier.
     * <p>
     * The estimate might be of if {@link #schema()} or {@link #object()} contains double quotes, or if
     * {@link QuoteStrategy#DIALECT_1} is used.
     * </p>
     * <p>
     * This can be used for pre-sizing a string builder for {@link #append(StringBuilder, QuoteStrategy)}.
     * </p>
     *
     * @return estimated length of the quoted identifier
     */
    public int estimatedLength() {
        // 2: double quotes, 1: separator
        return (schema.isEmpty() ? 0 : 2 + schema.length() + 1) + 2 + object.length();
    }

    /**
     * Produces the string of the identifier chain.
     *
     * @param quoteStrategy
     *         quote strategy to apply on {@code schema} and {@code object}
     * @return identifier chain
     */
    public String toString(QuoteStrategy quoteStrategy) {
        if (!schema.isEmpty()) {
            var sb = new StringBuilder(estimatedLength());
            quoteStrategy.appendQuoted(schema, sb).append('.');
            quoteStrategy.appendQuoted(object, sb);
            return sb.toString();
        }
        return quoteStrategy.quoteObjectName(object);
    }

    /**
     * Appends the identifier chain to {@code sb}, using {@code quoteStrategy}.
     *
     * @param sb
     *         StringBuilder for appending
     * @param quoteStrategy
     *         quote strategy to apply on {@code schema} and {@code object}
     * @return the StringBuilder for method chaining
     * @see #estimatedLength()
     */
    public StringBuilder append(StringBuilder sb, QuoteStrategy quoteStrategy) {
        if (!schema.isEmpty()) {
            quoteStrategy.appendQuoted(schema, sb).append('.');
        }
        quoteStrategy.appendQuoted(object, sb);
        return sb;
    }

    /**
     * Creates a qualified name from {@code originalSchema} and {@code originalTableName} of
     * {@code fieldDescriptor}.
     *
     * @param fieldDescriptor
     *         field descriptor
     * @return a schema-qualified name of the original table from {@code fieldDescriptor} or empty if its
     * {@code originalTableName} is empty string or {@code null}
     */
    public static Optional<QualifiedName> of(FieldDescriptor fieldDescriptor) {
        String tableName = fieldDescriptor.getOriginalTableName();
        if (StringUtils.isNullOrEmpty(tableName)) {
            return Optional.empty();
        }
        // NOTE: This will produce an exception if tableName is blank and not empty, we accept that as that shouldn't
        // happen in normal use
        return Optional.of(new QualifiedName(fieldDescriptor.getOriginalSchema(), tableName));
    }

}
