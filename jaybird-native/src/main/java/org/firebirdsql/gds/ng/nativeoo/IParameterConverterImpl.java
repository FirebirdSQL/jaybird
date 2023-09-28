package org.firebirdsql.gds.ng.nativeoo;

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
 * Implementation of {@link org.firebirdsql.gds.ng.ParameterConverter} for native OO API.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 6.0
 */
public class IParameterConverterImpl extends AbstractParameterConverter<NativeDatabaseConnection,
        IServiceConnectionImpl> {

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
