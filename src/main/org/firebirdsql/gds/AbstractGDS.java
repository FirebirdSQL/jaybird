/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.gds;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;

/**
 * Base class for GDS implementations. This base class allows the GDS implementation
 * to be serialized and deserialized safeley.
 *
 */
public abstract class AbstractGDS implements GDS, Externalizable
    {
    public AbstractGDS()
        {
        }

    public AbstractGDS(GDSType gdsType)
        {
        this.gdsType = gdsType;
        }

    /**
     * Get the type of this <code>GDS</code> implementation. The returned
     * value will be equal to one of the static final fields of
     * {@link GDSType}.
     *
     * @return The type of the current <code>GDS</code> implementation
     * 
     * @deprecated use {@link #getType()} instead. 
     */
    public GDSType getGdsType()
        {
        return getType();
        }
    
    public GDSType getType() {
        return gdsType;
    }

    public void close()
        {
        }


    public void writeExternal(ObjectOutput out) throws IOException
        {
        out.writeObject(gdsType);
        }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
        {
        gdsType = (GDSType) in.readObject();
        }

    public Object readResolve()
        {
        return GDSFactory.getGDSForType(gdsType);
        }


    private GDSType gdsType;
    }
