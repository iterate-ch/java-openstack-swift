package ch.iterate.openstack.swift.handler;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import ch.iterate.openstack.swift.AuthenticationResponse;
import ch.iterate.openstack.swift.Response;
import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.GenericException;

public class AuthenticationXml11ResponseHandler implements ResponseHandler<AuthenticationResponse> {

    public AuthenticationResponse handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        if(response.getStatusLine().getStatusCode() == 200 ||
                response.getStatusLine().getStatusCode() == 203) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
            }
            catch(ParserConfigurationException e) {
                throw new IOException(e.getMessage(), e);
            }
            try {
                Document document = builder.parse(response.getEntity().getContent());
                return new AuthenticationResponse(response, null, null);
            }
            catch(SAXException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
        else if(response.getStatusLine().getStatusCode() == 401 || response.getStatusLine().getStatusCode() == 403) {
            throw new AuthorizationException(new Response(response));
        }
        else {
            throw new GenericException(new Response(response));
        }
    }
}
