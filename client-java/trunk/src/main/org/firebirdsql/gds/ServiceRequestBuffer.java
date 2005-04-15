/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.firebirdsql.gds;

/**
 * The interface <code>ServiceRequestBuffer</code> models represents the interbase Service Request Buffer.
 */
public interface ServiceRequestBuffer
    {
    /**
     * Set a void (valueless) parameter on this 
     * <code>ServiceRequestBuffer</code>.
     *
     * @param argumentType The parameter to be set
     */
    public void addArgument(int argumentType);

    /**
     * Set a <code>String</code> parameter on this
     * <code>ServiceRequestBuffer</code>.
     *
     * @param argumentType The parameter to be set
     * @param value The value to set for the given parameter
     */
    public void addArgument(int argumentType, String value);

    /**
     * Set an <code>int</code> parameter on this
     * <code>ServiceRequestBuffer</code>.
     *
     * @param argumentType The parameter to be set
     * @param value The value to set for the given parameter
     */
    public void addArgument(int argumentType, int value);

    /**
     * Set a <code>byte</code> parameter on this
     * <code>ServiceRequestBuffer</code>.
     *
     * @param argumentType The parameter to be set
     * @param value The value to the set for the given parameter
     */
    public void addArgument(int argumentType, byte value);
}
