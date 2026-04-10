// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.extension;

import org.firebirdsql.common.FBTestProperties;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.EnumSet;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.hasMappedDatabaseDirectory;
import static org.firebirdsql.common.FBTestProperties.isEventPortAvailable;
import static org.firebirdsql.common.FBTestProperties.isSameHostServer;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Simple (and naive) extension to check for certain environment requirements.
 *
 * @author Mark Rotteveel
 */
@NullMarked
public class RunEnvironmentExtension implements BeforeAllCallback {

    private final List<EnvironmentRequirement> unmetRequirements;

    private RunEnvironmentExtension(EnumSet<EnvironmentRequirement> environmentRequirements) {
        this.unmetRequirements = environmentRequirements.stream().filter(EnvironmentRequirement::isNotMet).toList();
    }

    @SuppressWarnings("unused")
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

        /**
         * Requires that Firebird accesses files directly on the filesystem of this host.
         * <p>
         * Use of {@link #requiresDbLocallyMapped()} is preferred whenever possible.
         * </p>
         *
         * @return this builder
         */
        @SuppressWarnings("unused")
        public Builder requiresDbOnLocalFileSystem() {
            aspects.add(EnvironmentRequirement.DB_LOCAL_FS);
            return this;
        }

        /**
         * Requires that tests can access files of the Firebird server in a directory (mounted or directly) on the
         * local filesystem.
         *
         * @return this builder
         */
        public Builder requiresDbLocallyMapped() {
            aspects.add(EnvironmentRequirement.DB_LOCALLY_MAPPED);
            return this;
        }

        /**
         * Requires that the event port is available.
         * <p>
         * Currently this requirement is considered not met if
         * </p>
         * <ol>
         * <li>Property {@code test.event.available} is {@code false}</li>
         * <li>Property {@code test.dbondocker} is {@code true} and {@code test.event.available} is not set, or is
         * not {@code true}</li>
         * </ol>
         *
         * @return this builder
         */
        public Builder requiresEventPortAvailable() {
            aspects.add(EnvironmentRequirement.EVENT_PORT_AVAILABLE);
            return this;
        }

        public RunEnvironmentExtension build() {
            return new RunEnvironmentExtension(EnumSet.copyOf(aspects));
        }
    }

    public enum EnvironmentRequirement {
        /**
         * Firebird server has direct local filesystem access.
         *
         * @see FBTestProperties#isSameHostServer()
         */
        DB_LOCAL_FS {
            @Override
            public boolean isMet() {
                return isSameHostServer();
            }
        },
        /**
         * Database files are, if placed in {@code test.db.dir}, available on the local filesystem.
         *
         * @see FBTestProperties#hasMappedDatabaseDirectory()
         */
        DB_LOCALLY_MAPPED {
            @Override
            public boolean isMet() {
                return hasMappedDatabaseDirectory();
            }
        },
        /**
         * The events port is available.
         *
         * @see FBTestProperties#isEventPortAvailable()
         */
        EVENT_PORT_AVAILABLE {
            @Override
            public boolean isMet() {
                return isEventPortAvailable();
            }
        },
        ALL_SRP_PLUGINS {
            @Override
            public boolean isMet() {
                // NOTE: This requirement exists for historic reasons; configuring all plugins on
                // the jacobalberty/firebird image was hard to do (especially using juarezr/firebirdsql-github-action)
                return true;
            }
        }
        ;

        public boolean isNotMet() {
            return !isMet();
        }

        public abstract boolean isMet();
    }

}
