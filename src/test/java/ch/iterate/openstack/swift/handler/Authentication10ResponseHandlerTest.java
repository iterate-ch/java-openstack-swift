package ch.iterate.openstack.swift.handler;

import org.apache.http.Header;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;

import ch.iterate.openstack.swift.AuthenticationResponse;
import ch.iterate.openstack.swift.exception.AuthorizationException;

import static org.junit.Assert.assertEquals;

public class Authentication10ResponseHandlerTest {

    @Test(expected = AuthorizationException.class)
    public void testFailure() throws Exception {
        new Authentication10ResponseHandler().handleResponse(new BasicHttpResponse(
                new BasicStatusLine(new ProtocolVersion("http", 1, 1), 403, "OK")
        ));
    }


    @Test
    public void testGetToken() throws Exception {
        final AuthenticationResponse response = new Authentication10ResponseHandler().handleResponse(new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "OK") {
            @Override
            public Header getFirstHeader(final String name) {
                return new BasicHeader("X-Auth-Token", "eaaafd18-0fed-4b3a-81b4-663c99ec1cbb");
            }
        });
        assertEquals("eaaafd18-0fed-4b3a-81b4-663c99ec1cbb", response.getAuthToken());
    }

    @Test
    public void testGetTokenNoContent() throws Exception {
        final AuthenticationResponse response = new Authentication10ResponseHandler().handleResponse(new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 204, "") {
            @Override
            public Header getFirstHeader(final String name) {
                return new BasicHeader("X-Auth-Token", "48ecf2ca-8978-4d48-8783-b98ae811a062");
            }
        });
        assertEquals("48ecf2ca-8978-4d48-8783-b98ae811a062", response.getAuthToken());
    }
}
