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
package org.firebirdsql.jaybird.props.internal;

import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.spi.ConnectionPropertyDefinerSpi;

import java.util.stream.Stream;

import static org.firebirdsql.jaybird.fb.constants.DpbItems.*;
import static org.firebirdsql.jaybird.fb.constants.SpbItems.*;
import static org.firebirdsql.jaybird.props.PropertyConstants.*;
import static org.firebirdsql.jaybird.props.PropertyNames.*;
import static org.firebirdsql.jaybird.props.def.ConnectionProperty.builder;
import static org.firebirdsql.jaybird.props.def.ConnectionPropertyType.BOOLEAN;
import static org.firebirdsql.jaybird.props.def.ConnectionPropertyType.INT;
import static org.firebirdsql.jaybird.props.def.ConnectionPropertyType.TRANSACTION_ISOLATION;
import static org.firebirdsql.jaybird.props.internal.TransactionNameMapping.TRANSACTION_NONE;
import static org.firebirdsql.jaybird.props.internal.TransactionNameMapping.TRANSACTION_READ_COMMITTED;
import static org.firebirdsql.jaybird.props.internal.TransactionNameMapping.TRANSACTION_READ_UNCOMMITTED;
import static org.firebirdsql.jaybird.props.internal.TransactionNameMapping.TRANSACTION_REPEATABLE_READ;
import static org.firebirdsql.jaybird.props.internal.TransactionNameMapping.TRANSACTION_SERIALIZABLE;

