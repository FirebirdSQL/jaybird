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

package org.firebirdsql.jgds;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import org.firebirdsql.gds.Clumplet;
import java.io.Serializable;

/**
 * Describe class <code>ClumpletImpl</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class ClumpletImpl 
    implements Clumplet, Xdrable, Serializable 
{

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

    ClumpletImpl(ClumpletImpl c) {
        this.type = c.type;
        this.content = c.content;
        if (c.next != null) {
            this.next = new ClumpletImpl(c.next);
        }
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

    public int getLength() {
        if (next == null) {
            return content.length + 2;
        }
        else {
            return content.length + 2 + next.getLength();
        }
    }

    //XDRable
    public void write(XdrOutputStream out) throws IOException{
        out.write(type);
        out.write(content.length);
        out.write(content);
        if (next != null) {
            next.write(out);
        }
    }

    //XDRable
    public void read(XdrInputStream in, int length) {}


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
