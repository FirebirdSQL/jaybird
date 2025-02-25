// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.extension;

import org.firebirdsql.common.FeatureCheck;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;

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

}
