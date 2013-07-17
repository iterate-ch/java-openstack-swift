/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift.model;

/**
 * Contains basic information about the container
 *
 * @author lvaughn
 */
public class ContainerInfo {

    private Region region;
    private String name;
    private int objectCount;
    private long totalSize;

    /**
     * @param containerCount The number of objects in the container
     * @param totalSize      The total size of the container (in bytes)
     */
    public ContainerInfo(Region region, String name, int containerCount, long totalSize) {
        this.region = region;
        this.name = name;
        this.objectCount = containerCount;
        this.totalSize = totalSize;
    }

    public Region getRegion() {
        return region;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the number of objects in the container
     *
     * @return The number of objects
     */
    public int getObjectCount() {
        return objectCount;
    }

    /**
     * @return The total size of the objects in the container (in bytes)
     */
    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final ContainerInfo that = (ContainerInfo) o;
        if(name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if(region != null ? !region.equals(that.region) : that.region != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = region != null ? region.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
