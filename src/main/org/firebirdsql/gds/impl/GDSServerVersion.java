/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Object representing a Firebird server version. The version string is returned
 * in response to the <code>isc_info_firebird_version</code> information call.
 * Expected version format is:
 * <p>
 * <code>&lt;platform&gt;-&lt;type&gt;&lt;majorVersion&gt;.&lt;minorVersion&gt;.&lt;variant&gt;.&lt;buildNum&gt;
 * &lt;serverName&gt;</code>, and additional version string elements if present.
 * </p>
 * where <code>platform</code> is a two-character platform identification string,
 * Windows for example is "WI", <code>type</code> is one of the three characters:
 * "V" - production version, "T" - beta version, "X" - development version.
 *
 */
public final class GDSServerVersion implements Serializable {

	private static final long serialVersionUID = -153657557318248541L;

	@SuppressWarnings("unused")
    public static final String TYPE_PRODUCTION = "V";
    @SuppressWarnings("unused")
    public static final String TYPE_BETA = "T";
    @SuppressWarnings("unused")
    public static final String TYPE_DEVELOPMENT = "X";

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("((\\w{2})-(\\w)(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)(?:-\\S+)?) (.+)");

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
    private final int majorVersion;
    private final int minorVersion;
    private final int variant;
    private final int buildNumber;

    private String serverName;

    private GDSServerVersion(String[] rawVersions, String platform, String type, String fullVersion, int majorVersion,
            int minorVersion, int variant, int buildNumber, String serverName) {
        this.rawVersions = rawVersions.clone();
        this.platform = platform;
        this.type = type;
        this.fullVersion = fullVersion;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.variant = variant;
        this.buildNumber = buildNumber;
        this.serverName = serverName;
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
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

    public String getExtendedServerName() {
        if (rawVersions.length < 2) {
            return null;
        } else if (rawVersions.length == 2) {
            return rawVersions[1];
        } else {
            StringBuilder sb = new StringBuilder();
            for (int idx = 1; idx < rawVersions.length; idx++) {
                if (idx > 1) {
                    sb.append(',');
                }
                sb.append(rawVersions[idx]);
            }
            return sb.toString();
        }
    }

    public String getFullVersion() {
    	return fullVersion;
    }

    public int hashCode() {
        return Arrays.hashCode(rawVersions);
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (!(obj instanceof GDSServerVersion)) return false;

        GDSServerVersion that = (GDSServerVersion) obj;

        return Arrays.equals(this.rawVersions, that.rawVersions);
    }

    public String toString() {
        if (rawVersions.length == 1) {
            return rawVersions[0];
        }
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        sb.append(rawVersions[idx++]);
        do {
            sb.append(',');
            sb.append(rawVersions[idx++]);
        } while (idx < rawVersions.length);
        return sb.toString();
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
        if (versionStrings == null || versionStrings.length == 0 || versionStrings[0] == null) {
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
    
}
