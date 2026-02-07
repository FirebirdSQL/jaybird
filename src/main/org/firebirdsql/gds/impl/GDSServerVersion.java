// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2011-2026 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2016 Artyom Smirnov
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.AbstractVersion;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.firebirdsql.jaybird.util.StringUtils.isNullOrBlank;

/**
 * Object representing a Firebird server version. The version string is returned
 * in response to the {@code isc_info_firebird_version} information call.
 * <p>
 * Expected version format is:
 * {@code <platform>-<type><majorVersion>.<minorVersion>.<variant>.<buildNum>[-<revision>] <serverName>},
 * and additional version string elements if present.
 * </p>
 * <p>
 * where {@code platform} is a two-character platform identification string,
 * Windows for example is "WI", {@code type} is one of the three characters:
 * "V" - production version, "T" - beta version, "X" - development version.
 * </p>
 */
public final class GDSServerVersion extends AbstractVersion {

    @Serial
    private static final long serialVersionUID = -3401092369588765195L;

    @SuppressWarnings("unused")
    public static final String TYPE_PRODUCTION = "V";
    @SuppressWarnings("unused")
    public static final String TYPE_BETA = "T";
    @SuppressWarnings("unused")
    public static final String TYPE_DEVELOPMENT = "X";
    public static final String CONNECTION_OPTION_ENCRYPTED = "C";
    public static final String CONNECTION_OPTION_COMPRESSION = "Z";

    /**
     * GDSServerVersion that can be used as a dummy/invalid object when a version object is required, but none is
     * available.
     */
    public static final GDSServerVersion INVALID_VERSION =
            new GDSServerVersion(new String[] { "INVALID" }, "", "", "", 0, 0, 0, 0, "");

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("((\\w{2})-(\\w)(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)(?:-\\S+)?) (.+)");

    private static final Pattern CONNECTION_METADATA_PATTERN = Pattern.compile("/P(\\d+)(?::([^:]+))?$");

    private static final int FULL_VERSION_IDX = 1;
    private static final int PLATFORM_IDX = 2;
    private static final int TYPE_IDX = 3;
    private static final int MAJOR_IDX = 4;
    private static final int MINOR_IDX = 5;
    private static final int VARIANT_IDX = 6;
    private static final int BUILD_IDX = 7;
    private static final int SERVER_NAME_IDX = 8;

    private final String[] rawVersions;

    private final String platform;
    private final String type;

    private final String fullVersion;
    private final int variant;
    private final int buildNumber;

    private final String serverName;

