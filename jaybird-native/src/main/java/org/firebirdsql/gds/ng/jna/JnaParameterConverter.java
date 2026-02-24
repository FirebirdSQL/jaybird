// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ConnectionParameterBuffer;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ParameterTagMapping;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.impl.DatabaseParameterBufferImp;
import org.firebirdsql.gds.impl.DatabaseParameterBufferImp.DpbMetaData;
import org.firebirdsql.gds.impl.ServiceParameterBufferImp;
import org.firebirdsql.gds.impl.ServiceParameterBufferImp.SpbMetaData;
import org.firebirdsql.gds.ng.AbstractConnection;
import org.firebirdsql.gds.ng.AbstractParameterConverter;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.jaybird.fb.constants.DpbItems;
import org.firebirdsql.jaybird.fb.constants.SpbItems;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.firebirdsql.jaybird.util.StringUtils.isNullOrEmpty;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.ParameterConverter} for JNA.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class JnaParameterConverter extends AbstractParameterConverter<JnaDatabaseConnection, JnaServiceConnection> {

    @Override
    protected DatabaseParameterBuffer createDatabaseParameterBuffer(JnaDatabaseConnection connection) {
        if (supportsUtf8ParameterBuffer(connection)) {
            Encoding stringEncoding = connection.getEncodingFactory().getEncodingForFirebirdName("UTF8");
            var dpb = new DatabaseParameterBufferImp(DpbMetaData.DPB_VERSION_1, stringEncoding);
            dpb.addArgument(DpbItems.isc_dpb_utf8_filename);
            return dpb;
        }
        return super.createDatabaseParameterBuffer(connection);
    }

    @Override
    protected ServiceParameterBuffer createServiceParameterBuffer(JnaServiceConnection connection) {
        if (supportsUtf8ParameterBuffer(connection)) {
            Encoding stringEncoding = connection.getEncodingFactory().getEncodingForFirebirdName("UTF8");
            var spb = new ServiceParameterBufferImp(SpbMetaData.SPB_VERSION_2_ATTACH, stringEncoding);
            spb.addArgument(SpbItems.isc_spb_utf8_filename);
            return spb;
        }
        return super.createServiceParameterBuffer(connection);
    }

    @Override
    protected void populateAuthenticationProperties(final AbstractConnection<?, ?> connection,
            final ConnectionParameterBuffer pb) throws SQLException {
        IAttachProperties<?> props = connection.getAttachProperties();
        ParameterTagMapping tagMapping = pb.getTagMapping();
        if (props.getUser() != null) {
            pb.addArgument(tagMapping.getUserNameTag(), props.getUser());
        }
        if (props.getPassword() != null) {
            pb.addArgument(tagMapping.getPasswordTag(), props.getPassword());
        }

        Map<String, String> configMap = new HashMap<>();

        if (props.getWireCryptAsEnum() != WireCrypt.DEFAULT) {
            configMap.put("WireCrypt", props.getWireCrypt());
        }

        String authPlugins = props.getAuthPlugins();
        if (!isNullOrEmpty(authPlugins)) {
            configMap.put("AuthClient", authPlugins);
        }

        if (!configMap.isEmpty()) {
            String configString = buildConfigString(configMap);
            pb.addArgument(tagMapping.getConfigTag(), configString);
        }
    }

    private String buildConfigString(Map<String, String> configMap) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> configEntry : configMap.entrySet()) {
            builder.append(configEntry.getKey())
                    .append('=')
                    .append(configEntry.getValue())
                    .append('\n');
        }
        return builder.toString();
    }

    private static boolean supportsUtf8ParameterBuffer(JnaConnection<?, ?> connection) {
        // Indicates this is a Firebird 3.0 or higher client library
        return connection.hasFeature(FbClientFeature.FB_PING);
    }

}
