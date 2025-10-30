// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.gds.AbstractVersion;

import java.io.Serial;

/**
 * Value class representing a version with {@code major.minor} version information.
 *
 * @author Mark Rotteveel
 * @since 7
 */
public final class BasicVersion extends AbstractVersion {

    @Serial
    private static final long serialVersionUID = -2133746651860946034L;

    private BasicVersion(int major, int minor) {
        super(major, minor);
    }

    /**
     * @return a basic version object with {@code major} and minor {@code 0}
     */
    public static BasicVersion of(int major) {
        return of(major, 0);
    }

    /**
     * @return a basic version object with {@code major} and {@code minor}
     */
    public static BasicVersion of(int major, int minor) {
        return new BasicVersion(major, minor);
    }

    @Override
    public BasicVersion toBasicVersion() {
        return this;
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
     * Factory method to derive a basic version from {@code version}.
     *
     * @param version
     *         version
     * @return a {@code BasicVersion} with the same {@code major.minor}, or {@code version} if it is a
     * {@code BasicVersion} (identity operation)
     */
    public static BasicVersion of(AbstractVersion version) {
        return version instanceof BasicVersion bv ? bv : of(version.major(), version.minor());
    }

}
