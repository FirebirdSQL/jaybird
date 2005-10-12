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

#include "handle_wrappers.h"

#include "jni_helpers.h"
#include "exceptions.h"

#include "ibase.h"
#include "jni.h"

#include <algorithm>



// JIscDatabaseHandle Class -------------------------------------------------------------------------------------

// Static Members

JClassBinding  JIscDatabaseHandle::sClassBinding;
	
JMethodBinding JIscDatabaseHandle::sMethodBinding_GetHandle;
JMethodBinding JIscDatabaseHandle::sMethodBinding_SetHandle;

JMethodBinding JIscDatabaseHandle::sMethodBinding_AddWarning;

bool	JIscDatabaseHandle::sIsInitilized = false;


// Static Methods

/*
 *
 */
void		JIscDatabaseHandle::Initilize( JNIEnv* javaEnvironment )
	{
	if( sIsInitilized )
		throw InternalException("Initilize has been called twice without an unitilize.");

	sClassBinding = JClassBinding( javaEnvironment, "org/firebirdsql/ngds/isc_db_handle_impl" );

	sMethodBinding_SetHandle = sClassBinding.GetMethodBinding( javaEnvironment, "setRdb_id", "(I)V" );
	sMethodBinding_GetHandle = sClassBinding.GetMethodBinding( javaEnvironment, "getRdb_id", "()I" );

	sMethodBinding_AddWarning = sClassBinding.GetMethodBinding( javaEnvironment, "addWarning", "(Lorg/firebirdsql/gds/GDSException;)V" );


	sIsInitilized = true;
	}


// Methods

/*
 *
 */
JIscDatabaseHandle::JIscDatabaseHandle( JNIEnv* javaEnvironment, jobject objectHandlle ) :
	mJavaEnvironment( javaEnvironment )	,
	mJavaObjectHandle( objectHandlle )
	{
	}


/*
 *
 */	
JIscDatabaseHandle::JIscDatabaseHandle( JNIEnv* javaEnvironment ) :
	mJavaEnvironment(javaEnvironment)	
	{
	mJavaObjectHandle = sClassBinding.CreateNewInstance(javaEnvironment, "()v");
	}

/*
 *
 */
JIscDatabaseHandle::~JIscDatabaseHandle()
	{
	}

/*
 *
 */
void JIscDatabaseHandle::SetHandleValue( isc_db_handle handle )
	{
	sMethodBinding_SetHandle.CallVoid( mJavaEnvironment, mJavaObjectHandle, handle );
	}

/*
 *
 */	
isc_db_handle		JIscDatabaseHandle::GetHandleValue()
	{
	return (isc_db_handle)sMethodBinding_GetHandle.CallInteger( mJavaEnvironment, mJavaObjectHandle );
	}

/*
 *
 */
void		JIscDatabaseHandle::AddWarning( jthrowable warning )
	{
	sMethodBinding_AddWarning.CallVoid( mJavaEnvironment, mJavaObjectHandle, warning );
	}
	

	
	

// JIscTransactionHandle Class -------------------------------------------------------------------------------------

// Static Members

JClassBinding  JIscTransactionHandle::sClassBinding;
	
JMethodBinding JIscTransactionHandle::sMethodBinding_GetHandle;
JMethodBinding JIscTransactionHandle::sMethodBinding_SetHandle;
JMethodBinding JIscTransactionHandle::sMethodBinding_AddWarning;

bool	JIscTransactionHandle::sIsInitilized = false;


// Static Methods

/*
 *
 */
void		JIscTransactionHandle::Initilize( JNIEnv* javaEnvironment )
	{
	if( sIsInitilized )
		throw InternalException("Initilize has been called twice without an unitilize.");

	sClassBinding = JClassBinding( javaEnvironment, "org/firebirdsql/ngds/isc_tr_handle_impl" );

	sMethodBinding_SetHandle = sClassBinding.GetMethodBinding( javaEnvironment, "setTransactionId", "(I)V" );
	sMethodBinding_GetHandle = sClassBinding.GetMethodBinding( javaEnvironment, "getTransactionId", "()I" );

	sMethodBinding_AddWarning = sClassBinding.GetMethodBinding( javaEnvironment, "addWarning", "(Lorg/firebirdsql/gds/GDSException;)V" );


	sIsInitilized = true;
	}	


