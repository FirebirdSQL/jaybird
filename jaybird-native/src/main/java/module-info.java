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
module org.firebirdsql.jna {
    requires org.firebirdsql.jaybird;
    requires com.sun.jna;
    requires static jakarta.servlet;

    exports org.firebirdsql.jna.embedded.classpath;
    exports org.firebirdsql.jna.embedded.spi;
    exports org.firebirdsql.jna.fbclient to com.sun.jna;
    exports org.firebirdsql.jna.jakarta;

    uses org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider;

    provides org.firebirdsql.gds.impl.GDSFactoryPlugin
            with org.firebirdsql.gds.impl.jni.NativeGDSFactoryPlugin,
                    org.firebirdsql.gds.impl.jni.EmbeddedGDSFactoryPlugin;

    provides org.firebirdsql.jaybird.props.spi.ConnectionPropertyDefinerSpi
            with org.firebirdsql.gds.ng.jna.NativeConnectionPropertyDefiner;
}