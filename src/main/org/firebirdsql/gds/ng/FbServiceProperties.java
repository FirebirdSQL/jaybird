// SPDX-FileCopyrightText: Copyright 2015-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.jspecify.annotations.Nullable;

/**
 * Mutable implementation of {@link IServiceProperties}.
 *
 * @author Mark Rotteveel
 * @see FbImmutableServiceProperties
 * @since 3.0
 */
public final class FbServiceProperties extends AbstractAttachProperties<IServiceProperties>
        implements IServiceProperties {

    private @Nullable FbImmutableServiceProperties immutableServicePropertiesCache;

    /**
     * Copy constructor for FbServiceProperties.
     * <p>
     * All properties defined in {@link IServiceProperties} are copied from {@code src} to the new instance.
     * </p>
     *
     * @param src
     *         Source to copy from
     */
    public FbServiceProperties(IServiceProperties src) {
        super(src);
    }

    /**
     * Default constructor for FbServiceProperties
     */
    public FbServiceProperties() {
    }

    @Override
    public IServiceProperties asImmutable() {
        if (immutableServicePropertiesCache == null) {
            immutableServicePropertiesCache = new FbImmutableServiceProperties(this);
        }
        return immutableServicePropertiesCache;
    }

    @Override
    public IServiceProperties asNewMutable() {
        return new FbServiceProperties(this);
    }

    @Override
    @SuppressWarnings("java:S1206")
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof FbServiceProperties)) return false;
        return super.equals(o);
    }

    @Override
    protected void dirtied() {
        immutableServicePropertiesCache = null;
    }
}
