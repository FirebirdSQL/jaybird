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

import org.firebirdsql.gds.ClumpletReader;
import org.firebirdsql.gds.ConnectionParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ParameterTagMapping;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

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

    private static final Pattern AUTH_PLUGIN_LIST_SPLIT = Pattern.compile("[ \t,;]+");

    private final IAttachProperties<?> attachProperties;
    private LinkedList<AuthenticationPluginSpi> pluginProviders;
    private final Set<String> serverPlugins = new LinkedHashSet<>();
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
        return getPluginNames(pluginProviders);
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
                switch (plugin.authenticate(this)) {
                case AUTH_SUCCESS:
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
        if (serverInfo != null) {
            if (currentPlugin != null && currentPlugin.hasServerData()) {
                // We should not change plugins iterator now
                return;
            }

            ClumpletReader serverList = new ClumpletReader(ClumpletReader.Kind.UnTagged, serverInfo);
            if (serverList.find(TAG_KNOWN_PLUGINS)) {
                String serverPluginNames = serverList.getString(StandardCharsets.US_ASCII);
                serverPlugins.clear();
                serverPlugins.addAll(Arrays.asList(AUTH_PLUGIN_LIST_SPLIT.split(serverPluginNames)));
            }
        }

        firstTime = true;
        currentPlugin = null;
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
            final byte[] pluginListBytes = pluginList.getBytes(StandardCharsets.UTF_8);
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
        if (hasPlugin() && Objects.equals(getCurrentPluginName(), pluginName)) {
            return false;
        }
        for (Iterator<AuthenticationPluginSpi> iterator = pluginProviders.iterator(); iterator.hasNext(); ) {
            AuthenticationPluginSpi pluginProvider = iterator.next();
            if (pluginProvider.getPluginName().equals(pluginName)) {
                currentPlugin = pluginProvider.createPlugin();
                return true;
            }
            iterator.remove();
        }
        return false;
    }

    public boolean hasPlugin() {
        return currentPlugin != null;
    }

    public AuthenticationPlugin.AuthStatus authenticate() throws SQLException {
        return currentPlugin.authenticate(this);
    }

    public void authFillParametersBlock(ConnectionParameterBuffer pb) throws SQLException {
        Iterator<AuthenticationPluginSpi> providerIterator = pluginProviders.iterator();
        while (providerIterator.hasNext()) {
            AuthenticationPluginSpi provider = providerIterator.next();
            AuthenticationPlugin plugin;
            if (hasPlugin() && provider.getPluginName().equals(getCurrentPluginName())) {
                plugin = currentPlugin;
            } else {
                plugin = provider.createPlugin();
            }
            log.debug("Trying authentication plugin " + plugin);
            try {
                switch (plugin.authenticate(this)) {
                case AUTH_SUCCESS:
                case AUTH_MORE_DATA:
                    log.debug("Trying authentication plugin " + plugin + " is OK");
                    currentPlugin = plugin;
                    cleanParameterBuffer(pb);
                    extractDataToParameterBuffer(pb);
                    return;
                case AUTH_CONTINUE:
                    providerIterator.remove();
                    break;
                }
            } catch (SQLException ex) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_login).cause(ex).toFlatSQLException();
            }

            log.debug(String.format("try next plugin, %s skipped", plugin));
        }
    }

    private void extractDataToParameterBuffer(ConnectionParameterBuffer pb) {
        byte[] clientData = getClientData();
        if (clientData == null || clientData.length == 0) {
            return;
        }
        String pluginName = getCurrentPluginName();
        ParameterTagMapping tagMapping = pb.getTagMapping();
        if (firstTime) {
            if (pluginName != null) {
                pb.addArgument(tagMapping.getAuthPluginNameTag(), pluginName);
            }
            pb.addArgument(tagMapping.getAuthPluginListTag(), getPluginNames());
            firstTime = false;
            log.debug("first time - added plugName & pluginList");
        }
        pb.addArgument(tagMapping.getSpecificAuthDataTag(), clientData);
        log.debug(String.format("Added %d bytes of spec data with tag isc_dpb_specific_auth_data", clientData.length));
    }

    private void cleanParameterBuffer(ConnectionParameterBuffer pb) {
        ParameterTagMapping tagMapping = pb.getTagMapping();
        pb.removeArgument(tagMapping.getPasswordTag());
        pb.removeArgument(tagMapping.getEncryptedPasswordTag());
        pb.removeArgument(tagMapping.getTrustedAuthTag());
    }
}
