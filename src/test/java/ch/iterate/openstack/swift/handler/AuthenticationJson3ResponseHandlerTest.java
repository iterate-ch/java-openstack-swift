package ch.iterate.openstack.swift.handler;

import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;

import java.net.URI;
import java.nio.charset.Charset;

import ch.iterate.openstack.swift.AuthenticationResponse;
import ch.iterate.openstack.swift.model.Region;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class AuthenticationJson3ResponseHandlerTest {

    @Test
    public void testHandleResponse() throws Exception {
        final AuthenticationResponse response = new AuthenticationJson3ResponseHandler().handleResponse(new BasicHttpResponse(
                new BasicStatusLine(new ProtocolVersion("http", 1, 1), 201, "OK")
        ) {
            @Override
            public HttpEntity getEntity() {
                return new StringEntity(
                        "{\n" +
                                "    \"token\": {\n" +
                                "        \"expires_at\": \"2013-02-27T18:30:59.999999Z\",\n" +
                                "        \"issued_at\": \"2013-02-27T16:30:59.999999Z\",\n" +
                                "        \"audit_ids\": [\"VcxU2JYqT8OzfUVvrjEITQ\", \"qNUTIJntTzO1-XUk5STybw\"],\n" +
                                "        \"methods\": [\n" +
                                "            \"password\"\n" +
                                "        ],\n" +
                                "        \"user\": {\n" +
                                "            \"domain\": {\n" +
                                "                \"id\": \"1789d1\",\n" +
                                "                \"name\": \"example.com\"\n" +
                                "            },\n" +
                                "            \"id\": \"0ca8f6\",\n" +
                                "            \"name\": \"Joe\"\n" +
                                "        }\n" +
                                "    }\n" +
                                "}"
                        , Charset.forName("UTF-8"));
            }
        });
        assertEquals("0ca8f6", response.getAuthToken());
    }

    @Test
    public void testHandleResponseWithServices() throws Exception {
        final AuthenticationResponse response = new AuthenticationJson3ResponseHandler().handleResponse(new BasicHttpResponse(
                new BasicStatusLine(new ProtocolVersion("http", 1, 1), 201, "OK")
        ) {
            @Override
            public HttpEntity getEntity() {
                return new StringEntity(
                        "{\n" +
                                "    \"token\": {\n" +
                                "        \"expires_at\": \"2013-02-27T18:30:59.999999Z\",\n" +
                                "        \"issued_at\": \"2013-02-27T16:30:59.999999Z\",\n" +
                                "        \"audit_ids\": [\"VcxU2JYqT8OzfUVvrjEITQ\", \"qNUTIJntTzO1-XUk5STybw\"],\n" +
                                "        \"methods\": [\n" +
                                "            \"password\"\n" +
                                "        ],\n" +
                                "        \"user\": {\n" +
                                "            \"domain\": {\n" +
                                "                \"id\": \"1789d1\",\n" +
                                "                \"name\": \"example.com\"\n" +
                                "            },\n" +
                                "            \"id\": \"0ca8f6\",\n" +
                                "            \"name\": \"Joe\"\n" +
                                "        },\n" +
                                "\"catalog\":[\n" +
                                "            {\n" +
                                "                \"id\": \"--service-id--\",\n" +
                                "                \"type\": \"object-store\",\n" +
                                "                \"name\": \"--service-name--\",\n" +
                                "                \"endpoints\": [\n" +
                                "                    {\n" +
                                "                        \"id\": \"--endpoint-id--\",\n" +
                                "                        \"interface\": \"--interface-name--\",\n" +
                                "                        \"region\": \"--region-name--\",\n" +
                                "                        \"url\": \"--endpoint-url--\"\n" +
                                "                    },\n" +
                                "                    {\n" +
                                "                        \"id\": \"--endpoint-id--\",\n" +
                                "                        \"interface\": \"public\",\n" +
                                "                        \"region\": \"my-region\",\n" +
                                "                        \"url\": \"https://storage\"\n" +
                                "                    }\n" +
                                "                ]\n" +
                                "            }\n" +
                                "        ]" +
                                "    }\n" +
                                "}"
                        , Charset.forName("UTF-8"));
            }
        });
        assertTrue(response.getRegions().contains(
                new Region("my-region", URI.create("https://storage"), null, false)
        ));

    }
}