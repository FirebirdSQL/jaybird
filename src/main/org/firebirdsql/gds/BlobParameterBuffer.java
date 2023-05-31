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
package org.firebirdsql.gds;

/**
 * Instance of this interface represents a BLOB Parameter Buffer from the
 * Firebird API documentation and specifies attributes for
 * {@link org.firebirdsql.gds.ng.FbDatabase#createBlobForOutput(org.firebirdsql.gds.ng.FbTransaction, BlobParameterBuffer)}
 * or
 * {@link org.firebirdsql.gds.ng.FbDatabase#createBlobForInput(org.firebirdsql.gds.ng.FbTransaction, BlobParameterBuffer, long)}
 * operations.
 * <p>
 * Two features are available:
 * <ul>
 * <li>Specifying the source and target BLOB types (server uses BLOB filters to
 * perform the conversion)</li>
 * <li>Specifying type of the BLOB - either segmented or stream. The only
 * visible to user difference between segmented and stream BLOBs is the fact
 * that "seek" operation is not defined for segmented BLOBs (see
 * {@link org.firebirdsql.gds.ng.FbBlob#seek(int, org.firebirdsql.gds.ng.FbBlob.SeekMode)}
 * for more details).</li>
 * </ul>
 */
public interface BlobParameterBuffer extends ParameterBuffer {

    /**
     * Set a void (valueless) parameter on this {@code BlobParameterBuffer}.
     *
     * @param argumentType The parameter to be set, either an {@code ISCConstants.isc_bpb_*} constant, or one of the
     *        constants of this interface
     */
    @Override
    void addArgument(int argumentType);

    /**
     * Set a {@code String} parameter on this {@code BlobParameterBuffer}.
     *
     * @param argumentType The parameter to be set, either an {@code ISCConstants.isc_bpb_*} constant, or one of the
     *        constants of this interface
     * @param value The value to set for the given parameter
     */
    @Override
    void addArgument(int argumentType, String value);

    /**
     * Set an {@code int} parameter on this {@code BlobParameterBuffer}.
     *
     * @param argumentType The parameter to be set, either an {@code ISCConstants.isc_bpb_*} constant, or one of the
     *        constants of this interface
     * @param value The value to set for the given parameter
     */
    @Override
    void addArgument(int argumentType, int value);

}