// Members

/*
 *
 */
JIscTransactionHandle::JIscTransactionHandle( JNIEnv* javaEnvironment, jobject objectHandlle ) :
	mJavaEnvironment( javaEnvironment )	,
	mJavaObjectHandle( objectHandlle )
	{
	}


/*
 *
 */	
JIscTransactionHandle::JIscTransactionHandle( JNIEnv* javaEnvironment ) :
	mJavaEnvironment(javaEnvironment)	
	{
	mJavaObjectHandle = sClassBinding.CreateNewInstance(javaEnvironment, "()v");
	}

	
/*
 *
 */
JIscTransactionHandle::~JIscTransactionHandle()
	{
	}

/*
 *
 */
void JIscTransactionHandle::SetHandleValue( isc_tr_handle handle )
	{
	sMethodBinding_SetHandle.CallVoid( mJavaEnvironment, mJavaObjectHandle, handle );
	}

/*
 *
 */	
isc_tr_handle		JIscTransactionHandle::GetHandleValue()
	{
	return (isc_db_handle)sMethodBinding_GetHandle.CallInteger( mJavaEnvironment, mJavaObjectHandle );
	}
	
/*
 *
 */
void		JIscTransactionHandle::AddWarning( jthrowable warning )
	{
	sMethodBinding_AddWarning.CallVoid( mJavaEnvironment, mJavaObjectHandle, warning );
	}
	



// JIscStatementHandle Class -------------------------------------------------------------------------------------

// Static Members

JClassBinding  JIscStatementHandle::sClassBinding;
	
JMethodBinding JIscStatementHandle::sMethodBinding_GetHandle;
JMethodBinding JIscStatementHandle::sMethodBinding_SetHandle;
JMethodBinding JIscStatementHandle::sMethodBinding_AddWarning;

bool	JIscStatementHandle::sIsInitilized = false;


// Static Methods

/*
 *
 */
void		JIscStatementHandle::Initilize( JNIEnv* javaEnvironment )
	{
	if( sIsInitilized )
		throw InternalException("Initilize has been called twice without an unitilize.");

	sClassBinding = JClassBinding( javaEnvironment, "org/firebirdsql/ngds/isc_stmt_handle_impl" );

	sMethodBinding_SetHandle = sClassBinding.GetMethodBinding( javaEnvironment, "setRsr_id", "(I)V" );
	sMethodBinding_GetHandle = sClassBinding.GetMethodBinding( javaEnvironment, "getRsr_id", "()I" );

		sMethodBinding_AddWarning = sClassBinding.GetMethodBinding( javaEnvironment, "addWarning", "(Lorg/firebirdsql/gds/GDSException;)V" );


	sIsInitilized = true;

	}


// Members

/*
 *
 */
JIscStatementHandle::JIscStatementHandle( JNIEnv* javaEnvironment, jobject objectHandlle ) :
	mJavaEnvironment( javaEnvironment )	,
	mJavaObjectHandle( objectHandlle )
	{
	}

/*
 *
 */
JIscStatementHandle::JIscStatementHandle( JNIEnv* javaEnvironment ) :
	mJavaEnvironment(javaEnvironment)	
	{
	mJavaObjectHandle = sClassBinding.CreateNewInstance(javaEnvironment, "()v");
	}

/*
 *
 */
JIscStatementHandle::~JIscStatementHandle()
{
}


/*
 *
 */
void JIscStatementHandle::SetHandleValue( isc_stmt_handle handle )
	{
	sMethodBinding_SetHandle.CallVoid( mJavaEnvironment, mJavaObjectHandle, handle );
	}

/*
 *
 */	
isc_stmt_handle		JIscStatementHandle::GetHandleValue()
	{
	return (isc_db_handle)sMethodBinding_GetHandle.CallInteger( mJavaEnvironment, mJavaObjectHandle );
	}

/*
 *
 */	
