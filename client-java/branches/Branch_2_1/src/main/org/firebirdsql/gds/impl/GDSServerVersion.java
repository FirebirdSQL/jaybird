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
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Object representing a Firebird server version. The version string is returned
 * in response to the <code>isc_info_firebird_version</code> information call.
 * Expected version format is:
 * <p>
 * <platform>-<type><majorVersion>.<minorVersion>.<variant>.<buildNum> <serverName>[,<extended server info>]
 * </p>
 * where <code>platform</code> is a two-character platform identification string,
 * Windows for example is "WI", <code>type</code> is one of the three characters:
 * "V" - production version, "T" - beta version, "X" - development version.
 *
 */
public class GDSServerVersion implements Serializable {

    public static final String TYPE_PRODUCTION = "V";
    public static final String TYPE_BETA = "T";
    public static final String TYPE_DEVELOPMENT = "X";

    private String rawStr;
    
    private String platform;
    private String type;

    private int majorVersion;
    private int minorVersion;
    private int variant;
    private int buildNumber;

    private String serverName;
    private String extendedServerName;

    /**
     * Create instance of this class for the specified version string.
     * 
     * @param rawStr raw string that was received from the server.
     * 
     * @throws GDSServerVersionException if the specified raw string cannot 
     * be correctly parsed.
     */
    public GDSServerVersion(String rawStr) throws GDSServerVersionException {
        this.rawStr = rawStr;

        parseRawStr(rawStr);
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
     * Parse the raw string and store the components in appropriate places.
     * 
     * @param str string to parse.
     * 
     * @throws GDSServerVersionException if parsing cannot be completed.
     */
    private void parseRawStr(String str) throws GDSServerVersionException {
        int firstSpacePosition = str.indexOf(' ');

        if (firstSpacePosition == -1)
            throw new GDSServerVersionException("No server name is available.");

        this.serverName = str.substring(firstSpacePosition + 1);
        int commaPosition = serverName.indexOf(',');
        if (commaPosition != -1) {
            this.extendedServerName = this.serverName
                    .substring(commaPosition + 1);
            this.serverName = this.serverName.substring(0, commaPosition);
        }

        String platformVersionStr = str.substring(0, firstSpacePosition);

        int dashPosition = platformVersionStr.indexOf('-');

        if (dashPosition == -1)
            throw new GDSServerVersionException("No platform/version available.");

        this.platform = platformVersionStr.substring(0, dashPosition);
        this.type = platformVersionStr.substring(dashPosition + 1, dashPosition + 2);

        String versionStr = platformVersionStr.substring(dashPosition + 2);

        try {
            StringTokenizer st = new StringTokenizer(versionStr, ".");
            String majorVersionStr = st.nextToken();
            String minorVersionStr = st.nextToken();
            String variantStr = st.nextToken();
            String buildNumStr = st.nextToken();

            this.majorVersion = Integer.parseInt(majorVersionStr);
            this.minorVersion = Integer.parseInt(minorVersionStr);
            this.variant = Integer.parseInt(variantStr);
            this.buildNumber = Integer.parseInt(buildNumStr);

        } catch (NoSuchElementException ex) {
            throw new GDSServerVersionException(
                    "One of the version components not available: " + str);
        } catch (NumberFormatException ex) {
            throw new GDSServerVersionException(
                    "One of the version components not a number : " + str);
        }

    }
}
