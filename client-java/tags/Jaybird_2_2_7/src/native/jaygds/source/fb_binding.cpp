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

#include "platform.h"
#include "fb_binding.h"
#include "exceptions.h"

// FirebirdApiBinding Class -----------------------------------------------------------------------------------

// Static methods

void FirebirdApiBinding::Load(SHARED_LIBRARY_HANDLE sHandle)
    {
    FB_ENTRYPOINT(isc_attach_database);
    FB_ENTRYPOINT(isc_blob_info);
    FB_ENTRYPOINT(isc_cancel_blob);
    FB_ENTRYPOINT(isc_close_blob);
    FB_ENTRYPOINT(isc_commit_retaining);
    FB_ENTRYPOINT(isc_commit_transaction);
    FB_ENTRYPOINT(isc_create_blob);
    FB_ENTRYPOINT(isc_create_blob2);
    FB_ENTRYPOINT(isc_create_database);
    FB_ENTRYPOINT(isc_database_info);
    FB_ENTRYPOINT(isc_decode_date);
    FB_ENTRYPOINT(isc_decode_sql_date);
    FB_ENTRYPOINT(isc_decode_sql_time);
    FB_ENTRYPOINT(isc_decode_timestamp);
    FB_ENTRYPOINT(isc_detach_database);
    FB_ENTRYPOINT(isc_drop_database);
    FB_ENTRYPOINT(isc_dsql_allocate_statement);
    FB_ENTRYPOINT(isc_dsql_alloc_statement2);
    FB_ENTRYPOINT(isc_dsql_describe);
    FB_ENTRYPOINT(isc_dsql_describe_bind);
    FB_ENTRYPOINT(isc_dsql_exec_immed2);
    FB_ENTRYPOINT(isc_dsql_execute);
    FB_ENTRYPOINT(isc_dsql_execute2);
    FB_ENTRYPOINT(isc_dsql_execute_immediate);
    FB_ENTRYPOINT(isc_dsql_fetch);
    FB_ENTRYPOINT(isc_dsql_finish);
    FB_ENTRYPOINT(isc_dsql_free_statement);
    FB_ENTRYPOINT(isc_dsql_insert);
    FB_ENTRYPOINT(isc_dsql_prepare);
    FB_ENTRYPOINT(isc_dsql_set_cursor_name);
    FB_ENTRYPOINT(isc_dsql_sql_info);
    FB_ENTRYPOINT(isc_encode_date);
    FB_ENTRYPOINT(isc_encode_sql_date);
    FB_ENTRYPOINT(isc_encode_sql_time);
    FB_ENTRYPOINT(isc_encode_timestamp);
    FB_ENTRYPOINT(isc_get_segment);
    FB_ENTRYPOINT(isc_interprete);
    FB_ENTRYPOINT(isc_open_blob);
    FB_ENTRYPOINT(isc_open_blob2);
    FB_ENTRYPOINT(isc_prepare_transaction2);
    FB_ENTRYPOINT(isc_print_sqlerror);
    FB_ENTRYPOINT(isc_print_status);
    FB_ENTRYPOINT(isc_put_segment);
    FB_ENTRYPOINT(isc_rollback_retaining);
    FB_ENTRYPOINT(isc_rollback_transaction);
    FB_ENTRYPOINT(isc_start_transaction);
    FB_ENTRYPOINT(isc_reconnect_transaction);
    FB_ENTRYPOINT(isc_sqlcode);
    FB_ENTRYPOINT(isc_sql_interprete);
    FB_ENTRYPOINT(isc_seek_blob);
    FB_ENTRYPOINT(isc_service_attach);
    FB_ENTRYPOINT(isc_service_detach);
    FB_ENTRYPOINT(isc_service_query);
    FB_ENTRYPOINT(isc_service_start);
    FB_ENTRYPOINT(isc_transaction_info);
    FB_ENTRYPOINT(isc_que_events);
    FB_ENTRYPOINT(isc_event_block);
    FB_ENTRYPOINT(isc_event_counts);
    FB_ENTRYPOINT(isc_wait_for_event);
    FB_ENTRYPOINT(isc_cancel_events);
    FB_ENTRYPOINT(isc_free);      
	FB_ENTRYPOINT_OPTIONAL(fb_cancel_operation);
    }

#define FB_COMP(X) if(X!=v.X) return false;

bool FirebirdApiBinding::operator==(const FirebirdApiBinding &v)
    {
    FB_COMP(isc_attach_database);
    FB_COMP(isc_blob_info);
    FB_COMP(isc_cancel_blob);
    FB_COMP(isc_close_blob);
    FB_COMP(isc_commit_retaining);
    FB_COMP(isc_commit_transaction);
    FB_COMP(isc_create_blob);
    FB_COMP(isc_create_blob2);
    FB_COMP(isc_create_database);
    FB_COMP(isc_database_info);
    FB_COMP(isc_decode_date);
    FB_COMP(isc_decode_sql_date);
    FB_COMP(isc_decode_sql_time);
    FB_COMP(isc_decode_timestamp);
    FB_COMP(isc_detach_database);
    FB_COMP(isc_drop_database);
    FB_COMP(isc_dsql_allocate_statement);
    FB_COMP(isc_dsql_alloc_statement2);
    FB_COMP(isc_dsql_describe);
    FB_COMP(isc_dsql_describe_bind);
    FB_COMP(isc_dsql_exec_immed2);
    FB_COMP(isc_dsql_execute);
    FB_COMP(isc_dsql_execute2);
    FB_COMP(isc_dsql_execute_immediate);
    FB_COMP(isc_dsql_fetch);
    FB_COMP(isc_dsql_finish);
    FB_COMP(isc_dsql_free_statement);
    FB_COMP(isc_dsql_insert);
    FB_COMP(isc_dsql_prepare);
    FB_COMP(isc_dsql_set_cursor_name);
    FB_COMP(isc_dsql_sql_info);
    FB_COMP(isc_encode_date);
    FB_COMP(isc_encode_sql_date);
    FB_COMP(isc_encode_sql_time);
    FB_COMP(isc_encode_timestamp);
    FB_COMP(isc_get_segment);
    FB_COMP(isc_interprete);
    FB_COMP(isc_open_blob);
    FB_COMP(isc_open_blob2);
    FB_COMP(isc_prepare_transaction2);
    FB_COMP(isc_print_sqlerror);
    FB_COMP(isc_print_status);
    FB_COMP(isc_put_segment);
    FB_COMP(isc_rollback_retaining);
    FB_COMP(isc_rollback_transaction);
    FB_COMP(isc_start_transaction);
    FB_COMP(isc_reconnect_transaction);
    FB_COMP(isc_sqlcode);
    FB_COMP(isc_sql_interprete);
    FB_COMP(isc_seek_blob);
    FB_COMP(isc_service_attach);
    FB_COMP(isc_service_detach);
    FB_COMP(isc_service_query);
    FB_COMP(isc_service_start);
    FB_COMP(isc_transaction_info);
    FB_COMP(isc_que_events);
    FB_COMP(isc_event_block);
    FB_COMP(isc_wait_for_event);
    FB_COMP(isc_event_counts);
    FB_COMP(isc_cancel_events);
    FB_COMP(isc_free);
	FB_COMP(fb_cancel_operation);
    return true;
    }
