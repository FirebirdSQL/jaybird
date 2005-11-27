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

#ifndef _JNGDS__FirebirdBinding
#define _JNGDS__FirebirdBinding


#include "ibase.h"
#include "jni.h"


typedef ISC_STATUS ISC_EXPORT prototype_isc_attach_database(ISC_STATUS *,
										  short,
										  char *,
										  isc_db_handle *,
										  short,
										  char *);


typedef ISC_STATUS ISC_EXPORT prototype_isc_blob_info(ISC_STATUS *,
									isc_blob_handle *,
									short,
									char *,
									short,
									char *);


typedef ISC_STATUS ISC_EXPORT prototype_isc_cancel_blob(ISC_STATUS *,
									  isc_blob_handle *);


typedef ISC_STATUS ISC_EXPORT prototype_isc_close_blob(ISC_STATUS *,
									 isc_blob_handle *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_commit_retaining(ISC_STATUS *,
										   isc_tr_handle *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_commit_transaction(ISC_STATUS *,
											 isc_tr_handle *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_create_blob(ISC_STATUS *,
									  isc_db_handle *,
									  isc_tr_handle *,
									  isc_blob_handle *,
									  ISC_QUAD *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_create_blob2(ISC_STATUS *,
									   isc_db_handle *,
									   isc_tr_handle *,
									   isc_blob_handle *,
									   ISC_QUAD *,
									   short,
									   char *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_create_database(ISC_STATUS *,
										  short,
										  char *,
										  isc_db_handle *,
										  short,
										  char *,
										  short);

typedef ISC_STATUS ISC_EXPORT prototype_isc_database_info(ISC_STATUS *,
										isc_db_handle *,
										short,
										char *,
										short,
										char *);

typedef void ISC_EXPORT prototype_isc_decode_date(ISC_QUAD *,
								void *);

typedef void ISC_EXPORT prototype_isc_decode_sql_date(ISC_DATE *,
									void *);

typedef void ISC_EXPORT prototype_isc_decode_sql_time(ISC_TIME *,
									void *);

typedef void ISC_EXPORT prototype_isc_decode_timestamp(ISC_TIMESTAMP *,
									 void *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_detach_database(ISC_STATUS *,
										  isc_db_handle *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_drop_database(ISC_STATUS *,
										isc_db_handle *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_allocate_statement(ISC_STATUS *,
												  isc_db_handle *,
												  isc_stmt_handle *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_alloc_statement2(ISC_STATUS *,
												isc_db_handle *,
												isc_stmt_handle *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_describe(ISC_STATUS *,
										isc_stmt_handle *,
										unsigned short,
										XSQLDA *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_describe_bind(ISC_STATUS *,
											 isc_stmt_handle *,
											 unsigned short,
											 XSQLDA *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_exec_immed2(ISC_STATUS *,
										   isc_db_handle *,
										   isc_tr_handle *,
										   unsigned short,
										   char *,
										   unsigned short,
										   XSQLDA *,
										   XSQLDA *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_execute(ISC_STATUS *,
									   isc_tr_handle *,
									   isc_stmt_handle *,
									   unsigned short,
									   XSQLDA *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_execute2(ISC_STATUS *,
										isc_tr_handle *,
										isc_stmt_handle *,
										unsigned short,
										XSQLDA *,
										XSQLDA *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_execute_immediate(ISC_STATUS *,
												 isc_db_handle *,
												 isc_tr_handle *,
												 unsigned short,
												 char *,
												 unsigned short,
												 XSQLDA *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_fetch(ISC_STATUS *,
									 isc_stmt_handle *,
									 unsigned short,
									 XSQLDA *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_finish(isc_db_handle *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_free_statement(ISC_STATUS *,
											  isc_stmt_handle *,
											  unsigned short);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_insert(ISC_STATUS *,
									  isc_stmt_handle *,
									  unsigned short,
									  XSQLDA *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_prepare(ISC_STATUS *,
									   isc_tr_handle *,
									   isc_stmt_handle *,
									   unsigned short,
									   char *,
									   unsigned short,
									   XSQLDA *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_set_cursor_name(ISC_STATUS *,
											   isc_stmt_handle *,
											   char *,
											   unsigned short);

typedef ISC_STATUS ISC_EXPORT prototype_isc_dsql_sql_info(ISC_STATUS *,
										isc_stmt_handle *,
										short,
										const char *,
										short,
										char *);

typedef void ISC_EXPORT prototype_isc_encode_date(void *,
								ISC_QUAD *);

typedef void ISC_EXPORT prototype_isc_encode_sql_date(void *,
									ISC_DATE *);

typedef void ISC_EXPORT prototype_isc_encode_sql_time(void *,
									ISC_TIME *);

typedef void ISC_EXPORT prototype_isc_encode_timestamp(void *,
									 ISC_TIMESTAMP *);




typedef ISC_STATUS ISC_EXPORT prototype_isc_get_segment(ISC_STATUS *,
									  isc_blob_handle *,
									  unsigned short *,
									  unsigned short,
									  char *);


typedef ISC_STATUS ISC_EXPORT prototype_isc_interprete(char *,
									 ISC_STATUS * *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_open_blob(ISC_STATUS *,
									isc_db_handle *,
									isc_tr_handle *,
									isc_blob_handle *,
									ISC_QUAD *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_open_blob2(ISC_STATUS *,
									 isc_db_handle *,
									 isc_tr_handle *,
									 isc_blob_handle *,
									 ISC_QUAD *,
									 ISC_USHORT,
									 ISC_UCHAR *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_prepare_transaction2(ISC_STATUS *,
											   isc_tr_handle *,
											   ISC_USHORT,
											   ISC_UCHAR *);

typedef void ISC_EXPORT prototype_isc_print_sqlerror(ISC_SHORT,
								   ISC_STATUS *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_print_status(ISC_STATUS *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_put_segment(ISC_STATUS *,
									  isc_blob_handle *,
									  unsigned short,
									  char *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_put_slice(ISC_STATUS *,
									isc_db_handle *,
									isc_tr_handle *,
									ISC_QUAD *,
									short,
									char *,
									short,
									ISC_LONG *,
									ISC_LONG,
									void *);


typedef ISC_STATUS ISC_EXPORT prototype_isc_rollback_retaining(ISC_STATUS *,
											 isc_tr_handle *);


typedef ISC_STATUS ISC_EXPORT prototype_isc_rollback_transaction(ISC_STATUS *,
											   isc_tr_handle *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_start_multiple(ISC_STATUS *,
										 isc_tr_handle *,
										 short,
										 void *);

typedef ISC_STATUS ISC_EXPORT_VARARG prototype_isc_start_transaction(ISC_STATUS *,
												   isc_tr_handle *,
												   short, ...);

typedef ISC_STATUS ISC_EXPORT_VARARG prototype_isc_reconnect_transaction(ISC_STATUS *,
                                                   isc_db_handle *,
                                                   isc_tr_handle *,
                                                   short, 
                                                   char*);


typedef ISC_LONG ISC_EXPORT prototype_isc_sqlcode(ISC_STATUS *);

typedef void ISC_EXPORT prototype_isc_sql_interprete(short,
								   char *,
								   short);




typedef ISC_STATUS ISC_EXPORT prototype_isc_seek_blob(ISC_STATUS *,
									isc_blob_handle *,
									short,
									ISC_LONG,
									ISC_LONG *);



typedef ISC_STATUS ISC_EXPORT prototype_isc_service_attach(ISC_STATUS *,
										 unsigned short,
										 char *,
										 isc_svc_handle *,
										 unsigned short,
										 char *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_service_detach(ISC_STATUS *,
										 isc_svc_handle *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_service_query(ISC_STATUS *,
										isc_svc_handle *,
										isc_resv_handle *,
										unsigned short,
										char *,
										unsigned short,
										char *,
										unsigned short,
										char *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_service_start(ISC_STATUS *,
										isc_svc_handle *,
										isc_resv_handle *,
										unsigned short,
										char *);

typedef ISC_STATUS ISC_EXPORT prototype_isc_transaction_info(ISC_STATUS *,
										isc_tr_handle *,
										short,
										char *,
										short,
										char *);
										
typedef ISC_STATUS ISC_EXPORT prototype_isc_que_events(ISC_STATUS*,
        isc_db_handle*,
        ISC_LONG*,
        short,
        char*,
        isc_callback,
        void*);

typedef long ISC_EXPORT prototype_isc_event_block(
        char** ,
        char** ,
        unsigned short ,
        ...);

typedef ISC_STATUS ISC_EXPORT prototype_isc_wait_for_event(
        ISC_STATUS*,
        isc_db_handle*,
        short,
        char*,
        char*);

typedef void ISC_EXPORT prototype_isc_event_counts(
        ISC_STATUS*,
        short,
        char*,
        char*);

typedef void ISC_EXPORT prototype_isc_cancel_events(
        ISC_STATUS*,
        isc_db_handle*,
        ISC_LONG*);

typedef void ISC_EXPORT prototype_isc_free(char *);





class FirebirdApiBinding
		{
		public:
		
		static void		Load(const char* const firebirdDllName);

		static void		Unload();

			
		static bool		IsLoaded();
	
		static prototype_isc_attach_database*		isc_attach_database;
		static prototype_isc_blob_info*				isc_blob_info;
		static prototype_isc_cancel_blob*			isc_cancel_blob;
		static prototype_isc_close_blob*			isc_close_blob;
		static prototype_isc_commit_retaining*		isc_commit_retaining;
		static prototype_isc_commit_transaction*	isc_commit_transaction;
		static prototype_isc_create_blob*			isc_create_blob;
		static prototype_isc_create_blob2*			isc_create_blob2;
		static prototype_isc_create_database*		isc_create_database;
		static prototype_isc_database_info*			isc_database_info;
		static prototype_isc_decode_date*			isc_decode_date;
		static prototype_isc_decode_sql_date*		isc_decode_sql_date;
		static prototype_isc_decode_sql_time*		isc_decode_sql_time;
		static prototype_isc_decode_timestamp*		isc_decode_timestamp;
		static prototype_isc_detach_database*		isc_detach_database;
		static prototype_isc_drop_database*			isc_drop_database;
		static prototype_isc_dsql_allocate_statement*	isc_dsql_allocate_statement;
		static prototype_isc_dsql_alloc_statement2*	isc_dsql_alloc_statement2;
		static prototype_isc_dsql_describe*			isc_dsql_describe;
		static prototype_isc_dsql_describe_bind*	isc_dsql_describe_bind;
		static prototype_isc_dsql_exec_immed2*		isc_dsql_exec_immed2; 
		static prototype_isc_dsql_execute*			isc_dsql_execute; 
		static prototype_isc_dsql_execute2*			isc_dsql_execute2;
		static prototype_isc_dsql_execute_immediate*	isc_dsql_execute_immediate;
		static prototype_isc_dsql_fetch*			isc_dsql_fetch;
		static prototype_isc_dsql_finish*			isc_dsql_finish;
		static prototype_isc_dsql_free_statement*	isc_dsql_free_statement;
		static prototype_isc_dsql_insert*			isc_dsql_insert; 
		static prototype_isc_dsql_prepare*			isc_dsql_prepare;
		static prototype_isc_dsql_set_cursor_name*	isc_dsql_set_cursor_name;
		static prototype_isc_dsql_sql_info*			isc_dsql_sql_info;
		static prototype_isc_encode_date*			isc_encode_date;
		static prototype_isc_encode_sql_date*		isc_encode_sql_date;
		static prototype_isc_encode_sql_time*		isc_encode_sql_time;
		static prototype_isc_encode_timestamp*		isc_encode_timestamp;
		static prototype_isc_get_segment*			isc_get_segment;
		static prototype_isc_interprete*			isc_interprete;
		static prototype_isc_open_blob*				isc_open_blob;
		static prototype_isc_open_blob2*			isc_open_blob2;
		static prototype_isc_prepare_transaction2*	isc_prepare_transaction2;
		static prototype_isc_print_sqlerror*		isc_print_sqlerror;
		static prototype_isc_print_status*			isc_print_status;
		static prototype_isc_put_segment*			isc_put_segment;
		static prototype_isc_rollback_retaining*	isc_rollback_retaining;
		static prototype_isc_rollback_transaction*	isc_rollback_transaction;
		static prototype_isc_start_transaction*		isc_start_transaction;
		static prototype_isc_reconnect_transaction*		isc_reconnect_transaction;
		static prototype_isc_sqlcode*				isc_sqlcode;
		static prototype_isc_sql_interprete*		isc_sql_interprete;
		static prototype_isc_seek_blob*				isc_seek_blob;
		static prototype_isc_service_attach*			isc_service_attach;
		static prototype_isc_service_detach*			isc_service_detach;
		static prototype_isc_service_query*			isc_service_query;
		static prototype_isc_service_start*			isc_service_start;
		static prototype_isc_transaction_info*		isc_transaction_info;
                static prototype_isc_que_events*                        isc_que_events;
                static prototype_isc_event_block*                       isc_event_block;
                static prototype_isc_wait_for_event*                    isc_wait_for_event;
                static prototype_isc_event_counts*                      isc_event_counts;
                static prototype_isc_cancel_events*                     isc_cancel_events;
                static prototype_isc_free*                              isc_free;

	

		
		private:
		// Constructor & Destructor
		FirebirdApiBinding();
		~FirebirdApiBinding() ;
		

		static SHARED_LIBRARY_HANDLE sHandle;
		static bool		sIsLoaded;
		};

#endif
