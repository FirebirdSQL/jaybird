/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.handler;

import java.util.Iterator;

/** This interface abstracts the message context that is processed by a handler
 * in the handle method.
 * 
 * The MessageContext interface provides methods to manage a property set.
 * MessageContext properties enable handlers in a handler chain to share
 * processing related state. 
 * 
 * @author Scott.Stark@jboss.org
 * @author Rahul Sharma, Roberto Chinnici (javadoc)
 * @version $Revision$
 */
public interface MessageContext
{
   /**
    * Returns true if the MessageContext contains a property with the specified name.
    * @param name Name of the property whose presense is to be tested
    * @return Returns true if the MessageContext contains the property; otherwise false
    */
   public boolean containsProperty(String name);

   /**
    * Gets the value of a specific property from the MessageContext
    * @param name Name of the property whose value is to be retrieved
    * @return Value of the property
    * @throws IllegalArgumentException if an illegal property name is specified
    */
   public Object getProperty(String name);

   /**
    * Returns an Iterator view of the names of the properties in this MessageContext
    * @return Iterator for the property names
    */
   public Iterator getPropertyNames();

   /**
    * Removes a property (name-value pair) from the MessageContext
    * @param name Name of the property to be removed
    * @throws IllegalArgumentException if an illegal property name is specified
    */
   public void removeProperty(String name);

   /**
    * Sets the name and value of a property associated with the MessageContext.
    * If the MessageContext contains a value of the same property, the old value is replaced.
    * @param name Name of the property associated with the MessageContext
    * @param value Value of the property
    * @throws IllegalArgumentException If some aspect of the property is prevents it from being stored in the context
    * @throws UnsupportedOperationException If this method is not supported.
    */
   public void setProperty(String name, Object value);
}
