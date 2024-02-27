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
package org.firebirdsql.gds.ng.wire.version13;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptData;
import org.firebirdsql.gds.ng.wire.FbWireAttachment;
import org.firebirdsql.gds.ng.wire.FbWireOperations;
import org.firebirdsql.gds.ng.wire.GenericResponse;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;
import org.firebirdsql.gds.ng.wire.crypt.CryptSessionConfig;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionIdentifier;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionInitInfo;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPlugin;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPluginRegistry;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPluginSpi;
import org.firebirdsql.gds.ng.wire.crypt.KnownServerKey;
import org.firebirdsql.gds.ng.wire.version11.V11WireOperations;
import org.firebirdsql.jaybird.util.ExceptionHelper;
import org.firebirdsql.jaybird.util.SQLExceptionChainBuilder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_cryptNoCryptKeyAvailable;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V13WireOperations extends V11WireOperations {

    private static final System.Logger log = System.getLogger(V13WireOperations.class.getName());

    public V13WireOperations(WireConnection<?, ?> connection, WarningMessageCallback defaultWarningMessageCallback) {
        super(connection, defaultWarningMessageCallback);
    }

    @Override
    public void authReceiveResponse(FbWireAttachment.AcceptPacket acceptPacket, DbCryptCallback dbCryptCallback,
            FbWireOperations.ProcessAttachCallback processAttachCallback) throws SQLException, IOException {
        assert acceptPacket == null || acceptPacket.operation == op_cond_accept
                : "Unexpected operation in AcceptPacket";
        final XdrInputStream xdrIn = getXdrIn();
        final XdrOutputStream xdrOut = getXdrOut();
        final ClientAuthBlock clientAuthBlock = getClientAuthBlock();
        final Encoding encoding = getEncoding();
        while (true) {
            String pluginName;
            byte[] data;
            if (acceptPacket != null) {
                data = acceptPacket.p_acpt_data;
                pluginName = acceptPacket.p_acpt_plugin;
                addServerKeys(acceptPacket.p_acpt_keys);
                acceptPacket = null;
            } else {
                int operation = readNextOperation();
                switch (operation) {
                case op_trusted_auth -> {
                    xdrIn.skipNBytes(4); // skip int: p_trau_data
                    throw FbExceptionBuilder
                            .forNonTransientConnectionException(JaybirdErrorCodes.jb_receiveTrustedAuth_NotSupported)
                            .toSQLException();
                }
                case op_cont_auth -> {
                    data = xdrIn.readBuffer(); // p_data
                    pluginName = xdrIn.readString(encoding); //p_name
                    xdrIn.skipBuffer(); // skip: p_list (ignore?)
                    addServerKeys(xdrIn.readBuffer()); // p_keys
                }
                case op_crypt_key_callback -> {
                    log.log(TRACE, "Handling db crypt callback using plugin {0}",
                            dbCryptCallback.getDbCryptCallbackName());
                    handleCryptKeyCallback(dbCryptCallback);
                    continue;
                }
                case op_cond_accept -> {
                    // Note this is the equivalent of handling the acceptPacket != null above
                    xdrIn.skipNBytes(3 * 4); // skip 3 ints: p_acpt_version, p_acpt_architecture, p_acpt_type
                    data = xdrIn.readBuffer(); // p_acpt_data
                    pluginName = xdrIn.readString(encoding); // p_acpt_plugin
                    xdrIn.skipNBytes(4); // skip int: p_acpt_authenticated
                    addServerKeys(xdrIn.readBuffer()); //p_acpt_keys
                }
                case op_response -> {
                    GenericResponse response = (GenericResponse) readOperationResponse(operation, null);
                    boolean wasAuthComplete = clientAuthBlock.isAuthComplete();
                    clientAuthBlock.setAuthComplete(true);
                    processAttachCallback.processAttachResponse(response);
                    addServerKeys(response.data());

                    WireCrypt wireCrypt = getAttachProperties().getWireCryptAsEnum();

                    if (!wasAuthComplete && wireCrypt != WireCrypt.DISABLED) {
                        tryKnownServerKeys();
                    }
                    return;
                }
                default -> throw new SQLException(format("Unsupported operation code: %d", operation));
                }
            }

            if (pluginName != null && !pluginName.isEmpty()
                && Objects.equals(pluginName, clientAuthBlock.getCurrentPluginName())) {
                pluginName = null;
            }

            if (pluginName != null && !pluginName.isEmpty() && !clientAuthBlock.switchPlugin(pluginName)) {
                break;
            }

            if (!clientAuthBlock.hasPlugin()) {
                break;
            }

            clientAuthBlock.setServerData(data);
            log.log(TRACE, "receiveResponse: authenticate({0})", clientAuthBlock.getCurrentPluginName());
            clientAuthBlock.authenticate();

            xdrOut.writeInt(op_cont_auth);
            // TODO Move to ClientAuthBlock?
            xdrOut.writeBuffer(clientAuthBlock.getClientData()); // p_data
            xdrOut.writeString(clientAuthBlock.getCurrentPluginName(), encoding); // p_name
            if (clientAuthBlock.isFirstTime()) {
                xdrOut.writeString(clientAuthBlock.getPluginNames(), encoding); // p_list
                clientAuthBlock.setFirstTime(false);
            } else {
                xdrOut.writeBuffer(null); // p_list
            }
            xdrOut.writeBuffer(null); // p_keys
            xdrOut.flush();
        }

        // If we have exited from the cycle, this mean auth failed
        throw FbExceptionBuilder.forException(ISCConstants.isc_login).toSQLException();
    }

    private CryptSessionConfig getCryptSessionConfig(EncryptionIdentifier encryptionIdentifier, byte[] specificData)
            throws SQLException {
        ClientAuthBlock clientAuthBlock = getClientAuthBlock();
        if (!clientAuthBlock.supportsEncryption() || !encryptionIdentifier.isTypeSymmetric()) {
            throw FbExceptionBuilder.forNonTransientException(jb_cryptNoCryptKeyAvailable)
                    .messageParameter(encryptionIdentifier.toString())
                    .toSQLException();
        }
        return CryptSessionConfig.symmetric(encryptionIdentifier, clientAuthBlock.getSessionKey(), specificData);
    }

    private void tryKnownServerKeys() throws IOException, SQLException {
        var chainBuilder = new SQLExceptionChainBuilder();

        EncryptionIdentifier selectedEncryption = null;
        for (KnownServerKey.PluginSpecificData pluginSpecificData : getPluginSpecificData()) {
            Optional<EncryptionIdentifier> selectedEncryptionOpt = tryKnownServerKey(pluginSpecificData, chainBuilder);
            if (selectedEncryptionOpt.isPresent()) {
                selectedEncryption = selectedEncryptionOpt.get();
                break;
            }
        }

        if (selectedEncryption == null) {
            throwIfWireCryptRequired(chainBuilder.getException());
        }

        logWireCryptPluginFailures(chainBuilder, selectedEncryption);
    }

    private Optional<EncryptionIdentifier> tryKnownServerKey(KnownServerKey.PluginSpecificData pluginSpecificData,
            SQLExceptionChainBuilder chainBuilder) throws IOException {
        EncryptionIdentifier encryptionIdentifier = pluginSpecificData.encryptionIdentifier();
        EncryptionPluginSpi currentEncryptionSpi =
                EncryptionPluginRegistry.getEncryptionPluginSpi(encryptionIdentifier);
        if (currentEncryptionSpi == null) {
            log.log(TRACE, "No wire encryption plugin available for {0}", encryptionIdentifier);
            return Optional.empty();
        }
        try (CryptSessionConfig cryptSessionConfig =
                     getCryptSessionConfig(encryptionIdentifier, pluginSpecificData.specificData())) {
            EncryptionPlugin encryptionPlugin = currentEncryptionSpi.createEncryptionPlugin(cryptSessionConfig);
            EncryptionInitInfo encryptionInitInfo = encryptionPlugin.initializeEncryption();
            if (encryptionInitInfo.isSuccess()) {
                enableEncryption(encryptionInitInfo);

                clearServerKeys();

                if (chainBuilder.hasException() && log.isLoggable(WARNING)) {
                    log.log(WARNING, "Wire encryption established with {0}, but some plugins failed; see debug level for stacktraces\n{1}",
                            encryptionIdentifier, ExceptionHelper.collectAllMessages(chainBuilder.getException()));
                } else {
                    log.log(TRACE, "Wire encryption established with {0}", encryptionIdentifier);
                }
                return Optional.of(encryptionIdentifier);
            } else {
                chainBuilder.append(encryptionInitInfo.getException());
            }
        } catch (SQLException e) {
            chainBuilder.append(e);
        }
        return Optional.empty();
    }

    private void throwIfWireCryptRequired(SQLException encryptionException) throws SQLException {
        if (getAttachProperties().getWireCryptAsEnum() == WireCrypt.REQUIRED) {
            throw FbExceptionBuilder.forNonTransientException(ISCConstants.isc_wirecrypt_incompatible)
                    .cause(encryptionException)
                    .toSQLException();
        }
    }

    private static void logWireCryptPluginFailures(SQLExceptionChainBuilder chainBuilder,
            EncryptionIdentifier selectedEncryption) {
        if (chainBuilder.hasException() && log.isLoggable(WARNING)) {
            if (selectedEncryption == null) {
                log.log(WARNING, "No wire encryption established because of plugin failures; see debug level for stacktraces:\n{0}",
                        ExceptionHelper.collectAllMessages(chainBuilder.getException()));
            }
            if (log.isLoggable(DEBUG)) {
                SQLException current = chainBuilder.getException();
                do {
                    log.log(DEBUG, "Encryption plugin failed", current);
                } while ((current = current.getNextException()) != null);
            }
        }
    }

    protected void enableEncryption(EncryptionInitInfo encryptionInitInfo) throws SQLException, IOException {
        final XdrInputStream xdrIn = getXdrIn();
        final XdrOutputStream xdrOut = getXdrOut();
        final Encoding encoding = getEncoding();
        final EncryptionIdentifier encryptionIdentifier = encryptionInitInfo.getEncryptionIdentifier();

        xdrOut.writeInt(op_crypt);
        xdrOut.writeString(encryptionIdentifier.pluginName(), encoding);
        xdrOut.writeString(encryptionIdentifier.type(), encoding);
        xdrOut.flush();

        xdrIn.setCipher(encryptionInitInfo.getDecryptionCipher());
        xdrOut.setCipher(encryptionInitInfo.getEncryptionCipher());

        readResponse(null);
    }

    @Override
    public final void handleCryptKeyCallback(DbCryptCallback dbCryptCallback) throws IOException, SQLException {
        final DbCryptData serverPluginData = readCryptKeyCallback();
        DbCryptData clientPluginResponse;
        try {
            clientPluginResponse = dbCryptCallback.handleCallback(serverPluginData);
        } catch (Exception e) {
            log.log(ERROR, "Error during database encryption callback, using default empty response", e);
            clientPluginResponse = DbCryptData.EMPTY_DATA;
        }
        writeCryptKeyCallback(clientPluginResponse);
    }

    /**
     * Reads the database encryption callback data from the connection.
     *
     * @return Database encryption callback data received from server
     * @throws IOException
     *         For errors reading data from the socket
     * @throws SQLException
     *         For database errors
     */
    protected DbCryptData readCryptKeyCallback() throws IOException, SQLException {
        final XdrInputStream xdrIn = getXdrIn();
        final byte[] pluginData = xdrIn.readBuffer(); // p_cc_data
        try {
            return new DbCryptData(pluginData, Integer.MIN_VALUE);
        } catch (RuntimeException e) {
            throw FbExceptionBuilder.forNonTransientConnectionException(JaybirdErrorCodes.jb_dbCryptDataError)
                    .cause(e)
                    .toSQLException();
        }
    }

    /**
     * Writes the database encryption callback response data to the connection.
     *
     * @param clientPluginResponse
     *         Database encryption callback response data to be sent to the server
     * @throws IOException
     *         For errors reading data from the socket
     * @throws SQLException
     *         For database errors
     */
    protected void writeCryptKeyCallback(DbCryptData clientPluginResponse) throws SQLException, IOException {
        final XdrOutputStream xdrOut = getXdrOut();
        xdrOut.writeInt(op_crypt_key_callback);
        xdrOut.writeBuffer(clientPluginResponse.getPluginData()); // p_cc_data
        xdrOut.flush();
    }

}
