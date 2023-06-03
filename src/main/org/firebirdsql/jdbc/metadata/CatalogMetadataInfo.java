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
package org.firebirdsql.jdbc.metadata;

/**
 * Metadata information related to catalogs.
 * <p>
 * Specifically, this reports the normal behaviour (no catalogs), or &mdash; Firebird 3.0 or higher with
 * {@code useCatalogAsPackage=true} &mdash; catalogs being used for packages.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 6
 */
public sealed class CatalogMetadataInfo {

    final DbMetadataMediator mediator;

    private CatalogMetadataInfo(DbMetadataMediator mediator) {
        this.mediator = mediator;
    }

    public static CatalogMetadataInfo create(DbMetadataMediator mediator) {
        if (mediator.isUseCatalogAsPackage()) {
            return CatalogAsPackageMetadataInfo.createInstance(mediator);
        } else {
            return new CatalogMetadataInfo(mediator);
        }
    }

    // NOTE: Methods report values for default (no catalog support), these should be overridden as needed in subclasses

    public String getCatalogSeparator() {
        // not supported, so need to report null as separator
        return null;
    }

    public String getCatalogTerm() {
        // not supported, so need to report null as term
        return null;
    }

    public boolean isCatalogAtStart() {
        return false;
    }

    public int getMaxCatalogNameLength() {
        // not supported, so reporting length 0
        return 0;
    }

    public boolean supportsCatalogsInDataManipulation() {
        return false;
    }

    public boolean supportsCatalogsInProcedureCalls() {
        return false;
    }

    public boolean supportsCatalogsInTableDefinitions() {
        return false;
    }

    public boolean supportsCatalogsInIndexDefinitions() {
        return false;
    }

    public boolean supportsCatalogsInPrivilegeDefinitions() {
        return false;
    }

    /**
     * Catalog metadata if catalogs are used to report package information.
     */
    private static final class CatalogAsPackageMetadataInfo extends CatalogMetadataInfo {

        private CatalogAsPackageMetadataInfo(DbMetadataMediator mediator) {
            super(mediator);
        }

        static CatalogMetadataInfo createInstance(DbMetadataMediator mediator) {
            return new CatalogAsPackageMetadataInfo(mediator);
        }

        @Override
        public String getCatalogSeparator() {
            return ".";
        }

        @Override
        public String getCatalogTerm() {
            return "PACKAGE";
        }

        @Override
        public boolean isCatalogAtStart() {
            return true;
        }

        @Override
        public int getMaxCatalogNameLength() {
            return mediator.getMetaData().getMaxObjectNameLength();
        }

        @Override
        public boolean supportsCatalogsInDataManipulation() {
            return true;
        }

        @Override
        public boolean supportsCatalogsInProcedureCalls() {
            return true;
        }

    }

}
