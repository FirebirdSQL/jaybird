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

import java.io.IOException;
import java.io.InputStream;
import javax.resource.ResourceException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import org.firebirdsql.jgds.XdrInputStream;



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
    FBXid(InputStream rawIn) throws ResourceException
    {
        try 
        {
            XdrInputStream in = new XdrInputStream(rawIn);
            
            if (in.read() != TDR_VERSION)
            {
                throw new FBResourceException("Wrong TDR_VERSION for xid");
            }
            if (in.read() != TDR_XID_FORMAT_ID)
            {
                throw new FBResourceException("Wrong TDR_XID_FORMAT_ID for xid");
            }
            formatId = in.readInt(); 
            if (in.read() != TDR_XID_GLOBAL_ID)
            {
                throw new FBResourceException("Wrong TDR_XID_GLOBAL_ID for xid");
            }
            globalId = in.readBuffer();
            if (in.read() != TDR_XID_BRANCH_ID)
            {
                throw new FBResourceException("Wrong TDR_XID_BRANCH_ID for xid");
            }
            branchId = in.readBuffer();
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
     *  Compare for equality.
     *
     *  Instances are considered equal if they are both instances of XidImpl,
     *  and if they have the same global transaction id and transaction
     *  branch qualifier.
     */
    public boolean equals(Object obj)
    {
        if (obj instanceof Xid) {
            Xid other = (Xid)obj;

            if (formatId != other.getFormatId()) {
                return false;
            }

            byte[] otherGlobalID = other.getGlobalTransactionId();
            byte[] otherBranchID = other.getBranchQualifier();

            if (globalId.length != otherGlobalID.length ||
                 branchId.length != otherBranchID.length) {
                return false;
            }

            for (int i = 0; i < globalId.length; ++i) {
                if (globalId[i] != otherGlobalID[i]) {
                    return false;
                }
            }

            for (int i = 0; i < branchId.length; ++i) {
                if (branchId[i] != otherBranchID[i]) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }


    public String toString()
    {
        return toString(this);
    }

    //package

    int getLength() {
        return 1 + 1 + 4 + 1 + 1 + globalId.length + 1 + 1 + branchId.length;
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
        b[i++] = (byte)globalId.length;
        System.arraycopy(globalId, 0, b, i, globalId.length);
        i += globalId.length;
        b[i++] = (byte)TDR_XID_BRANCH_ID;
        b[i++] = (byte)branchId.length;
        System.arraycopy(branchId, 0, b, i, branchId.length);
        return b;
    }


}

