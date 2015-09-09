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
package org.firebirdsql.gds.ng.wire.auth;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * Manages client authentication with multiple pluginProviders.
 * <p>
 * This is similar to the {@code ClntAuthBlock} in Firebird. For ease of understanding, and correlating with the
 * Firebird implementation, this uses the same (or very similar) terms and structure (if it makes sense).
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class ClientAuthBlock {

    private static final Logger log = LoggerFactory.getLogger(ClientAuthBlock.class);

    private final IAttachProperties<?> attachProperties;
    private LinkedList<AuthenticationPluginSpi> pluginProviders;
    private Set<String> serverPlugins = Collections.emptySet();
    private String pluginNames;
    private AuthenticationPlugin currentPlugin;
    private boolean authComplete;
    private boolean firstTime = true;

    public ClientAuthBlock(IAttachProperties<?> attachProperties) throws SQLException {
        this.attachProperties = attachProperties;
        resetClient(null);
    }

    public String getLogin() {
        return attachProperties.getUser();
    }

    public String getPassword() {
        return attachProperties.getPassword();
    }

    public boolean isAuthComplete() {
        return authComplete;
    }

    public void setAuthComplete(boolean authComplete) {
        this.authComplete = authComplete;
    }

    public String getCurrentPluginName() {
        return currentPlugin != null ? currentPlugin.getName() : null;
    }

    /**
     * @return Comma separated list of available plugins.
     */
    public String getPluginNames() {
        return pluginNames;
    }

    public byte[] getClientData() {
        return currentPlugin != null ? currentPlugin.getClientData() : null;
    }

    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public void authenticateStep0() throws SQLException {
        Iterator<AuthenticationPluginSpi> providerIterator = pluginProviders.iterator();
        while (providerIterator.hasNext()) {
            AuthenticationPluginSpi provider = providerIterator.next();
            AuthenticationPlugin plugin = provider.createPlugin();
            log.debug("Trying authentication plugin " + plugin);
            try {
                switch(plugin.authenticate(this)) {
                case AUTH_SUCCESS:
                    // TODO Temporary workaround
                    attachProperties.setAuthData(plugin.getClientData());
                case AUTH_MORE_DATA:
                    currentPlugin = plugin;
                    return;
                case AUTH_CONTINUE:
                    providerIterator.remove();
                    break;
                }
            } catch (SQLException ex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_login).cause(ex).toFlatSQLException();
            }
        }
    }

    public void resetClient(byte[] serverInfo) throws SQLException {
        if (serverInfo != null && serverInfo.length > 0) {
            if (currentPlugin != null && currentPlugin.hasServerData()) {
                // We should not change plugins iterator now
                return;
            }

            // TODO consider implementing a ClumpletReader like in Firebird
            int currentPosition = 0;
            while(currentPosition < serverInfo.length && serverInfo[currentPosition++] != TAG_KNOWN_PLUGINS) {
                int skipLength = serverInfo[currentPosition++];
                currentPosition += skipLength;
            }
            if (currentPosition < serverInfo.length) {
                // found
                int dataLength = serverInfo[currentPosition++];
                if (dataLength > 0) {
                    String serverPluginNames =
                            new String(serverInfo, currentPosition, dataLength, StandardCharsets.US_ASCII).trim();
                    serverPlugins = new HashSet<>(Arrays.asList(serverPluginNames.split("[ \t,;]+")));
                }
            }
        }

        firstTime = true;
        pluginProviders = new LinkedList<>(getSupportedPluginProviders());

        if (!serverPlugins.isEmpty()) {
            LinkedList<AuthenticationPluginSpi> mergedProviderList = new LinkedList<>();
            for (AuthenticationPluginSpi clientProvider : pluginProviders) {
                if (serverPlugins.contains(clientProvider.getPluginName())) {
                    mergedProviderList.add(clientProvider);
                }
            }

            if (mergedProviderList.isEmpty()) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_login)
                        .exception(ISCConstants.isc_random).messageParameter("No matching plugins on server")
                        .toFlatSQLException();
            }
            pluginProviders = mergedProviderList;
        }

        pluginNames = getPluginNames(pluginProviders);
    }

    public void setServerData(byte[] serverData) {
        if (currentPlugin == null) {
            // TODO Check if this is valid to ignore
            log.debug("Received server data without current plugin");
        } else {
            currentPlugin.setServerData(serverData);
        }
    }

    private static String getPluginNames(List<AuthenticationPluginSpi> pluginProviders) {
        if (pluginProviders.size() == 0) {
            return null;
        }
        StringBuilder names = new StringBuilder();
        for (int idx = 0; idx < pluginProviders.size(); idx++) {
            if (idx > 0) {
                names.append(',');
            }
            names.append(pluginProviders.get(idx).getPluginName());
        }
        return names.toString();
    }

    public void writePluginDataTo(OutputStream userId) throws IOException {
        if (attachProperties.getUser() != null) {
            final byte[] loginBytes = attachProperties.getUser().getBytes(StandardCharsets.UTF_8);
            userId.write(CNCT_login);
            int loginLength = Math.min(loginBytes.length, 255);
            userId.write(loginLength);
            userId.write(loginBytes, 0, loginLength);
        }

        final String pluginName = getCurrentPluginName();
        if (pluginName != null) {
            userId.write(CNCT_plugin_name);
            final byte[] pluginNameBytes = pluginName.getBytes(StandardCharsets.UTF_8);
            userId.write(pluginNameBytes.length);
            userId.write(pluginNameBytes, 0, pluginNameBytes.length);
        }

        final String pluginList = getPluginNames();
        if (pluginList != null) {
            userId.write(CNCT_plugin_list);
            final byte[] pluginListBytes = "Srp,Legacy_Auth".getBytes(StandardCharsets.UTF_8);
            userId.write(pluginListBytes.length);
            userId.write(pluginListBytes, 0, pluginListBytes.length);
        }

        if (currentPlugin != null) {
            byte[] specificDataBytes = currentPlugin.getClientData();
            if (specificDataBytes != null) {
                addMultiPartConnectParameter(userId, CNCT_specific_data, specificDataBytes);
            }
        }
    }

    private void addMultiPartConnectParameter(OutputStream userId, int paramType, byte[] specificDataBytes)
            throws IOException {
        // write specific data
        int remaining = specificDataBytes.length;
        int position = 0;
        int step = 0;
        while (remaining > 0) {
            userId.write(paramType);
            int toWrite = Math.min(remaining, 254);
            userId.write(toWrite + 1);
            userId.write(step++);
            userId.write(specificDataBytes, position, toWrite);
            remaining -= toWrite;
            position += toWrite;
        }
    }

    private static List<AuthenticationPluginSpi> getSupportedPluginProviders() {
        // TODO Create from service provider interface; use properties?
        return Collections.unmodifiableList(
                Arrays.<AuthenticationPluginSpi>asList(new SrpAuthenticationPluginSpi(), new LegacyAuthenticationPluginSpi()));
    }

    public boolean switchPlugin(String pluginName) {
        for (AuthenticationPluginSpi pluginProvider : pluginProviders) {
            if (pluginProvider.getPluginName().equals(pluginName)) {
                currentPlugin = pluginProvider.createPlugin();
                return true;
            }
        }
        return false;
    }

    public boolean hasPlugin() {
        return currentPlugin != null;
    }

    public AuthenticationPlugin.AuthStatus authenticate() throws SQLException {
        AuthenticationPlugin.AuthStatus authStatus = currentPlugin.authenticate(this);
        // TODO Temporary workaround for behavior in V13ParameterConverter, needs to be removed
        if (authStatus == AuthenticationPlugin.AuthStatus.AUTH_SUCCESS) {
            attachProperties.setAuthData(currentPlugin.getClientData());
        }
        return authStatus;
    }
}
