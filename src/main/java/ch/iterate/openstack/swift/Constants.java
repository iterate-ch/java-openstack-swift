/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift;

import org.apache.http.HttpHeaders;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Constants {
    /**
     * HTTP Header token that identifies the username
     */
    public static final String X_STORAGE_USER_DEFAULT = "X-Auth-User";
    /**
     * HTTP header token that identifies the password
     */
    public static final String X_STORAGE_PASS_DEFAULT = "X-Auth-Key";
    /**
     * HTTP header token that identifies the Storage URL after a successful user login
     */
    public static final String X_STORAGE_URL = "X-Storage-Url";
    /**
     * HTTP header that identifies the CDN Management URL after a successful login
     */
    public static final String X_CDN_MANAGEMENT_URL = "X-CDN-Management-URL";
    /**
     * HTTP header token that identifies the Storage Token after a successful user login
     */
    public static final String X_AUTH_TOKEN = "X-Auth-Token";
    /**
     * HTTP header token that is returned on a HEAD request against a Container.
     * The value of this header is the number of Objects in the Container
     */
    public static final String X_CONTAINER_OBJECT_COUNT = "X-Container-Object-Count";
    /**
     * HTTP header token that is returned on a HEAD request against a Container.
     * The value of this header is the number of Objects in the Container
     */
    public static final String X_CONTAINER_BYTES_USED = "X-Container-Bytes-Used";
    /**
     * HTTP header token that is returned on a HEAD request against an Account.
     * The value of this header is the number of Containers in the Account
     */
    public static final String X_ACCOUNT_CONTAINER_COUNT = "X-Account-Container-Count";
    /**
     * HTTP header token that is returned on a HEAD request against an Account.
     * The value of this header is the total size of the Objects in the Account
     */
    public static final String X_ACCOUNT_BYTES_USED = "X-Account-Bytes-Used";
    /**
     * HTTP header token that is returned on a HEAD request against an Account.
     * This key can be any arbitrary sequence
     */
    public static final String X_ACCOUNT_META_TEMP_URL_KEY = "X-Account-Meta-Temp-URL-Key";
    /**
     * HTTP header token that is returned by calls to the CDN Management API
     */
    public static final String X_CDN_URI = "X-CDN-URI";
    /**
     * HTTP header token that is returned by calls to the CDN Management API
     */
    public static final String X_CDN_SSL_URI = "X-CDN-SSL-URI";
    /**
     * HTTP header token that is returned by calls to the CDN Management API
     */
    public static final String X_CDN_Streaming_URI = "X-CDN-Streaming-URI";
    public static final String X_CDN_IOS_URI = "X-Cdn-Ios-Uri";
    /**
     * HTTP header token that is returned by calls to the CDN Management API
     */
    public static final String X_CDN_TTL = "X-TTL";
    /**
     * HTTP header token that is returned by calls to the CDN Management API
     */
    public static final String X_CDN_RETAIN_LOGS = "X-Log-Retention";
    /**
     * HTTP header token that is returned by calls to the CDN Management API
     */
    public static final String X_CDN_ENABLED = "X-CDN-Enabled";
    /**
     * HTTP header token that is returned by calls to the CDN Management API
     */
    public static final String X_CDN_USER_AGENT_ACL = "X-User-Agent-ACL";
    /**
     * HTTP header token that is returned by calls to the CDN Management API
     */
    public static final String X_CDN_REFERRER_ACL = "X-Referrer-ACL ";
    /**
     * HTTP header used by Cloud Files for the source of an object being copied. The value of this header is /<sourcecontainer>/<sourceobject>
     */
    public static final String X_COPY_FROM = "X-Copy-From";
    /**
     * HTTP Header used for Dynamic Large Object Manifest *
     */
    public static final String MANIFEST_HEADER = "X-Object-Manifest";
    /**
     * HTTP Header used for Static Large Object Manifest Indicator *
     */
    public static final String X_STATIC_LARGE_OBJECT = "X-Static-Large-Object";

    public static final String X_PURGE_EMAIL = "X-Purge-Email";
    /**
     * Container ACLs to grant other users permission to read and/or write objects in specific containers
     */
    public static final String X_CONTAINER_READ = "X-Container-Read";
    /**
     * Container ACLs to grant other users permission to read and/or write objects in specific containers
     */
    public static final String X_CONTAINER_WRITE = "X-Container-Write";

    /**
     * Prefix Cloud Files expects on all Meta data headers on Objects *
     */
    public static final String X_OBJECT_META = "X-Object-Meta-";
    public static final String X_CONTAINER_META = "X-Container-Meta-";

    public static final List<String> HTTP_HEADER_EDITABLE_NAMES = Arrays.asList(
            MANIFEST_HEADER.toLowerCase(Locale.ENGLISH),
            X_STATIC_LARGE_OBJECT.toLowerCase(Locale.ENGLISH),
            X_PURGE_EMAIL.toLowerCase(Locale.ENGLISH),
            X_CONTAINER_READ.toLowerCase(Locale.ENGLISH),
            X_CONTAINER_WRITE.toLowerCase(Locale.ENGLISH),
            HttpHeaders.CONTENT_TYPE.toLowerCase(Locale.ENGLISH),
            HttpHeaders.CONTENT_LANGUAGE.toLowerCase(Locale.ENGLISH),
            HttpHeaders.CONTENT_ENCODING.toLowerCase(Locale.ENGLISH),
            "content-disposition",
            HttpHeaders.EXPIRES.toLowerCase(Locale.ENGLISH),
            HttpHeaders.CACHE_CONTROL.toLowerCase(Locale.ENGLISH),
            "origin",
            "access-control-allow-origin",
            "access-control-allow-credentials",
            "access-control-expose-headers",
            "access-control-max-age",
            "access-control-allow-methods",
            "access-control-allow-headers",
            "access-control-request-method",
            "access-control-request-headers"
    );
}
