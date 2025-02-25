// SPDX-CopyrightText: Copyright 2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
@SuppressWarnings({ "JavaModuleNaming", "module" })
module org.firebirdsql.jaybird.chacha64 {
    requires org.firebirdsql.jaybird;
    requires org.bouncycastle.provider;

    provides org.firebirdsql.gds.ng.wire.crypt.EncryptionPluginSpi
            with org.firebirdsql.jaybird.chacha64.ChaCha64EncryptionPluginSpi;
}
