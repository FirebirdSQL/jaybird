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

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * A GDS-specific exception
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class GDSException extends Exception {

    private static final long serialVersionUID = -2993273656432230359L;
    
    private static final AtomicReferenceFieldUpdater<GDSException,GDSException> nextUpdater = 
            AtomicReferenceFieldUpdater.newUpdater(GDSException.class, GDSException.class, "next");
    
    private final int type;
    private final int intParam;
    private final String strParam;

    /**
     * The variable <code>xaErrorCode</code> is used to allow the same
     * code to be used for transaction control from the XAResource,
     * LocalTransaction, and Connection.  This code may be added to
     * the GDSException without obscuring the message: only at the
     * final level is the GDSException converted to the spec-required
     * exception.
     *
     */
    private int xaErrorCode;

    /**
     * My child
     */
    private volatile GDSException next;

    /**
     * Factory method to create a new instance with a given <code>XA</code>
     * error code.
     *
     * @param message Message for the new instance
     * @param xaErrorCode The <code>XA</code> error code
     */
    public static GDSException createWithXAErrorCode(String message, int xaErrorCode) {
        GDSException gdse = new GDSException(message);
        gdse.setXAErrorCode(xaErrorCode);
        return gdse;
    }

    /**
     * Create a new instance.
     *
     * @param type type of the exception, should be always 
     *        {@link ISCConstants#isc_arg_gds}, otherwise no message will be 
     *        displayed.
     * @param intParam Additional int parameter about the new exception
     */
    public GDSException(int type, int intParam) {
        this.type = type;
        this.intParam = intParam;
        this.strParam = null;
    }

    /**
     * Create a new instance.
     *
     * @param type type of the exception, should be always 
     *        {@link ISCConstants#isc_arg_gds}, otherwise no message will be 
     *        displayed.
     * @param strParam value of the string parameter that will substitute 
     *        <code>{0}</code> entry in error message corresponding to the 
     *        specified error code.
     */
    public GDSException(int type, String strParam) {
        this.type = type;
        this.strParam = strParam;
        this.intParam = 0;
    }
    
    /**
     * Construct instance of this class. This method correctly constructs
     * chain of exceptions for one string parameter.
     * 
     * @param type type of the exception, should be always 
     * {@link ISCConstants#isc_arg_gds}, otherwise no message will be displayed.
     * 
     * @param fbErrorCode Firebird error code, one of the constants declared in {@link ISCConstants}
     * 
     * @param strParam value of the string parameter that will substitute 
     * <code>{0}</code> entry in error message corresponding to the specified
     * error code.
     */
    public GDSException(int type, int fbErrorCode, String strParam) {
        this.type = type;
        this.intParam = fbErrorCode;
        this.strParam = null;
        setNext(new GDSException(ISCConstants.isc_arg_string, strParam));
    }
    
    /**
     * Construct instance of this class. This method correctly constructs
     * chain of exceptions for one string parameter.
     * 
     * @param type type of the exception, should be always 
     * {@link ISCConstants#isc_arg_gds}, otherwise no message will be displayed.
     * 
     * @param fbErrorCode Firebird error code, one of the constants declared in {@link ISCConstants}
     * 
     * @param strParam value of the string parameter that will substitute 
     * <code>{0}</code> entry in error message corresponding to the specified
     * error code.
     * 
     * @param cause Cause of this exception
     */
    public GDSException(int type, int fbErrorCode, String strParam, Throwable cause) {
        this(type, fbErrorCode, strParam);
        initCause(cause);
        setNext(new GDSException(ISCConstants.isc_arg_string, strParam));
    }

    /**
     * Create a new instance.
     *
     * @param fbErrorCode Firebird error code, one of the constants declared in {@link ISCConstants}
     */
    public GDSException(int fbErrorCode) {
        this.intParam = fbErrorCode;
        this.type = ISCConstants.isc_arg_gds;
        this.strParam = null;
    }
    
    /**
     * Create a new instance.
     *
     * @param fbErrorCode Firebird error code, one of the constants declared in {@link ISCConstants}
     * @param cause Cause of this exception
     */
    public GDSException(int fbErrorCode, Throwable cause) {
        this(fbErrorCode);
        initCause(cause);
    }

    /**
     * Create a new instance with only a simple message.
     *
     * @param message Message for the new exception
     */
    public GDSException(String message) {
        super(message);
        this.type = ISCConstants.isc_arg_string;
        this.intParam = 0;
        this.strParam = null;
    }

    /**
     * Get the Firebird-specific error code for this exception.
     *
     * @return The Firebird error code
     */
    public int getFbErrorCode() {
        switch (type) {
        case ISCConstants.isc_arg_number:
        case ISCConstants.isc_arg_gds:
        case ISCConstants.isc_arg_warning:
            return intParam;
        default:
            return -1;
        }
    }
    
    /**
     * Get the SQL state of this exception.
     * 
     * @return the SQL state of this exception or <code>null</code> if this 
     * object does not represent an error.
     */
    public String getSQLState() {
        switch (type) {
        case ISCConstants.isc_arg_number:
        case ISCConstants.isc_arg_gds:
            return GDSExceptionHelper.getSQLState(intParam);
        default:
            return null;
        }
    }

    /**
     * Get the <code>int</code> parameter for this exception.
     *
     * @return The <code>int</code> parameter
     */
    public int getIntParam() {
        return intParam;
    }


    /**
     * Get the XaErrorCode value.
     * @return the XaErrorCode value.
     */
    public int getXAErrorCode() {
        return xaErrorCode;
    }

    /**
     * Set the XaErrorCode value.
     * @param xaErrorCode The new XaErrorCode value.
     */
    public void setXAErrorCode(int xaErrorCode) {
        this.xaErrorCode = xaErrorCode;
    }

    /**
     * Set the next exception in the chain.
     *
     * @param e The next chained exception
     */
    public void setNext(GDSException e) {
        GDSException current = this;
        for(;;) {
            GDSException next = current.next;
            if (next != null) {
                current = next;
                continue;
            }

            if (nextUpdater.compareAndSet(current, null, e)) {
                return;
            }
            current = current.next;
        }
    }

    /**
     * Get the next chained exception.
     *
     * @return The next chained exception
     */
    public GDSException getNext() {
        return next;
    }
    
    /**
     * Retrieve whether this exception is a warning.
     *
     * @return <code>true</code> if this is a warning, 
     *         <code>false</code> otherwise
     */
    public boolean isWarning() {
        return type == ISCConstants.isc_arg_warning;
    }

    /**
     * Returns a string representation of this exception.
     */
    public String getMessage() {
        String msg;
        
        GDSException child = this.next;
        
        // If I represent a GDSMessage code, then let's format it nicely.
        if (type == ISCConstants.isc_arg_gds || type == ISCConstants.isc_arg_warning) {
            // get message
            GDSExceptionHelper.GDSMessage message =
                GDSExceptionHelper.getMessage(intParam);

            // substitute parameters using my children
            int paramCount = message.getParamCount();
            for(int i = 0; i < paramCount; i++) {
                if (child == null) break;
                message.setParameter(i, child.getParam());
                child = child.next;
            }

            // convert message to string
            msg = message.toString();
        }
        else {
            // No GDSMessage code, so use the default message.
            msg = super.getMessage();
        }
  
        // Do we have more children? Then include their messages too.
        if (child != null) {
            msg += "\n" + child.getMessage();
        }
 
        return msg;
    }

    /**
     * Returns the parameter depending on the type of the error code.
     */
    public String getParam() {
        switch (type) {
        case ISCConstants.isc_arg_interpreted:
        case ISCConstants.isc_arg_string:
            return strParam;
        case ISCConstants.isc_arg_number:
            return String.valueOf(intParam);
        default:
            return "";
        }
    }

}

