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
package org.firebirdsql.internal.tools;

import java.util.function.Supplier;

/**
 * Output formats for messages.
 *
 * @author Mark Rotteveel
 */
public enum OutputFormat {

    /**
     * One file for messages, one file for SQLSTATE.
     */
    SINGLE(SingleFileStore::new),
    /**
     * Per facility a file for messages and a file for SQLSTATE.
     */
    PER_FACILITY(PerFacilityStore::new),
    /**
     * CSV used by Firebird Language Reference.
     * <p>
     * Specifically, appendix 2, <em>Exception Codes and Messages</em>, section <em>SQLCODE and GDSCODE Error Codes and
     * Descriptions</em>.
     * </p>
     */
    LANG_REF_CSV(LangRefCsvStore::new),
    ;

    private final Supplier<FirebirdErrorStore> messageStoreSupplier;

    OutputFormat(Supplier<FirebirdErrorStore> messageStoreSupplier) {
        this.messageStoreSupplier = messageStoreSupplier;
    }

    FirebirdErrorStore createMessageStore() {
        return messageStoreSupplier.get();
    }
}
