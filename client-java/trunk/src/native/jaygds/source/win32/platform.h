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

#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

// Defines for fb_binding.h and fb_binding.cpp
typedef HMODULE SHARED_LIBRARY_HANDLE;


#define FB_ENTRYPOINT(X) \
			if ((##X = (prototype_##X*)GetProcAddress(sHandle, #X)) == NULL) \
				throw InternalException("FirebirdApiBinding:Initialize() - Entry-point "#X" not found")

SHARED_LIBRARY_HANDLE PlatformLoadLibrary(const char* const name);


#endif // ifndef(_JNGDS__Platform)
