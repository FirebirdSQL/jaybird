// SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2013-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.event;

record DatabaseEventImpl(String name, int count) implements DatabaseEvent {

    @Override
    public int getEventCount() {
        return count();
    }

    @Override
    public String getEventName() {
        return name();
    }

}
