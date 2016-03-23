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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.wire.FbWireAttachment;
import org.firebirdsql.gds.ng.wire.FbWireOperations;
import org.firebirdsql.gds.ng.wire.GenericResponse;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;
import org.firebirdsql.gds.ng.wire.version11.V11WireOperations;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V13WireOperations extends V11WireOperations {

    private static final Logger log = LoggerFactory.getLogger(V13WireOperations.class);

    public V13WireOperations(WireConnection<?, ?> connection,
            WarningMessageCallback defaultWarningMessageCallback, Object syncObject) {
        super(connection, defaultWarningMessageCallback, syncObject);
    }

    @Override
    public void authReceiveResponse(FbWireAttachment.AcceptPacket acceptPacket,
            FbWireOperations.ProcessAttachCallback processAttachCallback) throws SQLException, IOException {
        assert acceptPacket == null || acceptPacket.operation == op_cond_accept
                : "Unexpected operation in AcceptPacket";
        final XdrInputStream xdrIn = getXdrIn();
        final XdrOutputStream xdrOut = getXdrOut();
        final ClientAuthBlock clientAuthBlock = getClientAuthBlock();
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
                    pluginName = xdrIn.readString(getEncoding()); //p_name
                    xdrIn.readBuffer(); // p_list (ignore?)
                    addServerKeys(xdrIn.readBuffer()); // p_keys
                    log.debug(String.format("authReceiveResponse: cont_auth data=%d pluginName=%d '%s'",
                            data.length, pluginName.length(), pluginName));
                    break;
                case op_cond_accept:
                    // Note this is the equivalent of handling the acceptPacket != null above
                    xdrIn.readInt(); // p_acpt_version
                    xdrIn.readInt(); // p_acpt_architecture
                    xdrIn.readInt(); // p_acpt_type
                    data = xdrIn.readBuffer(); // p_acpt_data
                    pluginName = xdrIn.readString(getEncoding()); // p_acpt_plugin
                    xdrIn.readInt(); // p_acpt_authenticated
                    addServerKeys(xdrIn.readBuffer()); //p_acpt_keys
                    log.debug(String.format("authReceiveResponse: cond_accept data=%d pluginName=%d '%s'",
                            data.length, pluginName.length(), pluginName));
                    // TODO handle compression
                    break;

                case op_response:
                    GenericResponse response = (GenericResponse) readOperationResponse(operation, null);
                    clientAuthBlock.setAuthComplete(true);
                    processAttachCallback.processAttachResponse(response);

                    // TODO equivalent of cBlock.tryNewKeys(port);
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
            xdrOut.writeString(clientAuthBlock.getCurrentPluginName(), getEncoding()); // p_name
            if (clientAuthBlock.isFirstTime()) {
                xdrOut.writeString(clientAuthBlock.getPluginNames(), getEncoding()); // p_list
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
}
