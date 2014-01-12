package ch.iterate.openstack.swift.handler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.iterate.openstack.swift.Response;
import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.exception.NotFoundException;
import ch.iterate.openstack.swift.model.StorageObject;

public class ObjectResponseHandler implements ResponseHandler<List<StorageObject>> {
    private static final Logger logger = Logger.getLogger(ObjectResponseHandler.class);

    public List<StorageObject> handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(response.getEntity().getContent());

                NodeList nodes = document.getChildNodes();
                Node containerList = nodes.item(0);
                if(!"container".equals(containerList.getNodeName())) {
                    throw new GenericException("Unexpected node name", response.getAllHeaders(), response.getStatusLine());
                }
                List<StorageObject> objectList = new ArrayList<StorageObject>();
                NodeList objectNodes = containerList.getChildNodes();
                for(int i = 0; i < objectNodes.getLength(); ++i) {
                    Node objectNode = objectNodes.item(i);
                    String nodeName = objectNode.getNodeName();
                    if(!("object".equals(nodeName) || "subdir".equals(nodeName))) {
                        continue;
                    }
                    String name = null;
                    String eTag = null;
                    long size = -1;
                    String mimeType = null;
                    String lastModified = null;
                    NodeList objectData = objectNode.getChildNodes();
                    if("subdir".equals(nodeName)) {
                        size = 0;
                        mimeType = "application/directory";
                        name = objectNode.getAttributes().getNamedItem("name").getNodeValue();
                    }
                    for(int j = 0; j < objectData.getLength(); ++j) {
                        Node data = objectData.item(j);
                        if("name".equals(data.getNodeName())) {
                            name = data.getTextContent();
                        }
                        else if("content_type".equals(data.getNodeName())) {
                            mimeType = data.getTextContent();
                        }
                        else if("hash".equals(data.getNodeName())) {
                            eTag = data.getTextContent();
                        }
                        else if("bytes".equals(data.getNodeName())) {
                            size = Long.parseLong(data.getTextContent());
                        }
                        else if("last_modified".equals(data.getNodeName())) {
                            lastModified = data.getTextContent();
                        }
                        else {
                            logger.warn("Unexpected tag:" + data.getNodeName());
                        }
                    }
                    if(name != null) {
                        StorageObject obj = new StorageObject(name);
                        obj.setMd5sum(eTag);
                        obj.setMimeType(mimeType);
                        obj.setSize(size);
                        obj.setLastModified(lastModified);
                        objectList.add(obj);
                    }
                }
                return objectList;
            }
            catch(ParserConfigurationException e) {
                throw new GenericException("Parser configuration failure", e);
            }
            catch(SAXException e) {
                throw new GenericException("Error parsing XML server response", e);
            }
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            return Collections.emptyList();
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            throw new NotFoundException(new Response(response));
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthorizationException(new Response(response));
        }
        throw new GenericException(new Response(response));
    }
}
