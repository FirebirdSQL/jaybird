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
import java.util.Arrays;

import java.io.ByteArrayOutputStream;

/**
 * Provides implementation common to both ServiceParameterBufferImp and ServiceRequestBufferImp
 */
abstract class ParameterBufferBase implements java.io.Serializable
    {
	// Parameter Buffer Implementation 
	
    public void addArgument(int argumentType, String value)
        {
        getArgumentsList().add(new StringArgument(argumentType, value ));
        }

    public void addArgument(int argumentType, int value)
        {
        getArgumentsList().add(new NumericArgument(argumentType, value));
        }

    public void addArgument(int argumentType)
        {
        getArgumentsList().add(new SingleItem(argumentType));
        }

    public void addArgument(int type, byte[] content)
        {
        getArgumentsList().add(new ByteArrayArgument(type, content));
        }

    public String getArgumentAsString(int type)
        {
        final List argumentsList = getArgumentsList();
        for( int i = 0, n = argumentsList.size(); i<n; i++ )
            {
            final Argument argument = (Argument)argumentsList.get(i);
            if( argument.getType() == type )
                {
                return argument.getValueAsString();
                }
            }
        return null;
        }
	
	public void removeArgument(int type)
		{
		final List argumentsList = getArgumentsList();
        for( int i = 0, n = argumentsList.size(); i<n; i++ )
            {
            final Argument argument = (Argument)argumentsList.get(i);
            if( argument.getType() == type )
                {
				argumentsList.remove(i);
                return;
                }
            }
		}
	
	// Object Implementation  
	
	public boolean equals(Object other)
        {
        if( other == null || other instanceof ParameterBufferBase == false )
            return false;

        final ParameterBufferBase otherServiceBufferBase = (ParameterBufferBase)other;

        return otherServiceBufferBase.getArgumentsList().equals(this.getArgumentsList());
        }

    public int hashCode()
        {
        return getArgumentsList().hashCode();
        }
	
	// Internal methods
	
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


    //---------------------------------------------------------------------------
	// Inner Classes
	//---------------------------------------------------------------------------

	
	//---------------------------------------------------------------------------
	// Argument - Abstract base 
	//---------------------------------------------------------------------------
    protected abstract static class Argument implements java.io.Serializable
        {
        abstract int  getType();

        String getValueAsString()
            {
            throw new RuntimeException("Cannot get the value for this argument type as a string");
            }

        abstract void writeTo(ByteArrayOutputStream outputStream);
        }

    


    //---------------------------------------------------------------------------
	// StringArgument
	//---------------------------------------------------------------------------
    protected static class StringArgument extends Argument
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

            writeLength(valueLength, outputStream);
            for(int i = 0; i<valueLength; i++)
                outputStream.write(valueBytes[i]);
            }

        String getValueAsString()
            {
            return value;
            }

        protected void writeLength(int length, ByteArrayOutputStream outputStream)
            {
            outputStream.write(length);
            }

        int  getType()
            {
            return type;
            }

        public int hashCode()
            {
            return value.hashCode();
            }

        public boolean equals(Object other)
            {
            if( other == null || other instanceof StringArgument == false )
                return false;

            final StringArgument otherStringArgument = (StringArgument)other;

            return type  == otherStringArgument.type &&
                   value.equals(otherStringArgument.value);
            }


        private final int type;
        private final String value;
        }


    //---------------------------------------------------------------------------
	// NumericArgument 
	//---------------------------------------------------------------------------
	protected static class NumericArgument extends Argument
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

            writeValue(outputStream, value);
            }

        protected void writeValue(ByteArrayOutputStream outputStream, final int value)
            {
            outputStream.write(value);
            outputStream.write(value>>8);
            outputStream.write(value>>16);
            outputStream.write(value>>24);
            }

        int  getType()
            {
            return type;
            }

        public int hashCode()
            {
            return type;
            }

        public boolean equals(Object other)
            {
            if( other == null || other instanceof NumericArgument == false )
                return false;

            final NumericArgument otherNumericArgument = (NumericArgument)other;

            return type  == otherNumericArgument.type &&
                   value == otherNumericArgument.value;
            }

        private final int type;
        private final int value;
        }

	
	//---------------------------------------------------------------------------
	// ByteArrayArgument
	//---------------------------------------------------------------------------
    private static final class ByteArrayArgument extends Argument
        {
        ByteArrayArgument( int type, byte[] value )
            {
            this.type = type;
            this.value = value;
            }

        void writeTo(ByteArrayOutputStream outputStream)
            {
            outputStream.write(type);
            final int valueLength = value.length;
            writeLength(valueLength, outputStream);
            for(int i = 0; i<valueLength; i++)
                outputStream.write(value[i]);
            }

        protected void writeLength(int length, ByteArrayOutputStream outputStream)
            {
            outputStream.write(length);
            }

        int  getType()
            {
            return type;
            }

        public int hashCode()
            {
            return type;
            }

        public boolean equals(Object other)
            {
            if( other == null || other instanceof ByteArrayArgument == false )
                return false;

            final ByteArrayArgument otherByteArrayArgument = (ByteArrayArgument)other;

            return type  == otherByteArrayArgument.type &&
                   Arrays.equals(value , otherByteArrayArgument.value);
            }

        private final int type;
        private final byte[] value;
        }

	
	//---------------------------------------------------------------------------
	// SingleItem
	//---------------------------------------------------------------------------
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

        int  getType()
            {
            return item;
            }

        public int hashCode()
            {
            return item;
            }

        public boolean equals(Object other)
            {
            if( other == null || other instanceof SingleItem == false )
                return false;

            final SingleItem otherSingleItem = (SingleItem)other;

            return item  == otherSingleItem.item;
            }

        private final int item;
        }

    }
