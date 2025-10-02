// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jdbc.QuoteStrategy;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An object reference is a &mdash; possibly ambiguous &mdash; identification of an object, like a {@code column},
 * {@code table}, {@code table.column}, {@code schema.table.column}, {@code alias.column}, etc.
 * <p>
 * An object reference consists of one or more identifiers. If it has a single identifier, it <em>is</em>
 * an {@link Identifier}, otherwise it is an identifier chain.
 * </p>
 *
 * @since 7
 */
public sealed abstract class ObjectReference permits Identifier, IdentifierChain {

    /**
     * Creates an object reference ({@link Identifier}) from {@code name}.
     *
     * @param name
     *         name (cannot be {@code null}, empty, or blank)
     * @return identifier
     * @throws IllegalArgumentException
     *         if {@code name} is {@code null}, empty, or blank
     */
    public static Identifier of(String name) {
        return new Identifier(name);
    }

    /**
     * Creates an object reference (a single {@link Identifier} or an identifier chain) from {@code names}.
     * <p>
     * The prefix of the {@code names} may be {@code null} or empty strings, these are ignored and excluded from
     * the final object reference, as long as there is at least one non-blank name remaining (the suffix)
     * </p>
     *
     * @param names
     *         one or more names
     * @return an object reference
     * @throws IllegalArgumentException
     *         if {@code names} is empty, all names are {@code null} or empty, or at least one name in the suffix is
     *         blank or null
     * @see #of(List)
     */
    public static ObjectReference of(@Nullable String... names) {
        return of(Arrays.asList(names));
    }

    /**
     * Creates an object reference (a single {@link Identifier} or an identifier chain) from {@code names}.
     *
     * @param names
     *         one or more names
     * @return an object reference
     * @throws IllegalArgumentException
     *         if {@code names} is empty, all names are {@code null} or empty, or at least one name in the suffix is
     *         blank or null
     */
    public static ObjectReference of(List<@Nullable String> names) {
        //noinspection DataFlowIssue : Identifier(String) is @NonNull, and produce an IllegalArgumentException for null
        List<Identifier> nameList = names.stream().dropWhile(StringUtils::isNullOrEmpty).map(Identifier::new).toList();
        if (nameList.size() == 1) {
            return nameList.get(0);
        }
        return new IdentifierChain(nameList);
    }

    /**
     * Creates an object reference of the original table in {@code fieldDescriptor} (from {@code originalSchema} and
     * {@code originalTableName}).
     *
     * @param fieldDescriptor
     *         field descriptor
     * @return a possibly schema-qualified name of the original table from {@code fieldDescriptor} or empty if its
     * {@code originalTableName} is empty string or {@code null}
     */
    public static Optional<ObjectReference> ofTable(FieldDescriptor fieldDescriptor) {
        String tableName = fieldDescriptor.getOriginalTableName();
        if (StringUtils.isNullOrEmpty(tableName)) {
            return Optional.empty();
        }
        // NOTE: This will produce an exception if tableName is blank and not empty, we accept that as that shouldn't
        // happen in normal use
        return Optional.of(
                ObjectReference.of(fieldDescriptor.getOriginalSchema(), fieldDescriptor.getOriginalTableName()));
    }

    /**
     * @return number of identifiers in this object reference ({@code >= 1})
     */
    public abstract int size();

    /**
     * Gets identifier at 0-based position {@code index}.
     *
     * @param index
     *         index of the identifier
     * @return the identifier
     * @throws IndexOutOfBoundsException
     *         if {@code index < 0 || index > size()}
     */
    public abstract Identifier at(int index);

    public Identifier first() {
        return at(0);
    }

    public Identifier last() {
        return at(size() - 1);
    }

    /**
     * The name(s), quoted using {@code quoteStrategy}.
     *
     * @param quoteStrategy
     *         quote strategy
     * @return name, possibly quoted
     */
    public abstract String toString(QuoteStrategy quoteStrategy);

    /**
     * Quoted name(s).
     *
     * @return quoted name(s) equivalent of {@link #toString(QuoteStrategy)} with {@link QuoteStrategy#DIALECT_3}
     */
    @Override
    public final String toString() {
        return toString(QuoteStrategy.DIALECT_3);
    }

    /**
     * Appends name(s) to {@code sb} using {@code quoteStrategy}.
     *
     * @param sb
     *         string builder to append to
     * @param quoteStrategy
     *         quote strategy
     * @return {@code sb} for chaining
     */
    public abstract StringBuilder append(StringBuilder sb, QuoteStrategy quoteStrategy);

    /**
     * @return stream of identifiers in this object reference
     */
    public abstract Stream<Identifier> stream();

    /**
     * @return list of identifiers in this object reference
     */
    public abstract List<Identifier> toList();

    /**
     * Resolves the given object reference against this object reference.
     * <p>
     * For example, if this object reference is {@code schema.table}, and {@code other} is {@code column}, then the
     * result is {@code schema.table.column}.
     * </p>
     * <p>
     * Or in other words, this method concatenates {@code this} and {@code other} and returns it as a new object
     * reference.
     * </p>
     *
     * @param other
     *         other object reference
     * @return new object reference (an identifier chain)
     */
    public ObjectReference resolve(ObjectReference other) {
        return new IdentifierChain(CollectionUtils.concat(toList(), other.toList()));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: Subclasses need to ensure consistency of equals for logically equivalent references in
     * different types (e.g. an Identifier and an IdentifierChain with a single Identifier).
     * </p>
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof ObjectReference other && size() == other.size() && toList().equals(other.toList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: Subclasses need to ensure consistency of hashCode for logically equivalent references in
     * different types (e.g. an Identifier and an IdentifierChain with a single Identifier).
     * </p>
     */
    @Override
    public abstract int hashCode();

}