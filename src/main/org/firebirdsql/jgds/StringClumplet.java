/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
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

public class StringClumplet extends ClumpletImpl 
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
