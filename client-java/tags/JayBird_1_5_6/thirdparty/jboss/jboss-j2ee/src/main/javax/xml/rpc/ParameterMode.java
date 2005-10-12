/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc;

/** A type-safe enumeration for parameter passing modes.
 *  
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class ParameterMode
{
   private String mode;

   public static final ParameterMode IN = new ParameterMode("IN");
   public static final ParameterMode INOUT = new ParameterMode("INOUT");
   public static final ParameterMode OUT = new ParameterMode("OUT");

   private ParameterMode(String mode)
   {
      this.mode = mode;
   }

   public String toString()
   {
      return mode;
   }
}
