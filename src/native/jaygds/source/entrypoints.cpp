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

#include "entrypoints_generated.h"

#include "fb_binding.h"
#include "fb_helpers.h"
#include "handle_wrappers.h"
#include "jni_helpers.h"
#include "xsqlda_wrapper.h"

#include "ibase.h"

#include "jni.h"

#include <new>

// Dll Entrypoints




// First some basic helper functions for error handling and a macro to use for the
// catch block in each JNI entrypoint.


// Must be initilized in Java_org_firebirdsql_ngds_GDS_1Impl_nativeInitilize
JClassBinding  sInternalErrorClassBinding;
JClassBinding  sOutOfMemoryErrorClassBinding;




void EnsureJavaExceptionIssued(JNIEnv * javaEnvironment, InternalException& exception)
	{
	if( javaEnvironment->ExceptionCheck() == false ) 
		{
		JString messageJString(javaEnvironment, exception.getMessage());

		javaEnvironment->Throw( (jthrowable)sInternalErrorClassBinding.CreateNewInstance(javaEnvironment, "(Ljava/lang/String;)V", messageJString.AsJString()) );
		}
	}

void EnsureJavaExceptionIssued(JNIEnv * javaEnvironment)
	{
	if( javaEnvironment->ExceptionCheck() == false ) 
		{
		JString messageJString(javaEnvironment, "Unexpected exception caught.");

		javaEnvironment->Throw( (jthrowable)sInternalErrorClassBinding.CreateNewInstance(javaEnvironment, "(Ljava/lang/String;)V", messageJString.AsJString()) );
		}
	}

void MaybeIssueOutOfMemory(JNIEnv * javaEnvironment, std::bad_alloc& badAlloc)
	{
	if( javaEnvironment->ExceptionCheck() == false ) 
		{
		javaEnvironment->Throw( (jthrowable)sOutOfMemoryErrorClassBinding.CreateNewInstance(javaEnvironment, "()V") );
		}
	}


#define ENTER_PROTECTED_BLOCK try {
	                             

#define LEAVE_PROTECTED_BLOCK	}                                                               \
                                    catch(std::bad_alloc& badAlloc)									\
										{															\
										MaybeIssueOutOfMemory(javaEnvironment, badAlloc);			\
										}															\
									catch(InternalException& exception)								\
										{															\
										EnsureJavaExceptionIssued( javaEnvironment, exception );	\
										}															\
									catch( ... )													\
										{															\
										EnsureJavaExceptionIssued( javaEnvironment );				\
										} 