    private GDSServerVersion(String[] rawVersions, String platform, String type, String fullVersion,
            int majorVersion, int minorVersion, int variant, int buildNumber, String serverName) {
        super(majorVersion, minorVersion);
        this.rawVersions = rawVersions.clone();
        this.platform = platform;
        this.type = type;
        this.fullVersion = fullVersion;
        this.variant = variant;
        this.buildNumber = buildNumber;
        this.serverName = serverName;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public int getMajorVersion() {
        return major();
    }

    public int getMinorVersion() {
        return minor();
    }

    public String getPlatform() {
        return platform;
    }

    public String getServerName() {
        return serverName;
    }

    public String getType() {
        return type;
    }

    public int getVariant() {
        return variant;
    }

    /**
     * @return an unmodifiable list of the raw version strings
     * @since 7
     */
    public List<String> getRawVersions() {
        return List.of(rawVersions);
    }

    public @Nullable String getExtendedServerName() {
        return switch (rawVersions.length) {
            case 0, 1 -> null;
            case 2 -> rawVersions[1];
            default -> String.join(",", Arrays.asList(rawVersions).subList(1, rawVersions.length));
        };
    }

    public String getFullVersion() {
        return fullVersion;
    }

    /**
     * @return Protocol version of the connection, or {@code -1} if this information is not available.
     */
    public int getProtocolVersion() {
        // We assume the protocol information is in the second version string,
        // this assumption may be wrong for multi-hop connections
        if (rawVersions.length == 1 || isNullOrBlank(rawVersions[1])) return -1;
        Matcher connectionMetadataMatcher = CONNECTION_METADATA_PATTERN.matcher(rawVersions[1]);
        if (!connectionMetadataMatcher.find()) return -1;

        String protocolVersion = connectionMetadataMatcher.group(1);
        return Integer.parseInt(protocolVersion);
    }

    /**
     * @return {@code true} if encryption is used, {@code false} if no encryption is used <b>or</b> if this information
     * is not available
     */
    public boolean isWireEncryptionUsed() {
        return getConnectionOptions().contains(CONNECTION_OPTION_ENCRYPTED);
    }

    public boolean isWireCompressionUsed() {
        return getConnectionOptions().contains(CONNECTION_OPTION_COMPRESSION);
    }

    private String getConnectionOptions() {
        // We assume the protocol information is in the second version string,
        // this assumption may be wrong for multi-hop connections
        if (rawVersions.length == 1 || isNullOrBlank(rawVersions[1])) return "";
        Matcher connectionMetadataMatcher = CONNECTION_METADATA_PATTERN.matcher(rawVersions[1]);
        if (!connectionMetadataMatcher.find()) return "";

        String connectionOptions = connectionMetadataMatcher.group(2);
        return connectionOptions != null ? connectionOptions : "";
    }

    public int hashCode() {
        return Arrays.hashCode(rawVersions);
    }

    public boolean equals(@Nullable Object obj) {
        return obj == this || obj instanceof GDSServerVersion that && Arrays.equals(this.rawVersions, that.rawVersions);
    }

    public String toString() {
        return rawVersions.length == 1 ? rawVersions[0] : String.join(",", rawVersions);
    }

    /**
     * Parse the raw version string and create a GDSServerVersion object.
     *
     * @param versionStrings
     *         strings to parse, expects a non-empty array with at least 1, and usually 2 version strings
     * @throws GDSServerVersionException
     *         if versionString does not match expected pattern
     */
    public static GDSServerVersion parseRawVersion(String... versionStrings) throws GDSServerVersionException {
        if (versionStrings == null || versionStrings.length == 0 || isNullOrBlank(versionStrings[0])) {
            throw new GDSServerVersionException("No version string information present");
        }

        Matcher matcher = VERSION_PATTERN.matcher(versionStrings[0]);
        if (!matcher.matches()) {
            throw new GDSServerVersionException(
                    String.format("Version string \"%s\" does not match expected format", versionStrings[0]));
        }

        return new GDSServerVersion(
                versionStrings,
                matcher.group(PLATFORM_IDX),
                matcher.group(TYPE_IDX),
                matcher.group(FULL_VERSION_IDX),
                Integer.parseInt(matcher.group(MAJOR_IDX)),
                Integer.parseInt(matcher.group(MINOR_IDX)),
                Integer.parseInt(matcher.group(VARIANT_IDX)),
                Integer.parseInt(matcher.group(BUILD_IDX)),
                matcher.group(SERVER_NAME_IDX));
    }

    /**
     * Convenience method to check if the major.minor.variant of this version is equal to or larger than the specified
     * required version.
     *
     * @param requiredMajorVersion
     *         Required major version
     * @param requiredMinorVersion
     *         Required minor version
     * @param requiredVariant
     *         Required variant version
     * @return {@code true} when current major is larger than required, or major is same and minor is equal to required
     * and variant equal to or larger than required, or major is same and minor is larger than required
     * @see #isEqualOrAbove(int)
     * @see #isEqualOrAbove(int, int)
     */
    public boolean isEqualOrAbove(int requiredMajorVersion, int requiredMinorVersion, int requiredVariant) {
        int majorVersion = major();
        int minorVersion = minor();
        return majorVersion > requiredMajorVersion ||
                (majorVersion == requiredMajorVersion &&
                        (minorVersion == requiredMinorVersion && variant >= requiredVariant ||
                                minorVersion > requiredMinorVersion
                        )
                );
    }

    @Override
    public int compareTo(AbstractVersion o) {
        int majorMinorDiff = super.compareTo(o);
        if (majorMinorDiff != 0 || !(o instanceof GDSServerVersion other)) return majorMinorDiff;
        int variantDiff = Integer.compare(this.variant, other.variant);
        if (variantDiff != 0) return variantDiff;
        return Integer.compare(this.buildNumber, other.buildNumber);
    }

}
