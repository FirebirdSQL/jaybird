// SPDX-CopyrightText: Copyright 2023-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
@SuppressWarnings({ "JavaModuleNaming", "module" })
module org.firebirdsql.jaybird.chacha64 {
    requires org.firebirdsql.jaybird;
    requires org.bouncycastle.provider;

    // Declare as optional for deployment simplicity
    requires static org.jspecify;

    provides org.firebirdsql.gds.ng.wire.crypt.EncryptionPluginSpi
            with org.firebirdsql.jaybird.chacha64.ChaCha64EncryptionPluginSpi;
}
