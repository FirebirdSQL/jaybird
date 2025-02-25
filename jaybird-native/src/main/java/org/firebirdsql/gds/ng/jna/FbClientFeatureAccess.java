// SPDX-FileCopyrightText: Copyright 2019-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.util.InternalApi;

import java.util.Set;

/**
 * Firebird client feature access.
 * <p>
 * This interface allows checks for features of the Firebird client API.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
@InternalApi
public interface FbClientFeatureAccess {

    /**
     * Checks for presence of a client feature.
     *
     * @param clientFeature Client feature
     * @return {@code true} if the feature is present, {@code false} otherwise
     */
    boolean hasFeature(FbClientFeature clientFeature);

    /**
     * @return an unmodifiable set with supported client features
     */
    Set<FbClientFeature> getFeatures();

}
