package org.firebirdsql.gds.impl.wire;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Implements JDK1.4+ specific socket creation.
 * 
 * This is necessary because of a bug found in the JDK1.4 and is / will not be
 * fixed until JDK7.0.
 * 
 * See bug details: <a
 * href='http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5092063'>
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5092063 </a>
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
	public Socket getSocket(String server, int port) throws IOException,
			UnknownHostException {

		// Check for a valid ip address.
		byte[] address = parseRawAddress(server);

		// Create the socket using an instance of the InetAddress class if the
		// ip address is a valid IPV4 or IPV6 address
		if (address != null)
			return new Socket(InetAddress.getByAddress(address), port);
		else
			return new Socket(server, port);

	}

	/**
	 * Parses a string representation of a raw ip address. Only 4 byte (IPv4) or
	 * 16 byte (IPv6) are acceptable.
	 * 
	 * @param server
	 *            a string representation of the host server.
	 * @return a byte array representing the ip address
	 */
	private byte[] parseRawAddress(String server) {

		byte[] address = null;
		String[] bytes = server.split(".");
		// Only 4 byte (IPv4) and 16 byte (IPv6) addresses are acceptable.
		if (bytes.length == 4 || bytes.length == 16) {

			// convert each string into a byte. If the string doesn't isn't in
			// the range of 0 .. 255 return null.
			address = new byte[bytes.length];
			for (int index = 0; index < bytes.length; index++) {

				int value = Integer.parseInt(bytes[index]);
				if (value > 0 && value < 255)
					address[index] = (byte) value;
				else
					return null;

			}

		}
		return address;

	}

}
