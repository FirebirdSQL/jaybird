/*
 * $Id$
 *
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jna.fbclient;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

/**
 * JNA library interface specifically for the Windows platform.
 * <p>
 * This interface instructs JNA to apply {@code __stdcall} conventions.
 * </p>
 * <p>
 * Warning: Some methods defined in {@code ibase.h} (those marked with {@code ISC_EXPORT_VARARG}) apply {@code __cdecl},
 * so those methods might break.
 * </p>
 *
 * @since 3.0
 */
public interface WinFbClientLibrary extends FbClientLibrary, StdCallLibrary {
    /**
     * FbShutdown Callback following the StdCall conventions
     * <p>
     * <i>native declaration : C:\Program Files\Firebird\Firebird_2_5\include\ibase.h</i>
     * </p>
     */
    interface FbShutdownStdCallback extends FbShutdownCallback, StdCallLibrary.StdCallCallback {
    }

    /**
     * IscEvent Callback following the StdCall conventions
     * <p>
     * <i>native declaration : C:\Program Files\Firebird\Firebird_2_5\include\ibase.h</i>
     * </p>
     */
    interface IscEventStdCallback extends IscEventCallback, StdCallLibrary.StdCallCallback {
    }

    ISC_STATUS isc_que_events(ISC_STATUS[] statusVector, IntByReference dbHandle, IntByReference eventId, short length,
            Pointer eventBuffer, IscEventStdCallback eventFunction, Pointer eventFunctionArg);
}