// A hack to ensure that nativeInitilize can be called multiple times
// until a client library is located.
bool sHasMostInitilizationBeenDone = false;

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_nativeInitilize
  (JNIEnv *javaEnvironment, jobject jThis, jstring firebirdDllName)
	{
	ENTER_PROTECTED_BLOCK
		if(sHasMostInitilizationBeenDone == false)
			{
			// Todo : If these fail then the exception handling for this method will not work.
			sInternalErrorClassBinding    = JClassBinding( javaEnvironment, "org/firebirdsql/ngds/InternalError" );
			sOutOfMemoryErrorClassBinding = JClassBinding( javaEnvironment, "java/lang/OutOfMemoryError" );

			JIscDatabaseHandle::Initilize(javaEnvironment);
			JIscTransactionHandle::Initilize(javaEnvironment);
			JIscStatementHandle::Initilize(javaEnvironment);
			JIscBlobHandle::Initilize(javaEnvironment);
			JIscServiceHandle::Initilize(javaEnvironment);
			JXSqlda::Initilize(javaEnvironment);
			FirebirdStatusVector::Initilize(javaEnvironment);

			sHasMostInitilizationBeenDone = true;
			}

		JString fileName( javaEnvironment, firebirdDllName );
		FirebirdApiBinding::Load(fileName.AsCString());
	LEAVE_PROTECTED_BLOCK
	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1create_1database
  (JNIEnv * javaEnvironment, jobject jThis, jstring jFileName, jobject jDatabaseHandle, jbyteArray jDpb)
	{

	ENTER_PROTECTED_BLOCK
		JString fileName( javaEnvironment, jFileName );
		JIscDatabaseHandle databaseHandle(javaEnvironment, jDatabaseHandle);
		JByteArray dpb( javaEnvironment, jDpb );

		FirebirdStatusVector status;
		isc_db_handle rawDatabaseHandle = databaseHandle.GetHandleValue();

		const char* const fileNameString = fileName.AsCString();

	
		FirebirdApiBinding::isc_create_database( status.RawAccess(), 0, const_cast<char*>(fileNameString), &rawDatabaseHandle, dpb.Size(), dpb.Read(), SQL_DIALECT_V6 );

		databaseHandle.SetHandleValue( rawDatabaseHandle );

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, databaseHandle);
	LEAVE_PROTECTED_BLOCK

	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1attach_1database
  (JNIEnv * javaEnvironment, jobject jThis, jstring jFileName, jobject jDatabaseHandle, jbyteArray jDpb)
	{
	ENTER_PROTECTED_BLOCK
		JString fileName( javaEnvironment, jFileName );
		JIscDatabaseHandle databaseHandle(javaEnvironment, jDatabaseHandle);
		JByteArray dpb( javaEnvironment, jDpb );

		FirebirdStatusVector status;
		isc_db_handle rawDatabaseHandle = databaseHandle.GetHandleValue();

		const char* const fileNameString = fileName.AsCString();

		FirebirdApiBinding::isc_attach_database( status.RawAccess(), 0, const_cast<char*>(fileNameString), &rawDatabaseHandle, dpb.Size(), dpb.Read() );

		databaseHandle.SetHandleValue( rawDatabaseHandle );

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, databaseHandle);
	LEAVE_PROTECTED_BLOCK

	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1database_1info
  (JNIEnv * javaEnvironment, jobject jThis, jobject jDatabaseHandle, jint jItemLength, jbyteArray jItems, jint jBufferLength, jbyteArray jBuffer)
	{
	ENTER_PROTECTED_BLOCK
		JIscDatabaseHandle databaseHandle(javaEnvironment, jDatabaseHandle);
		JByteArray items( javaEnvironment, jItems );
		JByteArray buffer( javaEnvironment, jBuffer );

		FirebirdStatusVector status;
		isc_db_handle rawDatabaseHandle = databaseHandle.GetHandleValue();

		FirebirdApiBinding::isc_database_info( status.RawAccess(), &rawDatabaseHandle, (short)jItemLength, items.Read(), (short)jBufferLength, buffer.Read() );

		databaseHandle.SetHandleValue( rawDatabaseHandle );

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, databaseHandle);
	LEAVE_PROTECTED_BLOCK

	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1detach_1database
  (JNIEnv * javaEnvironment, jobject jThis, jobject jDatabaseHandle)
	{
	ENTER_PROTECTED_BLOCK
		JIscDatabaseHandle databaseHandle(javaEnvironment, jDatabaseHandle);

		FirebirdStatusVector status;
		isc_db_handle rawDatabaseHandle = databaseHandle.GetHandleValue();

		FirebirdApiBinding::isc_detach_database( status.RawAccess(), &rawDatabaseHandle );

		databaseHandle.SetHandleValue( rawDatabaseHandle );

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, databaseHandle);
	LEAVE_PROTECTED_BLOCK

	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1drop_1database
  (JNIEnv * javaEnvironment, jobject jThis, jobject jDatabaseHandle)
	{
	ENTER_PROTECTED_BLOCK
		JIscDatabaseHandle databaseHandle(javaEnvironment, jDatabaseHandle);

		FirebirdStatusVector status;
		isc_db_handle rawDatabaseHandle = databaseHandle.GetHandleValue();

		FirebirdApiBinding::isc_drop_database( status.RawAccess(), &rawDatabaseHandle );

		databaseHandle.SetHandleValue( rawDatabaseHandle );

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, databaseHandle);
	LEAVE_PROTECTED_BLOCK

	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1start_1transaction
  (JNIEnv * javaEnvironment, jobject jThis, jobject jTransactionHandle, jobject jDatabaseHandle, jbyteArray jTpb)
	{
	ENTER_PROTECTED_BLOCK
		JIscTransactionHandle transactionHandle(javaEnvironment, jTransactionHandle);
		JIscDatabaseHandle databaseHandle(javaEnvironment, jDatabaseHandle);
		JByteArray tpb( javaEnvironment, jTpb );

		FirebirdStatusVector status;
		isc_db_handle rawDatabaseHandle = databaseHandle.GetHandleValue();
		isc_tr_handle rawTransactionHandle = transactionHandle.GetHandleValue();

		FirebirdApiBinding::isc_start_transaction( status.RawAccess(), &rawTransactionHandle, 1, &rawDatabaseHandle, tpb.Size(), tpb.Read()  );
		
		databaseHandle.SetHandleValue( rawDatabaseHandle );
		transactionHandle.SetHandleValue(rawTransactionHandle);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, databaseHandle);
	LEAVE_PROTECTED_BLOCK

	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1commit_1transaction
  (JNIEnv * javaEnvironment, jobject jThis, jobject jTransactionHandle)
	{
	ENTER_PROTECTED_BLOCK
		JIscTransactionHandle transactionHandle(javaEnvironment, jTransactionHandle);

		FirebirdStatusVector status;
		isc_tr_handle rawTransactionHandle = transactionHandle.GetHandleValue();
		
		FirebirdApiBinding::isc_commit_transaction( status.RawAccess(), &rawTransactionHandle );
		
		transactionHandle.SetHandleValue(rawTransactionHandle);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, transactionHandle);
	LEAVE_PROTECTED_BLOCK

	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1commit_1retaining
  (JNIEnv * javaEnvironment, jobject jThis, jobject jTransactionHandle)
	{
	ENTER_PROTECTED_BLOCK
		JIscTransactionHandle transactionHandle(javaEnvironment, jTransactionHandle);

		FirebirdStatusVector status;
		isc_tr_handle rawTransactionHandle = transactionHandle.GetHandleValue();
		
		FirebirdApiBinding::isc_commit_retaining( status.RawAccess(), &rawTransactionHandle );
		
		transactionHandle.SetHandleValue(rawTransactionHandle);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, transactionHandle);
	LEAVE_PROTECTED_BLOCK

	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1prepare_1transaction
 (JNIEnv * javaEnvironment, jobject jThis, jobject jTransactionHandle)
	{
	ENTER_PROTECTED_BLOCK
		JIscTransactionHandle transactionHandle(javaEnvironment, jTransactionHandle);

		FirebirdStatusVector status;
		isc_tr_handle rawTransactionHandle = transactionHandle.GetHandleValue();
		
		FirebirdApiBinding::isc_prepare_transaction2 ( status.RawAccess(), &rawTransactionHandle, 0, NULL );
		
		transactionHandle.SetHandleValue(rawTransactionHandle);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, transactionHandle);
	LEAVE_PROTECTED_BLOCK

	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1prepare_1transaction2
  (JNIEnv * javaEnvironment, jobject jThis, jobject jTransactionHandle, jbyteArray jBytes)
	{
	ENTER_PROTECTED_BLOCK
		JIscTransactionHandle transactionHandle(javaEnvironment, jTransactionHandle);

		FirebirdStatusVector status;
		isc_tr_handle rawTransactionHandle = transactionHandle.GetHandleValue();
		
		JByteArray tpb( javaEnvironment, jBytes );

		FirebirdApiBinding::isc_prepare_transaction2( status.RawAccess(), &rawTransactionHandle, tpb.Size(), (unsigned char*)tpb.Read() );
		
		transactionHandle.SetHandleValue(rawTransactionHandle);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, transactionHandle);
	LEAVE_PROTECTED_BLOCK

	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1rollback_1transaction
  (JNIEnv * javaEnvironment, jobject jThis, jobject jTransactionHandle)
	{
	ENTER_PROTECTED_BLOCK
		JIscTransactionHandle transactionHandle(javaEnvironment, jTransactionHandle);

		FirebirdStatusVector status;
		isc_tr_handle rawTransactionHandle = transactionHandle.GetHandleValue();
		
		FirebirdApiBinding::isc_rollback_transaction( status.RawAccess(), &rawTransactionHandle );
		
		transactionHandle.SetHandleValue(rawTransactionHandle);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, transactionHandle);
	LEAVE_PROTECTED_BLOCK

	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1rollback_1retaining
 (JNIEnv * javaEnvironment, jobject jThis, jobject jTransactionHandle)
	{
	ENTER_PROTECTED_BLOCK
		JIscTransactionHandle transactionHandle(javaEnvironment, jTransactionHandle);

		FirebirdStatusVector status;
		isc_tr_handle rawTransactionHandle = transactionHandle.GetHandleValue();
		
		FirebirdApiBinding::isc_rollback_retaining( status.RawAccess(), &rawTransactionHandle );
		
		transactionHandle.SetHandleValue(rawTransactionHandle);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, transactionHandle);
	LEAVE_PROTECTED_BLOCK

	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1dsql_1allocate_1statement
  (JNIEnv * javaEnvironment, jobject jThis, jobject jDatabaseHandle, jobject jStatementHandle )
	{
	ENTER_PROTECTED_BLOCK
		JIscDatabaseHandle databaseHandle(javaEnvironment, jDatabaseHandle);
		JIscStatementHandle statementHandle(javaEnvironment, jStatementHandle);

		FirebirdStatusVector status;
		isc_db_handle rawDatabaseHandle = databaseHandle.GetHandleValue();
		isc_stmt_handle rawStatementHandle = statementHandle.GetHandleValue();
		
		FirebirdApiBinding::isc_dsql_allocate_statement( status.RawAccess(), &rawDatabaseHandle, &rawStatementHandle );

		databaseHandle.SetHandleValue(rawDatabaseHandle);
		statementHandle.SetHandleValue(rawStatementHandle);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, databaseHandle);
	LEAVE_PROTECTED_BLOCK

	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1dsql_1free_1statement
  (JNIEnv *javaEnvironment, jobject jThis, jobject jStatementHandle, jint jValue)
	{
	ENTER_PROTECTED_BLOCK
		JIscStatementHandle statementHandle(javaEnvironment, jStatementHandle);

		FirebirdStatusVector status;
		
		isc_stmt_handle rawStatementHandle = statementHandle.GetHandleValue();
		
		FirebirdApiBinding::isc_dsql_free_statement( status.RawAccess(), &rawStatementHandle, jValue );

		
		statementHandle.SetHandleValue(rawStatementHandle);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, statementHandle);
	LEAVE_PROTECTED_BLOCK

	}




JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1dsql_1alloc_1statement2
  (JNIEnv * javaEnvironment, jobject jThis, jobject jDatabaseHandle, jobject jStatementHandle )
	{
	ENTER_PROTECTED_BLOCK
		JIscDatabaseHandle databaseHandle(javaEnvironment, jDatabaseHandle);
		JIscStatementHandle statementHandle(javaEnvironment, jStatementHandle);

		FirebirdStatusVector status;
		isc_db_handle rawDatabaseHandle = databaseHandle.GetHandleValue();
		isc_stmt_handle rawStatementHandle = statementHandle.GetHandleValue();
		
		FirebirdApiBinding::isc_dsql_alloc_statement2( status.RawAccess(), &rawDatabaseHandle, &rawStatementHandle );

		databaseHandle.SetHandleValue(rawDatabaseHandle);
		statementHandle.SetHandleValue(rawStatementHandle);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, databaseHandle);
	LEAVE_PROTECTED_BLOCK


	}

JNIEXPORT jobject JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1dsql_1prepare
  (JNIEnv *javaEnvironment, jobject jThis, jobject jTransactionHandle, jobject jStatementHandle, jbyteArray statement, jint dialect)

{
	ENTER_PROTECTED_BLOCK
		JIscTransactionHandle transactionHandle(javaEnvironment, jTransactionHandle);
		JIscStatementHandle statementHandle(javaEnvironment, jStatementHandle);
		JByteArray statementStringBytes( javaEnvironment, statement );

		FirebirdStatusVector status;
		isc_tr_handle rawTransactionHandle = transactionHandle.GetHandleValue();
		isc_stmt_handle rawStatementHandle = statementHandle.GetHandleValue();

	
		JXSqlda xsqlda(javaEnvironment);
		
		FirebirdApiBinding::isc_dsql_prepare( status.RawAccess(), &rawTransactionHandle, &rawStatementHandle, 0, statementStringBytes.Read(), dialect, xsqlda.RawAccess() );

		if(xsqlda.RawAccess()->sqln != xsqlda.RawAccess()->sqld )
			{
			xsqlda.Resize( xsqlda.RawAccess()->sqld );
			
				// Re-describe the statement. 
			FirebirdApiBinding::isc_dsql_describe( status.RawAccess(), &rawStatementHandle, dialect, xsqlda.RawAccess() );
			}


		transactionHandle.SetHandleValue(rawTransactionHandle);
		statementHandle.SetHandleValue(rawStatementHandle);

		jobject returnValue = xsqlda.AllocateJavaXSqlda(javaEnvironment);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, statementHandle);

		return returnValue;

	LEAVE_PROTECTED_BLOCK

	return NULL;

	}

