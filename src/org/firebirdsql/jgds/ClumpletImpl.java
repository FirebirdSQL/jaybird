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

public class ClumpletImpl implements Clumplet, Xdrable {
    
    int type;
    byte[] content;
    ClumpletImpl next;
    
    ClumpletImpl(int type, byte[] content) {
        this.type = type;
        this.content = content;
//        this.length = content.length + 2; //+1 for type byte, +1 for length byte
    }

    public void append(Clumplet c) {
        if (next == null) {
            next = (ClumpletImpl)c;
        }
        else {
            next.append(c);
        }
    }
    
    public int getLength() {
        if (next == null) {
            return content.length + 2;
        }
        else {
            return content.length + 2 + next.getLength();
        }
    }
    
    
    public void write(XdrOutputStream out) throws IOException{
        out.write(type);
        out.write(content.length);
        out.write(content);
        if (next != null) {
            next.write(out);
        }
    }
    
    public void read(XdrInputStream in, int length) {}
    
}
