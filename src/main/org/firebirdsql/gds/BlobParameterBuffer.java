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
 * This interface replaces Clumplet in calls to isc_create_blob2 and isc_open_blob2.
 * 
 * Instances are created via GDS.newBlobParameterBuffer();
 *
 * Constants from ISCConstants that are relevant to a blob parameter buffer are duplicated 
 * on this interface. If the original name where isc_bpb_source_type the new name is source_type. 
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
     *
	 * 
     * @param argumentType
     */
    void addArgument(int argumentType);

    /**
     *
     * @param argumentType
     * @param value
     */
    void addArgument(int argumentType, String value);

    /**
     *
     * @param argumentType
     * @param value
     */
    void addArgument(int argumentType, int value);
    }
