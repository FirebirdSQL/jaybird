#include <string.h>
#include <memory.h>
#include "locks.h"

#include "platform.h"

#include "fb_binding.h"

#include "locks.h"

#include "interface_manager.h"

//static	Mutex	lock;	


InterfaceManager::InterfaceManager():lock()
{
	size = 0;
	libs = 0;
	grow();
}

InterfaceManager::~InterfaceManager()
{
	for(size_t i=0; i<size; i++) 
	{
		if (libs[i].intfPtr) 
		{
			releaseInterface(libs[i].intfPtr);
			libs[i].intfPtr = 0;
		}

		if (libs[i].libHandle) 
		{
			unloadLibrary(libs[i].libHandle);
			libs[i].libHandle = 0;
		}

		if (libs[i].libName) 
		{
			delete[] libs[i].libName;
			libs[i].libName = 0;
		}
	}

	delete[] libs;
	libs = 0;
}


long InterfaceManager::LoadInterface(const char *libName)
{
	MutexLockGuard lckGuard(lock);

	const size_t i = findLib(libName);

	if ((i < size) && libs[i].libHandle)
		return (long) i;

	if (i == size) {
		grow();
	}

	libs[i].libHandle = loadLibrary(libName);
	if (libs[i].libHandle)
	{
		const size_t l = strlen(libName) + 1;
		libs[i].libName = new char[l];
		strcpy(libs[i].libName, libName);

		libs[i].intfPtr = getInterface(libs[i].libHandle);
		libs[i].useCount = 1;
		return (long) i;
	}
	else
		return -1; // can't load library
}


long InterfaceManager::ReleaseInterface(const long intfHandle)
{
	MutexLockGuard lckGuard(lock);

	if (intfHandle < 0 || intfHandle >= (long) size)
		return -1;

	const size_t i = (size_t) intfHandle;
	if (!libs[i].useCount)
		return -1;

	if (!--libs[i].useCount)
	{
		releaseInterface(libs[i].intfPtr);
		libs[i].intfPtr = 0;

		unloadLibrary(libs[i].libHandle);
		libs[i].libHandle = 0;

		return 0;
	}

	return (long) i;
}	

FirebirdApiBinding* InterfaceManager::GetInterface(const long intfHandle)
{
	MutexLockGuard lckGuard(lock);

	if (intfHandle < 0 || intfHandle >= (long) size)
		return 0;

	const size_t i = (size_t) intfHandle;
	return (FirebirdApiBinding*)libs[i].intfPtr;
}

SHARED_LIBRARY_HANDLE InterfaceManager::loadLibrary(const char *libName)
{
	return PlatformLoadLibrary(libName);
}


void InterfaceManager::unloadLibrary(SHARED_LIBRARY_HANDLE hLib)
{
	if (hLib && (hLib != (SHARED_LIBRARY_HANDLE)-1))
	PlatformUnLoadLibrary(hLib);
}

FirebirdApiBinding* InterfaceManager::getInterface(SHARED_LIBRARY_HANDLE hLib)
{
	FirebirdApiBinding* fb_binding=new FirebirdApiBinding();
	fb_binding->Load(hLib);
	return fb_binding;
}

void InterfaceManager::releaseInterface(FirebirdApiBinding* intfPtr)
{
	delete intfPtr;
}

size_t InterfaceManager::findLib(const char *libName)
{
	size_t i = 0;
	for(; i<size; i++) {
		if (libs[i].libName)
		{
			if (strcmp(libName, libs[i].libName) == 0) 
			{
				libs[i].useCount++;
				return (long) i;
			}
		}
		else 
			break;
	}

	return i;
}

void InterfaceManager::grow(const size_t inc)
{
	const size_t newSize = size + inc;
	intfLib* newLibs = new intfLib[newSize];

	if (size) {
		memmove(newLibs, libs, sizeof(libs[0]) * size);
		delete[] libs;
	}
	memset(newLibs + size, 0, sizeof(libs[0]) * inc );
	libs = newLibs;
	size = newSize;
}


long InterfaceManager::AddInterface(const char *libName, FirebirdApiBinding* intfPtr)
{
	MutexLockGuard lckGuard(lock);

	const size_t i = findLib(libName);

	if (i < size && libs[i].libHandle) 
	{
		FirebirdApiBinding &a=*intfPtr,&b=*((FirebirdApiBinding*)libs[i].intfPtr);
		if (a==b)
			return (long)i;
		else
			return -1; // same library name but diff interface pointer
	}

	if (i == size) {
		grow();
	}

	libs[i].libHandle = (SHARED_LIBRARY_HANDLE)-1;
	const size_t l = strlen(libName) + 1;
	libs[i].libName = new char[l];
	strcpy(libs[i].libName, libName);
	FirebirdApiBinding* newIntfPtr=new FirebirdApiBinding();
	* newIntfPtr=*intfPtr;
	libs[i].intfPtr = newIntfPtr; // тут нужно скопировать стр-ру
	libs[i].useCount = 1;
	return (long) i;
}
	
