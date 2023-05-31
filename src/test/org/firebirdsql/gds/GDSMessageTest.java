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

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link org.firebirdsql.gds.GDSExceptionHelper.GDSMessage}.
 *
 * @author Mark Rotteveel
 */
class GDSMessageTest {

    @Test
    void getParamCount_templateWithoutPlaceholders() {
        GDSExceptionHelper.GDSMessage message = new GDSExceptionHelper.GDSMessage("Template without placeholders");

        assertEquals(0, message.getParamCount());
    }

    @Test
    void getParamCount_templateSinglePlaceholder() {
        GDSExceptionHelper.GDSMessage message = new GDSExceptionHelper.GDSMessage("Template with {0} placeholders");

        assertEquals(1, message.getParamCount());
    }

    @Test
    void getParamCount_templateMultiplePlaceholders() {
        GDSExceptionHelper.GDSMessage message = new GDSExceptionHelper.GDSMessage(
                "Template with {0} placeholders in {1}");

        assertEquals(2, message.getParamCount());
    }

    @Test
    void toString_noParametersSet() {
        final String template = "Template with {0} placeholders";
        final String expected = "Template with (null) placeholders";
        GDSExceptionHelper.GDSMessage message = new GDSExceptionHelper.GDSMessage(template);

        assertThat(message.toString(), equalTo(expected));
    }

    @Test
    void toString_parameterSet() {
        final String template = "Template with {0} placeholders";
        final String expected = "Template with xyz placeholders";
        GDSExceptionHelper.GDSMessage message = new GDSExceptionHelper.GDSMessage(template);
        message.setParameter(0, "xyz");

        assertThat(message.toString(), equalTo(expected));
    }

    @Test
    void toString_multiParam_Extra() {
        final String template = "Template with {0} placeholders in {1}";
        final String expected = "Template with abc placeholders in def; ghi; jkl";
        GDSExceptionHelper.GDSMessage message = new GDSExceptionHelper.GDSMessage(template);
        message.setParameters(Arrays.asList("abc", "def", "ghi", "jkl"));

        assertThat(message.toString(), equalTo(expected));
    }

    @Test
    void parameterValuesWithSlashesAndDollarSign() {
        final String template = "Template with {0} and {1} trailing text";
        final String expected = "Template with D:\\value and $1 trailing text";
        GDSExceptionHelper.GDSMessage message = new GDSExceptionHelper.GDSMessage(template);
        message.setParameters(Arrays.asList("D:\\value", "$1"));

        assertThat(message.toString(), equalTo(expected));
    }
}
