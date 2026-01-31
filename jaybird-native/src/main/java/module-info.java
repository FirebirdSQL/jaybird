// SPDX-FileCopyrightText: Copyright 2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
module org.firebirdsql.jna {
    requires org.firebirdsql.jaybird;
    requires com.sun.jna;
    requires static jakarta.servlet;

    // Declare as optional for deployment simplicity
    requires static org.jspecify;

    exports org.firebirdsql.jna.embedded.classpath;
    exports org.firebirdsql.jna.embedded.spi;
    exports org.firebirdsql.jna.jakarta;

    opens org.firebirdsql.jna.fbclient to com.sun.jna;

    uses org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider;

    provides org.firebirdsql.gds.impl.GDSFactoryPlugin
            with org.firebirdsql.gds.impl.jni.NativeGDSFactoryPlugin,
                    org.firebirdsql.gds.impl.jni.EmbeddedGDSFactoryPlugin;

    provides org.firebirdsql.jaybird.props.spi.ConnectionPropertyDefinerSpi
            with org.firebirdsql.gds.ng.jna.NativeConnectionPropertyDefiner;
}