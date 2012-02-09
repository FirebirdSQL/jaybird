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

#include "xsqlda_wrapper.h"

#include "fb_helpers.h"
#include "jni_helpers.h"

#include "ibase.h"
#include "jni.h"

// JXSqlda Class ------------------------------------------------------------------------------

// static members
JClassBinding JXSqlda::sXSQLDAClassBinding;
JClassBinding JXSqlda::sXSQLVARClassBinding;
JFieldBinding JXSqlda::sXSQLDAFieldBinding_sqln;
JFieldBinding JXSqlda::sXSQLDAFieldBinding_sqld;
JFieldBinding JXSqlda::sXSQLDAFieldBinding_sqlvar;
JFieldBinding JXSqlda::sXSQLVARFieldBinding_sqltype;
JFieldBinding JXSqlda::sXSQLVARFieldBinding_sqlscale;
JFieldBinding JXSqlda::sXSQLVARFieldBinding_sqlsubtype;
JFieldBinding JXSqlda::sXSQLVARFieldBinding_sqlen;
JFieldBinding JXSqlda::sXSQLVARFieldBinding_sqldata;
JFieldBinding JXSqlda::sXSQLVARFieldBinding_sqlname;
JFieldBinding JXSqlda::sXSQLVARFieldBinding_relname;
JFieldBinding JXSqlda::sXSQLVARFieldBinding_ownname;
JFieldBinding JXSqlda::sXSQLVARFieldBinding_aliasname;
bool JXSqlda::sIsInitilized= false;

// static methods

void JXSqlda::Initilize( JNIEnv* javaEnvironment )
	{
	if( sIsInitilized )
		throw InternalException("Initilize has been called twice without an unitilize.");

	sXSQLDAClassBinding = JClassBinding( javaEnvironment,  "org/firebirdsql/gds/impl/jni/XSQLDAImpl" );
#ifdef ARCH_IS_BIG_ENDIAN
	sXSQLVARClassBinding = JClassBinding( javaEnvironment, "org/firebirdsql/gds/impl/jni/XSQLVARBigEndianImpl" );
#else
	sXSQLVARClassBinding = JClassBinding( javaEnvironment, "org/firebirdsql/gds/impl/jni/XSQLVARLittleEndianImpl" );
#endif

	sXSQLDAFieldBinding_sqln = sXSQLDAClassBinding.GetFieldBinding( javaEnvironment, "sqln", "I" );
	sXSQLDAFieldBinding_sqld = sXSQLDAClassBinding.GetFieldBinding( javaEnvironment, "sqld", "I" );
	sXSQLDAFieldBinding_sqlvar = sXSQLDAClassBinding.GetFieldBinding( javaEnvironment, "sqlvar", "[Lorg/firebirdsql/gds/XSQLVAR;" );
	sXSQLVARFieldBinding_sqltype = sXSQLVARClassBinding.GetFieldBinding( javaEnvironment, "sqltype", "I" );
	sXSQLVARFieldBinding_sqlscale = sXSQLVARClassBinding.GetFieldBinding( javaEnvironment, "sqlscale", "I" );
	sXSQLVARFieldBinding_sqlsubtype = sXSQLVARClassBinding.GetFieldBinding( javaEnvironment, "sqlsubtype", "I" );
	sXSQLVARFieldBinding_sqlen = sXSQLVARClassBinding.GetFieldBinding( javaEnvironment, "sqllen", "I" );
	sXSQLVARFieldBinding_sqldata = sXSQLVARClassBinding.GetFieldBinding( javaEnvironment, "sqldata", "[B" );
	sXSQLVARFieldBinding_sqlname = sXSQLVARClassBinding.GetFieldBinding( javaEnvironment, "sqlname", "Ljava/lang/String;" );
	sXSQLVARFieldBinding_relname = sXSQLVARClassBinding.GetFieldBinding( javaEnvironment, "relname", "Ljava/lang/String;" );
	sXSQLVARFieldBinding_ownname = sXSQLVARClassBinding.GetFieldBinding( javaEnvironment, "ownname", "Ljava/lang/String;" );
	sXSQLVARFieldBinding_aliasname = sXSQLVARClassBinding.GetFieldBinding( javaEnvironment, "aliasname", "Ljava/lang/String;" );
	sIsInitilized = true;
	}

