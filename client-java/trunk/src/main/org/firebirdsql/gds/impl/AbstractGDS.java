/*
 * $Id$
 * 
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
package org.firebirdsql.gds.impl;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.IscDbHandle;
import org.firebirdsql.gds.IscStmtHandle;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Base class for GDS implementations. This base class allows the GDS
 * implementation to be serialized and deserialized safely.
 */
public abstract class AbstractGDS implements GDS, Externalizable {

    private static Logger log = LoggerFactory.getLogger(AbstractGDS.class, false);

    private GDSType gdsType;
    
    protected static final byte[] DESCRIBE_DATABASE_INFO_BLOCK = new byte[] {
        ISCConstants.isc_info_db_sql_dialect,
        ISCConstants.isc_info_firebird_version,
        ISCConstants.isc_info_ods_version,
        ISCConstants.isc_info_ods_minor_version,
        ISCConstants.isc_info_implementation,
        ISCConstants.isc_info_db_class, 
        ISCConstants.isc_info_base_level,
        ISCConstants.isc_info_end };

    public AbstractGDS() {
    }

    public AbstractGDS(GDSType gdsType) {
        this.gdsType = gdsType;
    }

    /**
     * Get type of this instance.
     * 
     * @return instance of {@link GDSType}.
     */
    public GDSType getType() {
        return gdsType;
    }

