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

public class AuthenticationJson20ResponseHandlerTest {

    @Test(expected = AuthorizationException.class)
    public void testFailure() throws Exception {
        new AuthenticationJson20ResponseHandler().handleResponse(new BasicHttpResponse(
                new BasicStatusLine(new ProtocolVersion("http", 1, 1), 403, "OK")
        ));
    }

    @Test
    public void testGetToken() throws Exception {
        final AuthenticationResponse response = new AuthenticationJson20ResponseHandler().handleResponse(new BasicHttpResponse(
                new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, "OK")
        ) {
            @Override
            public HttpEntity getEntity() {
                return new StringEntity("{\n" +
                        "    \"access\":{\n" +
                        "        \"token\":{\n" +
                        "            \"expires\":\"2012-02-05T00:00:00\",\n" +
                        "            \"id\":\"887665443383838\",\n" +
                        "            \"tenant\":{\n" +
                        "                \"id\":\"1\",\n" +
                        "                \"name\":\"customer-x\"\n" +
                        "            }\n" +
                        "        },\n" +
                        "        \"serviceCatalog\":[\n" +
                        "            {\n" +
                        "                \"endpoints\":[\n" +
                        "                {\n" +
                        "                    \"adminURL\":\"http://swift.admin-nets.local:8080/\",\n" +
                        "                    \"region\":\"RegionOne\",\n" +
                        "                    \"internalURL\":\"http://127.0.0.1:8080/v1/AUTH_1\",\n" +
                        "                    \"publicURL\":\"http://swift.publicinternets.com/v1/AUTH_1\"\n" +
                        "                }\n" +
                        "                ],\n" +
                        "                \"type\":\"object-store\",\n" +
                        "                \"name\":\"swift\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "                \"endpoints\":[\n" +
                        "                {\n" +
                        "                    \"adminURL\":\"http://cdn.admin-nets.local/v1.1/1\",\n" +
                        "                    \"region\":\"RegionOne\",\n" +
                        "                    \"internalURL\":\"http://127.0.0.1:7777/v1.1/1\",\n" +
                        "                    \"publicURL\":\"http://cdn.publicinternets.com/v1.1/1\"\n" +
                        "                }\n" +
                        "                ],\n" +
                        "                \"type\":\"object-store\",\n" +
                        "                \"name\":\"cdn\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "        \"user\":{\n" +
                        "            \"id\":\"1\",\n" +
                        "            \"roles\":[\n" +
                        "                {\n" +
                        "                \"tenantId\":\"1\",\n" +
                        "                \"id\":\"3\",\n" +
                        "                \"name\":\"Member\"\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"name\":\"joeuser\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n", Charset.forName("UTF-8"));
            }
        });
        assertEquals("887665443383838", response.getAuthToken());
    }

    @Test
    public void testGetRegionsRackspace() throws Exception {
        final AuthenticationResponse r = new AuthenticationJson20ResponseHandler().handleResponse(new BasicHttpResponse(
                new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, "OK")
        ) {
            @Override
            public HttpEntity getEntity() {
                return new StringEntity("{\n" +
                        "   \"access\": {\n" +
                        "       \"serviceCatalog\": [\n" +
                        "            {\n" +
                        "               \"endpoints\": [\n" +
                        "                   {\n" +
                        "                       \"publicURL\":\"https://ord.servers.api.iterate.com/v2/12345\",\n" +
                        "                       \"region\":\"ORD\",\n" +
                        "                       \"tenantId\":\"12345\",\n" +
                        "                       \"versionId\":\"2\",\n" +
                        "                       \"versionInfo\":\"https://ord.servers.api.iterate.com/v2\",\n" +
                        "                       \"versionList\":\"https://ord.servers.api.iterate.com/\"\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                       \"publicURL\":\"https://dfw.servers.api.iterate.com/v2/12345\",\n" +
                        "                       \"region\":\"DFW\",\n" +
                        "                       \"tenantId\":\"12345\",\n" +
                        "                       \"versionId\":\"2\",\n" +
                        "                       \"versionInfo\":\"https://dfw.servers.api.iterate.com/v2\",\n" +
                        "                       \"versionList\":\"https://dfw.servers.api.iterate.com/\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "               \"name\":\"cloudServersOpenStack\",\n" +
                        "               \"type\":\"compute\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "               \"endpoints\": [\n" +
                        "                    {\n" +
                        "                       \"publicURL\":\"https://ord.databases.api.iterate.com/v1.0/12345\",\n" +
                        "                       \"region\":\"ORD\",\n" +
                        "                       \"tenantId\":\"12345\"\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                       \"publicURL\":\"https://dfw.databases.api.iterate.com/v1.0/12345\",\n" +
                        "                       \"region\":\"DFW\",\n" +
                        "                       \"tenantId\":\"12345\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "               \"name\":\"cloudDatabases\",\n" +
                        "               \"type\":\"rax:database\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "               \"endpoints\": [\n" +
                        "                    {\n" +
                        "                       \"publicURL\":\"https://ord.loadbalancers.api.iterate.com/v1.0/12345\",\n" +
                        "                       \"region\":\"ORD\",\n" +
                        "                       \"tenantId\":\"645990\"\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                       \"publicURL\":\"https://dfw.loadbalancers.api.iterate.com/v1.0/12345\",\n" +
                        "                       \"region\":\"DFW\",\n" +
                        "                       \"tenantId\":\"12345\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "               \"name\":\"cloudLoadBalancers\",\n" +
                        "               \"type\":\"rax:load-balancer\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "               \"endpoints\": [\n" +
                        "                    {\n" +
                        "                       \"publicURL\":\"https://cdn1.clouddrive.com/v1/MossoCloudFS_aaaa-bbbb-cccc\",\n" +
                        "                       \"region\":\"DFW\",\n" +
                        "                       \"tenantId\":\"MossoCloudFS_aaaa-bbbb-cccc\"\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                       \"publicURL\":\"https://cdn2.clouddrive.com/v1/MossoCloudFS_aaaa-bbbb-cccc\",\n" +
                        "                       \"region\":\"ORD\",\n" +
                        "                       \"tenantId\":\"MossoCloudFS_aaaa-bbbb-cccc\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "               \"name\":\"cloudFilesCDN\",\n" +
                        "               \"type\":\"rax:object-cdn\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "               \"endpoints\": [\n" +
                        "                    {\n" +
                        "                       \"publicURL\":\"https://dns.api.iterate.com/v1.0/12345\",\n" +
                        "                       \"tenantId\":\"12345\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "               \"name\":\"cloudDNS\",\n" +
                        "               \"type\":\"rax:dns\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "               \"endpoints\": [\n" +
                        "                    {\n" +
                        "                       \"publicURL\":\"https://servers.api.iterate.com/v1.0/12345\",\n" +
                        "                       \"tenantId\":\"12345\",\n" +
                        "                       \"versionId\":\"1.0\",\n" +
                        "                       \"versionInfo\":\"https://servers.api.iterate.com/v1.0\",\n" +
                        "                       \"versionList\":\"https://servers.api.iterate.com/\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "               \"name\":\"cloudServers\",\n" +
                        "               \"type\":\"compute\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "               \"endpoints\": [\n" +
                        "                    {\n" +
                        "                       \"publicURL\":\"https://monitoring.api.iterate.com/v1.0/12345\",\n" +
                        "                       \"tenantId\":\"12345\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "               \"name\":\"cloudMonitoring\",\n" +
                        "               \"type\":\"rax:monitor\"\n" +
                        "            },\n" +
                        "            {\n" +
                        "               \"endpoints\": [\n" +
                        "                    {\n" +
                        "                       \"internalURL\":\"https://snet-storage101.dfw1.clouddrive.com/v1/MossoCloudFS_aaaa-bbbb-cccc\",\n" +
                        "                       \"publicURL\":\"https://storage101.dfw1.clouddrive.com/v1/MossoCloudFS_aaaa-bbbb-cccc\",\n" +
                        "                       \"region\":\"DFW\",\n" +
                        "                       \"tenantId\":\"MossoCloudFS_aaaa-bbbb-cccc\"\n" +
                        "                    },\n" +
                        "                    {\n" +
                        "                       \"internalURL\":\"https://snet-storage101.ord1.clouddrive.com/v1/MossoCloudFS_aaaa-bbbb-cccc\",\n" +
                        "                       \"publicURL\":\"https://storage101.ord1.clouddrive.com/v1/MossoCloudFS_aaaa-bbbb-cccc\",\n" +
                        "                       \"region\":\"ORD\",\n" +
                        "                       \"tenantId\":\"MossoCloudFS_aaaa-bbbb-cccc\"\n" +
                        "                    }\n" +
                        "                ],\n" +
                        "               \"name\":\"cloudFiles\",\n" +
                        "               \"type\":\"object-store\"\n" +
                        "            }\n" +
                        "        ],\n" +
                        "       \"token\": {\n" +
                        "           \"expires\":\"2012-04-13T13:15:00.000-05:00\",\n" +
                        "           \"id\":\"aaaaa-bbbbb-ccccc-dddd\"\n" +
                        "        },\n" +
                        "       \"user\": {\n" +
                        "       \"RAX-AUTH:defaultRegion\":\"DFW\",\n" +
                        "           \"id\":\"161418\",\n" +
                        "           \"name\":\"demoauthor\",\n" +
                        "           \"roles\": [\n" +
                        "                {\n" +
                        "                   \"description\":\"User Admin Role.\",\n" +
                        "                   \"id\":\"3\",\n" +
                        "                   \"name\":\"identity:user-admin\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    }\n" +
                        "}", Charset.forName("UTF-8"));
            }
        });
        assertTrue(r.getRegions().contains(
                new Region("DFW",
                        URI.create("https://storage101.dfw1.clouddrive.com/v1/MossoCloudFS_aaaa-bbbb-cccc"),
                        URI.create("https://cdn1.clouddrive.com/v1/MossoCloudFS_aaaa-bbbb-cccc"), false)
        ));
        assertTrue(r.getRegions().contains(
                new Region("ORD",
                        URI.create("https://storage101.ord1.clouddrive.com/v1/MossoCloudFS_aaaa-bbbb-cccc"),
                        URI.create("https://cdn2.clouddrive.com/v1/MossoCloudFS_aaaa-bbbb-cccc"), false)
        ));
    }


    @Test
    public void testGetRegionsHpcloud() throws Exception {
        final AuthenticationResponse r = new AuthenticationJson20ResponseHandler().handleResponse(new BasicHttpResponse(
                new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, "OK")
        ) {
            @Override
            public HttpEntity getEntity() {
                return new StringEntity("{\n" +
                        "    \"access\": {\n" +
                        "        \"serviceCatalog\": [\n" +
                        "            {\n" +
                        "                \"endpoints\": [\n" +
                        "                    {\n" +
                        "                        \"publicURL\": \"https://region-a.geo-1.cdnmgmt.hpcloudsvc.com/v1.0/88650632417788\", \n" +
                        "                        \"region\": \"region-a.geo-1\", \n" +
                        "                        \"tenantId\": \"88650632417788\", \n" +
                        "                        \"versionId\": \"1.0\", \n" +
                        "                        \"versionInfo\": \"https://region-a.geo-1.cdnmgmt.hpcloudsvc.com/v1.0/\", \n" +
                        "                        \"versionList\": \"https://region-a.geo-1.cdnmgmt.hpcloudsvc.com/\"\n" +
                        "                    }, \n" +
                        "                    {\n" +
                        "                        \"publicURL\": \"https://region-b.geo-1.cdnmgmt.hpcloudsvc.com/v1.0/88650632417788\", \n" +
                        "                        \"region\": \"region-b.geo-1\", \n" +
                        "                        \"tenantId\": \"88650632417788\", \n" +
                        "                        \"versionId\": \"1.0\", \n" +
                        "                        \"versionInfo\": \"https://region-b.geo-1.cdnmgmt.hpcloudsvc.com/v1.0/\", \n" +
                        "                        \"versionList\": \"https://region-b.geo-1.cdnmgmt.hpcloudsvc.com/\"\n" +
                        "                    }\n" +
                        "                ], \n" +
                        "                \"name\": \"CDN\", \n" +
                        "                \"type\": \"hpext:cdn\"\n" +
                        "            }, \n" +
                        "            {\n" +
                        "                \"endpoints\": [\n" +
                        "                    {\n" +
                        "                        \"publicURL\": \"https://region-a.geo-1.objects.hpcloudsvc.com/v1/88650632417788\", \n" +
                        "                        \"region\": \"region-a.geo-1\", \n" +
                        "                        \"tenantId\": \"88650632417788\", \n" +
                        "                        \"versionId\": \"1.0\", \n" +
                        "                        \"versionInfo\": \"https://region-a.geo-1.objects.hpcloudsvc.com/v1.0/\", \n" +
                        "                        \"versionList\": \"https://region-a.geo-1.objects.hpcloudsvc.com\"\n" +
                        "                    }, \n" +
                        "                    {\n" +
                        "                        \"publicURL\": \"https://region-b.geo-1.objects.hpcloudsvc.com:443/v1/88650632417788\", \n" +
                        "                        \"region\": \"region-b.geo-1\", \n" +
                        "                        \"tenantId\": \"88650632417788\", \n" +
                        "                        \"versionId\": \"1\", \n" +
                        "                        \"versionInfo\": \"https://region-b.geo-1.objects.hpcloudsvc.com:443/v1/\", \n" +
                        "                        \"versionList\": \"https://region-b.geo-1.objects.hpcloudsvc.com:443\"\n" +
                        "                    }\n" +
                        "                ], \n" +
                        "                \"name\": \"Object Storage\", \n" +
                        "                \"type\": \"object-store\"\n" +
                        "            }, \n" +
                        "            {\n" +
                        "                \"endpoints\": [\n" +
                        "                    {\n" +
                        "                        \"publicURL\": \"https://region-a.geo-1.identity.hpcloudsvc.com:35357/v2.0/\", \n" +
                        "                        \"region\": \"region-a.geo-1\", \n" +
                        "                        \"versionId\": \"2.0\", \n" +
                        "                        \"versionInfo\": \"https://region-a.geo-1.identity.hpcloudsvc.com:35357/v2.0/\", \n" +
                        "                        \"versionList\": \"https://region-a.geo-1.identity.hpcloudsvc.com:35357\"\n" +
                        "                    }, \n" +
                        "                    {\n" +
                        "                        \"publicURL\": \"https://region-a.geo-1.identity.hpcloudsvc.com:35357/v3/\", \n" +
                        "                        \"region\": \"region-a.geo-1\", \n" +
                        "                        \"versionId\": \"3.0\", \n" +
                        "                        \"versionInfo\": \"https://region-a.geo-1.identity.hpcloudsvc.com:35357/v3/\", \n" +
                        "                        \"versionList\": \"https://region-a.geo-1.identity.hpcloudsvc.com:35357\"\n" +
                        "                    }, \n" +
                        "                    {\n" +
                        "                        \"publicURL\": \"https://region-b.geo-1.identity.hpcloudsvc.com:35357/v2.0/\", \n" +
                        "                        \"region\": \"region-b.geo-1\", \n" +
                        "                        \"versionId\": \"2.0\", \n" +
                        "                        \"versionInfo\": \"https://region-b.geo-1.identity.hpcloudsvc.com:35357/v2.0/\", \n" +
                        "                        \"versionList\": \"https://region-b.geo-1.identity.hpcloudsvc.com:35357\"\n" +
                        "                    }, \n" +
                        "                    {\n" +
                        "                        \"publicURL\": \"https://region-b.geo-1.identity.hpcloudsvc.com:35357/v3/\", \n" +
                        "                        \"region\": \"region-b.geo-1\", \n" +
                        "                        \"versionId\": \"3.0\", \n" +
                        "                        \"versionInfo\": \"https://region-b.geo-1.identity.hpcloudsvc.com:35357/v3/\", \n" +
                        "                        \"versionList\": \"https://region-b.geo-1.identity.hpcloudsvc.com:35357\"\n" +
                        "                    }\n" +
                        "                ], \n" +
                        "                \"name\": \"Identity\", \n" +
                        "                \"type\": \"identity\"\n" +
                        "            }\n" +
                        "        ], \n" +
                        "        \"token\": {\n" +
                        "            \"expires\": \"2013-06-12T02:17:11.578Z\", \n" +
                        "            \"id\": \"HPAuth10_cd5c7f8bc6e649dcb01cf34c6278602924b55ab6857f694611735847b2be4e71\", \n" +
                        "            \"tenant\": {\n" +
                        "                \"id\": \"88650632417788\", \n" +
                        "                \"name\": \"dkocher@cyberduck.ch-tenant1\"\n" +
                        "            }\n" +
                        "        }, \n" +
                        "        \"user\": {\n" +
                        "            \"id\": \"71697489570738\", \n" +
                        "            \"name\": \"dkocher@cyberduck.ch\", \n" +
                        "            \"otherAttributes\": {\n" +
                        "                \"domainStatus\": \"enabled\"\n" +
                        "            }, \n" +
                        "            \"roles\": [\n" +
                        "                {\n" +
                        "                    \"id\": \"00000000004004\", \n" +
                        "                    \"name\": \"domainuser\", \n" +
                        "                    \"serviceId\": \"100\"\n" +
                        "                }, \n" +
                        "                {\n" +
                        "                    \"id\": \"00000000004022\", \n" +
                        "                    \"name\": \"Admin\", \n" +
                        "                    \"serviceId\": \"110\", \n" +
                        "                    \"tenantId\": \"88650632417788\"\n" +
                        "                }, \n" +
                        "                {\n" +
                        "                    \"id\": \"00000000004014\", \n" +
                        "                    \"name\": \"cdn-admin\", \n" +
                        "                    \"serviceId\": \"150\", \n" +
                        "                    \"tenantId\": \"88650632417788\"\n" +
                        "                }, \n" +
                        "                {\n" +
                        "                    \"id\": \"00000000004003\", \n" +
                        "                    \"name\": \"domainadmin\", \n" +
                        "                    \"serviceId\": \"100\"\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n", Charset.forName("UTF-8"));
            }
        });
        assertTrue(r.getRegions().contains(
                new Region("region-a.geo-1",
                        URI.create("https://region-a.geo-1.objects.hpcloudsvc.com/v1/88650632417788"),
                        URI.create("https://region-a.geo-1.cdnmgmt.hpcloudsvc.com/v1.0/88650632417788"), false)
        ));
        assertTrue(r.getRegions().contains(
                new Region("region-b.geo-1",
                        URI.create("https://region-b.geo-1.objects.hpcloudsvc.com:443/v1/88650632417788"),
                        URI.create("https://region-b.geo-1.cdnmgmt.hpcloudsvc.com/v1.0/88650632417788"), false)
        ));
    }
}
