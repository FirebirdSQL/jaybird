/*
 * $Id$
 *
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
package org.firebirdsql.gds.impl;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.argument.NumericArgument;
import org.firebirdsql.gds.impl.argument.StringArgument;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Implementation of ServiceRequestBufferImp.
 */
public class ServiceRequestBufferImp extends ParameterBufferBase implements ServiceRequestBuffer {

    /**
     * Every ServiceRequestBuffer has an associated taskIdentifier.
     *
     * @param taskIdentifier
     *         Service request task
     */
    public ServiceRequestBufferImp(int taskIdentifier) {
        super(ISCConstants.isc_spb_current_version, new byte[] { (byte) taskIdentifier });
    }

    @Override
    public void addArgument(int argumentType, String value) {
        getArgumentsList().add(new ServiceStringArgument(argumentType, value));
    }

    @Override
    public void addArgument(int argumentType, String value, Encoding encoding) {
        getArgumentsList().add(new ServiceStringArgument(argumentType, value, encoding));
    }

    @Override
    public void addArgument(int argumentType, int value) {

        getArgumentsList().add(new NumericArgument(argumentType, value) {

            @Override
            public int getLength() {
                return 5;
            }

            @Override
            protected void writeValue(OutputStream outputStream, int value) throws IOException {
                outputStream.write(value);
                outputStream.write(value >> 8);
                outputStream.write(value >> 16);
                outputStream.write(value >> 24);
            }
        });
    }

    @Override
    public void addArgument(int argumentType, byte value) {
        getArgumentsList().add(new NumericArgument(argumentType, value) {

            @Override
            public int getLength() {
                return 2;
            }

            @Override
            protected void writeValue(OutputStream outputStream, int value) throws IOException {
                outputStream.write(value);
            }
        });
    }

    private static final class ServiceStringArgument extends StringArgument {

        @Deprecated
        public ServiceStringArgument(int argumentType, String value) {
            super(argumentType, value);
        }

        public ServiceStringArgument(int argumentType, String value, Encoding encoding) {
            super(argumentType, value, encoding);
        }

        @Override
        public int getLength() {
            return super.getLength() + 1;
        }

        @Override
        protected void writeLength(int length, OutputStream outputStream) throws IOException {
            outputStream.write(length);
            outputStream.write(length >> 8);
        }

        @Override
        protected int getMaxSupportedLength() {
            // TODO Check if this might be signed
            return 65535;
        }
    }
}