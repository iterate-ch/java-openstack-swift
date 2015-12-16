package ch.iterate.openstack.swift.method;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.net.URI;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class Authentication3UsernamePasswordProjectRequestTest {

    @Test
    public void testAuthenticate() throws Exception {
        final Authentication3UsernamePasswordProjectRequest request
                = new Authentication3UsernamePasswordProjectRequest(URI.create("https://target/auth"),
                "my-username", "my-password", "project-x");

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        request.getEntity().writeTo(out);
        assertEquals("{\"auth\":{\"identity\":{\"methods\":[\"password\"],\"password\":{\"user\":{\"domain\":{\"name\":\"default\"},\"name\":\"my-username\",\"password\":\"my-password\"}}},\"scope\":{\"project\":{\"domain\":{\"name\":\"default\"},\"name\":\"project-x\"}}}}", new String(out.toByteArray(), "UTF-8"));
    }

    @Test
    public void testAuthenticateNoTenant() throws Exception {
        final Authentication3UsernamePasswordProjectRequest request
                = new Authentication3UsernamePasswordProjectRequest(URI.create("https://target/auth"),
                "my-username", "my-password", null);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        request.getEntity().writeTo(out);
        assertEquals("{\"auth\":{\"identity\":{\"methods\":[\"password\"],\"password\":{\"user\":{\"domain\":{\"name\":\"default\"},\"name\":\"my-username\",\"password\":\"my-password\"}}}}}", new String(out.toByteArray(), "UTF-8"));
    }
}