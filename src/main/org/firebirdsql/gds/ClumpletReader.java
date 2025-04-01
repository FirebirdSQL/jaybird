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
package org.firebirdsql.gds;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.jaybird.fb.constants.SpbItems;
import org.firebirdsql.jaybird.fb.constants.TpbItems;
import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Arrays;

import static org.firebirdsql.gds.ISCConstants.isc_spb_version;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_clumpletReaderUsageError;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_invalidClumpletStructure;

/**
 * Reader for clumplets, similar to the implementation of {@code ClumpletReader.cpp} in Firebird.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@NullMarked
public final class ClumpletReader {

    private final Kind kind;
    private final byte[] buffer;
    private int spbState; // Reflects state of spb parser/writer
    private int position;

    public ClumpletReader(Kind kind, byte @Nullable [] buffer) {
        this.kind = kind;
        this.buffer = buffer != null ? buffer : ByteArrayHelper.emptyByteArray();
    }

    public boolean isTagged() {
        return kind.isTagged();
    }

    public int getBufferTag() throws SQLException {
        if (!isTagged()) {
            throw usageMistake("buffer is not tagged");
        }
        return switch (kind) {
            case Tpb, Tagged, WideTagged -> {
                if (buffer.length == 0) {
                    throw invalidStructure("empty buffer");
                }
                yield buffer[0];
            }
            case SpbAttach -> {
                if (buffer.length == 0) {
                    throw invalidStructure("empty buffer");
                } else if (buffer[0] == isc_spb_version) {
                    if (buffer.length == 1) {
                        throw invalidStructure("empty buffer");
                    }
                    yield buffer[1];
                }
                yield buffer[0];
            }
            default -> throw new SQLException("Unexpected clumplet kind: " + kind);
        };
    }

    public ClumpletType getClumpletType(byte tag) throws SQLException {
        return switch (kind) {
        case Tagged, UnTagged, SpbAttach -> ClumpletType.TraditionalDpb;
        case WideTagged, WideUnTagged -> ClumpletType.Wide;
        case Tpb -> switch (tag) {
                case TpbItems.isc_tpb_lock_write, TpbItems.isc_tpb_lock_read, TpbItems.isc_tpb_lock_timeout ->
                        ClumpletType.TraditionalDpb;
                default -> ClumpletType.SingleTpb;
            };
        case SpbSendItems -> switch (tag) {
                case ISCConstants.isc_info_svc_auth_block -> ClumpletType.Wide;
                case ISCConstants.isc_info_end, ISCConstants.isc_info_truncated, ISCConstants.isc_info_error,
                     ISCConstants.isc_info_data_not_ready, ISCConstants.isc_info_length,
                     ISCConstants.isc_info_flag_end -> ClumpletType.SingleTpb;
                default -> ClumpletType.StringSpb;
            };
        case SpbReceiveItems, InfoItems -> ClumpletType.SingleTpb;
        case SpbStart -> {
            switch (tag) {
            case SpbItems.isc_spb_auth_block:
            case SpbItems.isc_spb_trusted_auth:
            case SpbItems.isc_spb_auth_plugin_name:
            case SpbItems.isc_spb_auth_plugin_list:
                yield ClumpletType.Wide;
            }

            yield switch (spbState) {
            case 0 -> ClumpletType.SingleTpb;
            case ISCConstants.isc_action_svc_backup, ISCConstants.isc_action_svc_restore -> switch (tag) {
                    case ISCConstants.isc_spb_bkp_file, SpbItems.isc_spb_dbname, ISCConstants.isc_spb_res_fix_fss_data,
                         ISCConstants.isc_spb_res_fix_fss_metadata, ISCConstants.isc_spb_bkp_stat ->
                            ClumpletType.StringSpb;
                    case ISCConstants.isc_spb_bkp_factor, ISCConstants.isc_spb_bkp_length,
                         ISCConstants.isc_spb_res_length, ISCConstants.isc_spb_res_buffers,
                         ISCConstants.isc_spb_res_page_size, SpbItems.isc_spb_options, SpbItems.isc_spb_verbint ->
                            ClumpletType.IntSpb;
                    case SpbItems.isc_spb_verbose -> ClumpletType.SingleTpb;
                    case ISCConstants.isc_spb_res_access_mode -> ClumpletType.ByteSpb;
                    default -> throw invalidStructure("unknown parameter for backup/restore");
                };
            case ISCConstants.isc_action_svc_repair -> switch (tag) {
                    case SpbItems.isc_spb_dbname -> ClumpletType.StringSpb;
                    case SpbItems.isc_spb_options, ISCConstants.isc_spb_rpr_commit_trans,
                         ISCConstants.isc_spb_rpr_rollback_trans, ISCConstants.isc_spb_rpr_recover_two_phase ->
                            ClumpletType.IntSpb;
                    case ISCConstants.isc_spb_rpr_commit_trans_64, ISCConstants.isc_spb_rpr_rollback_trans_64,
                         ISCConstants.isc_spb_rpr_recover_two_phase_64 -> ClumpletType.BigIntSpb;
                    default -> throw invalidStructure("unknown parameter for repair");
                };
            case ISCConstants.isc_action_svc_add_user, ISCConstants.isc_action_svc_delete_user,
                 ISCConstants.isc_action_svc_modify_user, ISCConstants.isc_action_svc_display_user,
                 ISCConstants.isc_action_svc_display_user_adm, ISCConstants.isc_action_svc_set_mapping,
                 ISCConstants.isc_action_svc_drop_mapping -> switch (tag) {
                    case SpbItems.isc_spb_dbname, SpbItems.isc_spb_sql_role_name, ISCConstants.isc_spb_sec_username,
                         ISCConstants.isc_spb_sec_password, ISCConstants.isc_spb_sec_groupname,
                         ISCConstants.isc_spb_sec_firstname, ISCConstants.isc_spb_sec_middlename,
                         ISCConstants.isc_spb_sec_lastname -> ClumpletType.StringSpb;
                    case ISCConstants.isc_spb_sec_userid, ISCConstants.isc_spb_sec_groupid,
                         ISCConstants.isc_spb_sec_admin -> ClumpletType.IntSpb;
                    default -> throw invalidStructure("unknown parameter for security database operation");
                };
            case ISCConstants.isc_action_svc_properties -> switch (tag) {
                    case SpbItems.isc_spb_dbname -> ClumpletType.StringSpb;
                    case ISCConstants.isc_spb_prp_page_buffers, ISCConstants.isc_spb_prp_sweep_interval,
                         ISCConstants.isc_spb_prp_shutdown_db, ISCConstants.isc_spb_prp_deny_new_attachments,
                         ISCConstants.isc_spb_prp_deny_new_transactions, ISCConstants.isc_spb_prp_set_sql_dialect,
                         SpbItems.isc_spb_options, ISCConstants.isc_spb_prp_force_shutdown,
                         ISCConstants.isc_spb_prp_attachments_shutdown,
                         ISCConstants.isc_spb_prp_transactions_shutdown -> ClumpletType.IntSpb;
                    case ISCConstants.isc_spb_prp_reserve_space, ISCConstants.isc_spb_prp_write_mode,
                         ISCConstants.isc_spb_prp_access_mode, ISCConstants.isc_spb_prp_shutdown_mode,
                         ISCConstants.isc_spb_prp_online_mode -> ClumpletType.ByteSpb;
                    default -> throw invalidStructure("unknown parameter for setting database properties");
                };
            case ISCConstants.isc_action_svc_db_stats -> switch (tag) {
                    case SpbItems.isc_spb_dbname, SpbItems.isc_spb_command_line, ISCConstants.isc_spb_sts_table ->
                            ClumpletType.StringSpb;
                    case SpbItems.isc_spb_options -> ClumpletType.IntSpb;
                    default -> throw invalidStructure("unknown parameter for getting statistics");
                };
            case ISCConstants.isc_action_svc_get_ib_log -> throw invalidStructure("unknown parameter for getting log");
            case ISCConstants.isc_action_svc_nbak, ISCConstants.isc_action_svc_nrest -> switch (tag) {
                    case ISCConstants.isc_spb_nbk_file, ISCConstants.isc_spb_nbk_direct, SpbItems.isc_spb_dbname,
                         ISCConstants.isc_spb_nbk_guid -> ClumpletType.StringSpb;
                    case ISCConstants.isc_spb_nbk_level, SpbItems.isc_spb_options, ISCConstants.isc_spb_nbk_keep_days,
                         ISCConstants.isc_spb_nbk_keep_rows -> ClumpletType.IntSpb;
                    case ISCConstants.isc_spb_nbk_clean_history -> ClumpletType.SingleTpb;
                    default -> throw invalidStructure("unknown parameter for nbackup");
                };
            case ISCConstants.isc_action_svc_nfix -> switch (tag) {
                    case SpbItems.isc_spb_dbname -> ClumpletType.StringSpb;
                    case SpbItems.isc_spb_options -> ClumpletType.IntSpb;
                    default -> throw invalidStructure("unknown parameter for nbackup");
                };
            case ISCConstants.isc_action_svc_trace_start, ISCConstants.isc_action_svc_trace_stop,
                 ISCConstants.isc_action_svc_trace_suspend, ISCConstants.isc_action_svc_trace_resume -> switch (tag) {
                    case ISCConstants.isc_spb_trc_cfg, ISCConstants.isc_spb_trc_name -> ClumpletType.StringSpb;
                    case ISCConstants.isc_spb_trc_id -> ClumpletType.IntSpb;
                    default -> throw invalidStructure("wrong spb state");
                };
            case ISCConstants.isc_action_svc_validate -> switch (tag) {
                    case ISCConstants.isc_spb_val_tab_incl, ISCConstants.isc_spb_val_tab_excl,
                         ISCConstants.isc_spb_val_idx_incl, ISCConstants.isc_spb_val_idx_excl,
                         SpbItems.isc_spb_dbname -> ClumpletType.StringSpb;
                    case ISCConstants.isc_spb_val_lock_timeout -> ClumpletType.IntSpb;
                    default -> throw invalidStructure("wrong spb state");
                };
            default -> throw invalidStructure("wrong spb state");
            };
        }
        case InfoResponse -> switch (tag) {
                case ISCConstants.isc_info_end, ISCConstants.isc_info_truncated, ISCConstants.isc_info_flag_end ->
                        ClumpletType.SingleTpb;
                default -> ClumpletType.StringSpb;
            };
        };
    }

    public void adjustSpbState() throws SQLException {
        if (kind == Kind.SpbStart
                && spbState == 0
                && getClumpletSize(true, true, true) == 1) {
            spbState = getClumpTag();
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

        // Used in SPB for 8-byte integers
        case BigIntSpb:
            dataSize = 8;
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
        } else if (kind == Kind.InfoResponse) {
            int clumpTag = getClumpTag();
            // terminating clumplet
            if (clumpTag == ISCConstants.isc_info_end || clumpTag == ISCConstants.isc_info_truncated) {
                position = getBufferLength();
                return;
            }
        }
        int cs = getClumpletSize(true, true, true);
        adjustSpbState();
        position += cs;
    }

    public void rewind() {
        if (buffer.length == 0 || !isTagged()) {
            position = 0;
        } else if (kind == Kind.SpbAttach && getBufferLength() > 0 && buffer[0] == isc_spb_version) {
            position = 2;
        } else {
            position = 1;
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

    /**
     * Finds the next {@code tag} in the reader, skipping tags to find it.
     * <p>
     * If {@code tag} is not found, the original position of this reader is retained.
     * </p>
     *
     * @param tag
     *         Tag to find
     * @return {@code true} if {@code tag} was found and this reader is positioned to read it, {@code false} otherwise
     * @throws SQLException
     *         For errors positioning
     * @see #directNext(int)
     */
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

    /**
     * Checks if the next tag in this reader is {@code tag}.
     * <p>
     * If the next tag is not {@code tag}, the original position of this reader is retained.
     * </p>
     *
     * @param tag
     *         Tag to find
     * @return {@code true} if the next tag is {@code tag} and this reader is positioned to read it, {@code false}
     * otherwise
     * @throws SQLException
     *         For errors positioning
     * @see #next(int)
     * @since 5
     */
    public boolean directNext(int tag) throws SQLException {
        if (!isEof()) {
            final int markPosition = position;
            moveNext();
            if (!isEof() && tag == getClumpTag()) {
                return true;
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

    public boolean isEof() {
        return position >= getBufferLength();
    }

    private int getBufferLength() {
        if (buffer.length == 1 && isTagged()) {
            return 0;
        }
        return buffer.length;
    }

    private static SQLException invalidStructure(String message) {
        return FbExceptionBuilder.forException(jb_invalidClumpletStructure).messageParameter(message).toSQLException();
    }

    private static SQLException usageMistake(String message) {
        return FbExceptionBuilder.forException(jb_clumpletReaderUsageError).messageParameter(message).toSQLException();
    }

    @SuppressWarnings("java:S115")
    public enum Kind {

        Tagged(true),
        UnTagged(false),
        SpbAttach(true),
        SpbStart(false),
        Tpb(true),
        WideTagged(true),
        WideUnTagged(false),
        SpbSendItems(false),
        SpbReceiveItems(false),
        InfoResponse(false),
        InfoItems(false),
        ;

        private final boolean tagged;

        Kind(boolean tagged) {
            this.tagged = tagged;
        }

        public boolean isTagged() {
            return tagged;
        }

    }

    @SuppressWarnings("java:S115")
    public enum ClumpletType {
        TraditionalDpb,
        SingleTpb,
        StringSpb,
        IntSpb,
        BigIntSpb,
        ByteSpb,
        Wide
    }

}
