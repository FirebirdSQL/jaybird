// SPDX-FileCopyrightText: Copyright 2015-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
import org.firebirdsql.gds.ng.wire.GenericResponse;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;
import org.firebirdsql.gds.ng.wire.crypt.CryptConnectionInfo;
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
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_receiveTrustedAuth_NotSupported;
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
    public void authReceiveResponse(FbWireAttachment.AcceptPacket acceptPacket, DbCryptCallback dbCryptCallback)
            throws SQLException, IOException {
        assert acceptPacket == null || acceptPacket.operation == op_cond_accept
                : "Unexpected operation in AcceptPacket";
        final XdrInputStream xdrIn = getXdrIn();
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
                    xdrIn.skipBuffer(); // p_trau_data
                     throw FbExceptionBuilder.toNonTransientConnectionException(jb_receiveTrustedAuth_NotSupported);
                }
                case op_cont_auth -> {
                    data = xdrIn.readBuffer(); // p_data
                    pluginName = xdrIn.readString(encoding); // p_name
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

            withTransmitLock(xdrOut -> {
                sendContAuthMsg(xdrOut, clientAuthBlock);
                xdrOut.flush();
            });
        }

        // If we have exited from the cycle, this mean auth failed
        throw FbExceptionBuilder.toException(ISCConstants.isc_login);
    }

    /**
     * Sends the continue auth message (struct {@code p_auth_continue}) to the server, without flushing.
     * <p>
     * The caller is responsible for obtaining and releasing the transmit lock.
     * </p>
     *
     * @param xdrOut
     *         XDR output stream
     * @param clientAuthBlock
     *         client auth block
     * @throws IOException
     *         for errors writing to the output stream
     * @since 7
     */
    protected void sendContAuthMsg(XdrOutputStream xdrOut, ClientAuthBlock clientAuthBlock) throws IOException {
        // TODO Consider doing something other than the suppression of DataFlowIssue below
        Encoding encoding = getEncoding();
        xdrOut.writeInt(op_cont_auth); // p_operation
        xdrOut.writeBuffer(clientAuthBlock.getClientData()); // p_data
        //noinspection DataFlowIssue : if we're continueing auth, we have a plugin
        xdrOut.writeString(clientAuthBlock.getCurrentPluginName(), encoding); // p_name
        if (clientAuthBlock.isFirstTime()) {
            //noinspection DataFlowIssue : if we're continueing auth, we have a plugin list
            xdrOut.writeString(clientAuthBlock.getPluginNames(), encoding); // p_list
            clientAuthBlock.setFirstTime(false);
        } else {
            xdrOut.writeBuffer(null); // p_list
        }
        xdrOut.writeBuffer(null); // p_keys
    }

    private CryptSessionConfig getCryptSessionConfig(EncryptionIdentifier encryptionIdentifier, byte[] specificData)
            throws SQLException {
        ClientAuthBlock clientAuthBlock = getClientAuthBlock();
        if (!clientAuthBlock.supportsEncryption() || !encryptionIdentifier.isTypeSymmetric()) {
            throw FbExceptionBuilder.forNonTransientException(jb_cryptNoCryptKeyAvailable)
                    .messageParameter(encryptionIdentifier)
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
        record ConnectionInfoImpl(int protocolVersion) implements CryptConnectionInfo {
        }

        EncryptionIdentifier encryptionIdentifier = pluginSpecificData.encryptionIdentifier();
        Optional<EncryptionPluginSpi> encryptionPluginSpiOpt =
                EncryptionPluginRegistry.getEncryptionPluginSpi(encryptionIdentifier);
        if (encryptionPluginSpiOpt.isEmpty()) {
            log.log(TRACE, "No wire encryption plugin available for {0}", encryptionIdentifier);
            return Optional.empty();
        }
        EncryptionPluginSpi encryptionPluginSpi = encryptionPluginSpiOpt.get();
        if (!encryptionPluginSpi.isSupported(new ConnectionInfoImpl(getConnection().getProtocolVersion()))) {
            log.log(TRACE, "Wire encryption plugin {0} skipped, not supported", encryptionIdentifier);
            return Optional.empty();
        }
        
        try (CryptSessionConfig cryptSessionConfig =
                     getCryptSessionConfig(encryptionIdentifier, pluginSpecificData.specificData())) {
            EncryptionPlugin encryptionPlugin = encryptionPluginSpi.createEncryptionPlugin(cryptSessionConfig);
            EncryptionInitInfo encryptionInitInfo = encryptionPlugin.initializeEncryption();
            if (encryptionInitInfo instanceof EncryptionInitInfo.Success success) {
                enableEncryption(success);

                clearServerKeys();

                if (chainBuilder.hasException() && log.isLoggable(WARNING)) {
                    //noinspection DataFlowIssue - we know chainBuilder has an exception
                    log.log(WARNING, "Wire encryption established with {0}, but some plugins failed; see debug level for stacktraces\n{1}",
                            encryptionIdentifier, ExceptionHelper.collectAllMessages(chainBuilder.getException()));
                } else {
                    log.log(TRACE, "Wire encryption established with {0}", encryptionIdentifier);
                }
                return Optional.of(encryptionIdentifier);
            } else if (encryptionInitInfo instanceof EncryptionInitInfo.Failure failure) {
                chainBuilder.append(failure.getCause());
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

    @SuppressWarnings("DataFlowIssue")
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

    protected void enableEncryption(EncryptionInitInfo.Success encryptionInitInfo) throws SQLException, IOException {
        withTransmitLock(xdrOut -> {
            sendCryptMsg(xdrOut, encryptionInitInfo.getEncryptionIdentifier());
            xdrOut.flush();
            // Set the ciphers under transmit lock to guarantee visibility for out-of-band operations
            getXdrIn().setCipher(encryptionInitInfo.getDecryptionCipher());
            xdrOut.setCipher(encryptionInitInfo.getEncryptionCipher());
        });

        readResponse(null);
    }

    /**
     * Sends the start encryption message (struct {@code p_crypt}) to the server, without flushing.
     * <p>
     * The caller is responsible for obtaining and releasing the transmit lock.
     * </p>
     *
     * @param xdrOut
     *         XDR output stream
     * @param encryptionIdentifier
     *         encryption identifier of the encryption to enable
     * @throws IOException
     *         for errors writing to the output stream
     * @since 7
     */
    protected void sendCryptMsg(XdrOutputStream xdrOut, EncryptionIdentifier encryptionIdentifier) throws IOException {
        Encoding encoding = getEncoding();
        xdrOut.writeInt(op_crypt); // p_operation
        xdrOut.writeString(encryptionIdentifier.pluginName(), encoding); // p_plugin
        xdrOut.writeString(encryptionIdentifier.type(), encoding); // p_key
    }

    @Override
    public final void handleCryptKeyCallback(DbCryptCallback dbCryptCallback) throws IOException, SQLException {
        DbCryptData clientPluginResponse = getClientPluginResponse(dbCryptCallback, readCryptKeyCallback());
        withTransmitLock(xdrOut -> {
            sendCryptKeyCallbackMsg(xdrOut, clientPluginResponse);
            xdrOut.flush();
        });
    }

    private static DbCryptData getClientPluginResponse(DbCryptCallback dbCryptCallback, DbCryptData serverPluginData) {
        try {
            return dbCryptCallback.handleCallback(serverPluginData);
        } catch (Exception e) {
            log.log(ERROR, "Error during database encryption callback, using default empty response", e);
            return DbCryptData.EMPTY_DATA;
        }
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
     * Sends the database encryption callback message (struct {@code p_crypt_callback}) to the server, without flushing.
     * <p>
     * The caller is responsible for obtaining and releasing the transmit lock.
     * </p>
     *
     * @param xdrOut
     *         XDR output stream
     * @param clientPluginResponse
     *         database encryption callback response data to be sent to the server
     * @throws IOException
     *         for errors writing data to the output stream
     */
    protected void sendCryptKeyCallbackMsg(XdrOutputStream xdrOut, DbCryptData clientPluginResponse)
            throws IOException {
        xdrOut.writeInt(op_crypt_key_callback); // p_operation
        xdrOut.writeBuffer(clientPluginResponse.getPluginData()); // p_cc_data
    }

}
