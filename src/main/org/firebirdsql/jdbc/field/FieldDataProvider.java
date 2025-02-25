// SPDX-FileCopyrightText: Copyright 2004 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2018-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.jspecify.annotations.Nullable;

/**
 * Provider of the row data.
 * 
 * @author Roman Rokytskyy
 */
public interface FieldDataProvider {
    
    /**
     * Get raw content of the filed. This method returns the array of bytes sent
     * by the server back.
     * 
     * @return contents of the field or <code>null</code> if NULL value was
     * sent from the server.
     */
    byte @Nullable [] getFieldData();
    
    /**
     * Set raw content of the field. 
     * 
     * @param data raw content of the field.
     */
    void setFieldData(byte @Nullable [] data);

}
