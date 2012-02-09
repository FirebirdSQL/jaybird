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

#include "fb_helpers.h"

#include "jni_helpers.h"

#include "ibase.h"


// FirebirdStatusVector Class ---------------------------------------------------------------------

FirebirdStatusVector::FirebirdStatusVector()
	{
	memset(mVector, 0, sizeof(mVector));
	}

// Static Member 

JClassBinding FirebirdStatusVector::sClassBinding;
JMethodBinding FirebirdStatusVector::sSetNextMethod;
JMethodBinding FirebirdStatusVector::sGetIsWarningMethod;

// Static Methods

void FirebirdStatusVector::Initilize(JNIEnv* javaEnvironment)
	{
	sClassBinding =	JClassBinding( javaEnvironment, "org/firebirdsql/gds/GDSException" );
	sSetNextMethod = sClassBinding.GetMethodBinding( javaEnvironment, "setNext", "(Lorg/firebirdsql/gds/GDSException;)V" );
	sGetIsWarningMethod = sClassBinding.GetMethodBinding( javaEnvironment, "isWarning", "()Z" );
	}

// Methods

ISC_STATUS* FirebirdStatusVector::RawAccess() 
	{ 
	return mVector; 
	}

void FirebirdStatusVector::IssueExceptionsAndOrAddWarnings(JNIEnv* javaEnvironment, JIscDatabaseHandle& databaseHandle)
	{
	jthrowable warning = IssueExceptionsAndOrAddWarnings(javaEnvironment);
	if( warning != NULL )
		databaseHandle.AddWarning(warning);
	}

void FirebirdStatusVector::IssueExceptionsAndOrAddWarnings(JNIEnv* javaEnvironment, JIscStatementHandle& databaseHandle)
	{
	jthrowable warning = IssueExceptionsAndOrAddWarnings(javaEnvironment);
	if( warning != NULL )
		databaseHandle.AddWarning(warning);
	}

void FirebirdStatusVector::IssueExceptionsAndOrAddWarnings(JNIEnv* javaEnvironment, JIscTransactionHandle& databaseHandle)
	{
	jthrowable warning = IssueExceptionsAndOrAddWarnings(javaEnvironment);
	if( warning != NULL )
		databaseHandle.AddWarning(warning);
	}

void FirebirdStatusVector::IssueExceptionsAndOrAddWarnings(JNIEnv* javaEnvironment, JIscBlobHandle& databaseHandle)
	{
	jthrowable warning = IssueExceptionsAndOrAddWarnings(javaEnvironment);
		if( warning != NULL )
			databaseHandle.AddWarning(warning);
	}

void FirebirdStatusVector::IssueExceptionsAndOrAddWarnings(JNIEnv* javaEnvironment, JIscServiceHandle& databaseHandle)
	{
	jthrowable warning = IssueExceptionsAndOrAddWarnings(javaEnvironment);
	if( warning != NULL )
		databaseHandle.AddWarning(warning);
	}

jthrowable FirebirdStatusVector::IssueExceptionsAndOrAddWarnings(JNIEnv* javaEnvironment)
	{
	// We need to build a chain of exception objects from the status vector.
	ISC_STATUS* status_vector = mVector; 
	jthrowable exceptionObjectHead = NULL;
	jthrowable exceptionObjectCurrent = NULL;

	int  index = 0;
	while (index<20)
		{
		jint type;

		jint lValue = 0;
		JString sValue = JString();
			
		type = status_vector[index++];
			
		switch (type)
			{
			case 0:	// End
				{
				if(exceptionObjectHead != NULL)
					{
					if( sGetIsWarningMethod.CallBoolean( javaEnvironment, exceptionObjectHead ) )
						{
						return exceptionObjectHead;
						}
					else
						{
						javaEnvironment->Throw( exceptionObjectHead );
						return NULL;
						}
					}
					
				return NULL;
				}
			break;

			case 1:	// Interbase Error
			case 4: // Generic long
				{
				lValue = status_vector[index++];	
				}
			break;
				
			case 2:	// cstring
			case 5:	// cstring
				{
				sValue = JString( javaEnvironment, (char*)(status_vector[index++]) );
				}
			break;

			case 3: // vstring
				{
				long stringLength = status_vector[index++];
				char* stringPtr = (char*)status_vector[index++];

				sValue = JString( javaEnvironment, stringPtr, stringLength );
				}
			break;
				
			default:
				{
				lValue = status_vector[index++];
				}
			break;
			}
			
		
			if(lValue != 0 || sValue.HasAValue() )
				{
				jthrowable newException;
				if( sValue.HasAValue() == false )
					newException = (jthrowable)sClassBinding.CreateNewInstance(javaEnvironment, "(II)V", type, lValue);
				else
					newException = (jthrowable)sClassBinding.CreateNewInstance(javaEnvironment, "(ILjava/lang/String;)V", type, sValue.AsJString());

				if( exceptionObjectHead == NULL )
					{
					exceptionObjectHead = newException;
					exceptionObjectCurrent = newException;
					}
				else
					{
					sSetNextMethod.CallVoid( javaEnvironment, exceptionObjectCurrent, newException );
					exceptionObjectCurrent = newException;
					}
				}
				
		}

	throw InternalException("Error parsing staus vector. Should never get here !");
	}