void		JIscStatementHandle::AddWarning( jthrowable warning )
	{
	sMethodBinding_AddWarning.CallVoid( mJavaEnvironment, mJavaObjectHandle, warning );
	}
	
	

	
// JIscBlobHandle Class -------------------------------------------------------------------------------------

// Static Members


JClassBinding  JIscBlobHandle::sClassBinding;
	
JMethodBinding JIscBlobHandle::sMethodBinding_GetHandle;
JMethodBinding JIscBlobHandle::sMethodBinding_SetHandle;

JMethodBinding JIscBlobHandle::sMethodBinding_GetId;
JMethodBinding JIscBlobHandle::sMethodBinding_SetId;
JFieldBinding	JIscBlobHandle::sFieldBinding_IsEof;

JMethodBinding JIscBlobHandle::sMethodBinding_AddWarning;

bool	JIscBlobHandle::sIsInitilized = false;

// Static Methods

/*
 *
 */
void		JIscBlobHandle::Initilize( JNIEnv* javaEnvironment )
	{
	if( sIsInitilized )
		throw InternalException("Initilize has been called twice without an unitilize.");

	sClassBinding = JClassBinding( javaEnvironment, "org/firebirdsql/ngds/isc_blob_handle_impl" );

	sMethodBinding_SetHandle = sClassBinding.GetMethodBinding( javaEnvironment, "setRbl_id", "(I)V" );
	sMethodBinding_GetHandle = sClassBinding.GetMethodBinding( javaEnvironment, "getRbl_id", "()I" );

	sMethodBinding_SetId = sClassBinding.GetMethodBinding( javaEnvironment, "setBlob_id", "(J)V" );
	sMethodBinding_GetId = sClassBinding.GetMethodBinding( javaEnvironment, "getBlob_id", "()J" );

	sFieldBinding_IsEof = sClassBinding.GetFieldBinding( javaEnvironment, "isEndOfFile", "Z" );

	sMethodBinding_AddWarning = sClassBinding.GetMethodBinding( javaEnvironment, "addWarning", "(Lorg/firebirdsql/gds/GDSException;)V" );

	sIsInitilized = true;
	}


// Members

/*
 *
 */
JIscBlobHandle::JIscBlobHandle( JNIEnv* javaEnvironment, jobject objectHandlle ) :
	mJavaEnvironment( javaEnvironment )	,
	mJavaObjectHandle( objectHandlle )
	{
	}

/*
 *
 */
JIscBlobHandle::JIscBlobHandle( JNIEnv* javaEnvironment ) :
	mJavaEnvironment(javaEnvironment)	
	{
	mJavaObjectHandle = sClassBinding.CreateNewInstance(javaEnvironment, "()v");
	}

/*
 *
 */
JIscBlobHandle::~JIscBlobHandle()
	{
	}

/*
 *
 */
void JIscBlobHandle::SetHandleValue( isc_blob_handle handle )
	{
	sMethodBinding_SetHandle.CallVoid( mJavaEnvironment, mJavaObjectHandle, handle );
	}

/*
 *
 */	
isc_blob_handle		JIscBlobHandle::GetHandleValue()
	{
	return (isc_blob_handle)sMethodBinding_GetHandle.CallInteger( mJavaEnvironment, mJavaObjectHandle );
	}

/*
 *
 */
void JIscBlobHandle::SetId( ISC_QUAD handle )
	{
	jlong valueToSet = GetJLongFromIscQuad(handle);

	sMethodBinding_SetId.CallVoid( mJavaEnvironment, mJavaObjectHandle, valueToSet );
	}


/*
 *
 */	
ISC_QUAD		JIscBlobHandle::GetId()
	{
	jlong value = sMethodBinding_GetId.CallLong( mJavaEnvironment, mJavaObjectHandle );

	return GetIscQuadFromJavaLong(value);
	}


/* The primary purpose of this method is to ensure that the byte ordering in the jlong
 * is the same across all platforms - the java code may set this into the sqldata field
 * on an XSQLDAVar structure so it must always be LSB first.
 */
