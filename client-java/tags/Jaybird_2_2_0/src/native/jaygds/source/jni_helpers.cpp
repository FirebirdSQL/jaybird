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
#include "jni_helpers.h"
#include "exceptions.h"
#include "jni.h"

// JClassBinding

JClassBinding::JClassBinding( ) : mJavaClassName(NULL), mJavaClassHandle(NULL)
	{
	}

JClassBinding::JClassBinding( JNIEnv* javaEnvironment, const char* const className ) : mJavaClassName(className)
	{
	mJavaClassHandle = (jclass)javaEnvironment->NewGlobalRef(javaEnvironment->FindClass(className));
	if( javaEnvironment->ExceptionCheck() )
		throw InternalException( "Failed to javaEnvironment->FindClass for %s ", className );
	}

void JClassBinding::checkObjectIsInitilized() const
	{
	if( mJavaClassHandle == NULL || mJavaClassName == NULL )
		throw InternalException("JClassBinding not initilized.");
	}

jclass JClassBinding::GetHandle() const
	{
	checkObjectIsInitilized();

	return mJavaClassHandle;
	}

JMethodBinding JClassBinding::GetMethodBinding( JNIEnv* javaEnvironment, const char* const methodName, const char* const methodSigniture ) const 
	{
	checkObjectIsInitilized();

	jmethodID methodId = javaEnvironment->GetMethodID( mJavaClassHandle, methodName, methodSigniture );
	if( methodId == NULL )
		throw InternalException( "Failed to javaEnvironment->GetMethodID on class '%s' for method name '%s' and signiture '%s'.", mJavaClassName, methodName, methodSigniture );

	return JMethodBinding( *this, methodId );
	}

JFieldBinding JClassBinding::GetFieldBinding( JNIEnv* javaEnvironment, const char* const methodName, const char* const methodSigniture ) const
	{
	checkObjectIsInitilized();

	jfieldID fieldId = javaEnvironment->GetFieldID( mJavaClassHandle, methodName, methodSigniture );
	if( fieldId == NULL )
		throw InternalException( "Failed to javaEnvironment->GetFieldID on class '%s' for field name '%s' and signiture '%s'.", mJavaClassName, methodName, methodSigniture );

	return JFieldBinding( *this, fieldId );
	}

jobject	JClassBinding::CreateNewInstance( JNIEnv* javaEnvironment, const char* const signiture, ... ) const
	{
	checkObjectIsInitilized();

	va_list parameters;
		
	JMethodBinding methodBinding( GetMethodBinding(javaEnvironment, "<init>", signiture) );
	
	va_start(parameters, signiture);
	jobject object = javaEnvironment->NewObjectV( mJavaClassHandle, methodBinding.GetMethodId(), parameters );
	va_end(parameters);

	if( object == NULL )
		throw InternalException( "Failed to javaEnvironment->NewObject on class '%s.", mJavaClassName );

	return object;
	}

// JMethodBinding

JMethodBinding::JMethodBinding() : mJavaClassBinding(), mMethodID(NULL)
	{
	}

JMethodBinding::JMethodBinding( const JClassBinding& classBinding, jmethodID methodID ) : mJavaClassBinding(classBinding), mMethodID(methodID)
	{
	}

int  JMethodBinding::CallInteger(JNIEnv* javaEnvironment, jobject object, ...) const
	{
	va_list parameters;

	va_start(parameters, object);
	jint returnValue = javaEnvironment->CallIntMethodV( object, mMethodID, parameters );
	va_end(parameters);

	if( javaEnvironment->ExceptionCheck() )
		throw InternalException( "Failed to javaEnvironment->CallIntegerMethod" );

	return returnValue;
	}

jlong  JMethodBinding::CallLong(JNIEnv* javaEnvironment, jobject object, ...) const 
	{
	va_list parameters;

	va_start(parameters, object);
	jlong returnValue = javaEnvironment->CallLongMethodV( object, mMethodID, parameters );
	va_end(parameters);

	if( javaEnvironment->ExceptionCheck() )
		throw InternalException( "Failed to javaEnvironment->CallIntegerMethod" );

	return returnValue;
	}

jboolean  JMethodBinding::CallBoolean(JNIEnv* javaEnvironment, jobject object, ...) const
	{
	va_list parameters;

	va_start(parameters, object);
	jboolean returnValue = javaEnvironment->CallBooleanMethodV( object, mMethodID, parameters );
	va_end(parameters);

	if( javaEnvironment->ExceptionCheck() )
		throw InternalException( "Failed to javaEnvironment->CallIntegerMethod" );

	return returnValue;
	}

void JMethodBinding::CallVoid(JNIEnv* javaEnvironment, jobject object, ...) const
	{
	va_list parameters;

	va_start(parameters, object);
	javaEnvironment->CallVoidMethodV( object, mMethodID, parameters );
	va_end(parameters);
	
	if( javaEnvironment->ExceptionCheck() )
		throw InternalException( "Failed to javaEnvironment->CallVoidMethod" );
	}

