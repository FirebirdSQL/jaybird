/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for Strings.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class StringHolder
   implements Holder
{
   public String value;

   public StringHolder()
   {
      
   }      
   public StringHolder(String value)
   {
      this.value = value;
   }
}
