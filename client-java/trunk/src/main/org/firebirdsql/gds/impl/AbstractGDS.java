/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds.impl;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;

import org.firebirdsql.gds.GDS;

/**
 * Base class for GDS implementations. This base class allows the GDS
 * implementation to be serialized and deserialized safeley.
 * 
 */
public abstract class AbstractGDS implements GDS, Externalizable {

    public AbstractGDS() {
    }

    public AbstractGDS(GDSType gdsType) {
        this.gdsType = gdsType;
    }

    /**
     * Get the type of this <code>GDS</code> implementation. The returned
     * value will be equal to one of the static final fields of {@link GDSType}.
     * 
     * @return The type of the current <code>GDS</code> implementation
     * 
     * @deprecated use {@link #getType()}instead.
     */
    @Deprecated
    public GDSType getGdsType() {
        return getType();
    }

    /**
     * Get type of this instance.
     * 
     * @return instance of {@link GDSType}.
     */
    public GDSType getType() {
        return gdsType;
    }

    /**
     * Close this instance. This method can be used to perform final cleanup of
     * the GDS instance when the wrapping component is closed/stopped.
     */
    public void close() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(gdsType);
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        gdsType = (GDSType) in.readObject();
    }

    public Object readResolve() {
        return GDSFactory.getGDSForType(gdsType);
    }

    private GDSType gdsType;
}
