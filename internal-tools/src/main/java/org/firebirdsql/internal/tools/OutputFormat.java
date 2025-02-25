// SPDX-FileCopyrightText: Copyright 2022-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
