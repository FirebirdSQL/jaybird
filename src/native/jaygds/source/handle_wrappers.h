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

#ifndef _JNGDS__HandleWrappers
#define _JNGDS__HandleWrappers

#include "jni_helpers.h"

#include "ibase.h"
#include "jni.h"

/*
 *	
 */
class JIscDatabaseHandle
	{
	public:
	

	/*
	 *	
	 */
	JIscDatabaseHandle( JNIEnv* jEnv, jobject handle );


	/*
	 *	
	 */
	JIscDatabaseHandle( JNIEnv* jEnv );
	

	/*
	 *	
	 */
	virtual ~JIscDatabaseHandle();


	/*
	 *	
	 */
	void SetHandleValue( isc_db_handle handle );
	

	/*
	 *	
	 */
	isc_db_handle		GetHandleValue();


	/*
	 *	
	 */
	void AddWarning( jthrowable warning );

	

	/*
	 *	
	 */
	static void		Initilize( JNIEnv* jEnv );
		

	private:
	
	JNIEnv* mJavaEnvironment;
	jobject mJavaObjectHandle;


	static JClassBinding  sClassBinding;
	
	static JMethodBinding sMethodBinding_GetHandle;
	static JMethodBinding sMethodBinding_SetHandle;

	static JMethodBinding sMethodBinding_AddWarning;

	// static
	static bool		sIsInitilized;
	
	};


/*
 *	
 */
class JIscTransactionHandle
	{
	public:

	/*
	 *	
	 */
	JIscTransactionHandle( JNIEnv* jEnv, jobject handle );

	/*
	 *	
	 */
	JIscTransactionHandle( JNIEnv* jEnv );
	
	/*
	 *	
	 */
	virtual ~JIscTransactionHandle();

	/*
	 *	
	 */
	void SetHandleValue( isc_tr_handle handle );
	
	/*
	 *	
	 */
	isc_tr_handle		GetHandleValue();

	/*
	 *	
	 */
	void AddWarning( jthrowable warning );
	
	/*
	 *	
	 */
	static void		Initilize( JNIEnv* jEnv );
		

	
	private:
	
	JNIEnv* mJavaEnvironment;
	jobject mJavaObjectHandle;


	static JClassBinding  sClassBinding;
	
	static JMethodBinding sMethodBinding_GetHandle;
	static JMethodBinding sMethodBinding_SetHandle;

	static JMethodBinding sMethodBinding_AddWarning;

	// static
	static bool		sIsInitilized;
	
	};


/*
 *	
 */
class JIscStatementHandle
	{
	public:
	
	/*
	 *	
	 */
	JIscStatementHandle( JNIEnv* jEnv, jobject handle );

	/*
	 *	
	 */
	JIscStatementHandle( JNIEnv* jEnv );
	
	/*
	 *	
	 */
	virtual ~JIscStatementHandle();

	/*
	 *	
	 */
	void SetHandleValue( isc_stmt_handle handle );
	
	/*
	 *	
	 */
	isc_stmt_handle		GetHandleValue();

	
	/*
	 *	
	 */
	static void		Initilize( JNIEnv* jEnv );

	/*
	 *	
	 */
	void AddWarning( jthrowable warning );
		

	
	private:
	
	JNIEnv* mJavaEnvironment;
	jobject mJavaObjectHandle;


	static JClassBinding  sClassBinding;
	
	static JMethodBinding sMethodBinding_GetHandle;
	static JMethodBinding sMethodBinding_SetHandle;

	static JMethodBinding sMethodBinding_AddWarning;

	// static
	static bool		sIsInitilized;
	
	};

/*
 *	
 */
class JIscBlobHandle
	{
	public:
	
	/*
	 *	
	 */
	JIscBlobHandle( JNIEnv* jEnv, jobject handle );

	/*
	 *	
	 */
	JIscBlobHandle( JNIEnv* jEnv );
	
	/*
	 *	
	 */
	virtual ~JIscBlobHandle();

	/*
	 *	
	 */
	void SetHandleValue( isc_blob_handle handle );
	
	/*
	 *	
	 */
	isc_blob_handle		GetHandleValue();

	/*
	 *	
	 */
	void SetId( ISC_QUAD handle );
	
	/*
	 *	
	 */
	ISC_QUAD		GetId();

	/*
	 *	
	 */
	void SetIsEndOfFile( bool isEnd);

	/*
	 *	
	 */
	void AddWarning( jthrowable warning );

	
	/*
	 *	
	 */
	static void		Initilize( JNIEnv* jEnv );
		

	
	private:
	
	JNIEnv* mJavaEnvironment;
	jobject mJavaObjectHandle;


	static JClassBinding  sClassBinding;
	
	static JMethodBinding sMethodBinding_GetHandle;
	static JMethodBinding sMethodBinding_SetHandle;

	static JMethodBinding sMethodBinding_GetId;
	static JMethodBinding sMethodBinding_SetId;

	static JFieldBinding sFieldBinding_IsEof;

	static JMethodBinding sMethodBinding_AddWarning;

	// static
	static bool		sIsInitilized;
	
	};

#endif
