// SPDX-FileCopyrightText: Copyright 2021-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.extension;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.EnumSet;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static org.firebirdsql.common.FBTestProperties.DB_ON_DOCKER;
import static org.firebirdsql.common.FBTestProperties.DB_SERVER_URL;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Simple (and naive) extension to check for certain environment requirements.
 *
 * @author Mark Rotteveel
 */
public class RunEnvironmentExtension implements BeforeAllCallback {

    private final List<EnvironmentRequirement> unmetRequirements;

    private RunEnvironmentExtension(EnumSet<EnvironmentRequirement> environmentRequirements) {
        this.unmetRequirements = environmentRequirements.stream().filter(EnvironmentRequirement::isNotMet).toList();
    }

    public List<EnvironmentRequirement> getUnmetRequirements() {
        return unmetRequirements;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        assumeTrue(unmetRequirements.isEmpty(), () -> "The following requirements were not met: " + unmetRequirements);
    }

    public static class Builder {

        private final EnumSet<EnvironmentRequirement> aspects = EnumSet.noneOf(EnvironmentRequirement.class);

        public Builder requiresDbOnLocalFileSystem() {
            aspects.add(EnvironmentRequirement.DB_LOCAL_FS);
            return this;
        }

        public Builder requiresEventPortAvailable() {
            aspects.add(EnvironmentRequirement.EVENT_PORT_AVAILABLE);
            return this;
        }

        public RunEnvironmentExtension build() {
            return new RunEnvironmentExtension(EnumSet.copyOf(aspects));
        }
    }

    public enum EnvironmentRequirement {
        DB_LOCAL_FS {
            @Override
            public boolean isMet() {
                if ("localhost".equals(DB_SERVER_URL) || "127.0.0.1".equals(DB_SERVER_URL)) {
                    return !DB_ON_DOCKER;
                }
                return false;
            }
        },
        EVENT_PORT_AVAILABLE {
            @Override
            public boolean isMet() {
                // NOTE: We're assuming that the AUX (event) port is (only) not available on Docker.
                // Otherwise, we need to check if we can actually establish an event connection.
                return !DB_ON_DOCKER;
            }
        },
        ALL_SRP_PLUGINS {
            @Override
            public boolean isMet() {
                // Enabling all SRP plugins is messy in the jacobalberty/firebird Docker image,
                // so assume it is not available
                return !DB_ON_DOCKER;
            }
        }
        ;

        public boolean isNotMet() {
            return !isMet();
        }

        public abstract boolean isMet();
    }

}
