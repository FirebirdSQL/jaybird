// SPDX-FileCopyrightText: Copyright 2015-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
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
     * @since 7
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
