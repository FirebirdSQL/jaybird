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
#include "allocator.h"
#include "exceptions.h"

/*
 * Classes used for managing heap allocated memory. These are the only 
 * classes that directly allocate heap memory.
 */

// ScratchPadAllocator Class -------------------------------------------------------------------

ScratchPadAllocator::ScratchPadAllocator() : mMemoryChunkHead(NULL)
	{
	}

ScratchPadAllocator::~ScratchPadAllocator()
	{
	FreeMemory();
	}

char* ScratchPadAllocator::AllocateMemory( int size )
	{
	// Will be set if we dont return in the for loop so we can append a chunk.
	MemoryChunk* lastMemoryChunkInChain = NULL;
	
	// See if any of the blocks in the chain have the memory we need and if so return it.
	for( MemoryChunk* currentMemoryChunk = mMemoryChunkHead; currentMemoryChunk != NULL; currentMemoryChunk = currentMemoryChunk->nextChunk )
		{
		char* possibleReturnValue = currentMemoryChunk->TryToAllocate( size );
		if(possibleReturnValue != NULL)
			return possibleReturnValue;

		lastMemoryChunkInChain = currentMemoryChunk;
		}

	// Get a suitable new chunk ensuring we can get our memory from it.
	MemoryChunk* newMemoryChunk = allocateAndInitializeChunk(size);
	char* returnValue = newMemoryChunk->TryToAllocate( size );
	if(returnValue == NULL)
		throw InternalException("mMemoryChunkHead->TryToAllocate( size ) should always sucseed here.");

	// Place the new meory chunk in the chain.
	if(lastMemoryChunkInChain != NULL)
		lastMemoryChunkInChain->nextChunk = newMemoryChunk;
	else
		mMemoryChunkHead = newMemoryChunk;

	return returnValue;
	}

char* ScratchPadAllocator::MemoryChunk::TryToAllocate( long sizeToAllocate )
	{
	// Ensure that all allocations are on an 8 byte boundary.
	allocOffset += allocOffset % 8;

	if( size - allocOffset >= sizeToAllocate )
		{
		char* returnValue = ((char*)this) + allocOffset;

		allocOffset = allocOffset + sizeToAllocate;
		
		return returnValue;
		}

	return NULL;
	}

ScratchPadAllocator::MemoryChunk* ScratchPadAllocator::allocateAndInitializeChunk(int size)
	{
	size += sizeof(MemoryChunk);
	size += 8;

	int sizeToAllocate = 65536 < size ? size : 65536;

	MemoryChunk* returnValue = (MemoryChunk*)new char[sizeToAllocate];

	returnValue->size = sizeToAllocate;
	returnValue->allocOffset = sizeof(MemoryChunk);
	returnValue->nextChunk = NULL;

	return returnValue;
	}

void ScratchPadAllocator::FreeMemory()
	{
	MemoryChunk* current = mMemoryChunkHead;

	while( current != NULL )
		{
		char* toDelete = (char*)current;
		current = current->nextChunk;
		delete[] toDelete;
		}

	mMemoryChunkHead = NULL;
	}

void ScratchPadAllocator::ClearMemory()
	{
	MemoryChunk* current = mMemoryChunkHead;

	while( current != NULL )
		{
		MemoryChunk*  toDelete = current;
		current = current->nextChunk;
		toDelete->allocOffset = sizeof(MemoryChunk);
		}
	}

// Buffer Class -------------------------------------------------------------------

Buffer::Buffer( int size )
	{
	mBuffer = new char[size];
	}

Buffer::~Buffer()
	{
	delete[] mBuffer;
	}

char* Buffer::access()
	{
	return mBuffer;
	}
