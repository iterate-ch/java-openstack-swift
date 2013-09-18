/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift.model;

/**
 * @author lvaughn
 */
public class AccountInfo {
    private long bytesUsed;
    private int containerCount;
    private String tempUrlKey;

    public AccountInfo(final long bytesUsed, final int containerCount, final String tempUrlKey) {
        this.bytesUsed = bytesUsed;
        this.containerCount = containerCount;
        this.tempUrlKey = tempUrlKey;
    }

    /**
     * Returns the total number of bytes used by all objects in a given account.
     *
     * @return the bytesUsed
     */
    public long getBytesUsed() {
        return bytesUsed;
    }

    /**
     * @param bytesUsed The number of bytes in the account
     */
    public void setBytesUsed(long bytesUsed) {
        this.bytesUsed = bytesUsed;
    }

    /**
     * The number of containers in a given account.
     *
     * @return the containerCount
     */
    public int getContainerCount() {
        return containerCount;
    }

    /**
     * @param containerCount the containerCount to set
     */
    public void setContainerCount(int containerCount) {
        this.containerCount = containerCount;
    }

    public String getTempUrlKey() {
        return tempUrlKey;
    }

    public void setTempUrlKey(final String tempUrlKey) {
        this.tempUrlKey = tempUrlKey;
    }
}
