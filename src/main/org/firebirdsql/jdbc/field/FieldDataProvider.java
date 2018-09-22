/*
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
package org.firebirdsql.jdbc.field;

/**
 * Provider of the row data.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public interface FieldDataProvider {
    
    /**
     * Get raw content of the filed. This method returns the array of bytes sent
     * by the server back.
     * 
     * @return contents of the field or <code>null</code> if NULL value was
     * sent from the server.
     */
    byte[] getFieldData();
    
    /**
     * Set raw content of the field. 
     * 
     * @param data raw content of the field.
     */
    void setFieldData(byte[] data);

}