jmethodID JMethodBinding::GetMethodId() const
	{
	return mMethodID;
	}

// JFieldBinding

JFieldBinding::JFieldBinding( ) : mJavaClassBinding(), mFieldID(NULL)
	{
	}

JFieldBinding::JFieldBinding( const JClassBinding& classBinding, jfieldID fieldID ) : mJavaClassBinding(classBinding), mFieldID(fieldID)
	{
	}

void JFieldBinding::SetBoolean(JNIEnv* javaEnvironment, jobject object, bool value ) const
	{
	javaEnvironment->SetBooleanField( object, mFieldID, value );
	}

void JFieldBinding::SetInt(JNIEnv* javaEnvironment, jobject object, jint value ) const
	{
	javaEnvironment->SetIntField( object, mFieldID, value );
	}

jint JFieldBinding::GetInt(JNIEnv* javaEnvironment, jobject object) const
	{
	return javaEnvironment->GetIntField( object, mFieldID );
	}

void JFieldBinding::SetByteArray( JNIEnv* javaEnvironment, jobject object, JByteArray& byteArray ) const
	{
	javaEnvironment->SetObjectField( object, mFieldID, byteArray.GetHandle() );
	}

void JFieldBinding::SetByteArrayNull( JNIEnv* javaEnvironment, jobject object ) const
	{
	javaEnvironment->SetObjectField( object, mFieldID, 0 );
	}

JByteArray JFieldBinding::GetByteArray(JNIEnv* javaEnvironment, jobject object) const
	{
	return JByteArray( javaEnvironment, (jbyteArray)javaEnvironment->GetObjectField(object, mFieldID) );
	}

void JFieldBinding::SetObjectArray( JNIEnv* javaEnvironment, jobject object, JObjectArray& byteArray ) const
	{
	javaEnvironment->SetObjectField( object, mFieldID, byteArray.GetHandle() );
	}

JObjectArray JFieldBinding::GetObjectArray(JNIEnv* javaEnvironment, jobject object) const
	{
	return JObjectArray( javaEnvironment, (jobjectArray)javaEnvironment->GetObjectField(object, mFieldID) );
	}

void JFieldBinding::SetString( JNIEnv* javaEnvironment, jobject object, JString& byteArray ) const
	{
	javaEnvironment->SetObjectField( object, mFieldID, byteArray.AsJString() );
	}

JString JFieldBinding::GetString(JNIEnv* javaEnvironment, jobject object) const
	{
	return JString( javaEnvironment, (jstring)javaEnvironment->GetObjectField(object, mFieldID) );
	}
 
jfieldID JFieldBinding::GetFieldId() const
	{
	return mFieldID;
	}

// JByteArray

JByteArray::JByteArray() : 
			mJavaEnvironment(NULL), mArrayHandle(NULL), mBuffer(NULL)
	{
	}

/* 
 * Creates a copy of 'other'. Only the handle is copied. The buffer pointer is
 * owned by this object - is created when accessed and released in the destructor.
 */
JByteArray::JByteArray( const JByteArray& other ) : 
			mJavaEnvironment(other.mJavaEnvironment), mArrayHandle(other.mArrayHandle), mBuffer(NULL)
	{
	}

/* 
 * Creates a an object for the suplied jbyteArray.The buffer pointer is
 * owned by this object - is created when accessed and released in the destructor.
 */
JByteArray::JByteArray( JNIEnv* javaEnvironment, jbyteArray byteArrayHandle  ) :
				mJavaEnvironment(javaEnvironment), mArrayHandle(byteArrayHandle), mBuffer(NULL)
	{
	}

/* 
 * Creates a an object for the suplied data. A java object is created for the
 *  data. The byteArray parameter is only used during the execution of this method.
 *  Thereafter if the data is accesses it hapens in the same way as normal.
 */
JByteArray::JByteArray( JNIEnv* javaEnvironment, const char* const byteArray, int length ) :
	mJavaEnvironment(javaEnvironment), mBuffer(NULL)
	{
	mArrayHandle = javaEnvironment->NewByteArray( length );
	if( mArrayHandle == NULL )
		throw InternalException( "Failed to javaEnvironment->NewIntArray" );

	if( byteArray != NULL )
		{
		mBuffer = javaEnvironment->GetByteArrayElements( mArrayHandle, NULL );
		if( mBuffer == NULL )
			throw InternalException( "Failed to javaEnvironment->GetByteArrayElements" );

		memcpy( mBuffer, byteArray, length );

		javaEnvironment->ReleaseByteArrayElements( mArrayHandle, mBuffer, 0 );
		mBuffer = NULL;
		if( javaEnvironment->ExceptionCheck() )
			throw InternalException( "Failed to javaEnvironment->ReleaseByteArrayElements" );
		}
	}

/*	
 * Creates a an object representing an array of the suplied length.
 */
