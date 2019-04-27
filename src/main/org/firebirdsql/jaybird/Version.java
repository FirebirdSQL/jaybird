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
package org.firebirdsql.jaybird;

import org.firebirdsql.logging.LoggerFactory;

import java.util.ResourceBundle;

/**
 * Class to access information from {@code org/firebirdsql/jaybird/version.properties}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class Version {

    public static final String JAYBIRD_SIMPLE_VERSION;
    public static final String JAYBIRD_DISPLAY_VERSION;
    public static final int JAYBIRD_MAJOR_VERSION;
    public static final int JAYBIRD_MINOR_VERSION;

    static {
        String jaybirdSimpleVersion;
        String jaybirdDisplayVersion;
        int jaybirdMajorVersion;
        int jaybirdMinorVersion;
        try {
            ResourceBundle resourceBundle = ResourceBundle.getBundle("org.firebirdsql.jaybird.version");
            jaybirdSimpleVersion = resourceBundle.getString("jaybird.version.simple");
            jaybirdDisplayVersion = resourceBundle.getString("jaybird.version.display");
            jaybirdMajorVersion = tryParseInt(resourceBundle.getString("jaybird.version.major"));
            jaybirdMinorVersion = tryParseInt(resourceBundle.getString("jaybird.version.minor"));
        } catch (Exception e) {
            // Intentionally not logging stacktrace
            LoggerFactory.getLogger(Version.class)
                    .error("org.firebirdsql.jaybird.Version: Unable to load version information: " + e);
            // Resource bundle missing, or key missing
            jaybirdSimpleVersion = "version unknown";
            jaybirdDisplayVersion = "Jaybird (version unknown)";
            jaybirdMajorVersion = 0;
            jaybirdMinorVersion = 0;
        }
        JAYBIRD_SIMPLE_VERSION = jaybirdSimpleVersion;
        JAYBIRD_DISPLAY_VERSION = jaybirdDisplayVersion;
        JAYBIRD_MAJOR_VERSION = jaybirdMajorVersion;
        JAYBIRD_MINOR_VERSION = jaybirdMinorVersion;
    }

    private Version() {
        // no instances
    }

    private static int tryParseInt(String stringValue) {
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            // Intentionally not logging stacktrace
            LoggerFactory.getLogger(Version.class)
                    .error("org.firebirdsql.jaybird.Version: Unable to parse number " + stringValue
                            + "; defaulting to 0: " + e);
            return 0;
        }
    }

}
