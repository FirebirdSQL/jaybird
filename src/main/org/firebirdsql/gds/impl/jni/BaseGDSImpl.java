/*
 * $Id$
 * 
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
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.AbstractGDS;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.UnsupportedEncodingException;

public abstract class BaseGDSImpl extends AbstractGDS {
    
    // TODO Synchronization seems to be inconsistent: sometimes on dbhandle, sometimes on this (and sometimes on blobhandle)
    // TODO Checking for validity of dbhandle is inconsistent (sometimes only null check, sometimes also .isValid())

    private static Logger log = LoggerFactory.getLogger(BaseGDSImpl.class);

    private static final String WARNING_CONNECT_TIMEOUT_NATIVE = 
            "WARNING: The native driver does not apply connectTimeout for establishing the socket connection (only for protocol negotiation with the Firebird server), " + 
            "it will not detect unreachable hosts within the specified timeout";

    public int isc_api_handle;
    
    public BaseGDSImpl() {
        super();
    }

    public BaseGDSImpl(GDSType gdsType) {
        super(gdsType);
    }

    protected abstract String getServerUrl(String file_name) throws GDSException;

    public DatabaseParameterBuffer createDatabaseParameterBuffer() {
        return new DatabaseParameterBufferImp();
    }

    public IscDbHandle createIscDbHandle() {
        return new isc_db_handle_impl();
    }

    public IscSvcHandle createIscSvcHandle() {
        return new isc_svc_handle_impl();
    }

    public IscTrHandle createIscTrHandle() {
        return new isc_tr_handle_impl();
    }

    public ServiceParameterBuffer createServiceParameterBuffer() {
        return new ServiceParameterBufferImp();
    }

    public ServiceRequestBuffer createServiceRequestBuffer(int taskIdentifier) {
        return new ServiceRequestBufferImp(taskIdentifier);
    }

    // isc_attach_database
    // ---------------------------------------------------------------------------------------------
    public void iscAttachDatabase(String file_name, IscDbHandle db_handle,
            DatabaseParameterBuffer databaseParameterBuffer)
            throws GDSException {
        if (db_handle == null) { 
            throw new GDSException(ISCConstants.isc_bad_db_handle); 
        }

        final byte[] dpbBytes;
        final String filenameCharset;
        if (databaseParameterBuffer != null) {
            DatabaseParameterBuffer cleanDPB = ((DatabaseParameterBufferExtension)databaseParameterBuffer).removeExtensionParams();
            if (cleanDPB.hasArgument(DatabaseParameterBuffer.CONNECT_TIMEOUT)) {
                // For the native driver isc_dpb_connect_timeout is not a socket connect timeout
                // It only applies to the steps for op_accept (negotiating protocol, etc)
                if (log != null) {
                    log.warn(WARNING_CONNECT_TIMEOUT_NATIVE);
                }
                db_handle.addWarning(new GDSWarning(WARNING_CONNECT_TIMEOUT_NATIVE));
            }

            dpbBytes = ((DatabaseParameterBufferImp) cleanDPB).getBytesForNativeCode();
            filenameCharset = databaseParameterBuffer.getArgumentAsString(DatabaseParameterBufferExtension.FILENAME_CHARSET);
        } else {
            dpbBytes = null;
            filenameCharset = null;
        }

        String serverUrl = getServerUrl(file_name);
        
        byte[] urlData;
        try {
            if (filenameCharset != null)
                urlData = serverUrl.getBytes(filenameCharset);
            else
                urlData = serverUrl.getBytes();
            
            byte[] nullTerminated = new byte[urlData.length + 1];
            System.arraycopy(urlData, 0, nullTerminated, 0, urlData.length);
            urlData = nullTerminated;
        } catch(UnsupportedEncodingException ex) {
            throw new GDSException(ISCConstants.isc_bad_dpb_content);
        }
        
        synchronized (db_handle) {
            native_isc_attach_database(urlData, db_handle, dpbBytes);
        }

        parseAttachDatabaseInfo(iscDatabaseInfo(db_handle,
                AbstractGDS.DESCRIBE_DATABASE_INFO_BLOCK, 1024), db_handle);
    }

    // isc_create_blob2
    // ---------------------------------------------------------------------------------------------
    public void iscCreateBlob2(IscDbHandle db_handle, IscTrHandle tr_handle,
            IscBlobHandle blob_handle, BlobParameterBuffer blobParameterBuffer)
            throws GDSException {
        if (db_handle == null) { 
            throw new GDSException(ISCConstants.isc_bad_db_handle); 
        }
        if (tr_handle == null) { 
            throw new GDSException(ISCConstants.isc_bad_trans_handle); 
        }
        if (blob_handle == null) { 
            throw new GDSException(ISCConstants.isc_bad_segstr_handle); 
        }

        final byte[] bpb = blobParameterBuffer == null ? null
                : ((BlobParameterBufferImp) blobParameterBuffer)
                        .getBytesForNativeCode();

        synchronized (db_handle) {
            native_isc_create_blob2(db_handle, tr_handle, blob_handle, bpb);

            blob_handle.setDb(db_handle);
            blob_handle.setTr(tr_handle);
            tr_handle.addBlob(blob_handle);
        }
    }

    // isc_attach_database
    // ---------------------------------------------------------------------------------------------
    public byte[] iscDatabaseInfo(IscDbHandle db_handle, byte[] items,
            int buffer_length) throws GDSException {
        synchronized (db_handle) {
            final byte[] returnValue = new byte[buffer_length];

            native_isc_database_info(db_handle, items.length, items,
                    buffer_length, returnValue);

            return returnValue;
        }
    }

    // isc_detach_database
    // ---------------------------------------------------------------------------------------------
    public void iscDetachDatabase(IscDbHandle db_handle) throws GDSException {
        if (db_handle == null) { throw new GDSException(ISCConstants.isc_bad_db_handle); }

        synchronized (db_handle) {
            native_isc_detach_database(db_handle);
            try {
                db_handle.invalidate();
            } catch (Exception e) {
                // Actual implementation does not throw exception
                // TODO : Invalidate should throw GDSException?
                throw new GDSException(ISCConstants.isc_network_error, e);
            }
        }
    }

    // isc_open_blob2
    // ---------------------------------------------------------------------------------------------
    public void iscOpenBlob2(IscDbHandle db_handle, IscTrHandle tr_handle,
            IscBlobHandle blob_handle, BlobParameterBuffer blobParameterBuffer)
            throws GDSException {
        if (db_handle == null) { 
            throw new GDSException(ISCConstants.isc_bad_db_handle); 
        }
        if (tr_handle == null) { 
            throw new GDSException(ISCConstants.isc_bad_trans_handle); 
        }
        if (blob_handle == null) { 
            throw new GDSException(ISCConstants.isc_bad_segstr_handle); 
        }

        final byte[] bpb = blobParameterBuffer == null ? null
                : ((BlobParameterBufferImp) blobParameterBuffer)
                        .getBytesForNativeCode();

        synchronized (db_handle) {
            native_isc_open_blob2(db_handle, tr_handle, blob_handle, bpb);

            blob_handle.setDb(db_handle);
            blob_handle.setTr(tr_handle);
            tr_handle.addBlob(blob_handle);
        }
    }

    // isc_prepare_transaction
    // ---------------------------------------------------------------------------------------------
    public void iscPrepareTransaction(IscTrHandle tr_handle)
            throws GDSException {
        if (tr_handle == null) { 
            throw new GDSException(ISCConstants.isc_bad_trans_handle); 
        }
        
        synchronized (tr_handle.getDbHandle()) {
            if (tr_handle.getState() != IscTrHandle.TRANSACTIONSTARTED) { throw new GDSException(
                    ISCConstants.isc_tra_state); }
            tr_handle.setState(IscTrHandle.TRANSACTIONPREPARING);

            native_isc_prepare_transaction(tr_handle);

            tr_handle.setState(IscTrHandle.TRANSACTIONPREPARED);
        }
    }

    public void iscSeekBlob(IscBlobHandle handle, int position, int mode)
            throws GDSException {
        isc_blob_handle_impl blob = (isc_blob_handle_impl) handle;
        synchronized (handle.getDb()) {
            // TODO Change native method to accept IscBlobHandle
            native_isc_seek_blob(blob, position, mode);
        }
    }

    // Services API

    public void iscServiceAttach(String service, IscSvcHandle serviceHandle,
            ServiceParameterBuffer serviceParameterBuffer) throws GDSException {
        final ServiceParameterBufferImp serviceParameterBufferImp = (ServiceParameterBufferImp) serviceParameterBuffer;
        final byte[] serviceParameterBufferBytes = serviceParameterBufferImp == null ? null
                : serviceParameterBufferImp.toByteArray();

        synchronized (serviceHandle) {
            if (serviceHandle.isValid())
                throw new GDSException("serviceHandle is already attached.");

            native_isc_service_attach(service, serviceHandle,
                    serviceParameterBufferBytes);
        }
    }

    public void iscServiceDetach(IscSvcHandle serviceHandle) throws GDSException {
        synchronized (serviceHandle) {
            if (serviceHandle.isNotValid())
                throw new GDSException("serviceHandle is not attached.");

            native_isc_service_detach(serviceHandle);
        }
    }

    public void iscServiceQuery(IscSvcHandle serviceHandle,
            ServiceParameterBuffer serviceParameterBuffer,
            ServiceRequestBuffer serviceRequestBuffer, byte[] resultBuffer)
            throws GDSException {
        final ServiceParameterBufferImp serviceParameterBufferImp = (ServiceParameterBufferImp) serviceParameterBuffer;
        final byte[] serviceParameterBufferBytes = serviceParameterBufferImp == null ? null
                : serviceParameterBufferImp.toByteArray();

        final ServiceRequestBufferImp serviceRequestBufferImp = (ServiceRequestBufferImp) serviceRequestBuffer;
        final byte[] serviceRequestBufferBytes = serviceRequestBufferImp == null ? null
                : serviceRequestBufferImp.toByteArray();

        synchronized (serviceHandle) {
            if (serviceHandle.isNotValid())
                throw new GDSException("serviceHandle is not attached.");

            native_isc_service_query(serviceHandle,
                    serviceParameterBufferBytes, serviceRequestBufferBytes,
                    resultBuffer);
        }
    }

    public void iscServiceStart(IscSvcHandle serviceHandle,
            ServiceRequestBuffer serviceRequestBuffer) throws GDSException {
        final ServiceRequestBufferImp serviceRequestBufferImp = (ServiceRequestBufferImp) serviceRequestBuffer;
        final byte[] serviceRequestBufferBytes = serviceRequestBufferImp == null ? null
                : serviceRequestBufferImp.toByteArray();

        synchronized (serviceHandle) {
            if (serviceHandle.isNotValid())
                throw new GDSException("serviceHandle is not attached.");

            native_isc_service_start(serviceHandle, serviceRequestBufferBytes);
        }
    }

    // isc_start_transaction
    // ---------------------------------------------------------------------------------------------
    public void iscStartTransaction(IscTrHandle tr_handle,
            IscDbHandle db_handle, TransactionParameterBuffer tpb)
            throws GDSException {
        TransactionParameterBufferImpl tpbImpl = (TransactionParameterBufferImpl) tpb;

        if (tr_handle == null) { 
            throw new GDSException(ISCConstants.isc_bad_trans_handle); 
        }

        if (db_handle == null) { 
            throw new GDSException(ISCConstants.isc_bad_db_handle); 
        }

        synchronized (db_handle) {
            if (tr_handle.getState() != IscTrHandle.NOTRANSACTION)
                throw new GDSException(ISCConstants.isc_tra_state);

            tr_handle.setState(IscTrHandle.TRANSACTIONSTARTING);

            byte[] arg = tpbImpl.getBytesForNativeCode();
            native_isc_start_transaction(tr_handle, db_handle, arg);

            tr_handle.setDbHandle(db_handle);

            tr_handle.setState(IscTrHandle.TRANSACTIONSTARTED);
        }
    }

    public abstract void native_isc_attach_database(byte[] file_name,
            IscDbHandle db_handle, byte[] dpbBytes);

    public abstract void native_isc_create_blob2(IscDbHandle db,
            IscTrHandle tr, IscBlobHandle blob, byte[] dpbBytes);

    public abstract void native_isc_database_info(IscDbHandle db_handle,
            int item_length, byte[] items, int buffer_length, byte[] buffer)
            throws GDSException;

    public abstract void native_isc_detach_database(IscDbHandle db_handle)
            throws GDSException;

    public abstract void native_isc_open_blob2(IscDbHandle db, IscTrHandle tr,
            IscBlobHandle blob, byte[] dpbBytes);

    public abstract void native_isc_prepare_transaction(IscTrHandle tr_handle)
            throws GDSException;

    public abstract void native_isc_seek_blob(isc_blob_handle_impl handle,
            int position, int mode) throws GDSException;

    // Services API abstract methods
    public abstract void native_isc_service_attach(String service,
            IscSvcHandle serviceHandle, byte[] serviceParameterBuffer)
            throws GDSException;

    public abstract void native_isc_service_detach(IscSvcHandle serviceHandle)
            throws GDSException;

    public abstract void native_isc_service_query(IscSvcHandle serviceHandle,
            byte[] sendServiceParameterBuffer,
            byte[] requestServiceParameterBuffer, byte[] resultBuffer)
            throws GDSException;

    public abstract void native_isc_service_start(IscSvcHandle serviceHandle,
            byte[] serviceParameterBuffer) throws GDSException;

    public abstract void native_isc_start_transaction(IscTrHandle tr_handle,
            IscDbHandle db_handle,
            // Set tpb) throws GDSException;
            byte[] tpb) throws GDSException;

    public abstract int native_isc_que_events(IscDbHandle db_handle,
            EventHandleImp eventHandle, EventHandler handler) 
            throws GDSException;

    public abstract long native_isc_event_block(EventHandleImp eventHandle,
            String eventNames) throws GDSException;

    public abstract void native_isc_event_counts(EventHandleImp eventHandle)
            throws GDSException;

    public abstract void native_isc_cancel_events(IscDbHandle db_handle,
            EventHandleImp eventHandle) throws GDSException;

    public TransactionParameterBuffer newTransactionParameterBuffer() {
        return new TransactionParameterBufferImpl();
    }

    public int iscQueueEvents(IscDbHandle dbHandle, 
            EventHandle eventHandle, EventHandler eventHandler) 
            throws GDSException {
        
        EventHandleImp eventHandleImp = (EventHandleImp)eventHandle;
        if (!eventHandleImp.isValid()){
            throw new IllegalStateException(
                    "Can't queue events on an invalid EventHandle");
        }
        if (eventHandleImp.isCancelled()){
            throw new IllegalStateException(
                    "Can't queue events on a cancelled EventHandle");
        }
        synchronized (dbHandle) {
            return native_isc_que_events(
                    dbHandle, eventHandleImp, eventHandler);
        }
    }

    public void iscEventBlock(EventHandle eventHandle) 
            throws GDSException {
        
        EventHandleImp eventHandleImp = (EventHandleImp)eventHandle;
        native_isc_event_block(
                eventHandleImp, eventHandle.getEventName());
    }

    public void iscEventCounts(EventHandle eventHandle)
            throws GDSException {

        EventHandleImp eventHandleImp = (EventHandleImp)eventHandle;
        if (!eventHandleImp.isValid()){
            throw new IllegalStateException(
                    "Can't get counts on an invalid EventHandle");
        }
        native_isc_event_counts(eventHandleImp);
    }


    public void iscCancelEvents(IscDbHandle dbHandle, EventHandle eventHandle)
            throws GDSException {

        EventHandleImp eventHandleImp = (EventHandleImp)eventHandle;
        if (!eventHandleImp.isValid()){
            throw new IllegalStateException(
                    "Can't cancel an invalid EventHandle");
        }
        if (eventHandleImp.isCancelled()){
            throw new IllegalStateException(
                    "Can't cancel a previously cancelled EventHandle");
        }
        eventHandleImp.cancel();
        synchronized (dbHandle){
            native_isc_cancel_events(dbHandle, eventHandleImp);
        }
    }

    public EventHandle createEventHandle(String eventName){
        return new EventHandleImp(eventName);
    }
    
}
