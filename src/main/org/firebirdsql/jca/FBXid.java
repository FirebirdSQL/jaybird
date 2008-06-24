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

package org.firebirdsql.jca;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.resource.ResourceException;
import javax.transaction.xa.Xid;

//import org.firebirdsql.gds.impl.XdrInputStream;



/**
 * The class <code>FBXid</code> has methods for serializing xids for 
 * firebird use, and reading them back into instances of itelf.  It is
 * a key component in adapting xa semantics and recovery to firebird
 * native operations and data format.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
class FBXid implements Xid {
   // Constants ---------------------------------------------------
   //Constants from alice.h
   public static final int TDR_VERSION = 1;
   public static final int TDR_HOST_SITE = 1;
   public static final int TDR_DATABASE_PATH = 2;
   public static final int TDR_TRANSACTION_ID = 3;
   public static final int TDR_REMOTE_SITE = 4;
   //new constants for xid encoding
   public static final int TDR_XID_FORMAT_ID = 5;
   public static final int TDR_XID_GLOBAL_ID = 6;
   public static final int TDR_XID_BRANCH_ID = 4;


   // Attributes ----------------------------------------------------
    private int formatId;

   /**
    *  Global transaction id of this instance.
    */
   private byte[] globalId;

   /**
    *  Branch qualifier of this instance.
    *  This identifies the branch of a transaction.
    */
   private byte[] branchId;
   
   private long firebirdTransactionId;


   /**
    *  Return a string that describes any Xid instance.
    */
   static String toString(Xid id) {
      if (id == null)
         return "[NULL Xid]";

      String s = id.getClass().getName();
      s = s.substring(s.lastIndexOf('.') + 1);
      s = s + " [FormatId=" + id.getFormatId() +
              ", GlobalId=" + new String(id.getGlobalTransactionId()).trim() +
              ", BranchQual=" + new String(id.getBranchQualifier()).trim()+"]";

      return s;
   }

   // Constructors --------------------------------------------------

   /**
    *  Create a new instance copying an existing one.
    */
    public FBXid(Xid xid) {
        formatId = xid.getFormatId();
        globalId = xid.getGlobalTransactionId();
        branchId = xid.getBranchQualifier();
    }

    /**
     * Creates a new <code>FBXid</code> instance from the byte representation
     * supplied. This is called by recover to reconstruct an xid 
     * from the toBytes() representation.
     *
     * @param bytes a <code>byte[]</code> value
     * @exception ResourceException if an error occurs
     */
    FBXid(InputStream rawIn, long firebirdTransactionId) throws ResourceException
    {
        this.firebirdTransactionId = firebirdTransactionId;
        
        try 
        {
            if (read(rawIn) != TDR_VERSION)
            {
                throw new FBIncorrectXidException("Wrong TDR_VERSION for xid");
            }
            if (read(rawIn) != TDR_XID_FORMAT_ID)
            {
                throw new FBIncorrectXidException("Wrong TDR_XID_FORMAT_ID for xid");
            }
            formatId = readInt(rawIn); 
            if (read(rawIn) != TDR_XID_GLOBAL_ID)
            {
                throw new FBIncorrectXidException("Wrong TDR_XID_GLOBAL_ID for xid");
            }
            globalId = readBuffer(rawIn);
            if (read(rawIn) != TDR_XID_BRANCH_ID)
            {
                throw new FBIncorrectXidException("Wrong TDR_XID_BRANCH_ID for xid");
            }
            branchId = readBuffer(rawIn);
        } 
        catch (IOException ioe) 
        {
            throw new FBResourceException("IOException: " + ioe);            
        } // end of try-catch
        
    }


    // Public --------------------------------------------------------



    // Xid implementation --------------------------------------------

    /**
     *  Return the global transaction id of this transaction.
     */
    public byte[] getGlobalTransactionId()
    {
       return (byte[])globalId.clone();
    }

    /**
     *  Return the branch qualifier of this transaction.
     */
    public byte[] getBranchQualifier()
    {
        return (byte[])branchId.clone();
    }

    /**
     *  Return the format identifier of this transaction.
     *
     *  The format identifier augments the global id and specifies
     *  how the global id and branch qualifier should be interpreted.
     */
    public int getFormatId() {
       return formatId;
    }

    /**
     * Return Firebird transaction ID.
     * 
     * @return Firebird transaction ID or 0 if no is available.
     */
    public long getFirebirdTransactionId() {
        return firebirdTransactionId;
    }
    
    /**
     *  Compare for equality.
     *
     *  Instances are considered equal if they are both instances of XidImpl,
     *  and if they have the same global transaction id and transaction
     *  branch qualifier.
     */
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Xid)) 
            return false;
        
            Xid other = (Xid)obj;

            boolean result = true;
            
            result &= formatId == other.getFormatId();
            
            byte[] otherGlobalID = other.getGlobalTransactionId();
            byte[] otherBranchID = other.getBranchQualifier();

            result &= Arrays.equals(globalId, otherGlobalID);
            result &= Arrays.equals(branchId, otherBranchID);
            
            return result;
    }


    public String toString()
    {
        return toString(this);
    }

    //package

    int getLength() {
        return 1 + 1 + 4 + 1 + 4 + globalId.length + 1 + 4 + branchId.length;
    }

    byte[] toBytes() {
        byte[] b = new byte[getLength()];
        int i = 0;
        b[i++] = (byte)TDR_VERSION;
        b[i++] = (byte)TDR_XID_FORMAT_ID;
        b[i++] = (byte)((formatId >>> 24) & 0xff);
        b[i++] = (byte)((formatId >>> 16) & 0xff);
        b[i++] = (byte)((formatId >>>  8) & 0xff);
        b[i++] = (byte)((formatId >>>  0) & 0xff);
        b[i++] = (byte)TDR_XID_GLOBAL_ID;
        b[i++] = (byte)((globalId.length >>> 24) & 0xff);
        b[i++] = (byte)((globalId.length >>> 16) & 0xff);
        b[i++] = (byte)((globalId.length >>>  8) & 0xff);
        b[i++] = (byte)((globalId.length >>>  0) & 0xff);
        System.arraycopy(globalId, 0, b, i, globalId.length);
        i += globalId.length;
        b[i++] = (byte)TDR_XID_BRANCH_ID;
        b[i++] = (byte)((branchId.length >>> 24) & 0xff);
        b[i++] = (byte)((branchId.length >>> 16) & 0xff);
        b[i++] = (byte)((branchId.length >>>  8) & 0xff);
        b[i++] = (byte)((branchId.length >>>  0) & 0xff);
        System.arraycopy(branchId, 0, b, i, branchId.length);
        return b;
    }

    private int read(InputStream in) throws IOException {
        return in.read();
    }
    
    private int readInt(InputStream in) throws IOException {
        return (read(in) << 24) | (read(in) << 16) | (read(in) << 8) | (read(in) << 0);
    }
    
    private byte[] readBuffer(InputStream in) throws IOException {
        int len = readInt(in);
        byte[] buffer = new byte[len];

        readFully(in, buffer, 0, len);
        
        return buffer;
    }
    
    private void readFully(InputStream in, byte[] buffer, int offset, int length) throws IOException {
        if (length == 0)
            return;
        
        int counter = 0;
        
        do {
            counter = in.read(buffer, offset, length);
            if (counter == -1 && length != 0)
                throw new EOFException();
            
            offset += counter;
            length -= counter;
            
        } while(length > 0);
    }
}

