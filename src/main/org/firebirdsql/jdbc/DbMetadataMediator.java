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
package org.firebirdsql.jdbc;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.firebirdsql.util.InternalApi;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Provides access to the database connection for executing database metadata queries.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
@InternalApi
public abstract class DbMetadataMediator {

    private static final DatatypeCoder datatypeCoder =
            DefaultDatatypeCoder.forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));

    public static RowDescriptorBuilder newRowDescriptorBuilder(int size) {
        return new RowDescriptorBuilder(size, datatypeCoder);
    }

    /**
     * Provides the {@link FirebirdSupportInfo} for the connection of the associated database metadata.
     *
     * @return Firebird support info object.
     */
    public abstract FirebirdSupportInfo getFirebirdSupportInfo();

    /**
     * Executes the provided metadata query on the connection of the associated database metadata.
     *
     * @param metadataQuery
     *         Result set producing query to execute
     * @return Result set of the query
     * @throws SQLException
     *         For failures to execute the query
     */
    public abstract ResultSet performMetaDataQuery(MetadataQuery metadataQuery) throws SQLException;

    /**
     * A {@code FBDatabaseMetaData} instance of the connection that created this mediator.
     *
     * @return metadata instance
     */
    public abstract FBDatabaseMetaData getMetaData();

    /**
     * @return GDSType of the current connection
     */
    public abstract GDSType getGDSType();

    /**
     * @return value of the {@code useCatalogAsPackage} connection property if packages are supported, otherwise
     * {@code false}
     * @since 6
     */
    public abstract boolean isUseCatalogAsPackage();

    /**
     * @return the client info properties to report for {@link FBDatabaseMetaData#getClientInfoProperties()}.
     * @since 6
     */
    public abstract Collection<String> getClientInfoPropertyNames();

    /**
     * Holder class for query text and parameters.
     */
    @InternalApi
    public static class MetadataQuery {

        private final String queryText;
        private final List<String> parameters;
        private final boolean standalone;

        /**
         * Creates a query whose prepared form may be cached.
         *
         * @param queryText
         *         Query text
         * @param parameters
         *         Parameters for query (possibly empty)
         */
        public MetadataQuery(String queryText, List<String> parameters) {
            this(queryText, parameters, false);
        }

        /**
         * Creates a query.
         *
         * @param queryText
         *         Query text
         * @param parameters
         *         Parameters for query (possibly empty)
         * @param standalone
         *         {@code true} if the query to be executed is a standalone query (should not be cached and be closed
         *         asap)
         */
        public MetadataQuery(String queryText, List<String> parameters, boolean standalone) {
            this.queryText = requireNonNull(queryText, "queryText");
            this.parameters = requireNonNull(parameters, "parameters");
            this.standalone = standalone;
        }

        public String getQueryText() {
            return queryText;
        }

        public List<String> getParameters() {
            return parameters;
        }

        /**
         * @return {@code false} if the query can be reused and can be cached, {@code false} if the query should not be
         * reused and should be closed as soon as possible
         */
        public boolean isStandalone() {
            return standalone;
        }
    }
}
