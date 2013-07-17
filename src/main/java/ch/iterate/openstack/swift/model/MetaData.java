/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift.model;

import java.util.HashMap;
import java.util.Map;

public class MetaData {
    protected String mimeType;
    protected String contentLength;
    protected String eTag;
    protected String lastModified;
    protected Map<String, String> metaData = new HashMap<String, String>();

    public MetaData(String mimeType, String lastModified, String contentLength, String eTag) {
        this.mimeType = mimeType;
        this.lastModified = lastModified;
        this.contentLength = contentLength;
        this.eTag = eTag;
    }

    /**
     * Set new metatdata for this object.  Warning, this metadata clears out all old metadata.  To add new fields, use
     * <code>setMetaData</code> instead.
     *
     * @param metaData The new metadata
     */
    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public void addMetaData(String key, String value) {
        this.metaData.put(key, value);
    }

    /**
     * The last time the object was modified
     *
     * @return The last modification date
     */
    public String getLastModified() {
        return lastModified;
    }

    /**
     * Set the last time the object was modified
     */
    void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * The MIME type of the object
     *
     * @return The MIME type of the object
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Set's the MIME type of the object
     *
     * @param mimeType Content MIME type
     */
    void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * The size of the object, in bytes
     *
     * @return The size of the object
     */
    public String getContentLength() {
        return contentLength;
    }

    /**
     * Set the size of the object
     *
     * @param contentLength The new content length
     */
    void setContentLength(String contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * The MD5 checksum represented in a hex-encoded string
     *
     * @return The eTag
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Set the MD5 checksum for this object
     *
     * @param eTag The new eTag
     */
    void setETag(String eTag) {
        this.eTag = eTag;
    }

    /**
     * The metadata associated with this object.
     *
     * @return The object's metadata
     */
    public Map<String, String> getMetaData() {
        return metaData;
    }
}
