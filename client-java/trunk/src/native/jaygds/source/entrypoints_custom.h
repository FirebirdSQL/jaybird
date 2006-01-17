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
 
#include <jni.h>
#include "exceptions.h"
 
JNIEXPORT void JNICALL Java_org_firebirdsql_gds_impl_jni_JniGDSImpl_VariableInitilize(JNIEnv *javaEnvironment, jobject jThis);
JNIEXPORT void EnsureJavaExceptionIssued(JNIEnv * javaEnvironment, InternalException& exception);
JNIEXPORT void EnsureJavaExceptionIssued(JNIEnv * javaEnvironment);
JNIEXPORT void MaybeIssueOutOfMemory(JNIEnv * javaEnvironment, std::bad_alloc& badAlloc);
