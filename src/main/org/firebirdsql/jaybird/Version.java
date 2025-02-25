// SPDX-FileCopyrightText: Copyright 2019-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird;

import java.util.ResourceBundle;

/**
 * Class to access information from {@code org/firebirdsql/jaybird/version.properties}.
 *
 * @author Mark Rotteveel
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
            System.getLogger(Version.class.getName()).log(System.Logger.Level.ERROR,
                    "org.firebirdsql.jaybird.Version: Unable to load version information: {0}", (Object) e);
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
            System.getLogger(Version.class.getName()).log(System.Logger.Level.ERROR,
                    "org.firebirdsql.jaybird.Version: Unable to parse number {0}; defaulting to 0: {1}", stringValue, e);
            return 0;
        }
    }

}
