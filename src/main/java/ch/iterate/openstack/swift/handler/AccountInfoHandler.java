package ch.iterate.openstack.swift.handler;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import java.io.IOException;

import ch.iterate.openstack.swift.Constants;
import ch.iterate.openstack.swift.Response;
import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.AccountInfo;

public class AccountInfoHandler implements ResponseHandler<AccountInfo> {

    public AccountInfo handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT ||
                response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return new AccountInfo(this.getAccountBytesUsed(response),
                    this.getAccountContainerCount(response),
                    this.getAccountTempUrlKey(response));
        }
        else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            throw new AuthorizationException(new Response(response));
        }
        throw new GenericException(new Response(response));
    }

    /**
     * Get the number of objects in the header
     *
     * @return Null if the header is not present or the correct value as defined by the header
     */
    private Integer getAccountContainerCount(final HttpResponse response) {
        Header contCountHeader = response.getFirstHeader(Constants.X_ACCOUNT_CONTAINER_COUNT);
        if(contCountHeader != null) {
            return Integer.parseInt(contCountHeader.getValue());
        }
        return null;
    }

    /**
     * Get the number of bytes used by the container
     *
     * @return Null if the header is not present or the correct value as defined by the header
     */
    private Long getAccountBytesUsed(final HttpResponse response) {
        Header accountBytesUsedHeader = response.getFirstHeader(Constants.X_ACCOUNT_BYTES_USED);
        if(accountBytesUsedHeader != null) {
            return Long.parseLong(accountBytesUsedHeader.getValue());
        }
        return null;
    }

    private String getAccountTempUrlKey(final HttpResponse response) {
        Header tempUrlKeyHeader = response.getFirstHeader(Constants.X_ACCOUNT_META_TEMP_URL_KEY);
        if(tempUrlKeyHeader != null) {
            return tempUrlKeyHeader.getValue();
        }
        return null;
    }
}
