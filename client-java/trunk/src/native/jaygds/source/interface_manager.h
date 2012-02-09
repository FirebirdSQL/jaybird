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

#ifndef _JNGDS__INTERFACEMANAGER
#define _JNGDS__INTERFACEMANAGER

#include "platform.h"

#include "fb_binding.h"

#include "locks.h"

class InterfaceManager
	{
	public:
	
	InterfaceManager();
	
	~InterfaceManager();

	/* 
	 * load library and returns handle to interface 
	 */
	long LoadInterface(const char *);

	/* 
	 * release interest in interface by its handle
	 */
	long ReleaseInterface(const long);

	/*
	 * returns interface pointer by interface handle
	 */
	FirebirdApiBinding* GetInterface(const long);

	/* 
	 * Add interface is specific name and returns handle to interface 
	 */
	long AddInterface(const char *, FirebirdApiBinding*);

	protected:
	
	/*
	 * OS function to load dll\so
	 */
	SHARED_LIBRARY_HANDLE loadLibrary(const char *);

	/* 
	 * OS function to unload dll\so
	 */
	void unloadLibrary(SHARED_LIBRARY_HANDLE);

	/* 
	 * allocate memory for interface and get 
	 * entrypoints from dll\so
	 */
	virtual FirebirdApiBinding* getInterface(SHARED_LIBRARY_HANDLE);

	/* 
	 * release interface memory
	 */
	virtual void releaseInterface(FirebirdApiBinding*);

	private:

	struct intfLib
		{
		char*	libName;
		SHARED_LIBRARY_HANDLE libHandle;
		FirebirdApiBinding*	intfPtr;	// you can use typed pointer here
		long	useCount;
		};

	intfLib* libs; 

	/*
	 * count of allocated libs
	 */
	size_t size;

	Mutex lock;	

	void grow(const size_t = 4);

	size_t findLib(const char *);
	};

#endif
