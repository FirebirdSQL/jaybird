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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLException;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;

/**
 * Abstract service implementation.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbService<T extends AbstractConnection<IServiceProperties, ? extends FbService>>
        extends AbstractFbAttachment<T> implements FbService {

    private static final Logger log = LoggerFactory.getLogger(AbstractFbService.class);

    protected AbstractFbService(T connection, DatatypeCoder datatypeCoder) {
        super(connection, datatypeCoder);
    }

    @Override
    public final <R> R getServiceInfo(ServiceParameterBuffer serviceParameterBuffer,
            ServiceRequestBuffer serviceRequestBuffer, int bufferLength, InfoProcessor<R> infoProcessor)
            throws SQLException {
        byte[] responseBuffer = getServiceInfo(serviceParameterBuffer, serviceRequestBuffer, bufferLength);
        return infoProcessor.process(responseBuffer);
    }

    protected ServiceRequestBuffer getDescribeServiceRequestBuffer() {
        ServiceRequestBuffer srb = createServiceRequestBuffer();
        srb.addArgument(isc_info_svc_server_version);
        return srb;
    }

    protected InfoProcessor<FbService> getServiceInformationProcessor() {
        return new ServiceInformationProcessor();
    }

    private class ServiceInformationProcessor implements InfoProcessor<FbService> {
        @Override
        public FbService process(byte[] info) throws SQLException {
            boolean debug = log.isDebugEnabled();
            if (info.length == 0) {
                throw new SQLException("Response buffer for service information request is empty");
            }
            if (debug)
                log.debug(String.format("ServiceInformationProcessor.process: first 2 bytes are %04X or: %02X, %02X",
                        iscVaxInteger2(info, 0), info[0], info[1]));
            int len;
            int i = 0;
            while (info[i] != isc_info_end) {
                switch (info[i++]) {
                case isc_info_svc_server_version:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    String firebirdVersion = new String(info, i, len);
                    i += len;
                    setServerVersion(firebirdVersion);
                    if (debug) log.debug("isc_info_firebird_version:" + firebirdVersion);
                    break;
                case isc_info_truncated:
                    if (debug) log.debug("isc_info_truncated ");
                    return AbstractFbService.this;
                default:
                    throw new FbExceptionBuilder().exception(isc_infunk).toSQLException();
                }
            }
            return AbstractFbService.this;
        }
    }
}
