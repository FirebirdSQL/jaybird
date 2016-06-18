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
package org.firebirdsql.gds.impl.wire;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;

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

    public int getTaskIdentifier() {
        return taskIdentifier;
    }
    
    @Override
    public void addArgument(int argumentType, String value) {
        getArgumentsList().add(new StringArgument(argumentType, value) {

            @Override
            int getLength() {
                return super.getLength() + 1;
            }
            
            protected void writeLength(int length, XdrOutputStream outputStream) throws IOException {
                outputStream.write(length);
                outputStream.write(length >> 8);
            }
        });
    }

    @Override
    public void addArgument(int argumentType, int value) {

        getArgumentsList().add(new NumericArgument(argumentType, value) {

            @Override
            int getLength() {
                return 5;
            }
            
            @Override
            protected void writeValue(XdrOutputStream outputStream, int value) throws IOException {
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
                int getLength() {
                    return 9;
                }

                @Override
                protected void writeValue(XdrOutputStream outputStream, long value) throws IOException {
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
    
    public void addArgument(int argumentType, byte value){
        getArgumentsList().add(new NumericArgument(argumentType, value){
            
            @Override
            int getLength() {
                return 2;
            }
            
            @Override
            protected void writeValue(XdrOutputStream outputStream, int value) throws IOException {
                outputStream.write(value);
            }
        });
    }

    /* (non-Javadoc)
     * @see org.firebirdsql.jgds.ParameterBufferBase#write(org.firebirdsql.gds.XdrOutputStream)
     */
    public void write(XdrOutputStream outputStream) throws IOException {
        outputStream.write(taskIdentifier);
        super.write(outputStream);
    }
    
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XdrOutputStream outputStream = new XdrOutputStream(out);
        
        write(outputStream);
        
        outputStream.flush();
        out.flush();
        
        return out.toByteArray();
    }
    
    private int taskIdentifier;
}