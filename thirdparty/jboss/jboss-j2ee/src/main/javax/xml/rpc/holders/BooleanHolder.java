/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for booleans.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class BooleanHolder
   implements Holder
{
   public boolean value;

   public BooleanHolder()
   {
      
   }      
   public BooleanHolder(boolean value)
   {
      this.value = value;
   }
}
