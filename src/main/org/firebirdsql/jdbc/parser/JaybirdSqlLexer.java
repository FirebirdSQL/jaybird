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
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JaybirdSqlLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, AS=3, DELETE=4, FROM=5, INSERT=6, INTO=7, MERGE=8, OR=9, 
		RETURNING=10, SET=11, UPDATE=12, LEFT_PAREN=13, RIGHT_PAREN=14, COMMA=15, 
		STRING=16, BINARY_STRING=17, Q_STRING=18, GENERIC_ID=19, QUOTED_ID=20, 
		SL_COMMENT=21, COMMENT=22, WS=23, OTHER=24;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "AS", "DELETE", "FROM", "INSERT", "INTO", "MERGE", "OR", 
		"RETURNING", "SET", "UPDATE", "LEFT_PAREN", "RIGHT_PAREN", "COMMA", "STRING", 
		"BINARY_STRING", "QS_OTHER_CH", "Q_STRING", "QUOTED_TEXT", "GENERIC_ID", 
		"QUOTED_ID", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", 
		"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", 
		"HEXIT", "DIGIT", "ID_LETTER", "ID_NUMBER_OR_SYMBOL", "ID_QUOTED_UNICODE", 
		"NEWLINE", "SL_COMMENT", "COMMENT", "WS", "OTHER"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "';'", "'.'", null, null, null, null, null, null, null, null, null, 
		null, "'('", "')'", "','"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, "AS", "DELETE", "FROM", "INSERT", "INTO", "MERGE", "OR", 
		"RETURNING", "SET", "UPDATE", "LEFT_PAREN", "RIGHT_PAREN", "COMMA", "STRING", 
		"BINARY_STRING", "Q_STRING", "GENERIC_ID", "QUOTED_ID", "SL_COMMENT", 
		"COMMENT", "WS", "OTHER"
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


	boolean ahead(String text) {
	    for (int i = 0; i < text.length(); i++) {
	        if (_input.LA(i + 1) != text.charAt(i)) {
	            return false;
	        }
	    }
	    return true;
	}


	public JaybirdSqlLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "JaybirdSql.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 19:
			return QUOTED_TEXT_sempred((RuleContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean QUOTED_TEXT_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return !ahead(getText().charAt(2) + "'");
		case 1:
			return ahead(getText().charAt(2) + "'");
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\32\u0187\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64"+
		"\t\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\3\2\3\2\3"+
		"\3\3\3\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3"+
		"\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3"+
		"\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21"+
		"\3\21\3\21\3\21\7\21\u00bf\n\21\f\21\16\21\u00c2\13\21\3\21\3\21\3\22"+
		"\3\22\3\22\3\22\3\22\7\22\u00cb\n\22\f\22\16\22\u00ce\13\22\3\22\3\22"+
		"\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\25\3\25\7\25\u00db\n\25\f\25\16"+
		"\25\u00de\13\25\3\25\3\25\3\25\7\25\u00e3\n\25\f\25\16\25\u00e6\13\25"+
		"\3\25\3\25\3\25\7\25\u00eb\n\25\f\25\16\25\u00ee\13\25\3\25\3\25\3\25"+
		"\7\25\u00f3\n\25\f\25\16\25\u00f6\13\25\3\25\3\25\3\25\3\25\7\25\u00fc"+
		"\n\25\f\25\16\25\u00ff\13\25\3\25\3\25\3\25\5\25\u0104\n\25\3\26\3\26"+
		"\3\26\7\26\u0109\n\26\f\26\16\26\u010c\13\26\3\27\3\27\6\27\u0110\n\27"+
		"\r\27\16\27\u0111\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3"+
		"\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3"+
		"$\3%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3,\3,\3-\3-\3.\3.\3/\3/"+
		"\3\60\3\60\3\61\3\61\3\62\3\62\5\62\u014c\n\62\3\63\3\63\3\64\3\64\3\65"+
		"\3\65\5\65\u0154\n\65\3\66\3\66\3\66\5\66\u0159\n\66\3\67\5\67\u015c\n"+
		"\67\3\67\3\67\38\38\38\38\78\u0164\n8\f8\168\u0167\138\38\38\58\u016b"+
		"\n8\38\38\39\39\39\39\79\u0173\n9\f9\169\u0176\139\39\39\39\39\39\3:\6"+
		":\u017e\n:\r:\16:\u017f\3:\3:\3;\3;\3;\3;\7\u00dc\u00e4\u00ec\u00f4\u0174"+
		"\2<\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35"+
		"\20\37\21!\22#\23%\2\'\24)\2+\25-\26/\2\61\2\63\2\65\2\67\29\2;\2=\2?"+
		"\2A\2C\2E\2G\2I\2K\2M\2O\2Q\2S\2U\2W\2Y\2[\2]\2_\2a\2c\2e\2g\2i\2k\2m"+
		"\2o\27q\30s\31u\32\3\2%\3\2))\t\2\13\f\17\17\"\"**>>]]}}\4\2CCcc\4\2D"+
		"Ddd\4\2EEee\4\2FFff\4\2GGgg\4\2HHhh\4\2IIii\4\2JJjj\4\2KKkk\4\2LLll\4"+
		"\2MMmm\4\2NNnn\4\2OOoo\4\2PPpp\4\2QQqq\4\2RRrr\4\2SSss\4\2TTtt\4\2UUu"+
		"u\4\2VVvv\4\2WWww\4\2XXxx\4\2YYyy\4\2ZZzz\4\2[[{{\4\2\\\\||\4\2CHch\3"+
		"\2\62;\4\2C\\c|\4\2&&aa\4\2\2#%\1\4\2\f\f\17\17\5\2\13\f\17\17\"\"\2\u017b"+
		"\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2"+
		"\2\2\2\'\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2o\3\2\2\2\2q\3\2\2\2\2s\3\2\2"+
		"\2\2u\3\2\2\2\3w\3\2\2\2\5y\3\2\2\2\7{\3\2\2\2\t~\3\2\2\2\13\u0085\3\2"+
		"\2\2\r\u008a\3\2\2\2\17\u0091\3\2\2\2\21\u0096\3\2\2\2\23\u009c\3\2\2"+
		"\2\25\u009f\3\2\2\2\27\u00a9\3\2\2\2\31\u00ad\3\2\2\2\33\u00b4\3\2\2\2"+
		"\35\u00b6\3\2\2\2\37\u00b8\3\2\2\2!\u00ba\3\2\2\2#\u00c5\3\2\2\2%\u00d1"+
		"\3\2\2\2\'\u00d3\3\2\2\2)\u0103\3\2\2\2+\u0105\3\2\2\2-\u010d\3\2\2\2"+
		"/\u0115\3\2\2\2\61\u0117\3\2\2\2\63\u0119\3\2\2\2\65\u011b\3\2\2\2\67"+
		"\u011d\3\2\2\29\u011f\3\2\2\2;\u0121\3\2\2\2=\u0123\3\2\2\2?\u0125\3\2"+
		"\2\2A\u0127\3\2\2\2C\u0129\3\2\2\2E\u012b\3\2\2\2G\u012d\3\2\2\2I\u012f"+
		"\3\2\2\2K\u0131\3\2\2\2M\u0133\3\2\2\2O\u0135\3\2\2\2Q\u0137\3\2\2\2S"+
		"\u0139\3\2\2\2U\u013b\3\2\2\2W\u013d\3\2\2\2Y\u013f\3\2\2\2[\u0141\3\2"+
		"\2\2]\u0143\3\2\2\2_\u0145\3\2\2\2a\u0147\3\2\2\2c\u014b\3\2\2\2e\u014d"+
		"\3\2\2\2g\u014f\3\2\2\2i\u0153\3\2\2\2k\u0158\3\2\2\2m\u015b\3\2\2\2o"+
		"\u015f\3\2\2\2q\u016e\3\2\2\2s\u017d\3\2\2\2u\u0183\3\2\2\2wx\7=\2\2x"+
		"\4\3\2\2\2yz\7\60\2\2z\6\3\2\2\2{|\5/\30\2|}\5S*\2}\b\3\2\2\2~\177\5\65"+
		"\33\2\177\u0080\5\67\34\2\u0080\u0081\5E#\2\u0081\u0082\5\67\34\2\u0082"+
		"\u0083\5U+\2\u0083\u0084\5\67\34\2\u0084\n\3\2\2\2\u0085\u0086\59\35\2"+
		"\u0086\u0087\5Q)\2\u0087\u0088\5K&\2\u0088\u0089\5G$\2\u0089\f\3\2\2\2"+
		"\u008a\u008b\5? \2\u008b\u008c\5I%\2\u008c\u008d\5S*\2\u008d\u008e\5\67"+
		"\34\2\u008e\u008f\5Q)\2\u008f\u0090\5U+\2\u0090\16\3\2\2\2\u0091\u0092"+
		"\5? \2\u0092\u0093\5I%\2\u0093\u0094\5U+\2\u0094\u0095\5K&\2\u0095\20"+
		"\3\2\2\2\u0096\u0097\5G$\2\u0097\u0098\5\67\34\2\u0098\u0099\5Q)\2\u0099"+
		"\u009a\5;\36\2\u009a\u009b\5\67\34\2\u009b\22\3\2\2\2\u009c\u009d\5K&"+
		"\2\u009d\u009e\5Q)\2\u009e\24\3\2\2\2\u009f\u00a0\5Q)\2\u00a0\u00a1\5"+
		"\67\34\2\u00a1\u00a2\5U+\2\u00a2\u00a3\5W,\2\u00a3\u00a4\5Q)\2\u00a4\u00a5"+
		"\5I%\2\u00a5\u00a6\5? \2\u00a6\u00a7\5I%\2\u00a7\u00a8\5;\36\2\u00a8\26"+
		"\3\2\2\2\u00a9\u00aa\5S*\2\u00aa\u00ab\5\67\34\2\u00ab\u00ac\5U+\2\u00ac"+
		"\30\3\2\2\2\u00ad\u00ae\5W,\2\u00ae\u00af\5M\'\2\u00af\u00b0\5\65\33\2"+
		"\u00b0\u00b1\5/\30\2\u00b1\u00b2\5U+\2\u00b2\u00b3\5\67\34\2\u00b3\32"+
		"\3\2\2\2\u00b4\u00b5\7*\2\2\u00b5\34\3\2\2\2\u00b6\u00b7\7+\2\2\u00b7"+
		"\36\3\2\2\2\u00b8\u00b9\7.\2\2\u00b9 \3\2\2\2\u00ba\u00c0\7)\2\2\u00bb"+
		"\u00bf\n\2\2\2\u00bc\u00bd\7)\2\2\u00bd\u00bf\7)\2\2\u00be\u00bb\3\2\2"+
		"\2\u00be\u00bc\3\2\2\2\u00bf\u00c2\3\2\2\2\u00c0\u00be\3\2\2\2\u00c0\u00c1"+
		"\3\2\2\2\u00c1\u00c3\3\2\2\2\u00c2\u00c0\3\2\2\2\u00c3\u00c4\7)\2\2\u00c4"+
		"\"\3\2\2\2\u00c5\u00c6\5]/\2\u00c6\u00cc\7)\2\2\u00c7\u00c8\5c\62\2\u00c8"+
		"\u00c9\5c\62\2\u00c9\u00cb\3\2\2\2\u00ca\u00c7\3\2\2\2\u00cb\u00ce\3\2"+
		"\2\2\u00cc\u00ca\3\2\2\2\u00cc\u00cd\3\2\2\2\u00cd\u00cf\3\2\2\2\u00ce"+
		"\u00cc\3\2\2\2\u00cf\u00d0\7)\2\2\u00d0$\3\2\2\2\u00d1\u00d2\n\3\2\2\u00d2"+
		"&\3\2\2\2\u00d3\u00d4\5O(\2\u00d4\u00d5\t\2\2\2\u00d5\u00d6\5)\25\2\u00d6"+
		"\u00d7\t\2\2\2\u00d7(\3\2\2\2\u00d8\u00dc\7>\2\2\u00d9\u00db\13\2\2\2"+
		"\u00da\u00d9\3\2\2\2\u00db\u00de\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dc\u00da"+
		"\3\2\2\2\u00dd\u00df\3\2\2\2\u00de\u00dc\3\2\2\2\u00df\u0104\7@\2\2\u00e0"+
		"\u00e4\7}\2\2\u00e1\u00e3\13\2\2\2\u00e2\u00e1\3\2\2\2\u00e3\u00e6\3\2"+
		"\2\2\u00e4\u00e5\3\2\2\2\u00e4\u00e2\3\2\2\2\u00e5\u00e7\3\2\2\2\u00e6"+
		"\u00e4\3\2\2\2\u00e7\u0104\7\177\2\2\u00e8\u00ec\7]\2\2\u00e9\u00eb\13"+
		"\2\2\2\u00ea\u00e9\3\2\2\2\u00eb\u00ee\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ec"+
		"\u00ea\3\2\2\2\u00ed\u00ef\3\2\2\2\u00ee\u00ec\3\2\2\2\u00ef\u0104\7_"+
		"\2\2\u00f0\u00f4\7*\2\2\u00f1\u00f3\13\2\2\2\u00f2\u00f1\3\2\2\2\u00f3"+
		"\u00f6\3\2\2\2\u00f4\u00f5\3\2\2\2\u00f4\u00f2\3\2\2\2\u00f5\u00f7\3\2"+
		"\2\2\u00f6\u00f4\3\2\2\2\u00f7\u0104\7+\2\2\u00f8\u00fd\5%\23\2\u00f9"+
		"\u00fa\6\25\2\2\u00fa\u00fc\13\2\2\2\u00fb\u00f9\3\2\2\2\u00fc\u00ff\3"+
		"\2\2\2\u00fd\u00fb\3\2\2\2\u00fd\u00fe\3\2\2\2\u00fe\u0100\3\2\2\2\u00ff"+
		"\u00fd\3\2\2\2\u0100\u0101\6\25\3\2\u0101\u0102\13\2\2\2\u0102\u0104\3"+
		"\2\2\2\u0103\u00d8\3\2\2\2\u0103\u00e0\3\2\2\2\u0103\u00e8\3\2\2\2\u0103"+
		"\u00f0\3\2\2\2\u0103\u00f8\3\2\2\2\u0104*\3\2\2\2\u0105\u010a\5g\64\2"+
		"\u0106\u0109\5g\64\2\u0107\u0109\5i\65\2\u0108\u0106\3\2\2\2\u0108\u0107"+
		"\3\2\2\2\u0109\u010c\3\2\2\2\u010a\u0108\3\2\2\2\u010a\u010b\3\2\2\2\u010b"+
		",\3\2\2\2\u010c\u010a\3\2\2\2\u010d\u010f\7$\2\2\u010e\u0110\5k\66\2\u010f"+
		"\u010e\3\2\2\2\u0110\u0111\3\2\2\2\u0111\u010f\3\2\2\2\u0111\u0112\3\2"+
		"\2\2\u0112\u0113\3\2\2\2\u0113\u0114\7$\2\2\u0114.\3\2\2\2\u0115\u0116"+
		"\t\4\2\2\u0116\60\3\2\2\2\u0117\u0118\t\5\2\2\u0118\62\3\2\2\2\u0119\u011a"+
		"\t\6\2\2\u011a\64\3\2\2\2\u011b\u011c\t\7\2\2\u011c\66\3\2\2\2\u011d\u011e"+
		"\t\b\2\2\u011e8\3\2\2\2\u011f\u0120\t\t\2\2\u0120:\3\2\2\2\u0121\u0122"+
		"\t\n\2\2\u0122<\3\2\2\2\u0123\u0124\t\13\2\2\u0124>\3\2\2\2\u0125\u0126"+
		"\t\f\2\2\u0126@\3\2\2\2\u0127\u0128\t\r\2\2\u0128B\3\2\2\2\u0129\u012a"+
		"\t\16\2\2\u012aD\3\2\2\2\u012b\u012c\t\17\2\2\u012cF\3\2\2\2\u012d\u012e"+
		"\t\20\2\2\u012eH\3\2\2\2\u012f\u0130\t\21\2\2\u0130J\3\2\2\2\u0131\u0132"+
		"\t\22\2\2\u0132L\3\2\2\2\u0133\u0134\t\23\2\2\u0134N\3\2\2\2\u0135\u0136"+
		"\t\24\2\2\u0136P\3\2\2\2\u0137\u0138\t\25\2\2\u0138R\3\2\2\2\u0139\u013a"+
		"\t\26\2\2\u013aT\3\2\2\2\u013b\u013c\t\27\2\2\u013cV\3\2\2\2\u013d\u013e"+
		"\t\30\2\2\u013eX\3\2\2\2\u013f\u0140\t\31\2\2\u0140Z\3\2\2\2\u0141\u0142"+
		"\t\32\2\2\u0142\\\3\2\2\2\u0143\u0144\t\33\2\2\u0144^\3\2\2\2\u0145\u0146"+
		"\t\34\2\2\u0146`\3\2\2\2\u0147\u0148\t\35\2\2\u0148b\3\2\2\2\u0149\u014c"+
		"\5e\63\2\u014a\u014c\t\36\2\2\u014b\u0149\3\2\2\2\u014b\u014a\3\2\2\2"+
		"\u014cd\3\2\2\2\u014d\u014e\t\37\2\2\u014ef\3\2\2\2\u014f\u0150\t \2\2"+
		"\u0150h\3\2\2\2\u0151\u0154\5e\63\2\u0152\u0154\t!\2\2\u0153\u0151\3\2"+
		"\2\2\u0153\u0152\3\2\2\2\u0154j\3\2\2\2\u0155\u0159\t\"\2\2\u0156\u0157"+
		"\7$\2\2\u0157\u0159\7$\2\2\u0158\u0155\3\2\2\2\u0158\u0156\3\2\2\2\u0159"+
		"l\3\2\2\2\u015a\u015c\7\17\2\2\u015b\u015a\3\2\2\2\u015b\u015c\3\2\2\2"+
		"\u015c\u015d\3\2\2\2\u015d\u015e\7\f\2\2\u015en\3\2\2\2\u015f\u0160\7"+
		"/\2\2\u0160\u0161\7/\2\2\u0161\u0165\3\2\2\2\u0162\u0164\n#\2\2\u0163"+
		"\u0162\3\2\2\2\u0164\u0167\3\2\2\2\u0165\u0163\3\2\2\2\u0165\u0166\3\2"+
		"\2\2\u0166\u016a\3\2\2\2\u0167\u0165\3\2\2\2\u0168\u016b\5m\67\2\u0169"+
		"\u016b\7\2\2\3\u016a\u0168\3\2\2\2\u016a\u0169\3\2\2\2\u016b\u016c\3\2"+
		"\2\2\u016c\u016d\b8\2\2\u016dp\3\2\2\2\u016e\u016f\7\61\2\2\u016f\u0170"+
		"\7,\2\2\u0170\u0174\3\2\2\2\u0171\u0173\13\2\2\2\u0172\u0171\3\2\2\2\u0173"+
		"\u0176\3\2\2\2\u0174\u0175\3\2\2\2\u0174\u0172\3\2\2\2\u0175\u0177\3\2"+
		"\2\2\u0176\u0174\3\2\2\2\u0177\u0178\7,\2\2\u0178\u0179\7\61\2\2\u0179"+
		"\u017a\3\2\2\2\u017a\u017b\b9\2\2\u017br\3\2\2\2\u017c\u017e\t$\2\2\u017d"+
		"\u017c\3\2\2\2\u017e\u017f\3\2\2\2\u017f\u017d\3\2\2\2\u017f\u0180\3\2"+
		"\2\2\u0180\u0181\3\2\2\2\u0181\u0182\b:\2\2\u0182t\3\2\2\2\u0183\u0184"+
		"\13\2\2\2\u0184\u0185\3\2\2\2\u0185\u0186\b;\2\2\u0186v\3\2\2\2\27\2\u00be"+
		"\u00c0\u00cc\u00dc\u00e4\u00ec\u00f4\u00fd\u0103\u0108\u010a\u0111\u014b"+
		"\u0153\u0158\u015b\u0165\u016a\u0174\u017f\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}