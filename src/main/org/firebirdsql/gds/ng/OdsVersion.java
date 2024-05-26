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
package org.firebirdsql.gds.ng;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Value class representing the Firebird On-Disk Structure (ODS) version.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public final class OdsVersion {

    private static final Map<Integer, OdsVersion> ODS_VERSION_CACHE = new ConcurrentHashMap<>();

    private final int major;
    private final int minor;

    private OdsVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
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
     * @return ODS major version
     */
    public int major() {
        return major;
    }

    /**
     * @return ODS minor version
     */
    public int minor() {
        return minor;
    }

    /**
     * Returns a - possibly cached - instance with the specified major version and the minor version of this instance.
     *
     * @param major
     *         ODS major version
     * @return instance with value of parameter {@code major} and {@link #minor()} of this instance
     */
    public OdsVersion withMajor(int major) {
        return this.major != major ? of(major, minor) : this;
    }

    /**
     * Returns a - possibly cached - instance with the major version of this instance and the specified minor version.
     *
     * @param minor
     *         ODS minor version
     * @return instance with {@link #major()} of this instance and value of parameter {@code minor}
     */
    public OdsVersion withMinor(int minor) {
        return this.minor != minor ? of(major, minor) : this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof OdsVersion that
               && this.major == that.major
               && this.minor == that.minor;
    }

    @Override
    public int hashCode() {
        return key(major, minor);
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }

}
