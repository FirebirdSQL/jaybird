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

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link org.firebirdsql.gds.MessageTemplate}.
 *
 * @author Mark Rotteveel
 */
class MessageTemplateTest {

    @Test
    void getMessageTemplate_noMessageFound() {
        final int errorCode = 1;
        final String expectedTemplateText = "No message for code 1 found.";

        DefaultMessageTemplate template = (DefaultMessageTemplate) MessageTemplate.of(1);

        assertEquals(errorCode, template.errorCode(), "errorCode");
        assertEquals(expectedTemplateText, template.templateText(), "templateText");
        assertNull(template.sqlState(), "sqlState");
    }

    @Test
    void getMessageTemplate_JaybirdRange() {
        // Test expectation is tied to the actual mapping defined.
        final int errorCode = JaybirdErrorCodes.jb_blobGetSegmentNegative;
        final String expectedTemplateText = "getSegment called with sizeRequested {0}, should be > 0";
        final String expectedSqlState = "HY090";

        DefaultMessageTemplate template = (DefaultMessageTemplate)  MessageTemplate.of(errorCode);

        assertEquals(errorCode, template.errorCode(), "errorCode");
        assertEquals(expectedTemplateText, template.templateText(), "templateText");
        assertEquals(expectedSqlState, template.sqlState(), "sqlState");
    }

    @Test
    void getMessageTemplate_FirebirdRange() {
        // Test expectation is tied to the actual state mapping defined.
        final int errorCode = ISCConstants.isc_integ_fail;
        final String expectedTemplateText = "action cancelled by trigger ({0}) to preserve data integrity";
        final String expectedSqlState = "27000";

        DefaultMessageTemplate template = (DefaultMessageTemplate)  MessageTemplate.of(errorCode);

        assertEquals(errorCode, template.errorCode(), "errorCode");
        assertEquals(expectedTemplateText, template.templateText(), "templateText");
        assertEquals(expectedSqlState, template.sqlState(), "sqlState");
    }

    @Test
    void extraParametersOfFormattedExceptionIgnored() {
        /* Formatted exception are already formatted on the server, but the server also sends the format parameters to
           the client. The normal rendering includes extra parameters in the message, but here it doesn't make sense */
        var template = MessageTemplate.of(ISCConstants.isc_formatted_exception);

        assertEquals("Already formatted exception", template.toMessage(
                List.of("Already formatted exception", "original parameter 1", "original parameter 2")));
    }

    @Test
    void parameterCount_templateWithoutPlaceholders() {
        var template = new DefaultMessageTemplate(0, "Template without placeholders");

        assertEquals(0, template.parameterCount());
    }

    @Test
    void parameterCount_templateSinglePlaceholder() {
        var template = new DefaultMessageTemplate(0, "Template with {0} placeholder");

        assertEquals(1, template.parameterCount());
    }

    @Test
    void parameterCount_templateMultiplePlaceholders() {
        var template = new DefaultMessageTemplate(0, "Template with {0} placeholders in {1}");

        assertEquals(2, template.parameterCount());
    }

    // Repeated parameters are only used in Jaybird messages, not in Firebird messages
    @Test
    void parameterCount_repeatedParameters() {
        var template = new DefaultMessageTemplate(0, "Template with {0} placeholders in {1}, repeating {0}");

        assertEquals(2, template.parameterCount());
    }

    // In practice, there should be no gaps in parameter numbering
    @Test
    void parameterCount_gaps() {
        var template = new DefaultMessageTemplate(0, "Template with parameter not starting with 0: {2}");

        assertEquals(3, template.parameterCount());
    }

    @Test
    void toMessage_noParameters() {
        final String templateText = "Template with {0} placeholders";
        final String expected = "Template with (null) placeholders";
        var template = new DefaultMessageTemplate(0, templateText);

        assertEquals(expected, template.toMessage(List.of()));
    }

    @Test
    void toMessage_withParameters() {
        final String templateText = "Template with {0} placeholders";
        final String expected = "Template with xyz placeholders";
        var template = new DefaultMessageTemplate(0, templateText);

        assertEquals(expected, template.toMessage(List.of("xyz")));
    }

    @Test
    void toMessage_withNullParameters() {
        final String templateText = "Template with {0} placeholders";
        final String expected = "Template with (null) placeholders";
        var template = new DefaultMessageTemplate(0, templateText);

        assertEquals(expected, template.toMessage(Collections.singletonList(null)));
    }

    @Test
    void toMessage_multiParam_Extra() {
        final String templateText = "Template with {0} placeholders in {1}";
        final String expected = "Template with abc placeholders in def; ghi; jkl";
        var template = new DefaultMessageTemplate(0, templateText);

        assertEquals(expected, template.toMessage(List.of("abc", "def", "ghi", "jkl")));
    }

    @Test
    void parameterValuesWithSlashesAndDollarSign() {
        final String templateText = "Template with {0} and {1} trailing text";
        final String expected = "Template with D:\\value and $1 trailing text";
        var template = new DefaultMessageTemplate(0, templateText);

        assertEquals(expected, template.toMessage(List.of("D:\\value", "$1")));
    }

    /**
     * Tests rendering of quotes around a message parameter.
     * <p>
     * NOTE: Reason for this test is that if we were using MessageFormat (we aren't), the current format using
     * "something '{0}'" would not work (that would require "something ''{0}''"). So, if we ever switch to using
     * MessageFormat or a class using the same parsing rules as MessageFormat, this test will fail unless the
     * message text is fixed.
     * </p>
     */
    @Test
    void templateWithQuotesAroundParameters() {
        var template = new DefaultMessageTemplate(0, "Argument1: '{0}', argument2: '{1}'");

        assertEquals("Argument1: 'Parameter 1', argument2: 'Parameter 2'",
                template.toMessage(List.of("Parameter 1", "Parameter 2")));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void withDefaultSqlState_currentValueNull() {
        var template = new DefaultMessageTemplate(0, "Test message", null);

        MessageTemplate withDefaultSqlState = template.withDefaultSqlState("0A000");
        assertEquals("0A000", withDefaultSqlState.sqlState());

        assertThrows(NullPointerException.class, () -> template.withDefaultSqlState(null));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void withDefaultSqlState_currentValueNonNull() {
        var template = new DefaultMessageTemplate(0, "Test message", "HY000");

        MessageTemplate withDefaultSqlState = template.withDefaultSqlState("0A000");
        assertEquals("HY000", withDefaultSqlState.sqlState());
        assertSame(template, withDefaultSqlState);

        assertDoesNotThrow(() -> template.withDefaultSqlState(null));
    }

    @Test
    void withSqlState_differentValue() {
        var template = new DefaultMessageTemplate(0, "Test message", "HY000");

        MessageTemplate withSqlState = template.withSqlState("0A000");
        assertEquals("0A000", withSqlState.sqlState());
    }

    @Test
    void withSqlState_sameValue() {
        var template = new DefaultMessageTemplate(0, "Test message", "HY000");

        MessageTemplate withSqlState = template.withSqlState("HY000");
        assertEquals("HY000", withSqlState.sqlState());
        assertSame(template, withSqlState);
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void withSqlState_null() {
        var template = new DefaultMessageTemplate(0, "Test message", "HY000");

        assertThrows(NullPointerException.class, () -> template.withSqlState(null));
    }

    @Test
    void appendErrorInfoSuffix() {
        var template = new DefaultMessageTemplate(15, "message", "0X123");
        var sb = new StringBuilder();
        template.appendErrorInfoSuffix(sb);

        assertEquals(" [SQLState:0X123, ISC error code:15]", sb.toString());
    }

}
