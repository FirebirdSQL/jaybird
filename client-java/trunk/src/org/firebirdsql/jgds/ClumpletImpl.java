/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Original developer David Jencks
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Lesser General Public License Version 2.1 or later
 * (the "LGPL"), in which case the provisions of the LGPL are applicable
 * instead of those above.  If you wish to allow use of your
 * version of this file only under the terms of the LGPL and not to
 * allow others to use your version of this file under the MPL,
 * indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by
 * the LGPL.  If you do not delete the provisions above, a recipient
 * may use your version of this file under either the MPL or the
 * LGPL.
 */

package org.firebirdsql.jgds;

import org.firebirdsql.gds.Clumplet;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ClumpletImpl implements Clumplet, Xdrable {

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
