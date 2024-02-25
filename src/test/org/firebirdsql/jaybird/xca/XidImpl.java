/*
 * Firebird Open Source JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */

/* Borrowed from
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.jaybird.util.ByteArrayHelper;

import javax.transaction.xa.Xid;
import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This object encapsulates the ID of a transaction. This implementation is immutable and always serializable at
 * runtime.
 *
 * @author Rickard Oberg (rickard.oberg@telkel.com)
 * @author Ole Husgaard
 * @author Mark Rotteveel
 */
public class XidImpl implements Xid, Serializable {

    public static final int JBOSS_FORMAT_ID = 0x0101;
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Hash code of this instance. This is really a sequence number.
     */
    private final int hash;

    /**
     * Global transaction id of this instance.
     * <p>
     * The coding of this class depends on the fact that this variable is initialized in the constructor and never
     * modified. References to this array are never given away, instead a clone is delivered.
     * </p>
     */
    private final byte[] globalId;

    /**
     * Branch qualifier of this instance. This identifies the branch of a transaction.
     */
    private final byte[] branchId;

    /**
     * The host name of this host, followed by a slash.
     * <p>
     * This is used for building globally unique transaction identifiers. It would be safer to use the IP address, but
     * a host name is better for humans to read and will do for now.
     * </p>
     */
    private static final String HOST_NAME;

    /**
     * The next transaction id to use on this host.
     */
    private static final AtomicInteger nextId = new AtomicInteger();

    /**
     * Return a new unique transaction id to use on this host.
     */
    private static int getNextId() {
        return nextId.getAndIncrement();
    }

    // Initialize the hostName class variable.
    static {
        String tempHostName;
        try {
            tempHostName = InetAddress.getLocalHost().getHostName() + "/";
        } catch (UnknownHostException e) {
            tempHostName = "localhost/";
        }
        // Ensure room for 14 digits of serial no.
        if (tempHostName.length() > MAXGTRIDSIZE - 15) {
            tempHostName = tempHostName.substring(0, MAXGTRIDSIZE - 15);
            tempHostName = tempHostName + "/";
        }
        HOST_NAME = tempHostName;
    }

    /**
     * Return a string that describes any Xid instance.
     */
    static String toString(Xid id) {
        if (id == null) return "[NULL Xid]";

        String s = id.getClass().getName();
        s = s.substring(s.lastIndexOf('.') + 1);
        s = s + " [FormatId=" + id.getFormatId() +
                ", GlobalId=" + new String(id.getGlobalTransactionId()).trim() +
                ", BranchQual=" + new String(id.getBranchQualifier()).trim() + "]";

        return s;
    }

    /**
     * Create a new instance.
     */
    public XidImpl() {
        hash = getNextId();
        globalId = (HOST_NAME + hash).getBytes();
        branchId = ByteArrayHelper.emptyByteArray();
    }

    /**
     * Return the global transaction id of this transaction.
     */
    public byte[] getGlobalTransactionId() {
        return globalId.clone();
    }

    /**
     * Return the branch qualifier of this transaction.
     */
    public byte[] getBranchQualifier() {
        if (branchId.length == 0) {
            // Zero length arrays are immutable.
            return branchId;
        } else {
            return branchId.clone();
        }
    }

    /**
     * Return the format identifier of this transaction.
     * <p>
     * The format identifier augments the global id and specifies how the global id and branch qualifier should be
     * interpreted.
     */
    public int getFormatId() {
        // The id we return here should be different from all other transaction
        // implementations.
        // Known IDs are:
        // -1:     Sometimes used to denote a null transaction id.
        // 0:      OSI TP (javadoc states OSI CCR, but that is a bit misleading
        //         as OSI CCR doesn't even have ACID properties. But OSI CCR and
        //         OSI TP do have the same id format.)
        // 1:      Was used by early betas of jBoss.
        // 0x0101: The JBOSS_FORMAT_ID we use here.
        // 0xBB14: Used by JONAS.
        // 0xBB20: Used by JONAS.

        return JBOSS_FORMAT_ID;
    }

    /**
     * Compare for equality.
     * <p>
     * Instances are considered equal if they are both instances of XidImpl, and if they have the same global
     * transaction id and transaction branch qualifier.
     * </p>
     */
    public boolean equals(Object obj) {
        if (obj instanceof XidImpl other && Arrays.equals(globalId, other.globalId)) {
            return Arrays.equals(branchId, other.branchId);
        }
        return false;
    }

    public int hashCode() {
        return hash;
    }

    public String toString() {
        return toString(this);
    }

}
