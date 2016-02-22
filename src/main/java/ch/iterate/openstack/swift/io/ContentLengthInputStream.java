package ch.iterate.openstack.swift.io;

import org.apache.commons.io.input.ProxyInputStream;

import java.io.InputStream;

public class ContentLengthInputStream extends ProxyInputStream {

    private Long length;

    public ContentLengthInputStream(final InputStream in, final Long length) {
        super(in);
        this.length = length;
    }

    public Long getLength() {
        return length;
    }
}

