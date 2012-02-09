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

#ifndef _JNGDS__XsqldaWrapper
#define _JNGDS__XsqldaWrapper

#include "jni_helpers.h"

#include "allocator.h"

#include "ibase.h"
#include "jni.h"

/* 
 * Class used to map between the org.firbirdsql.gds.XSQLDA class and 
 * firebirds native XSQLDA structure.
 *
 * Static Initilize must be called before class is used.
 */
class JXSqlda
	{
	public:

	/* 
	 * Creates a JXSqlda object representing the given java XSQLDA object.
	 * The RawAccess method will return a native XSQLDA instance that mirrors
	 * the java object. This structure can be modified and the Resync used to
	 * write these modifications to the java object.
	 *
	 * isFetching - A last minute hack to correct some bad behaviour. Affects how the
	 * xsqlind field is initilized.
	 */
	JXSqlda( JNIEnv* javaEnvironment, jobject handle, bool isFetching = false );

	/* 
	 * Creates a JXSqlda object that does not represent an existing java XSQLDA
	 * object. The RawAccess method will return a native XSQLDA instance.
	 * Once data is writen to this instance a java object can be created 
	 * using the AllocateJavaXSqlda 
	 *
	 * isFetching - A last minute hack to correct some bad behaviour. Affects how the
	 * xsqlind field is initilized.
	 */
	JXSqlda( JNIEnv* jEnv, bool isFetching = false );
	
	virtual ~JXSqlda();

	XSQLDA* RawAccess();
	
	void Resize(short n);

	void Resync(JNIEnv* javaEnvironment);

	jobject AllocateJavaXSqlda( JNIEnv* javaEnvironment );

	static void Initilize( JNIEnv* jEnv );

	private:

	JXSqlda& operator=( const JXSqlda& other );

	JXSqlda( const JXSqlda& other );

	jobject AllocateJavaXSqlda( JNIEnv* javaEnvironment, XSQLDA* xsqlda );

	jobject AllocateJavaXsqlvar( JNIEnv* javaEnvironment, XSQLVAR& xsqlvar );
	
	XSQLDA*				mXsqlda;	
	
	JNIEnv* mJavaEnvironment;
	
	jobject mJavaObjectHandle;

	ScratchPadAllocator mAllocator;

	// static
	static JClassBinding sXSQLDAClassBinding;
	static JClassBinding sXSQLVARClassBinding;
	static JFieldBinding sXSQLDAFieldBinding_sqln;
	static JFieldBinding sXSQLDAFieldBinding_sqld;
	static JFieldBinding sXSQLDAFieldBinding_sqlvar;
	static JFieldBinding sXSQLVARFieldBinding_sqltype;
	static JFieldBinding sXSQLVARFieldBinding_sqlscale;
	static JFieldBinding sXSQLVARFieldBinding_sqlsubtype;
	static JFieldBinding sXSQLVARFieldBinding_sqlen;
	static JFieldBinding sXSQLVARFieldBinding_sqldata;
	static JFieldBinding sXSQLVARFieldBinding_sqlname;
	static JFieldBinding sXSQLVARFieldBinding_relname;
	static JFieldBinding sXSQLVARFieldBinding_ownname;
	static JFieldBinding sXSQLVARFieldBinding_aliasname;
	static bool sIsInitilized;
	};

#endif
