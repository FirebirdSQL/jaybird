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

#include "Stdafx.h"

#include "fb_binding.h"


#include "exceptions.h"


// FirebirdApiBinding Class -----------------------------------------------------------------------------------

// Static Members

bool	FirebirdApiBinding::sIsLoaded = false;
HMODULE FirebirdApiBinding::sHandle = NULL;

prototype_isc_attach_database*		FirebirdApiBinding::isc_attach_database  = NULL;
prototype_isc_array_gen_sdl*			FirebirdApiBinding::isc_array_gen_sdl  = NULL;
prototype_isc_array_get_slice*		FirebirdApiBinding::isc_array_get_slice  = NULL;
prototype_isc_array_lookup_bounds*	FirebirdApiBinding::isc_array_lookup_bounds  = NULL;
prototype_isc_array_lookup_desc*		FirebirdApiBinding::isc_array_lookup_desc  = NULL;
prototype_isc_array_set_desc*		FirebirdApiBinding::isc_array_set_desc  = NULL;
prototype_isc_array_put_slice*		FirebirdApiBinding::isc_array_put_slice  = NULL;
prototype_isc_blob_default_desc*		FirebirdApiBinding::isc_blob_default_desc  = NULL;;
prototype_isc_blob_gen_bpb*			FirebirdApiBinding::isc_blob_gen_bpb  = NULL;
prototype_isc_blob_info*				FirebirdApiBinding::isc_blob_info  = NULL;
prototype_isc_blob_lookup_desc*		FirebirdApiBinding::isc_blob_lookup_desc  = NULL;
prototype_isc_blob_set_desc*			FirebirdApiBinding::isc_blob_set_desc  = NULL;
prototype_isc_cancel_blob*			FirebirdApiBinding::isc_cancel_blob  = NULL;
prototype_isc_cancel_events*			FirebirdApiBinding::isc_cancel_events  = NULL;
prototype_isc_close_blob*			FirebirdApiBinding::isc_close_blob  = NULL;
prototype_isc_commit_retaining*		FirebirdApiBinding::isc_commit_retaining  = NULL;
prototype_isc_commit_transaction*	FirebirdApiBinding::isc_commit_transaction  = NULL;
prototype_isc_create_blob*			FirebirdApiBinding::isc_create_blob  = NULL;
prototype_isc_create_blob2*			FirebirdApiBinding::isc_create_blob2  = NULL;
prototype_isc_create_database*		FirebirdApiBinding::isc_create_database  = NULL;
prototype_isc_database_info*			FirebirdApiBinding::isc_database_info  = NULL;
prototype_isc_decode_date*			FirebirdApiBinding::isc_decode_date  = NULL;
prototype_isc_decode_sql_date*		FirebirdApiBinding::isc_decode_sql_date  = NULL;
prototype_isc_decode_sql_time*		FirebirdApiBinding::isc_decode_sql_time  = NULL;
prototype_isc_decode_timestamp*		FirebirdApiBinding::isc_decode_timestamp  = NULL;
prototype_isc_detach_database*		FirebirdApiBinding::isc_detach_database  = NULL;
prototype_isc_drop_database*			FirebirdApiBinding::isc_drop_database  = NULL;
prototype_isc_dsql_allocate_statement*	FirebirdApiBinding::isc_dsql_allocate_statement  = NULL;
prototype_isc_dsql_alloc_statement2*	FirebirdApiBinding::isc_dsql_alloc_statement2  = NULL;
prototype_isc_dsql_describe*			FirebirdApiBinding::isc_dsql_describe  = NULL;
prototype_isc_dsql_describe_bind*	FirebirdApiBinding::isc_dsql_describe_bind  = NULL;
prototype_isc_dsql_exec_immed2*		FirebirdApiBinding::isc_dsql_exec_immed2  = NULL;
prototype_isc_dsql_execute*			FirebirdApiBinding::isc_dsql_execute  = NULL;
prototype_isc_dsql_execute2*			FirebirdApiBinding::isc_dsql_execute2  = NULL;
prototype_isc_dsql_execute_immediate*	FirebirdApiBinding::isc_dsql_execute_immediate  = NULL;
prototype_isc_dsql_fetch*			FirebirdApiBinding::isc_dsql_fetch  = NULL;
prototype_isc_dsql_finish*			FirebirdApiBinding::isc_dsql_finish  = NULL;
prototype_isc_dsql_free_statement*	FirebirdApiBinding::isc_dsql_free_statement  = NULL;
prototype_isc_dsql_insert*			FirebirdApiBinding::isc_dsql_insert  = NULL;
prototype_isc_dsql_prepare*			FirebirdApiBinding::isc_dsql_prepare  = NULL;
prototype_isc_dsql_set_cursor_name*	FirebirdApiBinding::isc_dsql_set_cursor_name  = NULL;
prototype_isc_dsql_sql_info*			FirebirdApiBinding::isc_dsql_sql_info  = NULL;
prototype_isc_encode_date*			FirebirdApiBinding::isc_encode_date  = NULL;
prototype_isc_encode_sql_date*		FirebirdApiBinding::isc_encode_sql_date  = NULL;
prototype_isc_encode_sql_time*		FirebirdApiBinding::isc_encode_sql_time  = NULL;
prototype_isc_encode_timestamp*		FirebirdApiBinding::isc_encode_timestamp  = NULL;
prototype_isc_event_block*			FirebirdApiBinding::isc_event_block  = NULL;
prototype_isc_event_counts*			FirebirdApiBinding::isc_event_counts  = NULL;
prototype_isc_expand_dpb*			FirebirdApiBinding::isc_expand_dpb  = NULL;
prototype_isc_modify_dpb*			FirebirdApiBinding::isc_modify_dpb  = NULL;
prototype_isc_free*					FirebirdApiBinding::isc_free  = NULL;
prototype_isc_get_segment*			FirebirdApiBinding::isc_get_segment  = NULL;
prototype_isc_get_slice*				FirebirdApiBinding::isc_get_slice  = NULL;
prototype_isc_interprete*			FirebirdApiBinding::isc_interprete  = NULL;
prototype_isc_open_blob*				FirebirdApiBinding::isc_open_blob  = NULL;
prototype_isc_open_blob2*			FirebirdApiBinding::isc_open_blob2  = NULL;
prototype_isc_prepare_transaction2*	FirebirdApiBinding::isc_prepare_transaction2  = NULL;
prototype_isc_print_sqlerror*		FirebirdApiBinding::isc_print_sqlerror  = NULL;
prototype_isc_print_status*			FirebirdApiBinding::isc_print_status  = NULL;
prototype_isc_put_segment*			FirebirdApiBinding::isc_put_segment  = NULL;
prototype_isc_put_slice*				FirebirdApiBinding::isc_put_slice  = NULL;
prototype_isc_que_events*			FirebirdApiBinding::isc_que_events  = NULL;
prototype_isc_rollback_retaining*	FirebirdApiBinding::isc_rollback_retaining  = NULL;
prototype_isc_rollback_transaction*	FirebirdApiBinding::isc_rollback_transaction  = NULL;
prototype_isc_start_multiple*		FirebirdApiBinding::isc_start_multiple  = NULL;
prototype_isc_start_transaction*		FirebirdApiBinding::isc_start_transaction  = NULL;
prototype_isc_sqlcode*				FirebirdApiBinding::isc_sqlcode  = NULL;
prototype_isc_sql_interprete*		FirebirdApiBinding::isc_sql_interprete  = NULL;
prototype_isc_transaction_info*		FirebirdApiBinding::isc_transaction_info  = NULL;
prototype_isc_transact_request*		FirebirdApiBinding::isc_transact_request  = NULL;
prototype_isc_vax_integer*			FirebirdApiBinding::isc_vax_integer  = NULL;


