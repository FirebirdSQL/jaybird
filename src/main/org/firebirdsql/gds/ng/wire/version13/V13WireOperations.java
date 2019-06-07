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
package org.firebirdsql.gds.ng.wire.version13;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptData;
import org.firebirdsql.gds.ng.wire.FbWireAttachment;
import org.firebirdsql.gds.ng.wire.FbWireOperations;
import org.firebirdsql.gds.ng.wire.GenericResponse;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionIdentifier;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionInitInfo;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPlugin;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPluginSpi;
import org.firebirdsql.gds.ng.wire.crypt.arc4.Arc4EncryptionPluginSpi;
import org.firebirdsql.gds.ng.wire.version11.V11WireOperations;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V13WireOperations extends V11WireOperations {

    private static final Logger log = LoggerFactory.getLogger(V13WireOperations.class);
    private static final EncryptionPluginSpi ARC4_ENCRYPTION_PLUGIN_SPI = new Arc4EncryptionPluginSpi();

    public V13WireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback, Object syncObject) {
        super(connection, defaultWarningMessageCallback, syncObject);
    }

    @Override
    public void authReceiveResponse(FbWireAttachment.AcceptPacket acceptPacket,
            DbCryptCallback dbCryptCallback,
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
                log.debug(String.format("authReceiveResponse: cond_accept data=%d pluginName=%d '%s'",
                        data.length, pluginName != null ? pluginName.length() : null, pluginName));
                // TODO handle compression
                acceptPacket = null;
            } else {
                int operation = readNextOperation();
                switch (operation) {
                case op_trusted_auth:
                    xdrIn.readBuffer(); // p_trau_data
                    throw new FbExceptionBuilder()
                            .nonTransientConnectionException(JaybirdErrorCodes.jb_receiveTrustedAuth_NotSupported)
                            .toFlatSQLException();
                case op_cont_auth:
                    data = xdrIn.readBuffer(); // p_data
                    pluginName = xdrIn.readString(encoding); //p_name
                    xdrIn.readBuffer(); // p_list (ignore?)
                    addServerKeys(xdrIn.readBuffer()); // p_keys
                    log.debug(String.format("authReceiveResponse: cont_auth data=%d pluginName=%d '%s'",
                            data.length, pluginName.length(), pluginName));
                    break;
                case op_crypt_key_callback:
                    log.debug("Handling db crypt callback using plugin " + dbCryptCallback.getDbCryptCallbackName());
                    handleCryptKeyCallback(dbCryptCallback);
                    continue;
                case op_cond_accept:
                    // Note this is the equivalent of handling the acceptPacket != null above
                    xdrIn.readInt(); // p_acpt_version
                    xdrIn.readInt(); // p_acpt_architecture
                    xdrIn.readInt(); // p_acpt_type
                    data = xdrIn.readBuffer(); // p_acpt_data
                    pluginName = xdrIn.readString(encoding); // p_acpt_plugin
                    xdrIn.readInt(); // p_acpt_authenticated
                    addServerKeys(xdrIn.readBuffer()); //p_acpt_keys
                    log.debug(String.format("authReceiveResponse: cond_accept data=%d pluginName=%d '%s'",
                            data.length, pluginName.length(), pluginName));
                    // TODO handle compression
                    break;

                case op_response:
                    GenericResponse response = (GenericResponse) readOperationResponse(operation, null);
                    boolean wasAuthComplete = clientAuthBlock.isAuthComplete();
                    clientAuthBlock.setAuthComplete(true);
                    processAttachCallback.processAttachResponse(response);
                    addServerKeys(response.getData());

                    WireCrypt wireCrypt = getAttachProperties().getWireCrypt();

                    if (!wasAuthComplete && wireCrypt != WireCrypt.DISABLED) {
                        tryKnownServerKeys();
                    }
                    return;
                default:
                    throw new SQLException(String.format("Unsupported operation code: %d", operation));
                }
            }

            if (pluginName != null && pluginName.length() > 0
                    && Objects.equals(pluginName, clientAuthBlock.getCurrentPluginName())) {
                pluginName = null;
            }

            if (pluginName != null && pluginName.length() > 0) {
                if (!clientAuthBlock.switchPlugin(pluginName)) {
                    break;
                }
            }

            if (!clientAuthBlock.hasPlugin()) {
                break;
            }

            clientAuthBlock.setServerData(data);
            log.debug(String.format("receiveResponse: authenticate(%s)", clientAuthBlock.getCurrentPluginName()));
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
        throw new FbExceptionBuilder().exception(ISCConstants.isc_login).toFlatSQLException();
    }

    private void tryKnownServerKeys() throws IOException, SQLException {
        boolean initializedEncryption = false;
        SQLExceptionChainBuilder<SQLException> chainBuilder = new SQLExceptionChainBuilder<>();

        // TODO Define separately and make configurable
        Map<EncryptionIdentifier, EncryptionPluginSpi> supportedEncryptionPlugins = new HashMap<>();
        supportedEncryptionPlugins.put(ARC4_ENCRYPTION_PLUGIN_SPI.getEncryptionIdentifier(), ARC4_ENCRYPTION_PLUGIN_SPI);

        for (EncryptionIdentifier encryptionIdentifier : getEncryptionIdentifiers()) {
            EncryptionPluginSpi currentEncryptionSpi =
                    supportedEncryptionPlugins.get(encryptionIdentifier);
            if (currentEncryptionSpi == null) {
                continue;
            }

            EncryptionPlugin encryptionPlugin =
                    currentEncryptionSpi.createEncryptionPlugin(getConnection());
            EncryptionInitInfo encryptionInitInfo = encryptionPlugin.initializeEncryption();
            if (encryptionInitInfo.isSuccess()) {
                enableEncryption(encryptionInitInfo);

                clearServerKeys();

                initializedEncryption = true;
                log.debug("Wire encryption established with " + encryptionIdentifier);
                break;
            } else {
                chainBuilder.append(encryptionInitInfo.getException());
            }
        }

        if (!initializedEncryption
                && getAttachProperties().getWireCrypt() == WireCrypt.REQUIRED) {
            SQLException exception = new FbExceptionBuilder()
                    .nonTransientException(ISCConstants.isc_wirecrypt_incompatible)
                    .toFlatSQLException();
            if (chainBuilder.hasException()) {
                exception.setNextException(chainBuilder.getException());
            }
            throw exception;
        }

        if (chainBuilder.hasException()) {
            log.warn(initializedEncryption
                    ? "No wire encryption established because of errors"
                    : "Wire encryption established, but some plugins failed; see other loglines for details");
            SQLException current = chainBuilder.getException();
            do {
                log.warn("Encryption plugin failed: " + current + "; see debug level for stacktrace");
                log.debug("Encryption plugin failed", current);
            } while ((current = current.getNextException()) != null);
        }
    }

    protected void enableEncryption(EncryptionInitInfo encryptionInitInfo) throws SQLException, IOException {
        final XdrInputStream xdrIn = getXdrIn();
        final XdrOutputStream xdrOut = getXdrOut();
        final Encoding encoding = getEncoding();
        final EncryptionIdentifier encryptionIdentifier = encryptionInitInfo.getEncryptionIdentifier();

        xdrOut.writeInt(op_crypt);
        xdrOut.writeString(encryptionIdentifier.getPluginName(), encoding);
        xdrOut.writeString(encryptionIdentifier.getType(), encoding);
        xdrOut.flush();

        xdrIn.setCipher(encryptionInitInfo.getDecryptionCipher());
        xdrOut.setCipher(encryptionInitInfo.getEncryptionCipher());

        readOperationResponse(readNextOperation(), null);
    }

    /**
     * Handles the database encryption key callback.
     *
     * @param dbCryptCallback
     *         Database encryption callback plugin
     * @throws IOException
     *         For errors reading data from the socket
     * @throws SQLException
     *         For database errors
     */
    protected final void handleCryptKeyCallback(DbCryptCallback dbCryptCallback) throws IOException, SQLException {
        final DbCryptData serverPluginData = readCryptKeyCallback();
        DbCryptData clientPluginResponse;
        try {
            clientPluginResponse = dbCryptCallback.handleCallback(serverPluginData);
        } catch (Exception e) {
            log.error("Error during database encryption callback, using default empty response", e);
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
            throw new FbExceptionBuilder().nonTransientConnectionException(JaybirdErrorCodes.jb_dbCryptDataError)
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
