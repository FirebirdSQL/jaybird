// SPDX-FileCopyrightText: Copyright 2013-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.firebirdsql.jaybird.props.DatabaseConnectionProperties;

/**
 * Connection properties for the Firebird connection.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface IConnectionProperties extends IAttachProperties<IConnectionProperties>, DatabaseConnectionProperties {

    /**
     * @return An immutable version of this instance as an implementation of {@link IConnectionProperties}
     */
    @Override
    IConnectionProperties asImmutable();

    /**
     * @return A new, mutable, instance as an implementation of {@link IConnectionProperties} with all properties
     * copied.
     */
    @Override
    IConnectionProperties asNewMutable();
}
