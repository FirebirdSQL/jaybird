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
package org.firebirdsql.jca;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.Parameter;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;
import org.firebirdsql.gds.impl.wire.Xdrable;

import javax.resource.cci.ConnectionSpec;
import javax.resource.spi.ConnectionRequestInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;

/**
 * The class <code>FBConnectionRequestInfo</code> holds a clumplet that is
 * used to store and transfer connection-specific information such as user,
 * password, and other dpb information..
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 2.0
 */
public class FBConnectionRequestInfo implements DatabaseParameterBufferExtension, ConnectionRequestInfo,
        ConnectionSpec, Serializable {

    private final DatabaseParameterBuffer dpb;

    public FBConnectionRequestInfo(DatabaseParameterBuffer dpb) {
        this.dpb = dpb;
    }

    /**
     * Perform a deep copy of this object, returning the copied instance.
     *
     * @return A deep-copied copy of this FBConnectionRequestInfo object
     */
    @Override
    public DatabaseParameterBuffer deepCopy() {
        return new FBConnectionRequestInfo(dpb.deepCopy());
    }

    /**
     * Get the underlying Database Parameter Buffer for this object.
     *
     * @return The underlying dpb for this object
     */
    public DatabaseParameterBuffer getDpb() {
        return dpb;
    }

    @Override
    public void addArgument(int argumentType, byte[] content) {
        dpb.addArgument(argumentType, content);
    }

    @Override
    public void addArgument(int argumentType, int value) {
        dpb.addArgument(argumentType, value);
    }

    @Override
    public void addArgument(int argumentType, String value) {
        dpb.addArgument(argumentType, value);
    }

    @Override
    public void addArgument(int argumentType, String value, Encoding encoding) {
        dpb.addArgument(argumentType, value, encoding);
    }

    @Override
    public int getType() {
        return dpb.getType();
    }

    @Override
    public void addArgument(int argumentType) {
        dpb.addArgument(argumentType);
    }

    @Override
    public int getArgumentAsInt(int argumentType) {
        return dpb.getArgumentAsInt(argumentType);
    }

    @Override
    public String getArgumentAsString(int argumentType) {
        return dpb.getArgumentAsString(argumentType);
    }

    @Override
    public boolean hasArgument(int argumentType) {
        return dpb.hasArgument(argumentType);
    }

    @Override
    public void removeArgument(int argumentType) {
        dpb.removeArgument(argumentType);
    }

    @Override
    public DatabaseParameterBuffer removeExtensionParams() {
        if (dpb instanceof DatabaseParameterBufferExtension)
            return ((DatabaseParameterBufferExtension) dpb).removeExtensionParams();
        else
            return dpb;
    }

    @Override
    public Iterator<Parameter> iterator() {
        return dpb.iterator();
    }

    @Override
    public void writeArgumentsTo(OutputStream outputStream) throws IOException {
        dpb.writeArgumentsTo(outputStream);
    }

    @Override
    public Xdrable toXdrable() {
        return dpb.toXdrable();
    }

    @Override
    public byte[] toBytes() {
        return dpb.toBytes();
    }

    @Override
    public byte[] toBytesWithType() {
        return dpb.toBytesWithType();
    }

    @Override
    public int size() {
        return dpb.size();
    }

    public void setUserName(String userName) {
        removeArgument(DatabaseParameterBufferExtension.USER_NAME);
        if (userName != null)
            addArgument(DatabaseParameterBufferExtension.USER_NAME, userName);
    }

    public void setPassword(String password) {
        removeArgument(DatabaseParameterBufferExtension.PASSWORD);
        if (password != null)
            addArgument(DatabaseParameterBufferExtension.PASSWORD, password);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof FBConnectionRequestInfo))
            return false;

        return this.dpb.equals(((FBConnectionRequestInfo) obj).dpb);
    }

    public int hashCode() {
        return dpb.hashCode();
    }
}
