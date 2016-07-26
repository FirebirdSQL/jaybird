/*
 * Firebird Open Source J2EE Connector - JDBC Driver
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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ng.listeners.DatabaseListener;

import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link DatabaseListener} for testing purposes.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class SimpleDatabaseListener implements DatabaseListener {

    private final List<SQLWarning> warnings = Collections.synchronizedList(new ArrayList<SQLWarning>());
    private boolean detaching = false;
    private boolean detached = false;

    public List<SQLWarning> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public void clear() {
        warnings.clear();
    }

    @Override
    public void detaching(FbDatabase database) {
        detaching = true;
    }

    public boolean isDetaching() {
        return detaching;
    }

    @Override
    public void detached(FbDatabase database) {
        detached = true;
    }

    public boolean isDetached() {
        return detached;
    }

    @Override
    public void warningReceived(FbDatabase database, SQLWarning warning) {
        warnings.add(warning);
    }
}
