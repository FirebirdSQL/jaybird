// SPDX-FileCopyrightText: Copyright 2014-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ServiceParameterBuffer;

import java.sql.SQLException;

/**
 * Provides conversion of parameters (e.g. from {@link org.firebirdsql.gds.ng.IConnectionProperties} to a
 * {@link org.firebirdsql.gds.DatabaseParameterBuffer}).
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface ParameterConverter<D extends AbstractConnection<IConnectionProperties, ?>, S extends AbstractConnection<IServiceProperties, ?>> {

    /**
     * Builds a {@code DatabaseParameterBuffer} from the supplied {code IConnectionProperties}.
     *
     * @param connection
     *         Database connection
     * @return Database parameter buffer populated based on the connection properties
     * @throws SQLException For errors
     */
    DatabaseParameterBuffer toDatabaseParameterBuffer(D connection) throws SQLException;

    /**
     * Builds a {@code ServiceParameterBuffer} from the supplied {code IServiceProperties}.
     *
     * @param connection
     *         Service connection
     * @return Service parameter buffer populated based on the service properties
     * @throws SQLException For errors
     */
    ServiceParameterBuffer toServiceParameterBuffer(S connection) throws SQLException;
}