JNIEXPORT jobject JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1dsql_1describe
  (JNIEnv * javaEnvironment, jobject jThis, jobject jStatementHandle, jint jDaVersion)
	{
	ENTER_PROTECTED_BLOCK
		JIscStatementHandle statementHandle(javaEnvironment, jStatementHandle);
	
		FirebirdStatusVector status;
		isc_stmt_handle rawStatementHandle = statementHandle.GetHandleValue();

		JXSqlda xsqlda(javaEnvironment);
		
		FirebirdApiBinding::isc_dsql_describe( status.RawAccess(), &rawStatementHandle, jDaVersion, xsqlda.RawAccess() );

		xsqlda.Resize( xsqlda.RawAccess()->sqld );

		FirebirdApiBinding::isc_dsql_describe_bind( status.RawAccess(), &rawStatementHandle, jDaVersion, xsqlda.RawAccess() );

		statementHandle.SetHandleValue(rawStatementHandle);

		jobject returnValue = xsqlda.AllocateJavaXSqlda(javaEnvironment);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, statementHandle);

		return returnValue;
	LEAVE_PROTECTED_BLOCK

	return NULL;
	}

JNIEXPORT jobject JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1dsql_1describe_1bind
  (JNIEnv * javaEnvironment, jobject jThis, jobject jStatementHandle, jint jDaVersion)
	{
	ENTER_PROTECTED_BLOCK
		JIscStatementHandle statementHandle(javaEnvironment, jStatementHandle);

		FirebirdStatusVector status;
		isc_stmt_handle rawStatementHandle = statementHandle.GetHandleValue();

		JXSqlda xsqlda(javaEnvironment);
		
		FirebirdApiBinding::isc_dsql_describe_bind( status.RawAccess(), &rawStatementHandle, jDaVersion, xsqlda.RawAccess() );

		xsqlda.Resize( xsqlda.RawAccess()->sqld );

		FirebirdApiBinding::isc_dsql_describe_bind( status.RawAccess(), &rawStatementHandle, jDaVersion, xsqlda.RawAccess() );

		statementHandle.SetHandleValue(rawStatementHandle);

		jobject returnValue = xsqlda.AllocateJavaXSqlda(javaEnvironment);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, statementHandle);

		return returnValue;
	LEAVE_PROTECTED_BLOCK

	return NULL;
	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1dsql_1execute2
