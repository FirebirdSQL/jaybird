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
package org.firebirdsql.util;

import org.firebirdsql.jdbc.JdbcVersionSupport;
import org.firebirdsql.jdbc.JdbcVersionSupportHolder;
import org.firebirdsql.logging.LoggerFactory;
import org.jmock.api.Imposteriser;
import org.jmock.lib.JavaReflectionImposteriser;

import java.lang.reflect.Field;

/**
 * Class imposteriser for use with jmock.
 * <p>
 * The ByteBuddyClassImposteriser doesn't work on Java 7, so this selects the imposteriser to use based on the runtime
 * version.
 * </p>
 */
public class ClassImposteriserAccess {

    public static final Imposteriser INSTANCE;

    static {
        JdbcVersionSupport jdbcVersionSupport = JdbcVersionSupportHolder.INSTANCE.getJdbcVersionSupport();
        String imposteriserClassName = jdbcVersionSupport.getClass().getSimpleName().equals("Jdbc41VersionSupport")
                ? "org.jmock.lib.legacy.ClassImposteriser"
                : "org.jmock.imposters.ByteBuddyClassImposteriser";

        Imposteriser instance;
        try {
            Class<?> imposteriserClass = Class.forName(imposteriserClassName);
            Field instanceField = imposteriserClass.getDeclaredField("INSTANCE");
            instance = (Imposteriser) instanceField.get(null);
        } catch (ReflectiveOperationException e) {
            LoggerFactory.getLogger(ClassImposteriserAccess.class)
                    .error("Could not load Imposteriser, falling back to interface imposteriser", e);
            // Use the normal Imposteriser, which will cause runtime test failures because it can't mock classes
            instance = JavaReflectionImposteriser.INSTANCE;
        }
        INSTANCE = instance;
    }

}
