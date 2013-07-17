/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift.model;

public class StorageObject {
    private String name;
    private String md5sum;
    private Long size;
    private String mimeType;
    private String lastModified;

    /**
     * Creates a new FilesObject with data from the server
     *
     * @param name The name of the object
     */
    public StorageObject(String name) {
        this.name = name;
    }

    /**
     * @return The object's name on the server
     */
    public String getName() {
        return name;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(final String md5sum) {
        this.md5sum = md5sum;
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(final String lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final StorageObject that = (StorageObject) o;
        if(lastModified != null ? !lastModified.equals(that.lastModified) : that.lastModified != null) {
            return false;
        }
        if(md5sum != null ? !md5sum.equals(that.md5sum) : that.md5sum != null) {
            return false;
        }
        if(mimeType != null ? !mimeType.equals(that.mimeType) : that.mimeType != null) {
            return false;
        }
        if(name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if(size != null ? !size.equals(that.size) : that.size != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (md5sum != null ? md5sum.hashCode() : 0);
        result = 31 * result + (size != null ? size.hashCode() : 0);
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
        return result;
    }
}
