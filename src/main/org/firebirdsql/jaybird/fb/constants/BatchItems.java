/*
 * Public Firebird Java API.
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
package org.firebirdsql.jaybird.fb.constants;

/**
 * Constants for batch items and values (batch config, blob policy and information items).
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
@SuppressWarnings("unused")
public final class BatchItems {

    public static final int BATCH_VERSION_1 = 1;
    
    // Tags
    public static final int TAG_MULTIERROR = 1;
    public static final int TAG_RECORD_COUNTS = 2;
    public static final int TAG_BUFFER_BYTES_SIZE = 3;
    public static final int TAG_BLOB_POLICY = 4;
    public static final int TAG_DETAILED_ERRORS = 5;

    // Information items
    public static final int INF_BUFFER_BYTES_SIZE = 10;
    public static final int INF_DATA_BYTES_SIZE = 11;
    public static final int INF_BLOBS_BYTES_SIZE = 12;
    public static final int INF_BLOB_ALIGNMENT = 13;
    public static final int INF_BLOB_HEADER = 14;

    // Blob policy values
    public static final int BLOB_NONE = 0;
    public static final int BLOB_ID_ENGINE = 1;
    public static final int BLOB_ID_USER = 2;
    public static final int BLOB_STREAM = 3;

    public static final int BLOB_SEGHDR_ALIGN = 2;

    public static final int BATCH_EXECUTE_FAILED = -1;
    public static final int BATCH_SUCCESS_NO_INFO = -2;

    private BatchItems() {
        throw new AssertionError("no instances");
    }

}
