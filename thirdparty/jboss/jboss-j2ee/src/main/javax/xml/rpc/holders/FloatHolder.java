/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for floats.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class FloatHolder
   implements Holder
{
   public float value;

   public FloatHolder()
   {
      
   }      
   public FloatHolder(float value)
   {
      this.value = value;
   }
}
