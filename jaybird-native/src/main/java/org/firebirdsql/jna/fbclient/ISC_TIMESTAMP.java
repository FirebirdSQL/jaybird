/*
 * Firebird Open Source JDBC Driver
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

import com.sun.jna.Structure;

/**
 * JNA wrapper for ISC_TIMESTAMP.
 * <p>
 * This file was initially autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>, a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.
 * </p>
 * <p>
 * This file was modified manually, <strong>do not automatically regenerate!</strong>
 * </p>
 * @since 3.0
 */
@Structure.FieldOrder({ "timestamp_date", "timestamp_time" })
@SuppressWarnings({ "unused", "java:S101", "java:S116", "java:S117", "java:S1104", "java:S2160" })
public class ISC_TIMESTAMP extends Structure {
	/// C type : ISC_DATE
	public int timestamp_date;
	/// C type : ISC_TIME
	public int timestamp_time;
	
	public ISC_TIMESTAMP() {
		super();
	}

	/**
	 * @param timestamp_date C type : ISC_DATE<br>
	 * @param timestamp_time C type : ISC_TIME
	 */
	public ISC_TIMESTAMP(int timestamp_date, int timestamp_time) {
		super();
		this.timestamp_date = timestamp_date;
		this.timestamp_time = timestamp_time;
	}

	public static class ByReference extends ISC_TIMESTAMP implements Structure.ByReference {
	}

	public static class ByValue extends ISC_TIMESTAMP implements Structure.ByValue {
	}
}