JByteArray::JByteArray( JNIEnv* javaEnvironment, int length ) :
	mJavaEnvironment(javaEnvironment), mBuffer(NULL)
	{
	mArrayHandle = javaEnvironment->NewByteArray( length );
	if( mArrayHandle == NULL )
		throw InternalException( "Failed to javaEnvironment->NewIntArray" );
	}

JByteArray& JByteArray::operator=(const JByteArray& other)
	{
	mJavaEnvironment = other.mJavaEnvironment;
	mArrayHandle = other.mArrayHandle;
	mBuffer = NULL;

	return *this;
	}

JByteArray::~JByteArray()
	{
	if(mBuffer != NULL)
		mJavaEnvironment->ReleaseByteArrayElements(mArrayHandle, mBuffer, 0);
	}

jint JByteArray::Size() const
	{
	if(mArrayHandle == NULL)
		return 0;

	return mJavaEnvironment->GetArrayLength(mArrayHandle);
	}

char* JByteArray::Read()
	{
	if(mBuffer == NULL && mArrayHandle != NULL)
		{
		mBuffer = mJavaEnvironment->GetByteArrayElements( mArrayHandle, NULL );
		if( mJavaEnvironment->ExceptionCheck() )
			throw InternalException( "Failed to javaEnvironment->GetByteArrayElements" );
		}

	return (char*)mBuffer;
	}

jbyteArray JByteArray::GetHandle() const
	{
	return mArrayHandle;
	}

// JObjectArray

JObjectArray::JObjectArray( JNIEnv* javaEnvironment, jobjectArray handle ) : mArrayHandle(handle), mJavaEnvironment(javaEnvironment)
	{
	}

JObjectArray::JObjectArray( JNIEnv* javaEnvironment, jclass claszz, int length ) : mJavaEnvironment(javaEnvironment)
	{
	mArrayHandle = javaEnvironment->NewObjectArray( length, claszz, 0 );
	if( mArrayHandle == NULL )
		throw InternalException( "Failed to javaEnvironment->NewIntArray" );
	}

jint JObjectArray::Size() const
	{
	return mJavaEnvironment->GetArrayLength(mArrayHandle);
	}

jobjectArray JObjectArray::GetHandle() const
	{
	return mArrayHandle;
	}

void JObjectArray::Set(JNIEnv* javaEnvironment, int index, jobject value)
	{
	mJavaEnvironment->SetObjectArrayElement ( mArrayHandle, index, value );
	}

jobject JObjectArray::Get(JNIEnv* javaEnvironment, int index) const
	{
	return mJavaEnvironment->GetObjectArrayElement ( mArrayHandle, index );
	}



// JString

bool JString::HasAValue()
	{
	return mJavaEnvironment != NULL;
	}

JString::JString( ) :
		mJavaEnvironment(NULL), mStringHandle(NULL), mStringBuffer(NULL)
	{

	}

JString::JString( const JString& other ) :
		mJavaEnvironment(other.mJavaEnvironment), mStringHandle(other.mStringHandle), mStringBuffer(NULL)
	{

	}

JString::JString( JNIEnv* javaEnvironment, jstring stringHandle ) :
		mJavaEnvironment(javaEnvironment), mStringHandle(stringHandle), mStringBuffer(NULL)
	{
	}

JString::JString( JNIEnv* javaEnvironment, const char* const string ) :
		mJavaEnvironment(javaEnvironment), mStringBuffer(NULL)
	{
	mStringHandle = javaEnvironment->NewStringUTF(string); 
	if( mStringHandle == NULL )
		throw InternalException( "Failed to javaEnvironment->NewIntArray" );
	}

JString::JString( JNIEnv* javaEnvironment, const char* const string, jint Length ) :
		mJavaEnvironment(javaEnvironment), mStringBuffer(NULL)
	{
	char* buffer = (char*)alloca( Length+1 );
	memset(buffer, 0, Length+1 );
	memcpy( buffer, string, Length ); 
	buffer[Length] = 0;

	mStringHandle = javaEnvironment->NewStringUTF(buffer); 
	if( mStringHandle == NULL )
		throw InternalException( "Failed to javaEnvironment->NewIntArray" );
	}

JString& JString::operator=(const JString& other)
	{
	mJavaEnvironment = other.mJavaEnvironment;
	mStringHandle = other.mStringHandle;
	mStringBuffer = NULL;

	return *this;
	}

jint JString::GetLength()
	{
	return mJavaEnvironment->GetStringUTFLength( mStringHandle );
	}

const char* JString::AsCString()
	{
	if( mStringBuffer == NULL )
		{
		mStringBuffer = mJavaEnvironment->GetStringUTFChars(mStringHandle, NULL);
		if( mStringBuffer == NULL )
			throw InternalException( "Failed to javaEnvironment->NewIntArray" );
		}
	
	return mStringBuffer;
	}

jstring JString::AsJString()
	{
	return mStringHandle;
	}

JString:: ~JString()
	{
	if( mStringBuffer != NULL )
		mJavaEnvironment->ReleaseStringUTFChars(mStringHandle, mStringBuffer);
	}


