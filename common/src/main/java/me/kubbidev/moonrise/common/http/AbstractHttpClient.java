package me.kubbidev.moonrise.common.http;

import com.google.gson.JsonElement;
import me.kubbidev.moonrise.common.util.gson.GsonProvider;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;

public abstract class AbstractHttpClient {

    public static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    /**
     * The http client
     */
    protected final OkHttpClient okHttp;

    public AbstractHttpClient(OkHttpClient okHttp) {
        this.okHttp = okHttp;
    }

    protected Response makeHttpRequest(Request request)
        throws IOException, UnsuccessfulRequestException {

        Response response = this.okHttp.newCall(request).execute();
        if (!response.isSuccessful()) {
            response.close();
            throw new UnsuccessfulRequestException(response);
        }
        return response;
    }

    public abstract String getUserAgent();

    /**
     * GETs json content from the http client
     *
     * @param url the url of the content
     * @return the content
     * @throws IOException if an error occurs
     */
    public @NotNull JsonElement getJsonContent(String url)
        throws IOException, UnsuccessfulRequestException {

        Request request = new Request.Builder()
            .header("User-Agent", this.getUserAgent())
            .url(url)
            .build();

        try (Response response = this.makeHttpRequest(request)) {
            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    throw new RuntimeException("No response");
                }

                try (Reader in = responseBody.charStream()) {
                    return GsonProvider.normal().fromJson(in, JsonElement.class);
                }
            }
        }
    }
}
