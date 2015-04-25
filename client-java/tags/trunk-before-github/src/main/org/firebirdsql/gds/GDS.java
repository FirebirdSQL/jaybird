/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/* The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 *
 * Contributors:
 * 
 *   Blas Rodrigues Somoza
 *   David Jencks               d_jencks@users.sourceforge.net
 *   Gabriel Reid
 *   Rick Fincher
 *   Roman Rokytskyy
 *   Ryan Baldwin
 *   Steven Jardine
 */

package org.firebirdsql.gds;

import org.firebirdsql.gds.impl.GDSType;

/**
 * The interface <code>GDS</code> has most of the C client interface functions
 * lightly mapped to java, as well as the constants returned from the server.
 * 
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface GDS {

    // Handle declaration methods

    /**
     * Factory method to create a new {@link IscDbHandle} instance specific to
     * the implementation of this interface.
     * 
     * @return instance of {@link IscDbHandle}
     */
    IscDbHandle createIscDbHandle();

    /**
     * Factory method to create a new {@link IscSvcHandle} instance
     * that is linked to the current <code>GDS</code> implemenation.
     * 
     * @return A new {@link IscSvcHandle} instance
     */
    IscSvcHandle createIscSvcHandle();

    /**
     * Create a new {@link ServiceParameterBuffer} instance for setting
     * service parameters in the current GDS implementation.
     * 
     * @return a new {@link ServiceParameterBuffer} 
     */
    ServiceParameterBuffer createServiceParameterBuffer();

    /**
     * Create a new {@link ServiceRequestBuffer} instance for setting
     * service request parameters in the current GDS implementation. The
     * returned {@link ServiceRequestBuffer} is linked to a specific
     * Services API task, and must be one of the <code>isc_info_svc_*</code>
     * or <code>isc_action_svc</code> constants from {@link ISCConstants}.
     * 
     * @param taskIdentifier
     *            The specific Services API task for which the
     *            {@link ServiceRequestBuffer} is created
     * @return A new {@link ServiceRequestBuffer}
     */
    ServiceRequestBuffer createServiceRequestBuffer(int taskIdentifier);

    /**
     * Create a new {@link DatabaseParameterBuffer} instance for setting
     * database parameters in the current GDS implementation.
     * 
     * @return A new {@link DatabaseParameterBuffer}
     */
    DatabaseParameterBuffer createDatabaseParameterBuffer();

    /**
     * Create new {@link TransactionParameterBuffer} instance for setting
     * transaction parameters in the current GDS implementation.
     * 
     * @return A new {@link TransactionParameterBuffer} .
     */
    TransactionParameterBuffer newTransactionParameterBuffer();

    /**
     * Close this GDS instance.
     */
    void close();

    // -------------------- Database functions -----------------------

    /**
     * Attach to an existing database via a filename.
     * 
     * 
     * @param fileName
     *            The filename for the database, including host and port. The
     *            expected format is
     *            <code>&lt;host name&gt;/&lt;port&gt;:&lt;file path&gt;</code>.
     *            The value for host is localhost if not supplied. The value for
     *            port is 3050 if not supplied.
     * @param dbHandle
     *            The handle to attach to the database
     * @param databaseParameterBuffer
     *            parameters for the database attachment
     * @throws GDSException
     *             if an error occurs while attaching to the database
     */
    void iscAttachDatabase(String fileName, IscDbHandle dbHandle,
            DatabaseParameterBuffer databaseParameterBuffer)
            throws GDSException;

    /**
     * Get information about the database to which {@link IscDbHandle} is
     * attached. The requested parameters are values set in <code>items</code>,
     * and the values in the returned byte-array correspond to the requested
     * parameters in <code>items</code>
     * 
     * @param dbHandle
     *            Handle to the database for which info is to be retrieved
     * @param items
     *            An array of values from the <code>isc_info_*</code> constant
     *            fields from {@link ISCConstants}
     * @param bufferLength
     *            The size of the byte array that is to be returned
     * @return array of bytes whose values correspond the requested parameters
     *         in <code>items</code>
     * @throws GDSException
     *             if an error occurs while retrieving database info
     */
    byte[] iscDatabaseInfo(IscDbHandle dbHandle, byte[] items, int bufferLength)
            throws GDSException;

    /**
     * Detach the given database handle from its database. This effectively
     * closes the connection to the database.
     * 
     * @param dbHandle
     *            The handle to be detached
     * @throws GDSException
     *             if an error occurs while detaching from the database
     */
    void iscDetachDatabase(IscDbHandle dbHandle) throws GDSException;

    /**
     * Retrieve an integer value from a sequence of bytes.
     * <p>
     * Behaviour is undefined for length &gt; 4
     * </p>
     * 
     * @param buffer
     *            The byte array from which the integer is to be retrieved
     * @param pos
     *            The offset starting position from which to start retrieving
     *            byte values
     * @param length
     *            The number of bytes to use in retrieving the integer value.
     * @return The integer value retrieved from the bytes
     */
    int iscVaxInteger(byte[] buffer, int pos, int length);
    
    /**
     * Retrieve a long value from a sequence of bytes.
     * <p>
     * Behaviour is undefined for length &gt; 8
     * </p>
     * 
     * @param buffer
     *            The byte array from which the integer is to be retrieved
     * @param pos
     *            The offset starting position from which to start retrieving
     *            byte values
     * @param length
     *            The number of bytes to use in retrieving the integer value.
     * @return The long value retrieved from the bytes
     */
    long iscVaxLong(byte[] buffer, int pos, int length);

    // -----------------------------------------------
    // Services API methods
    // -----------------------------------------------

    /**
     * Attach to a Service Manager.
     * 
     * @param service
     *            The name/path to the service manager
     * @param serviceHandle
     *            Handle to be linked to the attached service manager
     * @param serviceParameterBuffer
     *            Contains parameters for attaching to the service manager
     * @throws GDSException
     *             if an error occurs while attaching
     */
    void iscServiceAttach(String service, IscSvcHandle serviceHandle,
            ServiceParameterBuffer serviceParameterBuffer) throws GDSException;

    /**
     * Detach from a Service Manager.
     * 
     * @param serviceHandle
     *            Handle to the service manager that is to be detached
     * @throws GDSException
     *             if an error occurs while detaching
     */
    void iscServiceDetach(IscSvcHandle serviceHandle) throws GDSException;

    /**
     * Start a service operation.
     * 
     * @param serviceHandle
     *            Handle to the service manager where the operation is to be
     *            started
     * @param serviceRequestBuffer
     *            parameters about the service to be started
     */
    void iscServiceStart(IscSvcHandle serviceHandle,
            ServiceRequestBuffer serviceRequestBuffer) throws GDSException;

    /**
     * Query a service manager
     * 
     * @param serviceHandle
     *            Handle to the service manager to be queried
     * @param serviceParameterBuffer
     *            parameters about the service
     * @param serviceRequestBuffer
     *            parameters requested in the query
     * @param resultBuffer
     *            buffer to hold the query results
     * @throws GDSException
     *             if an error occurs while querying
     */
    void iscServiceQuery(IscSvcHandle serviceHandle,
            ServiceParameterBuffer serviceParameterBuffer,
            ServiceRequestBuffer serviceRequestBuffer, byte[] resultBuffer)
            throws GDSException;

    /**
     * Queue an EventHandler.
     *
     * @param dbHandle 
     *              Handle to the database where events are to be listened for
     * @param eventHandle 
     *              Handle for event management
     * @param eventHandler 
     *              Callback to be called when an event occurs
     * @throws GDSException
     *              If a database communication error occurs
     */
    int iscQueueEvents(IscDbHandle dbHandle, EventHandle eventHandle,
            EventHandler eventHandler) throws GDSException; 

    /**
     * Initialize the internal databastructures for an 
     * {@link EventHandle}.
     *
     * @param eventHandle 
     *              The event handle to be initialized
     * @throws GDSException
     *              If a database communication error occurs
     */
    void iscEventBlock(EventHandle eventHandle) 
            throws GDSException; 

    /**
     * Retrieve count information after an event has occurred.
     *
     * @param eventHandle 
     *              The handle containing event data
     * @throws GDSException
     *              If a database communication error occurs
     */
    void iscEventCounts(EventHandle eventHandle)
            throws GDSException;

    /**
     * Cancel event listening based on an {@link EventHandle}.
     *
     * @param dbHandle
     *              Handle to the database where events are being 
     *              listened for
     * @param eventHandle
     *              Datastructure for handling event data
     * @throws GDSException
     *              If a database communication error occurs
     */
    void iscCancelEvents(IscDbHandle dbHandle, EventHandle eventHandle)
            throws GDSException;

    /**
     * Create a new {@link EventHandle} specific to a given 
     * <code>GDS</code> implementation.
     *
     * @return The newly created {@link EventHandle}
     */
    EventHandle createEventHandle(String eventName);

    /**
     * Get type of this instance.
     * 
     * @return instance of {@link GDSType}.
     */
    GDSType getType();
}
