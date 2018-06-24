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

/**
 * Implementation of {@link org.firebirdsql.gds.ng.ParameterConverter} for JNA.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class JnaParameterConverter extends AbstractParameterConverter<JnaDatabaseConnection, JnaServiceConnection> {

    @Override
    protected void populateAuthenticationProperties(final AbstractConnection connection,
            final ConnectionParameterBuffer pb) throws SQLException {
        IAttachProperties props = connection.getAttachProperties();
        ParameterTagMapping tagMapping = pb.getTagMapping();
        if (props.getUser() != null) {
            pb.addArgument(tagMapping.getUserNameTag(), props.getUser());
        }
        if (props.getPassword() != null) {
            pb.addArgument(tagMapping.getPasswordTag(), props.getPassword());
        }

        Map<String, String> configMap = new HashMap<>();

        if (props.getWireCrypt() != WireCrypt.DEFAULT) {
            configMap.put("WireCrypt", props.getWireCrypt().name());
        }

        String authPlugins = props.getAuthPlugins();
        if (authPlugins != null && !authPlugins.isEmpty()) {
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
