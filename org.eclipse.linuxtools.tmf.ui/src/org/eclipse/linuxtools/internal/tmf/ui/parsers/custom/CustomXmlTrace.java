/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTraceDefinition.InputAttribute;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTraceDefinition.InputElement;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class CustomXmlTrace extends TmfTrace<CustomXmlEvent> {

    private static final TmfLocation<Long> NULL_LOCATION = new TmfLocation<Long>((Long) null);
    private static final int DEFAULT_CACHE_SIZE = 100;

    private final CustomXmlTraceDefinition fDefinition;
    private final CustomXmlEventType fEventType;
    private final InputElement fRecordInputElement;

    public CustomXmlTrace(final CustomXmlTraceDefinition definition) {
        fDefinition = definition;
        fEventType = new CustomXmlEventType(fDefinition);
        fRecordInputElement = getRecordInputElement(fDefinition.rootInputElement);
    }

    public CustomXmlTrace(final IResource resource, final CustomXmlTraceDefinition definition, final String path, final int pageSize) throws FileNotFoundException {
        super(null, CustomXmlEvent.class, path, (pageSize > 0) ? pageSize : DEFAULT_CACHE_SIZE, true);
        fDefinition = definition;
        fEventType = new CustomXmlEventType(fDefinition);
        fRecordInputElement = getRecordInputElement(fDefinition.rootInputElement);
    }

    @Override
    public void initTrace(final IResource resource, final String path, final Class<CustomXmlEvent> eventType) throws FileNotFoundException {
        super.initTrace(resource, path, eventType);
    }

    @Override
    public TmfContext seekLocation(final ITmfLocation<?> location) {
        final CustomXmlTraceContext context = new CustomXmlTraceContext(NULL_LOCATION, ITmfContext.INITIAL_RANK);
        if (NULL_LOCATION.equals(location) || !new File(getPath()).isFile())
            return context;
        try {
            context.raFile = new BufferedRandomAccessFile(getPath(), "r"); //$NON-NLS-1$
            if (location != null && location.getLocation() instanceof Long)
                context.raFile.seek((Long)location.getLocation());

            String line;
            final String recordElementStart = "<" + fRecordInputElement.elementName; //$NON-NLS-1$
            long rawPos = context.raFile.getFilePointer();

            while ((line = context.raFile.getNextLine()) != null) {
                final int idx = line.indexOf(recordElementStart);
                if (idx != -1) {
                    context.setLocation(new TmfLocation<Long>(rawPos + idx));
                    return context;
                }
                rawPos = context.raFile.getFilePointer();
            }
            return context;
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            return context;
        } catch (final IOException e) {
            e.printStackTrace();
            return context;
        }

    }

    @Override
    public TmfContext seekLocation(final double ratio) {
        BufferedRandomAccessFile raFile = null;
        try {
            raFile = new BufferedRandomAccessFile(getPath(), "r"); //$NON-NLS-1$
            long pos = (long) (ratio * raFile.length());
            while (pos > 0) {
                raFile.seek(pos - 1);
                if (raFile.read() == '\n') break;
                pos--;
            }
            final ITmfLocation<?> location = new TmfLocation<Long>(pos);
            final TmfContext context = seekLocation(location);
            context.setRank(ITmfContext.UNKNOWN_RANK);
            return context;
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            return new CustomXmlTraceContext(NULL_LOCATION, ITmfContext.INITIAL_RANK);
        } catch (final IOException e) {
            e.printStackTrace();
            return new CustomXmlTraceContext(NULL_LOCATION, ITmfContext.INITIAL_RANK);
        } finally {
            if (raFile != null)
                try {
                    raFile.close();
                } catch (final IOException e) {
                }
        }
    }

    @Override
    public double getLocationRatio(final ITmfLocation<?> location) {
        RandomAccessFile raFile = null;
        try {
            if (location.getLocation() instanceof Long) {
                raFile = new RandomAccessFile(getPath(), "r"); //$NON-NLS-1$
                return (double) ((Long) location.getLocation()) / raFile.length();
            }
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (raFile != null)
                try {
                    raFile.close();
                } catch (final IOException e) {
                }
        }
        return 0;
    }

    @Override
    public ITmfLocation<?> getCurrentLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized TmfEvent getNextEvent(final ITmfContext context) {
        final ITmfContext savedContext = context.clone();
        final TmfEvent event = parseEvent(context);
        if (event != null) {
            updateIndex(savedContext, savedContext.getRank(), event.getTimestamp());
            context.increaseRank();
        }
        return event;
    }

    @Override
    public TmfEvent parseEvent(final ITmfContext tmfContext) {
        if (!(tmfContext instanceof CustomXmlTraceContext))
            return null;

        final CustomXmlTraceContext context = (CustomXmlTraceContext) tmfContext;
        if (!(context.getLocation().getLocation() instanceof Long) || NULL_LOCATION.equals(context.getLocation()))
            return null;

        synchronized (context.raFile) {
            CustomXmlEvent event = null;
            try {
                if (context.raFile.getFilePointer() != (Long)context.getLocation().getLocation() + 1)
                    context.raFile.seek((Long)context.getLocation().getLocation() + 1); // +1 is for the <
                final StringBuffer elementBuffer = new StringBuffer("<"); //$NON-NLS-1$
                readElement(elementBuffer, context.raFile);
                final Element element = parseElementBuffer(elementBuffer);

                event = extractEvent(element, fRecordInputElement);
                ((StringBuffer) event.getContent().getValue()).append(elementBuffer);

                String line;
                final String recordElementStart = "<" + fRecordInputElement.elementName; //$NON-NLS-1$
                long rawPos = context.raFile.getFilePointer();

                while ((line = context.raFile.getNextLine()) != null) {
                    final int idx = line.indexOf(recordElementStart);
                    if (idx != -1) {
                        context.setLocation(new TmfLocation<Long>(rawPos + idx));
                        return event;
                    }
                    rawPos = context.raFile.getFilePointer();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            context.setLocation(NULL_LOCATION);
            return event;
        }
    }

    private Element parseElementBuffer(final StringBuffer elementBuffer) {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();

            // The following allows xml parsing without access to the dtd
            final EntityResolver resolver = new EntityResolver () {
                @Override
                public InputSource resolveEntity (final String publicId, final String systemId) {
                    final String empty = ""; //$NON-NLS-1$
                    final ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());
                    return new InputSource(bais);
                }
            };
            db.setEntityResolver(resolver);

            // The following catches xml parsing exceptions
            db.setErrorHandler(new ErrorHandler(){
                @Override
                public void error(final SAXParseException saxparseexception) throws SAXException {}
                @Override
                public void warning(final SAXParseException saxparseexception) throws SAXException {}
                @Override
                public void fatalError(final SAXParseException saxparseexception) throws SAXException {
                    throw saxparseexception;
                }});

            final Document doc = db.parse(new ByteArrayInputStream(elementBuffer.toString().getBytes()));
            return doc.getDocumentElement();
        } catch (final ParserConfigurationException e) {
            e.printStackTrace();
        } catch (final SAXException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void readElement(final StringBuffer buffer, final RandomAccessFile raFile) {
        try {
            int numRead = 0;
            boolean startTagClosed = false;
            int i;
            while ((i = raFile.read()) != -1) {
                numRead++;
                final char c = (char)i;
                buffer.append(c);
                if (c == '"')
                    readQuote(buffer, raFile, '"');
                else if (c == '\'')
                    readQuote(buffer, raFile, '\'');
                else if (c == '<')
                    readElement(buffer, raFile);
                else if (c == '/' && numRead == 1)
                    break; // found "</"
                else if (c == '-' && numRead == 3 && buffer.substring(buffer.length() - 3, buffer.length() - 1).equals("!-")) //$NON-NLS-1$
                    readComment(buffer, raFile); // found "<!--"
                else if (i == '>')
                    if (buffer.charAt(buffer.length() - 2) == '/')
                        break; // found "/>"
                    else if (startTagClosed)
                        break; // found "<...>...</...>"
                    else
                        startTagClosed = true; // found "<...>"
            }
            return;
        } catch (final IOException e) {
            return;
        }
    }

    private void readQuote(final StringBuffer buffer, final RandomAccessFile raFile, final char eq) {
        try {
            int i;
            while ((i = raFile.read()) != -1) {
                final char c = (char)i;
                buffer.append(c);
                if (c == eq)
                    break; // found matching end-quote
            }
            return;
        } catch (final IOException e) {
            return;
        }
    }

    private void readComment(final StringBuffer buffer, final RandomAccessFile raFile) {
        try {
            int numRead = 0;
            int i;
            while ((i = raFile.read()) != -1) {
                numRead++;
                final char c = (char)i;
                buffer.append(c);
                if (c == '>' && numRead >= 2 && buffer.substring(buffer.length() - 3, buffer.length() - 1).equals("--")) //$NON-NLS-1$
                    break; // found "-->"
            }
            return;
        } catch (final IOException e) {
            return;
        }
    }

    public static StringBuffer parseElement(final Element parentElement, final StringBuffer buffer) {
        final NodeList nodeList = parentElement.getChildNodes();
        String separator = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (separator == null)
                    separator = " | "; //$NON-NLS-1$
                else
                    buffer.append(separator);
                final Element element = (Element) node;
                if (element.hasChildNodes() == false)
                    buffer.append(element.getNodeName());
                else if (element.getChildNodes().getLength() == 1 && element.getFirstChild().getNodeType() == Node.TEXT_NODE)
                    buffer.append(element.getNodeName() + ":" + element.getFirstChild().getNodeValue().trim()); //$NON-NLS-1$
                else {
                    buffer.append(element.getNodeName());
                    buffer.append(" [ "); //$NON-NLS-1$
                    parseElement(element, buffer);
                    buffer.append(" ]"); //$NON-NLS-1$
                }
            } else if (node.getNodeType() == Node.TEXT_NODE)
                if (node.getNodeValue().trim().length() != 0)
                    buffer.append(node.getNodeValue().trim());
        }
        return buffer;
    }

    public InputElement getRecordInputElement(final InputElement inputElement) {
        if (inputElement.logEntry)
            return inputElement;
        else if (inputElement.childElements != null)
            for (final InputElement childInputElement : inputElement.childElements) {
                final InputElement recordInputElement = getRecordInputElement(childInputElement);
                if (recordInputElement != null)
                    return recordInputElement;
            }
        return null;
    }

    public CustomXmlEvent extractEvent(final Element element, final InputElement inputElement) {
        final CustomXmlEvent event = new CustomXmlEvent(fDefinition, this, TmfTimestamp.ZERO, "", fEventType,""); //$NON-NLS-1$ //$NON-NLS-2$
        event.setContent(new CustomEventContent(event, "")); //$NON-NLS-1$
        parseElement(element, event, inputElement);
        return event;
    }

    private void parseElement(final Element element, final CustomXmlEvent event, final InputElement inputElement) {
        if (inputElement.inputName != null && !inputElement.inputName.equals(CustomXmlTraceDefinition.TAG_IGNORE))
            event.parseInput(parseElement(element, new StringBuffer()).toString(), inputElement.inputName, inputElement.inputAction, inputElement.inputFormat);
        if (inputElement.attributes != null)
            for (final InputAttribute attribute : inputElement.attributes)
                event.parseInput(element.getAttribute(attribute.attributeName), attribute.inputName, attribute.inputAction, attribute.inputFormat);
        final NodeList childNodes = element.getChildNodes();
        if (inputElement.childElements != null)
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node node = childNodes.item(i);
                if (node instanceof Element)
                    for (final InputElement child : inputElement.childElements)
                        if (node.getNodeName().equals(child.elementName)) {
                            parseElement((Element) node, event, child);
                            break;
                        }
            }
        return;
    }

    public CustomTraceDefinition getDefinition() {
        return fDefinition;
    }
}
