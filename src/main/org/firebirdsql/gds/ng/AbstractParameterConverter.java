/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.gds.ng;

import javax.xml.bind.DatatypeConverter;
import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.Parameter;
import org.firebirdsql.gds.ServiceParameterBuffer;

import static org.firebirdsql.gds.ISCConstants.*;

/**
 * Abstract class for behavior common to {@code ParameterConverter} implementations.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractParameterConverter {

    /**
     * Populates the database parameter buffer with the standard Firebird properties explicitly supported through
     * {@code IConnectionProperties}.
     *
     * @param props
     *         Properties
     * @param encodingFactory
     *         Encoding factory
     * @param dpb
     *         Database parameter buffer to populate
     * @param encoding
     *         Encoding to use for string properties
     */
    protected void populateDefaultProperties(final IConnectionProperties props, final IEncodingFactory encodingFactory,
            final DatabaseParameterBuffer dpb, final Encoding encoding) {
        dpb.addArgument(isc_dpb_lc_ctype,
                encodingFactory.getDefaultEncodingDefinition().getFirebirdEncodingName(), encoding);
        if (props.getPageCacheSize() != IConnectionProperties.DEFAULT_BUFFERS_NUMBER) {
            dpb.addArgument(isc_dpb_num_buffers, props.getPageCacheSize());
        }
        if (props.getUser() != null) {
            dpb.addArgument(isc_dpb_user_name, props.getUser(), encoding);
        }
        if (props.getPassword() != null && props.getAuthData() == null) {
            dpb.addArgument(isc_dpb_password, props.getPassword(), encoding);
        }
        if (props.getRoleName() != null) {
            dpb.addArgument(isc_dpb_sql_role_name, props.getRoleName(), encoding);
        }
        if (props.getAuthData() != null) {
            dpb.addArgument(isc_dpb_specific_auth_data, DatatypeConverter.printHexBinary(props.getAuthData()), encoding);
        }
        dpb.addArgument(isc_dpb_sql_dialect, props.getConnectionDialect());
        if (props.getConnectTimeout() != IConnectionProperties.DEFAULT_CONNECT_TIMEOUT) {
            dpb.addArgument(isc_dpb_connect_timeout, props.getConnectTimeout());
        }
    }

    /**
     * Populates the database parameter buffer with the non-standard properties (in
     * {@link org.firebirdsql.gds.ng.IConnectionProperties#getExtraDatabaseParameters()}).
     *
     * @param props
     *         Properties
     * @param dpb
     *         Database parameter buffer to populate
     * @param encoding
     *         Encoding to use for string properties
     */
    protected void populateNonStandardProperties(final IConnectionProperties props, final DatabaseParameterBuffer dpb,
            final Encoding encoding) {
        for (Parameter parameter : props.getExtraDatabaseParameters()) {
            parameter.copyTo(dpb, encoding);
        }
    }

    /**
     * Populates the database parameter buffer with the standard Firebird properties explicitly supported through
     * {@code IConnectionProperties}.
     *
     * @param props
     *         Properties
     * @param encodingFactory
     *         Encoding factory
     * @param spb
     *         Service parameter buffer to populate
     * @param encoding
     *         Encoding to use for string properties
     */
    protected void populateDefaultProperties(final IServiceProperties props, final IEncodingFactory encodingFactory,
            final ServiceParameterBuffer spb, final Encoding encoding) {
        // TODO Is there an equivalent to set connection character set for a service
//        dpb.addArgument(isc_dpb_lc_ctype,
//                encodingFactory.getDefaultEncodingDefinition().getFirebirdEncodingName(), encoding);
        if (props.getUser() != null) {
            spb.addArgument(isc_spb_user_name, props.getUser(), encoding);
        }
        if (props.getPassword() != null && props.getAuthData() == null) {
            spb.addArgument(isc_spb_password, props.getPassword(), encoding);
        }
        if (props.getRoleName() != null) {
            spb.addArgument(isc_spb_sql_role_name, props.getRoleName(), encoding);
        }
        if (props.getAuthData() != null) {
            spb.addArgument(isc_spb_specific_auth_data, DatatypeConverter.printHexBinary(props.getAuthData()), encoding);
        }
        if (props.getConnectTimeout() != IConnectionProperties.DEFAULT_CONNECT_TIMEOUT) {
            spb.addArgument(isc_spb_connect_timeout, props.getConnectTimeout());
        }
    }
}
