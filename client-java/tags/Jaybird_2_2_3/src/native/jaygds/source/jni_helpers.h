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

/*
 * jni_helpers.h
 *
 * Some very basic classes for wrapping parts of the Java Native Interface.
 *
 * These classes are designed to be allocated and passed on the stack or static allocated memory.
 *
 * These classes use InternalException from exceptions.h to report errors.
 */

#ifndef _JNGDS__JniHelpers
#define _JNGDS__JniHelpers

#include "jni.h"

class JMethodBinding;
class JFieldBinding;
class JString;
class JByteArray;
class JObjectArray;

/*
 * Represents a java class. 
 *
 * todo: currently never frees reference to class object.
 */
class JNIEXPORT JClassBinding
    {
    public: 
    
    /**
     *  Constructs and un-initilized instance intended to later be initilized.
     */ 
    JClassBinding();

    /**
     *  Constructs an initilized instance.
     * 
     *  This method stores away the className pointer so this must be staticly
     *  allocated.
     */
    JClassBinding( JNIEnv* javaEnvironment, const char* const className );

    JMethodBinding GetMethodBinding( JNIEnv* javaEnvironment, const char* const methodName, const char* const methodSigniture ) const;

    JFieldBinding GetFieldBinding( JNIEnv* javaEnvironment, const char* const fieldName, const char* const methodSigniture ) const;

    jobject CreateNewInstance(JNIEnv* javaEnvironment, const char* const signiture, ...) const;

    jclass GetHandle() const;

	private:
    
	void checkObjectIsInitilized() const;

	jclass mJavaClassHandle;
    const char*  mJavaClassName;
    };

/*
 *  Represents a method on a java class.
 *
 *  Because JClassBinding object never frees there reference to there class object
 *  and JMethodBinding objects are obtained through JClassBinding object the jmethodID
 *  should remain valid for the remainder of the execution of the program.
 */
class JNIEXPORT JMethodBinding
    {
    public: 

    /**
     *  Constructs and un-initilized instance intended to later be initilized.
     */ 
    JMethodBinding();
	    
    JMethodBinding( const JClassBinding& classBinding, jmethodID methodID );

    jlong CallLong(JNIEnv* JEnv, jobject object, ...) const;
    
    int CallInteger(JNIEnv* JEnv, jobject object, ...) const;

    void CallVoid(JNIEnv* JEnv, jobject object, ...) const;

    jboolean CallBoolean(JNIEnv* JEnv, jobject object, ...) const;

    jmethodID GetMethodId() const;
    
    private:
    JClassBinding mJavaClassBinding;
    jmethodID mMethodID;
    };
    
class JNIEXPORT JFieldBinding
    {
    public: 

    /**
     *  Constructs and un-initilized instance intended to later be initilized.
     */ 
    JFieldBinding();

    JFieldBinding( const JClassBinding& classBinding, jfieldID fieldID ) ;

    jfieldID GetFieldId() const;

    void SetBoolean( JNIEnv* javaEnvironment, jobject object, bool value ) const;

    void SetInt( JNIEnv* javaEnvironment, jobject object, jint value ) const;

    void SetByteArray( JNIEnv* javaEnvironment, jobject object, JByteArray& byteArray ) const;

    void SetByteArrayNull( JNIEnv* javaEnvironment, jobject object ) const;

    void SetObjectArray( JNIEnv* javaEnvironment, jobject object, JObjectArray& byteArray ) const;

    void SetString( JNIEnv* javaEnvironment, jobject object, JString& byteArray ) const;
    
    jint GetInt( JNIEnv* javaEnvironment, jobject object ) const;

    JByteArray GetByteArray(JNIEnv* javaEnvironment, jobject object) const;
        
    JObjectArray GetObjectArray(JNIEnv* javaEnvironment, jobject object) const;

    JString GetString(JNIEnv* javaEnvironment, jobject object) const;
    
    private:
    JClassBinding mJavaClassBinding;
    jfieldID mFieldID;
    };

/*
 *  Class used either to create a java byte array or read data from an existing one.
 */
class JNIEXPORT JByteArray
    {
    public:

    JByteArray();

    /*  
	 *  Creates a copy of 'other'. Only the handle is copied. The buffer pointer is
     *  owned by this object - is created when accessed and released in the destructor.
     */
    JByteArray( const JByteArray& other );

    /*  
	 *  Creates a an object for the suplied jbyteArray.The buffer pointer is
     *  owned by this object - is created when accessed and released in the destructor.
     */
    JByteArray( JNIEnv* javaEnvironment, jbyteArray byteArrayHandle  );

    /*  
	 *  Creates a an object for the suplied data. A java object is created for the
     *  data. The byteArray parameter is only used during the execution of this method.
     *  Thereafter if the data is accesses it hapens in the same way as normal.
     */
    JByteArray( JNIEnv* javaEnvironment, const char* const byteArray, int length );

    /*  
	 * Creates a an object representing an array of the suplied length.
     */
    JByteArray( JNIEnv* javaEnvironment, int length );

    /*
     *  Operates in a similar way to copy constructor.
     */
    JByteArray& operator=(const JByteArray& other);

    /*
     *  Release and commit mBuffer is necesary.
     */
    virtual ~JByteArray();

    jint Size() const;

    /*
     *  Access a buffer for the java array. This buffer is valid as long as this object
     *  remains in valid.
     */
    char* Read();

    jbyteArray GetHandle() const;

    private:
    jbyte* mBuffer;
    jbyteArray mArrayHandle;
    JNIEnv* mJavaEnvironment;
    };

class JNIEXPORT JObjectArray
    {
    public:

    JObjectArray( JNIEnv* javaEnvironment, jclass clazz, int size );

    JObjectArray( JNIEnv* javaEnvironment, jobjectArray handle );

    jint Size() const;

    void Set(JNIEnv* javaEnvironment, int index, jobject value);

    jobject Get(JNIEnv* javaEnvironment, int index ) const;

    jobjectArray GetHandle() const;

    private:
    JNIEnv* mJavaEnvironment;
    jobjectArray mArrayHandle;
    };

class JNIEXPORT JString
    {
	public:

    JString( );

    JString( const JString& other );

    JString( JNIEnv* jEnv, jstring String );

    JString( JNIEnv* jEnv, const char* const String );

    JString( JNIEnv* jEnv, const char* const String, jint Length );

    JString& operator=(const JString& other);
    
    virtual ~JString();

    const char* AsCString();

    jstring AsJString();

    jint GetLength();
    
    bool HasAValue();

    private:
    JNIEnv* mJavaEnvironment;
    const char* mStringBuffer;
    jstring mStringHandle;
    };

#endif

