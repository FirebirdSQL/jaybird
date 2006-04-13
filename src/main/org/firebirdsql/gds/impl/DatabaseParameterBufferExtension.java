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
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ISCConstants;

/**
 * Extension of the {@link org.firebirdsql.gds.DatabaseParameterBuffer} 
 * interface that allows GDS implementations remove the DPB extension parameters
 * that driver implementation uses for client-side configuration.
 */
public interface DatabaseParameterBufferExtension extends DatabaseParameterBuffer {

    /*
     * Driver-specific DPB params that must be removed before sending them
     * to the server. These params influence only the client side.
     */
    int SOCKET_BUFFER_SIZE      = ISCConstants.isc_dpb_socket_buffer_size;
    int BLOB_BUFFER_SIZE        = ISCConstants.isc_dpb_blob_buffer_size;
    int USE_STREAM_BLOBS        = ISCConstants.isc_dpb_use_stream_blobs;
    int PARANOIA_MODE           = ISCConstants.isc_dpb_paranoia_mode;
    int TIMESTAMP_USES_LOCAL_TIMEZONE =     ISCConstants.isc_dpb_timestamp_uses_local_timezone;
    int USE_STANDARD_UDF        = ISCConstants.isc_dpb_use_standard_udf;
    int LOCAL_ENCODING          = ISCConstants.isc_dpb_local_encoding;
    int MAPPING_PATH            = ISCConstants.isc_dpb_mapping_path;
    int NO_RESULT_SET_TRACKING  = ISCConstants.isc_dpb_no_result_set_tracking;
    

    /**
     * Remove extension parameters in the newly created deep copy of this class.
     * 
     * @return a deep copy of this class where all extension parameters are 
     * removed; needed to filter JayBird extensions that are not understood by
     * Firebird. 
     */
    DatabaseParameterBuffer removeExtensionParams();
}
