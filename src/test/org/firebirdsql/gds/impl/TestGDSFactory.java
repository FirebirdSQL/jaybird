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
package org.firebirdsql.gds.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link GDSFactory}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestGDSFactory {

    @Test
    public void testGetTypeForProtocol() {
        assertEquals("PURE_JAVA", String.valueOf(GDSFactory.getTypeForProtocol("jdbc:firebirdsql://localhost/mydb")));
        assertEquals("PURE_JAVA", String.valueOf(GDSFactory.getTypeForProtocol("jdbc:firebirdsql:localhost:mydb")));
        assertEquals("PURE_JAVA", String.valueOf(GDSFactory.getTypeForProtocol("jdbc:firebirdsql:java://localhost/mydb")));
        assertEquals("OOREMOTE", String.valueOf(GDSFactory.getTypeForProtocol("jdbc:firebirdsql:oo://localhost/mydb")));
        assertEquals("NATIVE", String.valueOf(GDSFactory.getTypeForProtocol("jdbc:firebirdsql:native://localhost/mydb")));
        assertEquals("NATIVE", String.valueOf(GDSFactory.getTypeForProtocol("jdbc:firebirdsql:native:localhost:mydb")));
        assertEquals("EMBEDDED", String.valueOf(GDSFactory.getTypeForProtocol("jdbc:firebirdsql:embedded:mydb")));
        assertEquals("LOCAL", String.valueOf(GDSFactory.getTypeForProtocol("jdbc:firebirdsql:local:mydb")));
    }
}
