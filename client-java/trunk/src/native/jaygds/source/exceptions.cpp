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

#include "exceptions.h"

#include <stdio.h>


// InternalException -------------------------------------------------------------------

/*
 *
 *
 */
InternalException::InternalException(const char* const message, ... )
	{
	va_list args;

	va_start(args, message);
	vsnprintf( mBuffer, sizeof(mBuffer), message, args );
	va_end(args);
	}

/*
 *
 *
 */
const char* InternalException::getMessage()
	{
	return mBuffer;
	}



