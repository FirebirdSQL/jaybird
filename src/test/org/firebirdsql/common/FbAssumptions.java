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
package org.firebirdsql.common;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Common assumptions for tests.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class FbAssumptions {

    private FbAssumptions() {
        // no instances
    }

    public static void assumeServerBatchSupport() {
        assumeTrue(getDefaultSupportInfo().supportsServerBatch(), "test requires server-side batch support");
        assumeThat("Server-side batch support only works with pure java connections",
                FBTestProperties.GDS_TYPE, isPureJavaType());
    }
}
