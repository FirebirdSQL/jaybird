/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.gds;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ng.FbExceptionBuilder;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Arrays;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_clumpletReaderUsageError;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_invalidClumpletStructure;

/**
 * Reader for clumplets, similar to the implementation {@code ClumpletReader.cpp}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class ClumpletReader {

    private final Kind kind;
    private final byte[] buffer;
    private int spbState; // Reflects state of spb parser/writer
    private int position;

    public ClumpletReader(Kind kind, byte[] buffer) {
        this.kind = kind;
        this.buffer = buffer;
    }

    public boolean isTagged() {
        switch (kind) {
        case Tpb:
        case Tagged:
        case WideTagged:
        case SpbAttach:
            return true;
        }

        return false;
    }

    public int getBufferTag() throws SQLException {
        switch (kind) {
        case Tpb:
        case Tagged:
        case WideTagged:
            if (buffer.length == 0) {
                throw invalidStructure("empty buffer");
            }
            return buffer[0];
        case SpbStart:
        case UnTagged:
        case WideUnTagged:
        case SpbSendItems:
        case SpbReceiveItems:
            throw usageMistake("buffer is not tagged");
        case SpbAttach:
            if (buffer.length == 0) {
                throw invalidStructure("empty buffer");
            }
            switch (buffer[0]) {
            case isc_spb_version1:
                // This is old SPB format, it's almost like DPB -
                // buffer's tag is the first byte.
                return buffer[0];
            case isc_spb_version:
                // Buffer's tag is the second byte
                if (buffer.length == 1) {
                    throw invalidStructure("buffer too short (1 byte)");
                }
                return buffer[1];
            case isc_spb_version3:
                // This is wide SPB attach format - buffer's tag is the first byte.
                return buffer[0];
            default:
                throw invalidStructure("spb in service attach should begin with isc_spb_version1 or isc_spb_version");
            }
        default:
            throw new SQLException("Unexpected clumplet kind: " + kind);
        }
    }

    public ClumpletType getClumpletType(byte tag) throws SQLException {
        switch (kind) {
        case Tagged:
        case UnTagged:
        case SpbAttach:
            return ClumpletType.TraditionalDpb;
        case WideTagged:
        case WideUnTagged:
            return ClumpletType.Wide;
        case Tpb:
            switch (tag) {
            case isc_tpb_lock_write:
            case isc_tpb_lock_read:
            case isc_tpb_lock_timeout:
                return ClumpletType.TraditionalDpb;
            }
            return ClumpletType.SingleTpb;
        case SpbSendItems:
            switch (tag) {
            case isc_info_svc_auth_block:
                return ClumpletType.Wide;
            case isc_info_end:
            case isc_info_truncated:
            case isc_info_error:
            case isc_info_data_not_ready:
            case isc_info_length:
            case isc_info_flag_end:
                return ClumpletType.SingleTpb;
            }
            return ClumpletType.StringSpb;
        case SpbReceiveItems:
            return ClumpletType.SingleTpb;
        case SpbStart:
            switch (tag) {
            case isc_spb_auth_block:
            case isc_spb_trusted_auth:
            case isc_spb_auth_plugin_name:
            case isc_spb_auth_plugin_list:
                return ClumpletType.Wide;
            }
            switch (spbState) {
            case 0:
                return ClumpletType.SingleTpb;
            case isc_action_svc_backup:
            case isc_action_svc_restore:
                switch (tag) {
                case isc_spb_bkp_file:
                case isc_spb_dbname:
                case isc_spb_res_fix_fss_data:
                case isc_spb_res_fix_fss_metadata:
                case isc_spb_bkp_stat:
                    return ClumpletType.StringSpb;
                case isc_spb_bkp_factor:
                case isc_spb_bkp_length:
                case isc_spb_res_length:
                case isc_spb_res_buffers:
                case isc_spb_res_page_size:
                case isc_spb_options:
                case isc_spb_verbint:
                    return ClumpletType.IntSpb;
                case isc_spb_verbose:
                    return ClumpletType.SingleTpb;
                case isc_spb_res_access_mode:
                    return ClumpletType.ByteSpb;
                }
                throw invalidStructure("unknown parameter for backup/restore");
            case isc_action_svc_repair:
                switch (tag) {
                case isc_spb_dbname:
                    return ClumpletType.StringSpb;
                case isc_spb_options:
                case isc_spb_rpr_commit_trans:
                case isc_spb_rpr_rollback_trans:
                case isc_spb_rpr_recover_two_phase:
                    return ClumpletType.IntSpb;
                }
                throw invalidStructure("unknown parameter for repair");
            case isc_action_svc_add_user:
            case isc_action_svc_delete_user:
            case isc_action_svc_modify_user:
            case isc_action_svc_display_user:
            case isc_action_svc_display_user_adm:
            case isc_action_svc_set_mapping:
            case isc_action_svc_drop_mapping:
                switch (tag) {
                case isc_spb_dbname:
                case isc_spb_sql_role_name:
                case isc_spb_sec_username:
                case isc_spb_sec_password:
                case isc_spb_sec_groupname:
                case isc_spb_sec_firstname:
                case isc_spb_sec_middlename:
                case isc_spb_sec_lastname:
                    return ClumpletType.StringSpb;
                case isc_spb_sec_userid:
                case isc_spb_sec_groupid:
                case isc_spb_sec_admin:
                    return ClumpletType.IntSpb;
                }
                throw invalidStructure("unknown parameter for security database operation");
            case isc_action_svc_properties:
                switch (tag) {
                case isc_spb_dbname:
                    return ClumpletType.StringSpb;
                case isc_spb_prp_page_buffers:
                case isc_spb_prp_sweep_interval:
                case isc_spb_prp_shutdown_db:
                case isc_spb_prp_deny_new_attachments:
                case isc_spb_prp_deny_new_transactions:
                case isc_spb_prp_set_sql_dialect:
                case isc_spb_options:
                case isc_spb_prp_force_shutdown:
                case isc_spb_prp_attachments_shutdown:
                case isc_spb_prp_transactions_shutdown:
                    return ClumpletType.IntSpb;
                case isc_spb_prp_reserve_space:
                case isc_spb_prp_write_mode:
                case isc_spb_prp_access_mode:
                case isc_spb_prp_shutdown_mode:
                case isc_spb_prp_online_mode:
                    return ClumpletType.ByteSpb;
                }
                throw invalidStructure("unknown parameter for setting database properties");
//		    case isc_action_svc_add_license:
//		    case isc_action_svc_remove_license:
            case isc_action_svc_db_stats:
                switch (tag) {
                case isc_spb_dbname:
                case isc_spb_command_line:
                case isc_spb_sts_table:
                    return ClumpletType.StringSpb;
                case isc_spb_options:
                    return ClumpletType.IntSpb;
                }
                throw invalidStructure("unknown parameter for getting statistics");
            case isc_action_svc_get_ib_log:
                throw invalidStructure("unknown parameter for getting log");
            case isc_action_svc_nbak:
            case isc_action_svc_nrest:
                switch (tag) {
                case isc_spb_nbk_file:
                case isc_spb_nbk_direct:
                case isc_spb_dbname:
                    return ClumpletType.StringSpb;
                case isc_spb_nbk_level:
                case isc_spb_options:
                    return ClumpletType.IntSpb;
                }
                throw invalidStructure("unknown parameter for nbackup");
            case isc_action_svc_trace_start:
            case isc_action_svc_trace_stop:
            case isc_action_svc_trace_suspend:
            case isc_action_svc_trace_resume:
                switch (tag) {
                case isc_spb_trc_cfg:
                case isc_spb_trc_name:
                    return ClumpletType.StringSpb;
                case isc_spb_trc_id:
                    return ClumpletType.IntSpb;
                }
                break;
            case isc_action_svc_validate:
                switch (tag) {
                case isc_spb_val_tab_incl:
                case isc_spb_val_tab_excl:
                case isc_spb_val_idx_incl:
                case isc_spb_val_idx_excl:
                case isc_spb_dbname:
                    return ClumpletType.StringSpb;
                case isc_spb_val_lock_timeout:
                    return ClumpletType.IntSpb;
                }
                break;
            }
            throw invalidStructure("wrong spb state");
        }
        throw invalidStructure("unknown reason");
    }

    public void adjustSpbState() throws SQLException {
        switch (kind) {
        case SpbStart:
            if (spbState == 0 &&                              // Just started with service start block ...
                    getClumpletSize(true, true, true) == 1) { // and this is action_XXX clumplet
                spbState = getClumpTag();
            }
            break;
        default:
            break;
        }
    }

    public int getClumpletSize(boolean wTag, boolean wLength, boolean wData) throws SQLException {
        // Check for EOF
        if (position >= buffer.length) {
            throw usageMistake("read past EOF");
        }

        int rc = wTag ? 1 : 0;
        int lengthSize = 0;
        int dataSize = 0;

        switch (getClumpletType(buffer[position])) {
        // This form allows clumplets of virtually any size
        case Wide:
            // Check did we receive length component for clumplet
            if (buffer.length - position < 5) {
                throw invalidStructure("buffer end before end of clumplet - no length component");
            }
            lengthSize = 4;
            dataSize = VaxEncoding.iscVaxInteger(buffer, position + 1, 4);
            break;

        // This is the most widely used form
        case TraditionalDpb:
            // Check did we receive length component for clumplet
            if (buffer.length - position < 2) {
                throw invalidStructure("buffer end before end of clumplet - no length component");
            }
            lengthSize = 1;
            dataSize = buffer[position + 1] & 0xFF;
            break;

        // Almost all TPB parameters are single bytes
        case SingleTpb:
            break;

        // Used in SPB for long strings
        case StringSpb:
            // Check did we receive length component for clumplet
            if (buffer.length - position < 3) {
                throw invalidStructure("buffer end before end of clumplet - no length component");
            }
            lengthSize = 2;
            dataSize = VaxEncoding.iscVaxInteger2(buffer, position + 1);
            break;

        // Used in SPB for 4-byte integers
        case IntSpb:
            dataSize = 4;
            break;

        // Used in SPB for single byte
        case ByteSpb:
            dataSize = 1;
            break;
        }

        int total = 1 + lengthSize + dataSize;
        if (position + total > buffer.length) {
            throw invalidStructure("buffer end before end of clumplet - clumplet too long");
        }

        if (wLength) {
            rc += lengthSize;
        }
        if (wData) {
            rc += dataSize;
        }
        return rc;
    }

    public void moveNext() throws SQLException {
        if (isEof()) {
            return; // no need to raise useless exceptions
        }
        int cs = getClumpletSize(true, true, true);
        adjustSpbState();
        position += cs;
    }

    public void rewind() throws SQLException {
        if (buffer == null || buffer.length == 0) {
            position = 0;
            spbState = 0;
            return;
        }
        switch (kind) {
        case UnTagged:
        case WideUnTagged:
        case SpbStart:
        case SpbSendItems:
        case SpbReceiveItems:
            position = 0;
            break;
        default:
            if (kind == Kind.SpbAttach && getBufferLength() > 0 && buffer[0] != isc_spb_version1) {
                position = 2;
            } else {
                position = 1;
            }
        }
        spbState = 0;
    }

    public boolean find(int tag) throws SQLException {
        final int markPosition = position;
        for (rewind(); !isEof(); moveNext()) {
            if (tag == getClumpTag()) {
                return true;
            }
        }
        position = markPosition;
        return false;
    }

    public boolean next(int tag) throws SQLException {
        if (!isEof()) {
            final int markPosition = position;
            if (tag == getClumpTag()) {
                moveNext();
            }
            for (; !isEof(); moveNext()) {
                if (tag == getClumpTag()) {
                    return true;
                }
            }
            position = markPosition;
        }
        return false;
    }

    // Methods which work with currently selected clumplet
    public int getClumpTag() throws SQLException {
        // Check for EOF
        if (position >= buffer.length) {
            throw usageMistake("read past EOF");
        }

        return buffer[position] & 0xFF;
    }

    public int getClumpLength() throws SQLException {
        return getClumpletSize(false, false, true);
    }

    public byte[] getBytes() throws SQLException {
        final int from = position + getClumpletSize(true, true, false);
        final int to = from + getClumpLength();
        return Arrays.copyOfRange(buffer, from, to);
    }

    public int getInt() throws SQLException {
        final int length = getClumpLength();
        if (length > 4) {
            throw invalidStructure("length of integer exceeds 4 bytes");
        }
        final int from = position + getClumpletSize(true, true, false);
        return VaxEncoding.iscVaxInteger(buffer, from, length);
    }

    public long getLong() throws SQLException {
        final int length = getClumpLength();
        if (length > 8) {
            throw invalidStructure("length of long exceeds 8 bytes");
        }
        final int from = position + getClumpletSize(true, true, false);
        return VaxEncoding.iscVaxLong(buffer, from, length);
    }

    public String getString(Encoding encoding) throws SQLException {
        final int length = getClumpLength();
        final int from = position + getClumpletSize(true, true, false);
        return encoding.decodeFromCharset(buffer, from, length);
    }

    public String getString(Charset charset) throws SQLException {
        final int length = getClumpLength();
        final int from = position + getClumpletSize(true, true, false);
        return new String(buffer, from, length, charset);
    }

    // TODO: Other types in ClumpletReader.cpp not copied

    private boolean isEof() {
        return position >= getBufferLength();
    }

    private int getBufferLength() {
        if (buffer.length == 1 && kind != Kind.UnTagged && kind != Kind.SpbStart &&
                kind != Kind.WideUnTagged && kind != Kind.SpbSendItems &&
                kind != Kind.SpbReceiveItems) {
            return 0;
        }
        return buffer.length;
    }

    private static SQLException invalidStructure(String message) {
        return FbExceptionBuilder.forException(jb_invalidClumpletStructure)
                .messageParameter(message)
                .toFlatSQLException();
    }

    private static SQLException usageMistake(String message) {
        return FbExceptionBuilder.forException(jb_clumpletReaderUsageError)
                .messageParameter(message)
                .toFlatSQLException();
    }

    public enum Kind {
        EndOfList,
        Tagged,
        UnTagged,
        SpbAttach,
        SpbStart,
        Tpb,
        WideTagged,
        WideUnTagged,
        SpbSendItems,
        SpbReceiveItems
    }

    public enum ClumpletType {
        TraditionalDpb,
        SingleTpb,
        StringSpb,
        IntSpb,
        ByteSpb,
        Wide
    }

}
