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

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JaybirdSqlParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, ALL=12, AND=13, AS=14, AVG=15, BOTH=16, CAST=17, CHARACTER=18, 
		COUNT=19, COLLATE=20, DEFAULT=21, DELETE=22, DISTINCT=23, DB_KEY=24, EXTRACT=25, 
		EXECUTE=26, FOR=27, FROM=28, GEN_ID=29, INSERT=30, INTO=31, LEADING=32, 
		MATCHING=33, MINIMUM=34, MAXIMUM=35, NULL=36, NEXT=37, OR=38, PROCEDURE=39, 
		RETURNING=40, SEGMENT=41, SELECT=42, SET=43, SUBSTRING=44, SUB_TYPE=45, 
		SUM=46, TRIM=47, TRAILING=48, UNKNOWN=49, UPDATE=50, VALUE=51, VALUES=52, 
		KW_BLOB=53, KW_BIGINT=54, KW_BOOLEAN=55, KW_CHAR=56, KW_DATE=57, KW_DECIMAL=58, 
		KW_DOUBLE=59, KW_PRECISION=60, KW_FLOAT=61, KW_INTEGER=62, KW_INT=63, 
		KW_NCHAR=64, KW_NUMERIC=65, KW_NVARCHAR=66, KW_SMALLINT=67, KW_TIME=68, 
		KW_TIMESTAMP=69, KW_VARCHAR=70, KW_SIZE=71, LEFT_PAREN=72, RIGHT_PAREN=73, 
		COMMA=74, INTEGER=75, NUMERIC=76, REAL=77, STRING=78, BINARY_STRING=79, 
		TRUTH_VALUE=80, GENERIC_ID=81, QUOTED_ID=82, SL_COMMENT=83, COMMENT=84, 
		WS=85;
	public static final int
		RULE_statement = 0, RULE_deleteStatement = 1, RULE_updateStatement = 2, 
		RULE_assignments = 3, RULE_assignment = 4, RULE_updateOrInsertStatement = 5, 
		RULE_matchingClause = 6, RULE_insertStatement = 7, RULE_insertColumns = 8, 
		RULE_insertValues = 9, RULE_returningClause = 10, RULE_defaultValuesClause = 11, 
		RULE_simpleIdentifier = 12, RULE_fullIdentifier = 13, RULE_tableName = 14, 
		RULE_columnList = 15, RULE_columnName = 16, RULE_valueList = 17, RULE_value = 18, 
		RULE_parameter = 19, RULE_nullValue = 20, RULE_simpleValue = 21, RULE_nextValueExpression = 22, 
		RULE_castExpression = 23, RULE_dataTypeDescriptor = 24, RULE_nonArrayType = 25, 
		RULE_simpleType = 26, RULE_charType = 27, RULE_nonCharSetCharType = 28, 
		RULE_charSetCharType = 29, RULE_nonCharType = 30, RULE_blobType = 31, 
		RULE_blobSubtype = 32, RULE_blobSegSize = 33, RULE_charSetClause = 34, 
		RULE_arrayType = 35, RULE_arraySpec = 36, RULE_arrayRange = 37, RULE_arrayElement = 38, 
		RULE_function = 39, RULE_substringFunction = 40, RULE_trimFunction = 41, 
		RULE_extractFunction = 42, RULE_trimSpecification = 43, RULE_selectClause = 44;
	public static final String[] ruleNames = {
		"statement", "deleteStatement", "updateStatement", "assignments", "assignment", 
		"updateOrInsertStatement", "matchingClause", "insertStatement", "insertColumns", 
		"insertValues", "returningClause", "defaultValuesClause", "simpleIdentifier", 
		"fullIdentifier", "tableName", "columnList", "columnName", "valueList", 
		"value", "parameter", "nullValue", "simpleValue", "nextValueExpression", 
		"castExpression", "dataTypeDescriptor", "nonArrayType", "simpleType", 
		"charType", "nonCharSetCharType", "charSetCharType", "nonCharType", "blobType", 
		"blobSubtype", "blobSegSize", "charSetClause", "arrayType", "arraySpec", 
		"arrayRange", "arrayElement", "function", "substringFunction", "trimFunction", 
		"extractFunction", "trimSpecification", "selectClause"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'='", "'.'", "'+'", "'-'", "'*'", "'/'", "'||'", "'?'", "'['", 
		"']'", "':'", null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, "'('", "')'", "','"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		"ALL", "AND", "AS", "AVG", "BOTH", "CAST", "CHARACTER", "COUNT", "COLLATE", 
		"DEFAULT", "DELETE", "DISTINCT", "DB_KEY", "EXTRACT", "EXECUTE", "FOR", 
		"FROM", "GEN_ID", "INSERT", "INTO", "LEADING", "MATCHING", "MINIMUM", 
		"MAXIMUM", "NULL", "NEXT", "OR", "PROCEDURE", "RETURNING", "SEGMENT", 
		"SELECT", "SET", "SUBSTRING", "SUB_TYPE", "SUM", "TRIM", "TRAILING", "UNKNOWN", 
		"UPDATE", "VALUE", "VALUES", "KW_BLOB", "KW_BIGINT", "KW_BOOLEAN", "KW_CHAR", 
		"KW_DATE", "KW_DECIMAL", "KW_DOUBLE", "KW_PRECISION", "KW_FLOAT", "KW_INTEGER", 
		"KW_INT", "KW_NCHAR", "KW_NUMERIC", "KW_NVARCHAR", "KW_SMALLINT", "KW_TIME", 
		"KW_TIMESTAMP", "KW_VARCHAR", "KW_SIZE", "LEFT_PAREN", "RIGHT_PAREN", 
		"COMMA", "INTEGER", "NUMERIC", "REAL", "STRING", "BINARY_STRING", "TRUTH_VALUE", 
		"GENERIC_ID", "QUOTED_ID", "SL_COMMENT", "COMMENT", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "JaybirdSql.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


	private boolean _inReturning;
	protected boolean _defaultValues;
	protected JaybirdStatementModel statementModel = new JaybirdStatementModel();

	protected ArrayList _errorMessages = new ArrayList();

	public JaybirdStatementModel getStatementModel() {
	    return statementModel;
	}

	public java.util.Collection getErrorMessages() {
	    return _errorMessages;
	}

	public String getColumn(int index) {
	    return (String)statementModel.getColumns().get(index);
	}

	public String getValue(int index) {
	    return (String)statementModel.getValues().get(index);
	}

	public String getTableName() {
	    return statementModel.getTableName();
	}

	public void emitErrorMessage(String msg) {
	    _errorMessages.add(msg);
	}

	public JaybirdSqlParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class StatementContext extends ParserRuleContext {
		public InsertStatementContext insertStatement() {
			return getRuleContext(InsertStatementContext.class,0);
		}
		public DeleteStatementContext deleteStatement() {
			return getRuleContext(DeleteStatementContext.class,0);
		}
		public UpdateStatementContext updateStatement() {
			return getRuleContext(UpdateStatementContext.class,0);
		}
		public UpdateOrInsertStatementContext updateOrInsertStatement() {
			return getRuleContext(UpdateOrInsertStatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_statement);
		try {
			setState(94);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(90);
				insertStatement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(91);
				deleteStatement();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(92);
				updateStatement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(93);
				updateOrInsertStatement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DeleteStatementContext extends ParserRuleContext {
		public TerminalNode DELETE() { return getToken(JaybirdSqlParser.DELETE, 0); }
		public TerminalNode FROM() { return getToken(JaybirdSqlParser.FROM, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public ReturningClauseContext returningClause() {
			return getRuleContext(ReturningClauseContext.class,0);
		}
		public DeleteStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_deleteStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterDeleteStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitDeleteStatement(this);
		}
	}

	public final DeleteStatementContext deleteStatement() throws RecognitionException {
		DeleteStatementContext _localctx = new DeleteStatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_deleteStatement);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
			match(DELETE);
			setState(97);
			match(FROM);
			setState(98);
			tableName();
			setState(102);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					{
					setState(99);
					matchWildcard();
					}
					} 
				}
				setState(104);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			}
			setState(106);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(105);
				returningClause();
				}
			}


			                statementModel.setStatementType(JaybirdStatementModel.DELETE_TYPE);
			            
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UpdateStatementContext extends ParserRuleContext {
		public TerminalNode UPDATE() { return getToken(JaybirdSqlParser.UPDATE, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public TerminalNode SET() { return getToken(JaybirdSqlParser.SET, 0); }
		public AssignmentsContext assignments() {
			return getRuleContext(AssignmentsContext.class,0);
		}
		public ReturningClauseContext returningClause() {
			return getRuleContext(ReturningClauseContext.class,0);
		}
		public UpdateStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_updateStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterUpdateStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitUpdateStatement(this);
		}
	}

	public final UpdateStatementContext updateStatement() throws RecognitionException {
		UpdateStatementContext _localctx = new UpdateStatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_updateStatement);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(110);
			match(UPDATE);
			setState(111);
			tableName();
			setState(112);
			match(SET);
			setState(113);
			assignments();
			setState(117);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					{
					setState(114);
					matchWildcard();
					}
					} 
				}
				setState(119);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			setState(121);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(120);
				returningClause();
				}
			}


			                statementModel.setStatementType(JaybirdStatementModel.UPDATE_TYPE);
			            
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AssignmentsContext extends ParserRuleContext {
		public List<AssignmentContext> assignment() {
			return getRuleContexts(AssignmentContext.class);
		}
		public AssignmentContext assignment(int i) {
			return getRuleContext(AssignmentContext.class,i);
		}
		public AssignmentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignments; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterAssignments(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitAssignments(this);
		}
	}

	public final AssignmentsContext assignments() throws RecognitionException {
		AssignmentsContext _localctx = new AssignmentsContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_assignments);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
			assignment();
			setState(130);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(126);
					match(COMMA);
					setState(127);
					assignment();
					}
					} 
				}
				setState(132);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AssignmentContext extends ParserRuleContext {
		public ColumnNameContext columnName() {
			return getRuleContext(ColumnNameContext.class,0);
		}
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitAssignment(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_assignment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(133);
			columnName();
			setState(134);
			match(T__0);
			setState(135);
			value(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UpdateOrInsertStatementContext extends ParserRuleContext {
		public TerminalNode UPDATE() { return getToken(JaybirdSqlParser.UPDATE, 0); }
		public TerminalNode OR() { return getToken(JaybirdSqlParser.OR, 0); }
		public TerminalNode INSERT() { return getToken(JaybirdSqlParser.INSERT, 0); }
		public TerminalNode INTO() { return getToken(JaybirdSqlParser.INTO, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public InsertValuesContext insertValues() {
			return getRuleContext(InsertValuesContext.class,0);
		}
		public InsertColumnsContext insertColumns() {
			return getRuleContext(InsertColumnsContext.class,0);
		}
		public MatchingClauseContext matchingClause() {
			return getRuleContext(MatchingClauseContext.class,0);
		}
		public ReturningClauseContext returningClause() {
			return getRuleContext(ReturningClauseContext.class,0);
		}
		public UpdateOrInsertStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_updateOrInsertStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterUpdateOrInsertStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitUpdateOrInsertStatement(this);
		}
	}

	public final UpdateOrInsertStatementContext updateOrInsertStatement() throws RecognitionException {
		UpdateOrInsertStatementContext _localctx = new UpdateOrInsertStatementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_updateOrInsertStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(137);
			match(UPDATE);
			setState(138);
			match(OR);
			setState(139);
			match(INSERT);
			setState(140);
			match(INTO);
			setState(141);
			tableName();
			setState(143);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LEFT_PAREN) {
				{
				setState(142);
				insertColumns();
				}
			}

			setState(145);
			insertValues();
			setState(147);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MATCHING) {
				{
				setState(146);
				matchingClause();
				}
			}

			setState(150);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(149);
				returningClause();
				}
			}


			                statementModel.setStatementType(JaybirdStatementModel.UPDATE_OR_INSERT_TYPE);
			            
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MatchingClauseContext extends ParserRuleContext {
		public TerminalNode MATCHING() { return getToken(JaybirdSqlParser.MATCHING, 0); }
		public ColumnListContext columnList() {
			return getRuleContext(ColumnListContext.class,0);
		}
		public MatchingClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchingClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterMatchingClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitMatchingClause(this);
		}
	}

	public final MatchingClauseContext matchingClause() throws RecognitionException {
		MatchingClauseContext _localctx = new MatchingClauseContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_matchingClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			match(MATCHING);
			setState(155);
			columnList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InsertStatementContext extends ParserRuleContext {
		public TerminalNode INSERT() { return getToken(JaybirdSqlParser.INSERT, 0); }
		public TerminalNode INTO() { return getToken(JaybirdSqlParser.INTO, 0); }
		public TableNameContext tableName() {
			return getRuleContext(TableNameContext.class,0);
		}
		public InsertValuesContext insertValues() {
			return getRuleContext(InsertValuesContext.class,0);
		}
		public SelectClauseContext selectClause() {
			return getRuleContext(SelectClauseContext.class,0);
		}
		public DefaultValuesClauseContext defaultValuesClause() {
			return getRuleContext(DefaultValuesClauseContext.class,0);
		}
		public InsertColumnsContext insertColumns() {
			return getRuleContext(InsertColumnsContext.class,0);
		}
		public ReturningClauseContext returningClause() {
			return getRuleContext(ReturningClauseContext.class,0);
		}
		public InsertStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insertStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterInsertStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitInsertStatement(this);
		}
	}

	public final InsertStatementContext insertStatement() throws RecognitionException {
		InsertStatementContext _localctx = new InsertStatementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_insertStatement);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(157);
			match(INSERT);
			setState(158);
			match(INTO);
			setState(159);
			tableName();
			setState(161);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LEFT_PAREN) {
				{
				setState(160);
				insertColumns();
				}
			}

			setState(172);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUES:
				{
				setState(163);
				insertValues();
				}
				break;
			case SELECT:
				{
				setState(164);
				selectClause();
				setState(168);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
				while ( _alt!=1 && _alt!= ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(165);
						matchWildcard();
						}
						} 
					}
					setState(170);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
				}
				}
				break;
			case DEFAULT:
				{
				setState(171);
				defaultValuesClause();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(175);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(174);
				returningClause();
				}
			}


			                statementModel.setStatementType(JaybirdStatementModel.INSERT_TYPE);
			            
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InsertColumnsContext extends ParserRuleContext {
		public ColumnListContext columnList() {
			return getRuleContext(ColumnListContext.class,0);
		}
		public InsertColumnsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insertColumns; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterInsertColumns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitInsertColumns(this);
		}
	}

	public final InsertColumnsContext insertColumns() throws RecognitionException {
		InsertColumnsContext _localctx = new InsertColumnsContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_insertColumns);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(179);
			match(LEFT_PAREN);
			setState(180);
			columnList();
			setState(181);
			match(RIGHT_PAREN);

			                _inReturning = false;
			            
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InsertValuesContext extends ParserRuleContext {
		public TerminalNode VALUES() { return getToken(JaybirdSqlParser.VALUES, 0); }
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public InsertValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insertValues; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterInsertValues(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitInsertValues(this);
		}
	}

	public final InsertValuesContext insertValues() throws RecognitionException {
		InsertValuesContext _localctx = new InsertValuesContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_insertValues);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(184);
			match(VALUES);
			setState(185);
			match(LEFT_PAREN);
			setState(186);
			valueList();
			setState(187);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReturningClauseContext extends ParserRuleContext {
		public TerminalNode RETURNING() { return getToken(JaybirdSqlParser.RETURNING, 0); }
		public ColumnListContext columnList() {
			return getRuleContext(ColumnListContext.class,0);
		}
		public ReturningClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_returningClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterReturningClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitReturningClause(this);
		}
	}

	public final ReturningClauseContext returningClause() throws RecognitionException {
		ReturningClauseContext _localctx = new ReturningClauseContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_returningClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
			match(RETURNING);
			_inReturning = true;
			setState(191);
			columnList();
			_inReturning = true;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefaultValuesClauseContext extends ParserRuleContext {
		public TerminalNode DEFAULT() { return getToken(JaybirdSqlParser.DEFAULT, 0); }
		public TerminalNode VALUES() { return getToken(JaybirdSqlParser.VALUES, 0); }
		public DefaultValuesClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defaultValuesClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterDefaultValuesClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitDefaultValuesClause(this);
		}
	}

	public final DefaultValuesClauseContext defaultValuesClause() throws RecognitionException {
		DefaultValuesClauseContext _localctx = new DefaultValuesClauseContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_defaultValuesClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(194);
			match(DEFAULT);
			setState(195);
			match(VALUES);

			                statementModel.setDefaultValues(true);
			            
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SimpleIdentifierContext extends ParserRuleContext {
		public TerminalNode GENERIC_ID() { return getToken(JaybirdSqlParser.GENERIC_ID, 0); }
		public TerminalNode QUOTED_ID() { return getToken(JaybirdSqlParser.QUOTED_ID, 0); }
		public SimpleIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterSimpleIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitSimpleIdentifier(this);
		}
	}

	public final SimpleIdentifierContext simpleIdentifier() throws RecognitionException {
		SimpleIdentifierContext _localctx = new SimpleIdentifierContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_simpleIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(198);
			_la = _input.LA(1);
			if ( !(_la==GENERIC_ID || _la==QUOTED_ID) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FullIdentifierContext extends ParserRuleContext {
		public List<SimpleIdentifierContext> simpleIdentifier() {
			return getRuleContexts(SimpleIdentifierContext.class);
		}
		public SimpleIdentifierContext simpleIdentifier(int i) {
			return getRuleContext(SimpleIdentifierContext.class,i);
		}
		public FullIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fullIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterFullIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitFullIdentifier(this);
		}
	}

	public final FullIdentifierContext fullIdentifier() throws RecognitionException {
		FullIdentifierContext _localctx = new FullIdentifierContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_fullIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			simpleIdentifier();
			setState(201);
			match(T__1);
			setState(202);
			simpleIdentifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TableNameContext extends ParserRuleContext {
		public SimpleIdentifierContext t;
		public SimpleIdentifierContext simpleIdentifier() {
			return getRuleContext(SimpleIdentifierContext.class,0);
		}
		public TableNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterTableName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitTableName(this);
		}
	}

	public final TableNameContext tableName() throws RecognitionException {
		TableNameContext _localctx = new TableNameContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_tableName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(204);
			((TableNameContext)_localctx).t = simpleIdentifier();

			                statementModel.setTableName((((TableNameContext)_localctx).t!=null?_input.getText(((TableNameContext)_localctx).t.start,((TableNameContext)_localctx).t.stop):null));
			            
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColumnListContext extends ParserRuleContext {
		public List<ColumnNameContext> columnName() {
			return getRuleContexts(ColumnNameContext.class);
		}
		public ColumnNameContext columnName(int i) {
			return getRuleContext(ColumnNameContext.class,i);
		}
		public ColumnListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterColumnList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitColumnList(this);
		}
	}

	public final ColumnListContext columnList() throws RecognitionException {
		ColumnListContext _localctx = new ColumnListContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_columnList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(207);
			columnName();
			setState(212);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(208);
				match(COMMA);
				setState(209);
				columnName();
				}
				}
				setState(214);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColumnNameContext extends ParserRuleContext {
		public SimpleIdentifierContext si;
		public FullIdentifierContext fi;
		public SimpleIdentifierContext simpleIdentifier() {
			return getRuleContext(SimpleIdentifierContext.class,0);
		}
		public FullIdentifierContext fullIdentifier() {
			return getRuleContext(FullIdentifierContext.class,0);
		}
		public ColumnNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterColumnName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitColumnName(this);
		}
	}

	public final ColumnNameContext columnName() throws RecognitionException {
		ColumnNameContext _localctx = new ColumnNameContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_columnName);
		try {
			setState(221);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(215);
				((ColumnNameContext)_localctx).si = simpleIdentifier();

				                if (_inReturning)
				                    statementModel.addReturningColumn((((ColumnNameContext)_localctx).si!=null?_input.getText(((ColumnNameContext)_localctx).si.start,((ColumnNameContext)_localctx).si.stop):null));
				                else
				                    statementModel.addColumn((((ColumnNameContext)_localctx).si!=null?_input.getText(((ColumnNameContext)_localctx).si.start,((ColumnNameContext)_localctx).si.stop):null));
				            
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(218);
				((ColumnNameContext)_localctx).fi = fullIdentifier();

				                if (_inReturning)
				                    statementModel.addReturningColumn((((ColumnNameContext)_localctx).fi!=null?_input.getText(((ColumnNameContext)_localctx).fi.start,((ColumnNameContext)_localctx).fi.stop):null));
				                else
				                    statementModel.addColumn((((ColumnNameContext)_localctx).fi!=null?_input.getText(((ColumnNameContext)_localctx).fi.start,((ColumnNameContext)_localctx).fi.stop):null));
				            
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueListContext extends ParserRuleContext {
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public ValueListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valueList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterValueList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitValueList(this);
		}
	}

	public final ValueListContext valueList() throws RecognitionException {
		ValueListContext _localctx = new ValueListContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_valueList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(223);
			value(0);
			setState(228);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(224);
				match(COMMA);
				setState(225);
				value(0);
				}
				}
				setState(230);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public SimpleValueContext simpleValue() {
			return getRuleContext(SimpleValueContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(JaybirdSqlParser.LEFT_PAREN, 0); }
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(JaybirdSqlParser.RIGHT_PAREN, 0); }
		public ParameterContext parameter() {
			return getRuleContext(ParameterContext.class,0);
		}
		public NullValueContext nullValue() {
			return getRuleContext(NullValueContext.class,0);
		}
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public NextValueExpressionContext nextValueExpression() {
			return getRuleContext(NextValueExpressionContext.class,0);
		}
		public CastExpressionContext castExpression() {
			return getRuleContext(CastExpressionContext.class,0);
		}
		public ArrayElementContext arrayElement() {
			return getRuleContext(ArrayElementContext.class,0);
		}
		public TerminalNode DB_KEY() { return getToken(JaybirdSqlParser.DB_KEY, 0); }
		public SimpleIdentifierContext simpleIdentifier() {
			return getRuleContext(SimpleIdentifierContext.class,0);
		}
		public TerminalNode COLLATE() { return getToken(JaybirdSqlParser.COLLATE, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitValue(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		return value(0);
	}

	private ValueContext value(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ValueContext _localctx = new ValueContext(_ctx, _parentState);
		ValueContext _prevctx = _localctx;
		int _startState = 36;
		enterRecursionRule(_localctx, 36, RULE_value, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(253);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				{
				setState(232);
				simpleValue();
				}
				break;
			case 2:
				{
				setState(233);
				match(T__2);
				setState(234);
				simpleValue();
				}
				break;
			case 3:
				{
				setState(235);
				match(T__3);
				setState(236);
				simpleValue();
				}
				break;
			case 4:
				{
				setState(237);
				match(LEFT_PAREN);
				setState(238);
				value(0);
				setState(239);
				match(RIGHT_PAREN);
				}
				break;
			case 5:
				{
				setState(241);
				parameter();
				}
				break;
			case 6:
				{
				setState(242);
				nullValue();
				}
				break;
			case 7:
				{
				setState(243);
				function();
				}
				break;
			case 8:
				{
				setState(244);
				nextValueExpression();
				}
				break;
			case 9:
				{
				setState(245);
				castExpression();
				}
				break;
			case 10:
				{
				setState(246);
				arrayElement();
				}
				break;
			case 11:
				{
				setState(247);
				match(DB_KEY);
				}
				break;
			case 12:
				{
				setState(248);
				simpleIdentifier();
				setState(249);
				match(T__1);
				setState(250);
				match(DB_KEY);
				}
				break;
			case 13:
				{
				setState(252);
				simpleIdentifier();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(275);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!= ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(273);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
					case 1:
						{
						_localctx = new ValueContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_value);
						setState(255);
						if (!(precpred(_ctx, 18))) throw new FailedPredicateException(this, "precpred(_ctx, 18)");
						setState(256);
						match(T__2);
						setState(257);
						value(19);
						}
						break;
					case 2:
						{
						_localctx = new ValueContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_value);
						setState(258);
						if (!(precpred(_ctx, 17))) throw new FailedPredicateException(this, "precpred(_ctx, 17)");
						setState(259);
						match(T__3);
						setState(260);
						value(18);
						}
						break;
					case 3:
						{
						_localctx = new ValueContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_value);
						setState(261);
						if (!(precpred(_ctx, 16))) throw new FailedPredicateException(this, "precpred(_ctx, 16)");
						setState(262);
						match(T__4);
						setState(263);
						value(17);
						}
						break;
					case 4:
						{
						_localctx = new ValueContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_value);
						setState(264);
						if (!(precpred(_ctx, 15))) throw new FailedPredicateException(this, "precpred(_ctx, 15)");
						setState(265);
						match(T__5);
						setState(266);
						value(16);
						}
						break;
					case 5:
						{
						_localctx = new ValueContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_value);
						setState(267);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(268);
						match(T__6);
						setState(269);
						value(15);
						}
						break;
					case 6:
						{
						_localctx = new ValueContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_value);
						setState(270);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(271);
						match(COLLATE);
						setState(272);
						simpleIdentifier();
						}
						break;
					}
					} 
				}
				setState(277);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class ParameterContext extends ParserRuleContext {
		public ParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitParameter(this);
		}
	}

	public final ParameterContext parameter() throws RecognitionException {
		ParameterContext _localctx = new ParameterContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_parameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(278);
			match(T__7);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NullValueContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(JaybirdSqlParser.NULL, 0); }
		public TerminalNode UNKNOWN() { return getToken(JaybirdSqlParser.UNKNOWN, 0); }
		public NullValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nullValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterNullValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitNullValue(this);
		}
	}

	public final NullValueContext nullValue() throws RecognitionException {
		NullValueContext _localctx = new NullValueContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_nullValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(280);
			_la = _input.LA(1);
			if ( !(_la==NULL || _la==UNKNOWN) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SimpleValueContext extends ParserRuleContext {
		public TerminalNode TRUTH_VALUE() { return getToken(JaybirdSqlParser.TRUTH_VALUE, 0); }
		public TerminalNode STRING() { return getToken(JaybirdSqlParser.STRING, 0); }
		public TerminalNode BINARY_STRING() { return getToken(JaybirdSqlParser.BINARY_STRING, 0); }
		public TerminalNode INTEGER() { return getToken(JaybirdSqlParser.INTEGER, 0); }
		public TerminalNode NUMERIC() { return getToken(JaybirdSqlParser.NUMERIC, 0); }
		public TerminalNode REAL() { return getToken(JaybirdSqlParser.REAL, 0); }
		public SimpleValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterSimpleValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitSimpleValue(this);
		}
	}

	public final SimpleValueContext simpleValue() throws RecognitionException {
		SimpleValueContext _localctx = new SimpleValueContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_simpleValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(282);
			_la = _input.LA(1);
			if ( !(((((_la - 75)) & ~0x3f) == 0 && ((1L << (_la - 75)) & ((1L << (INTEGER - 75)) | (1L << (NUMERIC - 75)) | (1L << (REAL - 75)) | (1L << (STRING - 75)) | (1L << (BINARY_STRING - 75)) | (1L << (TRUTH_VALUE - 75)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NextValueExpressionContext extends ParserRuleContext {
		public TerminalNode NEXT() { return getToken(JaybirdSqlParser.NEXT, 0); }
		public TerminalNode VALUE() { return getToken(JaybirdSqlParser.VALUE, 0); }
		public TerminalNode FOR() { return getToken(JaybirdSqlParser.FOR, 0); }
		public SimpleIdentifierContext simpleIdentifier() {
			return getRuleContext(SimpleIdentifierContext.class,0);
		}
		public TerminalNode GEN_ID() { return getToken(JaybirdSqlParser.GEN_ID, 0); }
		public TerminalNode INTEGER() { return getToken(JaybirdSqlParser.INTEGER, 0); }
		public NextValueExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nextValueExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterNextValueExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitNextValueExpression(this);
		}
	}

	public final NextValueExpressionContext nextValueExpression() throws RecognitionException {
		NextValueExpressionContext _localctx = new NextValueExpressionContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_nextValueExpression);
		try {
			setState(295);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NEXT:
				enterOuterAlt(_localctx, 1);
				{
				setState(284);
				match(NEXT);
				setState(285);
				match(VALUE);
				setState(286);
				match(FOR);
				setState(287);
				simpleIdentifier();
				}
				break;
			case GEN_ID:
				enterOuterAlt(_localctx, 2);
				{
				setState(288);
				match(GEN_ID);
				setState(289);
				match(LEFT_PAREN);
				setState(290);
				simpleIdentifier();
				setState(291);
				match(COMMA);
				setState(292);
				match(INTEGER);
				setState(293);
				match(RIGHT_PAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CastExpressionContext extends ParserRuleContext {
		public TerminalNode CAST() { return getToken(JaybirdSqlParser.CAST, 0); }
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode AS() { return getToken(JaybirdSqlParser.AS, 0); }
		public DataTypeDescriptorContext dataTypeDescriptor() {
			return getRuleContext(DataTypeDescriptorContext.class,0);
		}
		public CastExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_castExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterCastExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitCastExpression(this);
		}
	}

	public final CastExpressionContext castExpression() throws RecognitionException {
		CastExpressionContext _localctx = new CastExpressionContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_castExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(297);
			match(CAST);
			setState(298);
			match(LEFT_PAREN);
			setState(299);
			value(0);
			setState(300);
			match(AS);
			setState(301);
			dataTypeDescriptor();
			setState(302);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DataTypeDescriptorContext extends ParserRuleContext {
		public NonArrayTypeContext nonArrayType() {
			return getRuleContext(NonArrayTypeContext.class,0);
		}
		public ArrayTypeContext arrayType() {
			return getRuleContext(ArrayTypeContext.class,0);
		}
		public DataTypeDescriptorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataTypeDescriptor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterDataTypeDescriptor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitDataTypeDescriptor(this);
		}
	}

	public final DataTypeDescriptorContext dataTypeDescriptor() throws RecognitionException {
		DataTypeDescriptorContext _localctx = new DataTypeDescriptorContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_dataTypeDescriptor);
		try {
			setState(306);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(304);
				nonArrayType();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(305);
				arrayType();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NonArrayTypeContext extends ParserRuleContext {
		public SimpleTypeContext simpleType() {
			return getRuleContext(SimpleTypeContext.class,0);
		}
		public BlobTypeContext blobType() {
			return getRuleContext(BlobTypeContext.class,0);
		}
		public NonArrayTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonArrayType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterNonArrayType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitNonArrayType(this);
		}
	}

	public final NonArrayTypeContext nonArrayType() throws RecognitionException {
		NonArrayTypeContext _localctx = new NonArrayTypeContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_nonArrayType);
		try {
			setState(310);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_BIGINT:
			case KW_BOOLEAN:
			case KW_CHAR:
			case KW_DATE:
			case KW_DECIMAL:
			case KW_DOUBLE:
			case KW_FLOAT:
			case KW_INTEGER:
			case KW_INT:
			case KW_NCHAR:
			case KW_NUMERIC:
			case KW_NVARCHAR:
			case KW_SMALLINT:
			case KW_TIME:
			case KW_TIMESTAMP:
			case KW_VARCHAR:
				enterOuterAlt(_localctx, 1);
				{
				setState(308);
				simpleType();
				}
				break;
			case KW_BLOB:
				enterOuterAlt(_localctx, 2);
				{
				setState(309);
				blobType();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SimpleTypeContext extends ParserRuleContext {
		public NonCharTypeContext nonCharType() {
			return getRuleContext(NonCharTypeContext.class,0);
		}
		public CharTypeContext charType() {
			return getRuleContext(CharTypeContext.class,0);
		}
		public SimpleTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterSimpleType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitSimpleType(this);
		}
	}

	public final SimpleTypeContext simpleType() throws RecognitionException {
		SimpleTypeContext _localctx = new SimpleTypeContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_simpleType);
		try {
			setState(314);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_BIGINT:
			case KW_BOOLEAN:
			case KW_DATE:
			case KW_DECIMAL:
			case KW_DOUBLE:
			case KW_FLOAT:
			case KW_INTEGER:
			case KW_INT:
			case KW_NUMERIC:
			case KW_SMALLINT:
			case KW_TIME:
			case KW_TIMESTAMP:
				enterOuterAlt(_localctx, 1);
				{
				setState(312);
				nonCharType();
				}
				break;
			case KW_CHAR:
			case KW_NCHAR:
			case KW_NVARCHAR:
			case KW_VARCHAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(313);
				charType();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CharTypeContext extends ParserRuleContext {
		public NonCharSetCharTypeContext nonCharSetCharType() {
			return getRuleContext(NonCharSetCharTypeContext.class,0);
		}
		public CharSetCharTypeContext charSetCharType() {
			return getRuleContext(CharSetCharTypeContext.class,0);
		}
		public CharTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_charType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterCharType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitCharType(this);
		}
	}

	public final CharTypeContext charType() throws RecognitionException {
		CharTypeContext _localctx = new CharTypeContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_charType);
		try {
			setState(318);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(316);
				nonCharSetCharType();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(317);
				charSetCharType();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NonCharSetCharTypeContext extends ParserRuleContext {
		public TerminalNode KW_CHAR() { return getToken(JaybirdSqlParser.KW_CHAR, 0); }
		public TerminalNode KW_NCHAR() { return getToken(JaybirdSqlParser.KW_NCHAR, 0); }
		public TerminalNode INTEGER() { return getToken(JaybirdSqlParser.INTEGER, 0); }
		public TerminalNode KW_VARCHAR() { return getToken(JaybirdSqlParser.KW_VARCHAR, 0); }
		public TerminalNode KW_NVARCHAR() { return getToken(JaybirdSqlParser.KW_NVARCHAR, 0); }
		public NonCharSetCharTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonCharSetCharType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterNonCharSetCharType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitNonCharSetCharType(this);
		}
	}

	public final NonCharSetCharTypeContext nonCharSetCharType() throws RecognitionException {
		NonCharSetCharTypeContext _localctx = new NonCharSetCharTypeContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_nonCharSetCharType);
		int _la;
		try {
			setState(330);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_CHAR:
			case KW_NCHAR:
				enterOuterAlt(_localctx, 1);
				{
				setState(320);
				_la = _input.LA(1);
				if ( !(_la==KW_CHAR || _la==KW_NCHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(324);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(321);
					match(LEFT_PAREN);
					setState(322);
					match(INTEGER);
					setState(323);
					match(RIGHT_PAREN);
					}
				}

				}
				break;
			case KW_NVARCHAR:
			case KW_VARCHAR:
				enterOuterAlt(_localctx, 2);
				{
				setState(326);
				_la = _input.LA(1);
				if ( !(_la==KW_NVARCHAR || _la==KW_VARCHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(327);
				match(LEFT_PAREN);
				setState(328);
				match(INTEGER);
				setState(329);
				match(RIGHT_PAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CharSetCharTypeContext extends ParserRuleContext {
		public NonCharSetCharTypeContext nonCharSetCharType() {
			return getRuleContext(NonCharSetCharTypeContext.class,0);
		}
		public CharSetClauseContext charSetClause() {
			return getRuleContext(CharSetClauseContext.class,0);
		}
		public CharSetCharTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_charSetCharType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterCharSetCharType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitCharSetCharType(this);
		}
	}

	public final CharSetCharTypeContext charSetCharType() throws RecognitionException {
		CharSetCharTypeContext _localctx = new CharSetCharTypeContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_charSetCharType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(332);
			nonCharSetCharType();
			setState(333);
			charSetClause();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NonCharTypeContext extends ParserRuleContext {
		public TerminalNode KW_BIGINT() { return getToken(JaybirdSqlParser.KW_BIGINT, 0); }
		public TerminalNode KW_DATE() { return getToken(JaybirdSqlParser.KW_DATE, 0); }
		public TerminalNode KW_DECIMAL() { return getToken(JaybirdSqlParser.KW_DECIMAL, 0); }
		public List<TerminalNode> INTEGER() { return getTokens(JaybirdSqlParser.INTEGER); }
		public TerminalNode INTEGER(int i) {
			return getToken(JaybirdSqlParser.INTEGER, i);
		}
		public TerminalNode KW_DOUBLE() { return getToken(JaybirdSqlParser.KW_DOUBLE, 0); }
		public TerminalNode KW_PRECISION() { return getToken(JaybirdSqlParser.KW_PRECISION, 0); }
		public TerminalNode KW_FLOAT() { return getToken(JaybirdSqlParser.KW_FLOAT, 0); }
		public TerminalNode KW_INTEGER() { return getToken(JaybirdSqlParser.KW_INTEGER, 0); }
		public TerminalNode KW_INT() { return getToken(JaybirdSqlParser.KW_INT, 0); }
		public TerminalNode KW_NUMERIC() { return getToken(JaybirdSqlParser.KW_NUMERIC, 0); }
		public TerminalNode KW_SMALLINT() { return getToken(JaybirdSqlParser.KW_SMALLINT, 0); }
		public TerminalNode KW_TIME() { return getToken(JaybirdSqlParser.KW_TIME, 0); }
		public TerminalNode KW_TIMESTAMP() { return getToken(JaybirdSqlParser.KW_TIMESTAMP, 0); }
		public TerminalNode KW_BOOLEAN() { return getToken(JaybirdSqlParser.KW_BOOLEAN, 0); }
		public NonCharTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonCharType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterNonCharType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitNonCharType(this);
		}
	}

	public final NonCharTypeContext nonCharType() throws RecognitionException {
		NonCharTypeContext _localctx = new NonCharTypeContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_nonCharType);
		int _la;
		try {
			setState(366);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_BIGINT:
				enterOuterAlt(_localctx, 1);
				{
				setState(335);
				match(KW_BIGINT);
				}
				break;
			case KW_DATE:
				enterOuterAlt(_localctx, 2);
				{
				setState(336);
				match(KW_DATE);
				}
				break;
			case KW_DECIMAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(337);
				match(KW_DECIMAL);
				setState(345);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(338);
					match(LEFT_PAREN);
					setState(339);
					match(INTEGER);
					setState(342);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(340);
						match(COMMA);
						setState(341);
						match(INTEGER);
						}
					}

					setState(344);
					match(RIGHT_PAREN);
					}
				}

				}
				break;
			case KW_DOUBLE:
				enterOuterAlt(_localctx, 4);
				{
				setState(347);
				match(KW_DOUBLE);
				setState(348);
				match(KW_PRECISION);
				}
				break;
			case KW_FLOAT:
				enterOuterAlt(_localctx, 5);
				{
				setState(349);
				match(KW_FLOAT);
				}
				break;
			case KW_INTEGER:
				enterOuterAlt(_localctx, 6);
				{
				setState(350);
				match(KW_INTEGER);
				}
				break;
			case KW_INT:
				enterOuterAlt(_localctx, 7);
				{
				setState(351);
				match(KW_INT);
				}
				break;
			case KW_NUMERIC:
				enterOuterAlt(_localctx, 8);
				{
				setState(352);
				match(KW_NUMERIC);
				setState(360);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(353);
					match(LEFT_PAREN);
					setState(354);
					match(INTEGER);
					setState(357);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(355);
						match(COMMA);
						setState(356);
						match(INTEGER);
						}
					}

					setState(359);
					match(RIGHT_PAREN);
					}
				}

				}
				break;
			case KW_SMALLINT:
				enterOuterAlt(_localctx, 9);
				{
				setState(362);
				match(KW_SMALLINT);
				}
				break;
			case KW_TIME:
				enterOuterAlt(_localctx, 10);
				{
				setState(363);
				match(KW_TIME);
				}
				break;
			case KW_TIMESTAMP:
				enterOuterAlt(_localctx, 11);
				{
				setState(364);
				match(KW_TIMESTAMP);
				}
				break;
			case KW_BOOLEAN:
				enterOuterAlt(_localctx, 12);
				{
				setState(365);
				match(KW_BOOLEAN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BlobTypeContext extends ParserRuleContext {
		public TerminalNode KW_BLOB() { return getToken(JaybirdSqlParser.KW_BLOB, 0); }
		public BlobSubtypeContext blobSubtype() {
			return getRuleContext(BlobSubtypeContext.class,0);
		}
		public BlobSegSizeContext blobSegSize() {
			return getRuleContext(BlobSegSizeContext.class,0);
		}
		public CharSetClauseContext charSetClause() {
			return getRuleContext(CharSetClauseContext.class,0);
		}
		public List<TerminalNode> INTEGER() { return getTokens(JaybirdSqlParser.INTEGER); }
		public TerminalNode INTEGER(int i) {
			return getToken(JaybirdSqlParser.INTEGER, i);
		}
		public BlobTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blobType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterBlobType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitBlobType(this);
		}
	}

	public final BlobTypeContext blobType() throws RecognitionException {
		BlobTypeContext _localctx = new BlobTypeContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_blobType);
		int _la;
		try {
			setState(386);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(368);
				match(KW_BLOB);
				setState(370);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SUB_TYPE) {
					{
					setState(369);
					blobSubtype();
					}
				}

				setState(373);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SEGMENT) {
					{
					setState(372);
					blobSegSize();
					}
				}

				setState(376);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CHARACTER) {
					{
					setState(375);
					charSetClause();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(378);
				match(KW_BLOB);
				setState(379);
				match(LEFT_PAREN);
				setState(380);
				match(INTEGER);
				setState(383);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(381);
					match(COMMA);
					setState(382);
					match(INTEGER);
					}
				}

				setState(385);
				match(RIGHT_PAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BlobSubtypeContext extends ParserRuleContext {
		public TerminalNode SUB_TYPE() { return getToken(JaybirdSqlParser.SUB_TYPE, 0); }
		public TerminalNode INTEGER() { return getToken(JaybirdSqlParser.INTEGER, 0); }
		public TerminalNode GENERIC_ID() { return getToken(JaybirdSqlParser.GENERIC_ID, 0); }
		public BlobSubtypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blobSubtype; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterBlobSubtype(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitBlobSubtype(this);
		}
	}

	public final BlobSubtypeContext blobSubtype() throws RecognitionException {
		BlobSubtypeContext _localctx = new BlobSubtypeContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_blobSubtype);
		try {
			setState(392);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(388);
				match(SUB_TYPE);
				setState(389);
				match(INTEGER);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(390);
				match(SUB_TYPE);
				setState(391);
				match(GENERIC_ID);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BlobSegSizeContext extends ParserRuleContext {
		public TerminalNode SEGMENT() { return getToken(JaybirdSqlParser.SEGMENT, 0); }
		public TerminalNode KW_SIZE() { return getToken(JaybirdSqlParser.KW_SIZE, 0); }
		public TerminalNode INTEGER() { return getToken(JaybirdSqlParser.INTEGER, 0); }
		public BlobSegSizeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blobSegSize; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterBlobSegSize(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitBlobSegSize(this);
		}
	}

	public final BlobSegSizeContext blobSegSize() throws RecognitionException {
		BlobSegSizeContext _localctx = new BlobSegSizeContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_blobSegSize);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(394);
			match(SEGMENT);
			setState(395);
			match(KW_SIZE);
			setState(396);
			match(INTEGER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CharSetClauseContext extends ParserRuleContext {
		public TerminalNode CHARACTER() { return getToken(JaybirdSqlParser.CHARACTER, 0); }
		public TerminalNode SET() { return getToken(JaybirdSqlParser.SET, 0); }
		public TerminalNode GENERIC_ID() { return getToken(JaybirdSqlParser.GENERIC_ID, 0); }
		public CharSetClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_charSetClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterCharSetClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitCharSetClause(this);
		}
	}

	public final CharSetClauseContext charSetClause() throws RecognitionException {
		CharSetClauseContext _localctx = new CharSetClauseContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_charSetClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(398);
			match(CHARACTER);
			setState(399);
			match(SET);
			setState(400);
			match(GENERIC_ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayTypeContext extends ParserRuleContext {
		public NonCharSetCharTypeContext nonCharSetCharType() {
			return getRuleContext(NonCharSetCharTypeContext.class,0);
		}
		public ArraySpecContext arraySpec() {
			return getRuleContext(ArraySpecContext.class,0);
		}
		public CharSetClauseContext charSetClause() {
			return getRuleContext(CharSetClauseContext.class,0);
		}
		public NonCharTypeContext nonCharType() {
			return getRuleContext(NonCharTypeContext.class,0);
		}
		public ArrayTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterArrayType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitArrayType(this);
		}
	}

	public final ArrayTypeContext arrayType() throws RecognitionException {
		ArrayTypeContext _localctx = new ArrayTypeContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_arrayType);
		int _la;
		try {
			setState(414);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_CHAR:
			case KW_NCHAR:
			case KW_NVARCHAR:
			case KW_VARCHAR:
				enterOuterAlt(_localctx, 1);
				{
				setState(402);
				nonCharSetCharType();
				setState(403);
				match(T__8);
				setState(404);
				arraySpec();
				setState(405);
				match(T__9);
				setState(407);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CHARACTER) {
					{
					setState(406);
					charSetClause();
					}
				}

				}
				break;
			case KW_BIGINT:
			case KW_BOOLEAN:
			case KW_DATE:
			case KW_DECIMAL:
			case KW_DOUBLE:
			case KW_FLOAT:
			case KW_INTEGER:
			case KW_INT:
			case KW_NUMERIC:
			case KW_SMALLINT:
			case KW_TIME:
			case KW_TIMESTAMP:
				enterOuterAlt(_localctx, 2);
				{
				setState(409);
				nonCharType();
				setState(410);
				match(T__8);
				setState(411);
				arraySpec();
				setState(412);
				match(T__9);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArraySpecContext extends ParserRuleContext {
		public List<ArrayRangeContext> arrayRange() {
			return getRuleContexts(ArrayRangeContext.class);
		}
		public ArrayRangeContext arrayRange(int i) {
			return getRuleContext(ArrayRangeContext.class,i);
		}
		public ArraySpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arraySpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterArraySpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitArraySpec(this);
		}
	}

	public final ArraySpecContext arraySpec() throws RecognitionException {
		ArraySpecContext _localctx = new ArraySpecContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_arraySpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(416);
			arrayRange();
			setState(419);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(417);
				match(COMMA);
				setState(418);
				arrayRange();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayRangeContext extends ParserRuleContext {
		public List<TerminalNode> INTEGER() { return getTokens(JaybirdSqlParser.INTEGER); }
		public TerminalNode INTEGER(int i) {
			return getToken(JaybirdSqlParser.INTEGER, i);
		}
		public ArrayRangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayRange; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterArrayRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitArrayRange(this);
		}
	}

	public final ArrayRangeContext arrayRange() throws RecognitionException {
		ArrayRangeContext _localctx = new ArrayRangeContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_arrayRange);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(421);
			match(INTEGER);
			{
			setState(422);
			match(T__10);
			setState(423);
			match(INTEGER);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayElementContext extends ParserRuleContext {
		public SimpleIdentifierContext simpleIdentifier() {
			return getRuleContext(SimpleIdentifierContext.class,0);
		}
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public ArrayElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayElement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterArrayElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitArrayElement(this);
		}
	}

	public final ArrayElementContext arrayElement() throws RecognitionException {
		ArrayElementContext _localctx = new ArrayElementContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_arrayElement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(425);
			simpleIdentifier();
			setState(426);
			match(T__8);
			setState(427);
			valueList();
			setState(428);
			match(T__9);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FunctionContext extends ParserRuleContext {
		public SimpleIdentifierContext simpleIdentifier() {
			return getRuleContext(SimpleIdentifierContext.class,0);
		}
		public ValueListContext valueList() {
			return getRuleContext(ValueListContext.class,0);
		}
		public SubstringFunctionContext substringFunction() {
			return getRuleContext(SubstringFunctionContext.class,0);
		}
		public TrimFunctionContext trimFunction() {
			return getRuleContext(TrimFunctionContext.class,0);
		}
		public ExtractFunctionContext extractFunction() {
			return getRuleContext(ExtractFunctionContext.class,0);
		}
		public TerminalNode SUM() { return getToken(JaybirdSqlParser.SUM, 0); }
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode ALL() { return getToken(JaybirdSqlParser.ALL, 0); }
		public TerminalNode DISTINCT() { return getToken(JaybirdSqlParser.DISTINCT, 0); }
		public TerminalNode COUNT() { return getToken(JaybirdSqlParser.COUNT, 0); }
		public TerminalNode AVG() { return getToken(JaybirdSqlParser.AVG, 0); }
		public TerminalNode MINIMUM() { return getToken(JaybirdSqlParser.MINIMUM, 0); }
		public TerminalNode MAXIMUM() { return getToken(JaybirdSqlParser.MAXIMUM, 0); }
		public FunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitFunction(this);
		}
	}

	public final FunctionContext function() throws RecognitionException {
		FunctionContext _localctx = new FunctionContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_function);
		int _la;
		try {
			setState(482);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(430);
				simpleIdentifier();
				setState(431);
				match(LEFT_PAREN);
				setState(432);
				valueList();
				setState(433);
				match(RIGHT_PAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(435);
				simpleIdentifier();
				setState(436);
				match(LEFT_PAREN);
				setState(437);
				match(RIGHT_PAREN);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(439);
				substringFunction();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(440);
				trimFunction();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(441);
				extractFunction();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(442);
				match(SUM);
				setState(443);
				match(LEFT_PAREN);
				setState(445);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL || _la==DISTINCT) {
					{
					setState(444);
					_la = _input.LA(1);
					if ( !(_la==ALL || _la==DISTINCT) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(447);
				value(0);
				setState(448);
				match(RIGHT_PAREN);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(450);
				match(COUNT);
				setState(451);
				match(LEFT_PAREN);
				setState(453);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL || _la==DISTINCT) {
					{
					setState(452);
					_la = _input.LA(1);
					if ( !(_la==ALL || _la==DISTINCT) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(455);
				value(0);
				setState(456);
				match(RIGHT_PAREN);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(458);
				match(AVG);
				setState(459);
				match(LEFT_PAREN);
				setState(461);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL || _la==DISTINCT) {
					{
					setState(460);
					_la = _input.LA(1);
					if ( !(_la==ALL || _la==DISTINCT) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(463);
				value(0);
				setState(464);
				match(RIGHT_PAREN);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(466);
				match(MINIMUM);
				setState(467);
				match(LEFT_PAREN);
				setState(469);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL || _la==DISTINCT) {
					{
					setState(468);
					_la = _input.LA(1);
					if ( !(_la==ALL || _la==DISTINCT) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(471);
				value(0);
				setState(472);
				match(RIGHT_PAREN);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(474);
				match(MAXIMUM);
				setState(475);
				match(LEFT_PAREN);
				setState(477);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL || _la==DISTINCT) {
					{
					setState(476);
					_la = _input.LA(1);
					if ( !(_la==ALL || _la==DISTINCT) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(479);
				value(0);
				setState(480);
				match(RIGHT_PAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SubstringFunctionContext extends ParserRuleContext {
		public TerminalNode SUBSTRING() { return getToken(JaybirdSqlParser.SUBSTRING, 0); }
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public TerminalNode FROM() { return getToken(JaybirdSqlParser.FROM, 0); }
		public TerminalNode FOR() { return getToken(JaybirdSqlParser.FOR, 0); }
		public TerminalNode INTEGER() { return getToken(JaybirdSqlParser.INTEGER, 0); }
		public SubstringFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_substringFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterSubstringFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitSubstringFunction(this);
		}
	}

	public final SubstringFunctionContext substringFunction() throws RecognitionException {
		SubstringFunctionContext _localctx = new SubstringFunctionContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_substringFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(484);
			match(SUBSTRING);
			setState(485);
			match(LEFT_PAREN);
			setState(486);
			value(0);
			setState(487);
			match(FROM);
			setState(488);
			value(0);
			setState(491);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FOR) {
				{
				setState(489);
				match(FOR);
				setState(490);
				match(INTEGER);
				}
			}

			setState(493);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TrimFunctionContext extends ParserRuleContext {
		public TerminalNode TRIM() { return getToken(JaybirdSqlParser.TRIM, 0); }
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public TrimSpecificationContext trimSpecification() {
			return getRuleContext(TrimSpecificationContext.class,0);
		}
		public TerminalNode FROM() { return getToken(JaybirdSqlParser.FROM, 0); }
		public TrimFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trimFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterTrimFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitTrimFunction(this);
		}
	}

	public final TrimFunctionContext trimFunction() throws RecognitionException {
		TrimFunctionContext _localctx = new TrimFunctionContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_trimFunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(495);
			match(TRIM);
			setState(496);
			match(LEFT_PAREN);
			setState(498);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BOTH) | (1L << LEADING) | (1L << TRAILING))) != 0)) {
				{
				setState(497);
				trimSpecification();
				}
			}

			setState(500);
			value(0);
			setState(503);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FROM) {
				{
				setState(501);
				match(FROM);
				setState(502);
				value(0);
				}
			}

			setState(505);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtractFunctionContext extends ParserRuleContext {
		public TerminalNode EXTRACT() { return getToken(JaybirdSqlParser.EXTRACT, 0); }
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public TerminalNode FROM() { return getToken(JaybirdSqlParser.FROM, 0); }
		public ExtractFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extractFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterExtractFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitExtractFunction(this);
		}
	}

	public final ExtractFunctionContext extractFunction() throws RecognitionException {
		ExtractFunctionContext _localctx = new ExtractFunctionContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_extractFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(507);
			match(EXTRACT);
			setState(508);
			match(LEFT_PAREN);
			setState(509);
			value(0);
			setState(510);
			match(FROM);
			setState(511);
			value(0);
			setState(512);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TrimSpecificationContext extends ParserRuleContext {
		public TerminalNode BOTH() { return getToken(JaybirdSqlParser.BOTH, 0); }
		public TerminalNode TRAILING() { return getToken(JaybirdSqlParser.TRAILING, 0); }
		public TerminalNode LEADING() { return getToken(JaybirdSqlParser.LEADING, 0); }
		public TrimSpecificationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trimSpecification; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterTrimSpecification(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitTrimSpecification(this);
		}
	}

	public final TrimSpecificationContext trimSpecification() throws RecognitionException {
		TrimSpecificationContext _localctx = new TrimSpecificationContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_trimSpecification);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(514);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BOTH) | (1L << LEADING) | (1L << TRAILING))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectClauseContext extends ParserRuleContext {
		public TerminalNode SELECT() { return getToken(JaybirdSqlParser.SELECT, 0); }
		public SelectClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).enterSelectClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JaybirdSqlListener ) ((JaybirdSqlListener)listener).exitSelectClause(this);
		}
	}

	public final SelectClauseContext selectClause() throws RecognitionException {
		SelectClauseContext _localctx = new SelectClauseContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_selectClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(516);
			match(SELECT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 18:
			return value_sempred((ValueContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean value_sempred(ValueContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 18);
		case 1:
			return precpred(_ctx, 17);
		case 2:
			return precpred(_ctx, 16);
		case 3:
			return precpred(_ctx, 15);
		case 4:
			return precpred(_ctx, 14);
		case 5:
			return precpred(_ctx, 10);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3W\u0209\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\3\2\3\2\3\2\3\2\5\2a\n\2\3\3\3\3\3\3\3\3\7\3g\n\3\f\3"+
		"\16\3j\13\3\3\3\5\3m\n\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\7\4v\n\4\f\4\16\4"+
		"y\13\4\3\4\5\4|\n\4\3\4\3\4\3\5\3\5\3\5\7\5\u0083\n\5\f\5\16\5\u0086\13"+
		"\5\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\5\7\u0092\n\7\3\7\3\7\5\7\u0096"+
		"\n\7\3\7\5\7\u0099\n\7\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\3\t\5\t\u00a4\n"+
		"\t\3\t\3\t\3\t\7\t\u00a9\n\t\f\t\16\t\u00ac\13\t\3\t\5\t\u00af\n\t\3\t"+
		"\5\t\u00b2\n\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3"+
		"\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\16\3\16\3\17\3\17\3\17\3\17\3\20"+
		"\3\20\3\20\3\21\3\21\3\21\7\21\u00d5\n\21\f\21\16\21\u00d8\13\21\3\22"+
		"\3\22\3\22\3\22\3\22\3\22\5\22\u00e0\n\22\3\23\3\23\3\23\7\23\u00e5\n"+
		"\23\f\23\16\23\u00e8\13\23\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\5\24"+
		"\u0100\n\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\3\24\3\24\3\24\3\24\3\24\7\24\u0114\n\24\f\24\16\24\u0117\13\24"+
		"\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30"+
		"\3\30\3\30\3\30\5\30\u012a\n\30\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\32"+
		"\3\32\5\32\u0135\n\32\3\33\3\33\5\33\u0139\n\33\3\34\3\34\5\34\u013d\n"+
		"\34\3\35\3\35\5\35\u0141\n\35\3\36\3\36\3\36\3\36\5\36\u0147\n\36\3\36"+
		"\3\36\3\36\3\36\5\36\u014d\n\36\3\37\3\37\3\37\3 \3 \3 \3 \3 \3 \3 \5"+
		" \u0159\n \3 \5 \u015c\n \3 \3 \3 \3 \3 \3 \3 \3 \3 \3 \5 \u0168\n \3"+
		" \5 \u016b\n \3 \3 \3 \3 \5 \u0171\n \3!\3!\5!\u0175\n!\3!\5!\u0178\n"+
		"!\3!\5!\u017b\n!\3!\3!\3!\3!\3!\5!\u0182\n!\3!\5!\u0185\n!\3\"\3\"\3\""+
		"\3\"\5\"\u018b\n\"\3#\3#\3#\3#\3$\3$\3$\3$\3%\3%\3%\3%\3%\5%\u019a\n%"+
		"\3%\3%\3%\3%\3%\5%\u01a1\n%\3&\3&\3&\5&\u01a6\n&\3\'\3\'\3\'\3\'\3(\3"+
		"(\3(\3(\3(\3)\3)\3)\3)\3)\3)\3)\3)\3)\3)\3)\3)\3)\3)\3)\5)\u01c0\n)\3"+
		")\3)\3)\3)\3)\3)\5)\u01c8\n)\3)\3)\3)\3)\3)\3)\5)\u01d0\n)\3)\3)\3)\3"+
		")\3)\3)\5)\u01d8\n)\3)\3)\3)\3)\3)\3)\5)\u01e0\n)\3)\3)\3)\5)\u01e5\n"+
		")\3*\3*\3*\3*\3*\3*\3*\5*\u01ee\n*\3*\3*\3+\3+\3+\5+\u01f5\n+\3+\3+\3"+
		"+\5+\u01fa\n+\3+\3+\3,\3,\3,\3,\3,\3,\3,\3-\3-\3.\3.\3.\5hw\u00aa\3&/"+
		"\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFH"+
		"JLNPRTVXZ\2\t\3\2ST\4\2&&\63\63\3\2MR\4\2::BB\4\2DDHH\4\2\16\16\31\31"+
		"\5\2\22\22\"\"\62\62\2\u0230\2`\3\2\2\2\4b\3\2\2\2\6p\3\2\2\2\b\177\3"+
		"\2\2\2\n\u0087\3\2\2\2\f\u008b\3\2\2\2\16\u009c\3\2\2\2\20\u009f\3\2\2"+
		"\2\22\u00b5\3\2\2\2\24\u00ba\3\2\2\2\26\u00bf\3\2\2\2\30\u00c4\3\2\2\2"+
		"\32\u00c8\3\2\2\2\34\u00ca\3\2\2\2\36\u00ce\3\2\2\2 \u00d1\3\2\2\2\"\u00df"+
		"\3\2\2\2$\u00e1\3\2\2\2&\u00ff\3\2\2\2(\u0118\3\2\2\2*\u011a\3\2\2\2,"+
		"\u011c\3\2\2\2.\u0129\3\2\2\2\60\u012b\3\2\2\2\62\u0134\3\2\2\2\64\u0138"+
		"\3\2\2\2\66\u013c\3\2\2\28\u0140\3\2\2\2:\u014c\3\2\2\2<\u014e\3\2\2\2"+
		">\u0170\3\2\2\2@\u0184\3\2\2\2B\u018a\3\2\2\2D\u018c\3\2\2\2F\u0190\3"+
		"\2\2\2H\u01a0\3\2\2\2J\u01a2\3\2\2\2L\u01a7\3\2\2\2N\u01ab\3\2\2\2P\u01e4"+
		"\3\2\2\2R\u01e6\3\2\2\2T\u01f1\3\2\2\2V\u01fd\3\2\2\2X\u0204\3\2\2\2Z"+
		"\u0206\3\2\2\2\\a\5\20\t\2]a\5\4\3\2^a\5\6\4\2_a\5\f\7\2`\\\3\2\2\2`]"+
		"\3\2\2\2`^\3\2\2\2`_\3\2\2\2a\3\3\2\2\2bc\7\30\2\2cd\7\36\2\2dh\5\36\20"+
		"\2eg\13\2\2\2fe\3\2\2\2gj\3\2\2\2hi\3\2\2\2hf\3\2\2\2il\3\2\2\2jh\3\2"+
		"\2\2km\5\26\f\2lk\3\2\2\2lm\3\2\2\2mn\3\2\2\2no\b\3\1\2o\5\3\2\2\2pq\7"+
		"\64\2\2qr\5\36\20\2rs\7-\2\2sw\5\b\5\2tv\13\2\2\2ut\3\2\2\2vy\3\2\2\2"+
		"wx\3\2\2\2wu\3\2\2\2x{\3\2\2\2yw\3\2\2\2z|\5\26\f\2{z\3\2\2\2{|\3\2\2"+
		"\2|}\3\2\2\2}~\b\4\1\2~\7\3\2\2\2\177\u0084\5\n\6\2\u0080\u0081\7L\2\2"+
		"\u0081\u0083\5\n\6\2\u0082\u0080\3\2\2\2\u0083\u0086\3\2\2\2\u0084\u0082"+
		"\3\2\2\2\u0084\u0085\3\2\2\2\u0085\t\3\2\2\2\u0086\u0084\3\2\2\2\u0087"+
		"\u0088\5\"\22\2\u0088\u0089\7\3\2\2\u0089\u008a\5&\24\2\u008a\13\3\2\2"+
		"\2\u008b\u008c\7\64\2\2\u008c\u008d\7(\2\2\u008d\u008e\7 \2\2\u008e\u008f"+
		"\7!\2\2\u008f\u0091\5\36\20\2\u0090\u0092\5\22\n\2\u0091\u0090\3\2\2\2"+
		"\u0091\u0092\3\2\2\2\u0092\u0093\3\2\2\2\u0093\u0095\5\24\13\2\u0094\u0096"+
		"\5\16\b\2\u0095\u0094\3\2\2\2\u0095\u0096\3\2\2\2\u0096\u0098\3\2\2\2"+
		"\u0097\u0099\5\26\f\2\u0098\u0097\3\2\2\2\u0098\u0099\3\2\2\2\u0099\u009a"+
		"\3\2\2\2\u009a\u009b\b\7\1\2\u009b\r\3\2\2\2\u009c\u009d\7#\2\2\u009d"+
		"\u009e\5 \21\2\u009e\17\3\2\2\2\u009f\u00a0\7 \2\2\u00a0\u00a1\7!\2\2"+
		"\u00a1\u00a3\5\36\20\2\u00a2\u00a4\5\22\n\2\u00a3\u00a2\3\2\2\2\u00a3"+
		"\u00a4\3\2\2\2\u00a4\u00ae\3\2\2\2\u00a5\u00af\5\24\13\2\u00a6\u00aa\5"+
		"Z.\2\u00a7\u00a9\13\2\2\2\u00a8\u00a7\3\2\2\2\u00a9\u00ac\3\2\2\2\u00aa"+
		"\u00ab\3\2\2\2\u00aa\u00a8\3\2\2\2\u00ab\u00af\3\2\2\2\u00ac\u00aa\3\2"+
		"\2\2\u00ad\u00af\5\30\r\2\u00ae\u00a5\3\2\2\2\u00ae\u00a6\3\2\2\2\u00ae"+
		"\u00ad\3\2\2\2\u00af\u00b1\3\2\2\2\u00b0\u00b2\5\26\f\2\u00b1\u00b0\3"+
		"\2\2\2\u00b1\u00b2\3\2\2\2\u00b2\u00b3\3\2\2\2\u00b3\u00b4\b\t\1\2\u00b4"+
		"\21\3\2\2\2\u00b5\u00b6\7J\2\2\u00b6\u00b7\5 \21\2\u00b7\u00b8\7K\2\2"+
		"\u00b8\u00b9\b\n\1\2\u00b9\23\3\2\2\2\u00ba\u00bb\7\66\2\2\u00bb\u00bc"+
		"\7J\2\2\u00bc\u00bd\5$\23\2\u00bd\u00be\7K\2\2\u00be\25\3\2\2\2\u00bf"+
		"\u00c0\7*\2\2\u00c0\u00c1\b\f\1\2\u00c1\u00c2\5 \21\2\u00c2\u00c3\b\f"+
		"\1\2\u00c3\27\3\2\2\2\u00c4\u00c5\7\27\2\2\u00c5\u00c6\7\66\2\2\u00c6"+
		"\u00c7\b\r\1\2\u00c7\31\3\2\2\2\u00c8\u00c9\t\2\2\2\u00c9\33\3\2\2\2\u00ca"+
		"\u00cb\5\32\16\2\u00cb\u00cc\7\4\2\2\u00cc\u00cd\5\32\16\2\u00cd\35\3"+
		"\2\2\2\u00ce\u00cf\5\32\16\2\u00cf\u00d0\b\20\1\2\u00d0\37\3\2\2\2\u00d1"+
		"\u00d6\5\"\22\2\u00d2\u00d3\7L\2\2\u00d3\u00d5\5\"\22\2\u00d4\u00d2\3"+
		"\2\2\2\u00d5\u00d8\3\2\2\2\u00d6\u00d4\3\2\2\2\u00d6\u00d7\3\2\2\2\u00d7"+
		"!\3\2\2\2\u00d8\u00d6\3\2\2\2\u00d9\u00da\5\32\16\2\u00da\u00db\b\22\1"+
		"\2\u00db\u00e0\3\2\2\2\u00dc\u00dd\5\34\17\2\u00dd\u00de\b\22\1\2\u00de"+
		"\u00e0\3\2\2\2\u00df\u00d9\3\2\2\2\u00df\u00dc\3\2\2\2\u00e0#\3\2\2\2"+
		"\u00e1\u00e6\5&\24\2\u00e2\u00e3\7L\2\2\u00e3\u00e5\5&\24\2\u00e4\u00e2"+
		"\3\2\2\2\u00e5\u00e8\3\2\2\2\u00e6\u00e4\3\2\2\2\u00e6\u00e7\3\2\2\2\u00e7"+
		"%\3\2\2\2\u00e8\u00e6\3\2\2\2\u00e9\u00ea\b\24\1\2\u00ea\u0100\5,\27\2"+
		"\u00eb\u00ec\7\5\2\2\u00ec\u0100\5,\27\2\u00ed\u00ee\7\6\2\2\u00ee\u0100"+
		"\5,\27\2\u00ef\u00f0\7J\2\2\u00f0\u00f1\5&\24\2\u00f1\u00f2\7K\2\2\u00f2"+
		"\u0100\3\2\2\2\u00f3\u0100\5(\25\2\u00f4\u0100\5*\26\2\u00f5\u0100\5P"+
		")\2\u00f6\u0100\5.\30\2\u00f7\u0100\5\60\31\2\u00f8\u0100\5N(\2\u00f9"+
		"\u0100\7\32\2\2\u00fa\u00fb\5\32\16\2\u00fb\u00fc\7\4\2\2\u00fc\u00fd"+
		"\7\32\2\2\u00fd\u0100\3\2\2\2\u00fe\u0100\5\32\16\2\u00ff\u00e9\3\2\2"+
		"\2\u00ff\u00eb\3\2\2\2\u00ff\u00ed\3\2\2\2\u00ff\u00ef\3\2\2\2\u00ff\u00f3"+
		"\3\2\2\2\u00ff\u00f4\3\2\2\2\u00ff\u00f5\3\2\2\2\u00ff\u00f6\3\2\2\2\u00ff"+
		"\u00f7\3\2\2\2\u00ff\u00f8\3\2\2\2\u00ff\u00f9\3\2\2\2\u00ff\u00fa\3\2"+
		"\2\2\u00ff\u00fe\3\2\2\2\u0100\u0115\3\2\2\2\u0101\u0102\f\24\2\2\u0102"+
		"\u0103\7\5\2\2\u0103\u0114\5&\24\25\u0104\u0105\f\23\2\2\u0105\u0106\7"+
		"\6\2\2\u0106\u0114\5&\24\24\u0107\u0108\f\22\2\2\u0108\u0109\7\7\2\2\u0109"+
		"\u0114\5&\24\23\u010a\u010b\f\21\2\2\u010b\u010c\7\b\2\2\u010c\u0114\5"+
		"&\24\22\u010d\u010e\f\20\2\2\u010e\u010f\7\t\2\2\u010f\u0114\5&\24\21"+
		"\u0110\u0111\f\f\2\2\u0111\u0112\7\26\2\2\u0112\u0114\5\32\16\2\u0113"+
		"\u0101\3\2\2\2\u0113\u0104\3\2\2\2\u0113\u0107\3\2\2\2\u0113\u010a\3\2"+
		"\2\2\u0113\u010d\3\2\2\2\u0113\u0110\3\2\2\2\u0114\u0117\3\2\2\2\u0115"+
		"\u0113\3\2\2\2\u0115\u0116\3\2\2\2\u0116\'\3\2\2\2\u0117\u0115\3\2\2\2"+
		"\u0118\u0119\7\n\2\2\u0119)\3\2\2\2\u011a\u011b\t\3\2\2\u011b+\3\2\2\2"+
		"\u011c\u011d\t\4\2\2\u011d-\3\2\2\2\u011e\u011f\7\'\2\2\u011f\u0120\7"+
		"\65\2\2\u0120\u0121\7\35\2\2\u0121\u012a\5\32\16\2\u0122\u0123\7\37\2"+
		"\2\u0123\u0124\7J\2\2\u0124\u0125\5\32\16\2\u0125\u0126\7L\2\2\u0126\u0127"+
		"\7M\2\2\u0127\u0128\7K\2\2\u0128\u012a\3\2\2\2\u0129\u011e\3\2\2\2\u0129"+
		"\u0122\3\2\2\2\u012a/\3\2\2\2\u012b\u012c\7\23\2\2\u012c\u012d\7J\2\2"+
		"\u012d\u012e\5&\24\2\u012e\u012f\7\20\2\2\u012f\u0130\5\62\32\2\u0130"+
		"\u0131\7K\2\2\u0131\61\3\2\2\2\u0132\u0135\5\64\33\2\u0133\u0135\5H%\2"+
		"\u0134\u0132\3\2\2\2\u0134\u0133\3\2\2\2\u0135\63\3\2\2\2\u0136\u0139"+
		"\5\66\34\2\u0137\u0139\5@!\2\u0138\u0136\3\2\2\2\u0138\u0137\3\2\2\2\u0139"+
		"\65\3\2\2\2\u013a\u013d\5> \2\u013b\u013d\58\35\2\u013c\u013a\3\2\2\2"+
		"\u013c\u013b\3\2\2\2\u013d\67\3\2\2\2\u013e\u0141\5:\36\2\u013f\u0141"+
		"\5<\37\2\u0140\u013e\3\2\2\2\u0140\u013f\3\2\2\2\u01419\3\2\2\2\u0142"+
		"\u0146\t\5\2\2\u0143\u0144\7J\2\2\u0144\u0145\7M\2\2\u0145\u0147\7K\2"+
		"\2\u0146\u0143\3\2\2\2\u0146\u0147\3\2\2\2\u0147\u014d\3\2\2\2\u0148\u0149"+
		"\t\6\2\2\u0149\u014a\7J\2\2\u014a\u014b\7M\2\2\u014b\u014d\7K\2\2\u014c"+
		"\u0142\3\2\2\2\u014c\u0148\3\2\2\2\u014d;\3\2\2\2\u014e\u014f\5:\36\2"+
		"\u014f\u0150\5F$\2\u0150=\3\2\2\2\u0151\u0171\78\2\2\u0152\u0171\7;\2"+
		"\2\u0153\u015b\7<\2\2\u0154\u0155\7J\2\2\u0155\u0158\7M\2\2\u0156\u0157"+
		"\7L\2\2\u0157\u0159\7M\2\2\u0158\u0156\3\2\2\2\u0158\u0159\3\2\2\2\u0159"+
		"\u015a\3\2\2\2\u015a\u015c\7K\2\2\u015b\u0154\3\2\2\2\u015b\u015c\3\2"+
		"\2\2\u015c\u0171\3\2\2\2\u015d\u015e\7=\2\2\u015e\u0171\7>\2\2\u015f\u0171"+
		"\7?\2\2\u0160\u0171\7@\2\2\u0161\u0171\7A\2\2\u0162\u016a\7C\2\2\u0163"+
		"\u0164\7J\2\2\u0164\u0167\7M\2\2\u0165\u0166\7L\2\2\u0166\u0168\7M\2\2"+
		"\u0167\u0165\3\2\2\2\u0167\u0168\3\2\2\2\u0168\u0169\3\2\2\2\u0169\u016b"+
		"\7K\2\2\u016a\u0163\3\2\2\2\u016a\u016b\3\2\2\2\u016b\u0171\3\2\2\2\u016c"+
		"\u0171\7E\2\2\u016d\u0171\7F\2\2\u016e\u0171\7G\2\2\u016f\u0171\79\2\2"+
		"\u0170\u0151\3\2\2\2\u0170\u0152\3\2\2\2\u0170\u0153\3\2\2\2\u0170\u015d"+
		"\3\2\2\2\u0170\u015f\3\2\2\2\u0170\u0160\3\2\2\2\u0170\u0161\3\2\2\2\u0170"+
		"\u0162\3\2\2\2\u0170\u016c\3\2\2\2\u0170\u016d\3\2\2\2\u0170\u016e\3\2"+
		"\2\2\u0170\u016f\3\2\2\2\u0171?\3\2\2\2\u0172\u0174\7\67\2\2\u0173\u0175"+
		"\5B\"\2\u0174\u0173\3\2\2\2\u0174\u0175\3\2\2\2\u0175\u0177\3\2\2\2\u0176"+
		"\u0178\5D#\2\u0177\u0176\3\2\2\2\u0177\u0178\3\2\2\2\u0178\u017a\3\2\2"+
		"\2\u0179\u017b\5F$\2\u017a\u0179\3\2\2\2\u017a\u017b\3\2\2\2\u017b\u0185"+
		"\3\2\2\2\u017c\u017d\7\67\2\2\u017d\u017e\7J\2\2\u017e\u0181\7M\2\2\u017f"+
		"\u0180\7L\2\2\u0180\u0182\7M\2\2\u0181\u017f\3\2\2\2\u0181\u0182\3\2\2"+
		"\2\u0182\u0183\3\2\2\2\u0183\u0185\7K\2\2\u0184\u0172\3\2\2\2\u0184\u017c"+
		"\3\2\2\2\u0185A\3\2\2\2\u0186\u0187\7/\2\2\u0187\u018b\7M\2\2\u0188\u0189"+
		"\7/\2\2\u0189\u018b\7S\2\2\u018a\u0186\3\2\2\2\u018a\u0188\3\2\2\2\u018b"+
		"C\3\2\2\2\u018c\u018d\7+\2\2\u018d\u018e\7I\2\2\u018e\u018f\7M\2\2\u018f"+
		"E\3\2\2\2\u0190\u0191\7\24\2\2\u0191\u0192\7-\2\2\u0192\u0193\7S\2\2\u0193"+
		"G\3\2\2\2\u0194\u0195\5:\36\2\u0195\u0196\7\13\2\2\u0196\u0197\5J&\2\u0197"+
		"\u0199\7\f\2\2\u0198\u019a\5F$\2\u0199\u0198\3\2\2\2\u0199\u019a\3\2\2"+
		"\2\u019a\u01a1\3\2\2\2\u019b\u019c\5> \2\u019c\u019d\7\13\2\2\u019d\u019e"+
		"\5J&\2\u019e\u019f\7\f\2\2\u019f\u01a1\3\2\2\2\u01a0\u0194\3\2\2\2\u01a0"+
		"\u019b\3\2\2\2\u01a1I\3\2\2\2\u01a2\u01a5\5L\'\2\u01a3\u01a4\7L\2\2\u01a4"+
		"\u01a6\5L\'\2\u01a5\u01a3\3\2\2\2\u01a5\u01a6\3\2\2\2\u01a6K\3\2\2\2\u01a7"+
		"\u01a8\7M\2\2\u01a8\u01a9\7\r\2\2\u01a9\u01aa\7M\2\2\u01aaM\3\2\2\2\u01ab"+
		"\u01ac\5\32\16\2\u01ac\u01ad\7\13\2\2\u01ad\u01ae\5$\23\2\u01ae\u01af"+
		"\7\f\2\2\u01afO\3\2\2\2\u01b0\u01b1\5\32\16\2\u01b1\u01b2\7J\2\2\u01b2"+
		"\u01b3\5$\23\2\u01b3\u01b4\7K\2\2\u01b4\u01e5\3\2\2\2\u01b5\u01b6\5\32"+
		"\16\2\u01b6\u01b7\7J\2\2\u01b7\u01b8\7K\2\2\u01b8\u01e5\3\2\2\2\u01b9"+
		"\u01e5\5R*\2\u01ba\u01e5\5T+\2\u01bb\u01e5\5V,\2\u01bc\u01bd\7\60\2\2"+
		"\u01bd\u01bf\7J\2\2\u01be\u01c0\t\7\2\2\u01bf\u01be\3\2\2\2\u01bf\u01c0"+
		"\3\2\2\2\u01c0\u01c1\3\2\2\2\u01c1\u01c2\5&\24\2\u01c2\u01c3\7K\2\2\u01c3"+
		"\u01e5\3\2\2\2\u01c4\u01c5\7\25\2\2\u01c5\u01c7\7J\2\2\u01c6\u01c8\t\7"+
		"\2\2\u01c7\u01c6\3\2\2\2\u01c7\u01c8\3\2\2\2\u01c8\u01c9\3\2\2\2\u01c9"+
		"\u01ca\5&\24\2\u01ca\u01cb\7K\2\2\u01cb\u01e5\3\2\2\2\u01cc\u01cd\7\21"+
		"\2\2\u01cd\u01cf\7J\2\2\u01ce\u01d0\t\7\2\2\u01cf\u01ce\3\2\2\2\u01cf"+
		"\u01d0\3\2\2\2\u01d0\u01d1\3\2\2\2\u01d1\u01d2\5&\24\2\u01d2\u01d3\7K"+
		"\2\2\u01d3\u01e5\3\2\2\2\u01d4\u01d5\7$\2\2\u01d5\u01d7\7J\2\2\u01d6\u01d8"+
		"\t\7\2\2\u01d7\u01d6\3\2\2\2\u01d7\u01d8\3\2\2\2\u01d8\u01d9\3\2\2\2\u01d9"+
		"\u01da\5&\24\2\u01da\u01db\7K\2\2\u01db\u01e5\3\2\2\2\u01dc\u01dd\7%\2"+
		"\2\u01dd\u01df\7J\2\2\u01de\u01e0\t\7\2\2\u01df\u01de\3\2\2\2\u01df\u01e0"+
		"\3\2\2\2\u01e0\u01e1\3\2\2\2\u01e1\u01e2\5&\24\2\u01e2\u01e3\7K\2\2\u01e3"+
		"\u01e5\3\2\2\2\u01e4\u01b0\3\2\2\2\u01e4\u01b5\3\2\2\2\u01e4\u01b9\3\2"+
		"\2\2\u01e4\u01ba\3\2\2\2\u01e4\u01bb\3\2\2\2\u01e4\u01bc\3\2\2\2\u01e4"+
		"\u01c4\3\2\2\2\u01e4\u01cc\3\2\2\2\u01e4\u01d4\3\2\2\2\u01e4\u01dc\3\2"+
		"\2\2\u01e5Q\3\2\2\2\u01e6\u01e7\7.\2\2\u01e7\u01e8\7J\2\2\u01e8\u01e9"+
		"\5&\24\2\u01e9\u01ea\7\36\2\2\u01ea\u01ed\5&\24\2\u01eb\u01ec\7\35\2\2"+
		"\u01ec\u01ee\7M\2\2\u01ed\u01eb\3\2\2\2\u01ed\u01ee\3\2\2\2\u01ee\u01ef"+
		"\3\2\2\2\u01ef\u01f0\7K\2\2\u01f0S\3\2\2\2\u01f1\u01f2\7\61\2\2\u01f2"+
		"\u01f4\7J\2\2\u01f3\u01f5\5X-\2\u01f4\u01f3\3\2\2\2\u01f4\u01f5\3\2\2"+
		"\2\u01f5\u01f6\3\2\2\2\u01f6\u01f9\5&\24\2\u01f7\u01f8\7\36\2\2\u01f8"+
		"\u01fa\5&\24\2\u01f9\u01f7\3\2\2\2\u01f9\u01fa\3\2\2\2\u01fa\u01fb\3\2"+
		"\2\2\u01fb\u01fc\7K\2\2\u01fcU\3\2\2\2\u01fd\u01fe\7\33\2\2\u01fe\u01ff"+
		"\7J\2\2\u01ff\u0200\5&\24\2\u0200\u0201\7\36\2\2\u0201\u0202\5&\24\2\u0202"+
		"\u0203\7K\2\2\u0203W\3\2\2\2\u0204\u0205\t\b\2\2\u0205Y\3\2\2\2\u0206"+
		"\u0207\7,\2\2\u0207[\3\2\2\2\63`hlw{\u0084\u0091\u0095\u0098\u00a3\u00aa"+
		"\u00ae\u00b1\u00d6\u00df\u00e6\u00ff\u0113\u0115\u0129\u0134\u0138\u013c"+
		"\u0140\u0146\u014c\u0158\u015b\u0167\u016a\u0170\u0174\u0177\u017a\u0181"+
		"\u0184\u018a\u0199\u01a0\u01a5\u01bf\u01c7\u01cf\u01d7\u01df\u01e4\u01ed"+
		"\u01f4\u01f9";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}