/*
 * $Id$
 *
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
package org.firebirdsql.encodings;

import java.nio.charset.Charset;

/**
 * Implementation of {@link Encoding} which uses the default functionality of {@link java.nio.charset.Charset} and
 * {@link java.lang.String}.
 * <p>
 * The main use for this class is for multi-byte character sets, but it also works for single byte character sets,
 * although {@link EncodingSingleByte} is more efficient for shorter strings.
 * </p>
 */
final class EncodingGeneric implements Encoding {

    // TODO Test claim that EncodingSingleByte is more efficient
    // Some testing seems to indicate that EncodingSingleByte is (slightly) faster for up to +/- 1500-2000 chars),
    // while EncodingGeneric is (slightly) faster with longer strings

    private final Charset charset;

    EncodingGeneric(final Charset charset) {
        this.charset = charset;
    }

    @Override
    public byte[] encodeToCharset(final String in) {
        return in.getBytes(charset);
    }

    @Override
    public String decodeFromCharset(final byte[] in) {
        return new String(in, charset);
    }

    @Override
    public String decodeFromCharset(final byte[] in, final int offset, final int length) {
        return new String(in, offset, length, charset);
    }

    @Override
    public Encoding withTranslation(final CharacterTranslator translator) {
        if (translator == null) return this;
        return new EncodingGenericWithTranslation(translator);
    }

    @Override
    public String getCharsetName() {
        return charset.name();
    }

    /**
     * Class for applying {@link EncodingGeneric} with translation.
     */
    private final class EncodingGenericWithTranslation implements Encoding {
        private final CharacterTranslator translator;

        /**
         * Creates an {@link EncodingGeneric} with translation.
         *
         * @param translator
         *         The translation to apply
         */
        private EncodingGenericWithTranslation(CharacterTranslator translator) {
            assert translator != null : "CharacterTranslator should never be null";
            this.translator = translator;
        }

        @Override
        public byte[] encodeToCharset(final String in) {
            return EncodingGeneric.this.encodeToCharset(new String(translate(in.toCharArray())));
        }

        @Override
        public String decodeFromCharset(final byte[] in) {
            final String result = EncodingGeneric.this.decodeFromCharset(in);
            return new String(translate(result.toCharArray()));
        }

        @Override
        public String decodeFromCharset(final byte[] in, final int off, final int len) {
            final String result = EncodingGeneric.this.decodeFromCharset(in, off, len);
            return new String(translate(result.toCharArray()));
        }

        /**
         * In a slight deviation from the contract of {@link Encoding#withTranslation(CharacterTranslator)},
         * this implementation returns the result of the parent implementation {@link
         * EncodingGeneric#withTranslation(CharacterTranslator)}, as applying multiple translations is a bad idea.
         *
         * @param translator
         *         The translation to apply
         * @return Instance with translation.
         */
        @Override
        public Encoding withTranslation(final CharacterTranslator translator) {
            return EncodingGeneric.this.withTranslation(translator);
        }

        @Override
        public String getCharsetName() {
            return charset.name();
        }

        private char[] translate(char[] chars) {
            for (int i = 0; i < chars.length; i++) {
                chars[i] = translator.getMapping(chars[i]);
            }
            return chars;
        }
    }
}