// methods

/* 
 * Creates a JXSqlda object representing the given java XSQLDA object.
 * The RawAccess method will return a native XSQLDA instance that mirrors
 * the java object. This structure can be modified and the Resync used to
 * write these modifications to the java object.
 *
 * isFetching - A last minute hack to correct some bad behaviour. Affects how the
 * xsqlind field is initilized.
 */
JXSqlda::JXSqlda( JNIEnv* javaEnvironment, jobject handle, bool isFetching ) : mJavaObjectHandle(handle), mXsqlda(NULL)
	{
	if( handle == NULL )
		return; // Initializer list is all we need.

	jint length = sXSQLDAFieldBinding_sqln.GetInt( javaEnvironment, handle );

	Resize((short)length);

	// We assume that when going this way(java to c++) that sqln == sqld. Resize sets these

	// Get hold of sqlvars
	JObjectArray objectArray(sXSQLDAFieldBinding_sqlvar.GetObjectArray(javaEnvironment, handle));

	for( int i = 0; i < mXsqlda->sqln; i++ )
		{
		jobject currentJavaXsqlvar = objectArray.Get(javaEnvironment, i);

		XSQLVAR& currentXsqlvar = mXsqlda->sqlvar[i];

		currentXsqlvar.sqltype    =	(short)sXSQLVARFieldBinding_sqltype.GetInt( javaEnvironment, currentJavaXsqlvar );
		currentXsqlvar.sqlscale   =	(short)sXSQLVARFieldBinding_sqlscale.GetInt( javaEnvironment, currentJavaXsqlvar );
		currentXsqlvar.sqlsubtype = (short)sXSQLVARFieldBinding_sqlsubtype.GetInt( javaEnvironment, currentJavaXsqlvar );
		currentXsqlvar.sqllen     =	(short)sXSQLVARFieldBinding_sqlen.GetInt( javaEnvironment, currentJavaXsqlvar );
		JByteArray byteArray      = sXSQLVARFieldBinding_sqldata.GetByteArray( javaEnvironment, currentJavaXsqlvar);
		currentXsqlvar.sqlind = (short*)mAllocator.AllocateMemory( sizeof(short));
		memset(currentXsqlvar.sqlind, 0, sizeof(short) );

		const bool isVarying = ( (currentXsqlvar.sqltype & ~1) == SQL_VARYING );
		const int dataSizeToAllocate = isVarying ? currentXsqlvar.sqllen + 3 :  currentXsqlvar.sqllen + 1;
		
		currentXsqlvar.sqldata = mAllocator.AllocateMemory(dataSizeToAllocate);
		if( isVarying )
			{
			memset(currentXsqlvar.sqldata, 0, 2);
			memset(currentXsqlvar.sqldata+2, ' ', currentXsqlvar.sqllen);
			currentXsqlvar.sqldata[currentXsqlvar.sqllen+2] = '\0';
			}
		else if( ( (currentXsqlvar.sqltype & ~1) == SQL_TEXT ) )
			{
			memset(currentXsqlvar.sqldata, ' ', currentXsqlvar.sqllen);
			currentXsqlvar.sqldata[currentXsqlvar.sqllen] = '\0';
			}
		else
			{
			memset(currentXsqlvar.sqldata, 0, dataSizeToAllocate);
			}

		if(byteArray.Read() != NULL)
			{
			if( isVarying )
				{
				*((short*)currentXsqlvar.sqldata) = (short)byteArray.Size();
				memcpy( currentXsqlvar.sqldata + 2, byteArray.Read(), byteArray.Size());
				}
			else
				{
				*((short*)currentXsqlvar.sqldata) = (short)byteArray.Size();
				memcpy( currentXsqlvar.sqldata, byteArray.Read(), byteArray.Size());
				}
			}
		else
			{
			if( isFetching == false )
				*((short*)currentXsqlvar.sqlind) = -1;
			}
		 
		JString sqlname = sXSQLVARFieldBinding_sqlname.GetString( javaEnvironment, currentJavaXsqlvar);
		JString relname = sXSQLVARFieldBinding_relname.GetString( javaEnvironment, currentJavaXsqlvar);
		JString ownname = sXSQLVARFieldBinding_ownname.GetString( javaEnvironment, currentJavaXsqlvar);
		JString aliasname = sXSQLVARFieldBinding_aliasname.GetString( javaEnvironment, currentJavaXsqlvar);
		currentXsqlvar.sqlname_length = (short)sqlname.GetLength();
		memcpy( currentXsqlvar.sqlname,  sqlname.AsCString(), currentXsqlvar.sqlname_length );
		currentXsqlvar.relname_length = (short)relname.GetLength();
		memcpy( currentXsqlvar.relname,   relname.AsCString(), currentXsqlvar.relname_length );
		currentXsqlvar.ownname_length = (short)ownname.GetLength();
		memcpy( currentXsqlvar.ownname,  ownname.AsCString(), currentXsqlvar.ownname_length );
		currentXsqlvar.aliasname_length = (short)aliasname.GetLength();
		memcpy( currentXsqlvar.aliasname,  aliasname.AsCString(), currentXsqlvar.aliasname_length );
		}
	}

