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
package org.firebirdsql.gds;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Message template for Firebird and Jaybird parameterized error messages.
 *
 * @since 6
 */
@NullMarked
public sealed abstract class MessageTemplate permits DefaultMessageTemplate, OverriddenSqlStateMessageTemplate {

    private static final MessageLookup MESSAGE_LOOKUP = new MessageLookup();

    MessageTemplate() {
    }

    /**
     * Returns a message template for the specified error code.
     *
     * @param errorCode
     *         error code
     * @return message template; if there is no error defined for {@code errorCode}, a message template is returned with
     * a "not found" message and SQLstate {@code null}.
     */
    public static MessageTemplate of(int errorCode) {
        return MESSAGE_LOOKUP.getMessageTemplate(errorCode);
    }

    /**
     * @return error code of this message
     */
    public abstract int errorCode();

    /**
     * @return SQLstate (can be {@code null})
     */
    public abstract @Nullable String sqlState();

    /**
     * Returns a copy of this template with {@code sqlState} set to {@code defaultSqlState} if the SQLstate of
     * this template is {@code null}, otherwise returns this template.
     *
     * @param defaultSqlState
     *         default SQLstate to apply if this template has {@code sqlState == null}
     * @return this template if {@code sqlState} is non-{@code null}, otherwise a copy with the {@code sqlState} set to
     * {@code defaultSqlState}
     * @throws NullPointerException
     *         if {@code defaultSqlState} is {@code null} and {@code sqlState} of this instance is also {@code null}
     * @throws IllegalArgumentException
     *         if {@code defaultSqlState} is not 5 characters long
     */
    public abstract MessageTemplate withDefaultSqlState(String defaultSqlState);

    /**
     * Returns a copy of this template with {@code sqlState} set, or this template if it already has {@code sqlState} as
     * its SQLstate value.
     *
     * @param sqlState
     *         new SQLstate value (not {@code null})
     * @return this template if it already has {@code sqlState} as its value, otherwise a copy with {@code sqlState} set
     * @throws NullPointerException
     *         if {@code sqlState} is {@code null}
     * @throws IllegalArgumentException
     *         if {@code sqlState} is not 5 characters long
     */
    public abstract MessageTemplate withSqlState(String sqlState);

    /**
     * Renders the message, formatted using {@code parameters}.
     *
     * @param parameters
     *         parameters
     * @return formatted message
     * @see #appendMessage(StringBuilder, List)
     */
    public final String toMessage(List<? extends @Nullable Object> parameters) {
        // Sizing to 0, as appendMessage will resize, and that will almost always be bigger than the default
        var messageBuffer = new StringBuilder(0);
        appendMessage(messageBuffer, parameters);
        return messageBuffer.toString();
    }

    /**
     * Appends the message, formatted using {@code parameters}, to {@code messageBuffer}.
     * <p>
     * Parameters that are missing or {@code null} are rendered as {@code (null)}. Excess parameters are concatenated
     * to the end of the message, unless the {@code errorCode} is {@link ISCConstants#isc_formatted_exception}.
     * </p>
     *
     * @param messageBuffer
     *         string builder to append to
     * @param parameters
     *         parameters to use for formatting (never {@code null}, may be empty)
     * @see #appendErrorInfoSuffix(StringBuilder)
     */
    public abstract void appendMessage(StringBuilder messageBuffer, List<? extends @Nullable Object> parameters);

    /**
     * Appends the SQLstate and error code to {@code messageBuffer}.
     *
     * @param messageBuffer
     *         string builder to append to
     * @see #appendMessage(StringBuilder, List)
     */
    public final void appendErrorInfoSuffix(StringBuilder messageBuffer) {
        messageBuffer.append(" [SQLState:").append(sqlState())
                .append(", ISC error code:").append(errorCode())
                .append(']');
    }

    /**
     * Validates if {@code sqlState} is a 5-character string, returning the value, or throwing
     * {@link IllegalArgumentException} if it is not.
     * <p>
     * This method does not verify the syntax as defined in the SQL standard, only if it is 5 characters in length.
     * </p>
     *
     * @param sqlState
     *         SQLstate to validate
     * @return {@code sqlState} if it is 5 characters long
     * @throws NullPointerException
     *         if {@code sqlState} is {@code null}
     * @throws IllegalArgumentException
     *         if {@code sqlState} is not 5-characters long
     */
    static String validateSqlState(String sqlState) {
        if (requireNonNull(sqlState, "sqlState").length() != 5) {
            throw new IllegalArgumentException("Value of sqlState must be a 5 character string");
        }
        return sqlState;
    }

}
