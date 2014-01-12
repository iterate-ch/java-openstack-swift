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
import java.util.List;

import ch.iterate.openstack.swift.Response;
import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.CDNContainer;
import ch.iterate.openstack.swift.model.Region;

public class CdnContainerInfoListHandler implements ResponseHandler<List<CDNContainer>> {
    private static final Logger logger = Logger.getLogger(ContainerInfoResponseHandler.class);

    private Region region;

    public CdnContainerInfoListHandler(final Region region) {
        this.region = region;
    }

    public List<CDNContainer> handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
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
                List<CDNContainer> list = new ArrayList<CDNContainer>();
                NodeList containerNodes = accountNode.getChildNodes();
                for(int i = 0; i < containerNodes.getLength(); ++i) {
                    Node containerNode = containerNodes.item(i);
                    if(!"container".equals(containerNode.getNodeName())) {
                        continue;
                    }
                    CDNContainer container = new CDNContainer(region);
                    NodeList objectData = containerNode.getChildNodes();
                    for(int j = 0; j < objectData.getLength(); ++j) {
                        Node data = objectData.item(j);
                        if("name".equals(data.getNodeName())) {
                            container.setName(data.getTextContent());
                        }
                        else if("cdn_url".equals(data.getNodeName())) {
                            container.setCdnURL(data.getTextContent());
                        }
                        else if("cdn_ssl_url".equals(data.getNodeName())) {
                            container.setSslURL(data.getTextContent());
                        }
                        else if("cdn_streaming_url".equals(data.getNodeName())) {
                            container.setStreamingURL(data.getTextContent());
                        }
                        else if("cdn_ios_url".equals(data.getNodeName())) {
                            container.setiOSStreamingURL(data.getTextContent());
                        }
                        else if("cdn_enabled".equals(data.getNodeName())) {
                            container.setEnabled(Boolean.parseBoolean(data.getTextContent()));
                        }
                        else if("log_retention".equals(data.getNodeName())) {
                            container.setRetainLogs(Boolean.parseBoolean(data.getTextContent()));
                        }
                        else if("ttl".equals(data.getNodeName())) {
                            container.setTtl(Integer.parseInt(data.getTextContent()));
                        }
                        else if("referrer_acl".equals(data.getNodeName())) {
                            container.setReferrerACL(data.getTextContent());
                        }
                        else if("useragent_acl".equals(data.getNodeName())) {
                            container.setUserAgentACL(data.getTextContent());
                        }
                        else {
                            logger.warn(String.format("Unexpected node name %s", data.getNodeName()));
                        }
                    }
                    if(container.getName() != null) {
                        list.add(container);
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
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthorizationException(new Response(response));
        }
        throw new GenericException(new Response(response));
    }
}