/**
 * Connection property definer for the standard connection properties of Jaybird.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
class StandardConnectionPropertyDefiner implements ConnectionPropertyDefinerSpi {

    @Override
    public Stream<ConnectionProperty> defineProperties() {
        return Stream.of(
                // Attachment properties (shared by database and service)
                builder(serverName).aliases("host"),
                builder(portNumber).type(INT).aliases("port"),
                builder(attachObjectName).aliases(databaseName, serviceName, "database"),
                builder(type),
                // NOTE: Intentionally not mapped to DPB/SPB item, that is handled during authentication
                builder(user).aliases("userName", "user_name", "isc_dpb_user_name"),
                // NOTE: Intentionally not mapped to DPB/SPB item, that is handled during authentication
                builder(password).aliases("isc_dpb_password"),
                builder(roleName).aliases("sqlRole", "role_name", "sql_role_name", "isc_dpb_sql_role_name")
                        .dpbItem(isc_dpb_sql_role_name).spbItem(isc_spb_sql_role_name),
                builder(processId).type(INT).aliases("process_id", "isc_dpb_process_id")
                        .dpbItem(isc_dpb_process_id).spbItem(isc_spb_process_id),
                builder(processName).aliases("process_name", "isc_dpb_process_name")
                        .dpbItem(isc_dpb_process_name).spbItem(isc_spb_process_name),
                builder(socketBufferSize).type(INT).aliases("socket_buffer_size"),
                builder(soTimeout).type(INT).aliases("so_timeout"),
                builder(connectTimeout).type(INT).aliases("connect_timeout", "isc_dpb_connect_timeout")
                        .dpbItem(isc_dpb_connect_timeout).spbItem(isc_spb_connect_timeout),
                builder(wireCrypt).aliases("wire_crypt_level")
                        .choices(WIRE_CRYPT_DEFAULT, WIRE_CRYPT_REQUIRED, WIRE_CRYPT_ENABLED, WIRE_CRYPT_DISABLED),
                builder(dbCryptConfig).aliases("db_crypt_config"),
                // NOTE: Intentionally not mapped to DPB/SPB item, that is handled during authentication
                builder(authPlugins).aliases("auth_plugin_list", "isc_dpb_auth_plugin_list"),
                builder(wireCompression).type(BOOLEAN).aliases("wire_compression"),

                // Database properties
                builder(charSet).aliases("charset", "localEncoding", "local_encoding"),
                // NOTE: Mapping this to isc_dpb_lc_ctype is handled separately in AbstractParameterConverter
                builder(encoding).aliases("lc_ctype", "isc_dpb_lc_ctype"),
                builder(sqlDialect).type(INT).aliases("dialect", "sql_dialect", "isc_dpb_sql_dialect")
                        .choices("1", "2", "3").dpbItem(isc_dpb_sql_dialect),
                builder(pageCacheSize).type(INT).aliases("buffersNumber", "num_buffers", "isc_dpb_num_buffers")
                        .dpbItem(isc_dpb_num_buffers),
                builder(dataTypeBind).aliases("set_bind", "isc_dpb_set_bind").dpbItem(isc_dpb_set_bind),
                // NOTE: Intentionally not mapped to DPB/SPB item, that is handled during attach
                builder(sessionTimeZone).aliases("session_time_zone", "isc_dpb_session_time_zone"),
                builder(blobBufferSize).aliases("blob_buffer_size"),
                builder(useStreamBlobs).type(BOOLEAN).aliases("use_stream_blobs"),
                builder(defaultResultSetHoldable).type(BOOLEAN).aliases("defaultHoldable", "result_set_holdable"),
                builder(useFirebirdAutocommit).type(BOOLEAN).aliases("use_firebird_autocommit"),
                builder(columnLabelForName).type(BOOLEAN).aliases("column_label_for_name"),
                builder(generatedKeysEnabled).aliases("generated_keys_enabled"),
                builder(ignoreProcedureType).type(BOOLEAN).aliases("ignore_procedure_type"),
                builder(decfloatRound).aliases("decfloat_round", "isc_dpb_decfloat_round")
                        .choices("ceiling", "up", "half_up", "half_even", "half_down", "down", "floor", "reround")
                        .dpbItem(isc_dpb_decfloat_round),
                builder(decfloatTraps).aliases("decfloat_traps", "isc_dpb_decfloat_traps")
                        .dpbItem(isc_dpb_decfloat_traps),
                builder(tpbMapping).aliases("tpb_mapping"),
                builder(defaultIsolation).type(TRANSACTION_ISOLATION)
                        .aliases("isolation", "defaultTransactionIsolation")
                        .choices(TRANSACTION_NONE, TRANSACTION_READ_UNCOMMITTED, TRANSACTION_READ_COMMITTED,
                                TRANSACTION_REPEATABLE_READ, TRANSACTION_SERIALIZABLE),
                // TODO Property should be considered deprecated, remove in Jaybird 6 or later
                builder("timestampUsesLocalTimezone").type(BOOLEAN).aliases("timestamp_uses_local_timezone"),

                // TODO Consider removing this property, otherwise formally add it to PropertyNames
                builder("filename_charset"),

                // Service properties
                // Nothing so far

                // Formally unsupported properties that will need explicit type mapping other than string to work
                builder("page_size").type(INT).aliases("isc_dpb_page_size").dpbItem(isc_dpb_page_size),
                builder("verify").type(INT).aliases("isc_dpb_verify").dpbItem(isc_dpb_verify),
                builder("sweep").type(INT).aliases("isc_dpb_sweep").dpbItem(isc_dpb_sweep),
                builder("dbkey_scope").type(INT).aliases("isc_dpb_dbkey_scope").dpbItem(isc_dpb_dbkey_scope),
                builder("no_garbage_collect").type(BOOLEAN).aliases("isc_dpb_no_garbage_collect")
                        .dpbItem(isc_dpb_no_garbage_collect),
                builder("damaged").type(INT).aliases("isc_dpb_damaged").dpbItem(isc_dpb_damaged),
                builder("activate_shadow").type(BOOLEAN).aliases("isc_dpb_activate_shadow")
                        .dpbItem(isc_dpb_activate_shadow),
                builder("sweep_interval").type(INT).aliases("isc_dpb_sweep_interval").dpbItem(isc_dpb_sweep_interval),
                builder("delete_shadow").type(BOOLEAN).aliases("isc_dpb_delete_shadow").dpbItem(isc_dpb_delete_shadow),
                builder("force_write").type(INT).aliases("isc_dpb_force_write").dpbItem(isc_dpb_force_write),
                builder("no_reserve").type(INT).aliases("isc_dpb_no_reserve").dpbItem(isc_dpb_no_reserve),
                builder("shutdown").type(INT).aliases("isc_dpb_shutdown").dpbItem(isc_dpb_shutdown),
                builder("online").type(INT).aliases("isc_dpb_online").dpbItem(isc_dpb_online),
                builder("shutdown_delay").type(INT).aliases("isc_dpb_shutdown_delay").dpbItem(isc_dpb_shutdown_delay),
                builder("overwrite").type(INT).aliases("isc_dpb_overwrite").dpbItem(isc_dpb_overwrite),
                builder("dummy_packet_interval").type(INT).aliases("isc_dpb_dummy_packet_interval")
                        .dpbItem(isc_dpb_dummy_packet_interval).spbItem(isc_spb_dummy_packet_interval),
                builder("set_page_buffers").type(INT).aliases("isc_dpb_set_page_buffers")
                        .dpbItem(isc_dpb_set_page_buffers),
                builder("set_db_readonly").type(INT).aliases("isc_dpb_set_db_readonly")
                        .dpbItem(isc_dpb_set_db_readonly),
                builder("set_db_sql_dialect").type(INT).aliases("isc_dpb_set_db_sql_dialect")
                        .dpbItem(isc_dpb_set_db_sql_dialect),
                builder("no_db_triggers").type(INT).aliases("isc_dpb_no_db_triggers").dpbItem(isc_dpb_no_db_triggers),
                builder("nolinger").type(BOOLEAN).aliases("isc_dpb_nolinger").dpbItem(isc_dpb_nolinger),
                builder("reset_icu").type(BOOLEAN).aliases("isc_dpb_reset_icu").dpbItem(isc_dpb_reset_icu),
                builder("set_db_replica").type(INT).aliases("isc_dpb_set_db_replica").dpbItem(isc_dpb_set_db_replica),
                builder("debug").type(INT).aliases("isc_dpb_debug").dpbItem(isc_dpb_debug),
                builder("trace").type(INT).aliases("isc_dpb_trace").dpbItem(isc_dpb_trace),
                builder("interp").type(INT).aliases("isc_dpb_interp").dpbItem(isc_dpb_interp),
                builder("ext_call_depth").type(INT).aliases("isc_dpb_ext_call_depth").dpbItem(isc_dpb_ext_call_depth)

                // NOTE: Properties not defined elsewhere will be defined through UnregisteredDpbDefiner as type string
        ).map(ConnectionProperty.Builder::build);
    }

    @Override
    public void notRegistered(ConnectionProperty connectionProperty) {
        // Built-in connection properties must be registered, if they cannot be registered,
        // there is something wrong in the implementation
        assert false : "Failed to define built-in connection property: " + connectionProperty;
    }

}
