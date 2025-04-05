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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.GDSServerVersionException;
import org.firebirdsql.gds.ng.FbAttachment;

import java.util.List;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface JnaAttachment extends FbAttachment {

    /**
     * Reports the client library version of this attachment.
     * <p>
     * The default implementation extracts the last raw version string of {@link #getServerVersion()} and parses that.
     * </p>
     *
     * @return client version, may report {@link GDSServerVersion#INVALID_VERSION} if the implementation can't determine
     * the client version or if parsing fails.
     * @since 6.0.2
     */
    default GDSServerVersion getClientVersion() {
        GDSServerVersion serverVersion = getServerVersion();
        List<String> rawVersions = serverVersion.getRawVersions();
        if (rawVersions.isEmpty()) {
            return GDSServerVersion.INVALID_VERSION;
        }
        try {
            return GDSServerVersion.parseRawVersion(rawVersions.get(rawVersions.size() - 1));
        } catch (GDSServerVersionException e) {
            return GDSServerVersion.INVALID_VERSION;
        }
    }

}