/* 
 * Creates a JXSqlda object that does not represent an existing java XSQLDA
 * object. The RawAccess method will return a native XSQLDA instance.
 * Once data is writen to this instance a java object can be created 
 * using the AllocateJavaXSqlda 
 *
 * isFetching - A last minute hack to correct some bad behaviour. Affects how the
 * xsqlind field is initilized.
 */
JXSqlda::JXSqlda( JNIEnv* jEnv, bool isFetching ) : mXsqlda(NULL), mJavaEnvironment(jEnv), mJavaObjectHandle(NULL)
	{
	Resize(1);
	}

JXSqlda::~JXSqlda()
	{
	}

XSQLDA* JXSqlda::RawAccess() 
	{
	return mXsqlda;
	}

void JXSqlda::Resize(short n)
	{
	mAllocator.ClearMemory();

	const size_t requiredSize = XSQLDA_LENGTH( n );

	mXsqlda = (XSQLDA*)mAllocator.AllocateMemory( requiredSize );

	memset(mXsqlda, 0, requiredSize);
	mXsqlda->version = SQLDA_VERSION1;
	mXsqlda->sqln = n;
	mXsqlda->sqld = n;
	}

void JXSqlda::Resync(JNIEnv* javaEnvironment)
	{
	if( mXsqlda == NULL )
		return;

	JObjectArray objectArray(sXSQLDAFieldBinding_sqlvar.GetObjectArray(javaEnvironment, mJavaObjectHandle));

	for( int i = 0; i < mXsqlda->sqln; i++ )
		{
		jobject currentJavaXsqlvar = objectArray.Get(javaEnvironment, i);
		XSQLVAR& currentXsqlvar = mXsqlda->sqlvar[i];

		sXSQLVARFieldBinding_sqltype.SetInt( javaEnvironment, currentJavaXsqlvar, currentXsqlvar.sqltype );
		sXSQLVARFieldBinding_sqlscale.SetInt( javaEnvironment, currentJavaXsqlvar, currentXsqlvar.sqlscale );
		sXSQLVARFieldBinding_sqlsubtype.SetInt( javaEnvironment, currentJavaXsqlvar, currentXsqlvar.sqlsubtype );
		sXSQLVARFieldBinding_sqlen.SetInt( javaEnvironment, currentJavaXsqlvar, currentXsqlvar.sqllen );
		
		if(*(currentXsqlvar.sqlind) == 0) // If field is not null
			{
			short dataLength;
			char* dataPtr;

			if( (currentXsqlvar.sqltype & ~1) == SQL_VARYING )
				{
				dataLength = *((short*)currentXsqlvar.sqldata);
				dataPtr = currentXsqlvar.sqldata + 2;
				}
			else
				{
				dataLength = currentXsqlvar.sqllen;
				dataPtr = currentXsqlvar.sqldata;
				}
		
			if(dataPtr == NULL)
				{
				dataPtr = mAllocator.AllocateMemory(1);
				dataPtr[0] = 0;
				}

			JByteArray sqldata(javaEnvironment, dataPtr, dataLength );
				
			sXSQLVARFieldBinding_sqldata.SetByteArray( javaEnvironment, currentJavaXsqlvar, sqldata );
			}
		else
			sXSQLVARFieldBinding_sqldata.SetByteArrayNull( javaEnvironment, currentJavaXsqlvar );

		JString jsqlName(javaEnvironment, currentXsqlvar.sqlname, currentXsqlvar.sqlname_length);
		JString jrelName(javaEnvironment, currentXsqlvar.relname, currentXsqlvar.relname_length);
		JString jownName(javaEnvironment, currentXsqlvar.ownname, currentXsqlvar.ownname_length);
		JString jaliasName(javaEnvironment, currentXsqlvar.aliasname, currentXsqlvar.aliasname_length);

		sXSQLVARFieldBinding_sqlname.SetString( javaEnvironment, currentJavaXsqlvar, jsqlName );
		sXSQLVARFieldBinding_relname.SetString( javaEnvironment, currentJavaXsqlvar, jrelName );
		sXSQLVARFieldBinding_ownname.SetString( javaEnvironment, currentJavaXsqlvar, jownName );
		sXSQLVARFieldBinding_aliasname.SetString( javaEnvironment, currentJavaXsqlvar, jaliasName );
		}
	}

