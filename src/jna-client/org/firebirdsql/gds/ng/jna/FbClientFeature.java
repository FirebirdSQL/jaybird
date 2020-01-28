/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.util.InternalApi;

import static java.util.Collections.singletonList;

/**
 * Firebird native client features.
 * <p>
 * This enum does not contain all possible features of the native client library, just those that Jaybird needs to
 * handle in some way if absent or present.
 * </p>
 *
 * @since 4.0
 */
@InternalApi
public enum FbClientFeature {
    
    STATEMENT_TIMEOUT("fb_dsql_set_timeout");

    // If a feature depends on multiple methods, consider changing this to a list or set
    private String featureMethod;

    FbClientFeature(String featureMethod) {
        this.featureMethod = featureMethod;
    }

    /**
     * @return Method names this feature requires.
     */
    Iterable<String> methodNames() {
        return singletonList(featureMethod);
    }
}
