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
 * {@link GDS#iscCreateBlob2(IscDbHandle, IscTrHandle, IscBlobHandle, BlobParameterBuffer)}
 * or
 * {@link GDS#iscOpenBlob2(IscDbHandle, IscTrHandle, IscBlobHandle, BlobParameterBuffer)}
 * operations.
 * <p>
 * Two features are available:
 * <ul>
 * <li>Specifying the source and target BLOB types (server uses BLOB filters to
 * perform the conversion)
 * <li>Specifying type of the BLOB - either segmented or stream. The only
 * visible to user difference between segmented and stream BLOBs is the fact
 * that "seek" operation is not defined for segmented BLOBs (see
 * {@link GDS#iscSeekBlob(IscBlobHandle, int, int)} for more details).
 * </ul>
 */
public interface BlobParameterBuffer {
    
    int SOURCE_TYPE             =  ISCConstants.isc_bpb_source_type;
    int TARGET_TYPE             =  ISCConstants.isc_bpb_target_type;

    int SOURCE_INTERP           =  ISCConstants.isc_bpb_source_interp;
    int TARGET_INTERP           =  ISCConstants.isc_bpb_target_interp;
    
    int FILTER_PARAMETER        =  ISCConstants.isc_bpb_filter_parameter;

    int TYPE                    =  ISCConstants.isc_bpb_type;
    int TYPE_SEGMENTED          =  ISCConstants.isc_bpb_type_segmented;
    int TYPE_STREAM             =  ISCConstants.isc_bpb_type_stream;

    /**
     * Set a void (valueless) parameter on this 
     * <code>BlobParameterBuffer</code>.
     *
     * @param argumentType The parameter to be set, either an 
     *        <code>ISCConstantsone.isc_bpb_*</code> constant, or one of the 
     *        fields of this interface
     */
    void addArgument(int argumentType);

    /**
     * Set a <code>String</code> parameter on this 
     * <code>BlobParameterBuffer</code>.
     *
     * @param argumentType The parameter to be set, either an 
     *        <code>ISCConstantsone.isc_bpb_*</code> constant, or one of the 
     *        fields of this interface
     * @param value The value to set for the given parameter
     */
    void addArgument(int argumentType, String value);

    /**
     * Set an <code>int</code> parameter on this 
     * <code>BlobParameterBuffer</code>.
     *
     * @param argumentType The parameter to be set, either an 
     *        <code>ISCConstantsone.isc_bpb_*</code> constant, or one of the 
     *        fields of this interface
     * @param value The value to set for the given parameter
     */
    void addArgument(int argumentType, int value);

}