(JNIEnv * javaEnvironment, jobject jThis, jobject jTransactionHandle, jobject jStatementHandle, jint jDaVersion, jobject jInXSqlda, jobject jOutXSqlda)
	{
	ENTER_PROTECTED_BLOCK
		JIscTransactionHandle transactionHandle(javaEnvironment, jTransactionHandle);
	
		JIscStatementHandle statementHandle(javaEnvironment, jStatementHandle);
		
		JXSqlda in_xsqlda( javaEnvironment, jInXSqlda );
		JXSqlda out_xsqlda( javaEnvironment, jOutXSqlda );

		FirebirdStatusVector status;
		isc_tr_handle rawTransactionHandle = transactionHandle.GetHandleValue();
		isc_stmt_handle rawStatementHandle = statementHandle.GetHandleValue();

		FirebirdApiBinding::isc_dsql_execute2( status.RawAccess(), &rawTransactionHandle, &rawStatementHandle, jDaVersion, in_xsqlda.RawAccess(), out_xsqlda.RawAccess() );

	
		transactionHandle.SetHandleValue(rawTransactionHandle);
		statementHandle.SetHandleValue(rawStatementHandle);

		in_xsqlda.Resync(javaEnvironment);
		out_xsqlda.Resync(javaEnvironment);


		
		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, statementHandle);
	LEAVE_PROTECTED_BLOCK


	}

JNIEXPORT jbyteArray JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1dsql_1sql_1info
  (JNIEnv *javaEnvironment, jobject jThis, jobject jStatementHandle, jbyteArray jItemsArray, jint jBufferLength)
{

ENTER_PROTECTED_BLOCK
		JIscStatementHandle statementHandle(javaEnvironment, jStatementHandle);

		JByteArray itemsArray( javaEnvironment, jItemsArray );

		JByteArray buffer( javaEnvironment, jBufferLength );
	
		FirebirdStatusVector status;
		isc_stmt_handle rawStatementHandle = statementHandle.GetHandleValue();

		FirebirdApiBinding::isc_dsql_sql_info( status.RawAccess(), &rawStatementHandle, itemsArray.Size(), itemsArray.Read(), buffer.Size(), buffer.Read() );

	
		statementHandle.SetHandleValue(rawStatementHandle);

		jbyteArray returnValue = buffer.GetHandle();
			
		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, statementHandle);

		return returnValue;
	LEAVE_PROTECTED_BLOCK

	return NULL;
	}


JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1dsql_1exec_1immed2
  (JNIEnv *javaEnvironment, jobject jThis, jobject jDatabaseHandle, jobject jTransactionHandle, jbyteArray jStatement, jint jDialect, jobject jInXsqlda, jobject jOutXsqlda)
{
	ENTER_PROTECTED_BLOCK
		JIscDatabaseHandle databaseHandle(javaEnvironment, jDatabaseHandle);
		JIscTransactionHandle transactionHandle(javaEnvironment, jTransactionHandle);
		JByteArray statementStringBytes( javaEnvironment, jStatement );

		
		
		JXSqlda in_xsqlda( javaEnvironment, jInXsqlda );
		JXSqlda out_xsqlda( javaEnvironment, jOutXsqlda );


		FirebirdStatusVector status;
		
	

		isc_db_handle rawDatabaseHandle = databaseHandle.GetHandleValue();
		isc_tr_handle rawTransactionHandle = transactionHandle.GetHandleValue();
	

		FirebirdApiBinding::isc_dsql_exec_immed2( status.RawAccess(), &rawDatabaseHandle, &rawTransactionHandle, 0, statementStringBytes.Read(), jDialect, in_xsqlda.RawAccess(), out_xsqlda.RawAccess() );

	
		databaseHandle.SetHandleValue(rawDatabaseHandle);
		transactionHandle.SetHandleValue(rawTransactionHandle);
	

		in_xsqlda.Resync(javaEnvironment);
		out_xsqlda.Resync(javaEnvironment);

		
		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, databaseHandle);
	LEAVE_PROTECTED_BLOCK

	}


