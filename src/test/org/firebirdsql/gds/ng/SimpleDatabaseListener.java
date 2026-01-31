// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.jspecify.annotations.NullMarked;

import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link DatabaseListener} for testing purposes.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@NullMarked
public class SimpleDatabaseListener implements DatabaseListener {

    private final List<SQLWarning> warnings = Collections.synchronizedList(new ArrayList<>());
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
