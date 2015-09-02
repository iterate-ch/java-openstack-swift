package ch.iterate.openstack.swift.handler;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
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
import java.util.logging.Logger;

import ch.iterate.openstack.swift.Response;
import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.ContainerNotFoundException;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.ContainerInfo;
import ch.iterate.openstack.swift.model.Region;

public class ContainerInfoResponseHandler implements ResponseHandler<List<ContainerInfo>> {
    private static final Logger logger = Logger.getLogger(ContainerInfoResponseHandler.class.getName());

    private Region region;

    public ContainerInfoResponseHandler(final Region region) {
        this.region = region;
    }

    public List<ContainerInfo> handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(response.getEntity().getContent());

                NodeList nodes = document.getChildNodes();
                Node accountNode = nodes.item(0);
                if(!"account".equals(accountNode.getNodeName())) {
                    throw new GenericException("Unexpected node name", response.getAllHeaders(), response.getStatusLine());
                }
                List<ContainerInfo> list = new ArrayList<ContainerInfo>();
                NodeList containerNodes = accountNode.getChildNodes();
                for(int i = 0; i < containerNodes.getLength(); ++i) {
                    Node containerNode = containerNodes.item(i);
                    if(!"container".equals(containerNode.getNodeName())) {
                        continue;
                    }
                    String name = null;
                    int count = -1;
                    long size = -1;
                    NodeList objectData = containerNode.getChildNodes();
                    for(int j = 0; j < objectData.getLength(); ++j) {
                        Node data = objectData.item(j);
                        if("name".equals(data.getNodeName())) {
                            name = data.getTextContent();
                        }
                        else if("bytes".equals(data.getNodeName())) {
                            size = Long.parseLong(data.getTextContent());
                        }
                        else if("count".equals(data.getNodeName())) {
                            count = Integer.parseInt(data.getTextContent());
                        }
                        else {
                            logger.warning(String.format("Unexpected node name %s", data.getNodeName()));
                        }
                    }
                    if(name != null) {
                        ContainerInfo info = new ContainerInfo(region, name, count, size);
                        list.add(info);
                    }
                }
                return list;
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
            throw new ContainerNotFoundException(new Response(response));
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthorizationException(new Response(response));
        }
        throw new GenericException(new Response(response));
    }
}
