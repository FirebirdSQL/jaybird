// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2011-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.impl;

import java.io.Serial;
import java.sql.SQLNonTransientException;

/**
 * Exception is thrown when server returns a version string in a format that this driver does not understand.
 */
public class GDSServerVersionException extends SQLNonTransientException {

    @Serial
    private static final long serialVersionUID = -7437228877120690612L;

    public GDSServerVersionException(String message) {
        super(message + "; Expected engine version format: " +
              "<platform>-<type><major version>.<minor version>.<variant>.<build number>[-<revision>] <server name>");
    }

}
