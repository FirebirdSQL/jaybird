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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Object representing a Firebird server version. The version string is returned
 * in response to the <code>isc_info_firebird_version</code> information call.
 * Expected version format is:
 * <p>
 * <code>&lt;platform&gt;-&lt;type&gt;&lt;majorVersion&gt;.&lt;minorVersion&gt;.&lt;variant&gt;.&lt;buildNum&gt; &lt;serverName&gt;[,&lt;extended server info&gt;]</code>
 * </p>
 * <p>
 * where <code>platform</code> is a two-character platform identification string,
 * Windows for example is "WI", <code>type</code> is one of the three characters:
 * "V" - production version, "T" - beta version, "X" - development version.
 * </p>
 */
public final class GDSServerVersion implements Serializable {

    private static final long serialVersionUID = -3401092369588765195L;

    public static final String TYPE_PRODUCTION = "V";
    public static final String TYPE_BETA = "T";
    public static final String TYPE_DEVELOPMENT = "X";

    /**
     * GDSServerVersion that can be used as a dummy/invalid object when a version object is required, but none is available.
     */
    public static final GDSServerVersion INVALID_VERSION = new GDSServerVersion("INVALID", "", "", "", 0, 0, 0, 0, "", "");

    private static final Pattern VERSION_PATTERN = 
    	Pattern.compile("((\\w{2})-(\\w)(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)) ([^-,]+)(?:[-,](.*))?");
    
    private static final int FULL_VERSION_IDX = 1;
    private static final int PLATFORM_IDX = 2;
    private static final int TYPE_IDX = 3;
    private static final int MAJOR_IDX = 4;
    private static final int MINOR_IDX = 5;
    private static final int VARIANT_IDX = 6;
    private static final int BUILD_IDX = 7;
    private static final int SERVER_NAME_IDX = 8;
    private static final int EXTENDED_INFO_IDX = 9;    

    private final String rawStr;
    
    private final String platform;
    private final String type;

    private final String fullVersion;
    private final int majorVersion;
    private final int minorVersion;
    private final int variant;
    private final int buildNumber;

    private final String serverName;
    private final String extendedServerName;

    private GDSServerVersion(String rawStr, String platform, String type, String fullVersion, int majorVersion,
            int minorVersion, int variant, int buildNumber, String serverName, String extendedServerName) {
        this.rawStr = rawStr;
        this.platform = platform;
        this.type = type;
        this.fullVersion = fullVersion;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.variant = variant;
        this.buildNumber = buildNumber;
        this.serverName = serverName;
        this.extendedServerName = extendedServerName;
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
        return extendedServerName;
    }
    
    public String getFullVersion() {
    	return fullVersion;
    }

    public int hashCode() {
        return rawStr.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (!(obj instanceof GDSServerVersion)) return false;

        GDSServerVersion that = (GDSServerVersion) obj;

        return rawStr.equals(that.rawStr);
    }

    public String toString() {
        return rawStr;
    }

    /**
     * Parse the raw version string and create a GDSServerVersion object.
     * 
     * @param versionString string to parse.
     * 
     * @throws GDSServerVersionException if versionString does not match expected pattern
     */
    public static GDSServerVersion parseRawVersion(String versionString) throws GDSServerVersionException {
        Matcher matcher = VERSION_PATTERN.matcher(versionString);
        if (!matcher.matches()) {
            throw new GDSServerVersionException(String.format("Version string \"%s\" does not match expected format",
            		versionString));
        }

        return new GDSServerVersion(
                versionString, 
                matcher.group(PLATFORM_IDX),
                matcher.group(TYPE_IDX), 
                matcher.group(FULL_VERSION_IDX), 
                Integer.parseInt(matcher.group(MAJOR_IDX)),
                Integer.parseInt(matcher.group(MINOR_IDX)), 
                Integer.parseInt(matcher.group(VARIANT_IDX)),
                Integer.parseInt(matcher.group(BUILD_IDX)), 
                matcher.group(SERVER_NAME_IDX),
                matcher.group(EXTENDED_INFO_IDX));
    }

    /**
     * Convenience method to check if the major.minor of this version is equal to or larger than the specified required
     * version.
     *
     * @param requiredMajorVersion Required major version
     * @param requiredMinorVersion Required minor version
     * @return <code>true</code> when current major is larger than required, or major is same and minor is equal to or
     * larger than required
     */
    public boolean isEqualOrAbove(int requiredMajorVersion, int requiredMinorVersion) {
        return majorVersion > requiredMajorVersion ||
                (majorVersion == requiredMajorVersion && minorVersion >= requiredMinorVersion);
    }
}
