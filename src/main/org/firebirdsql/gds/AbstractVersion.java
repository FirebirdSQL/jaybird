// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds;

import org.firebirdsql.jaybird.util.BasicVersion;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.io.Serializable;

/**
 * Abstract version for {@code major.minor} version information.
 *
 * @author Mark Rotteveel
 * @since 7
 */
public abstract class AbstractVersion implements Comparable<AbstractVersion>, Serializable {

    @Serial
    private static final long serialVersionUID = 909074721396393952L;

    private final int major;
    private final int minor;

    protected AbstractVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    /**
     * @return major version
     */
    public final int major() {
        return major;
    }

    /**
     * @return minor version
     */
    public final int minor() {
        return minor;
    }

    /**
     * Convenience method to check if the <em>major</em> of this version is equal to or larger than the specified
     * required version.
     *
     * @param requiredMajorVersion
     *         required major version
     * @return {@code true} when current major is equal to or larger than required
     */
    public final boolean isEqualOrAbove(int requiredMajorVersion) {
        return major >= requiredMajorVersion;
    }

    /**
     * Convenience method to check if the <em>major.minor</em> of this version is equal to or larger than the specified
     * required version.
     *
     * @param requiredMajorVersion
     *         required major version
     * @param requiredMinorVersion
     *         required minor version
     * @return {@code true} when current major is larger than required, or major is same and minor is equal to or
     * larger than required
     */
    public final boolean isEqualOrAbove(int requiredMajorVersion, int requiredMinorVersion) {
        return major > requiredMajorVersion
                || (major == requiredMajorVersion && minor >= requiredMinorVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AbstractVersion that = (AbstractVersion) o;
        return major == that.major && minor == that.minor;
    }

    @Override
    public int hashCode() {
        return 31 * major + minor;
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }

    /**
     * @return a - possibly cached - basic version ({@code major.minor} only) from the major and minor of this object
     */
    public BasicVersion toBasicVersion() {
        return BasicVersion.of(this);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation compares major and minor; subclasses with more version fields may compare those
     * additional fields for instances of their own type and its subclasses. This can result in an unstable order, but
     * we accept that as we expect only to compare two versions of potentially differing types, or collections of
     * versions of the same type.
     * </p>
     * <p>
     * If a stable order is required, use a custom comparator that only compares major and minor.
     * </p>
     */
    @Override
    public int compareTo(@NonNull AbstractVersion other) {
        int majorDiff = Integer.compare(this.major, other.major);
        if (majorDiff != 0) return majorDiff;
        return Integer.compare(this.minor, other.minor);
    }

}
