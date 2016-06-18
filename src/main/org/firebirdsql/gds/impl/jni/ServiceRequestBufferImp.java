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
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;

import java.io.ByteArrayOutputStream;

/**
 * ngds implementation for ServiceRequestBufferImp.
 */
class ServiceRequestBufferImp extends ParameterBufferBase implements
        ServiceRequestBuffer {

    /**
     * Every ServiceRequestBuffer has an associated taskIdentifier.
     * 
     * @param taskIdentifier
     */
    ServiceRequestBufferImp(int taskIdentifier) {
        this.taskIdentifier = taskIdentifier;
    }

    @Override
    public void addArgument(int argumentType, String value) {
        getArgumentsList().add(new StringArgument(argumentType, value) {

            @Override
            protected void writeLength(int length,
                    ByteArrayOutputStream outputStream) {
                outputStream.write(length);
                outputStream.write(length >> 8);
            }
        });
    }
    
    @Override
    public void addArgument(int argumentType, int value) {
        getArgumentsList().add(new NumericArgument(argumentType, value) {

            @Override
            protected void writeValue(ByteArrayOutputStream outputStream, int value)  {
                outputStream.write(value);
                outputStream.write(value>>8);
                outputStream.write(value>>16);
                outputStream.write(value>>24);
            }
        });
    }

    @Override
    public void addArgument(int argumentType, long value) {
        boolean isBigIntSpb = argumentType == ISCConstants.isc_spb_rpr_commit_trans_64
                || argumentType == ISCConstants.isc_spb_rpr_rollback_trans_64
                || argumentType == ISCConstants.isc_spb_rpr_recover_two_phase_64;
        if (isBigIntSpb) {
            getArgumentsList().add(new BigIntArgument(argumentType, value) {
                @Override
                protected void writeValue(ByteArrayOutputStream outputStream, long value) {
                    outputStream.write((int) value);
                    outputStream.write((int) (value >> 8));
                    outputStream.write((int) (value >> 16));
                    outputStream.write((int) (value >> 24));
                    outputStream.write((int) (value >> 32));
                    outputStream.write((int) (value >> 40));
                    outputStream.write((int) (value >> 48));
                    outputStream.write((int) (value >> 56));
                }
            });
        } else {
            addArgument(argumentType, (int) value);
        }
    }
    
    public void addArgument(int argumentType, byte value) {
        getArgumentsList().add(new NumericArgument(argumentType, value) {
            
            @Override
            protected void writeValue(ByteArrayOutputStream outputStream,
                    final int value) {
                outputStream.write(value);
            }
        });
    }

    /**
     * Pacakage local method for obtaining buffer suitable for passing to native
     * method.
     * 
     * @return
     */
    byte[] toByteArray() {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.write(taskIdentifier);

        super.writeArgumentsTo(byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }

    // PRIVATE MEMBERS

    private int taskIdentifier;
}
