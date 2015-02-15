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
/*
 * The Original Code is the Firebird Java GDS implementation.
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */
package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

	private static Logger log = LoggerFactory.getLogger(AbstractJavaGDSImpl.class);

	public AbstractJavaGDSImpl() {
		super(GDSType.getType(PURE_JAVA_TYPE_NAME));
	}

	// Database functions

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

				db.out.writeTyped(databaseParameterBuffer);
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
     * Adds the processName to the dpb, if available.
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
     * Adds the processId (pid) to the dpb, if available.
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

	// Handle declaration methods
	public IscDbHandle createIscDbHandle() {
		return new isc_db_handle_impl();
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

        return nextOperation(in);
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
		int op;
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

	public DatabaseParameterBuffer createDatabaseParameterBuffer() {
		return new DatabaseParameterBufferImp();
	}

	public TransactionParameterBuffer newTransactionParameterBuffer() {
		return new TransactionParameterBufferImpl();
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

				svc.out.writeTyped(serviceParameterBuffer);
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

			svc.out.writeBuffer(svcBuff.toBytes());
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

	public void iscServiceQuery(IscSvcHandle serviceHandle, ServiceParameterBuffer serviceParameterBuffer,
			ServiceRequestBuffer serviceRequestBuffer, byte[] resultBuffer) throws GDSException {

		isc_svc_handle_impl svc = (isc_svc_handle_impl) serviceHandle;

		if (svc == null || svc.out == null)
			throw new GDSException(ISCConstants.isc_bad_svc_handle);

		try {
			svc.out.writeInt(op_service_info);
			svc.out.writeInt(svc.getHandle());
			svc.out.writeInt(0);
            svc.out.writeBuffer(serviceParameterBuffer != null ? serviceParameterBuffer.toBytes() : null);
			svc.out.writeBuffer(serviceRequestBuffer != null ? serviceRequestBuffer.toBytes() : null);
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

                    db.eventCoordinator = new EventCoordinatorImp(auxHandle, ipAddress, port);
                }

                db.eventCoordinator.queueEvents(db, (EventHandleImp) eventHandle, eventHandler);
            } catch (IOException ioe){
                throw new GDSException(ISCConstants.isc_arg_gds, ISCConstants.isc_net_read_err, ioe.getMessage());
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
        private final Map<Integer, EventGlob> globMap = Collections.synchronizedMap(new HashMap<Integer, EventGlob>());
        private volatile boolean running = true;

        public EventCoordinatorImp(int handle, String ipAddress, int port) throws GDSException {
            this.handle = handle;
            this.ipAddress = ipAddress;
            this.port = port;
            connect();
            Thread eventThread = new Thread(this);
            eventThread.setDaemon(true);
            eventThread.start();
        }

        public boolean cancelEvents(EventHandleImp eventHandle){
            return globMap.remove(eventHandle.getLocalId()) != null;
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
                            for (int i = buffer.length - 4; i < buffer.length; i++){
                                count += ((buffer[i] & 0xff) << shift);
                                shift += 8;
                            }
                        }

                        EventGlob glob = globMap.remove(eventId);
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
                throw new GDSException(ISCConstants.isc_arg_gds, ISCConstants.isc_network_error, ipAddress, uhe);
            } catch (IOException ioe){
                throw new GDSException(ISCConstants.isc_arg_gds, ISCConstants.isc_network_error, ipAddress, ioe);
            }
        }

        public void close() throws IOException {
            running = false;
        }

        private void doClose() throws IOException {
            db.invalidate();
        }

        public void queueEvents(isc_db_handle_impl mainDb, EventHandleImp eventHandle, EventHandler eventHandler)
                throws GDSException {
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
                        globMap.put(eventHandle.getLocalId(), new EventGlob(eventHandler, eventHandle));
                    }

                } catch (IOException ioe){
                    throw new GDSException(ISCConstants.isc_arg_gds, ISCConstants.isc_net_read_err, ioe.getMessage());
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

    private static boolean debug() {
        return log != null && log.isDebugEnabled();
    }
}
