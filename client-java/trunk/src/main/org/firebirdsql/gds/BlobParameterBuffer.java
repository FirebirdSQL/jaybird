 /*
 * Firebird Open Source J2ee connector - jdbc driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
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
    int source_type             =  ISCConstants.isc_bpb_source_type;
    int target_type             =  ISCConstants.isc_bpb_target_type;
    int type                    =  ISCConstants.isc_bpb_type;
    int source_interp           =  ISCConstants.isc_bpb_source_interp;
    int target_interp           =  ISCConstants.isc_bpb_target_interp;
    int filter_parameter        =  ISCConstants.isc_bpb_filter_parameter;

    int type_segmented          =  ISCConstants.isc_bpb_type_segmented;
    int type_stream             =  ISCConstants.isc_bpb_type_stream;

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
