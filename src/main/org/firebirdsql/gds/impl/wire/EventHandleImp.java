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
/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */


package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.gds.EventHandle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EventHandleImp implements EventHandle {
    
    private int eventCount = -1;
    private String eventName;
    private int eventId = -1;
    private int localId = -1;
    private int internalCount = 0;
    private int previousInternalCount = 0;

    public EventHandleImp(String eventName){
        if (eventName == null){
            throw new NullPointerException();
        }
        this.eventName = eventName;
    }

    public String getEventName(){
        return this.eventName;
    }

    public int getEventCount(){
        return this.eventCount;
    }

    public int getEventId(){
       return this.eventId;
    } 

    public void setEventId(int eventId){
        this.eventId = eventId;
    }

    public void setLocalId(int localId){
        this.localId = localId;
    }

    public int getLocalId(){
        return this.localId;
    }

    public byte [] getParameterBuffer(){
        return new byte[]{};
    }

    public String toString(){
        return "EventHandle: internal id = " 
            + localId 
            + ", external id = " 
            + eventId;
    }

    void setInternalCount(int count){
        this.internalCount = count;
    }

    synchronized void calculateCount(){
        eventCount = internalCount - previousInternalCount;
        previousInternalCount = internalCount;
    }

    byte[] toByteArray() throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        XdrOutputStream xdr = new XdrOutputStream(byteOut);

        byte[] eventNameBytes = this.eventName.getBytes();
        xdr.write(1);   // Event version
        xdr.write(eventNameBytes.length);
        xdr.write(eventNameBytes);

        for (int shift = 0; shift <= 24; shift += 8){
            xdr.write((internalCount >> shift) & 0xff);
        }
        xdr.flush();
        byteOut.flush();

        return byteOut.toByteArray();
    }

}
