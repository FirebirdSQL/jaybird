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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.gds.ISCConstants.isc_formatted_exception;

/**
 * Message template holds the template text, error code, SQLstate, and parameter count, and can render the message.
 * <p>
 * The template text uses a simplified form of {@code MessageFormat}. Basically, {@code {n}} always represents
 * a parameter with index {@code n}, where {@code n >= 0}. There are no escapes defined.
 * </p>
 *
 * @since 6
 */
@NullMarked
final class DefaultMessageTemplate extends MessageTemplate {

    private static final int PARAM_SIZE_FACTOR = 20;
    private static final int COUNT_UNKNOWN = -1;
    private static final Pattern MESSAGE_PARAM_PATTERN = Pattern.compile("\\{(\\d+)}");

    private final int errorCode;
    private final String templateText;
    private final @Nullable String sqlState;
    private int parameterCount;

    /**
     * Creates a message template with {@code errorCode} and {@code templateText}, and SQLstate {@code null}.
     *
     * @param errorCode
     *         error code
     * @param templateText
     *         template text
     */
    DefaultMessageTemplate(int errorCode, String templateText) {
        this(errorCode, templateText, null, COUNT_UNKNOWN);
    }

    /**
     * Creates a message template with {@code errorCode}, {@code templateText}, and {@code sqlState}.
     *
     * @param errorCode
     *         error code
     * @param templateText
     *         template text
     * @param sqlState
     *         SQLstate (can be {@code null})
     * @throws IllegalArgumentException
     *         if {@code sqlState} is non-{@code null}, and not 5 characters long
     */
    DefaultMessageTemplate(int errorCode, String templateText, @Nullable String sqlState) {
        this(errorCode, templateText, sqlState, COUNT_UNKNOWN);
    }

    /**
     * Creates a message template with {@code errorCode}, {@code templateText}, {@code sqlState} and a parameter count.
     *
     * @param errorCode
     *         error code
     * @param templateText
     *         template text
     * @param sqlState
     *         SQLstate (can be {@code null})
     * @param parameterCount
     *         number of parameters in template, use {@code -1} ({@link #COUNT_UNKNOWN}) to determine the count on first
     *         use; the parameter count should be determined by the highest parameter number + 1
     * @throws IllegalArgumentException
     *         if {@code sqlState} is non-{@code null}, and not 5 characters long
     */
    private DefaultMessageTemplate(int errorCode, String templateText, @Nullable String sqlState, int parameterCount) {
        assert parameterCount >= -1 : "parameterCount must be greater than or equal to -1";
        this.errorCode = errorCode;
        this.templateText = requireNonNull(templateText, "templateText");
        // This default message template allows null, while validateSqlState rejects null
        this.sqlState = sqlState != null ? MessageTemplate.validateSqlState(sqlState) : null;
        this.parameterCount = parameterCount;
    }

    @Override
    public int errorCode() {
        return errorCode;
    }

    @Override
    public @Nullable String sqlState() {
        return sqlState;
    }

    /**
     * @return template text of this message
     * @see #appendMessage(StringBuilder, List)
     */
    String templateText() {
        return templateText;
    }

    /**
     * Number of parameters in the message.
     * <p>
     * Phrased differently, this is the highest parameter number + 1. That is, even if a template only has one
     * parameter, if that number is &mdash; for example &mdash; 5, then the parameter count is 6 because
     * parameters 0 - 4 are counted even if they do not occur in the template itself. And having two parameters 0,
     * means the parameter count is 1, not 2.
     * </p>
     *
     * @return parameter count
     */
    int parameterCount() {
        // NOTE: We accept that concurrent invocation on multiple threads may duplicate the work done
        if (parameterCount != COUNT_UNKNOWN) return parameterCount;
        return parameterCount = getParamCountInternal();
    }

    private int getParamCountInternal() {
        int maxIndex = -1;
        Matcher matcher = createParameterMatcher();
        while (matcher.find()) {
            int parameterIndex = Integer.parseInt(matcher.group(1));
            maxIndex = Math.max(maxIndex, parameterIndex);
        }
        return maxIndex + 1;
    }

    @Override
    public MessageTemplate withDefaultSqlState(String defaultSqlState) {
        if (this.sqlState != null) return this;
        return withSqlState(defaultSqlState);
    }

    @Override
    public MessageTemplate withSqlState(String sqlState) {
        if (requireNonNull(sqlState, "sqlState").equals(this.sqlState)) return this;
        return new OverriddenSqlStateMessageTemplate(this, sqlState);
    }

    private Matcher createParameterMatcher() {
        return MESSAGE_PARAM_PATTERN.matcher(templateText);
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
     */
    @Override
    public void appendMessage(StringBuilder messageBuffer, List<? extends @Nullable Object> parameters) {
        int templateParameterCount = parameterCount();
        int actualParameterCount = parameters.size();
        messageBuffer.ensureCapacity(messageBuffer.length()
                + estimateBufferCapacity(Math.max(templateParameterCount, actualParameterCount)));
        Matcher matcher = createParameterMatcher();
        while (matcher.find()) {
            int paramIndex = Integer.parseInt(matcher.group(1));
            Object parameterValue = paramIndex < actualParameterCount ? parameters.get(paramIndex) : null;
            matcher.appendReplacement(messageBuffer, "");
            // Append separately to avoid having to quote the replacement string
            messageBuffer.append(parameterValue != null ? parameterValue : "(null)");
        }
        matcher.appendTail(messageBuffer);
        if (errorCode != isc_formatted_exception && actualParameterCount > templateParameterCount) {
            // Include extra parameters at the end of the message
            for (Object extraParameter : parameters.subList(templateParameterCount, actualParameterCount)) {
                messageBuffer.append("; ").append(extraParameter != null ? extraParameter : "(null)");
            }
        }
    }

    private int estimateBufferCapacity(int parameterSize) {
        return templateText.length() + parameterSize * PARAM_SIZE_FACTOR;
    }

    /**
     * Creates a message template with a "not found" message for {@code errorCode} and SQLstate {@code null}.
     *
     * @param errorCode
     *         error code
     * @return message template
     */
    static MessageTemplate notFound(int errorCode) {
        return new DefaultMessageTemplate(errorCode, "No message for code " + errorCode + " found.", null, 0);
    }

}
