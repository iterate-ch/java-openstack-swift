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
import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.model.Region;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AuthenticationJson11ResponseHandlerTest {

    @Test(expected = AuthorizationException.class)
    public void testFailure() throws Exception {
        new AuthenticationJson11ResponseHandler().handleResponse(new BasicHttpResponse(
                new BasicStatusLine(new ProtocolVersion("http", 1, 1), 403, "OK")
        ));
    }

    @Test
    public void testGetToken() throws Exception {
        final AuthenticationResponse r = new AuthenticationJson11ResponseHandler().handleResponse(new BasicHttpResponse(
                new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, "OK")
        ) {
            @Override
            public HttpEntity getEntity() {
                return new StringEntity("{\n" +
                        "    \"auth\" : {\n" +
                        "        \"token\" : {\n" +
                        "            \"id\" : \"asdasdasd-adsasdads-asdasdasd-adsadsasd\",\n" +
                        "            \"expires\" : \"2010-11-01T03:32:15-05:00\"\n" +
                        "        },\n" +
                        "        \"serviceCatalog\" : {\n" +
                        "            \"cloudFiles\" : [\n" +
                        "                {\n" +
                        "                    \"region\" : \"DFW\",\n" +
                        "                    \"v1Default\" : true,\n" +
                        "                    \"publicURL\" : \"https://storage.clouddrive.com/v1/RackCloudFS_demo\",\n" +
                        "                    \"internalURL\" : \"https://storage-snet.clouddrive.com/v1/RackCloudFS_demo\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"region\" : \"ORD\",\n" +
                        "                    \"publicURL\" : \"https://otherstorage.clouddrive.com/v1/RackCloudFS_demo\",\n" +
                        "                    \"internalURL\" : \"https://otherstorage-snet.clouddrive.com/v1/RackCloudFS_demo\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"cloudFilesCDN\" : [\n" +
                        "                {\n" +
                        "                    \"region\" : \"DFW\",\n" +
                        "                    \"v1Default\" : true,\n" +
                        "                    \"publicURL\" : \"https://cdn.clouddrive.com/v1/RackCloudFS_demo\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"region\" : \"ORD\",\n" +
                        "                    \"publicURL\" : \"https://othercdn.clouddrive.com/v1/RackCloudFS_demo\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"cloudServers\" : [\n" +
                        "                {\n" +
                        "                    \"v1Default\" : true,\n" +
                        "                    \"publicURL\" : \"https://servers.api.iterate.com/v1.0/322781\"\n" +
                        "                }\n" +
                        "                        ],\n" +
                        "            \"cloudServersOpenStack\" : [\n" +
                        "                {\n" +
                        "                    \"v1Default\" : false,\n" +
                        "                    \"publicURL\" : \"https://dfw.servers.api.iterate.com/v2/322781\", \n" +
                        "                    \"region\" : \"DFW\"\n" +
                        "                }    \n" +
                        "            ]\n" +
                        "        }\n" +
                        "    }\n" +
                        "}", Charset.forName("UTF-8"));
            }
        });
        assertEquals("asdasdasd-adsasdads-asdasdasd-adsadsasd", r.getAuthToken());
    }

    @Test
    public void testGetToken2() throws Exception {
        final AuthenticationResponse r = new AuthenticationJson11ResponseHandler().handleResponse(new BasicHttpResponse(
                new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, "OK")
        ) {
            @Override
            public HttpEntity getEntity() {
                return new StringEntity("{\"auth\":{\"token\":{\"id\":\"1f00ab25-41b4-458b-b436-9da14179be36\",\"expires\":\"2013-05-22T02:27:48.000-05:00\"},\"serviceCatalog\":{\"cloudServersOpenStack\":[{\"region\":\"ORD\",\"publicURL\":\"https:\\/\\/ord.servers.api.iterate.com\\/v2\\/418003\"},{\"region\":\"DFW\",\"publicURL\":\"https:\\/\\/dfw.servers.api.iterate.com\\/v2\\/418003\"}],\"cloudDNS\":[{\"publicURL\":\"https:\\/\\/dns.api.iterate.com\\/v1.0\\/418003\"}],\"cloudFilesCDN\":[{\"region\":\"DFW\",\"publicURL\":\"https:\\/\\/cdn1.clouddrive.com\\/v1\\/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee\",\"v1Default\":true},{\"region\":\"ORD\",\"publicURL\":\"https:\\/\\/cdn2.clouddrive.com\\/v1\\/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee\"}],\"cloudFiles\":[{\"region\":\"DFW\",\"publicURL\":\"https:\\/\\/storage101.dfw1.clouddrive.com\\/v1\\/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee\",\"v1Default\":true,\"internalURL\":\"https:\\/\\/snet-storage101.dfw1.clouddrive.com\\/v1\\/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee\"},{\"region\":\"ORD\",\"publicURL\":\"https:\\/\\/storage101.ord1.clouddrive.com\\/v1\\/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee\",\"internalURL\":\"https:\\/\\/snet-storage101.ord1.clouddrive.com\\/v1\\/MossoCloudFS_59113590-c679-46c3-bf62-9d7c3d5176ee\"}],\"cloudMonitoring\":[{\"publicURL\":\"https:\\/\\/monitoring.api.iterate.com\\/v1.0\\/418003\"}],\"cloudLoadBalancers\":[{\"region\":\"DFW\",\"publicURL\":\"https:\\/\\/dfw.loadbalancers.api.iterate.com\\/v1.0\\/418003\"},{\"region\":\"ORD\",\"publicURL\":\"https:\\/\\/ord.loadbalancers.api.iterate.com\\/v1.0\\/418003\"}],\"cloudBlockStorage\":[{\"region\":\"ORD\",\"publicURL\":\"https:\\/\\/ord.blockstorage.api.iterate.com\\/v1\\/418003\"},{\"region\":\"DFW\",\"publicURL\":\"https:\\/\\/dfw.blockstorage.api.iterate.com\\/v1\\/418003\"}],\"cloudBackup\":[{\"publicURL\":\"https:\\/\\/backup.api.iterate.com\\/v1.0\\/418003\"}],\"cloudDatabases\":[{\"region\":\"ORD\",\"publicURL\":\"https:\\/\\/ord.databases.api.iterate.com\\/v1.0\\/418003\"},{\"region\":\"DFW\",\"publicURL\":\"https:\\/\\/dfw.databases.api.iterate.com\\/v1.0\\/418003\"}],\"cloudServers\":[{\"publicURL\":\"https:\\/\\/servers.api.iterate.com\\/v1.0\\/418003\",\"v1Default\":true}]}}}", Charset.forName("UTF-8"));
            }
        });
        assertEquals("1f00ab25-41b4-458b-b436-9da14179be36", r.getAuthToken());
    }

    @Test
    public void testGetRegions() throws Exception {
        final AuthenticationResponse r = new AuthenticationJson11ResponseHandler().handleResponse(new BasicHttpResponse(
                new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, "OK")
        ) {
            @Override
            public HttpEntity getEntity() {
                return new StringEntity("{\n" +
                        "    \"auth\" : {\n" +
                        "        \"token\" : {\n" +
                        "            \"id\" : \"asdasdasd-adsasdads-asdasdasd-adsadsasd\",\n" +
                        "            \"expires\" : \"2010-11-01T03:32:15-05:00\"\n" +
                        "        },\n" +
                        "        \"serviceCatalog\" : {\n" +
                        "            \"cloudFiles\" : [\n" +
                        "                {\n" +
                        "                    \"region\" : \"DFW\",\n" +
                        "                    \"v1Default\" : true,\n" +
                        "                    \"publicURL\" : \"https://storage.clouddrive.com/v1/RackCloudFS_demo\",\n" +
                        "                    \"internalURL\" : \"https://storage-snet.clouddrive.com/v1/RackCloudFS_demo\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"region\" : \"ORD\",\n" +
                        "                    \"publicURL\" : \"https://otherstorage.clouddrive.com/v1/RackCloudFS_demo\",\n" +
                        "                    \"internalURL\" : \"https://otherstorage-snet.clouddrive.com/v1/RackCloudFS_demo\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"cloudFilesCDN\" : [\n" +
                        "                {\n" +
                        "                    \"region\" : \"DFW\",\n" +
                        "                    \"v1Default\" : true,\n" +
                        "                    \"publicURL\" : \"https://cdn.clouddrive.com/v1/RackCloudFS_demo\"\n" +
                        "                },\n" +
                        "                {\n" +
                        "                    \"region\" : \"ORD\",\n" +
                        "                    \"publicURL\" : \"https://othercdn.clouddrive.com/v1/RackCloudFS_demo\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"cloudServers\" : [\n" +
                        "                {\n" +
                        "                    \"v1Default\" : true,\n" +
                        "                    \"publicURL\" : \"https://servers.api.iterate.com/v1.0/322781\"\n" +
                        "                }\n" +
                        "                        ],\n" +
                        "            \"cloudServersOpenStack\" : [\n" +
                        "                {\n" +
                        "                    \"v1Default\" : false,\n" +
                        "                    \"publicURL\" : \"https://dfw.servers.api.iterate.com/v2/322781\", \n" +
                        "                    \"region\" : \"DFW\"\n" +
                        "                }    \n" +
                        "            ]\n" +
                        "        }\n" +
                        "    }\n" +
                        "}", Charset.forName("UTF-8"));
            }
        });
        assertTrue(r.getRegions().contains(
                new Region("DFW",
                        URI.create("https://storage.clouddrive.com/v1/RackCloudFS_demo"),
                        URI.create("https://cdn.clouddrive.com/v1/RackCloudFS_demo"), true)
        ));
        assertTrue(r.getRegions().contains(
                new Region("ORD",
                        URI.create("https://otherstorage.clouddrive.com/v1/RackCloudFS_demo"),
                        URI.create("https://othercdn.clouddrive.com/v1/RackCloudFS_demo"), false)
        ));
    }
}
