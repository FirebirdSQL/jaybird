// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.gds.ConnectionParameterBuffer;
import org.firebirdsql.gds.ParameterTagMapping;
import org.firebirdsql.gds.ng.AbstractConnection;
import org.firebirdsql.gds.ng.AbstractParameterConverter;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.gds.ng.WireCrypt;

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
}
