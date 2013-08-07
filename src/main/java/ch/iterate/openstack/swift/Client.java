/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.ContainerExistsException;
import ch.iterate.openstack.swift.exception.ContainerNotEmptyException;
import ch.iterate.openstack.swift.exception.ContainerNotFoundException;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.exception.NotFoundException;
import ch.iterate.openstack.swift.handler.*;
import ch.iterate.openstack.swift.method.Authentication10UsernameKeyRequest;
import ch.iterate.openstack.swift.method.Authentication11UsernameKeyRequest;
import ch.iterate.openstack.swift.method.Authentication20UsernamePasswordRequest;
import ch.iterate.openstack.swift.method.AuthenticationRequest;
import ch.iterate.openstack.swift.model.AccountInfo;
import ch.iterate.openstack.swift.model.CDNContainer;
import ch.iterate.openstack.swift.model.Container;
import ch.iterate.openstack.swift.model.ContainerInfo;
import ch.iterate.openstack.swift.model.ContainerMetadata;
import ch.iterate.openstack.swift.model.ObjectMetadata;
import ch.iterate.openstack.swift.model.Region;
import ch.iterate.openstack.swift.model.StorageObject;

/**
 * An OpenStack Swift client interface.  Here follows a basic example of logging in, creating a container and an
 * object, retrieving the object, and then deleting both the object and container.  For more examples,
 * see the code in com.iterate.openstack.cloudfiles.sample, which contains a series of examples.
 * <p/>
 * <pre>
 *
 *  //  Create the openstack object for username "jdoe", password "johnsdogsname".
 * 	FilesClient myClient = FilesClient("jdoe", "johnsdogsname");
 *
 *  // Log in (<code>login()</code> will return false if the login was unsuccessful.
 *  assert(myClient.login());
 *
 *  // Make sure there are no containers in the account
 *  assert(myClient.listContainers.length() == 0);
 *
 *  // Create the container
 *  assert(myClient.createContainer("myContainer"));
 *
 *  // Now we should have one
 *  assert(myClient.listContainers.length() == 1);
 *
 *  // Upload the file "alpaca.jpg"
 *  assert(myClient.storeObject("myContainer", new File("alapca.jpg"), "image/jpeg"));
 *
 *  // Download "alpaca.jpg"
 *  FilesObject obj = myClient.getObject("myContainer", "alpaca.jpg");
 *  byte data[] = obj.getObject();
 *
 *  // Clean up after ourselves.
 *  // Note:  Order here is important, you can't delete non-empty containers.
 *  assert(myClient.deleteObject("myContainer", "alpaca.jpg"));
 *  assert(myClient.deleteContainer("myContainer");
 * </pre>
 *
 * @author lvaughn
 */
public class Client {

    private String username;
    private String password;
    private String tenantId;
    private AuthVersion authVersion = AuthVersion.v10;
    private URI authenticationURL;

    private AuthenticationResponse authenticationResponse;

    private HttpClient client;

