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
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.common.FBJUnit4TestBase;

import javax.transaction.xa.Xid;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.firebirdsql.common.FBTestProperties.*;

/**
 * THIS FILE INCLUDES AN XID IMPLEMENTATION FROM THE JBOSS PROJECT
 * www.jboss.org.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 */
public abstract class TestXABase extends FBJUnit4TestBase {

    /**
     * Creates a suitable {@link FBManagedConnectionFactory} for testing.
     * <p>
     * The created MCF is a shared MCF. This is equivalent to using {@code initMcf(true)}.
     * </p>
     *
     * @return connection factory
     */
    public FBManagedConnectionFactory initMcf() {
        return initMcf(true);
    }

    /**
     * Creates a suitable {@link FBManagedConnectionFactory} for testing.
     *
     * @param shared
     *         {@code true} factory is suitable for sharing, {@code false} otherwise
     * @return connection factory
     */
    public FBManagedConnectionFactory initMcf(boolean shared) {
        FBManagedConnectionFactory mcf = createFBManagedConnectionFactory(shared);
        mcf.setDatabaseName(DB_DATASOURCE_URL);
        mcf.setUser(DB_USER);
        mcf.setPassword(DB_PASSWORD);
        mcf.setEncoding(DB_LC_CTYPE);

        return mcf;
    }

    /*Borrowed from
     * JBoss, the OpenSource EJB server
     *
     * Distributable under LGPL license.
     * See terms of license at gnu.org.
     */

    /**
     * This object encapsulates the ID of a transaction.
     * This implementation is immutable and always serializable at runtime.
     *
     * @author Rickard Oberg (rickard.oberg@telkel.com)
     * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
     */
    public static class XidImpl implements Xid, Serializable {

        public static final int JBOSS_FORMAT_ID = 0x0101;
        private static final long serialVersionUID = 1L;

        // Attributes ----------------------------------------------------

        /**
         * Hash code of this instance. This is really a sequence number.
         */
        private int hash;

        /**
         * Global transaction id of this instance.
         * <p>
         * The coding of this class depends on the fact that this variable is
         * initialized in the constructor and never modified. References to
         * this array are never given away, instead a clone is delivered.
         * </p>
         */
        private byte[] globalId;

        /**
         * Branch qualifier of this instance.
         * This identifies the branch of a transaction.
         */
        private byte[] branchId;

        /**
         * The host name of this host, followed by a slash.
         * <p>
         * This is used for building globally unique transaction identifiers.
         * It would be safer to use the IP address, but a host name is better
         * for humans to read and will do for now.
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
        private static synchronized int getNextId() {
            return nextId.getAndIncrement();
        }

        /**
         * Singleton for no branch qualifier.
         */
        private static final byte[] noBranchQualifier = new byte[0];

        /*
         *  Initialize the <code>hostName</code> class variable.
         */
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
            if (id == null)
                return "[NULL Xid]";

            String s = id.getClass().getName();
            s = s.substring(s.lastIndexOf('.') + 1);
            s = s + " [FormatId=" + id.getFormatId() +
                    ", GlobalId=" + new String(id.getGlobalTransactionId()).trim() +
                    ", BranchQual=" + new String(id.getBranchQualifier()).trim() + "]";

            return s;
        }

        // Constructors --------------------------------------------------

        /**
         * Create a new instance.
         */
        public XidImpl() {
            hash = getNextId();
            globalId = (HOST_NAME + hash).getBytes();
            branchId = noBranchQualifier;
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
                return branchId; // Zero length arrays are immutable.
            } else {
                return branchId.clone();
            }
        }

        /**
         * Return the format identifier of this transaction.
         * <p>
         * The format identifier augments the global id and specifies
         * how the global id and branch qualifier should be interpreted.
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
         * Instances are considered equal if they are both instances of XidImpl,
         * and if they have the same global transaction id and transaction
         * branch qualifier.
         */
        public boolean equals(Object obj) {
            if (obj instanceof XidImpl) {
                XidImpl other = (XidImpl) obj;

                if (globalId.length != other.globalId.length || branchId.length != other.branchId.length) {
                    return false;
                }

                for (int i = 0; i < globalId.length; ++i) {
                    if (globalId[i] != other.globalId[i]) {
                        return false;
                    }
                }

                for (int i = 0; i < branchId.length; ++i) {
                    if (branchId[i] != other.branchId[i]) {
                        return false;
                    }
                }

                return true;
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
}