JNIEXPORT jboolean JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1dsql_1fetch
  (JNIEnv *javaEnvironment, jobject jThis, jobject jStatementHandle, jint jDaVersion, jobject jXsqlda, jint jFetchSize)
{
ENTER_PROTECTED_BLOCK
		JIscStatementHandle statementHandle(javaEnvironment, jStatementHandle);
		
		
		JXSqlda out_xsqlda( javaEnvironment, jXsqlda, true );


		FirebirdStatusVector status;
		
		
		isc_stmt_handle rawStatementHandle = statementHandle.GetHandleValue();
	

		ISC_STATUS fetch_stat = FirebirdApiBinding::isc_dsql_fetch( status.RawAccess(), &rawStatementHandle, jDaVersion, out_xsqlda.RawAccess() );


		statementHandle.SetHandleValue(rawStatementHandle);
	
	
		out_xsqlda.Resync(javaEnvironment);


		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, statementHandle);

		if(fetch_stat == 100L)
			return JNI_FALSE;
		else
			return JNI_TRUE;
	LEAVE_PROTECTED_BLOCK


	return JNI_FALSE;
	}


JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1dsql_1set_1cursor_1name
  (JNIEnv *javaEnvironment, jobject jThis, jobject jStatementHandle , jstring jCursorName, jint jType)
{
	ENTER_PROTECTED_BLOCK
		JIscStatementHandle statementHandle(javaEnvironment, jStatementHandle);
		JString cursornameString( javaEnvironment, jCursorName );
		
	
		FirebirdStatusVector status;
		
		const char* const cursorname = cursornameString.AsCString();

		
		isc_stmt_handle rawStatementHandle = statementHandle.GetHandleValue();
	

		FirebirdApiBinding::isc_dsql_set_cursor_name( status.RawAccess(), &rawStatementHandle, const_cast<char*>(cursorname), jType );

	
		statementHandle.SetHandleValue(rawStatementHandle);
	
		
		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, statementHandle);
	LEAVE_PROTECTED_BLOCK

	}


JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1create_1blob2
  (JNIEnv *javaEnvironment, jobject jThis, jobject jDatabaseHandle, jobject jTransctionHandle, jobject jBlobHandle, jbyteArray jClumpetBytes)
	{
	ENTER_PROTECTED_BLOCK
		JIscDatabaseHandle databaseHandle(javaEnvironment, jDatabaseHandle);
		JIscTransactionHandle transactionHandle(javaEnvironment, jTransctionHandle);
		JIscBlobHandle blobHandle(javaEnvironment, jBlobHandle);
		JByteArray clumpetBytes(javaEnvironment, jClumpetBytes);
		
	
		FirebirdStatusVector status;
		
		
		isc_db_handle rawDatabaseHandle = databaseHandle.GetHandleValue();
		isc_tr_handle rawTransactionHandle = transactionHandle.GetHandleValue();
		isc_blob_handle rawBlobHandle = blobHandle.GetHandleValue();
		ISC_QUAD rawBlobId = blobHandle.GetId();
		

		FirebirdApiBinding::isc_create_blob2( status.RawAccess(), &rawDatabaseHandle, &rawTransactionHandle, &rawBlobHandle, &rawBlobId, clumpetBytes.Size(), clumpetBytes.Read() );

	
		databaseHandle.SetHandleValue(rawDatabaseHandle);
		transactionHandle.SetHandleValue(rawTransactionHandle);
		blobHandle.SetHandleValue(rawBlobHandle);
		blobHandle.SetId(rawBlobId);
	
		
		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, databaseHandle);
	LEAVE_PROTECTED_BLOCK

	}


JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1open_1blob2
  (JNIEnv *javaEnvironment, jobject jThis, jobject jDatabaseHandle, jobject jTransctionHandle, jobject jBlobHandle, jbyteArray jClumpetBytes)
	{
	ENTER_PROTECTED_BLOCK
		JIscDatabaseHandle databaseHandle(javaEnvironment, jDatabaseHandle);
		JIscTransactionHandle transactionHandle(javaEnvironment, jTransctionHandle);
		JIscBlobHandle blobHandle(javaEnvironment, jBlobHandle);
		JByteArray clumpetBytes(javaEnvironment, jClumpetBytes);
		
	
		FirebirdStatusVector status;
		
		
		isc_db_handle rawDatabaseHandle = databaseHandle.GetHandleValue();
		isc_tr_handle rawTransactionHandle = transactionHandle.GetHandleValue();
		isc_blob_handle rawBlobHandle = blobHandle.GetHandleValue();
		ISC_QUAD rawBlobId = blobHandle.GetId();
		

		FirebirdApiBinding::isc_open_blob2( status.RawAccess(), &rawDatabaseHandle, &rawTransactionHandle, &rawBlobHandle, &rawBlobId, clumpetBytes.Size(), (unsigned char*)clumpetBytes.Read() );

	
		databaseHandle.SetHandleValue(rawDatabaseHandle);
		transactionHandle.SetHandleValue(rawTransactionHandle);
		blobHandle.SetHandleValue(rawBlobHandle);
		blobHandle.SetId(rawBlobId);
	
		
		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, databaseHandle);
	LEAVE_PROTECTED_BLOCK

	}


