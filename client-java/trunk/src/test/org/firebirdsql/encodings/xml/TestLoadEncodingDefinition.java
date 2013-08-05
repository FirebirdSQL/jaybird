/*
 * $Id$
 *
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a source repository history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.encodings.xml;

import org.firebirdsql.encodings.Encoding;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestLoadEncodingDefinition {

    @Test
    public void testLoadEncodingDefinition() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(Encodings.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        final InputStream inputStream = Encoding.class.getResourceAsStream("default-firebird-encodings.xml");
        assertNotNull("default-firebird-encodings.xml not found", inputStream);
        Encodings encodings = (Encodings) unmarshaller.unmarshal(inputStream);

        assertNotNull("No encoding object", encodings);
        assertEquals("Unexpected count of encodingDefinitions in default-firebird-encodings.xml", 52, encodings.encodingDefinition.size());
    }
}
