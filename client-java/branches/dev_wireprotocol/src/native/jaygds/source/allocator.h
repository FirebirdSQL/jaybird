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
 * Classes used for managing heap allocated memory. These are the only 
 * classes that directly allocate heap memory.
 */

#ifndef _JNGDS__Allocator
#define _JNGDS__Allocator


/*	ScratchPadAllocator
 *	 
 *  Class to be used to make many small allocations that will be cleared all 
 *  at once. It allocated chunks of memory for itself using new and then allocates
 *  off these by handing out blocks of memory and incrimenting an alloaction offset.
 *  When the memory is to be cleared the allocation offset for each chunk is reset.
 *
 *	This class frees all memory it has allocated when it goes out of scope.
 *
 */
class ScratchPadAllocator
	{
	public:

	ScratchPadAllocator();

	virtual ~ScratchPadAllocator();

	char* AllocateMemory( int size );
	
	void FreeMemory();

	void ClearMemory();

	private:

	ScratchPadAllocator& operator=( const ScratchPadAllocator& other );

	ScratchPadAllocator( const ScratchPadAllocator& other );

	struct MemoryChunk
		{
		int	size;
		int	allocOffset;
		MemoryChunk* nextChunk;

		char* TryToAllocate( long size );
		};

	MemoryChunk* allocateAndInitializeChunk(int size);

	MemoryChunk* mMemoryChunkHead;
	};


/*
 * Buffer
 * 
 * Simply allocates a buffer and provides access to it.
 *
 * This buffer is freed when the object goes out of scope.
 */
class Buffer
	{
	public:

	Buffer( int size );

	~Buffer();

	char* access();

	private:
	
	Buffer& operator=( const Buffer& other );

	Buffer( const Buffer& other );

	char* mBuffer;
	};

#endif // _JNGDS__Allocator

