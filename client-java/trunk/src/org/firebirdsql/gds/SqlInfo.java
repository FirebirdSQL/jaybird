package org.firebirdsql.gds;

import org.firebirdsql.gds.*;

public class SqlInfo {
	  private int statementType;
	  private int insertCount;
	  private int updateCount;
	  private int deleteCount;
	  private int selectCount; //????

	  public SqlInfo(byte[] buffer, GDS gds) {
			int pos = 0;
			int length;
			int type;
			while ((type = buffer[pos++]) != GDS.isc_info_end) {
				 length = gds.isc_vax_integer(buffer, pos, 2);
				 pos += 2;
				 switch (type) {
					  case GDS.isc_info_sql_records:
							int l;
							int t;
							while ((t = buffer[pos++]) != GDS.isc_info_end) {
								 l = gds.isc_vax_integer(buffer, pos, 2);
								 pos += 2;
								 switch (t) {
									  case GDS.isc_info_req_insert_count:
											insertCount = gds.isc_vax_integer(buffer, pos, l);
											break;
									  case GDS.isc_info_req_update_count:
											updateCount = gds.isc_vax_integer(buffer, pos, l);
											break;
									  case GDS.isc_info_req_delete_count:
											deleteCount = gds.isc_vax_integer(buffer, pos, l);
											break;
									  case GDS.isc_info_req_select_count:
											selectCount = gds.isc_vax_integer(buffer, pos, l);
											break;
									  default:
											break;
								 }
								 pos += l;
							}
							break;
					  case GDS.isc_info_sql_stmt_type:
							statementType = gds.isc_vax_integer(buffer, pos, length);
							pos += length;
							break;
					  default:
							pos += length;
							break;
				 }
			}
	  }

	  public int getStatementType() {
			return statementType;
	  }

	  public int getInsertCount() {
			return insertCount;
	  }

	  public int getUpdateCount() {
			return updateCount;
	  }

	  public int getDeleteCount() {
			return deleteCount;
	  }

	  public int getSelectCount() {
			return selectCount;
	  }
 }