    /**
     * Close this instance. This method can be used to perform final cleanup of
     * the GDS instance when the wrapping component is closed/stopped.
     */
    public void close() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(gdsType);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        gdsType = (GDSType) in.readObject();
    }

    public Object readResolve() {
        return GDSFactory.getGDSForType(gdsType);
    }

    private static final byte[] stmtInfo = new byte[] { 
    	    ISCConstants.isc_info_sql_records,
            ISCConstants.isc_info_sql_stmt_type, 
            ISCConstants.isc_info_end };

    private static final int INFO_SIZE = 128;

    public void getSqlCounts(final IscStmtHandle stmt_handle) throws GDSException {
        stmt_handle.setInsertCount(0);
        stmt_handle.setUpdateCount(0);
        stmt_handle.setDeleteCount(0);
        stmt_handle.setSelectCount(0);

        final byte[] buffer = iscDsqlSqlInfo(stmt_handle, stmtInfo, INFO_SIZE);
        int pos = 0;
        int type;
        while ((type = buffer[pos++]) != ISCConstants.isc_info_end) {
            int infoLength = iscVaxInteger2(buffer, pos);
            pos += 2;
            switch (type) {
            case ISCConstants.isc_info_sql_records:
                int countLength;
                int t;
                while ((t = buffer[pos++]) != ISCConstants.isc_info_end) {
                    countLength = iscVaxInteger2(buffer, pos);
                    pos += 2;
                    switch (t) {
                    case ISCConstants.isc_info_req_insert_count:
                        stmt_handle.setInsertCount(iscVaxInteger(buffer, pos, countLength));
                        break;
                    case ISCConstants.isc_info_req_update_count:
                        stmt_handle.setUpdateCount(iscVaxInteger(buffer, pos, countLength));
                        break;
                    case ISCConstants.isc_info_req_delete_count:
                        stmt_handle.setDeleteCount(iscVaxInteger(buffer, pos, countLength));
                        break;
                    case ISCConstants.isc_info_req_select_count:
                        stmt_handle.setSelectCount(iscVaxInteger(buffer, pos, countLength));
                        break;
                    default:
                        break;
                    }
                    pos += countLength;
                }
                break;
            case ISCConstants.isc_info_sql_stmt_type:
                stmt_handle.setStatementType(iscVaxInteger(buffer, pos, infoLength));
                pos += infoLength;
                break;
            default:
                pos += infoLength;
                break;
            }
        }
    }

    public int iscVaxInteger(final byte[] buffer, int pos, int length) {
        int value = 0;
        int shift = 0;

        int i = pos;
        while (--length >= 0) {
            value += (buffer[i++] & 0xff) << shift;
            shift += 8;
        }
        return value;
    }

    public long iscVaxLong(final byte[] buffer, int pos, int length) {
        long value = 0;
        int shift = 0;

        int i = pos;
        while (--length >= 0) {
            value += (buffer[i++] & 0xffL) << shift;
            shift += 8;
        }
        return value;
    }

    /**
     * Implementation of {@link #iscVaxInteger(byte[], int, int)} specifically
     * for two-byte integers.
     * 
     * @param buffer
     *            The byte array from which the integer is to be retrieved
     * @param pos
     *            The offset starting position from which to start retrieving
     *            byte values
     * @return The integer value retrieved from the bytes
     */
    public int iscVaxInteger2(final byte[] buffer, final int pos) {
    	// Small performance benefit over generic iscVaxInteger
        return (buffer[pos] & 0xff) | ((buffer[pos + 1] & 0xff) << 8);
    }

    /**
     * Parse database info returned after attach. This method assumes that it is
     * not truncated.
     * 
     * @param info
     *            information returned by isc_database_info call
     * @param handle
     *            isc_db_handle to set connection parameters
     * @throws GDSException
     *             if something went wrong :))
     */
    protected void parseAttachDatabaseInfo(final byte[] info, final IscDbHandle handle) throws GDSException {
        boolean debug = log != null && log.isDebugEnabled();
        // TODO: Check if info will never be empty
        //      if (info.length == 0) {
        //            throw new GDSException(ISCConstants.isc_dsql_sqlda_err);
        //        }
        if (debug)
            log.debug("parseDatabaseInfo: first 2 bytes are " + 
                    iscVaxInteger2(info, 0) + " or: " + info[0] + ", " + info[1]);
        int value = 0;
        int len = 0;
        int i = 0;
        while (info[i] != ISCConstants.isc_info_end) {
            switch (info[i++]) {
            case ISCConstants.isc_info_db_sql_dialect:
                len = iscVaxInteger2(info, i);
                i += 2;
                value = iscVaxInteger(info, i, len);
                i += len;
                handle.setDialect(value);
                if (debug) log.debug("isc_info_db_sql_dialect:" + value);
                break;
            case ISCConstants.isc_info_ods_version:
                len = iscVaxInteger2(info, i);
                i += 2;
                value = iscVaxInteger(info, i, len);
                i += len;
                handle.setODSMajorVersion(value);
                if (debug) log.debug("isc_info_ods_version:" + value);
                break;
            case ISCConstants.isc_info_ods_minor_version:
                len = iscVaxInteger2(info, i);
                i += 2;
                value = iscVaxInteger(info, i, len);
                i += len;
                handle.setODSMinorVersion(value);
                if (debug) log.debug("isc_info_ods_minor_version:" + value);
                break;
            case ISCConstants.isc_info_firebird_version:
                len = iscVaxInteger2(info, i);
                i += 2;
                byte[] fb_vers = new byte[len - 2];
                System.arraycopy(info, i + 2, fb_vers, 0, len - 2);
                i += len;
                String fb_versS = new String(fb_vers);
                handle.setVersion(fb_versS);
                if (debug) log.debug("isc_info_firebird_version:" + fb_versS);
                break;
            case ISCConstants.isc_info_implementation:
                len = iscVaxInteger2(info, i);
                i += 2;
                byte[] impl = new byte[len - 2];
                System.arraycopy(info, i + 2, impl, 0, len - 2);
                i += len;
                break;
            case ISCConstants.isc_info_db_class:
                len = iscVaxInteger2(info, i);
                i += 2;
                byte[] db_class = new byte[len - 2];
                System.arraycopy(info, i + 2, db_class, 0, len - 2);
                i += len;
                break;
            case ISCConstants.isc_info_base_level:
                len = iscVaxInteger2(info, i);
                i += 2;
                byte[] base_level = new byte[len - 2];
                System.arraycopy(info, i + 2, base_level, 0, len - 2);
                i += len;
                break;
            case ISCConstants.isc_info_truncated:
                if (debug) log.debug("isc_info_truncated ");
                return;
            default:
                throw new GDSException(ISCConstants.isc_dsql_sqlda_err);
            }
        }
    }
}
