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

package org.firebirdsql.gds;

/**
 * A GDS-specific exception
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class GDSException extends Exception {

    protected int type;
    protected int intParam;
    protected String strParam;

    /**
     * The variable <code>xaErrorCode</code> is used to allow the same
     * code to be used for transaction control from the XAResource,
     * LocalTransaction, and Connection.  This code may be added to
     * the GDSException without obscuring the message: only at the
     * final level is the GDSException converted to the spec-required
     * exception.
     *
     */
    protected int xaErrorCode;

    /**
     * My child
     */
    protected GDSException next;

    /**
     * Factory method to create a new instance with a given <code>XA</code>
     * error code.
     *
     * @param message Message for the new instance
     * @param xaErrorCode The <code>XA</code> error code
     */
    public static GDSException createWithXAErrorCode(String message, int xaErrorCode)
    {
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
    }
    
    /**
     * Construct instance of this class. This method correctly constructs
     * chain of exceptions for one string parameter.
     * 
     * @param type type of the exception, should be always 
     * {@link ISCConstants#isc_arg_gds}, otherwise no message will be displayed.
     * 
     * @param fbErrorCode Firebird error code, one of the constants declared
     * in {@link GDS} interface.
     * 
     * @param strParam value of the string parameter that will substitute 
     * <code>{0}</code> entry in error message corresponding to the specified
     * error code.
     */
    public GDSException(int type, int fbErrorCode, String strParam) {
        this.type = type;
        this.intParam = fbErrorCode;
        setNext(new GDSException(ISCConstants.isc_arg_string, strParam));
    }

    /**
     * Create a new instance.
     *
     * @param fbErrorCode Firebird error code, one of the constants declared
     *        in {@link GDS} interface
     */
    public GDSException(int fbErrorCode) {
        // this.fbErrorCode = fbErrorCode;
        this.intParam = fbErrorCode;
        this.type = ISCConstants.isc_arg_gds;
    }

    /**
     * Create a new instance with only a simple message.
     *
     * @param message Message for the new exception
     */
    public GDSException(String message) {
        super(message);
        this.type = ISCConstants.isc_arg_string;
    }

    /**
     * Get the Firebird-specific error code for this exception.
     *
     * @return The Firebird error code
     */
    public int getFbErrorCode() {
        //return fbErrorCode;
        if (type == ISCConstants.isc_arg_number)
            return intParam;
        else
            return -1;
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
    public int getXAErrorCode()
    {
        return xaErrorCode;
    }

    /**
     * Set the XaErrorCode value.
     * @param xaErrorCode The new XaErrorCode value.
     */
    public void setXAErrorCode(int xaErrorCode)
    {
        this.xaErrorCode = xaErrorCode;
    }

    /**
     * Set the next exception in the chain.
     *
     * @param e The next chained exception
     */
    public void setNext(GDSException e) {
        next = e;
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
        //while (child != null) {
        if (child != null)
            msg += "\n" + child.getMessage();
        //    child = child.next;
        //}
 
        return msg;
    }

    /**
     * Retrieve whether this is a fatal exception, or if this exception
     * is chained to a fatal exception.
     *
     * @return <code>true</code> if this is a fatal exception
     *         <code>false</code> otherwise
     */
    public boolean isFatal()
    {
        return isThisFatal() || (next != null && next.isFatal());
    }


    private boolean isThisFatal()
    {
        for (int i = 0; i < ISCConstants.FATAL_ERRORS.length 
                 && intParam >= ISCConstants.FATAL_ERRORS[i]; i++)
        {
            if (intParam == ISCConstants.FATAL_ERRORS[i]) 
            {
                return true;
            } // end of if ()
            
        } // end of for ()
        return false;
    }


    /**
     * Returns the parameter depending on the type of the
     * error code.
     */
    protected String getParam() {
        if ((type == ISCConstants.isc_arg_interpreted) ||
                (type == ISCConstants.isc_arg_string))
            return strParam;
        else
        if (type == ISCConstants.isc_arg_number)
            return "" + intParam;
        else
            return "";
    }

}

