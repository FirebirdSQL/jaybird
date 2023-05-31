/**
 * APIs and implementation to manage or obtain information about the Firebird server or a specific database.
 * <p>
 * This includes functionality like:
 * </p>
 * <ul>
 * <li>Creating and dropping databases ({@link org.firebirdsql.management.FBManager})</li>
 * <li>{@code gbak} equivalent ({@link org.firebirdsql.management.FBBackupManager},
 * {@link org.firebirdsql.management.FBStreamingBackupManager})</li>
 * <li>{@code nbackup} equivalent {@link org.firebirdsql.management.FBNBackupManager})</li>
 * <li>{@code gstat} equivalent ({@link org.firebirdsql.management.FBStatisticsManager})</li>
 * <li>{@code gfix} equivalent ({@link org.firebirdsql.management.FBMaintenanceManager})</li>
 * <li>{@code fbtracemgr} equivalent ({@link org.firebirdsql.management.FBTraceManager}</li>
 * <li>{@code gsec} equivalent ({@link org.firebirdsql.management.FBUserManager}) &mdash; NOTE: this has been deprecated
 * in Firebird 3.0, and it is recommend to use SQL statements for user management instead</li>
 * <li>Obtaining connection-specific table statistics ({@link org.firebirdsql.management.FBTableStatisticsManager})</li>
 * </ul>
 */
package org.firebirdsql.management;