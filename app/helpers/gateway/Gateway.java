package helpers.gateway;

import okhttp3.*;
import okhttp3.internal.http.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import play.libs.Codec;
import play.mvc.Http;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author jtremeaux
 */
public class Gateway {
    private Map<String, String> headers = new LinkedHashMap<>();

    private Gateway() {
    }

    public void proxy(Http.Request request, Http.Response response, String backendUrl) {
        try {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(backendUrl).newBuilder();
            for (NameValuePair param : URLEncodedUtils.parse(request.querystring, Charset.defaultCharset())) {
                urlBuilder.addQueryParameter(param.getName(), param.getValue());
            }
            Request.Builder requestBuilder = new Request.Builder()
                    .url(urlBuilder.build())
                    .method(request.method, HttpMethod.permitsRequestBody(request.method) ?
                            RequestBody.create(MediaType.parse(request.contentType), IOUtils.toByteArray(request.body)) :
                            null);
            for (Map.Entry<String, String> header: headers.entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(60 * 30, TimeUnit.SECONDS)
                    .build();
            Response backendResponse = okHttpClient
                    .newCall(requestBuilder.build())
                    .execute();
            response.status = backendResponse.code();
            for (String header : backendResponse.headers().names()) {
                response.setHeader(header, backendResponse.headers().get(header));
            }
            IOUtils.copy(backendResponse.body().byteStream(), response.out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {
        Gateway gateway = new Gateway();

        public Builder() {
        }

        public Gateway build() {
            return gateway;
        }

        public Builder addHeader(String key, String value) {
            gateway.headers.put(key, value);
            return this;
        }

        public Builder setBasicAuth(String login, String password) {
            addHeader("Authorization", "Basic " + Codec.encodeBASE64(login + ":" + password));
            return this;
        }
    }
}
