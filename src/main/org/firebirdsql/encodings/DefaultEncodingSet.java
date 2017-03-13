/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.encodings;

import org.firebirdsql.encodings.xml.EncodingDefinitionType;
import org.firebirdsql.encodings.xml.Encodings;
import org.firebirdsql.encodings.xml.ObjectFactory;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The default encoding set for Jaybird.
 * <p>
 * This {@link EncodingSet} loads the definitions from the file <code>default-firebird-encodings.xml</code> in
 * <code>org.firebirdsql.encodings</code>
 * </p>
 * <p>
 * This class can be subclassed to load other definitions
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class DefaultEncodingSet implements EncodingSet {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEncodingSet.class);
    private List<EncodingDefinition> encodingDefinitions = null;

    @Override
    public int getPreferenceWeight() {
        return 0;
    }

    @Override
    public final synchronized List<EncodingDefinition> getEncodings() {
        if (encodingDefinitions == null) {
            encodingDefinitions = createEncodingDefinitions(getXmlResourceName());
        }
        return encodingDefinitions;
    }

    /**
     * Relative or absolute resource reference to the xml file to load.
     *
     * @return Path of the XML file
     * @see #loadEncodingsFromXml(String)
     */
    protected String getXmlResourceName() {
        return "default-firebird-encodings.xml";
    }

    /**
     * Loads the {@link Encodings} from the specified file.
     * <p>
     * The loaded file must conform to the <code>http://www.firebirdsql.org/schemas/Jaybird/encodings/1</code> schema
     * (as found in <code>org/firebirdsql/encodings/xml/encodings.xsd</code>)
     * </p>
     * <p>
     * This file is loading using <code>getClass().getResourceAsStream(xmlFileResource)</code>
     * </p>
     *
     * @param xmlFileResource
     *         Absolute or relative path of the resource containing the encodings definition
     * @return Loaded encodings, or <code>null</code> if the resource could not be found
     * @throws JAXBException
     *         For errors unmarshalling the objects from the XML file
     */
    protected final Encodings loadEncodingsFromXml(String xmlFileResource) throws JAXBException {
        InputStream inputStream = null;
        try {
            JAXBContext ctx = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                    ObjectFactory.class.getClassLoader());
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            inputStream = getClass().getResourceAsStream(xmlFileResource);
            if (inputStream == null) {
                logger.fatal(String.format("The encoding definition file %s was not found", xmlFileResource));
                return null;
            }
            return (Encodings) unmarshaller.unmarshal(inputStream);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Creates all encodings listed in xmlFileResource.
     *
     * @param xmlFileResource
     *         Absolute or relative path of the resource containing the encodings definition
     * @return List of {@link Encoding} instances
     * @see #loadEncodingsFromXml(String)
     */
    protected final List<EncodingDefinition> createEncodingDefinitions(String xmlFileResource) {
        try {
            Encodings encodings = loadEncodingsFromXml(xmlFileResource);
            if (encodings == null) {
                return Collections.emptyList();
            }
            List<EncodingDefinition> encodingSet = new ArrayList<>();
            for (EncodingDefinitionType definition : encodings.getEncodingDefinition()) {
                final EncodingDefinition encoding = createEncodingDefinition(definition);
                if (encoding != null) {
                    encodingSet.add(encoding);
                }
            }
            return encodingSet;
        } catch (JAXBException e) {
            logger.fatal(String.format("Error loading encoding definition from %s", xmlFileResource), e);
            return Collections.emptyList();
        }
    }

    /**
     * Creates an {@link Encoding} for the <code>definition</code>
     *
     * @param definition
     *         XML definition of the encoding
     * @return Encoding instance or <code>null</code> if creating the instance failed for any reason.
     */
    protected EncodingDefinition createEncodingDefinition(final EncodingDefinitionType definition) {
        try {
            if (definition.getEncodingDefinitionImplementation() != null) {
                return createEncodingDefinitionImplementation(definition);
            } else {
                try {
                    final Charset charset = definition.getJavaName() != null ? Charset.forName(definition.getJavaName()) : null;
                    return new DefaultEncodingDefinition(definition.getFirebirdName(), charset, definition.getMaxBytesPerCharacter(), definition.getCharacterSetId(), definition.isFirebirdOnly());
                } catch (IllegalCharsetNameException e) {
                    logger.warn(String.format("javaName=\"%s\" specified for encoding \"%s\" is an illegal character set name, skipping encoding",
                            definition.getJavaName(), definition.getFirebirdName()), e);
                } catch (UnsupportedCharsetException e) {
                    logger.warn(String.format("javaName=\"%s\" specified for encoding \"%s\" is not supported by the jvm, creating information-only EncodingDefinition",
                            definition.getJavaName(), definition.getFirebirdName()));
                    // Create an 'information-only' definition by using null for charset
                    return new DefaultEncodingDefinition(definition.getFirebirdName(), null, definition.getMaxBytesPerCharacter(), definition.getCharacterSetId(), definition.isFirebirdOnly());
                }
            }
        } catch (Exception e) {
            logger.warn(String.format("Loading information for encoding \"%s\" failed with an Exception", definition.getFirebirdName()), e);
        }
        return null;
    }

    /**
     * Creates an instance of {@link EncodingDefinition} by creating an instance of the class specified by
     * encodingDefinitionImplementation in
     * the xml definition.
     *
     * @param definition
     *         XML definition of the encoding
     * @return Instance of Encoding, or <code>null</code> if the specified class could not be loaded or did not meet
     *         the
     *         expectations.
     */
    protected EncodingDefinition createEncodingDefinitionImplementation(final EncodingDefinitionType definition) {
        assert definition.getEncodingDefinitionImplementation() != null;
        try {
            final Class<?> encodingClazz = Class.forName(definition.getEncodingDefinitionImplementation());
            if (!EncodingDefinition.class.isAssignableFrom(encodingClazz)) {
                logger.warn(String.format("encodingDefinitionImplementation=\"%s\" specified for encoding \"%s\" is not an implementation of org.firebirdsql.encodings.EncodingDefinition",
                        definition.getEncodingDefinitionImplementation(), definition.getFirebirdName()));
            }

            final EncodingDefinition encoding = (EncodingDefinition) encodingClazz.newInstance();
            if (encoding.getFirebirdEncodingName().equals(definition.getFirebirdName())) {
                return encoding;
            } else {
                logger.warn(String.format("Property value FirebirdEncodingName \"%s\" of encodingDefinitionImplementation=\"%s\" specified for encoding \"%s\" does not match",
                        encoding.getFirebirdEncodingName(), definition.getEncodingDefinitionImplementation(), definition.getFirebirdName()));
                return null;
            }
        } catch (ClassNotFoundException e) {
            logger.warn(String.format("encodingDefinitionImplementation=\"%s\" specified for encoding \"%s\" could not be found",
                    definition.getEncodingDefinitionImplementation(), definition.getFirebirdName()), e);
        } catch (InstantiationException e) {
            logger.warn(String.format("encodingDefinitionImplementation=\"%s\" specified for encoding \"%s\" is abstract or does not have a no-arg constructor",
                    definition.getEncodingDefinitionImplementation(), definition.getFirebirdName()), e);
        } catch (IllegalAccessException e) {
            logger.warn(String.format("encodingDefinitionImplementation=\"%s\" specified for encoding \"%s\" or its constructor is not accessible",
                    definition.getEncodingDefinitionImplementation(), definition.getFirebirdName()), e);
        }
        return null;
    }
}
