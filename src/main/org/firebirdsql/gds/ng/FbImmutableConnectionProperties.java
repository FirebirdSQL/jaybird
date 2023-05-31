/*
 * Firebird Open Source JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FbImmutableConnectionProperties)) return false;
        return super.equals(o);
    }

}
