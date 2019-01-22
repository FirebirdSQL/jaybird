/*
 * Firebird Open Source JavaEE connector - jdbc driver, public Firebird-specific
 * JDBC extensions.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.jdbc;

import org.firebirdsql.util.Volatile;

/**
 * Type codes specific for Jaybird.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@Volatile(reason = "Defined type codes may receive a different value when standardized in JDBC")
public final class JaybirdTypeCodes {

    // TODO Remove when standardized in JDBC

    @Volatile(reason = "To be standardized by future version of JDBC, type code may change")
    public static final int DECFLOAT = -6001;

    // TODO Remove when Java 7 support is removed

    @Volatile(reason = "Provided for JDBC 4.1 / Java 7 compatibility")
    public static final int REF_CURSOR = 2012; // java.sql.Types.REF_CURSOR
    @Volatile(reason = "Provided for JDBC 4.1 / Java 7 compatibility")
    public static final int TIME_WITH_TIMEZONE = 2013; // java.sql.Types.TIME_WITH_TIMEZONE
    @Volatile(reason = "Provided for JDBC 4.1 / Java 7 compatibility")
    public static final int TIMESTAMP_WITH_TIMEZONE = 2014; // java.sql.Types.TIMESTAMP_WITH_TIMEZONE

    private JaybirdTypeCodes() {
        // no instances
    }
}
