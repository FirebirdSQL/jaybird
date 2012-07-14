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

/* win32/platform.h
 * 
 * Platform specific includes, code and defines for win32
 */

#ifndef _JNGDS__Platform
#define _JNGDS__Platform

// Windows includes and stuff 

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#define WIN32_LEAN_AND_MEAN 

#include <windows.h>

#include <malloc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

// Dont know if this is a good thing too do 
#define vsnprintf _vsnprintf
#define alloca _alloca

// Defines for fb_binding.h and fb_binding.cpp
typedef HMODULE SHARED_LIBRARY_HANDLE;

void processFailedEntryPoint(const char* const message);

#define FB_ENTRYPOINT(X) \
            if ((FirebirdApiBinding::X = (prototype_##X*)GetProcAddress(sHandle, #X)) == NULL) \
                processFailedEntryPoint("FirebirdApiBinding:Initialize() - Entry-point "#X" not found")

#define FB_ENTRYPOINT_OPTIONAL(X) \
			FirebirdApiBinding::X = (prototype_##X*)GetProcAddress(sHandle, #X)

SHARED_LIBRARY_HANDLE PlatformLoadLibrary(const char* const name);

void PlatformUnLoadLibrary(SHARED_LIBRARY_HANDLE);

template <typename T> T PlatformFindSymbol(SHARED_LIBRARY_HANDLE library,
	const char* symbolName, T& pointer)
	{
	pointer = reinterpret_cast<T>(GetProcAddress(library, symbolName));
	return pointer;
	}

#define OFFSETA(struct, fld)     ((size_t) ((struct) NULL)->fld)

#define DEF_CALL_API(X) \
    jint pointer_##X=isc_api_handle.GetInt(javaEnvironment,jThis);\
    prototype_##X *X=interfaceManager.GetInterface(pointer_##X)->X;\
	if (X == NULL) \
		processFailedEntryPoint("FirebirdApiBinding:Initialize() - Entry-point "#X" not found");

#define CALL_API(X) DEF_CALL_API(X)\
    X

#endif // ifndef(_JNGDS__Platform)
