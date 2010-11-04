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

/* solaris/platform.h
 * 
 * Platform specific includes, code and defines for win32
 */

#ifndef _JNGDS__Platform
#define _JNGDS__Platform

#include <dlfcn.h>

#include <malloc.h>
#include <alloca.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

// Defines for fb_binding.h and fb_binding.cpp
typedef void* SHARED_LIBRARY_HANDLE;


#define FB_ENTRYPOINT(X) \
			if ((##X = (prototype_##X*)dlsym(sHandle, #X)) == NULL) \
				throw InternalException("FirebirdApiBinding:Initialize() - Entry-point "#X" not found")

#define FB_ENTRYPOINT_OPTIONAL(X) \
			##X = (prototype_##X*)dlsym(sHandle, #X)

SHARED_LIBRARY_HANDLE PlatformLoadLibrary(const char* const name);

void PlatformUnLoadLibrary(SHARED_LIBRARY_HANDLE);

#define OFFSETA(struct, fld)     ((size_t) ((struct) NULL)->fld)

#define DEF_CALL_API(X) \
    jint pointer_##X=isc_api_handle.GetInt(javaEnvironment,jThis);\
    prototype_##X *X=interfaceManager.GetInterface(pointer_##X)->X;\ 
	if (X == NULL) \
		processFailedEntryPoint("FirebirdApiBinding:Initialize() - Entry-point "#X" not found");

#define CALL_API(X) DEF_CALL_API(X)\
    X

#endif // ifndef(_JNGDS__Platform)