JNIEXPORT jbyteArray JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1get_1segment
  (JNIEnv *javaEnvironment, jobject jThis, jobject jBlobHandle, jint jMaxRead)
{
	ENTER_PROTECTED_BLOCK
		JIscBlobHandle blobHandle(javaEnvironment, jBlobHandle);
		
	
		FirebirdStatusVector status;
		
		
		isc_blob_handle rawBlobHandle = blobHandle.GetHandleValue();
		ISC_QUAD rawBlobId = blobHandle.GetId();
		
		
	
		Buffer buffer(jMaxRead);

		unsigned short lengthRead = 0;

		ISC_STATUS statusPart = FirebirdApiBinding::isc_get_segment( status.RawAccess(), &rawBlobHandle, &lengthRead, jMaxRead, buffer.access() );

		JByteArray returnBytes(javaEnvironment, buffer.access(), lengthRead);

		blobHandle.SetHandleValue(rawBlobHandle);
		blobHandle.SetId(rawBlobId);

		jbyteArray returnValue = returnBytes.GetHandle();

		if( statusPart == isc_segstr_eof )
			blobHandle.SetIsEndOfFile(true);
		else 
			{
			blobHandle.SetIsEndOfFile(false);
			
            if( statusPart != isc_segment )
				status.IssueExceptionsAndOrAddWarnings(javaEnvironment, blobHandle);
			}

		return returnValue;
	LEAVE_PROTECTED_BLOCK


	return NULL;
	}	


JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1put_1segment
  (JNIEnv *javaEnvironment, jobject jThis, jobject jBlobHandle, jbyteArray jBytesToWrite)
	{
	ENTER_PROTECTED_BLOCK
		JIscBlobHandle blobHandle(javaEnvironment, jBlobHandle);
		JByteArray bytesToWrite(javaEnvironment, jBytesToWrite);

		FirebirdStatusVector status;
		
		
		isc_blob_handle rawBlobHandle = blobHandle.GetHandleValue();
		ISC_QUAD rawBlobId = blobHandle.GetId();
		
		FirebirdApiBinding::isc_put_segment( status.RawAccess(), &rawBlobHandle, bytesToWrite.Size(), bytesToWrite.Read() );

		blobHandle.SetHandleValue(rawBlobHandle);
		blobHandle.SetId(rawBlobId);
			
		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, blobHandle);
	LEAVE_PROTECTED_BLOCK

	}


JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1close_1blob
  (JNIEnv *javaEnvironment, jobject jThis, jobject jBlobHandle)
	{
	ENTER_PROTECTED_BLOCK
		JIscBlobHandle blobHandle(javaEnvironment, jBlobHandle);
	
		FirebirdStatusVector status;
		
		
		isc_blob_handle rawBlobHandle = blobHandle.GetHandleValue();
		ISC_QUAD rawBlobId = blobHandle.GetId();
		
		FirebirdApiBinding::isc_close_blob( status.RawAccess(), &rawBlobHandle );
	
		blobHandle.SetHandleValue(rawBlobHandle);
		blobHandle.SetId(rawBlobId);
			
		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, blobHandle);
	LEAVE_PROTECTED_BLOCK

	}


JNIEXPORT jbyteArray JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1blob_1info
  (JNIEnv *javaEnvironment, jobject jThis, jobject jBlobHandle, jbyteArray jItemsArrayHandle, jint jBufferLength)
	{
	ENTER_PROTECTED_BLOCK
		JIscBlobHandle blobHandle(javaEnvironment, jBlobHandle);
	    JByteArray bytesToWrite(javaEnvironment, jItemsArrayHandle);
	
		FirebirdStatusVector status;
		
		
		isc_blob_handle rawBlobHandle = blobHandle.GetHandleValue();
		ISC_QUAD rawBlobId = blobHandle.GetId();

		char* resultBuffer = (char*)alloca(jBufferLength);
		
		FirebirdApiBinding::isc_blob_info( status.RawAccess(), &rawBlobHandle, bytesToWrite.Size(), bytesToWrite.Read(), jBufferLength, resultBuffer );
	

		blobHandle.SetHandleValue(rawBlobHandle);
		blobHandle.SetId(rawBlobId);

		JByteArray returnBytes(javaEnvironment, resultBuffer, jBufferLength);
			
		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, blobHandle);

		return returnBytes.GetHandle();
	LEAVE_PROTECTED_BLOCK
	return NULL;
	}


JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1seek_1blob
  (JNIEnv *javaEnvironment, jobject jThis, jobject jBlobHandle, jint position, jint mode)
	{
	ENTER_PROTECTED_BLOCK
		JIscBlobHandle blobHandle(javaEnvironment, jBlobHandle);
	
		FirebirdStatusVector status;
		
		
		isc_blob_handle rawBlobHandle = blobHandle.GetHandleValue();
		ISC_QUAD rawBlobId = blobHandle.GetId();

		ISC_LONG result;
		
		FirebirdApiBinding::isc_seek_blob( status.RawAccess(), &rawBlobHandle, mode, position, &result );
	
		blobHandle.SetHandleValue(rawBlobHandle);
		blobHandle.SetId(rawBlobId);
			
		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, blobHandle);
	LEAVE_PROTECTED_BLOCK
	}


JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1service_1attach
  (JNIEnv *javaEnvironment, jobject jThis, jstring jServiceString, jobject jServiceHandle, jbyteArray jServiceParameterBuffer)
	{
	ENTER_PROTECTED_BLOCK
		JIscServiceHandle serviceHandle(javaEnvironment, jServiceHandle);
		JString serviceString(javaEnvironment, jServiceString);
		JByteArray serviceParameterBuffer(javaEnvironment, jServiceParameterBuffer);

		FirebirdStatusVector status;

		isc_svc_handle rawServiceHandle = serviceHandle.GetHandleValue();

		FirebirdApiBinding::isc_service_attach( status.RawAccess(), serviceString.GetLength(), (char*)serviceString.AsCString(),
			&rawServiceHandle, serviceParameterBuffer.Size(), serviceParameterBuffer.Read() );


		serviceHandle.SetHandleValue(rawServiceHandle);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, serviceHandle);
	LEAVE_PROTECTED_BLOCK
	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1service_1detach
  (JNIEnv *javaEnvironment, jobject jThis, jobject jServiceHandle)
	{
	ENTER_PROTECTED_BLOCK
		JIscServiceHandle serviceHandle(javaEnvironment, jServiceHandle);

		FirebirdStatusVector status;

		isc_svc_handle rawServiceHandle = serviceHandle.GetHandleValue();

		FirebirdApiBinding::isc_service_detach( status.RawAccess(), &rawServiceHandle );

		serviceHandle.SetHandleValue(rawServiceHandle);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, serviceHandle);
	LEAVE_PROTECTED_BLOCK
	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1service_1start
  (JNIEnv *javaEnvironment, jobject jThis, jobject jServiceHandle, jbyteArray jServiceParameterBuffer)
	{
	ENTER_PROTECTED_BLOCK
		JIscServiceHandle serviceHandle(javaEnvironment, jServiceHandle);
		JByteArray serviceParameterBuffer(javaEnvironment, jServiceParameterBuffer);

		FirebirdStatusVector status;

		isc_svc_handle rawServiceHandle = serviceHandle.GetHandleValue();

		FirebirdApiBinding::isc_service_start( status.RawAccess(), &rawServiceHandle, NULL, serviceParameterBuffer.Size(), serviceParameterBuffer.Read() );

		serviceHandle.SetHandleValue(rawServiceHandle);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, serviceHandle);
	LEAVE_PROTECTED_BLOCK
	}

JNIEXPORT void JNICALL Java_org_firebirdsql_ngds_GDS_1Impl_native_1isc_1service_1query
  (JNIEnv *javaEnvironment, jobject jThis, jobject jServiceHandle, jbyteArray jSendServiceParameterBuffer, 
   jbyteArray jRequestServiceParameterBuffer, jbyteArray jResultBuffer)
	{
	ENTER_PROTECTED_BLOCK
		JIscServiceHandle serviceHandle(javaEnvironment, jServiceHandle);

		JByteArray sendParameterBuffer(javaEnvironment, jSendServiceParameterBuffer);
		JByteArray requestParameterBuffer(javaEnvironment, jRequestServiceParameterBuffer);

		JByteArray resultBuffer(javaEnvironment, jResultBuffer);

		FirebirdStatusVector status;

		isc_svc_handle rawServiceHandle = serviceHandle.GetHandleValue();

		FirebirdApiBinding::isc_service_query( status.RawAccess(), &rawServiceHandle, NULL, sendParameterBuffer.Size(), sendParameterBuffer.Read(),
			requestParameterBuffer.Size(), requestParameterBuffer.Read(), resultBuffer.Size(), resultBuffer.Read());


		serviceHandle.SetHandleValue(rawServiceHandle);

		status.IssueExceptionsAndOrAddWarnings(javaEnvironment, serviceHandle);
	LEAVE_PROTECTED_BLOCK
	}
