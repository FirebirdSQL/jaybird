// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.gds.AbstractVersion;

import java.io.Serial;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Value class representing a version with {@code major.minor} version information.
 * <p>
 * Implementation limit: {@code major} and {@code minor} must be between 0 and 0xFFFF (65535).
 * </p>
 *
 * @since 7
 */
public final class BasicVersion extends AbstractVersion {

    @Serial
    private static final long serialVersionUID = -2133746651860946034L;

    private static final Map<Integer, BasicVersion> VERSION_CACHE = new ConcurrentHashMap<>();

    private BasicVersion(int major, int minor) {
        super(major, minor);
    }

    /**
     * @return a - possibly cached - basic version object with {@code major} and {@code minor}
     */
    public static BasicVersion of(int major, int minor) {
        if ((major & 0xFFFF) != major || (minor & 0xFFFF) != minor) {
            throw new IllegalArgumentException("Implementation limit for major or minor exceeded");
        }
        return VERSION_CACHE.computeIfAbsent(key(major, minor), ignored -> new BasicVersion(major, minor));
    }

    @Override
    public BasicVersion toBasicVersion() {
        return this;
    }

    /**
     * Returns a - possibly cached - instance with the specified major version and the minor version of this instance.
     *
     * @param major
     *         major version
     * @return instance with value of parameter {@code major} and {@link #minor()} of this instance
     */
    public BasicVersion withMajor(int major) {
        return major() != major ? of(major, minor()) : this;
    }

    /**
     * Returns a - possibly cached - instance with the major version of this instance and the specified minor version.
     *
     * @param minor
     *         minor version
     * @return instance with {@link #major()} of this instance and value of parameter {@code minor}
     */
    public BasicVersion withMinor(int minor) {
        return minor() != minor ? of(major(), minor) : this;
    }

    /**
     * @return a - possibly cached - basic version object with major {@code 0} and minor {@code 0}
     */
    public static BasicVersion none() {
        return of(0, 0);
    }

    /**
     * @return a - possibly cached - basic version object with {@code major} and minor {@code 0}
     */
    public static BasicVersion of(int major) {
        return of(major, 0);
    }

    private static int key(int major, int minor) {
        /* In practice, relatively small values occur. Striping them will ensure that in most cases a value with less
           than 7 bits set will be produced, which will allow the cache key to be an Integer from the Integer cache (for
           major <= 31 and minor <= 3) */
        return (major & 0x1F) | ((minor & 0xFFFF) << 5) | ((major & 0xFFE0) << 16);
    }

    /**
     * Identity factory method.
     *
     * @param version
     *         version
     * @return {@code version} (identity operation)
     */
    public static BasicVersion of(BasicVersion version) {
        return version;
    }

    /**
     * Factory method to derive a - possibly cached - basic version from {@code version}.
     *
     * @param version
     *         version
     * @return a {@code BasicVersion} with the same {@code major.minor}, or {@code version} if it is a
     * {@code BasicVersion} (identity operation)
     */
    public static BasicVersion of(AbstractVersion version) {
        return version instanceof BasicVersion bv ? bv : of(version.major(), version.minor());
    }

    @Override
    public int hashCode() {
        return key(major(), minor());
    }

    @Serial
    private Object readResolve() {
        // Return cached variant
        return of(major(), minor());
    }

}
