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

/* The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 *
 */

package org.firebirdsql.gds;

import java.util.Set;



/**
 * The interface <code>GDS</code> has most of the C client interface functions
 * lightly mapped to java, as well as the constants returned from the server..
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface GDS {

    // Database functions

    void isc_create_database(String file_name,
                            isc_db_handle db_handle,
                            Clumplet c ) throws GDSException;

    void isc_attach_database(String file_name,
                            isc_db_handle db_handle,
                            Clumplet c) throws GDSException;

    void isc_database_info(isc_db_handle db_handle,
                            int item_length,
                            byte[] items,
                            int buffer_length,
                            byte[] buffer) throws GDSException;

    void isc_detach_database(isc_db_handle db_handle) throws GDSException;

    void isc_drop_database(isc_db_handle db_handle) throws GDSException;

    byte[] isc_expand_dpb(byte[] dpb, int dpb_length,
                          int param, Object[] params) throws GDSException;


    // Transactions

    void isc_start_transaction(    isc_tr_handle tr_handle,
                                isc_db_handle db_handle,
//                                Set tpb) throws GDSException;
                                byte[] tpb) throws GDSException;


    void isc_commit_transaction(    isc_tr_handle tr_handle) throws GDSException;

    void isc_commit_retaining(isc_tr_handle tr_handle) throws GDSException;

    void isc_prepare_transaction(isc_tr_handle tr_handle) throws GDSException;

    void isc_prepare_transaction2(isc_tr_handle tr_handle,
                                   byte[] bytes) throws GDSException;

    void isc_rollback_transaction(isc_tr_handle tr_handle) throws GDSException;

    void isc_rollback_retaining(isc_tr_handle tr_handle) throws GDSException;


    // Dynamic SQL

    void isc_dsql_allocate_statement(isc_db_handle db_handle,
                                       isc_stmt_handle stmt_handle) throws GDSException;

    void isc_dsql_alloc_statement2(isc_db_handle db_handle,
                                     isc_stmt_handle stmt_handle) throws GDSException;

    XSQLDA isc_dsql_describe(isc_stmt_handle stmt_handle,
                            int da_version) throws GDSException;

    XSQLDA isc_dsql_describe_bind(isc_stmt_handle stmt_handle,
                                  int da_version) throws GDSException;

    void isc_dsql_execute(isc_tr_handle tr_handle,
                           isc_stmt_handle stmt_handle,
                           int da_version,
                           XSQLDA xsqlda) throws GDSException;

    void isc_dsql_execute2(isc_tr_handle tr_handle,
                            isc_stmt_handle stmt_handle,
                            int da_version,
                            XSQLDA in_xsqlda,
                            XSQLDA out_xsqlda) throws GDSException;

    void isc_dsql_execute_immediate(isc_db_handle db_handle,
                                      isc_tr_handle tr_handle,
                                      String statement,
                                      int dialect,
                                      XSQLDA xsqlda) throws GDSException;
    void isc_dsql_execute_immediate(isc_db_handle db_handle,
                                      isc_tr_handle tr_handle,
                                      String statement,
                                      String encoding,
                                      int dialect,
                                      XSQLDA xsqlda) throws GDSException;

    void isc_dsql_exec_immed2(isc_db_handle db_handle,
                               isc_tr_handle tr_handle,
                               String statement,
                               int dialect,
                               XSQLDA in_xsqlda,
                               XSQLDA out_xsqlda) throws GDSException;
                               
    void isc_dsql_exec_immed2(isc_db_handle db_handle,
                               isc_tr_handle tr_handle,
                               String statement,
                               String encoding,
                               int dialect,
                               XSQLDA in_xsqlda,
                               XSQLDA out_xsqlda) throws GDSException;

//    byte[][] isc_dsql_fetch(isc_stmt_handle stmt_handle,
    void isc_dsql_fetch(isc_stmt_handle stmt_handle,
                         int da_version,
                         XSQLDA xsqlda, int fetchSize) throws GDSException;

    void isc_dsql_free_statement(isc_stmt_handle stmt_handle,
                                   int option) throws GDSException;

    XSQLDA isc_dsql_prepare(isc_tr_handle tr_handle,
                           isc_stmt_handle stmt_handle,
                           String statement,
                           int dialect) throws GDSException;
                           
    XSQLDA isc_dsql_prepare(isc_tr_handle tr_handle,
                           isc_stmt_handle stmt_handle,
                           String statement,
                           String encoding,
                           int dialect) throws GDSException;

    void isc_dsql_set_cursor_name(isc_stmt_handle stmt_handle,
                                    String cursor_name,
                                    int type) throws GDSException;


    byte[] isc_dsql_sql_info(isc_stmt_handle stmt_handle,
                            /* int item_length, */
                            byte[] items,
                            int buffer_length) throws GDSException;

    void getSqlCounts(isc_stmt_handle stmt) throws GDSException;

    int isc_vax_integer(byte[] buffer, int pos, int length);


    //-----------------------------------------------
    //Blob methods
    //-----------------------------------------------

    void isc_create_blob2(isc_db_handle db,
                        isc_tr_handle tr,
                        isc_blob_handle blob,
                        Clumplet bpb) throws GDSException;

    void isc_open_blob2(isc_db_handle db,
                        isc_tr_handle tr,
                        isc_blob_handle blob,
                        Clumplet bpb) throws GDSException;

    byte[] isc_get_segment(isc_blob_handle blob,
                           int maxread) throws GDSException;

    void isc_put_segment(isc_blob_handle blob_handle,
             byte[] buffer) throws GDSException;

    void isc_close_blob(isc_blob_handle blob) throws GDSException;

    // Handle declaration methods
    isc_db_handle get_new_isc_db_handle();

    isc_tr_handle get_new_isc_tr_handle();

    isc_stmt_handle get_new_isc_stmt_handle();

    isc_blob_handle get_new_isc_blob_handle();
}