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

// Generated from D:/Development/project/Jaybird/jaybird/src/main/org/firebirdsql/jdbc/parser\JaybirdSql.g4 by ANTLR 4.7
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
	 * Enter a parse tree produced by {@link JaybirdSqlParser#assignments}.
	 * @param ctx the parse tree
	 */
	void enterAssignments(JaybirdSqlParser.AssignmentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#assignments}.
	 * @param ctx the parse tree
	 */
	void exitAssignments(JaybirdSqlParser.AssignmentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(JaybirdSqlParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(JaybirdSqlParser.AssignmentContext ctx);
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
	 * Enter a parse tree produced by {@link JaybirdSqlParser#matchingClause}.
	 * @param ctx the parse tree
	 */
	void enterMatchingClause(JaybirdSqlParser.MatchingClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#matchingClause}.
	 * @param ctx the parse tree
	 */
	void exitMatchingClause(JaybirdSqlParser.MatchingClauseContext ctx);
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
	 * Enter a parse tree produced by {@link JaybirdSqlParser#insertColumns}.
	 * @param ctx the parse tree
	 */
	void enterInsertColumns(JaybirdSqlParser.InsertColumnsContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#insertColumns}.
	 * @param ctx the parse tree
	 */
	void exitInsertColumns(JaybirdSqlParser.InsertColumnsContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#insertValues}.
	 * @param ctx the parse tree
	 */
	void enterInsertValues(JaybirdSqlParser.InsertValuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#insertValues}.
	 * @param ctx the parse tree
	 */
	void exitInsertValues(JaybirdSqlParser.InsertValuesContext ctx);
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
	 * Enter a parse tree produced by {@link JaybirdSqlParser#defaultValuesClause}.
	 * @param ctx the parse tree
	 */
	void enterDefaultValuesClause(JaybirdSqlParser.DefaultValuesClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#defaultValuesClause}.
	 * @param ctx the parse tree
	 */
	void exitDefaultValuesClause(JaybirdSqlParser.DefaultValuesClauseContext ctx);
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
	 * Enter a parse tree produced by {@link JaybirdSqlParser#columnList}.
	 * @param ctx the parse tree
	 */
	void enterColumnList(JaybirdSqlParser.ColumnListContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#columnList}.
	 * @param ctx the parse tree
	 */
	void exitColumnList(JaybirdSqlParser.ColumnListContext ctx);
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
	 * Enter a parse tree produced by {@link JaybirdSqlParser#valueList}.
	 * @param ctx the parse tree
	 */
	void enterValueList(JaybirdSqlParser.ValueListContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#valueList}.
	 * @param ctx the parse tree
	 */
	void exitValueList(JaybirdSqlParser.ValueListContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(JaybirdSqlParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(JaybirdSqlParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterParameter(JaybirdSqlParser.ParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitParameter(JaybirdSqlParser.ParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#nullValue}.
	 * @param ctx the parse tree
	 */
	void enterNullValue(JaybirdSqlParser.NullValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#nullValue}.
	 * @param ctx the parse tree
	 */
	void exitNullValue(JaybirdSqlParser.NullValueContext ctx);
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
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#nextValueExpression}.
	 * @param ctx the parse tree
	 */
	void enterNextValueExpression(JaybirdSqlParser.NextValueExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#nextValueExpression}.
	 * @param ctx the parse tree
	 */
	void exitNextValueExpression(JaybirdSqlParser.NextValueExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#castExpression}.
	 * @param ctx the parse tree
	 */
	void enterCastExpression(JaybirdSqlParser.CastExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#castExpression}.
	 * @param ctx the parse tree
	 */
	void exitCastExpression(JaybirdSqlParser.CastExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#dataTypeDescriptor}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeDescriptor(JaybirdSqlParser.DataTypeDescriptorContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#dataTypeDescriptor}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeDescriptor(JaybirdSqlParser.DataTypeDescriptorContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#nonArrayType}.
	 * @param ctx the parse tree
	 */
	void enterNonArrayType(JaybirdSqlParser.NonArrayTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#nonArrayType}.
	 * @param ctx the parse tree
	 */
	void exitNonArrayType(JaybirdSqlParser.NonArrayTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#simpleType}.
	 * @param ctx the parse tree
	 */
	void enterSimpleType(JaybirdSqlParser.SimpleTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#simpleType}.
	 * @param ctx the parse tree
	 */
	void exitSimpleType(JaybirdSqlParser.SimpleTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#charType}.
	 * @param ctx the parse tree
	 */
	void enterCharType(JaybirdSqlParser.CharTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#charType}.
	 * @param ctx the parse tree
	 */
	void exitCharType(JaybirdSqlParser.CharTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#nonCharSetCharType}.
	 * @param ctx the parse tree
	 */
	void enterNonCharSetCharType(JaybirdSqlParser.NonCharSetCharTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#nonCharSetCharType}.
	 * @param ctx the parse tree
	 */
	void exitNonCharSetCharType(JaybirdSqlParser.NonCharSetCharTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#charSetCharType}.
	 * @param ctx the parse tree
	 */
	void enterCharSetCharType(JaybirdSqlParser.CharSetCharTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#charSetCharType}.
	 * @param ctx the parse tree
	 */
	void exitCharSetCharType(JaybirdSqlParser.CharSetCharTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#nonCharType}.
	 * @param ctx the parse tree
	 */
	void enterNonCharType(JaybirdSqlParser.NonCharTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#nonCharType}.
	 * @param ctx the parse tree
	 */
	void exitNonCharType(JaybirdSqlParser.NonCharTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#blobType}.
	 * @param ctx the parse tree
	 */
	void enterBlobType(JaybirdSqlParser.BlobTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#blobType}.
	 * @param ctx the parse tree
	 */
	void exitBlobType(JaybirdSqlParser.BlobTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#blobSubtype}.
	 * @param ctx the parse tree
	 */
	void enterBlobSubtype(JaybirdSqlParser.BlobSubtypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#blobSubtype}.
	 * @param ctx the parse tree
	 */
	void exitBlobSubtype(JaybirdSqlParser.BlobSubtypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#blobSegSize}.
	 * @param ctx the parse tree
	 */
	void enterBlobSegSize(JaybirdSqlParser.BlobSegSizeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#blobSegSize}.
	 * @param ctx the parse tree
	 */
	void exitBlobSegSize(JaybirdSqlParser.BlobSegSizeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#charSetClause}.
	 * @param ctx the parse tree
	 */
	void enterCharSetClause(JaybirdSqlParser.CharSetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#charSetClause}.
	 * @param ctx the parse tree
	 */
	void exitCharSetClause(JaybirdSqlParser.CharSetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#arrayType}.
	 * @param ctx the parse tree
	 */
	void enterArrayType(JaybirdSqlParser.ArrayTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#arrayType}.
	 * @param ctx the parse tree
	 */
	void exitArrayType(JaybirdSqlParser.ArrayTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#arraySpec}.
	 * @param ctx the parse tree
	 */
	void enterArraySpec(JaybirdSqlParser.ArraySpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#arraySpec}.
	 * @param ctx the parse tree
	 */
	void exitArraySpec(JaybirdSqlParser.ArraySpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#arrayRange}.
	 * @param ctx the parse tree
	 */
	void enterArrayRange(JaybirdSqlParser.ArrayRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#arrayRange}.
	 * @param ctx the parse tree
	 */
	void exitArrayRange(JaybirdSqlParser.ArrayRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#arrayElement}.
	 * @param ctx the parse tree
	 */
	void enterArrayElement(JaybirdSqlParser.ArrayElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#arrayElement}.
	 * @param ctx the parse tree
	 */
	void exitArrayElement(JaybirdSqlParser.ArrayElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunction(JaybirdSqlParser.FunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunction(JaybirdSqlParser.FunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#substringFunction}.
	 * @param ctx the parse tree
	 */
	void enterSubstringFunction(JaybirdSqlParser.SubstringFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#substringFunction}.
	 * @param ctx the parse tree
	 */
	void exitSubstringFunction(JaybirdSqlParser.SubstringFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#trimFunction}.
	 * @param ctx the parse tree
	 */
	void enterTrimFunction(JaybirdSqlParser.TrimFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#trimFunction}.
	 * @param ctx the parse tree
	 */
	void exitTrimFunction(JaybirdSqlParser.TrimFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#extractFunction}.
	 * @param ctx the parse tree
	 */
	void enterExtractFunction(JaybirdSqlParser.ExtractFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#extractFunction}.
	 * @param ctx the parse tree
	 */
	void exitExtractFunction(JaybirdSqlParser.ExtractFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#trimSpecification}.
	 * @param ctx the parse tree
	 */
	void enterTrimSpecification(JaybirdSqlParser.TrimSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#trimSpecification}.
	 * @param ctx the parse tree
	 */
	void exitTrimSpecification(JaybirdSqlParser.TrimSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link JaybirdSqlParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void enterSelectClause(JaybirdSqlParser.SelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link JaybirdSqlParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void exitSelectClause(JaybirdSqlParser.SelectClauseContext ctx);
}