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

package org.firebirdsql.ngds;

import java.util.List;
import java.util.ArrayList;

import java.io.ByteArrayOutputStream;

/**
 * Provides implementation common to both ServiceParameterBufferImp and ServiceRequestBufferImp
 */
abstract class ServiceBufferBase
    {
    public void addArgument(int argumentType, String value)
        {
        this.arguments.add(new StringArgument(argumentType, value ));
        }

    public void addArgument(int argumentType, int value)
        {
        this.arguments.add(new NumericArgument(argumentType, value));
        }

    public void addArgument(int argumentType)
        {
        this.arguments.add(new SingleItem(argumentType));
        }

    // PROTECTED INNER CLASS AND METHODS - Interface to derived classes.

    protected abstract static class Argument
        {
        abstract void writeTo(ByteArrayOutputStream outputStream);
        }

    protected void writeArgumentsTo(ByteArrayOutputStream outputStream)
        {
        for( int i = 0, n = arguments.size(); i<n; i++)
            {
            final Argument currentArgument = ((Argument)arguments.get(i));

            currentArgument.writeTo(outputStream);
            }
        }

    protected List getArgumentsList()
        {
        return arguments;
        }


    // PRIVATE MEMBERS

    private final List arguments = new ArrayList();


    // PRIVATE CLASSES - STOCK

    private static final class StringArgument extends Argument
        {
        StringArgument( int type, String value )
            {
            this.type = type;
            this.value = value;
            }

        void writeTo(ByteArrayOutputStream outputStream)
            {
            outputStream.write(type);

            final byte[] valueBytes = this.value.getBytes();
            final int valueLength = valueBytes.length;

            outputStream.write(valueLength);
            outputStream.write(valueLength>>8);
            for(int i = 0; i<valueLength; i++)
                outputStream.write(valueBytes[i]);
            }

        private int type;
        private String value;
        }

    private static final class NumericArgument extends Argument
        {
        NumericArgument( int type, int value )
            {
            this.type = type;
            this.value = value;
            }

        void writeTo(ByteArrayOutputStream outputStream)
            {
            outputStream.write(type);

            final int value = this.value;

            outputStream.write(value);
            outputStream.write(value>>8);
            outputStream.write(value>>16);
            outputStream.write(value>>24);
            }

        private int type;
        private int value;
        }

    private static final class SingleItem extends Argument
        {
        SingleItem( int item )
            {
            this.item = item;
            }

        void writeTo(ByteArrayOutputStream outputStream)
            {
            outputStream.write(item);
            }

        private int item;
        }

    }
