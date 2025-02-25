// SPDX-FileCopyrightText: Copyright 2019-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
    
    STATEMENT_TIMEOUT("fb_dsql_set_timeout"),
    // Can be used to identify Firebird 3.0 or higher fbclient
    FB_PING("fb_ping"),
    FB_DISCONNECT_TRANSACTION("fb_disconnect_transaction");

    // If a feature depends on multiple methods, consider changing this to a list or set
    private final String featureMethod;

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
