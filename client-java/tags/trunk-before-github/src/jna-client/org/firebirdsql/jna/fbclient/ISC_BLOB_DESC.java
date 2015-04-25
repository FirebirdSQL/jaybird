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

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * JNA wrapper for ISC_BLOB_DESC.
 * <p>
 * This file was initially autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>, a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.
 * </p>
 * <p>
 * This file was modified manually, <strong>do not automatically regenerate!</strong>
 * </p>
 * @since 3.0
 */
public class ISC_BLOB_DESC extends Structure {
	public short blob_desc_subtype;
	public short blob_desc_charset;
	public short blob_desc_segment_size;
	/// C type : ISC_UCHAR[32]
	public byte[] blob_desc_field_name = new byte[32];
	/// C type : ISC_UCHAR[32]
	public byte[] blob_desc_relation_name = new byte[32];
	public ISC_BLOB_DESC() {
		super();
	}

    @Override
    protected List getFieldOrder() {
        return Arrays.asList("blob_desc_subtype", "blob_desc_charset", "blob_desc_segment_size", "blob_desc_field_name", "blob_desc_relation_name");
    }

	/**
	 * @param blob_desc_field_name C type : ISC_UCHAR[32]<br>
	 * @param blob_desc_relation_name C type : ISC_UCHAR[32]
	 */
	public ISC_BLOB_DESC(short blob_desc_subtype, short blob_desc_charset, short blob_desc_segment_size, byte blob_desc_field_name[], byte blob_desc_relation_name[]) {
		super();
		this.blob_desc_subtype = blob_desc_subtype;
		this.blob_desc_charset = blob_desc_charset;
		this.blob_desc_segment_size = blob_desc_segment_size;
		if (blob_desc_field_name.length != this.blob_desc_field_name.length) 
			throw new IllegalArgumentException("Wrong array size !");
		this.blob_desc_field_name = blob_desc_field_name;
		if (blob_desc_relation_name.length != this.blob_desc_relation_name.length) 
			throw new IllegalArgumentException("Wrong array size !");
		this.blob_desc_relation_name = blob_desc_relation_name;
	}
	public static class ByReference extends ISC_BLOB_DESC implements Structure.ByReference {
	}
	public static class ByValue extends ISC_BLOB_DESC implements Structure.ByValue {
	}
}
