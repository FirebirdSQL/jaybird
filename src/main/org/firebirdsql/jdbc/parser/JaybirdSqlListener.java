/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */

// Generated from D:/Development/project/Jaybird/jaybird/src/main/org/firebirdsql/jdbc/parser\JaybirdSql.g4 by ANTLR 4.7.2
package org.firebirdsql.jdbc.parser;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JaybirdSqlParser}.
 */
public interface JaybirdSqlListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(JaybirdSqlParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(JaybirdSqlParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#deleteStatement}.
	 * @param ctx the parse tree
	 */
	void enterDeleteStatement(JaybirdSqlParser.DeleteStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#deleteStatement}.
	 * @param ctx the parse tree
	 */
	void exitDeleteStatement(JaybirdSqlParser.DeleteStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#updateStatement}.
	 * @param ctx the parse tree
	 */
	void enterUpdateStatement(JaybirdSqlParser.UpdateStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#updateStatement}.
	 * @param ctx the parse tree
	 */
	void exitUpdateStatement(JaybirdSqlParser.UpdateStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#updateOrInsertStatement}.
	 * @param ctx the parse tree
	 */
	void enterUpdateOrInsertStatement(JaybirdSqlParser.UpdateOrInsertStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#updateOrInsertStatement}.
	 * @param ctx the parse tree
	 */
	void exitUpdateOrInsertStatement(JaybirdSqlParser.UpdateOrInsertStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#insertStatement}.
	 * @param ctx the parse tree
	 */
	void enterInsertStatement(JaybirdSqlParser.InsertStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#insertStatement}.
	 * @param ctx the parse tree
	 */
	void exitInsertStatement(JaybirdSqlParser.InsertStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#mergeStatement}.
	 * @param ctx the parse tree
	 */
	void enterMergeStatement(JaybirdSqlParser.MergeStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#mergeStatement}.
	 * @param ctx the parse tree
	 */
	void exitMergeStatement(JaybirdSqlParser.MergeStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#returningClause}.
	 * @param ctx the parse tree
	 */
	void enterReturningClause(JaybirdSqlParser.ReturningClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#returningClause}.
	 * @param ctx the parse tree
	 */
	void exitReturningClause(JaybirdSqlParser.ReturningClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#simpleIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterSimpleIdentifier(JaybirdSqlParser.SimpleIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#simpleIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitSimpleIdentifier(JaybirdSqlParser.SimpleIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#fullIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterFullIdentifier(JaybirdSqlParser.FullIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#fullIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitFullIdentifier(JaybirdSqlParser.FullIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(JaybirdSqlParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(JaybirdSqlParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#returningColumnList}.
	 * @param ctx the parse tree
	 */
	void enterReturningColumnList(JaybirdSqlParser.ReturningColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#returningColumnList}.
	 * @param ctx the parse tree
	 */
	void exitReturningColumnList(JaybirdSqlParser.ReturningColumnListContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#columnName}.
	 * @param ctx the parse tree
	 */
	void enterColumnName(JaybirdSqlParser.ColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#columnName}.
	 * @param ctx the parse tree
	 */
	void exitColumnName(JaybirdSqlParser.ColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(JaybirdSqlParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(JaybirdSqlParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#simpleValue}.
	 * @param ctx the parse tree
	 */
	void enterSimpleValue(JaybirdSqlParser.SimpleValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#simpleValue}.
	 * @param ctx the parse tree
	 */
	void exitSimpleValue(JaybirdSqlParser.SimpleValueContext ctx);
}