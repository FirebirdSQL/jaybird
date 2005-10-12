/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

/** An object that stores a MIME header name and its value. One or more
 * MimeHeader objects may be contained in a MimeHeaders object.
 *  
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class MimeHeader
{
   private String name;
   private String value;

   public MimeHeader(String name, String value) 
   {
      this.name = name;
      this.value = value;
   }

   public String getName()
   {
      return name;
   }

   public String getValue()
   {
      return value;
   }
}
