// SPDX-FileCopyrightText: Copyright 2024-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.AbstractVersion;

import java.io.Serial;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Value class representing the Firebird On-Disk Structure (ODS) version.
 * <p>
 * Implementation limit: {@code major} and {@code minor} must be between 0 and 0xFFFF (65535).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 6
 */
public final class OdsVersion extends AbstractVersion {

    @Serial
    private static final long serialVersionUID = 4152662579163138758L;

    private static final Map<Integer, OdsVersion> ODS_VERSION_CACHE = new ConcurrentHashMap<>();

    private OdsVersion(int major, int minor) {
        super(major, minor);
    }

    /**
     * Returns a - possibly cached - instance with the specified major and minor version.
     *
     * @param major
     *         ODS major version
     * @param minor
     *         ODS minor version
     * @return ODS version instance
     */
    public static OdsVersion of(int major, int minor) {
        if ((major & 0xFFFF) != major || (minor & 0xFFFF) != minor) {
            throw new IllegalArgumentException(
                    "Implementation limit for major or minor exceeded (must be between 0 and 65535)");
        }
        return ODS_VERSION_CACHE.computeIfAbsent(key(major, minor), ignored -> new OdsVersion(major, minor));
    }

    private static int key(int major, int minor) {
        /* major and minor are both 16 bits, and in practice, relatively small values occur (e.g. Firebird 5 is ODS
           13.1). Striping them will ensure that in most cases a value with less than 7 bits set will be produced, which
           will allow the cache key to be an Integer from the Integer cache (for major <= 31 and minor <= 3) */
        return (major & 0x1F) | ((minor & 0xFFFF) << 5) | ((major & 0xFFE0) << 16);
    }

    /**
     * @return an ODS version object with major {@code 0} and minor {@code 0}
     */
    public static OdsVersion none() {
        return of(0, 0);
    }

    /**
     * Returns a - possibly cached - instance with the specified major version and the minor version of this instance.
     *
     * @param major
     *         ODS major version
     * @return instance with value of parameter {@code major} and {@link #minor()} of this instance
     */
    public OdsVersion withMajor(int major) {
        return major() != major ? of(major, minor()) : this;
    }

    /**
     * Returns a - possibly cached - instance with the major version of this instance and the specified minor version.
     *
     * @param minor
     *         ODS minor version
     * @return instance with {@link #major()} of this instance and value of parameter {@code minor}
     */
    public OdsVersion withMinor(int minor) {
        return minor() != minor ? of(major(), minor) : this;
    }

    @Serial
    private Object readResolve() {
        // Return cached variant
        return of(major(), minor());
    }

}
