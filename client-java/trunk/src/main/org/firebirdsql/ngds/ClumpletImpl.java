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

package org.firebirdsql.ngds;

import java.io.IOException;
import java.util.Arrays;
import org.firebirdsql.gds.Clumplet;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;

/**
 * Describe class <code>ClumpletImpl</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class ClumpletImpl 
    implements Clumplet, Serializable
{

	 public Clumplet remove(int type) {
        ClumpletImpl c = this;
        ClumpletImpl result = null;
        if (this.type == type)
            c = this.next;

        while(c != null) {
            if (c.type != type) {
                ClumpletImpl clone = c.getCopy();
                if (result != null)
                    result.append(clone);
                else
                    result = clone;
            }

            c = c.next;
        }

        return result;
    }

	protected ClumpletImpl getCopy() {
        return new ClumpletImpl(this.type, this.content);
    }
    //format of a clumplet:
    //type - 1 byte
    //length - 1 byte
    //contents - length bytes.

    int type;
    byte[] content;
    ClumpletImpl next;

    ClumpletImpl(int type, byte[] content) {
        this.type = type;
        this.content = content;
    }

    ClumpletImpl cloneClumplet() {
        ClumpletImpl newClumplet = new ClumpletImpl(type, content);
        if (next != null) {
            newClumplet.next = next.cloneClumplet();
        }
        return newClumplet;
    }

    //not a very safe implementation, only works for one unchained clumplet.
    public void append(Clumplet c) {
        ClumpletImpl ci = (ClumpletImpl)c;
        if (this.type == ci.type) {
            this.content = ci.content;
        }
        else if (next == null) {
            next = ci;
        }
        else {
            next.append(c);
        }
    }

    public byte[] find(int type)
    {
        if (type == this.type) 
        {
            return content;        
        } // end of if ()
        if (next == null) 
        {
            return null;        
        } // end of if ()
        return next.find(type);
    }

    public String findString(int type)
    {
        if (type == this.type) 
        {
            throw new IllegalStateException("Do not call getStringContent on a non-StringClumplet!");
        } // end of if ()
        if (next == null) 
        {
            return null;        
        } // end of if ()
        return next.findString(type);
    }

    public int getLength() {
        if (next == null) {
            return content.length + 2;
        }
        else {
            return content.length + 2 + next.getLength();
        }
    }


    byte[] toBytes() throws IOException
        {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ClumpletImpl currentCumplet = this;
        while(currentCumplet!=null)
            {
            baos.write(currentCumplet.type);
            baos.write(currentCumplet.content.length);
            baos.write(currentCumplet.content);

            currentCumplet = currentCumplet.next;
            }

        baos.close();

        return baos.toByteArray();
        }



    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof ClumpletImpl)) {
            return false;
        }
        ClumpletImpl c = (ClumpletImpl)o;
        if (type != c.type || !Arrays.equals(content, c.content)) {
            return false; //these have different contents
        }
        if (next != null) {
            return next.equals(c.next);//we have next, compare with c.next
        }
        return (c.next == null); //contents the same, we have no next, == if c has no next.
    }


    public int hashCode() {
        int arrayhash = type;
        for (int i = 0; i< content.length; i++) {
            arrayhash ^= ((int)content[i])<<(8 * (i % 4));
        }
        if (next != null) {
            arrayhash ^= next.hashCode();
        }
        return arrayhash;
    }

}