jlong  JIscBlobHandle::GetJLongFromIscQuad(ISC_QUAD value)
	{
	jlong returnValue = *((jlong*)&value);

	if( IsLittleEndianByteOrdering() == false )
		{
		char* pointerToReturnValue = (char*)&returnValue;

		std::reverse(pointerToReturnValue, pointerToReturnValue + sizeof(jlong));
		}
	
	return returnValue;
	}

/* The inverse of the above method.
 *
 *
 */
ISC_QUAD JIscBlobHandle::GetIscQuadFromJavaLong(jlong value)
	{
	ISC_QUAD returnValue = *((ISC_QUAD*)&value);
	
	if( IsLittleEndianByteOrdering() == false )
		{
		char* pointerToReturnValue = (char*)&returnValue;

		std::reverse(pointerToReturnValue, pointerToReturnValue + sizeof(ISC_QUAD));
		}
	
	return returnValue;
	}



//ISC_QUAD Java

bool JIscBlobHandle::IsLittleEndianByteOrdering()
	{
	union 
		{
		char c;
		int i;
		} u;

	u.i = 0;
	u.c = 1;

	return (u.i == 1);
	}


/*
 *
 */	
void		JIscBlobHandle::AddWarning( jthrowable warning )
	{
	sMethodBinding_AddWarning.CallVoid( mJavaEnvironment, mJavaObjectHandle, warning );
	}

/*
 *
 */
void JIscBlobHandle::SetIsEndOfFile( bool isEnd)
	{
	sFieldBinding_IsEof.SetBoolean( mJavaEnvironment, mJavaObjectHandle, isEnd );
	}


// JIscServiceHandle Class -------------------------------------------------------------------------------------

// Static Members


JClassBinding  JIscServiceHandle::sClassBinding;
	
JMethodBinding JIscServiceHandle::sMethodBinding_GetHandle;
JMethodBinding JIscServiceHandle::sMethodBinding_SetHandle;

JMethodBinding JIscServiceHandle::sMethodBinding_AddWarning;

bool	JIscServiceHandle::sIsInitilized = false;

// Static Methods

/*
 *
 */
void		JIscServiceHandle::Initilize( JNIEnv* javaEnvironment )
	{
	if( sIsInitilized )
		throw InternalException("Initilize has been called twice without an unitilize.");

	sClassBinding = JClassBinding( javaEnvironment, "org/firebirdsql/ngds/isc_svc_handle_impl" );

	sMethodBinding_SetHandle = sClassBinding.GetMethodBinding( javaEnvironment, "setHandle", "(I)V" );
	sMethodBinding_GetHandle = sClassBinding.GetMethodBinding( javaEnvironment, "getHandle", "()I" );

	sMethodBinding_AddWarning = sClassBinding.GetMethodBinding( javaEnvironment, "addWarning", "(Lorg/firebirdsql/gds/GDSException;)V" );

	sIsInitilized = true;
	}


// Members

/*
 *
 */
JIscServiceHandle::JIscServiceHandle( JNIEnv* javaEnvironment, jobject objectHandlle ) :
	mJavaEnvironment( javaEnvironment )	,
	mJavaObjectHandle( objectHandlle )
	{
	}

/*
 *
 */
JIscServiceHandle::JIscServiceHandle( JNIEnv* javaEnvironment ) :
	mJavaEnvironment(javaEnvironment)	
	{
	mJavaObjectHandle = sClassBinding.CreateNewInstance(javaEnvironment, "()v");
	}

/*
 *
 */
JIscServiceHandle::~JIscServiceHandle()
	{
	}

/*
 *
 */
void JIscServiceHandle::SetHandleValue( isc_svc_handle handle )
	{
	sMethodBinding_SetHandle.CallVoid( mJavaEnvironment, mJavaObjectHandle, handle );
	}

/*
 *
 */	
isc_svc_handle		JIscServiceHandle::GetHandleValue()
	{
	return (isc_blob_handle)sMethodBinding_GetHandle.CallInteger( mJavaEnvironment, mJavaObjectHandle );
	}

/*
 *
 */	
void		JIscServiceHandle::AddWarning( jthrowable warning )
	{
	sMethodBinding_AddWarning.CallVoid( mJavaEnvironment, mJavaObjectHandle, warning );
	}
	

	
	