jobject JXSqlda::AllocateJavaXSqlda( JNIEnv* javaEnvironment )
	{
	if(mXsqlda == NULL)
		return false;

	return AllocateJavaXSqlda(javaEnvironment, mXsqlda);
	}

jobject JXSqlda::AllocateJavaXSqlda( JNIEnv* javaEnvironment, XSQLDA* xsqlda )
	{
	JObjectArray xsqlvars( javaEnvironment, sXSQLVARClassBinding.GetHandle(), xsqlda->sqln );
	for( int i = 0; i < xsqlda->sqln; i++ )
		{
		xsqlvars.Set( javaEnvironment, i, AllocateJavaXsqlvar( javaEnvironment, xsqlda->sqlvar[i] ) );
		}

	return sXSQLDAClassBinding.CreateNewInstance(javaEnvironment, "(II[Lorg/firebirdsql/gds/XSQLVAR;)V",
			xsqlda->sqln,
			xsqlda->sqld,
			xsqlvars.GetHandle() );
	}

jobject JXSqlda::AllocateJavaXsqlvar( JNIEnv* javaEnvironment, XSQLVAR& xsqlvar )
	{
	JByteArray sqlData;

	if(xsqlvar.sqlind != NULL && *(xsqlvar.sqlind) == 0) // If field is not null
		{
		short dataLength;
		char* dataPtr;

		if( (xsqlvar.sqltype & ~1) == SQL_VARYING )
			{
			dataLength = *((short*)xsqlvar.sqldata);
			dataPtr = xsqlvar.sqldata + 2;
			}
		else
			{
			dataLength = xsqlvar.sqllen;
			dataPtr = xsqlvar.sqldata;
			}

		if(dataPtr == NULL)
			dataPtr = mAllocator.AllocateMemory(1);

		dataPtr[0] = 0;

		sqlData = JByteArray(javaEnvironment, dataPtr, dataLength );
		}

	JString sqlname( javaEnvironment, xsqlvar.sqlname, xsqlvar.sqlname_length );
	JString relname( javaEnvironment, xsqlvar.relname, xsqlvar.relname_length );
	JString ownname( javaEnvironment, xsqlvar.ownname, xsqlvar.ownname_length );
	JString aliasname( javaEnvironment, xsqlvar.aliasname, xsqlvar.aliasname_length );

	return sXSQLVARClassBinding.CreateNewInstance(javaEnvironment, "(IIII[BLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
		xsqlvar.sqltype,
		xsqlvar.sqlscale,
		xsqlvar.sqlsubtype,
		xsqlvar.sqllen,
		sqlData.GetHandle(),
		sqlname.AsJString(),
		relname.AsJString(),
		ownname.AsJString(),
		aliasname.AsJString()
		);
	}
