package ch.iterate.openstack.swift.handler;

import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;

import java.util.Collections;

import ch.iterate.openstack.swift.Constants;
import ch.iterate.openstack.swift.model.ContainerMetadata;
import ch.iterate.openstack.swift.model.ObjectMetadata;

import static org.junit.Assert.*;

/**
 * Header names are case-insensitive. Some reverse proxies send them in lower case.
 */
public class MetadataResponseHandlerCaseTest {

    @Test
    public void testContainerMetadataLowerCase() throws Exception {
        final BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 204, "No Content");
        response.addHeader("x-container-meta-quota-bytes", "42949672960");
        response.addHeader("x-container-read", ".r:*");
        response.addHeader("x-container-object-count", "4");
        final ContainerMetadata metadata = new ContainerMetadataResponseHandler().handleResponse(response);
        assertEquals("42949672960", metadata.getMetaData().get("X-Container-Meta-Quota-Bytes"));
        assertEquals(".r:*", metadata.getMetaData().get(Constants.X_CONTAINER_READ));
        assertFalse(metadata.getMetaData().containsKey("X-Container-Object-Count"));
        assertEquals(2, metadata.getMetaData().size());
    }

    @Test
    public void testContainerMetadataCanonicalCase() throws Exception {
        final BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 204, "No Content");
        response.addHeader("X-Container-Meta-Quota-Bytes", "1024");
        final ContainerMetadata metadata = new ContainerMetadataResponseHandler().handleResponse(response);
        assertEquals("1024", metadata.getMetaData().get("X-Container-Meta-Quota-Bytes"));
        // Original header name is kept as key
        assertEquals(Collections.singleton("X-Container-Meta-Quota-Bytes"), metadata.getMetaData().keySet());
    }

    @Test
    public void testObjectMetadataLowerCase() throws Exception {
        final BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "OK");
        response.addHeader("x-object-meta-mtime", "1794346046.123");
        response.addHeader("x-object-manifest", ".file-segments/a/b/");
        response.addHeader("x-static-large-object", "True");
        final ObjectMetadata metadata = new ObjectMetadataResponseHandler().handleResponse(response);
        assertEquals("1794346046.123", metadata.getMetaData().get("X-Object-Meta-Mtime"));
        // Lookups by constant as in Client#listObjectSegments
        assertTrue(metadata.getMetaData().containsKey(Constants.MANIFEST_HEADER));
        assertEquals(".file-segments/a/b/", metadata.getMetaData().get(Constants.MANIFEST_HEADER));
        assertTrue(metadata.getMetaData().containsKey(Constants.X_STATIC_LARGE_OBJECT));
    }

    @Test
    public void testSetMetaDataCaseInsensitive() {
        final ObjectMetadata metadata = new ObjectMetadata(null, null, null, null);
        metadata.setMetaData(Collections.singletonMap("x-object-meta-a", "1"));
        assertEquals("1", metadata.getMetaData().get("X-Object-Meta-A"));
        metadata.setMetaData(null);
        assertTrue(metadata.getMetaData().isEmpty());
    }
}
