// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.jaybird.props.PropertyConstants;

import java.util.stream.Stream;

/**
 * Method sources for parameterized tests of implementation variants.
 */
public final class ImplementationVariantSources {

    private ImplementationVariantSources() {
        // no instances
    }

    public static Stream<String> callableImplementations() {
        return Stream.of(PropertyConstants.CALLABLE_IMPLEMENTATION_V1/*, PropertyConstants.CALLABLE_IMPLEMENTATION_V2*/);
    }

}
