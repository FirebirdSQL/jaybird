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
package org.firebirdsql.gds.ng.wire.auth;

import org.firebirdsql.gds.ClumpletReader;
import org.firebirdsql.gds.ConnectionParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ParameterTagMapping;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.IAttachProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;
import static org.firebirdsql.jaybird.props.PropertyConstants.DEFAULT_AUTH_PLUGINS;

/**
 * Manages client authentication with multiple pluginProviders.
 * <p>
 * This is similar to the {@code ClntAuthBlock} in Firebird. For ease of understanding, and correlating with the
 * Firebird implementation, this uses the same (or very similar) terms and structure (if it makes sense).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class ClientAuthBlock {

    private static final System.Logger log = System.getLogger(ClientAuthBlock.class.getName());

    private static final Pattern AUTH_PLUGIN_LIST_SPLIT = Pattern.compile("[ \t,;]+");

    private final IAttachProperties<?> attachProperties;
    private List<AuthenticationPluginSpi> pluginProviders;
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

    public String getNormalizedLogin() {
        return normalizeLogin(getLogin());
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
            log.log(TRACE, "Trying authentication plugin {0}", plugin);
            try {
                switch (plugin.authenticate(this)) {
                case AUTH_SUCCESS, AUTH_MORE_DATA -> {
                    currentPlugin = plugin;
                    return;
                }
                case AUTH_CONTINUE -> providerIterator.remove();
                }
            } catch (SQLException ex) {
                throw FbExceptionBuilder.forException(ISCConstants.isc_login).cause(ex).toSQLException();
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
                String serverPluginNames = serverList.getString(StandardCharsets.ISO_8859_1);
                serverPlugins.clear();
                serverPlugins.addAll(splitPluginList(serverPluginNames));
            }
        }

        firstTime = true;
        currentPlugin = null;
        pluginProviders = getSupportedPluginProviders();

        if (!serverPlugins.isEmpty()) {
            List<AuthenticationPluginSpi> mergedProviderList = new ArrayList<>();
            for (AuthenticationPluginSpi clientProvider : pluginProviders) {
                if (serverPlugins.contains(clientProvider.getPluginName())) {
                    mergedProviderList.add(clientProvider);
                }
            }

            if (mergedProviderList.isEmpty()) {
                throw FbExceptionBuilder.forException(ISCConstants.isc_login)
                        .exception(ISCConstants.isc_random).messageParameter("No matching plugins on server")
                        .toSQLException();
            }
            pluginProviders = mergedProviderList;
        }
    }

    public void setServerData(byte[] serverData) {
        if (currentPlugin == null) {
            log.log(DEBUG, "Received server data without current plugin");
        } else {
            currentPlugin.setServerData(serverData);
        }
    }

    private static String getPluginNames(List<AuthenticationPluginSpi> pluginProviders) {
        if (pluginProviders.isEmpty()) {
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
        final String user = getLogin();
        if (user != null) {
            final byte[] loginBytes = user.getBytes(StandardCharsets.UTF_8);
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
            log.log(TRACE, "Trying authentication plugin {0}", plugin);
            try {
                switch (plugin.authenticate(this)) {
                case AUTH_SUCCESS, AUTH_MORE_DATA -> {
                    log.log(TRACE, "Trying authentication plugin {0} is OK", plugin);
                    currentPlugin = plugin;
                    cleanParameterBuffer(pb);
                    extractDataToParameterBuffer(pb);
                    return;
                }
                case AUTH_CONTINUE -> providerIterator.remove();
                }
            } catch (SQLException ex) {
                throw FbExceptionBuilder.forException(ISCConstants.isc_login).cause(ex).toSQLException();
            }

            log.log(TRACE, "try next plugin, {0} skipped", plugin);
        }
    }

    /**
     * TODO Need to handle this differently
     * @return {@code true} if the encryption is supported
     * @throws SQLException
     *         If it is impossible to determine if encryption is supported (e.g. there is no current auth plugin)
     */
    public boolean supportsEncryption() throws SQLException {
        if (currentPlugin == null) {
            throw new SQLException("No authentication plugin available");
        }
        return currentPlugin.generatesSessionKey();
    }

    /**
     * @return Session key
     * @throws SQLException
     *         If a session key cannot be provided
     */
    public byte[] getSessionKey() throws SQLException {
        if (currentPlugin == null) {
            throw new SQLException("No authentication plugin available");
        }
        return currentPlugin.getSessionKey();
    }

    /**
     * Normalizes a login by uppercasing unquoted usernames, or stripping and unescaping (double) quoted user names.
     *
     * @param login Login to process
     * @return Normalized login
     */
    static String normalizeLogin(String login) {
        if (login == null || login.isEmpty()) {
            return login;
        }
        // Contrary to Firebird, check if login is enclosed in double quotes, not just starting with a double quote
        if (login.length() > 2 && login.charAt(0) == '"' && login.charAt(login.length() - 1) == '"') {
            return normalizeQuotedLogin(login);
        }
        return login.toUpperCase(Locale.ROOT);
    }

    private static String normalizeQuotedLogin(String login) {
        final StringBuilder sb = new StringBuilder(login.length() - 2);
        sb.append(login, 1, login.length() - 1);

        for (int idx = 0; idx < sb.length(); idx++) {
            // Double double quotes ("") escape a double quote in a quoted string
            if (sb.charAt(idx) == '"') {
                // Strip double quote escape
                sb.deleteCharAt(idx);
                if (idx < sb.length() && sb.charAt(idx) == '"') {
                    // Retain escaped double quote
                    idx += 1;
                } else {
                    // The character after escape is not a double quote, we terminate the conversion and truncate.
                    // Firebird does this as well (see common/utils.cpp#dpbItemUpper)
                    sb.setLength(idx);
                    return sb.toString();
                }
            }
        }

        return sb.toString();
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
            log.log(TRACE, "first time - added plugName & pluginList");
        }
        pb.addArgument(tagMapping.getSpecificAuthDataTag(), clientData);
        log.log(TRACE, "Added {0} bytes of spec data with tag isc_dpb_specific_auth_data", clientData.length);
    }

    private void cleanParameterBuffer(ConnectionParameterBuffer pb) {
        ParameterTagMapping tagMapping = pb.getTagMapping();
        pb.removeArgument(tagMapping.getPasswordTag());
        pb.removeArgument(tagMapping.getEncryptedPasswordTag());
        pb.removeArgument(tagMapping.getTrustedAuthTag());
    }

    private List<AuthenticationPluginSpi> getSupportedPluginProviders() throws SQLException {
        List<String> requestedPluginNames = getRequestedPluginNames();
        List<AuthenticationPluginSpi> pluginProviders = new ArrayList<>(requestedPluginNames.size());
        for (String pluginName : requestedPluginNames) {
            AuthenticationPluginSpi pluginSpi = AuthenticationPluginRegistry.getAuthenticationPluginSpi(pluginName);
            if (pluginSpi != null) {
                pluginProviders.add(pluginSpi);
            } else {
                log.log(WARNING, "No authentication plugin available with name {0}", pluginName);
            }
        }

        if (pluginProviders.isEmpty()) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_noKnownAuthPlugins)
                    .messageParameter(requestedPluginNames.toString())
                    .toSQLException();
        }
        return pluginProviders;
    }

    private List<String> getRequestedPluginNames() {
        String pluginListString = attachProperties.getAuthPlugins();
        if (pluginListString == null || pluginListString.isEmpty()) {
            pluginListString = DEFAULT_AUTH_PLUGINS;
        }
        return splitPluginList(pluginListString);
    }

    private static List<String> splitPluginList(String pluginList) {
        return Arrays.asList(AUTH_PLUGIN_LIST_SPLIT.split(pluginList));
    }

}
