// SPDX-FileCopyrightText: Copyright 2015-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

/**
 * Immutable implementation of {@link org.firebirdsql.gds.ng.IServiceProperties}.
 *
 * @author @author Mark Rotteveel
 * @see FbServiceProperties
 * @since 3.0
 */
public final class FbImmutableServiceProperties extends AbstractImmutableAttachProperties<IServiceProperties>
        implements IServiceProperties {

    /**
     * Copy constructor for FbServiceProperties.
     * <p>
     * All properties defined in {@link IServiceProperties} are  copied from <code>src</code> to the new instance.
     * </p>
     *
     * @param src
     *         Source to copy from
     */
    public FbImmutableServiceProperties(IServiceProperties src) {
        super(src);
    }

    @Override
    public IServiceProperties asImmutable() {
        // Immutable already, so just return this
        return this;
    }

    @Override
    public IServiceProperties asNewMutable() {
        return new FbServiceProperties(this);
    }

    @Override
    @SuppressWarnings("java:S1206")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FbImmutableServiceProperties)) return false;
        return super.equals(o);
    }

}
