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

import org.firebirdsql.gds.Clumplet;

/**
 * StringClumplet.java
 *
 *
 * Created: Thu Oct 17 16:34:38 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public final class StringClumplet extends ClumpletImpl 
{

    private String stringContent;

    StringClumplet(int type, String content) 
    {
        super(type, content.getBytes());
        this.stringContent = content;
    }

    ClumpletImpl cloneClumplet() 
    {
        ClumpletImpl newClumplet = new StringClumplet(type, stringContent);
        if (next != null) {
            newClumplet.next = next.cloneClumplet();
        }
        return newClumplet;        
    }

    public String findString(int type)
    {
        if (type == this.type) 
        {
            return stringContent;
        } // end of if ()
        return super.findString(type);
    }

    public void append(Clumplet c) {
        ClumpletImpl ci = (ClumpletImpl)c;
        if (this.type == ci.type) {
            this.content = ci.content;
            this.stringContent = ((StringClumplet)ci).stringContent;
        }
        else if (next == null) {
            next = ci;
        }
        else {
            next.append(c);
        }
    }


    
}// StringClumplet
