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
/*
 * The Original Code is the Firebird Java GDS implementation.
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */
package org.firebirdsql.gds.impl.wire;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.IscBlobHandle;
import org.firebirdsql.gds.IscDbHandle;
import org.firebirdsql.gds.IscStmtHandle;
import org.firebirdsql.gds.IscSvcHandle;
import org.firebirdsql.gds.IscTrHandle;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.XSQLDA;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.gds.impl.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;

/**
 * Describe class <code>GDS_Impl</code> here.
 * 
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public abstract class AbstractJavaGDSImpl extends AbstractGDS implements GDS {

	public static final String PURE_JAVA_TYPE_NAME = "PURE_JAVA";

	private static Logger log = LoggerFactory.getLogger(AbstractJavaGDSImpl.class, false);

    private static final byte[] zero = new XSQLVAR().encodeInt(0);
    private static final byte[] minusOne = new XSQLVAR().encodeInt(-1);

	static final int MAX_BUFFER_SIZE = 1024;
	
	// TODO: sql_prepare_info* and describe_select_info* are identical, remove one?
	
    final static byte[] sql_prepare_info2 = new byte[] {
            ISCConstants.isc_info_sql_stmt_type,
            ISCConstants.isc_info_sql_select,
            ISCConstants.isc_info_sql_describe_vars,
            ISCConstants.isc_info_sql_sqlda_seq,
            ISCConstants.isc_info_sql_type, ISCConstants.isc_info_sql_sub_type,
            ISCConstants.isc_info_sql_scale, ISCConstants.isc_info_sql_length,
            ISCConstants.isc_info_sql_field,
            ISCConstants.isc_info_sql_relation,
            ISCConstants.isc_info_sql_relation_alias,
            ISCConstants.isc_info_sql_owner, ISCConstants.isc_info_sql_alias,
            ISCConstants.isc_info_sql_describe_end};

    /**
     * Info buffer for Firebird 1.5 and below - it does not support the relation
     * alias info item.
     */
    final static byte[] sql_prepare_info15 = new byte[] {
            ISCConstants.isc_info_sql_stmt_type,
            ISCConstants.isc_info_sql_select,
            ISCConstants.isc_info_sql_describe_vars,
            ISCConstants.isc_info_sql_sqlda_seq,
            ISCConstants.isc_info_sql_type, ISCConstants.isc_info_sql_sub_type,
            ISCConstants.isc_info_sql_scale, ISCConstants.isc_info_sql_length,
            ISCConstants.isc_info_sql_field,
            ISCConstants.isc_info_sql_relation,
            ISCConstants.isc_info_sql_owner, ISCConstants.isc_info_sql_alias,
            ISCConstants.isc_info_sql_describe_end};

    final static byte[] describe_select_info2 = new byte[] {
            ISCConstants.isc_info_sql_stmt_type,
            ISCConstants.isc_info_sql_select,
            ISCConstants.isc_info_sql_describe_vars,
            ISCConstants.isc_info_sql_sqlda_seq,
            ISCConstants.isc_info_sql_type, ISCConstants.isc_info_sql_sub_type,
            ISCConstants.isc_info_sql_scale, ISCConstants.isc_info_sql_length,
            ISCConstants.isc_info_sql_field,
            ISCConstants.isc_info_sql_relation,
            ISCConstants.isc_info_sql_relation_alias,
            ISCConstants.isc_info_sql_owner, ISCConstants.isc_info_sql_alias,
            ISCConstants.isc_info_sql_describe_end};

    /**
     * Info buffer for Firebird 1.5 and below - it does not support the relation
     * alias info item.
     */
    final static byte[] describe_select_info15 = new byte[] {
            ISCConstants.isc_info_sql_stmt_type,
            ISCConstants.isc_info_sql_select,
            ISCConstants.isc_info_sql_describe_vars,
            ISCConstants.isc_info_sql_sqlda_seq,
            ISCConstants.isc_info_sql_type, ISCConstants.isc_info_sql_sub_type,
            ISCConstants.isc_info_sql_scale, ISCConstants.isc_info_sql_length,
            ISCConstants.isc_info_sql_field,
            ISCConstants.isc_info_sql_relation,
            ISCConstants.isc_info_sql_owner, ISCConstants.isc_info_sql_alias,
            ISCConstants.isc_info_sql_describe_end};
    
    final static byte[] describe_bind_info2 = new byte[] {
            ISCConstants.isc_info_sql_stmt_type,
            ISCConstants.isc_info_sql_bind,
            ISCConstants.isc_info_sql_describe_vars,
            ISCConstants.isc_info_sql_sqlda_seq,
            ISCConstants.isc_info_sql_type, ISCConstants.isc_info_sql_sub_type,
            ISCConstants.isc_info_sql_scale, ISCConstants.isc_info_sql_length,
            ISCConstants.isc_info_sql_field,
            ISCConstants.isc_info_sql_relation,
            ISCConstants.isc_info_sql_relation_alias,
            ISCConstants.isc_info_sql_owner, ISCConstants.isc_info_sql_alias,
            ISCConstants.isc_info_sql_describe_end};

    /**
     * Info buffer for Firebird 1.5 and below - it does not support the relation
     * alias info item.
     */
    final static byte[] describe_bind_info15 = new byte[] {
            ISCConstants.isc_info_sql_stmt_type,
            ISCConstants.isc_info_sql_bind,
            ISCConstants.isc_info_sql_describe_vars,
            ISCConstants.isc_info_sql_sqlda_seq,
            ISCConstants.isc_info_sql_type, ISCConstants.isc_info_sql_sub_type,
            ISCConstants.isc_info_sql_scale, ISCConstants.isc_info_sql_length,
            ISCConstants.isc_info_sql_field,
            ISCConstants.isc_info_sql_relation,
            ISCConstants.isc_info_sql_owner, ISCConstants.isc_info_sql_alias,
            ISCConstants.isc_info_sql_describe_end};
    
	public AbstractJavaGDSImpl() {
		super(GDSType.getType(PURE_JAVA_TYPE_NAME));
	}

	// Database functions

	/**
	 * <code>isc_create_database</code> creates a database based on the file
	 * name and Clumplet of database properties supplied. The supplied db handle
	 * is attached to the newly created database.
	 * 
	 * @param file_name
	 *            a <code>String</code> the file name, including host and
	 *            port, for the database. The expected format is
	 *            host:port:path_to_file. The value for host is localhost if not
	 *            supplied. The value for port is 3050 if not supplied.
	 * @param db_handle
	 *            an <code>isc_db_handle</code> The db handle to attach to the
	 *            new database.
	 * @param databaseParameterBuffer
	 *            a <code>Clumplet</code> The parameters for the new database
	 *            and the attachment to it. See docs for dpb (database parameter
	 *            block.)
	 * @exception GDSException
	 *                if an error occurs
	 */
	public void iscCreateDatabase(String file_name, IscDbHandle db_handle,
			DatabaseParameterBuffer databaseParameterBuffer)
			throws GDSException {

	    DbAttachInfo dbai = new DbAttachInfo(file_name);
	    internalAttachDatabase(dbai, db_handle, databaseParameterBuffer, true);
	}

	public void iscAttachDatabase(String connectString, IscDbHandle db_handle,
			DatabaseParameterBuffer databaseParameterBuffer)
			throws GDSException {

		DbAttachInfo dbai = new DbAttachInfo(connectString);
		internalAttachDatabase(dbai, db_handle, databaseParameterBuffer, false);
	}

	protected void internalAttachDatabase(DbAttachInfo dbai, IscDbHandle db_handle,
			DatabaseParameterBuffer databaseParameterBuffer, boolean create)
			throws GDSException {

		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

		if (db == null) {
			throw new GDSException(ISCConstants.isc_bad_db_handle);
		}

		synchronized (db) {
            EncodingDefinition connectionEncoding = EncodingFactory.getDefaultInstance().getEncodingDefinition(
                    databaseParameterBuffer.getArgumentAsString(DatabaseParameterBuffer.LC_CTYPE),
                    databaseParameterBuffer.getArgumentAsString(DatabaseParameterBufferExtension.LOCAL_ENCODING));
            db.setEncodingFactory(EncodingFactory.getDefaultInstance().withDefaultEncodingDefinition(connectionEncoding));

			connect(db, dbai, databaseParameterBuffer);
            
            String filenameCharset = databaseParameterBuffer.getArgumentAsString(
                DatabaseParameterBufferExtension.FILENAME_CHARSET);
            
			try {
				if (debug)
					log.debug(create ? "op_create " : "op_attach ");
				db.out.writeInt(create ? op_create : op_attach);
				db.out.writeInt(0); // packet->p_atch->p_atch_database
                final Encoding filenameEncoding;
                if (filenameCharset == null) {
                    filenameEncoding = db.getEncodingFactory().getDefaultEncoding();
                } else {
                    filenameEncoding = EncodingFactory.getDefaultInstance().getOrCreateEncodingForCharset(Charset.forName(filenameCharset));
                }
				db.out.writeString(dbai.getFileName(), filenameEncoding);

			    databaseParameterBuffer = ((DatabaseParameterBufferExtension)
			            databaseParameterBuffer).removeExtensionParams();
                addProcessId(databaseParameterBuffer);
                addProcessName(databaseParameterBuffer);

				db.out.writeTyped(ISCConstants.isc_dpb_version1, (Xdrable) databaseParameterBuffer);
				db.out.flush();
				if (debug)
					log.debug("sent");

				try {
					receiveResponse(db, -1);
					db.setRdbId(db.getResp_object());
				} catch (GDSException ge) {
					disconnect(db);
					throw ge;
				}
				// read database information
				byte[] iscDatabaseInfo = iscDatabaseInfo(db, AbstractGDS.DESCRIBE_DATABASE_INFO_BLOCK, 1024);
                parseAttachDatabaseInfo(iscDatabaseInfo, db);
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_write_err);
			}
		}
	}

    /**
     * Adds the processId (pid) to the dpb, if available.
     * 
     * @param databaseParameterBuffer
     */
    protected void addProcessName(DatabaseParameterBuffer databaseParameterBuffer) {
        String processName = getSystemPropertyPrivileged("org.firebirdsql.jdbc.processName");
        if (processName != null) {
            databaseParameterBuffer.addArgument(DatabaseParameterBuffer.PROCESS_NAME, processName);
        }
    }

    /**
     * Adds the processName to the dpb, if available.
     * 
     * @param databaseParameterBuffer
     */
    protected void addProcessId(DatabaseParameterBuffer databaseParameterBuffer) {
        String pidStr = getSystemPropertyPrivileged("org.firebirdsql.jdbc.pid");
        if (pidStr != null) {
            try {
                int pid = Integer.parseInt(pidStr);
                databaseParameterBuffer.addArgument(DatabaseParameterBuffer.PROCESS_ID, pid);
            } catch(NumberFormatException ex) {
                // ignore
            }
        }
    }

	private static String getSystemPropertyPrivileged(final String propertyName) {
	    return AccessController.doPrivileged(new PrivilegedAction<String>() {
	       public String run() {
	           return System.getProperty(propertyName);
	       } 
	    });
	}
	
	public byte[] iscDatabaseInfo(IscDbHandle handle, byte[] items,
			int buffer_length) throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) handle;
		synchronized (db) {
			try {
				if (debug)
					log.debug("op_info_database ");
				db.out.writeInt(op_info_database);
				db.out.writeInt(db.getRdbId());
				db.out.writeInt(0);
				db.out.writeBuffer(items);
				db.out.writeInt(buffer_length);
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
				// if (debug) log.debug("parseSqlInfo: first 2 bytes are " +
				// iscVaxInteger2(db.getResp_data(), 0) + " or: " +
				// db.getResp_data()[0] + ", " + db.getResp_data()[1]);
				return db.getResp_data_truncated();
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_network_error, ex);
			}
		}
	}

	public byte[] iscBlobInfo(IscBlobHandle handle, byte[] items,
			int buffer_length) throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) handle.getDb();
		synchronized (handle) {
			try {
				if (debug)
					log.debug("op_info_blob ");
				db.out.writeInt(op_info_blob);
				db.out.writeInt(handle.getRblId());
				db.out.writeInt(0);
				db.out.writeBuffer(items);
				db.out.writeInt(buffer_length);
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
				// if (debug) log.debug("parseSqlInfo: first 2 bytes are " +
				// iscVaxInteger2(db.getResp_data(), 0) + " or: " +
				// db.getResp_data()[0] + ", " + db.getResp_data()[1]);
				return db.getResp_data_truncated();
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_network_error, ex);
			}
		}
	}

	public void iscSeekBlob(IscBlobHandle handle, int position, int seekMode)
			throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) handle.getDb();
		synchronized (handle) {
			try {
				if (debug)
					log.debug("op_info_blob ");
				db.out.writeInt(op_seek_blob);
				db.out.writeInt(handle.getRblId());
				db.out.writeInt(seekMode);
				db.out.writeInt(position);
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_network_error, ex);
			}
		}
	}

	public void iscDetachDatabase(IscDbHandle db_handle) throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) db_handle;
		if (db == null || !db.isValid()) {
			throw new GDSException(ISCConstants.isc_bad_db_handle);
		}

		synchronized (db) {
			try {
                if (db.eventCoordinator != null){
                    db.eventCoordinator.close();
                }

				if (debug)
					log.debug("op_detach ");
				db.out.writeInt(op_detach);
				db.out.writeInt(db.getRdbId());
				db.out.writeInt(op_disconnect);
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);

			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_network_error, ex);
			} finally {
				try {
					disconnect(db);
				} catch (IOException ex2) {
					throw new GDSException(ISCConstants.isc_network_error);
				}
			}
		}
	}

	public void iscDropDatabase(IscDbHandle db_handle) throws GDSException {

		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

		if (db == null) {
			throw new GDSException(ISCConstants.isc_bad_db_handle);
		}

		synchronized (db) {

			try {
				if (debug)
					log.debug("op_drop_database ");
				db.out.writeInt(op_drop_database);
				db.out.writeInt(db.getRdbId());
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_network_error);
			}
		}

	}

	// Transaction functions

	public void iscStartTransaction(IscTrHandle tr_handle,
			IscDbHandle db_handle,
			TransactionParameterBuffer tpb) throws GDSException {

		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

		TransactionParameterBufferImpl tpbImpl = (TransactionParameterBufferImpl) tpb;

		if (tr_handle == null) {
			throw new GDSException(ISCConstants.isc_bad_trans_handle);
		}
		if (db == null) {
			throw new GDSException(ISCConstants.isc_bad_db_handle);
		}
		
		synchronized (db) {
			if (tr_handle.getState() != IscTrHandle.NOTRANSACTION) {
				throw new GDSException(ISCConstants.isc_tra_state);
			}
			tr_handle.setState(IscTrHandle.TRANSACTIONSTARTING);

			try {
				if (debug)
					log.debug("op_transaction ");
				db.out.writeInt(op_transaction);
				db.out.writeInt(db.getRdbId());
				db.out.writeTyped(ISCConstants.isc_tpb_version3, tpbImpl);
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_network_error, ex);
			}
			tr_handle.setTransactionId(db.getResp_object());

			tr_handle.setDbHandle(db);
			tr_handle.setState(IscTrHandle.TRANSACTIONSTARTED);
		}

	}

	public void iscReconnectTransaction(IscTrHandle tr_handle,
			IscDbHandle db_handle, long transactionId) throws GDSException {

		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

		if (tr_handle == null)
			throw new GDSException(ISCConstants.isc_bad_trans_handle);

		if (db == null)
			throw new GDSException(ISCConstants.isc_bad_db_handle);

		synchronized (db) {
			if (tr_handle.getState() != IscTrHandle.NOTRANSACTION)
				throw new GDSException(ISCConstants.isc_tra_state);

			tr_handle.setState(IscTrHandle.TRANSACTIONSTARTING);

			try {
				if (debug)
					log.debug("op_reconnect ");
				db.out.writeInt(op_reconnect);

				// TODO check if sending db handle is needed, most likely not
				db.out.writeInt(db.getRdbId());
				byte[] buf = new byte[4];
				for (int i = 0; i < 4; i++) {
					buf[i] = (byte) (transactionId >>> (i * 8));
				}
				db.out.writeBuffer(buf);
				db.out.flush();
				if (debug)
					log.debug("sent");
				// out.flush();
				receiveResponse(db, -1);
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_network_error, ex);
			}
			tr_handle.setTransactionId(db.getResp_object());

			tr_handle.setDbHandle(db);
			tr_handle.setState(IscTrHandle.TRANSACTIONSTARTED);
			// db.rdb_transactions.addElement(tr);
		}
	}

	public void iscCommitTransaction(IscTrHandle tr_handle) throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		if (tr_handle == null) {
			throw new GDSException(ISCConstants.isc_bad_trans_handle);
		}

		isc_db_handle_impl db = (isc_db_handle_impl) tr_handle.getDbHandle();

		if (db == null || !db.isValid())
		    throw new GDSException(ISCConstants.isc_bad_db_handle);
		
		synchronized (db) {

			if (tr_handle.getState() != IscTrHandle.TRANSACTIONSTARTED
					&& tr_handle.getState() != IscTrHandle.TRANSACTIONPREPARED) {
				throw new GDSException(ISCConstants.isc_tra_state);
			}
			tr_handle.setState(IscTrHandle.TRANSACTIONCOMMITTING);

			try {
				if (debug) {
					log.debug("op_commit ");
					log.debug("tr.rtr_id: " + tr_handle.getTransactionId());
				}
				db.out.writeInt(op_commit);
				db.out.writeInt(tr_handle.getTransactionId());
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}

			tr_handle.setState(IscTrHandle.NOTRANSACTION);
			tr_handle.unsetDbHandle();
		}

	}

	public void iscCommitRetaining(IscTrHandle tr_handle) throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		if (tr_handle == null) {
			throw new GDSException(ISCConstants.isc_bad_trans_handle);
		}
		isc_db_handle_impl db = (isc_db_handle_impl) tr_handle.getDbHandle();
		if (db == null || !db.isValid())
            throw new GDSException(ISCConstants.isc_bad_db_handle);

		synchronized (db) {
			if (tr_handle.getState() != IscTrHandle.TRANSACTIONSTARTED
					&& tr_handle.getState() != IscTrHandle.TRANSACTIONPREPARED) {
				throw new GDSException(ISCConstants.isc_tra_state);
			}
			tr_handle.setState(IscTrHandle.TRANSACTIONCOMMITTING);

			try {
				if (debug)
					log.debug("op_commit_retaining ");
				db.out.writeInt(op_commit_retaining);
				db.out.writeInt(tr_handle.getTransactionId());
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}
			tr_handle.setState(IscTrHandle.TRANSACTIONSTARTED);
		}

	}

	public void iscPrepareTransaction(IscTrHandle tr_handle)
			throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		if (tr_handle == null) {
			throw new GDSException(ISCConstants.isc_bad_trans_handle);
		}
		isc_db_handle_impl db = (isc_db_handle_impl) tr_handle.getDbHandle();

		synchronized (db) {
			if (tr_handle.getState() != IscTrHandle.TRANSACTIONSTARTED) {
				throw new GDSException(ISCConstants.isc_tra_state);
			}
			tr_handle.setState(IscTrHandle.TRANSACTIONPREPARING);
			try {
				if (debug)
					log.debug("op_prepare ");
				db.out.writeInt(op_prepare);
				db.out.writeInt(tr_handle.getTransactionId());
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}
			tr_handle.setState(IscTrHandle.TRANSACTIONPREPARED);
		}
	}

	public void iscPrepareTransaction2(IscTrHandle tr_handle, byte[] bytes)
			throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		if (tr_handle == null) {
			throw new GDSException(ISCConstants.isc_bad_trans_handle);
		}
		isc_db_handle_impl db = (isc_db_handle_impl) tr_handle.getDbHandle();

		synchronized (db) {
			if (tr_handle.getState() != IscTrHandle.TRANSACTIONSTARTED) {
				throw new GDSException(ISCConstants.isc_tra_state);
			}
			tr_handle.setState(IscTrHandle.TRANSACTIONPREPARING);
			try {
				if (debug)
					log.debug("op_prepare2 ");
				db.out.writeInt(op_prepare2);
				db.out.writeInt(tr_handle.getTransactionId());
				db.out.writeBuffer(bytes);
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}

			tr_handle.setState(IscTrHandle.TRANSACTIONPREPARED);
		}
	}

	public void iscRollbackTransaction(IscTrHandle tr_handle)
			throws GDSException {

		boolean debug = log != null && log.isDebugEnabled();
		if (tr_handle == null) {
			throw new GDSException(ISCConstants.isc_bad_trans_handle);
		}
		isc_db_handle_impl db = (isc_db_handle_impl) tr_handle.getDbHandle();
		if (db == null || !db.isValid())
            throw new GDSException(ISCConstants.isc_bad_db_handle);

		synchronized (db) {

			if (tr_handle.getState() == IscTrHandle.NOTRANSACTION) {
				throw new GDSException(ISCConstants.isc_tra_state);
			}
			tr_handle.setState(IscTrHandle.TRANSACTIONROLLINGBACK);

			try {
				if (debug)
					log.debug("op_rollback ");
				db.out.writeInt(op_rollback);
				db.out.writeInt(tr_handle.getTransactionId());
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			} finally {
				tr_handle.setState(IscTrHandle.NOTRANSACTION);
				tr_handle.unsetDbHandle();
			}
		}
	}

	public void iscRollbackRetaining(IscTrHandle tr_handle) throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		if (tr_handle == null) {
			throw new GDSException(ISCConstants.isc_bad_trans_handle);
		}
		isc_db_handle_impl db = (isc_db_handle_impl) tr_handle.getDbHandle();
		if (db == null || !db.isValid())
            throw new GDSException(ISCConstants.isc_bad_db_handle);

		synchronized (db) {
			if (tr_handle.getState() != IscTrHandle.TRANSACTIONSTARTED
					&& tr_handle.getState() != IscTrHandle.TRANSACTIONPREPARED) {
				throw new GDSException(ISCConstants.isc_tra_state);
			}
			tr_handle.setState(IscTrHandle.TRANSACTIONROLLINGBACK);

			try {
				if (debug)
					log.debug("op_rollback_retaining ");
				db.out.writeInt(op_rollback_retaining);
				db.out.writeInt(tr_handle.getTransactionId());
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}
			tr_handle.setState(IscTrHandle.TRANSACTIONSTARTED);
		}
	}

	public byte[] iscTransactionInformation(IscTrHandle tr_handle,
			byte[] requestBuffer, int bufferLen) throws GDSException {

		boolean debug = log != null && log.isDebugEnabled();
		if (tr_handle == null) {
			throw new GDSException(ISCConstants.isc_bad_trans_handle);
		}
		isc_db_handle_impl db = (isc_db_handle_impl) tr_handle.getDbHandle();

		synchronized (db) {
			try {
				if (debug)
					log.debug("op_info_transaction ");
				db.out.writeInt(op_info_transaction);
				db.out.writeInt(tr_handle.getTransactionId());
				db.out.writeInt(0);
				db.out.writeBuffer(requestBuffer);
				db.out.writeInt(bufferLen);
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
				return db.getResp_data();
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}
		}
	}

	// Dynamic SQL

	public void iscDsqlAllocateStatement(IscDbHandle db_handle,
			IscStmtHandle stmt_handle) throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

		if (db_handle == null) {
			throw new GDSException(ISCConstants.isc_bad_db_handle);
		}

		if (stmt_handle == null) {
			throw new GDSException(ISCConstants.isc_bad_req_handle);
		}

		synchronized (db) {
			try {
				if (debug)
					log.debug("op_allocate_statement ");
				db.out.writeInt(op_allocate_statement);
				db.out.writeInt(db.getRdbId());
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
				stmt_handle.setRsrId(db.getResp_object());
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}

			stmt_handle.setRsr_rdb(db);

			// TODO implement statement handle tracking correctly
			// db.rdb_sql_requests.addElement(stmt);
			stmt_handle.setAllRowsFetched(false);
		}

	}

	public void isc_dsql_alloc_statement2(IscDbHandle db_handle,
			IscStmtHandle stmt_handle) throws GDSException {
		throw new GDSException(ISCConstants.isc_wish_list);
	}

	public XSQLDA iscDsqlDescribe(IscStmtHandle stmt_handle, int da_version) throws GDSException {
		byte[] describeSelectInfo = getDescribeSelectInfo(stmt_handle);
        byte[] buffer = iscDsqlSqlInfo(stmt_handle, describeSelectInfo, MAX_BUFFER_SIZE);
        return parseSqlInfo(stmt_handle, buffer, buffer.length, describeSelectInfo);
	}

	public XSQLDA iscDsqlDescribeBind(IscStmtHandle stmt_handle, int da_version) throws GDSException {
		byte[] describeBindInfo = getDescribeBindInfo(stmt_handle);
        byte[] buffer = iscDsqlSqlInfo(stmt_handle, describeBindInfo, MAX_BUFFER_SIZE);
        isc_stmt_handle_impl stmt = (isc_stmt_handle_impl) stmt_handle;
		stmt.setInSqlda(parseSqlInfo(stmt_handle, buffer, buffer.length, describeBindInfo));
		return stmt.getInSqlda();
	}

	public void iscDsqlExecute(IscTrHandle tr_handle,
			IscStmtHandle stmt_handle, int da_version, XSQLDA xsqlda)
			throws GDSException {

		iscDsqlExecute2(tr_handle, stmt_handle, da_version, xsqlda, null);
	}

	public void iscDsqlExecute2(IscTrHandle tr_handle,
			IscStmtHandle stmt_handle, int da_version, XSQLDA in_xsqlda,
			XSQLDA out_xsqlda) throws GDSException {

		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) stmt_handle.getRsr_rdb();

		// Test Handles needed here
		synchronized (db) {
			XdrOutputStream out = db.out;
			try {
				if (debug)
					log.debug((out_xsqlda == null) ? "op_execute "
							: "op_execute2 ");

				out.writeInt((out_xsqlda == null) ? op_execute : op_execute2);
				out.writeInt(stmt_handle.getRsrId());
				out.writeInt(tr_handle.getTransactionId());

				if (in_xsqlda != null) {
					out.writeBuffer(in_xsqlda.blr);
					out.writeInt(0); // message number = in_message_type
					out.writeInt(1); // stmt->rsr_bind_format
					writeSQLData(out, in_xsqlda);
				} else {
					out.writeBuffer(null);
					out.writeInt(0); // message number = in_message_type
					out.writeInt(0); // stmt->rsr_bind_format
				}

				if (out_xsqlda != null) {
				    stmt_handle.clearRows();
					// only need to clear if there is a
					out.writeBuffer(out_xsqlda.blr);
					out.writeInt(0); // out_message_number = out_message_type
				}
				out.flush();
				if (stmt_handle.getOutSqlda() != null)
				    stmt_handle.notifyOpenResultSet();
				if (debug)
					log.debug("sent");
				int op = nextOperation(db.in);
				if (op == op_sql_response) {
					// this would be an Execute procedure
				    stmt_handle.ensureCapacity(1);
					receiveSqlResponse(db, out_xsqlda, (isc_stmt_handle_impl) stmt_handle);
					op = nextOperation(db.in);
					stmt_handle.setAllRowsFetched(true);
					stmt_handle.setSingletonResult(true);
				} else {
				    stmt_handle.setSingletonResult(false);
				    stmt_handle.setAllRowsFetched(false);
				} // end of else
				receiveResponse(db, op);
                
				stmt_handle.registerTransaction(tr_handle);
                
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}
		}
	}

    /**
     * Write a set of SQL data from a <code>XSQLDA</code> data structure.
     *
     * @param xsqlda
     *         The datastructure containing the SQL data to be written
     * @throws IOException
     *         if an error occurs while writing to the
     *         underlying output stream
     */
    protected void writeSQLData(XdrOutputStream out, XSQLDA xsqlda) throws IOException {
        for (int i = 0; i < xsqlda.sqld; i++) {
            XSQLVAR xsqlvar = xsqlda.sqlvar[i];
            if (log != null && log.isDebugEnabled()) {
                if (out == null) {
                    log.debug("db.out null in writeSQLDatum");
                }
                if (xsqlvar.sqldata == null) {
                    log.debug("sqldata null in writeSQLDatum: " + xsqlvar);
                }
                if (xsqlvar.sqldata == null) {
                    log.debug("sqldata still null in writeSQLDatum: " + xsqlvar);
                }
            }
            int len = xsqlda.ioLength[i];
            byte[] buffer = xsqlvar.sqldata;
            int tempType = xsqlvar.sqltype & ~1;
            if (tempType == ISCConstants.SQL_NULL) {
                out.write(xsqlvar.sqldata != null ? zero : minusOne, 4, 0);
            } else if (len == 0) {
                if (buffer != null) {
                    len = buffer.length;
                    out.writeInt(len);
                    out.write(buffer, len, (4 - len) & 3);
                    // sqlind
                    out.write(zero, 4, 0);
                } else {
                    out.writeInt(0);
                    // sqlind
                    out.write(minusOne, 4, 0);
                }
            } else if (len < 0) {
                if (buffer != null) {
                    out.write(buffer, -len, 0);
                    // sqlind
                    out.write(zero, 4, 0);
                } else {
                    out.writePadding(-len, 0x20);
                    // sqlind
                    out.write(minusOne, 4, 0);
                }
            } else {
                // decrement length because it was incremented before
                // TODO Where was it incremented?
                len--;
                if (buffer != null) {
                    int buflen = buffer.length;
                    if (buflen >= len) {
                        out.write(buffer, len, (4 - len) & 3);
                    } else {
                        out.write(buffer, buflen, 0);
                        out.writePadding(len - buflen + ((4 - len) & 3), 0x20);
                    }
                    // sqlind
                    out.write(zero, 4, 0);
                } else {
                    out.writePadding(len + ((4 - len) & 3), 0x20);
                    // sqlind
                    out.write(minusOne, 4, 0);
                }
            }
        }
    }

    /**
     * Read a row of SQL data and store it in the results set of a statement.
     *
     * @param ioLength
     *         array containing the lengths of each column in the
     *         data row that is to be read
     * @param stmt
     *         The statement where the row is to be stored
     * @throws IOException
     *         if an error occurs while reading from the
     *         underlying input stream
     */
    public void readSQLData(XdrInputStream in, int[] ioLength, IscStmtHandle stmt) throws IOException {
        // This only works if not (port->port_flags & PORT_symmetric)
        int numCols = ioLength.length;
        byte[][] row = new byte[numCols][];
        byte[] buffer;
        for (int i = 0; i < numCols; i++) {
            int len = ioLength[i];
            if (len == 0) {
                len = in.readInt();
                buffer = new byte[len];
                in.readFully(buffer, 0, len);
                in.skipPadding(len);
            } else if (len < 0) {
                buffer = new byte[-len];
                in.readFully(buffer, 0, -len);
            } else {
                // len is incremented to avoid value 0 so it must be decremented
                len--;
                buffer = new byte[len];
                in.readFully(buffer, 0, len);
                in.skipPadding(len);
            }
            if (in.readInt() == -1)
                buffer = null;
            row[i] = buffer;
        }
        if (stmt != null)
            stmt.addRow(row);
    }

	public void iscDsqlExecuteImmediate(IscDbHandle db_handle,
			IscTrHandle tr_handle, String statement, int dialect, XSQLDA xsqlda)
			throws GDSException {
		iscDsqlExecImmed2(db_handle, tr_handle, statement, dialect, xsqlda,
				null);
	}

	public void iscDsqlExecuteImmediate(IscDbHandle db_handle,
			IscTrHandle tr_handle, String statement, String encoding,
			int dialect, XSQLDA xsqlda) throws GDSException {
		iscDsqlExecImmed2(db_handle, tr_handle, statement, encoding, dialect,
				xsqlda, null);
	}

	public void iscDsqlExecuteImmediate(IscDbHandle db_handle,
			IscTrHandle tr_handle, byte[] statement, int dialect, XSQLDA xsqlda)
			throws GDSException {

		iscDsqlExecImmed2(db_handle, tr_handle, statement, dialect, xsqlda,
				null);
	}

	public void iscDsqlExecImmed2(IscDbHandle db_handle, IscTrHandle tr_handle,
			String statement, int dialect, XSQLDA in_xsqlda, XSQLDA out_xsqlda)
			throws GDSException {
	    // TODO Suspicious use of NONE
		iscDsqlExecImmed2(db_handle, tr_handle, statement, "NONE", dialect,
				in_xsqlda, out_xsqlda);
	}

	public void iscDsqlExecImmed2(IscDbHandle db_handle, IscTrHandle tr_handle,
			String statement, String encoding, int dialect, XSQLDA in_xsqlda,
			XSQLDA out_xsqlda) throws GDSException {
        iscDsqlExecImmed2(db_handle, tr_handle, getByteArrayForString(db_handle, statement, encoding),
                dialect, in_xsqlda, out_xsqlda);
	}

	public void iscDsqlExecImmed2(IscDbHandle db_handle, IscTrHandle tr_handle,
			byte[] statement, int dialect, XSQLDA in_xsqlda, XSQLDA out_xsqlda)
			throws GDSException {

		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) db_handle;

		// Test Handles

		synchronized (db) {
			XdrOutputStream out = db.out;
			try {

				if (in_xsqlda == null && out_xsqlda == null) {
					if (debug)
						log.debug("op_exec_immediate ");
					out.writeInt(op_exec_immediate);
				} else {
					if (debug)
						log.debug("op_exec_immediate2 ");
					out.writeInt(op_exec_immediate2);

					if (in_xsqlda != null) {
						out.writeBuffer(in_xsqlda.blr);
						out.writeInt(0);
						out.writeInt(1);
						writeSQLData(out, in_xsqlda);
					} else {
						out.writeBuffer(null);
						out.writeInt(0);
						out.writeInt(0);
					}
					if (out_xsqlda != null)
						out.writeBuffer(out_xsqlda.blr);
					else
						out.writeBuffer(null);
					out.writeInt(0);
				}

				out.writeInt(tr_handle.getTransactionId());
				out.writeInt(0);
				out.writeInt(dialect);
				out.writeBuffer(statement);
				out.writeString("", db.getEncodingFactory().getDefaultEncoding());
				out.writeInt(0);
				out.flush();

				if (debug)
					log.debug("sent");

				int op = nextOperation(db.in);
				if (op == op_sql_response) {
					receiveSqlResponse(db, out_xsqlda, null);
					op = nextOperation(db.in);
				}
				receiveResponse(db, op);
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}
		}
	}

	public void iscDsqlFetch(IscStmtHandle stmt_handle, int da_version,
			XSQLDA xsqlda, int fetchSize) throws GDSException {
	    
	    if (stmt_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_req_handle);
        }
	    if (xsqlda == null) {
            throw new GDSException(ISCConstants.isc_dsql_sqlda_err);
        }

		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) stmt_handle.getRsr_rdb();

		if (db == null || !db.isValid()) {
		    throw new GDSException(ISCConstants.isc_bad_db_handle);
		}

		if (fetchSize <= 0) {
			throw new GDSException(ISCConstants.isc_dsql_sqlda_err);
		}
		// Apply fetchSize
		synchronized (db) {
			XdrOutputStream out = db.out;
			XdrInputStream in = db.in;
			try {
				// Fetch next batch of rows
			    stmt_handle.ensureCapacity(fetchSize);
				if (debug)
					log.debug("op_fetch ");
				out.writeInt(op_fetch);
				out.writeInt(stmt_handle.getRsrId());
				out.writeBuffer(xsqlda.blr);
				out.writeInt(0); // p_sqldata_message_number
				out.writeInt(fetchSize); // p_sqldata_messages
				out.flush();
				if (debug)
					log.debug("sent");

				int op = nextOperation(db.in);
				stmt_handle.notifyOpenResultSet();
				if (op == op_fetch_response) {

					int sqldata_status;
					int sqldata_messages;

					do {
						sqldata_status = in.readInt();
						sqldata_messages = in.readInt();

						if (sqldata_messages > 0 && sqldata_status == 0) {
							readSQLData(in, xsqlda.ioLength, stmt_handle);
							// TODO Replace with while
							do {
								op = nextOperation(db.in);
								if (op == op_response) {
									receiveResponse(db, op);
									continue;
								}
							} while (false);
						}

					} while (sqldata_messages > 0 && sqldata_status == 0);

					if (sqldata_status == 100) {
						if (debug)
							log.debug("all rows successfully fetched");
						stmt_handle.setAllRowsFetched(true);
					}
				} else {
					receiveResponse(db, op);
				}
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}
		}
	}

	public static void calculateIOLength(XSQLDA xsqlda) {
		xsqlda.ioLength = new int[xsqlda.sqld];
		for (int i = 0; i < xsqlda.sqld; i++) {
			switch (xsqlda.sqlvar[i].sqltype & ~1) {
			case ISCConstants.SQL_TEXT:
				xsqlda.ioLength[i] = xsqlda.sqlvar[i].sqllen + 1;
				break;
			case ISCConstants.SQL_VARYING:
				xsqlda.ioLength[i] = 0;
				break;
			case ISCConstants.SQL_SHORT:
			case ISCConstants.SQL_LONG:
			case ISCConstants.SQL_FLOAT:
			case ISCConstants.SQL_TYPE_TIME:
			case ISCConstants.SQL_TYPE_DATE:
				xsqlda.ioLength[i] = -4;
				break;
			// case SQL_D_FLOAT:
			// break;
			case ISCConstants.SQL_DOUBLE:
			case ISCConstants.SQL_TIMESTAMP:
			case ISCConstants.SQL_BLOB:
			case ISCConstants.SQL_ARRAY:
			case ISCConstants.SQL_QUAD:
			case ISCConstants.SQL_INT64:
				xsqlda.ioLength[i] = -8;
				break;
			case ISCConstants.SQL_NULL:
			    xsqlda.ioLength[i] = 0;
			    break;
			}
		}
	}

	public void iscDsqlFreeStatement(IscStmtHandle stmt_handle, int option)
			throws GDSException {
	    
	    if (stmt_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_req_handle);
        }
	    
		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) stmt_handle.getRsr_rdb();

		// Does not seem to be possible or necessary to close
		// an execute procedure statement.
		if (stmt_handle.isSingletonResult() && option == ISCConstants.DSQL_close) {
			return;
		}

		synchronized (db) {
			try {
				if (!db.isValid()) {
					// too late, socket has been closed
					return;
				}

				if (debug)
					log.debug("op_free_statement ");
				db.out.writeInt(op_free_statement);
				db.out.writeInt(stmt_handle.getRsrId());
				db.out.writeInt(option);
				db.out.flush();
				if (debug)
					log.debug("sent");

				receiveResponse(db, -1);
				if (option == ISCConstants.DSQL_drop) {
				    stmt_handle.setInSqlda(null);
				    stmt_handle.setOutSqlda(null);
				    stmt_handle.setRsr_rdb(null);
				}
				// those rows are used by cachedFetcher don't clear
				stmt_handle.clearRows();

                try {
                    IscTrHandle tr = stmt_handle.getTransaction();
                    if (tr != null)
                        tr.unregisterStatementFromTransaction(stmt_handle);
                } finally {
                    stmt_handle.unregisterTransaction();
                }

				// TODO implement statement handle tracking correctly
				// db.rdb_sql_requests.remove(stmt);
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}
		}
	}

	public XSQLDA iscDsqlPrepare(IscTrHandle tr_handle,
			IscStmtHandle stmt_handle, String statement, int dialect) throws GDSException {
	    // TODO Suspicious use of NONE
		return iscDsqlPrepare(tr_handle, stmt_handle, statement, "NONE", dialect);
	}

	public XSQLDA iscDsqlPrepare(IscTrHandle tr_handle,
			IscStmtHandle stmt_handle, String statement, String encoding,
			int dialect) throws GDSException {
        return iscDsqlPrepare(tr_handle, stmt_handle, getByteArrayForString(tr_handle.getDbHandle(), statement, encoding),
                dialect);
	}

	public XSQLDA iscDsqlPrepare(IscTrHandle tr_handle,
			IscStmtHandle stmt_handle, byte[] statement, int dialect)
			throws GDSException {

	    if (tr_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_trans_handle);
        }
        if (stmt_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_req_handle);
        }
	    
	    boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) stmt_handle.getRsr_rdb();

		// reinitialize stmt SQLDA members.
		stmt_handle.setInSqlda(null);
		stmt_handle.setOutSqlda(null);

		synchronized (db) {
			try {
				if (debug)
					log.debug("op_prepare_statement ");
				db.out.writeInt(op_prepare_statement);
				db.out.writeInt(tr_handle.getTransactionId());
				db.out.writeInt(stmt_handle.getRsrId());
				db.out.writeInt(dialect);
				db.out.writeBuffer(statement);
				byte[] sqlPrepareInfo = getSqlPrepareInfo(stmt_handle);
                db.out.writeBuffer(sqlPrepareInfo);
				db.out.writeInt(MAX_BUFFER_SIZE);
				db.out.flush();

				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
				stmt_handle.setOutSqlda(parseSqlInfo(stmt_handle, db.getResp_data(),
						db.getResp_data_len(), sqlPrepareInfo));
				return stmt_handle.getOutSqlda();
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}

			// RSR_blob ??????????
		}

	}

	public void iscDsqlSetCursorName(IscStmtHandle stmt_handle,
			String cursor_name, int type) throws GDSException {
	    
	    if (stmt_handle == null) {
            throw new GDSException(ISCConstants.isc_bad_req_handle);
        }
	    
		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) stmt_handle.getRsr_rdb();

		synchronized (db) {
			try {
				if (debug)
					log.debug("op_set_cursor ");
				db.out.writeInt(op_set_cursor);
				db.out.writeInt(stmt_handle.getRsrId());

				byte[] buffer = new byte[cursor_name.length() + 1];
				System.arraycopy(cursor_name.getBytes(), 0, buffer, 0,
						cursor_name.length());
				buffer[cursor_name.length()] = (byte) 0;

				db.out.writeBuffer(buffer);
				db.out.writeInt(0);
				db.out.flush();
				if (debug)
					log.debug("sent");

				receiveResponse(db, -1);
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}
		}

	}

	public byte[] iscDsqlSqlInfo(IscStmtHandle stmt_handle, byte[] items,
			int buffer_length) throws GDSException {

		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) stmt_handle.getRsr_rdb();

		synchronized (db) {
			try {
				if (debug)
					log.debug("op_info_sql ");
				db.out.writeInt(op_info_sql);
				db.out.writeInt(stmt_handle.getRsrId());
				db.out.writeInt(0);
				db.out.writeBuffer(items);
				db.out.writeInt(buffer_length);
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
				return db.getResp_data_truncated();
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}
		}

	}

	// -----------------------------------------------
	// Blob methods
	// -----------------------------------------------

	public void iscCreateBlob2(IscDbHandle db_handle, IscTrHandle tr_handle,
			IscBlobHandle blob_handle, // contains blob_id
			BlobParameterBuffer blobParameterBuffer) throws GDSException {
		openOrCreateBlob(db_handle, tr_handle, blob_handle,
				blobParameterBuffer,
				(blobParameterBuffer == null) ? op_create_blob
						: op_create_blob2);
	}

	public void iscOpenBlob2(IscDbHandle db_handle, IscTrHandle tr_handle,
			IscBlobHandle blob_handle, // contains blob_id
			BlobParameterBuffer blobParameterBuffer) throws GDSException {
		openOrCreateBlob(db_handle, tr_handle, blob_handle,
				blobParameterBuffer,
				(blobParameterBuffer == null) ? op_open_blob : op_open_blob2);
	}

	private final void openOrCreateBlob(IscDbHandle db_handle,
			IscTrHandle tr_handle, IscBlobHandle blob_handle, // contains
			// blob_id
			BlobParameterBuffer blobParameterBuffer, int op)
			throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) db_handle;
		if (db == null) {
			throw new GDSException(ISCConstants.isc_bad_db_handle);
		}
		if (tr_handle == null) {
			throw new GDSException(ISCConstants.isc_bad_trans_handle);
		}
		if (blob_handle == null) {
			throw new GDSException(ISCConstants.isc_bad_segstr_handle);
		}
		synchronized (db) {
			try {

				if (debug) {
					log
							.debug((blobParameterBuffer == null) ? "op_open/create_blob "
									: "op_open/create_blob2 ");
					log.debug("op: " + op);
				}
				db.out.writeInt(op);
				if (blobParameterBuffer != null) {
					db.out.writeTyped(ISCConstants.isc_bpb_version1,
							(Xdrable) blobParameterBuffer);
				}
				db.out.writeInt(tr_handle.getTransactionId());
				if (debug)
					log.debug("sending blob_id: " + blob_handle.getBlobId());
				db.out.writeLong(blob_handle.getBlobId());
				db.out.flush();

				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
				blob_handle.setDb(db);
				blob_handle.setTr(tr_handle);
				blob_handle.setRblId(db.getResp_object());
				blob_handle.setBlobId(db.getResp_blob_id());
				tr_handle.addBlob(blob_handle);
			} catch (IOException ioe) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}
		}
	}

	public byte[] iscGetSegment(IscBlobHandle blob_handle, int requested)
			throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) blob_handle.getDb();
		if (db == null) {
			throw new GDSException(ISCConstants.isc_bad_db_handle);
		}
		IscTrHandle tr = blob_handle.getTr();
		if (tr == null) {
			throw new GDSException(ISCConstants.isc_bad_trans_handle);
		}
		synchronized (db) {
			try {

				if (debug)
					log.debug("op_get_segment ");
				db.out.writeInt(op_get_segment);
				db.out.writeInt(blob_handle.getRblId());
				if (debug)
					log
							.debug("trying to read bytes: "
									+ ((requested + 2 < Short.MAX_VALUE) ? requested + 2
											: Short.MAX_VALUE));
				db.out
						.writeInt((requested + 2 < Short.MAX_VALUE) ? requested + 2
								: Short.MAX_VALUE);
				db.out.writeInt(0);// writeBuffer for put segment;
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
				if (db.getResp_object() == 2) {
					blob_handle.setEof();
				}

				if (db.getResp_data_len() == 0)
					return new byte[0];

				byte[] buffer = db.getResp_data();
				int bufferLength = db.getResp_data_len();
				// if (buffer.length == 0) {//previous segment was last, this
				// has no data
				// return buffer;
				// }
				int len = 0;
				int srcpos = 0;
				int destpos = 0;
				// TODO It looks like this might cause IndexOutOfBounds if srcpos = bufferLength - 1 and bufferLength = buffer.length
				// TODO Or might read garbage if bufferLength is smaller than buffer.length
				while (srcpos < bufferLength) {
					len = iscVaxInteger2(buffer, srcpos);
					srcpos += 2;
					System.arraycopy(buffer, srcpos, buffer, destpos, len);
					srcpos += len;
					destpos += len;
				}
				byte[] result = new byte[destpos];
				System.arraycopy(buffer, 0, result, 0, destpos);
				return result;

			} catch (IOException ioe) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}
		}
	}

	public void iscPutSegment(IscBlobHandle blob_handle, byte[] buffer)
			throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		isc_db_handle_impl db = (isc_db_handle_impl) blob_handle.getDb();
		if (db == null) {
			throw new GDSException(ISCConstants.isc_bad_db_handle);
		}
		IscTrHandle tr = blob_handle.getTr();
		if (tr == null) {
			throw new GDSException(ISCConstants.isc_bad_trans_handle);
		}
		synchronized (db) {
			try {

				if (debug)
					log.debug("op_batch_segments ");
				db.out.writeInt(op_batch_segments);
				if (debug)
					log.debug("blob.rbl_id:  " + blob_handle.getRblId());
				db.out.writeInt(blob_handle.getRblId());
				if (debug)
					log.debug("buffer.length " + buffer.length);
				db.out.writeBlobBuffer(buffer);
				db.out.flush();
				if (debug)
					log.debug("sent");
				receiveResponse(db, -1);
			} catch (IOException ioe) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}
		}
	}

	public void iscCloseBlob(IscBlobHandle blob_handle) throws GDSException {
	    isc_db_handle_impl db = (isc_db_handle_impl) blob_handle.getDb();
		if (db == null) {
			throw new GDSException(ISCConstants.isc_bad_db_handle);
		}
		IscTrHandle tr = blob_handle.getTr();
		if (tr == null) {
			throw new GDSException(ISCConstants.isc_bad_trans_handle);
		}
		releaseObject(db, op_close_blob, blob_handle.getRblId());
		tr.removeBlob(blob_handle);
	}

	private byte[] getByteArrayForString(IscDbHandle db_handle, String statement, String encoding) {
		Encoding javaEncoding = db_handle.getEncodingFactory().getEncodingForFirebirdName(encoding);
		return javaEncoding.encodeToCharset(statement);
	}

	// Handle declaration methods
	public IscDbHandle createIscDbHandle() {
		return new isc_db_handle_impl();
	}

	public IscTrHandle createIscTrHandle() {
		return new isc_tr_handle_impl();
	}

	public IscStmtHandle createIscStmtHandle() {
		return new isc_stmt_handle_impl();
	}

	public IscBlobHandle createIscBlobHandle() {
		return new isc_blob_handle_impl();
	}

	public void connect(isc_db_handle_impl db, String host, Integer port,
			String filename, DatabaseParameterBuffer databaseParameterBuffer)
			throws GDSException {
		DbAttachInfo dbai = new DbAttachInfo(host, port, filename);
		connect(db, dbai, databaseParameterBuffer);
	}

    protected void connect(isc_db_handle_impl db, DbAttachInfo dbai,
			DatabaseParameterBuffer databaseParameterBuffer)
			throws GDSException {

		boolean debug = log != null && log.isDebugEnabled();

		final int socketBufferSize;
        if (databaseParameterBuffer.hasArgument(DatabaseParameterBufferExtension.SOCKET_BUFFER_SIZE)) {
            socketBufferSize = databaseParameterBuffer.getArgumentAsInt(DatabaseParameterBufferExtension.SOCKET_BUFFER_SIZE);
        } else {
            socketBufferSize = -1;
        }
        
        final int soTimeout;
        if (databaseParameterBuffer.hasArgument(DatabaseParameterBufferExtension.SO_TIMEOUT)) {
            soTimeout = databaseParameterBuffer.getArgumentAsInt(DatabaseParameterBufferExtension.SO_TIMEOUT);
        } else {
            soTimeout = -1;
        }
        
        final int connectTimeout;
        if (databaseParameterBuffer.hasArgument(DatabaseParameterBuffer.CONNECT_TIMEOUT)) {
            connectTimeout = databaseParameterBuffer.getArgumentAsInt(DatabaseParameterBuffer.CONNECT_TIMEOUT) * 1000;
        } else {
            connectTimeout = 0;
        }

		try {
			openSocket(db, dbai, debug, socketBufferSize, soTimeout, connectTimeout);
			
			XdrOutputStream out = db.out;
			XdrInputStream in = db.in;
			String fileName = dbai.getFileName();

			int nextOperation = sendConnectPacket(out, in, fileName);
			
			if (nextOperation == op_accept) {
				db.setProtocol(in.readInt()); // Protocol version number
				in.readInt(); // Architecture for protocol
				in.readInt(); // Minimum type
				if (debug)
					log.debug("received");
			} else {
				disconnect(db);
				if (debug)
					log.debug("not received");
				throw new GDSException(ISCConstants.isc_connect_reject);
			}
		} catch (IOException ex) {
			if (debug)
				log.debug("IOException while trying to connect to db:", ex);
			throw new GDSException(ISCConstants.isc_arg_gds,
					ISCConstants.isc_network_error, dbai.getServer(), ex);
		}
	}

	protected int sendConnectPacket(XdrOutputStream out, XdrInputStream in,
			String fileName) throws IOException {
		
		boolean debug = log != null && log.isDebugEnabled();

		// Here we identify the user to the engine. This may or may not be
		// used as login info to a database.
		String user = getSystemUser();
		
		if (debug)
			log.debug("user.name: " + user);
		
		String host = getSystemHostName();

		byte[] userBytes = user.getBytes();
		byte[] hostBytes = host.getBytes();

		byte[] user_id = new byte[6 + userBytes.length + hostBytes.length];
		int n = 0;
		user_id[n++] = 1; // CNCT_user
		user_id[n++] = (byte) userBytes.length;
		System.arraycopy(userBytes, 0, user_id, n, userBytes.length);
		n += userBytes.length;

		/*
		 * String passwd = "masterkey"; user_id[n++] = 2; // CNCT_passwd
		 * user_id[n++] = (byte) passwd.length();
		 * System.arraycopy(passwd.getBytes(), 0, user_id, n,
		 * passwd.length()); n += passwd.length();
		 */

		user_id[n++] = 4; // CNCT_host
		user_id[n++] = (byte) hostBytes.length;
		System.arraycopy(hostBytes, 0, user_id, n, hostBytes.length);
		n += hostBytes.length;

		user_id[n++] = 6; // CNCT_user_verification
		user_id[n++] = 0;

		if (debug)
			log.debug("op_connect ");
		out.writeInt(op_connect);
		out.writeInt(op_attach);
		out.writeInt(2); // CONNECT_VERSION2
		out.writeInt(1); // arch_generic
		out.writeString(fileName, EncodingFactory.getDefaultInstance().getDefaultEncoding()); // p_cnct_file
		out.writeInt(1); // p_cnct_count
		out.writeBuffer(user_id); // p_cnct_user_id

		out.writeInt(10); // PROTOCOL_VERSION10
		out.writeInt(1); // arch_generic
		out.writeInt(2); // ptype_rpc
		out.writeInt(3); // ptype_batch_send
		out.writeInt(2);
		out.flush();
		if (debug)
			log.debug("sent");

		if (debug)
			log.debug("op_accept ");
		
		int nextOperation = nextOperation(in);
		return nextOperation;
	}
	
	private String getSystemUser() {
        try {
            return getSystemPropertyPrivileged("user.name");
        } catch (SecurityException ex) {
            if (debug())
                log.debug("Unable to retrieve user.name property", ex);
            // TODO Find out if using empty string is sufficient
            return "";
        }
    }
    
    private String getSystemHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch(UnknownHostException ex) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch(UnknownHostException ex1) {
                return "127.0.0.1";
            }
        }
    }

	protected void openSocket(isc_db_handle_impl db, DbAttachInfo dbai,
			boolean debug, int socketBufferSize, int soTimeout, int connectTimeout) throws IOException,
			SocketException, GDSException {
		try {
			db.socket = new Socket();
			// TODO: consider not disabling Nagle
			db.socket.setTcpNoDelay(true);
			
			if (soTimeout != -1)
			    db.socket.setSoTimeout(soTimeout);

			if (socketBufferSize != -1) {
				db.socket.setReceiveBufferSize(socketBufferSize);
				db.socket.setSendBufferSize(socketBufferSize);
			}
			db.socket.connect(new InetSocketAddress(dbai.getServer(), dbai.getPort()), connectTimeout);

			if (debug)
				log.debug("Got socket");
		} catch (UnknownHostException ex2) {
			String message = "Cannot resolve host " + dbai.getServer();
			if (debug)
				log.error(message, ex2);
			throw new GDSException(ISCConstants.isc_arg_gds,
					ISCConstants.isc_network_error, dbai.getServer(), ex2);
		}

		db.out = new XdrOutputStream(db.socket.getOutputStream());
		db.in = new XdrInputStream(db.socket.getInputStream());
	}

	public void disconnect(isc_db_handle_impl db) throws IOException {
		if (log != null)
			log.debug("About to invalidate db handle");
		db.invalidate();
		if (log != null)
			log.debug("successfully invalidated db handle");
	}

	private void receiveSqlResponse(isc_db_handle_impl db, XSQLDA xsqlda,
			isc_stmt_handle_impl stmt) throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		try {
			if (debug)
				log.debug("op_sql_response ");
			int messages = db.in.readInt();
			if (debug)
				log.debug("received");
			if (messages > 0) {
				readSQLData(db.in, xsqlda.ioLength, stmt);
			}
		} catch (IOException ex) {
			if (debug)
				log.warn("IOException in receiveSQLResponse", ex);
			// ex.getMessage() makes little sense here, it will not be displayed
			// because error message for isc_net_read_err does not accept params
			throw new GDSException(ISCConstants.isc_arg_gds,
					ISCConstants.isc_net_read_err, ex.getMessage());
		}
	}

	public void receiveResponse(isc_db_handle_impl db, int op)
			throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		// when used directly
		try {
			if (op == -1)
				op = nextOperation(db.in);
			if (debug)
				log.debug("op_response ");
			if (op == op_response) {
				db.setResp_object(db.in.readInt());
				db.setResp_blob_id(db.in.readLong());

                int len = db.in.readInt();

                byte[] buffer = db.getResp_data();
                if (len > buffer.length) {
                    buffer = new byte[len];
                    db.setResp_data(buffer);
                }

                db.in.readFully(buffer, 0, len);
                db.in.skipPadding(len);
                db.setResp_data_len(len);

                // // db.setResp_data(db.in.readBuffer());
				// if (debug) {
				// log.debug("op_response resp_object: " + db.getResp_object());
				// log.debug("op_response resp_blob_id: " +
				// db.getResp_blob_id());
				// log.debug("op_response resp_data size: " +
				// db.getResp_data().length);
				// }
				// for (int i = 0; i < ((r.resp_data.length< 16) ?
				// r.resp_data.length: 16) ; i++) {
				// if (debug) log.debug("byte: " + r.resp_data[i]);
				// }
				readStatusVector(db);
				if (debug) {
					log.debug("received");
					// checkAllRead(db.in);//DEBUG
				}
			} else {
				if (debug) {
					log.debug("not received: op is " + op);
					// checkAllRead(db.in);
				}
			}
		} catch (IOException ex) {
			if (debug)
				log.warn("IOException in receiveResponse", ex);
			// ex.getMessage() makes little sense here, it will not be displayed
			// because error message for isc_net_read_err does not accept params
			throw new GDSException(ISCConstants.isc_arg_gds,
					ISCConstants.isc_net_read_err, ex.getMessage());
		}
	}

	protected int nextOperation(XdrInputStream in) throws IOException {
		boolean debug = log != null && log.isDebugEnabled();
		int op = 0;
		do {
			op = in.readInt();
			if (debug && op == op_dummy) {
            	log.debug("op_dummy received");
            }
		} while (op == op_dummy);
		return op;
	}

	private void readStatusVector(isc_db_handle_impl db) throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		try {
			GDSException head = null;
			GDSException tail = null;
			while (true) {
				int arg = db.in.readInt();
				switch (arg) {
				case ISCConstants.isc_arg_gds:
					int er = db.in.readInt();
					if (debug)
						log.debug("readStatusVector arg:isc_arg_gds int: " + er);
					if (er != 0) {
						GDSException td = new GDSException(arg, er);
						if (head == null) {
							head = td;
							tail = td;
						} else {
							tail.setNext(td);
							tail = td;
						}
					}
					break;
				case ISCConstants.isc_arg_end:
					if (head != null && !head.isWarning())
						throw head;
					else if (head != null && head.isWarning())
						db.addWarning(head);
					return;
				case ISCConstants.isc_arg_interpreted:
				case ISCConstants.isc_arg_string:
					GDSException ts = new GDSException(arg, db.in.readString(db.getEncodingFactory().getDefaultEncoding()));
					if (debug)
						log.debug("readStatusVector string: " + ts.getMessage());
					if (head == null) {
						head = ts;
						tail = ts;
					} else {
						tail.setNext(ts);
						tail = ts;
					}
					break;
				case ISCConstants.isc_arg_number: {
					int arg_value = db.in.readInt();
					if (debug)
						log.debug("readStatusVector arg:isc_arg_number int: "
								+ arg_value);
					GDSException td = new GDSException(arg, arg_value);
					if (head == null) {
						head = td;
						tail = td;
					} else {
						tail.setNext(td);
						tail = td;
					}
					break;
				}
				default:
					int e = db.in.readInt();
					if (debug)
						log.debug("readStatusVector arg: " + arg + " int: " + e);
					if (e != 0) {
						GDSException td = new GDSException(arg, e);
						if (head == null) {
							head = td;
							tail = td;
						} else {
							tail.setNext(td);
							tail = td;
						}
					}
					break;
				}
			}
		} catch (IOException ioe) {
			// ioe.getMessage() makes little sense here, it will not be displayed
			// because error message for isc_net_read_err does not accept params
			throw new GDSException(ISCConstants.isc_arg_gds,
					ISCConstants.isc_net_read_err, ioe.getMessage());
		}
	}

	public static void calculateBLR(XSQLDA xsqlda) throws GDSException {
		int blr_len = 0;

		if (xsqlda != null) {
			// Determine the BLR length

			blr_len = 8;
			int par_count = 0;
			for (int i = 0; i < xsqlda.sqld; i++) {
				int dtype = xsqlda.sqlvar[i].sqltype & ~1;
				if (dtype == ISCConstants.SQL_VARYING
						|| dtype == ISCConstants.SQL_TEXT 
						|| dtype == ISCConstants.SQL_NULL) {
					blr_len += 3;
				} else if (dtype == ISCConstants.SQL_SHORT
						|| dtype == ISCConstants.SQL_LONG
						|| dtype == ISCConstants.SQL_INT64
						|| dtype == ISCConstants.SQL_QUAD
						|| dtype == ISCConstants.SQL_BLOB
						|| dtype == ISCConstants.SQL_ARRAY) {
					blr_len += 2;
				} else {
					blr_len++;
				}
				blr_len += 2;
				par_count += 2;
			}

			byte[] blr = new byte[blr_len];

			int n = 0;
			blr[n++] = 5; // blr_version5
			blr[n++] = 2; // blr_begin
			blr[n++] = 4; // blr_message
			blr[n++] = 0;

			blr[n++] = (byte) (par_count & 255);
			blr[n++] = (byte) (par_count >> 8);

			for (int i = 0; i < xsqlda.sqld; i++) {
				int dtype = xsqlda.sqlvar[i].sqltype & ~1;
				int len = xsqlda.sqlvar[i].sqllen;
				if (dtype == ISCConstants.SQL_VARYING) {
					blr[n++] = 37; // blr_varying
					blr[n++] = (byte) (len & 255);
					blr[n++] = (byte) (len >> 8);
				} else if (dtype == ISCConstants.SQL_TEXT) {
					blr[n++] = 14; // blr_text
					blr[n++] = (byte) (len & 255);
					blr[n++] = (byte) (len >> 8);
                } else if (dtype == ISCConstants.SQL_NULL) {
                    blr[n++] = 14; // blr_text
                    blr[n++] = 0;
                    blr[n++] = 0;
				} else if (dtype == ISCConstants.SQL_DOUBLE) {
					blr[n++] = 27; // blr_double
				} else if (dtype == ISCConstants.SQL_FLOAT) {
					blr[n++] = 10; // blr_float
				} else if (dtype == ISCConstants.SQL_D_FLOAT) {
					blr[n++] = 11; // blr_d_float
				} else if (dtype == ISCConstants.SQL_TYPE_DATE) {
					blr[n++] = 12; // blr_sql_date
				} else if (dtype == ISCConstants.SQL_TYPE_TIME) {
					blr[n++] = 13; // blr_sql_time
				} else if (dtype == ISCConstants.SQL_TIMESTAMP) {
					blr[n++] = 35; // blr_timestamp
				} else if (dtype == ISCConstants.SQL_BLOB) {
					blr[n++] = 9; // blr_quad
					blr[n++] = 0;
				} else if (dtype == ISCConstants.SQL_ARRAY) {
					blr[n++] = 9; // blr_quad
					blr[n++] = 0;
				} else if (dtype == ISCConstants.SQL_LONG) {
					blr[n++] = 8; // blr_long
					blr[n++] = (byte) xsqlda.sqlvar[i].sqlscale;
				} else if (dtype == ISCConstants.SQL_SHORT) {
					blr[n++] = 7; // blr_short
					blr[n++] = (byte) xsqlda.sqlvar[i].sqlscale;
				} else if (dtype == ISCConstants.SQL_INT64) {
					blr[n++] = 16; // blr_int64
					blr[n++] = (byte) xsqlda.sqlvar[i].sqlscale;
				} else if (dtype == ISCConstants.SQL_QUAD) {
					blr[n++] = 9; // blr_quad
					blr[n++] = (byte) xsqlda.sqlvar[i].sqlscale;
				} else {
					// return error_dsql_804 (gds__dsql_sqlda_value_err);
				}

				blr[n++] = 7; // blr_short
				blr[n++] = 0;
			}

			blr[n++] = (byte) 255; // blr_end
			blr[n++] = 76; // blr_eoc
			// save
			xsqlda.blr = blr;
		}
	}

	private XSQLDA parseSqlInfo(IscStmtHandle stmt_handle, byte[] info,
			int infoLength, byte[] items) throws GDSException {

		boolean debug = log != null && log.isDebugEnabled();
		if (debug)
			log.debug("parseSqlInfo started");

        // check the statement type
        int i = 0;
        if (info[i] == ISCConstants.isc_info_sql_stmt_type) {
            int dataLength = iscVaxInteger2(info, ++i);
            i += 2;
            int statementType = iscVaxInteger(info, i, dataLength);
            ((isc_stmt_handle_impl)stmt_handle).setStatementType(statementType);
            i += dataLength;
        }
        
		XSQLDA xsqlda = new XSQLDA();
		int lastindex = 0;
		int index = 0;
		while ((index = parseTruncSqlInfo(i + 2, info, infoLength, xsqlda, lastindex)) > 0) {
			byte[] new_items = new byte[4 + items.length - 1];
			new_items[0] = ISCConstants.isc_info_sql_sqlda_start;
			new_items[1] = 2;
			new_items[2] = (byte) (index & 255);
			new_items[3] = (byte) (index >> 8);
			System.arraycopy(items, 1, new_items, 4, items.length - 1);

			int size = infoLength;

			// this situation happens only if one XSQLVAR does not fit
			// the buffer. in this case we increase buffer twice and try
			// again
			if (index == lastindex)
				size = infoLength * 2;

			info = iscDsqlSqlInfo(stmt_handle, new_items, size);
			lastindex = index;
            i = 0;
		}
		if (debug)
			log.debug("parseSqlInfo ended");
		calculateBLR(xsqlda);
		calculateIOLength(xsqlda);
		return xsqlda;
	}

	private int parseTruncSqlInfo(int startAt, byte[] info, int infoLength, XSQLDA xsqlda,
			int lastindex) throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		byte item;
		int index = 0;
		if (debug)
			log.debug("parseSqlInfo: first 2 bytes are "
					+ iscVaxInteger2(info, 0) + " or: " + info[0] + ", "
					+ info[1]);

		int i = startAt;

		int len = iscVaxInteger2(info, i);
		i += 2;
		int n = iscVaxInteger(info, i, len);
		i += len;
		if (xsqlda.sqlvar == null) {
			xsqlda.sqld = xsqlda.sqln = n;
			xsqlda.sqlvar = new XSQLVAR[xsqlda.sqln];
		}
		if (debug)
			log.debug("xsqlda.sqln read as " + xsqlda.sqln);
        if (debug)
            log.debug ("info: " + new String(info));

		while (info[i] != ISCConstants.isc_info_end) {
			while ((item = info[i++]) != ISCConstants.isc_info_sql_describe_end) {
				switch (item) {
				case ISCConstants.isc_info_sql_sqlda_seq:
					len = iscVaxInteger2(info, i);
					i += 2;
					index = iscVaxInteger(info, i, len);
					i += len;
					xsqlda.sqlvar[index - 1] = new XSQLVAR();
					if (debug)
						log.debug("new xsqlvar " + (index - 1));
					break;
				case ISCConstants.isc_info_sql_type:
					len = iscVaxInteger2(info, i);
					i += 2;
					xsqlda.sqlvar[index - 1].sqltype = iscVaxInteger(info, i,
							len);
					i += len;
					if (debug)
						log.debug("isc_info_sql_type "
								+ xsqlda.sqlvar[index - 1].sqltype);
					break;
				case ISCConstants.isc_info_sql_sub_type:
					len = iscVaxInteger2(info, i);
					i += 2;
					xsqlda.sqlvar[index - 1].sqlsubtype = iscVaxInteger(info,
							i, len);
					i += len;
					if (debug)
						log.debug("isc_info_sql_sub_type "
								+ xsqlda.sqlvar[index - 1].sqlsubtype);
					break;
				case ISCConstants.isc_info_sql_scale:
					len = iscVaxInteger2(info, i);
					i += 2;
					xsqlda.sqlvar[index - 1].sqlscale = iscVaxInteger(info, i,
							len);
					i += len;
					if (debug)
						log.debug("isc_info_sql_scale "
								+ xsqlda.sqlvar[index - 1].sqlscale);
					break;
				case ISCConstants.isc_info_sql_length:
					len = iscVaxInteger2(info, i);
					i += 2;
					xsqlda.sqlvar[index - 1].sqllen = iscVaxInteger(info, i,
							len);
					i += len;
					if (debug)
						log.debug("isc_info_sql_length "
								+ xsqlda.sqlvar[index - 1].sqllen);
					break;
				case ISCConstants.isc_info_sql_field:
					len = iscVaxInteger2(info, i);
					i += 2;
					xsqlda.sqlvar[index - 1].sqlname = new String(info, i, len);
					i += len;
					if (debug)
						log.debug("isc_info_sql_field "
								+ xsqlda.sqlvar[index - 1].sqlname);
					break;
				case ISCConstants.isc_info_sql_relation:
					len = iscVaxInteger2(info, i);
					i += 2;
					xsqlda.sqlvar[index - 1].relname = new String(info, i, len);
					i += len;
					if (debug)
						log.debug("isc_info_sql_relation "
								+ xsqlda.sqlvar[index - 1].relname);
					break;
				case ISCConstants.isc_info_sql_relation_alias:
					len = iscVaxInteger2(info, i);
					i += 2;
					xsqlda.sqlvar[index - 1].relaliasname = new String(info, i, len);
					i += len;
					if (debug)
						log.debug("isc_info_sql_relation_alias "
								+ xsqlda.sqlvar[index - 1].relaliasname);
					break;
	
				case ISCConstants.isc_info_sql_owner:
					len = iscVaxInteger2(info, i);
					i += 2;
					xsqlda.sqlvar[index - 1].ownname = new String(info, i, len);
					i += len;
					if (debug)
						log.debug("isc_info_sql_owner "
								+ xsqlda.sqlvar[index - 1].ownname);
					break;
				case ISCConstants.isc_info_sql_alias:
					len = iscVaxInteger2(info, i);
					i += 2;
					xsqlda.sqlvar[index - 1].aliasname = new String(info, i,
							len);
					i += len;
					if (debug)
						log.debug("isc_info_sql_alias "
								+ xsqlda.sqlvar[index - 1].aliasname);
					break;
				case ISCConstants.isc_info_truncated:
					if (debug)
						log.debug("isc_info_truncated ");
					return index;
				default:
					throw new GDSException(ISCConstants.isc_dsql_sqlda_err);
				}
			}

			lastindex++;
		}
		return 0;
	}

	private void releaseObject(isc_db_handle_impl db, int op, int id)
			throws GDSException {
		synchronized (db) {
			try {
				db.out.writeInt(op);
				db.out.writeInt(id);
				db.out.flush();
				receiveResponse(db, -1);
			} catch (IOException ioe) {
				throw new GDSException(ISCConstants.isc_net_read_err);
			}
		}
	}

	public DatabaseParameterBuffer createDatabaseParameterBuffer() {
		return new DatabaseParameterBufferImp();
	}

	public TransactionParameterBuffer newTransactionParameterBuffer() {
		return new TransactionParameterBufferImpl();
	}

	public BlobParameterBuffer createBlobParameterBuffer() {
		return new BlobParameterBufferImp();
	}

	public ServiceParameterBuffer createServiceParameterBuffer() {
		return new ServiceParameterBufferImp();
	}

	public ServiceRequestBuffer createServiceRequestBuffer(int taskIdentifier) {
		return new ServiceRequestBufferImp(taskIdentifier);
	}

	public void iscServiceAttach(String service, IscSvcHandle serviceHandle,
			ServiceParameterBuffer serviceParameterBuffer) throws GDSException {

		boolean debug = log != null && log.isDebugEnabled();

		if (serviceHandle == null) {
			throw new GDSException(ISCConstants.isc_bad_svc_handle);
		}

		String serviceMgrStr = "service_mgr";

		int mgrIndex = service.indexOf(serviceMgrStr);
		if (mgrIndex == -1
				|| mgrIndex + serviceMgrStr.length() != service.length())
			throw new GDSException(ISCConstants.isc_arg_gds,
					ISCConstants.isc_svcnotdef, service);

        if (mgrIndex > 0 && service.charAt(mgrIndex - 1) != ':')
            throw new GDSException(ISCConstants.isc_arg_gds,
                ISCConstants.isc_svcnotdef, service);
        
        int port = 3050;
        String host = null;
        
        if (mgrIndex > 0) {
            String server = service.substring(0, mgrIndex - 1);
            host = server;

    		int portIndex = server.indexOf('/');
    		if (portIndex != -1) {
    			try {
    				port = Integer.parseInt(server.substring(portIndex + 1));
    				host = server.substring(0, portIndex);
    			} catch (NumberFormatException ex) {
    				// ignore, nothing happened, we try to connect directly
    			}
    		}
        }

        isc_svc_handle_impl svc = (isc_svc_handle_impl) serviceHandle;
		synchronized (svc) {
			try {
				try {
				    svc.socket = new Socket();
					svc.socket.setTcpNoDelay(true);

					// TODO: Introduce buffer sizes for services
					// TODO: Introduce soTimeout for services
					// if (socketBufferSize != -1) {
					// svc.socket.setReceiveBufferSize(socketBufferSize);
					// svc.socket.setSendBufferSize(socketBufferSize);
					// }
					
					// TODO Use connect timeout
					svc.socket.connect(new InetSocketAddress(host, port));
					if (debug)
						log.debug("Got socket");
				} catch (UnknownHostException ex2) {
					String message = "Cannot resolve host " + host;
					if (debug)
						log.error(message, ex2);
					throw new GDSException(ISCConstants.isc_arg_gds,
							ISCConstants.isc_network_error, host, ex2);
				}

				svc.out = new XdrOutputStream(svc.socket.getOutputStream());
				svc.in = new XdrInputStream(svc.socket.getInputStream());

				int nextOperation = sendConnectPacket(svc.out, svc.in, serviceMgrStr);
				
				if (nextOperation == op_accept) {
					svc.in.readInt(); // Protocol version number
					svc.in.readInt(); // Architecture for protocol
					svc.in.readInt(); // Minimum type
					if (debug)
						log.debug("received");
				} else {
					svc.invalidate();
					if (debug)
						log.debug("not received");
					throw new GDSException(ISCConstants.isc_connect_reject);
				}

				
				if (debug)
					log.debug("op_service_attach ");
				svc.out.writeInt(op_service_attach);
				svc.out.writeInt(0);
                svc.out.writeString(serviceMgrStr, svc.getEncodingFactory().getDefaultEncoding());

				svc.out.writeTyped(ISCConstants.isc_spb_version,
						(Xdrable) serviceParameterBuffer);
				svc.out.flush();

				if (debug)
					log.debug("sent");

				try {
					receiveResponse(svc, -1);
					svc.setHandle(svc.getResp_object());
				} catch (GDSException ge) {
					if (log != null)
						log.debug("About to invalidate db handle");
					svc.invalidate();
					if (log != null)
						log.debug("successfully invalidated db handle");
					throw ge;
				}
			} catch (IOException ex) {
				throw new GDSException(ISCConstants.isc_net_write_err);
			}
		}

	}

	public void receiveResponse(isc_svc_handle_impl svc, int op)
			throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		// when used directly
		try {
			if (op == -1)
				op = nextOperation(svc.in);
			if (debug)
				log.debug("op_response ");
			if (op == op_response) {
				svc.setResp_object(svc.in.readInt());
				svc.setResp_blob_id(svc.in.readLong());
				svc.setResp_data(svc.in.readBuffer());
				if (debug) {
					log.debug("op_response resp_object: "
							+ svc.getResp_object());
				}
				readStatusVector(svc);
				if (debug) {
					log.debug("received");
				}
			} else {
				if (debug) {
					log.debug("not received: op is " + op);
				}
			}
		} catch (IOException ex) {
			if (debug)
				log.warn("IOException in receiveResponse", ex);
			// ex.getMessage() makes little sense here, it will not be displayed
			// because error message for isc_net_read_err does not accept params
			throw new GDSException(ISCConstants.isc_arg_gds,
					ISCConstants.isc_net_read_err, ex.getMessage());
		}
	}

	private void readStatusVector(isc_svc_handle_impl svc) throws GDSException {
		boolean debug = log != null && log.isDebugEnabled();
		try {
			GDSException head = null;
			GDSException tail = null;
			while (true) {
				int arg = svc.in.readInt();
				switch (arg) {
				case ISCConstants.isc_arg_gds:
					int er = svc.in.readInt();
					if (debug)
						log.debug("readStatusVector arg:isc_arg_gds int: " + er);
					if (er != 0) {
						GDSException td = new GDSException(arg, er);
						if (head == null) {
							head = td;
							tail = td;
						} else {
							tail.setNext(td);
							tail = td;
						}
					}
					break;
				case ISCConstants.isc_arg_end:
					if (head != null && !head.isWarning())
						throw head;
					else if (head != null && head.isWarning())
						svc.addWarning(head);

					return;
				case ISCConstants.isc_arg_interpreted:
				case ISCConstants.isc_arg_string:
					GDSException ts = new GDSException(arg, svc.in.readString(svc.getEncodingFactory().getDefaultEncoding()));
					if (debug)
						log.debug("readStatusVector string: " + ts.getMessage());
					if (head == null) {
						head = ts;
						tail = ts;
					} else {
						tail.setNext(ts);
						tail = ts;
					}
					break;
				case ISCConstants.isc_arg_number: {
					int arg_value = svc.in.readInt();
					if (debug)
						log.debug("readStatusVector arg:isc_arg_number int: "
								+ arg_value);
					GDSException td = new GDSException(arg, arg_value);
					if (head == null) {
						head = td;
						tail = td;
					} else {
						tail.setNext(td);
						tail = td;
					}
					break;
				}
				default:
					int e = svc.in.readInt();
					if (debug)
						log
								.debug("readStatusVector arg: " + arg
										+ " int: " + e);
					if (e != 0) {
						GDSException td = new GDSException(arg, e);
						if (head == null) {
							head = td;
							tail = td;
						} else {
							tail.setNext(td);
							tail = td;
						}
					}
					break;
				}
			}
		} catch (IOException ioe) {
			// ioe.getMessage() makes little sense here, it will not be
			// displayed
			// because error message for isc_net_read_err does not accept params
			throw new GDSException(ISCConstants.isc_arg_gds,
					ISCConstants.isc_net_read_err, ioe.getMessage());
		}
	}

	public void iscServiceDetach(IscSvcHandle serviceHandle)
			throws GDSException {

		isc_svc_handle_impl svc = (isc_svc_handle_impl) serviceHandle;

		if (svc == null || svc.out == null)
			throw new GDSException(ISCConstants.isc_bad_svc_handle);

		try {
			try {
				svc.out.writeInt(op_service_detach);
				svc.out.writeInt(svc.getHandle());
				svc.out.writeInt(op_disconnect);
				svc.out.flush();

				receiveResponse(svc, -1);
			} finally {
				svc.invalidate();
			}
		} catch (IOException ex) {
			if (log != null && log.isDebugEnabled())
				log.warn("IOException in isc_service_detach", ex);
			// ex.getMessage() makes little sense here, it will not be displayed
			// because error message for isc_net_read_err does not accept params
			throw new GDSException(ISCConstants.isc_arg_gds,
					ISCConstants.isc_net_read_err, ex.getMessage());
		}
	}

	public void iscServiceStart(IscSvcHandle serviceHandle,
			ServiceRequestBuffer serviceRequestBuffer) throws GDSException {

		isc_svc_handle_impl svc = (isc_svc_handle_impl) serviceHandle;
		ServiceRequestBufferImp svcBuff = (ServiceRequestBufferImp) serviceRequestBuffer;

		if (svc == null || svc.out == null)
			throw new GDSException(ISCConstants.isc_bad_svc_handle);
		try {
			svc.out.writeInt(op_service_start);
			svc.out.writeInt(svc.getHandle());
			svc.out.writeInt(0);

			svc.out.writeBuffer(svcBuff.toByteArray());
			svc.out.flush();

			receiveResponse(svc, -1);
		} catch (IOException ex) {
			if (log != null && log.isDebugEnabled())
				log.warn("IOException in isc_service_start", ex);
			// ex.getMessage() makes little sense here, it will not be displayed
			// because error message for isc_net_read_err does not accept params
			throw new GDSException(ISCConstants.isc_arg_gds,
					ISCConstants.isc_net_read_err, ex.getMessage());
		}
	}

	public void iscServiceQuery(IscSvcHandle serviceHandle,
			ServiceParameterBuffer serviceParameterBuffer,
			ServiceRequestBuffer serviceRequestBuffer, byte[] resultBuffer)
			throws GDSException {

		isc_svc_handle_impl svc = (isc_svc_handle_impl) serviceHandle;
		ServiceParameterBufferImp spb = (ServiceParameterBufferImp) serviceParameterBuffer;
		ServiceRequestBufferImp srb = (ServiceRequestBufferImp) serviceRequestBuffer;

		if (svc == null || svc.out == null)
			throw new GDSException(ISCConstants.isc_bad_svc_handle);

		try {
			svc.out.writeInt(op_service_info);
			svc.out.writeInt(svc.getHandle());
			svc.out.writeInt(0);
			svc.out.writeBuffer(spb != null ? spb.toByteArray() : null);
			svc.out.writeBuffer(srb != null ? srb.toByteArray() : null);
			svc.out.writeInt(resultBuffer.length);
			svc.out.flush();

			receiveResponse(svc, -1);

			int toCopy = Math.min(resultBuffer.length,
					svc.getResp_data().length);
			System.arraycopy(svc.getResp_data(), 0, resultBuffer, 0, toCopy);
		} catch (IOException ex) {
			if (log != null && log.isDebugEnabled())
				log.warn("IOException in isc_service_query", ex);
			// ex.getMessage() makes little sense here, it will not be displayed
			// because error message for isc_net_read_err does not accept params
			throw new GDSException(ISCConstants.isc_arg_gds,
					ISCConstants.isc_net_read_err, ex.getMessage());
		}
	}

	public IscSvcHandle createIscSvcHandle() {
		return new isc_svc_handle_impl();
	}

    public int iscQueueEvents(IscDbHandle dbHandle, 
            EventHandle eventHandle, EventHandler eventHandler) 
            throws GDSException {

        boolean debug = log != null && log.isDebugEnabled();
        isc_db_handle_impl db = (isc_db_handle_impl)dbHandle;

        if (db == null) {
            throw new GDSException(ISCConstants.isc_bad_db_handle);
        }

        synchronized (db) {
            try {
                if (db.eventCoordinator == null){
                    if (debug)
                        log.debug("op_connect_request ");
                    
                    db.out.writeInt(op_connect_request);
                    db.out.writeInt(1);  // Connection type
                    db.out.writeInt(db.getRdbId());
                    db.out.writeInt(0);
                    db.out.flush();
                   
                    nextOperation(db.in); 

                    int auxHandle = db.in.readInt();
                    // garbage
                    db.in.readRawBuffer(8);

                    int respLen = db.in.readInt();
                    respLen += respLen % 4;

                    // sin family
                    db.in.readShort();
                    respLen -= 2;
                    
                    // sin port
                    int port = db.in.readShort();
                    respLen -= 2;

                    // IP address
                    byte[] ipBytes = db.in.readRawBuffer(4);
                    respLen -= 4;
                    
                    StringBuilder ipBuf = new StringBuilder();
                    for (int i = 0; i < 4; i++){
                        ipBuf.append(ipBytes[i] & 0xff);
                        if (i < 3) ipBuf.append(".");
                    }
                    String ipAddress = ipBuf.toString();

                    // Ignore
                    db.in.readRawBuffer(respLen);
                    readStatusVector(db);

                    db.eventCoordinator = 
                        new EventCoordinatorImp(auxHandle, ipAddress, port);
                }

                db.eventCoordinator.queueEvents(
                    db,
                    (EventHandleImp)eventHandle, 
                    eventHandler);
            } catch (IOException ioe){
                throw new GDSException(
                        ISCConstants.isc_arg_gds, 
                        ISCConstants.isc_net_read_err, 
                        ioe.getMessage());
            }
        }

        return 0;
    }

    public void iscEventBlock(EventHandle eventHandle) 
            throws GDSException {

        // Don't need to do anything here, this method just exists
        // for the Type2 driver to map directly to the Interbase API
    }

    public void iscEventCounts(EventHandle eventHandle)
            throws GDSException {
        EventHandleImp handleImp = (EventHandleImp)eventHandle;
        handleImp.calculateCount();
    }

    public void iscCancelEvents(IscDbHandle dbHandle, EventHandle eventHandle)
            throws GDSException {
        isc_db_handle_impl db = (isc_db_handle_impl)dbHandle;
        EventHandleImp handleImp = (EventHandleImp)eventHandle;
        if (db == null){
            throw new GDSException(ISCConstants.isc_bad_db_handle);
        }
        
        if (db.eventCoordinator != null && db.eventCoordinator.cancelEvents(handleImp)){
            synchronized (db){
                try {
                    db.out.writeInt(op_cancel_events);
                    db.out.writeInt(db.getRdbId());
                    db.out.writeInt(handleImp.getLocalId());
                    db.out.flush();
                    receiveResponse(db, -1);
                } catch (IOException ioe){
                    throw new GDSException(
                            ISCConstants.isc_arg_gds, 
                            ISCConstants.isc_net_read_err, 
                            ioe.getMessage());
                }
            }
        }
    }

    public EventHandle createEventHandle(String eventName){
        return new EventHandleImp(eventName);
    }

    class EventCoordinatorImp implements EventCoordinator, Runnable {

        private final int handle;
        private final String ipAddress;
        private final int port;
        private int eventsId;
        isc_db_handle_impl db;
        private final Map<String, EventGlob> globMap = Collections.synchronizedMap(new HashMap<String, EventGlob>());
        private volatile boolean running = true;

        public EventCoordinatorImp(int handle, String ipAddress, int port) 
                throws GDSException {
            this.handle = handle;
            this.ipAddress = ipAddress;
            this.port = port;
            connect();
            Thread eventThread = new Thread(this);
            eventThread.setDaemon(true);
            eventThread.start();
        }

        public boolean cancelEvents(EventHandleImp eventHandle){
            return globMap.remove(
                    Integer.toString(eventHandle.getLocalId())) != null;
        }

        public void run(){
            try {
                while (running){
                    int op = nextOperation(db.in);
                    switch (op){
                    case op_response:
                        receiveResponse(db, op);
                        break;
                    case op_exit:
                    case op_disconnect:
                        close();
                        break;
                    case op_event:
                        // db handle
                        db.in.readInt();
                        byte [] buffer = db.in.readBuffer();

                        // AST info, can be ignored
                        db.in.readLong();

                        int eventId = db.in.readInt();
                        
                        int count = 0;
                        int shift = 0;
                        
                        if (buffer.length > 4) {
                            for (int i = buffer.length - 4; 
                                    i < buffer.length; i++){
                                count += ((buffer[i] & 0xff) << shift);
                                shift += 8;
                            }
                        }

                        EventGlob glob = globMap.remove(Integer.toString(eventId));
                        if (glob != null){
                            glob.getEventHandle().setInternalCount(count);
                            glob.getEventHandler().eventOccurred();
                        }
                        break;
                    }
                }                 
            } catch (IOException ioe) {
                if (log != null) {
                    log.fatal("IOException in event loop: " + ioe.getMessage(), ioe);
                }
            } catch (GDSException gdse) {
                if (log != null) {
                    log.fatal("GDSException in event loop: " + gdse.getMessage(), gdse);
                }
            } finally {
                try {
                    doClose();
                } catch (IOException e) {
                    if (log != null && log.isDebugEnabled()) {
                        log.debug("IOException closing event connection", e);
                    }
                }
            }
        }
        
        /**
         * Connect to receive event callbacks
         */
        private void connect() throws GDSException {
            try {
                db = new isc_db_handle_impl();
                db.socket = new Socket(ipAddress, port);
                db.socket.setTcpNoDelay(true);
                db.out = new XdrOutputStream(db.socket.getOutputStream());
                db.in = new XdrInputStream(db.socket.getInputStream());
            } catch (UnknownHostException uhe){
                throw new GDSException(
                        ISCConstants.isc_arg_gds, 
                        ISCConstants.isc_network_error,
                        ipAddress, uhe);
            } catch (IOException ioe){
                throw new GDSException(
                        ISCConstants.isc_arg_gds, 
                        ISCConstants.isc_network_error, 
                        ipAddress, ioe);
            }
        }

        public void close() throws IOException {
            running = false;
        }

        private void doClose() throws IOException {
            db.invalidate();
        }

        public void queueEvents(isc_db_handle_impl mainDb, 
                EventHandleImp eventHandle, 
                EventHandler eventHandler) throws GDSException {
            synchronized (mainDb){
                try {
                    synchronized (globMap){
                        eventHandle.setLocalId(++eventsId);
                        mainDb.out.writeInt(op_que_events);
                        mainDb.out.writeInt(handle);
                        byte [] epb = eventHandle.toByteArray();
                        mainDb.out.writeBuffer(epb);
                        mainDb.out.writeInt(0); // Address of ast
                        mainDb.out.writeInt(0); // Address of ast arg
                        mainDb.out.writeInt(eventHandle.getLocalId());
                        mainDb.out.flush();

                        receiveResponse(mainDb,-1);
                        int eventId = mainDb.getResp_object();
                        eventHandle.setEventId(eventId);
                        globMap.put(
                                Integer.toString(eventHandle.getLocalId()), 
                                new EventGlob(eventHandler, eventHandle));
                    }

                } catch (IOException ioe){
                    throw new GDSException(
                            ISCConstants.isc_arg_gds, 
                            ISCConstants.isc_net_read_err, 
                            ioe.getMessage());
                }
            }
        }
    }

    static class EventGlob {
        private final EventHandler eventHandler;
        private final EventHandleImp eventHandle;

        public EventGlob(EventHandler handler, EventHandleImp handle){
            eventHandler = handler;
            eventHandle = handle;
        }

        public EventHandler getEventHandler(){
            return eventHandler;
        }

        public EventHandleImp getEventHandle(){
            return eventHandle;
        }
    }

    public void fbCancelOperation(IscDbHandle dbHandle, int kind)
            throws GDSException {
        
        boolean debug = log != null && log.isDebugEnabled();
        isc_db_handle_impl db = (isc_db_handle_impl) dbHandle;
        if (db == null) {
            throw new GDSException(ISCConstants.isc_bad_db_handle);
        }

        //synchronized (db) {
        // TODO: Isn't this going to go wrong when this method and other method interleave?
            try {
                if (debug)
                    log.debug("op_cancel ");
                db.out.writeInt(op_cancel);
                db.out.writeInt(kind);
                db.out.flush();
                if (debug)
                    log.debug("sent");
                // receiveResponse(db, -1);

            } catch (IOException ex) {
                throw new GDSException(ISCConstants.isc_network_error, ex);
            } 
        //}
    }
    
    protected byte[] getDescribeSelectInfo(IscStmtHandle stmtHandle) {
        IscDbHandle db = stmtHandle.getRsr_rdb();
        if (db.getDatabaseProductMajorVersion() == 1 && db.getDatabaseProductMinorVersion() <= 5) {
            return describe_select_info15;
        }
        return describe_select_info2;
    }
    
    protected byte[] getDescribeBindInfo(IscStmtHandle stmtHandle) {
        IscDbHandle db = stmtHandle.getRsr_rdb();
        if (db.getDatabaseProductMajorVersion() == 1 && db.getDatabaseProductMinorVersion() <= 5) {
            return describe_bind_info15;
        }
        return describe_bind_info2;
    }
    
    protected byte[] getSqlPrepareInfo(IscStmtHandle stmtHandle) {
        IscDbHandle db = stmtHandle.getRsr_rdb();
        if (db.getDatabaseProductMajorVersion() == 1 && db.getDatabaseProductMinorVersion() <= 5) {
            return sql_prepare_info15;
        }
        return sql_prepare_info2;
    }
    
    private static boolean debug() {
        return log != null && log.isDebugEnabled();
    }
}
