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

import org.firebirdsql.util.FirebirdSupportInfo;
import org.opentest4j.TestAbortedException;

import java.util.function.Predicate;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Common assumptions for tests.
 * <p>
 * Feature-based assumptions use {@link FBTestProperties#getDefaultSupportInfo()}.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class FbAssumptions {

    private FbAssumptions() {
        // no instances
    }

    public static void assumeServerBatchSupport() {
        assumeFeature(FirebirdSupportInfo::supportsServerBatch, "test requires server-side batch support");
        assumeThat("Server-side batch support only works with pure java connections",
                FBTestProperties.GDS_TYPE, isPureJavaType());
    }

    /**
     * Assume {@code featureTest} returns {@code true}, and fail otherwise.
     *
     * @param featureTest
     *         predicate to test against {@link FirebirdSupportInfo}
     * @param message
     *         message on failure
     * @throws TestAbortedException
     *         if the predicate returns {@code false}
     */
    public static void assumeFeature(Predicate<FirebirdSupportInfo> featureTest, String message) {
        assumeTrue(featureTest.test(getDefaultSupportInfo()), message);
    }

    /**
     * Assume {@code featureCheck} has a positive result, and fail otherwise.
     *
     * @param featureCheck
     *         feature check
     * @throws TestAbortedException
     *         if the feature check is negative
     */
    public static void assumeFeature(FeatureCheck featureCheck) {
        featureCheck.accept(getDefaultSupportInfo());
    }

    /**
     * Assume {@code featureTest} returns {@code false}, and fail otherwise.
     *
     * @param featureTest
     *         predicate to test against {@link FirebirdSupportInfo}
     * @param message
     *         message on failure
     * @throws TestAbortedException
     *         if the predicate returns {@code true}
     */
    public static void assumeFeatureMissing(Predicate<FirebirdSupportInfo> featureTest, String message) {
        assumeFalse(featureTest.test(getDefaultSupportInfo()), message);
    }

    /**
     * Assume {@code featureCheck} has a negative result, and fail otherwise.
     * <p>
     * NOTE: The feature check is negated with {@link FeatureCheck#negate()}, this may result in awkward messages.
     * </p>
     *
     * @param featureCheck
     *         feature check
     * @throws TestAbortedException
     *         if the feature check is positive
     */
    public static void assumeFeatureMissing(FeatureCheck featureCheck) {
        assumeFeature(featureCheck.negate());
    }

}
