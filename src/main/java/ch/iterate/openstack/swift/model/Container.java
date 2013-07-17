/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift.model;

public class Container {

    private Region region;
    private String name;

    /**
     * @param name The name of the container
     */
    public Container(Region region, String name) {
        this.region = region;
        this.name = name;

    }

    public Region getRegion() {
        return region;
    }

    /**
     * Get the name of the container
     *
     * @return The name of this container
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final Container that = (Container) o;
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
