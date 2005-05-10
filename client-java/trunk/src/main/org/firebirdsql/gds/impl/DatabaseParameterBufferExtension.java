package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ISCConstants;


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
