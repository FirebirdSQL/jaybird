// SPDX-FileCopyrightText: Copyright 2013-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

/**
 * Immutable implementation of {@link org.firebirdsql.gds.ng.IConnectionProperties}.
 *
 * @author @author Mark Rotteveel
 * @see FbConnectionProperties
 * @since 3.0
 */
public final class FbImmutableConnectionProperties extends AbstractImmutableAttachProperties<IConnectionProperties>
        implements IConnectionProperties {

    /**
     * Copy constructor for FbConnectionProperties.
     * <p>
     * All properties defined in {@link org.firebirdsql.gds.ng.IConnectionProperties} are copied from {@code src} to
     * the new instance.
     * </p>
     *
     * @param src
     *         Source to copy from
     */
    public FbImmutableConnectionProperties(IConnectionProperties src) {
        super(src);
    }

    @Override
    public IConnectionProperties asImmutable() {
        // Immutable already, so just return this
        return this;
    }

    @Override
    public IConnectionProperties asNewMutable() {
        return new FbConnectionProperties(this);
    }

    @Override
    @SuppressWarnings("java:S1206")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FbImmutableConnectionProperties)) return false;
        return super.equals(o);
    }

}
