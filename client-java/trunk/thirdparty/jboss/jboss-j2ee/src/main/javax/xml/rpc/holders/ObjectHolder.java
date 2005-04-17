/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for Objects.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class ObjectHolder
   implements Holder
{
   public Object value;

   public ObjectHolder()
   {
      
   }      
   public ObjectHolder(Object value)
   {
      this.value = value;
   }
}
