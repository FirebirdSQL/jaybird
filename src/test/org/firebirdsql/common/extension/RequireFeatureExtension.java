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
package org.firebirdsql.common.extension;

import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Extension to check features.
 *
 * @author Mark Rotteveel
 */
public class RequireFeatureExtension implements BeforeAllCallback {

    private final List<Consumer<FirebirdSupportInfo>> featureChecks;

    private RequireFeatureExtension(List<Consumer<FirebirdSupportInfo>> featureChecks) {
        this.featureChecks = featureChecks;
    }

    public static Builder withFeatureCheck(Predicate<FirebirdSupportInfo> featureTest, String failureMessage) {
        return new Builder().withFeatureCheck(featureTest, failureMessage);
    }

    @SuppressWarnings("unused")
    public static Builder withFeatureCheck(Consumer<FirebirdSupportInfo> featureCheck) {
        return new Builder().withFeatureCheck(featureCheck);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        featureChecks.forEach(featureCheck -> featureCheck.accept(supportInfo));
    }

    public static final class Builder {

        private final List<Consumer<FirebirdSupportInfo>> featureChecks = new ArrayList<>();

        public Builder withFeatureCheck(Predicate<FirebirdSupportInfo> featureTest, String failureMessage) {
            featureChecks.add(new FeatureCheck(featureTest, failureMessage));
            return this;
        }

        public Builder withFeatureCheck(Consumer<FirebirdSupportInfo> featureCheck) {
            featureChecks.add(featureCheck);
            return this;
        }

        public RequireFeatureExtension build() {
            return new RequireFeatureExtension(new ArrayList<>(featureChecks));
        }

    }

    /**
     * Check for presence of a feature.
     */
    public static final class FeatureCheck implements Consumer<FirebirdSupportInfo> {

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
    }

}
