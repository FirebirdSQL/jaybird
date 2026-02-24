// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2015 Hajime Nakagami
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.DatabaseParameterBufferImp;
import org.firebirdsql.gds.impl.DatabaseParameterBufferImp.DpbMetaData;
import org.firebirdsql.gds.impl.ServiceParameterBufferImp;
import org.firebirdsql.gds.impl.ServiceParameterBufferImp.SpbMetaData;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.def.ConnectionPropertyType;

import java.sql.SQLException;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import static org.firebirdsql.jaybird.fb.constants.DpbItems.isc_dpb_lc_ctype;
import static org.firebirdsql.jaybird.fb.constants.DpbItems.isc_dpb_session_time_zone;
import static org.firebirdsql.jaybird.fb.constants.SpbItems.isc_spb_connect_timeout;
import static org.firebirdsql.jaybird.fb.constants.SpbItems.isc_spb_sql_role_name;
import static org.firebirdsql.jaybird.props.PropertyConstants.SESSION_TIME_ZONE_SERVER;

/**
 * Abstract class for behavior common to {@code ParameterConverter} implementations.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractParameterConverter<D extends AbstractConnection<IConnectionProperties, ?>, S extends AbstractConnection<IServiceProperties, ?>>
        implements ParameterConverter<D, S> {

    protected DatabaseParameterBuffer createDatabaseParameterBuffer(final D connection) {
        return new DatabaseParameterBufferImp(DpbMetaData.DPB_VERSION_1, connection.getEncoding());
    }

    protected ServiceParameterBuffer createServiceParameterBuffer(final S connection) {
        return new ServiceParameterBufferImp(SpbMetaData.SPB_VERSION_2_ATTACH, connection.getEncoding());
    }

    @Override
    public final DatabaseParameterBuffer toDatabaseParameterBuffer(final D connection) throws SQLException {
        DatabaseParameterBuffer dpb = createDatabaseParameterBuffer(connection);

        // Map standard properties
        populateDefaultProperties(connection, dpb);

        return dpb;
    }

    /**
     * Populates the database parameter buffer with the standard Firebird properties explicitly supported through
     * {@code IConnectionProperties}.
     *
     * @param connection
     *         Database connection
     * @param dpb
     *         Database parameter buffer to populate
     * @throws SQLException
     *         For errors generating authentication information
     */
    protected void populateDefaultProperties(final D connection, final DatabaseParameterBuffer dpb)
            throws SQLException {
        dpb.addArgument(isc_dpb_lc_ctype, connection.getEncodingDefinition().getFirebirdEncodingName());
        IConnectionProperties props = connection.getAttachProperties();
        populateFromProperties(props, dpb, ConnectionProperty::hasDpbItem, ConnectionProperty::dpbItem);
        populateAuthenticationProperties(connection, dpb);
        String sessionTimeZone = props.getSessionTimeZone();
        if (sessionTimeZone != null && !SESSION_TIME_ZONE_SERVER.equalsIgnoreCase(sessionTimeZone)) {
            dpb.addArgument(isc_dpb_session_time_zone, sessionTimeZone);
        }
    }

    /**
     * Populates the authentication properties of the parameter buffer.
     *
     * @param connection
     *         Database connection
     * @param pb
     *         Parameter buffer to populate
     * @throws SQLException
     *         For errors generating authentication information
     */
    protected abstract void populateAuthenticationProperties(AbstractConnection<?, ?> connection,
            ConnectionParameterBuffer pb) throws SQLException;

    @Override
    public final ServiceParameterBuffer toServiceParameterBuffer(final S connection) throws SQLException {
        final ServiceParameterBuffer spb = createServiceParameterBuffer(connection);

        // Map standard properties
        populateDefaultProperties(connection, spb);

        return spb;
    }

    /**
     * Populates the database parameter buffer with the standard Firebird properties explicitly supported through
     * {@code IConnectionProperties}.
     *
     * @param connection
     *         Service connection
     * @param spb
     *         Service parameter buffer to populate
     * @throws SQLException
     *         For errors generating authentication information
     */
    protected void populateDefaultProperties(final S connection, final ServiceParameterBuffer spb) throws SQLException {
        populateAuthenticationProperties(connection, spb);
        IServiceProperties props = connection.getAttachProperties();
        populateFromProperties(props, spb, ConnectionProperty::hasSpbItem, ConnectionProperty::spbItem);
        if (props.getRoleName() != null) {
            spb.addArgument(isc_spb_sql_role_name, props.getRoleName());
        }
        if (props.getConnectTimeout() != IAttachProperties.DEFAULT_CONNECT_TIMEOUT) {
            spb.addArgument(isc_spb_connect_timeout, props.getConnectTimeout());
        }
    }

    private void populateFromProperties(IAttachProperties<?> props, ParameterBuffer pb,
            Predicate<ConnectionProperty> hasPbItem, ToIntFunction<ConnectionProperty> getPbItem) {
        Map<ConnectionProperty, Object> connectionProperties = props.connectionPropertyValues();
        for (Map.Entry<ConnectionProperty, Object> entry : connectionProperties.entrySet()) {
            ConnectionProperty property = entry.getKey();
            Object propertyValue = entry.getValue();
            //noinspection ConstantValue : null-check for robustness
            if (propertyValue == null || !hasPbItem.test(property)) continue;
            ConnectionPropertyType propertyType = property.type();
            int pbItem = getPbItem.applyAsInt(property);
            property.pbType().addValue(pb, pbItem, propertyValue, propertyType);
        }
    }

}
