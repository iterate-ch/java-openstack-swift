/*
 * See COPYING for license information.
 */

package ch.iterate.openstack.swift.exception;

import ch.iterate.openstack.swift.Response;

public class ContainerNotFoundException extends GenericException {
    private static final long serialVersionUID = 7751467778430037798L;

    public ContainerNotFoundException(Response response) {
        super(response);
    }
}