// Static methods

/*
 *
 */
void FirebirdApiBinding::Load(const char* const firebirdDllName)
	{
	if (sHandle == NULL)
		{
		sHandle = LoadLibrary(firebirdDllName);
		if (sHandle == NULL)
			{
			throw InternalException("FirebirdApiBinding::Initialize - Could not find or load the GDS32.DLL");
			}

		#define FB_ENTRYPOINT(X) \
			if ((##X = (prototype_##X*)GetProcAddress(sHandle, #X)) == NULL) \
				throw InternalException("FirebirdApiBinding:Initialize() - Entry-point "#X" not found")


		FB_ENTRYPOINT(isc_attach_database);
		FB_ENTRYPOINT(isc_array_gen_sdl);
		FB_ENTRYPOINT(isc_array_get_slice);
		FB_ENTRYPOINT(isc_array_lookup_bounds);
		FB_ENTRYPOINT(isc_array_lookup_desc);
		FB_ENTRYPOINT(isc_array_set_desc);
		FB_ENTRYPOINT(isc_array_put_slice);
		FB_ENTRYPOINT(isc_blob_default_desc);
		FB_ENTRYPOINT(isc_blob_gen_bpb);
		FB_ENTRYPOINT(isc_blob_info);
		FB_ENTRYPOINT(isc_blob_lookup_desc);
		FB_ENTRYPOINT(isc_blob_set_desc);
		FB_ENTRYPOINT(isc_cancel_blob);
		FB_ENTRYPOINT(isc_cancel_events);
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
		FB_ENTRYPOINT(isc_event_block);
		FB_ENTRYPOINT(isc_event_counts);
		FB_ENTRYPOINT(isc_expand_dpb);
		FB_ENTRYPOINT(isc_modify_dpb);
		FB_ENTRYPOINT(isc_free);
		FB_ENTRYPOINT(isc_get_segment);
		FB_ENTRYPOINT(isc_get_slice);
		FB_ENTRYPOINT(isc_interprete);
		FB_ENTRYPOINT(isc_open_blob);
		FB_ENTRYPOINT(isc_open_blob2);
		FB_ENTRYPOINT(isc_prepare_transaction2);
		FB_ENTRYPOINT(isc_print_sqlerror);
		FB_ENTRYPOINT(isc_print_status);
		FB_ENTRYPOINT(isc_put_segment);
		FB_ENTRYPOINT(isc_put_slice);
		FB_ENTRYPOINT(isc_que_events);
		FB_ENTRYPOINT(isc_rollback_retaining);
		FB_ENTRYPOINT(isc_rollback_transaction);
		FB_ENTRYPOINT(isc_start_multiple);
		FB_ENTRYPOINT(isc_start_transaction);
		FB_ENTRYPOINT(isc_sqlcode);
		FB_ENTRYPOINT(isc_sql_interprete);
		FB_ENTRYPOINT(isc_transaction_info);
		FB_ENTRYPOINT(isc_transact_request);
		FB_ENTRYPOINT(isc_vax_integer);


		sIsLoaded = true;
		}
	else
		throw InternalException("GDS32 already loaded ?");

	return;
	}


/*
 *
 */
void FirebirdApiBinding::Unload()
	{
	if (sHandle != NULL)
		{
		FreeLibrary(sHandle);
		}
	
	sIsLoaded = false;
	sHandle = NULL;
	}


/*
 *
 */
bool		FirebirdApiBinding::IsLoaded()
	{ 
	return sIsLoaded; 
	}