    /**
     * @param connectionTimeOut The connection timeout, in ms.
     */
    public Client(final int connectionTimeOut) {
        this(new DefaultHttpClient() {
            @Override
            protected HttpParams createHttpParams() {
                BasicHttpParams params = new BasicHttpParams();
                HttpConnectionParams.setSoTimeout(params, connectionTimeOut);
                return params;
            }

            @Override
            protected ClientConnectionManager createClientConnectionManager() {
                SchemeRegistry schemeRegistry = new SchemeRegistry();
                schemeRegistry.register(
                        new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
                schemeRegistry.register(
                        new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
                return new PoolingClientConnectionManager(schemeRegistry);
            }
        });
    }

    /**
     * @param client The HttpClient to talk to Swift
     */
    public Client(HttpClient client) {
        this.client = client;
    }

    /**
     * Release all connections
     */
    public void disconnect() {
        this.client.getConnectionManager().shutdown();
    }

    public enum AuthVersion {
        /**
         * Legacy authentication. ReSTful calls no longer use HTTP headers for request or response parameters.
         * Parameters are now sent via the XML or JSON message body.
         */
        v10,
        /**
         * Legacy authentication. Service endpoint URLs are now capable of specifying a region.
         */
        v11,
        v20
    }

    /**
     * @param authVersion Version
     * @param authUrl     Authentication endpoint of identity service
     * @param username    User or access key
     * @param password    Password or secret key
     * @param tenantId    Tenant or null
     * @return Authentication response with supported regions and authentication token for subsequent requests
     */
    public AuthenticationResponse authenticate(AuthVersion authVersion, URI authUrl, String username, String password, String tenantId) throws IOException {
        this.authenticationURL = authUrl;
        this.authVersion = authVersion;
        this.username = username;
        this.password = password;
        this.tenantId = tenantId;
        return this.authenticate();
    }

    protected AuthenticationResponse authenticate() throws IOException {
        switch(authVersion) {
            case v10:
            default:
                return this.authenticate(new Authentication10UsernameKeyRequest(authenticationURL, username, password));
            case v11:
                return this.authenticate(new Authentication11UsernameKeyRequest(authenticationURL, username, password));
            case v20:
                return this.authenticate(new Authentication20UsernamePasswordRequest(authenticationURL, username, password, tenantId));
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) throws IOException {
        switch(request.getVersion()) {
            case v10:
            default:
                return this.authenticate(request, new Authentication10ResponseHandler());
            case v11:
                return this.authenticate(request, new AuthenticationJson11ResponseHandler());
            case v20:
                return this.authenticate(request, new AuthenticationJson20ResponseHandler());
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request,
                                               ResponseHandler<AuthenticationResponse> handler) throws IOException {
        return authenticationResponse = client.execute(request, handler);
    }

    public AuthenticationResponse getAuthentication() {
        return authenticationResponse;
    }

    public Set<Region> getRegions() {
        return authenticationResponse.getRegions();
    }

    public void setUserAgent(String userAgent) {
        client.getParams().setParameter(HTTP.USER_AGENT, userAgent);
    }

    public String getUserAgent() {
        return client.getParams().getParameter(HTTP.USER_AGENT).toString();
    }

    /**
     * List all of the containers available in an account, ordered by container name.
     *
     * @return null if the user is not logged in or the Account is not found.  A List of FSContainers with all of the containers in the account.
     *         if there are no containers in the account, the list will be zero length.
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *          Unexpected response
     * @throws ch.iterate.openstack.swift.exception.AuthorizationException
     *          The openstack's login was invalid.
     */
    public List<ContainerInfo> listContainersInfo(Region region) throws IOException {
        return listContainersInfo(region, -1, null);
    }

    /**
     * List the containers available in an account, ordered by container name.
     *
     * @param limit The maximum number of containers to return.  -1 returns an unlimited number.
     * @return null if the user is not logged in or the Account is not found.  A List of FSContainers with all of the containers in the account.
     *         if there are no containers in the account, the list will be zero length.
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *          Unexpected response
     * @throws ch.iterate.openstack.swift.exception.AuthorizationException
     *          The openstack's login was invalid.
     */
    public List<ContainerInfo> listContainersInfo(Region region, int limit) throws IOException {
        return listContainersInfo(region, limit, null);
    }

    /**
     * List the containers available in an account, ordered by container name.
     *
     * @param limit  The maximum number of containers to return.  -1 returns an unlimited number.
     * @param marker Return containers that occur after this lexicographically.
     * @return null if the user is not logged in or the Account is not found.  A List of FSContainers with all of the containers in the account.
     *         if there are no containers in the account, the list will be zero length.
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *          Unexpected response
     * @throws ch.iterate.openstack.swift.exception.AuthorizationException
     *          The openstack's login was invalid.
     */
    public List<ContainerInfo> listContainersInfo(Region region, int limit, String marker) throws IOException {
        LinkedList<NameValuePair> parameters = new LinkedList<NameValuePair>();
        if(limit > 0) {
            parameters.add(new BasicNameValuePair("limit", String.valueOf(limit)));
        }
        if(marker != null) {
            parameters.add(new BasicNameValuePair("marker", marker));
        }
        parameters.add(new BasicNameValuePair("format", "xml"));
        HttpGet method = new HttpGet(region.getStorageUrl(parameters));
        return this.execute(method, new ContainerInfoResponseHandler(region));
    }

    /**
     * List the containers available in an account.
     *
     * @return null if the user is not logged in or the Account is not found.  A List of FilesContainer with all of the containers in the account.
     *         if there are no containers in the account, the list will be zero length.
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *          Unexpected response
     * @throws ch.iterate.openstack.swift.exception.AuthorizationException
     *          The openstack's login was invalid.
     */
    public List<Container> listContainers(Region region) throws IOException {
        return listContainers(region, -1, null);
    }

    /**
     * List the containers available in an account.
     *
     * @param limit The maximum number of containers to return.  -1 denotes no limit.
     * @return null if the user is not logged in or the Account is not found.  A List of FilesContainer with all of the containers in the account.
     *         if there are no containers in the account, the list will be zero length.
     * @throws IOException There was an IO error doing network communication
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *                     Unexpected response
     * @throws ch.iterate.openstack.swift.exception.AuthorizationException
     *                     The openstack's login was invalid.
     */
    public List<Container> listContainers(Region region, int limit) throws IOException {
        return listContainers(region, limit, null);
    }

    /**
     * List the containers available in an account.
     *
     * @param limit  The maximum number of containers to return.  -1 denotes no limit.
     * @param marker Only return containers after this container.  Null denotes starting at the beginning (lexicographically).
     * @return A List of FilesContainer with all of the containers in the account.
     *         if there are no containers in the account, the list will be zero length.
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *          Unexpected response
     * @throws ch.iterate.openstack.swift.exception.AuthorizationException
     *          The openstack's login was invalid.
     */
    public List<Container> listContainers(Region region, int limit, String marker) throws IOException {
        LinkedList<NameValuePair> parameters = new LinkedList<NameValuePair>();

        if(limit > 0) {
            parameters.add(new BasicNameValuePair("limit", String.valueOf(limit)));
        }
        if(marker != null) {
            parameters.add(new BasicNameValuePair("marker", marker));
        }
        HttpGet method = new HttpGet(region.getStorageUrl(parameters));
        return this.execute(method, new ContainerResponseHandler(region));
    }

    private Response execute(final HttpRequestBase method) throws IOException {
        try {
            method.setHeader(Constants.X_AUTH_TOKEN, authenticationResponse.getAuthToken());
            try {
                return new DefaultResponseHandler().handleResponse(client.execute(method));
            }
            catch(AuthorizationException e) {
                method.abort();
                authenticationResponse = this.authenticate();
                method.reset();
                // Add new auth token retrieved
                method.setHeader(Constants.X_AUTH_TOKEN, authenticationResponse.getAuthToken());
                // Retry
                return new DefaultResponseHandler().handleResponse(client.execute(method));
            }
        }
        catch(IOException e) {
            // In case of an IOException the connection will be released back to the connection manager automatically
            method.abort();
            throw e;
        }
    }

    private <T> T execute(final HttpRequestBase method, ResponseHandler<T> handler) throws IOException {
        try {
            method.setHeader(Constants.X_AUTH_TOKEN, authenticationResponse.getAuthToken());
            try {
                return client.execute(method, handler);
            }
            catch(AuthorizationException e) {
                method.abort();
                authenticationResponse = this.authenticate();
                method.reset();
                // Add new auth token retrieved
                method.setHeader(Constants.X_AUTH_TOKEN, authenticationResponse.getAuthToken());
                // Retry
                return client.execute(method, handler);
            }
        }
        catch(IOException e) {
            // In case of an IOException the connection will be released back to the connection manager automatically
            method.abort();
            throw e;
        }
        finally {
            method.reset();
        }
    }

    /**
     * List all of the objects in a container with the given starting string.
     *
     * @param container  The container name
     * @param startsWith The string to start with
     * @param path       Only look for objects in this path
     * @param limit      Return at most <code>limit</code> objects
     * @param marker     Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.
     * @return A list of FilesObjects starting with the given string
     * @throws IOException            There was an IO error doing network communication
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *                                Unexpected response
     * @throws AuthorizationException The openstack's login was invalid.
     */
    public List<StorageObject> listObjectsStartingWith(Region region, String container,
                                                       String startsWith, String path, int limit, String marker) throws IOException {
        return listObjectsStartingWith(region, container, startsWith, path, limit, marker, null);
    }

    /**
     * List all of the objects in a container with the given starting string.
     *
     * @param container  The container name
     * @param startsWith The string to start with
     * @param path       Only look for objects in this path
     * @param limit      Return at most <code>limit</code> objects
     * @param marker     Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.
     * @param delimiter  Use this argument as the delimiter that separates "directories"
     * @return A list of FilesObjects starting with the given string
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *                                Unexpected response
     * @throws AuthorizationException The openstack's login was invalid.
     */
    public List<StorageObject> listObjectsStartingWith(Region region, String container, String startsWith, String path, int limit, String marker, Character delimiter) throws IOException {

        LinkedList<NameValuePair> parameters = new LinkedList<NameValuePair>();
        parameters.add(new BasicNameValuePair("format", "xml"));
        if(startsWith != null) {
            parameters.add(new BasicNameValuePair("prefix", startsWith));
        }
        if(path != null) {
            parameters.add(new BasicNameValuePair("path", path));
        }
        if(limit > 0) {
            parameters.add(new BasicNameValuePair("limit", String.valueOf(limit)));
        }
        if(marker != null) {
            parameters.add(new BasicNameValuePair("marker", marker));
        }
        if(delimiter != null) {
            parameters.add(new BasicNameValuePair("delimiter", delimiter.toString()));
        }
        HttpGet method = new HttpGet(region.getStorageUrl(container, parameters));
        return this.execute(method, new ObjectResponseHandler());
    }

    /**
     * List the objects in a container in lexicographic order.
     *
     * @param container The container name
     * @return A list of FilesObjects starting with the given string
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *                                Unexpected response
     * @throws AuthorizationException The openstack's login was invalid.
     */
    public List<StorageObject> listObjects(Region region, String container) throws IOException {
        return listObjectsStartingWith(region, container, null, null, -1, null, null);
    }

    /**
     * List the objects in a container in lexicographic order.
     *
     * @param container The container name
     * @param delimiter Use this argument as the delimiter that separates "directories"
     * @return A list of FilesObjects starting with the given string
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *                                Unexpected response
     * @throws AuthorizationException The openstack's login was invalid.
     */
    public List<StorageObject> listObjects(Region region, String container, Character delimiter) throws IOException {
        return listObjectsStartingWith(region, container, null, null, -1, null, delimiter);
    }

    /**
     * List the objects in a container in lexicographic order.
     *
     * @param container The container name
     * @param limit     Return at most <code>limit</code> objects
     * @return A list of FilesObjects starting with the given string
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *                                Unexpected response
     * @throws AuthorizationException The openstack's login was invalid.
     */
    public List<StorageObject> listObjects(Region region, String container, int limit) throws IOException {
        return listObjectsStartingWith(region, container, null, null, limit, null, null);
    }

    /**
     * List the objects in a container in lexicographic order.
     *
     * @param container The container name
     * @param path      Only look for objects in this path
     * @return A list of FilesObjects starting with the given string
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *                                Unexpected response
     * @throws AuthorizationException
     */
    public List<StorageObject> listObjects(Region region, String container, String path) throws IOException {
        return listObjectsStartingWith(region, container, null, path, -1, null, null);
    }

    /**
     * List the objects in a container in lexicographic order.
     *
     * @param container The container name
     * @param path      Only look for objects in this path
     * @param delimiter Use this argument as the delimiter that separates "directories"
     * @return A list of FilesObjects starting with the given string
     * @throws IOException            There was an IO error doing network communication
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *                                Unexpected response
     * @throws AuthorizationException
     */
    public List<StorageObject> listObjects(Region region, String container, String path, Character delimiter) throws IOException {
        return listObjectsStartingWith(region, container, null, path, -1, null, delimiter);
    }

    /**
     * List the objects in a container in lexicographic order.
     *
     * @param container The container name
     * @param path      Only look for objects in this path
     * @param limit     Return at most <code>limit</code> objects
     * @return A list of FilesObjects starting with the given string
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *                                Unexpected response
     * @throws AuthorizationException The openstack's login was invalid.
     */
    public List<StorageObject> listObjects(Region region, String container, String path, int limit) throws IOException {
        return listObjectsStartingWith(region, container, null, path, limit, null);
    }

    /**
     * List the objects in a container in lexicographic order.
     *
     * @param container The container name
     * @param path      Only look for objects in this path
     * @param limit     Return at most <code>limit</code> objects
     * @param marker    Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.
     * @return A list of FilesObjects starting with the given string
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *                                Unexpected response
     * @throws AuthorizationException
     */
    public List<StorageObject> listObjects(Region region, String container, String path, int limit, String marker) throws IOException {
        return listObjectsStartingWith(region, container, null, path, limit, marker);
    }

    /**
     * List the objects in a container in lexicographic order.
     *
     * @param container The container name
     * @param limit     Return at most <code>limit</code> objects
     * @param marker    Returns objects lexicographically greater than <code>marker</code>.  Used in conjunction with <code>limit</code> to paginate the list.
     * @return A list of FilesObjects starting with the given string
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *                                Unexpected response
     * @throws AuthorizationException The openstack's login was invalid.
     */
    public List<StorageObject> listObjects(Region region, String container, int limit, String marker) throws IOException {
        return listObjectsStartingWith(region, container, null, null, limit, marker);
    }

    /**
     * Convenience method to test for the existence of a container in Cloud Files.
     *
     * @param container Container name
     * @return true if the container exists.  false otherwise.
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *          Unexpected response
     */
    public boolean containerExists(Region region, String container) throws IOException {
        try {
            this.getContainerInfo(region, container);
            return true;
        }
        catch(ContainerNotFoundException notfound) {
            return false;
        }
    }

    /**
     * Gets information for the given account.
     *
     * @return The FilesAccountInfo with information about the number of containers and number of bytes used
     *         by the given account.
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *                                Unexpected response
     * @throws AuthorizationException The openstack's login was invalid.
     */
    public AccountInfo getAccountInfo(Region region) throws IOException {
        HttpHead method = new HttpHead(region.getStorageUrl());
        return this.execute(method, new AccountInfoHandler());
    }

    /**
     * Get basic information on a container (number of items and the total size).
     *
     * @param container The container to get information for
     * @return ContainerInfo object of the container is present or null if its not present
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *                                There was an protocol level exception while talking to Cloudfiles
     * @throws ch.iterate.openstack.swift.exception.NotFoundException
     *                                The container was not found
     * @throws AuthorizationException The openstack was not logged in or the log in expired.
     */
    public ContainerInfo getContainerInfo(Region region, String container) throws IOException {
        HttpHead method = new HttpHead(region.getStorageUrl(container));
        return this.execute(method, new ContainerInfoHandler(region, container));
    }


    /**
     * Creates a container
     *
     * @param name The name of the container to be created
     * @throws ch.iterate.openstack.swift.exception.GenericException
     *                                Unexpected response
     * @throws AuthorizationException The openstack was not property logged in
     */
    public void createContainer(Region region, String name) throws IOException {
        HttpPut method = new HttpPut(region.getStorageUrl(name));
        Response response = this.execute(method, new DefaultResponseHandler());
        if(response.getStatusCode() == HttpStatus.SC_CREATED) {
            return;
        }
        else if(response.getStatusCode() == HttpStatus.SC_ACCEPTED) {
            throw new ContainerExistsException(response);
        }
        else {
            throw new GenericException(response);
        }
    }

    /**
     * Deletes a container
     *
     * @param name The name of the container
     * @throws GenericException       Unexpected response
     * @throws AuthorizationException The user is not Logged in
     * @throws ch.iterate.openstack.swift.exception.NotFoundException
     *                                The container doesn't exist
     * @throws ch.iterate.openstack.swift.exception.ContainerNotEmptyException
     *                                The container was not empty
     */
    public void deleteContainer(Region region, String name) throws IOException {
        HttpDelete method = new HttpDelete(region.getStorageUrl(name));
        Response response = this.execute(method, new DefaultResponseHandler());
        if(response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            return;
        }
        else if(response.getStatusCode() == HttpStatus.SC_CONFLICT) {
            throw new ContainerNotEmptyException(response);
        }
    }

    /**
     * Enables access of files in this container via the Content Delivery Network.
     *
     * @param name The name of the container to enable
     * @return The CDN Url of the container
     * @throws IOException      There was an IO error doing network communication
     * @throws GenericException Unexpected response
     */
    public String cdnEnableContainer(Region region, String name) throws IOException {
        HttpPut method = new HttpPut(region.getCDNManagementUrl(name));
        Response response = this.execute(method, new DefaultResponseHandler());
        if(response.getStatusCode() == HttpStatus.SC_CREATED || response.getStatusCode() == HttpStatus.SC_ACCEPTED) {
            return response.getResponseHeader(Constants.X_CDN_URI).getValue();
        }
        else {
            throw new GenericException(response);
        }
    }

    public String cdnUpdateContainer(Region region, String name, int ttl, boolean enabled, boolean retainLogs)
            throws IOException {
        return cdnUpdateContainer(region, name, ttl, enabled, null, null, retainLogs);
    }

    /**
     * Enables access of files in this container via the Content Delivery Network.
     *
     * @param name         The name of the container to enable
     * @param ttl          How long the CDN can use the content before checking for an update.  A negative value will result in this not being changed.
     * @param enabled      True if this container should be accessible, false otherwise
     * @param referrerAcl  ACL
     * @param userAgentACL ACL
     * @param retainLogs   True if cdn access logs should be kept for this container, false otherwise
     * @return The CDN Url of the container
     * @throws GenericException Unexpected response
     */
    /*
     * @param referrerAcl Unused for now
     * @param userAgentACL Unused for now
     */
    private String cdnUpdateContainer(Region region, String name, int ttl, boolean enabled, String referrerAcl, String userAgentACL, boolean retainLogs)
            throws IOException {
        HttpPost method = new HttpPost(region.getCDNManagementUrl(name));
        if(ttl > 0) {
            method.setHeader(Constants.X_CDN_TTL, Integer.toString(ttl));
        }
        method.setHeader(Constants.X_CDN_ENABLED, Boolean.toString(enabled));
        method.setHeader(Constants.X_CDN_RETAIN_LOGS, Boolean.toString(retainLogs));
        if(referrerAcl != null) {
            method.setHeader(Constants.X_CDN_REFERRER_ACL, referrerAcl);
        }
        if(userAgentACL != null) {
            method.setHeader(Constants.X_CDN_USER_AGENT_ACL, userAgentACL);
        }
        Response response = this.execute(method, new DefaultResponseHandler());
        if(response.getStatusCode() == HttpStatus.SC_ACCEPTED) {
            return response.getResponseHeader(Constants.X_CDN_URI).getValue();
        }
        else {
            throw new GenericException(response);
        }
    }


    /**
     * Gets current CDN sharing status of the container
     *
     * @param name Container
     * @return Information on the container
     * @throws GenericException Unexpected response
     * @throws ch.iterate.openstack.swift.exception.NotFoundException
     *                          The Container has never been CDN enabled
     */
    public CDNContainer getCDNContainerInfo(Region region, String name) throws IOException {
        HttpHead method = new HttpHead(region.getCDNManagementUrl(name));
        return this.execute(method, new CdnContainerInfoHandler(region, name));
    }

    /**
     * Gets current CDN sharing status of the container
     *
     * @param container Container name
     * @return Information on the container
     * @throws GenericException Unexpected response
     * @throws ch.iterate.openstack.swift.exception.NotFoundException
     *                          The Container has never been CDN enabled
     */
    public boolean isCDNEnabled(Region region, String container) throws IOException {
        HttpHead method = new HttpHead(region.getCDNManagementUrl(container));
        Response response = this.execute(method, new DefaultResponseHandler());
        if(response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            for(Header hdr : response.getResponseHeaders()) {
                String name = hdr.getName().toLowerCase();
                if(Constants.X_CDN_ENABLED.equalsIgnoreCase(name)) {
                    return Boolean.valueOf(hdr.getValue());
                }
            }
            throw new GenericException(response);
        }
        else if(response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            return false;
        }
        else {
            throw new GenericException(response);
        }
    }


    /**
     * Creates a path (but not any of the sub portions of the path)
     *
     * @param container The name of the container.
     * @param path      The name of the Path
     * @throws GenericException Unexpected response
     */
    public void createPath(Region region, String container, String path) throws IOException {
        this.storeObject(region, container, new ByteArrayInputStream(new byte[]{}), "application/directory", path,
                new HashMap<String, String>());
    }

    /**
     * Purges all items from a given container from the CDN
     *
     * @param container      The name of the container
     * @param emailAddresses An optional comma separated list of email addresses to be notified when the purge is complete.
     *                       <code>null</code> if desired.
     * @throws AuthorizationException Log in was not successful, or account is suspended
     * @throws GenericException       Unexpected response
     */
    public void purgeCDNContainer(Region region, String container, String emailAddresses) throws IOException {
        HttpDelete method = new HttpDelete(region.getCDNManagementUrl(container));
        if(emailAddresses != null) {
            method.setHeader(Constants.X_PURGE_EMAIL, emailAddresses);
        }
        Response response = this.execute(method, new DefaultResponseHandler());
        if(response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            return;
        }
        else {
            throw new GenericException(response);
        }
    }

    /**
     * Purges all items from a given container from the CDN
     *
     * @param container      The name of the container
     * @param object         The name of the object
     * @param emailAddresses An optional comma separated list of email addresses to be notified when the purge is complete.
     *                       <code>null</code> if desired.
     * @throws GenericException       Unexpected response
     * @throws AuthorizationException Log in was not successful, or account is suspended
     */
    public void purgeCDNObject(Region region, String container, String object, String emailAddresses) throws IOException {
        HttpDelete method = new HttpDelete(region.getCDNManagementUrl(container, object));
        if(emailAddresses != null) {
            method.setHeader(Constants.X_PURGE_EMAIL, emailAddresses);
        }
        Response response = this.execute(method, new DefaultResponseHandler());
        if(response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            return;
        }
        else {
            throw new GenericException(response);
        }
    }

    /**
     * Gets list of all of the containers associated with this account.
     *
     * @return A list of containers
     * @throws GenericException Unexpected response
     */
    public List<CDNContainer> listCdnContainerInfo(Region region) throws IOException {
        return listCdnContainerInfo(region, -1, null);
    }

    /**
     * Gets list of all of the containers associated with this account.
     *
     * @param limit The maximum number of container names to return
     * @return A list of containers
     * @throws GenericException Unexpected response
     */
    public List<CDNContainer> listCdnContainerInfo(Region region, int limit) throws IOException {
        return listCdnContainerInfo(region, limit, null);
    }

    /**
     * Gets list of all of the containers associated with this account.
     *
     * @param limit  The maximum number of container names to return
     * @param marker All of the names will come after <code>marker</code> lexicographically.
     * @return A list of containers
     * @throws GenericException Unexpected response
     */
    public List<CDNContainer> listCdnContainerInfo(Region region, int limit, String marker) throws IOException {
        LinkedList<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("format", "xml"));
        if(limit > 0) {
            params.add(new BasicNameValuePair("limit", String.valueOf(limit)));
        }
        if(marker != null) {
            params.add(new BasicNameValuePair("marker", marker));
        }
        HttpGet method = new HttpGet(region.getCDNManagementUrl(params));
        return this.execute(method, new CdnContainerInfoListHandler(region));
    }

    /**
     * Create a manifest on the server, including metadata
     *
     * @param container   The name of the container
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param manifest    Set manifest content here
     * @return True if response code is 201
     * @throws GenericException Unexpected response
     */
    public boolean createManifestObject(Region region, String container, String contentType, String name, String manifest) throws IOException {
        return createManifestObject(region, container, contentType, name, manifest, new HashMap<String, String>());
    }

    /**
     * Create a manifest on the server, including metadata
     *
     * @param container   The name of the container
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param manifest    Set manifest content here
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @return True if response code is 201
     * @throws GenericException Unexpected response
     */
    public boolean createManifestObject(Region region, String container, String contentType, String name, String manifest, Map<String, String> metadata) throws IOException {
        byte[] arr = new byte[0];
        HttpPut method = new HttpPut(region.getStorageUrl(container, name));
        method.setHeader(Constants.MANIFEST_HEADER, manifest);
        ByteArrayEntity entity = new ByteArrayEntity(arr);
        entity.setContentType(contentType);
        method.setEntity(entity);
        for(Map.Entry<String, String> key : this.renameObjectMetadata(metadata).entrySet()) {
            method.setHeader(key.getKey(), key.getValue());
        }
        Response response = this.execute(method, new DefaultResponseHandler());
        if(response.getStatusCode() == HttpStatus.SC_CREATED) {
            return true;
        }
        else {
            throw new GenericException(response);
        }
    }

    /**
     * Store a file on the server, including metadata, with the contents coming from an input stream.  This allows you to
     * not know the entire length of your content when you start to write it.  Nor do you have to hold it entirely in memory
     * at the same time.
     *
     * @param container   The name of the container
     * @param data        Any object that implements InputStream
     * @param contentType The MIME type of the file
     * @param name        The name of the file on the server
     * @param metadata    A map with the metadata as key names and values as the metadata values
     * @return True if response code is 201
     * @throws GenericException Unexpected response
     */
    public String storeObject(Region region, String container, InputStream data, String contentType, String name, Map<String, String> metadata) throws IOException {
        HttpPut method = new HttpPut(region.getStorageUrl(container, name));
        InputStreamEntity entity = new InputStreamEntity(data, -1);
        entity.setChunked(true);
        entity.setContentType(contentType);
        method.setEntity(entity);
        for(Map.Entry<String, String> key : this.renameObjectMetadata(metadata).entrySet()) {
            method.setHeader(key.getKey(), key.getValue());
        }
        Response response = this.execute(method, new DefaultResponseHandler());
        if(response.getStatusCode() == HttpStatus.SC_CREATED) {
            return response.getResponseHeader(HttpHeaders.ETAG).getValue();
        }
        else {
            throw new GenericException(response);
        }
    }

    /**
     * @param container The name of the container
     * @param name      The name of the object
     * @param entity    The name of the request entity (make sure to set the Content-Type
     * @param metadata  The metadata for the object
     * @param md5sum    The 32 character hex encoded MD5 sum of the data
     * @return The ETAG if the save was successful, null otherwise
     * @throws GenericException There was a protocol level error talking to CloudFiles
     */
    public String storeObject(Region region, String container, String name, HttpEntity entity, Map<String, String> metadata, String md5sum) throws IOException {
        HttpPut method = new HttpPut(region.getStorageUrl(container, name));
        method.setEntity(entity);
        if(md5sum != null) {
            method.setHeader(HttpHeaders.ETAG, md5sum);
        }
        method.setHeader(entity.getContentType());
        for(Map.Entry<String, String> key : this.renameObjectMetadata(metadata).entrySet()) {
            method.setHeader(key.getKey(), key.getValue());
        }
        Response response = this.execute(method, new DefaultResponseHandler());
        if(response.getStatusCode() == HttpStatus.SC_CREATED) {
            return response.getResponseHeader(HttpHeaders.ETAG).getValue();
        }
        else {
            throw new GenericException(response);
        }
    }

    private Map<String, String> renameContainerMetadata(Map<String, String> metadata) {
        return this.renameMetadata(metadata, Constants.X_CONTAINER_META);
    }

    private Map<String, String> renameObjectMetadata(Map<String, String> metadata) {
        return this.renameMetadata(metadata, Constants.X_OBJECT_META);
    }

    private Map<String, String> renameMetadata(Map<String, String> metadata, String prefix) {
        final Map<String, String> converted = new HashMap<String, String>(metadata.size());
        for(Map.Entry<String, String> entry : metadata.entrySet()) {
            if(entry.getKey().startsWith(prefix)) {
                converted.put(entry.getKey(), entry.getValue());
            }
            else {
                if(!Constants.HTTP_HEADER_EDITABLE_NAMES.contains(entry.getKey().toLowerCase(Locale.ENGLISH))) {
                    converted.put(prefix + entry.getKey(), encode(entry.getValue()));
                }
                else {
                    converted.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return converted;
    }

    private static String encode(String object) {
        URLCodec codec = new URLCodec();
        try {
            return codec.encode(object).replaceAll("\\+", "%20");
        }
        catch(EncoderException ee) {
            return object;
        }
    }

    /**
     * This method copies the object found in the source container with the
     * source object name to the destination container with the destination
     * object name.
     *
     * @param sourceContainer of object to copy
     * @param sourceObjName   of object to copy
     * @param destContainer   where object copy will be copied
     * @param destObjName     of object copy
     * @return ETag if successful, else null
     * @throws GenericException Unexpected response
     */
    public String copyObject(Region region, String sourceContainer,
                             String sourceObjName, String destContainer, String destObjName)
            throws IOException {
        HttpPut method = new HttpPut(region.getStorageUrl(destContainer, destObjName));
        method.setHeader(Constants.X_COPY_FROM, encode(sourceContainer) + "/" + encode(sourceObjName));
        Response response = this.execute(method, new DefaultResponseHandler());
        if(response.getStatusCode() == HttpStatus.SC_CREATED) {
            return response.getResponseHeader(HttpHeaders.ETAG).getValue();
        }
        else {
            throw new GenericException(response);
        }
    }

    /**
     * Delete the given object from it's container.
     *
     * @param container The container name
     * @param object    The object name
     * @throws IOException      There was an IO error doing network communication
     * @throws GenericException Unexpected response
     * @throws ch.iterate.openstack.swift.exception.NotFoundException
     *                          The file was not found
     */
    public void deleteObject(Region region, String container, String object) throws IOException {
        HttpDelete method = new HttpDelete(region.getStorageUrl(container, object));
        Response response = this.execute(method, new DefaultResponseHandler());
        if(response.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            // Deleted
        }
        else {
            throw new GenericException(response);
        }
    }

    /**
     * Get an object's metadata
     *
     * @param container The name of the container
     * @param object    The name of the object
     * @return The object's metadata
     * @throws GenericException       Unexpected response
     * @throws AuthorizationException The Client's Login was invalid.
     * @throws ch.iterate.openstack.swift.exception.NotFoundException
     *                                The file was not found
     */
    public ObjectMetadata getObjectMetaData(Region region, String container, String object) throws IOException {
        HttpHead method = new HttpHead(region.getStorageUrl(container, object));
        return this.execute(method, new ObjectMetadataResponseHandler());
    }

    /**
     * Get an container's metadata
     *
     * @param container The name of the container
     * @return The container's metadata
     * @throws GenericException       Unexpected response
     * @throws AuthorizationException The Client's Login was invalid.
     */
    public ContainerMetadata getContainerMetaData(Region region, String container) throws IOException {
        HttpHead method = new HttpHead(region.getStorageUrl(container));
        return this.execute(method, new ContainerMetadataResponseHandler());
    }

    /**
     * Get's the given object's content as a stream
     *
     * @param container The name of the container
     * @param object    The name of the object
     * @return An input stream that will give the objects content when read from.
     * @throws GenericException Unexpected response
     */
    public InputStream getObject(Region region, String container, String object) throws IOException {
        HttpGet method = new HttpGet(region.getStorageUrl(container, object));
        Response response = this.execute(method);
        if(response.getStatusCode() == HttpStatus.SC_OK) {
            return response.getResponseBodyAsStream();
        }
        else if(response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            method.abort();
            throw new NotFoundException(response);
        }
        else {
            method.abort();
            throw new GenericException(response);
        }
    }

    public InputStream getObject(Region region, String container, String object, long offset, long length) throws IOException {
        HttpGet method = new HttpGet(region.getStorageUrl(container, object));
        method.setHeader("Range", "bytes=" + offset + "-" + length);
        Response response = this.execute(method);
        if(response.getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT) {
            return response.getResponseBodyAsStream();
        }
        else if(response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            method.abort();
            throw new NotFoundException(response);
        }
        else {
            method.abort();
            throw new GenericException(response);
        }
    }

    public void updateObjectManifest(Region region, String container, String object, String manifest) throws IOException {
        this.updateObjectMetadataAndManifest(region, container, object, new HashMap<String, String>(), manifest);
    }

    public void updateObjectMetadata(Region region, String container, String object,
                                     Map<String, String> metadata) throws IOException {
        this.updateObjectMetadataAndManifest(region, container, object, metadata, null);
    }

    public void updateObjectMetadataAndManifest(Region region, String container, String object,
                                                Map<String, String> metadata, String manifest) throws IOException {
        HttpPost method = new HttpPost(region.getStorageUrl(container, object));
        if(manifest != null) {
            method.setHeader(Constants.MANIFEST_HEADER, manifest);
        }
        for(Map.Entry<String, String> key : this.renameObjectMetadata(metadata).entrySet()) {
            method.setHeader(key.getKey(), key.getValue());
        }
        this.execute(method, new DefaultResponseHandler());
    }

    public void updateContainerMetadata(Region region, String container,
                                        Map<String, String> metadata) throws IOException {
        HttpPost method = new HttpPost(region.getStorageUrl(container));
        for(Map.Entry<String, String> key : this.renameContainerMetadata(metadata).entrySet()) {
            method.setHeader(key.getKey(), key.getValue());
        }
        this.execute(method, new DefaultResponseHandler());
    }
}
