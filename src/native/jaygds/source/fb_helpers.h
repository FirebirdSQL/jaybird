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

#ifndef _JNGDS__FirebirdHelpers
#define _JNGDS__FirebirdHelpers

#include "exceptions.h"


#include "jni_helpers.h"
#include "handle_wrappers.h"

#include "ibase.h"
#include "jni.h"

class FirebirdStatusVector
	{
	public:
	FirebirdStatusVector(){ }
	
	ISC_STATUS*		RawAccess();
			
	void		IssueExceptionsAndOrAddWarnings(JNIEnv* javaEnvironment, JIscDatabaseHandle& databaseHandle);
	void		IssueExceptionsAndOrAddWarnings(JNIEnv* javaEnvironment, JIscStatementHandle& databaseHandle);
	void		IssueExceptionsAndOrAddWarnings(JNIEnv* javaEnvironment, JIscTransactionHandle& databaseHandle);
	void		IssueExceptionsAndOrAddWarnings(JNIEnv* javaEnvironment, JIscBlobHandle& databaseHandle);

	static void		Initilize( JNIEnv* jEnv );
			
	private:
	FirebirdStatusVector( FirebirdStatusVector& sv );					
	const FirebirdStatusVector& operator=( FirebirdStatusVector& sv ) ;


	jthrowable		IssueExceptionsAndOrAddWarnings(JNIEnv* javaEnvironment);

	static JClassBinding	sClassBinding;
	static JMethodBinding	sSetNextMethod;

	static JMethodBinding	sGetIsWarningMethod;


	ISC_STATUS mVector[20];
	};


#endif

