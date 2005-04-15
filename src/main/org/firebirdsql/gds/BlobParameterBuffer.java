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
 * This interface replaces Clumplet in calls to <code>isc_create_blob2</code> 
 * and <code>isc_open_blob2</code>.
 * <p>
 * Instances are created via <code>GDS.newBlobParameterBuffer();</code>
 * <p>
 * Constants from <code>ISCConstants</code> that are relevant to a blob 
 * parameter buffer are duplicated on this interface. If the original name was
 * <code>isc_bpb_source_type</code> then the new name is 
 * <code>source_type</code>. 
 */
public interface BlobParameterBuffer
    {
    int SOURCE_TYPE             =  ISCConstants.isc_bpb_source_type;
    int TARGET_TYPE             =  ISCConstants.isc_bpb_target_type;
    int TYPE                    =  ISCConstants.isc_bpb_type;
    int SOURCE_INTERP           =  ISCConstants.isc_bpb_source_interp;
    int TARGET_INTERP           =  ISCConstants.isc_bpb_target_interp;
    int FILTER_PARAMETER        =  ISCConstants.isc_bpb_filter_parameter;

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
