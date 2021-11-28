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
package org.firebirdsql.gds.ng;

/**
 * Type of fetch. Parallels P_FETCH from Firebird.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public enum FetchType {

    // P_FETCH.fetch_next
    NEXT(0, true),
    // P_FETCH.fetch_prior
    PRIOR(1, true),
    // P_FETCH.fetch_first
    FIRST(2, false),
    // P_FETCH.fetch_last
    LAST(3, false),
    // P_FETCH.fetch_absolute
    ABSOLUTE(4, false),
    // P_FETCH.fetch_relative
    RELATIVE(5, false),
    ;

    private final int fbFetchType;
    private final boolean batch;

    FetchType(int fbFetchType, boolean batch) {
        this.fbFetchType = fbFetchType;
        this.batch = batch;
    }

    /**
     * @return Firebird fetch type (P_FETCH)
     */
    public int getFbFetchType() {
        return fbFetchType;
    }

    /**
     * @return {@code true} allows batched fetch (multiple rows), {@code false} if fetching only a single row is allowed
     */
    public boolean supportsBatch() {
        return batch;
    }
}
