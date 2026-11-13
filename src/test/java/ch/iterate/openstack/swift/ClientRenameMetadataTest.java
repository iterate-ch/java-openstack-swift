package ch.iterate.openstack.swift;

import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.iterate.openstack.swift.handler.ContainerMetadataResponseHandler;
import ch.iterate.openstack.swift.handler.ObjectMetadataResponseHandler;

import static org.junit.Assert.*;

/**
 * Metadata read from lower-case response headers and passed back for an update keeps its key.
 */
public class ClientRenameMetadataTest {

    @Test
    public void testContainerReadThenUpdateLowerCase() throws Exception {
        final BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 204, "No Content");
        response.addHeader("x-container-meta-quota-bytes", "42949672960");
        final Map<String, String> read = new ContainerMetadataResponseHandler().handleResponse(response).getMetaData();
        final Map<String, String> update = Client.renameContainerMetadata(read);
        assertEquals(Collections.singletonMap("x-container-meta-quota-bytes", "42949672960"), update);
    }

    @Test
    public void testObjectReadThenUpdateLowerCase() throws Exception {
        final BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion("http", 1, 1), 200, "OK");
        response.addHeader("x-object-meta-mtime", "1794346046.123");
        final Map<String, String> read = new ObjectMetadataResponseHandler().handleResponse(response).getMetaData();
        final Map<String, String> update = Client.renameObjectMetadata(read);
        assertEquals(Collections.singletonMap("x-object-meta-mtime", "1794346046.123"), update);
    }

    @Test
    public void testPrefixMatchIgnoresCase() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("X-Object-Meta-A", "1");
        metadata.put("X-OBJECT-META-B", "2");
        metadata.put("C", "3");
        final Map<String, String> update = Client.renameObjectMetadata(metadata);
        assertEquals("1", update.get("X-Object-Meta-A"));
        assertEquals("2", update.get("X-OBJECT-META-B"));
        // Unprefixed keys still get the prefix
        assertEquals("3", update.get("X-Object-Meta-C"));
        assertEquals(3, update.size());
    }

    @Test
    public void testContainerPrefixMatchIgnoresCase() {
        final Map<String, String> update = Client.renameContainerMetadata(Collections.singletonMap("X-CONTAINER-META-Quota-Bytes", "1"));
        assertEquals(Collections.singletonMap("X-CONTAINER-META-Quota-Bytes", "1"), update);
    }
}
