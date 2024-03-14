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

import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Check for presence of a feature.
 */
public final class FeatureCheck implements Consumer<FirebirdSupportInfo> {

    private final Predicate<FirebirdSupportInfo> featureTest;
    private final String failureMessage;

    /**
     * Creates a feature check.
     *
     * @param featureTest
     *         predicate that returns {@code true} when the feature is supported
     * @param failureMessage
     *         message to report when the feature is not supported
     */
    public FeatureCheck(Predicate<FirebirdSupportInfo> featureTest, String failureMessage) {
        this.featureTest = featureTest;
        this.failureMessage = failureMessage;
    }

    /**
     * Tests if the feature is available, otherwise throws a {@link org.opentest4j.TestAbortedException}.
     *
     * @param firebirdSupportInfo
     *         support info object
     */
    @Override
    public void accept(FirebirdSupportInfo firebirdSupportInfo) {
        assumeTrue(featureTest.test(firebirdSupportInfo), failureMessage);
    }

    /**
     * Negates the current feature test, and prefixes its failure message with {@code "should not have: "}/
     *
     * @return negated feature check
     * @since 6
     */
    public FeatureCheck negate() {
        return new FeatureCheck(featureTest.negate(), "should not have: " + failureMessage);
    }

}
