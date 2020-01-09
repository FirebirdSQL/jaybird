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

import java.util.Set;

/**
 * Firebird client feature access.
 * <p>
 * This interface allows checks for features of the Firebird client API.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
