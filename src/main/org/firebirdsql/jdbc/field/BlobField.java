// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.firebirdsql.jdbc.FBObjectListener;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullMarked;

/**
 * Field which expects a blob listener.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
@NullMarked
public interface BlobField {

    /**
     * Sets the blob listener of the field.
     *
     * @param blobListener
     *         blob listener
     */
    void setBlobListener(FBObjectListener.BlobListener blobListener);

}
