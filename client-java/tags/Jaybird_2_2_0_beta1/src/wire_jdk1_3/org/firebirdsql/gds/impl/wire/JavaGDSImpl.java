package org.firebirdsql.gds.impl.wire;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Implements the JDK1.3 specific socket creation.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steve Jardine </a>
 */
public class JavaGDSImpl extends AbstractJavaGDSImpl {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.firebirdsql.gds.impl.wire.AbstractJavaGDSImpl#getSocket(java.lang.String,
	 *      int)
	 */
	protected Socket getSocket(String server, int port) throws IOException,
			UnknownHostException {

		return new Socket(server, port);

	}

}
