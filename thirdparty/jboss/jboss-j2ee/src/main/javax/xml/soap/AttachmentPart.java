/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

import javax.activation.DataHandler;
import java.util.Iterator;

/**
 * A single attachment to a SOAPMessage object. A SOAPMessage object may contain zero, one, or many AttachmentPart objects.
 * Each AttachmentPart object consists of two parts, application-specific content and associated MIME headers.
 * The MIME headers consists of name/value pairs that can be used to identify and describe the content.
 *
 * An AttachmentPart object must conform to certain standards.
 *
 * 1. It must conform to MIME [RFC2045] standards</li>
 * 2. It MUST contain content</li>
 * 3. The header portion MUST include the following header:
 *
 *    Content-Type
 *    This header identifies the type of data in the content of an AttachmentPart object and MUST conform to [RFC2045].
 *    The following is an example of a Content-Type header:
 *
 *       Content-Type:  application/xml
 *
 *    The following line of code, in which ap is an AttachmentPart object, sets the header shown in the previous example.
 *
 *       ap.setMimeHeader("Content-Type", "application/xml");
 *
 * There are no restrictions on the content portion of an AttachmentPart object. The content may be anything from a
 * simple plain text object to a complex XML document or image file.
 *
 * An AttachmentPart object is created with the method SOAPMessage.createAttachmentPart.
 * After setting its MIME headers, the AttachmentPart object is added to the message that
 * created it with the method SOAPMessage.addAttachmentPart.
 *
 * The following code fragment, in which m is a SOAPMessage object and contentStringl is a String,
 * creates an instance of AttachmentPart, sets the AttachmentPart object with some content and header information,
 * and adds the AttachmentPart object to the SOAPMessage object.
 *
 *    AttachmentPart ap1 = m.createAttachmentPart();
 *    ap1.setContent(contentString1, "text/plain");
 *    m.addAttachmentPart(ap1);
 *
 * The following code fragment creates and adds a second AttachmentPart instance to the same message.
 * jpegData is a binary byte buffer representing the jpeg file.
 *
 *    AttachmentPart ap2 = m.createAttachmentPart();
 *    byte[] jpegData =  ...;
 *    ap2.setContent(new ByteArrayInputStream(jpegData), "image/jpeg");
 *    m.addAttachmentPart(ap2);
 *
 * The getContent method retrieves the contents and header from an AttachmentPart object.
 * Depending on the DataContentHandler objects present, the returned Object can either be a typed Java object
 * corresponding to the MIME type or an InputStream object that contains the content as bytes.
 *
 *    String content1 = ap1.getContent();
 *    java.io.InputStream content2 = ap2.getContent();
 *
 * The method clearContent removes all the content from an AttachmentPart object but does not affect its header information.
 *
 *    ap1.clearContent();
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public abstract class AttachmentPart
{
   private String contentId;
   private String contentLocation;
   private String contentType;

   /**
    * Adds a MIME header with the specified name and value to this AttachmentPart object.
    *
    * Note that RFC822 headers can contain only US-ASCII characters.
    * @param name a String giving the name of the header to be added
    * @param value a String giving the value of the header to be added
    */
   public abstract void addMimeHeader(String name, String value);

   /**
    * Clears out the content of this AttachmentPart object. The MIME header portion is left untouched.
    */
   public abstract void clearContent();

   /**
    * Retrieves all the headers for this AttachmentPart object as an iterator over the MimeHeader objects.
    * @return an Iterator object with all of the Mime headers for this AttachmentPart object
    */
   public abstract Iterator getAllMimeHeaders();

   /**
    * Gets the content of this AttachmentPart object as a Java object.
    * The type of the returned Java object depends on
    * (1) the DataContentHandler object that is used to interpret the bytes and
    * (2) the Content-Type given in the header.
    *
    * For the MIME content types "text/plain", "text/html" and "text/xml", the DataContentHandler object does the
    * conversions to and from the Java types corresponding to the MIME types.
    *
    * For other MIME types,the DataContentHandler object can return an InputStream object that contains the content
    * data as raw bytes.
    *
    * A SAAJ-compliant implementation must, as a minimum, return a java.lang.String object corresponding to any
    * content stream with a Content-Type value of text/plain, a javax.xml.transform.stream.StreamSource object
    * corresponding to a content stream with a Content-Type value of text/xml,
    * a java.awt.Image object corresponding to a content stream with a Content-Type value of image/gif or image/jpeg.
    *
    * For those content types that an installed DataContentHandler object does not understand, the DataContentHandler
    * object is required to return a java.io.InputStream object with the raw bytes.
    *
    * @return a Java object with the content of this AttachmentPart object
    * @throws SOAPException if there is no content set into this AttachmentPart object or if there was a data transformation error
    */
   public abstract Object getContent() throws SOAPException;

   /**
    * Gets the DataHandler object for this AttachmentPart object.
    * @return object associated with this AttachmentPart object
    * @throws SOAPException if there is no data in this AttachmentPart object
    */
   public abstract DataHandler getDataHandler() throws SOAPException;

   /**
    * Retrieves all MimeHeader objects that match a name in the given array.
    * @param names a String array with the name(s) of the MIME headers to be returned
    * @return all of the MIME headers that match one of the names in the given array as an Iterator object
    */
   public abstract Iterator getMatchingMimeHeaders(String[] names);

   /**
    * Gets all the values of the header identified by the given String.
    * @param name the name of the header; example: "Content-Type"
    * @return a String array giving the value for the specified header
    */
   public abstract String[] getMimeHeader(String name);

   /**
    * Retrieves all MimeHeader objects whose name does not match a name in the given array.
    * @param names a String array with the name(s) of the MIME headers not to be returned
    * @return all of the MIME headers in this AttachmentPart object except those that match one of the names
    * in the given array. The nonmatching MIME headers are returned as an Iterator object.
    */
   public abstract Iterator getNonMatchingMimeHeaders(String[] names);

   /**
    * Returns the number of bytes in this AttachmentPart object.
    * @return the size of this AttachmentPart object in bytes or -1 if the size cannot be determined
    * @throws SOAPException if the content of this attachment is corrupted of if there was an exception while trying to determine the size.
    */
   public abstract int getSize() throws SOAPException;

   /**
    * Removes all the MIME header entries.
    */
   public abstract void removeAllMimeHeaders();

   /**
    * Removes all MIME headers that match the given name.
    * @param name the string name of the MIME header/s to be removed
    */
   public abstract void removeMimeHeader(String name);

   /**
    * Sets the content of this attachment part to that of the given Object and sets the value of the Content-Type header
    * to the given type. The type of the Object should correspond to the value given for the Content-Type.
    * This depends on the particular set of DataContentHandler objects in use.
    *
    * @param object the Java object that makes up the content for this attachment part
    * @param contentType the MIME string that specifies the type of the content
    * @throws IllegalArgumentException if the contentType does not match the type of the content object,
    * or if there was no DataContentHandler object for this content object
    */
   public abstract void setContent(Object object, String contentType);

   /**
    * Sets the given DataHandler object as the data handler for this AttachmentPart object.
    * Typically, on an incoming message, the data handler is automatically set.
    * When a message is being created and populated with content, the setDataHandler method can be used to get data
    * from various data sources into the message.
    * @param dataHandler the DataHandler object to be set
    * @throws IllegalArgumentException if there was a problem with the specified DataHandler object
    */
   public abstract void setDataHandler(DataHandler dataHandler);

   /**
    * Changes the first header entry that matches the given name to the given value, adding a new header if no existing
    * header matches. This method also removes all matching headers but the first.
    *
    * Note that RFC822 headers can only contain US-ASCII characters.
    * @param name a String giving the name of the header for which to search
    * @param value a String giving the value to be set for the header whose name matches the given name
    * @throws IllegalArgumentException if there was a problem with the specified mime header name or value
    */
   public abstract void setMimeHeader(String name, String value);

   /**
    * Gets the value of the MIME header whose name is "Content-Id".
    * @return a String giving the value of the "Content-Id" header or null if there is none
    */
   public String getContentId()
   {
      return contentId;
   }

   /**
    * Sets the MIME header whose name is "Content-Id" with the given value.
    * @param contentId a String giving the value of the "Content-Id" header
    * @throws IllegalArgumentException if there was a problem with the specified contentId value
    */
   public void setContentId(String contentId)
   {
      this.contentId = contentId;
   }

   /**
    * Gets the value of the MIME header whose name is "Content-Location".
    * @return a String giving the value of the "Content-Location" header or null if there is none
    */
   public String getContentLocation()
   {
      return contentLocation;
   }

   /**
    * Sets the MIME header whose name is "Content-Location" with the given value.
    * @throws IllegalArgumentException if there was a problem with the specified content location
    */
   public void setContentLocation(String contentLocation)
   {
      this.contentLocation = contentLocation;
   }

   /**
    * Gets the value of the MIME header whose name is "Content-Type".
    * @return a String giving the value of the "Content-Type" header or null if there is none
    */
   public String getContentType()
   {
      return contentType;
   }

   /**
    * Sets the MIME header whose name is "Content-Type" with the given value.
    * @param contentType a String giving the value of the "Content-Type" header
    * @throws IllegalArgumentException if there was a problem with the specified content type
    */
   public void setContentType(String contentType)
   {
      this.contentType = contentType;
   }
}
