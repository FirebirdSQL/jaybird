/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

import javax.activation.DataHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;

/** The root class for all SOAP messages. As transmitted on the "wire", a SOAP message is an XML document or a
 * MIME message whose first body part is an XML/SOAP document.
 *
 * A SOAPMessage object consists of a SOAP part and optionally one or more attachment parts.
 * The SOAP part for a SOAPMessage object is a SOAPPart object, which contains information used for message routing and
 * identification, and which can contain application-specific content. All data in the SOAP Part of a message must be in XML format.
 *
 * A new SOAPMessage object contains the following by default:
 *
 * A SOAPPart object
 * A SOAPEnvelope object
 * A SOAPBody object
 * A SOAPHeader object
 *
 * The SOAP part of a message can be retrieved by calling the method SOAPMessage.getSOAPPart().
 * The SOAPEnvelope object is retrieved from the SOAPPart object, and the SOAPEnvelope object is used to retrieve the
 * SOAPBody and SOAPHeader objects.
 *
 * SOAPPart sp = message.getSOAPPart();
 * SOAPEnvelope se = sp.getEnvelope();
 * SOAPBody sb = se.getBody();
 * SOAPHeader sh = se.getHeader();
 *
 * In addition to the mandatory SOAPPart object, a SOAPMessage object may contain zero or more AttachmentPart objects,
 * each of which contains application-specific data. The SOAPMessage interface provides methods for creating AttachmentPart
 * objects and also for adding them to a SOAPMessage object. A party that has received a SOAPMessage object can examine
 * its contents by retrieving individual attachment parts.
 *
 * Unlike the rest of a SOAP message, an attachment is not required to be in XML format and can therefore be anything from
 * simple text to an image file. Consequently, any message content that is not in XML format must be in an AttachmentPart object.
 *
 * A MessageFactory object may create SOAPMessage objects with behavior that is specialized to a particular
 * implementation or application of SAAJ. For instance, a MessageFactory object may produce SOAPMessage objects that
 * conform to a particular Profile such as ebXML. In this case a MessageFactory object might produce SOAPMessage
 * objects that are initialized with ebXML headers.
 *
 * In order to ensure backward source compatibility, methods that are added to this class after version 1.1 of the SAAJ
 * specification are all concrete instead of abstract and they all have default implementations.
 * Unless otherwise noted in the JavaDocs for those methods the default implementations simply throw an
 * UnsupportedOperationException and the SAAJ implementation code must override them with methods that provide
 * the specified behavior. Legacy client code does not have this restriction, however, so long as there is no claim
 * made that it conforms to some later version of the specification than it was originally written for.
 * A legacy class that extends the SOAPMessage class can be compiled and/or run against succeeding versions of
 * the SAAJ API without modification. If such a class was correctly implemented then it will continue to behave
 * correctly relative the the version of the specification against which it was written.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public abstract class SOAPMessage
{
   private HashMap properties = new HashMap();

   /** Specifies the character type encoding for the SOAP Message. Valid values include "utf-8" and "utf-16". The default is "utf-8". */
   public static final String CHARACTER_SET_ENCODING = "javax.xml.soap.character-set-encoding";
   /** Specifies whether the SOAP Message will contain an XML declaration when it is sent. The default is "false". */
   public static final String WRITE_XML_DECLARATION = "javax.xml.soap.write-xml-declaration";

   /**
    * Adds the given AttachmentPart object to this SOAPMessage object.
    * An AttachmentPart object must be created before it can be added to a message.
    * @param attachmentpart an AttachmentPart object that is to become part of this SOAPMessage object
    */
   public abstract void addAttachmentPart(AttachmentPart attachmentpart);

   /**
    * Creates a new empty AttachmentPart object. Note that the method addAttachmentPart must be called with this new
    * AttachmentPart object as the parameter in order for it to become an attachment to this SOAPMessage object.
    * @return a new AttachmentPart object that can be populated and added to this SOAPMessage object
    */
   public abstract AttachmentPart createAttachmentPart();

   /**
    * Creates an AttachmentPart object and populates it using the given DataHandler object.
    * @param datahandler the javax.activation.DataHandler object that will generate the content for this SOAPMessage object
    * @return a new AttachmentPart object that contains data generated by the given DataHandler object
    */
   public AttachmentPart createAttachmentPart(DataHandler datahandler)
   {
      AttachmentPart part = createAttachmentPart();
      part.setDataHandler(datahandler);
      return part;
   }

   /**
    * Creates an AttachmentPart object and populates it with the specified data of the specified content type.
    * @param content an Object containing the content for this SOAPMessage object
    * @param contentType a String object giving the type of content; examples are "text/xml", "text/plain", and "image/jpeg"
    * @return  a new AttachmentPart object that contains the given data
    */
   public AttachmentPart createAttachmentPart(Object content, String contentType)
   {
      AttachmentPart part = createAttachmentPart();
      part.setContent(content, contentType);
      return part;
   }

   /**
    * Retrieves value of the specified property.
    * @param property the name of the property to retrieve
    * @return the value associated with the named property or null if no such property exists.
    * @throws SOAPException if the property name is not recognized.
    */
   public Object getProperty(String property)
           throws SOAPException
   {
      return properties.get(property);
   }

   /**
    * Associates the specified value with the specified property. If there was already a value associated with this
    * property, the old value is replaced.
    *
    * The valid property names include WRITE_XML_DECLARATION and CHARACTER_SET_ENCODING.
    * All of these standard SAAJ properties are prefixed by "javax.xml.soap".
    * Vendors may also add implementation specific properties.
    * These properties must be prefixed with package names that are unique to the vendor.
    *
    * Setting the property WRITE_XML_DECLARATION to "true" will cause an XML Declaration to be written out at the start
    * of the SOAP message. The default value of "false" suppresses this declaration.
    *
    * The property CHARACTER_SET_ENCODING defaults to the value "utf-8" which causes the SOAP message to be
    * encoded using UTF-8. Setting CHARACTER_SET_ENCODING to "utf-16" causes the SOAP message to be encoded using UTF-16.
    *
    * Some implementations may allow encodings in addition to UTF-8 and UTF-16. Refer to your vendor's documentation for details.
    *
    * @param property the property with which the specified value is to be associated.
    * @param value the value to be associated with the specified property
    * @throws SOAPException if the property name is not recognized
    */
   public void setProperty(String property, Object value) throws SOAPException
   {
      properties.put(property, value);
   }

   /**
    * Gets the SOAP Body contained in this SOAPMessage object.
    * @return the SOAPBody object contained by this SOAPMessage object
    * @throws SOAPException if the SOAP Body does not exist or cannot be retrieved
    */
   public SOAPBody getSOAPBody() throws SOAPException
   {
      throw new SOAPException("SOAPMessage does not implement getSOAPBody");
   }

   /**
    * Gets the SOAP Header contained in this SOAPMessage object.
    * @return the SOAPHeader object contained by this SOAPMessage object
    * @throws SOAPException if the SOAP Header does not exist or cannot be retrieved
    */
   public SOAPHeader getSOAPHeader() throws SOAPException
   {
      throw new SOAPException("SOAPMessage does not implement getSOAPHeader");
   }

   /**
    * Retrieves a description of this SOAPMessage object's content.
    * @return  a String describing the content of this message or null if no description has been set
    */
   public abstract String getContentDescription();

   /**
    * Sets the description of this SOAPMessage object's content with the given description.
    * @param description a String describing the content of this message
    */
   public abstract void setContentDescription(String description);

   /**
    * Returns all the transport-specific MIME headers for this SOAPMessage object in a transport-independent fashion.
    * @return a MimeHeaders object containing the MimeHeader objects
    */
   public abstract MimeHeaders getMimeHeaders();

   /**
    * Gets the SOAP part of this SOAPMessage object.
    *
    * SOAPMessage object contains one or more attachments, the SOAP Part must be the first MIME body part in the message.
    *
    * @return the SOAPPart object for this SOAPMessage object
    */
   public abstract SOAPPart getSOAPPart();

   /**
    * Removes all AttachmentPart objects that have been added to this SOAPMessage object.
    *
    * This method does not touch the SOAP part.
    */
   public abstract void removeAllAttachments();

   /**
    * Gets a count of the number of attachments in this message. This count does not include the SOAP part.
    * @return the number of AttachmentPart objects that are part of this SOAPMessage object
    */
   public abstract int countAttachments();

   /**
    * Retrieves all the AttachmentPart objects that are part of this SOAPMessage object.
    * @return an iterator over all the attachments in this message
    */
   public abstract Iterator getAttachments();

   /**
    * Retrieves all the AttachmentPart objects that have header entries that match the specified headers.
    * Note that a returned attachment could have headers in addition to those specified.
    * @param mimeheaders a MimeHeaders object containing the MIME headers for which to search
    * @return an iterator over all attachments that have a header that matches one of the given headers
    */
   public abstract Iterator getAttachments(MimeHeaders mimeheaders);

   /**
    * Updates this SOAPMessage object with all the changes that have been made to it.
    * This method is called automatically when writeTo(OutputStream) is called.
    * However, if changes are made to a message that was received or to one that has already been sent, the method
    * saveChanges needs to be called explicitly in order to save the changes.
    * The method saveChanges also generates any changes that can be read back (for example,
    * a MessageId in profiles that support a message id). All MIME headers in a message that is created for sending
    * purposes are guaranteed to have valid values only after saveChanges has been called.
    *
    * In addition, this method marks the point at which the data from all constituent AttachmentPart objects are pulled into the message.
    *
    * @throws SOAPException if there was a problem saving changes to this message.
    */
   public abstract void saveChanges() throws SOAPException;

   /**
    * Indicates whether this SOAPMessage object needs to have the method saveChanges called on it.
    * @return true if saveChanges needs to be called; false otherwise.
    */
   public abstract boolean saveRequired();

   /**
    * Writes this SOAPMessage object to the given output stream. The externalization format is as defined by the
    * SOAP 1.1 with Attachments specification.
    *
    * If there are no attachments, just an XML stream is written out. For those messages that have attachments,
    * writeTo writes a MIME-encoded byte stream.
    *
    * @param outputstream the OutputStream object to which this SOAPMessage object will be written
    * @throws SOAPException if there was a problem in externalizing this SOAP message
    * @throws IOException if an I/O error occurs
    */
   public abstract void writeTo(OutputStream outputstream) throws SOAPException, IOException;
}
