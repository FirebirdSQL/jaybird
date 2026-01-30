// SPDX-FileCopyrightText: Copyright 2015-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.firebirdsql.jaybird.props.ServiceConnectionProperties;
import org.jspecify.annotations.NullMarked;

/**
 * Connection properties for a Firebird service attachment.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@NullMarked
public interface IServiceProperties extends IAttachProperties<IServiceProperties>, ServiceConnectionProperties {

    /**
     * @return An immutable version of this instance as an implementation of {@link IServiceProperties}
     */
    @Override
    IServiceProperties asImmutable();

    /**
     * @return A new, mutable, instance as an implementation of {@link IServiceProperties} with all properties copied.
     */
    @Override
    IServiceProperties asNewMutable();
}
