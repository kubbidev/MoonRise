package me.kubbidev.moonrise.common.http;

import okhttp3.*;

import java.io.IOException;

public class BytebinClient extends AbstractHttpClient {

    /** The bytebin URL */
    private final String url;

    /** The client user agent */
    private final String userAgent;

    public BytebinClient(OkHttpClient okHttp, String url, String userAgent) {
        super(okHttp);
        this.url = url.endsWith("/") ? url : url + "/";
        this.userAgent = userAgent;
    }

    public String getUrl() {
        return this.url;
    }

    @Override
    public String getUserAgent() {
        return this.userAgent;
    }

    @Override
    public Response makeHttpRequest(Request request) throws IOException, UnsuccessfulRequestException {
        return super.makeHttpRequest(request);
    }

    /**
     * POSTs GZIP compressed content to bytebin.
     *
     * @param buf the compressed content
     * @param contentType the type of the content
     * @param userAgentExtra extra string to append to the user agent
     * @return the key of the resultant content
     * @throws IOException if an error occurs
     */
    public Content postContent(byte[] buf, MediaType contentType, String userAgentExtra)
            throws IOException, UnsuccessfulRequestException {

        RequestBody body = RequestBody.create(buf, contentType);

        String userAgent = this.userAgent;
        if (userAgentExtra != null) {
            userAgent += '/' + userAgentExtra;
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(this.url + "post")
                .header("User-Agent", userAgent)
                .header("Content-Encoding", "gzip");

        Request request = requestBuilder.post(body).build();
        try (Response response = this.makeHttpRequest(request)) {
            var key = response.header("Location");
            if (key == null) {
                throw new IllegalStateException("Key not returned");
            }
            return new Content(key);
        }
    }

    public Content postContent(byte[] buf, MediaType contentType) throws IOException, UnsuccessfulRequestException {
        return this.postContent(buf, contentType, null);
    }

    public record Content(String key) {
    }
}
