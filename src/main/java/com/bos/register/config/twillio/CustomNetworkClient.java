package com.bos.register.config.twillio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;

import com.google.common.collect.Lists;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.http.HttpClient;
import com.twilio.http.HttpMethod;
import com.twilio.http.Request;
import com.twilio.http.Response;

public class CustomNetworkClient extends HttpClient {
    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int SOCKET_TIMEOUT = 5000;
    private static final int CONNECTIONS_PER_ROUTE = 10;

    private org.apache.http.client.HttpClient client;

    private String twilioRequestId;

    private Float twilioResponseDuration;


    /**
     * Create a new HTTP Client.
     */
    public CustomNetworkClient()
    {
        this.invokeHttpProxy();
    }

    /**
     * Make a request.
     *
     * @param request request to make
     * @return Response of the HTTP request
     */
    public Response makeRequest(final Request request) {
        twilioResponseDuration = null;
        twilioRequestId = null;

        RequestBuilder builder = RequestBuilder.create(request.getMethod().toString())
                .setUri(request.constructURL().toString())
                .setVersion(HttpVersion.HTTP_1_1)
                .setCharset(StandardCharsets.UTF_8);

        if (request.requiresAuthentication()) {
            builder = builder.addHeader(HttpHeaders.AUTHORIZATION, request.getAuthString());
        }

        HttpMethod method = request.getMethod();
        if (method == HttpMethod.POST) {
            builder = builder.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

            for (Map.Entry<String, List<String>> entry : request.getPostParams().entrySet()) {
                for (String value : entry.getValue()) {
                    builder = builder.addParameter(entry.getKey(), value);
                }
            }
        }

        try {
            HttpResponse response = client.execute(builder.build());

            if (response.containsHeader("Twilio-Request-Id"))
                twilioRequestId = response.getFirstHeader("Twilio-Request-Id").getValue();

            if (response.containsHeader("Twilio-Request-Duration"))
                twilioResponseDuration = new Float(response.getFirstHeader("Twilio-Request-Duration").getValue());

            return new Response(
                    response.getEntity() == null ? null : response.getEntity().getContent(),
                    response.getStatusLine().getStatusCode()
            );
        } catch (IOException e) {
            throw new ApiException(e.getMessage());
        }
    }

    public String getTwilioRequestId() {
        return twilioRequestId;
    }

    public Float getTwilioResponseDuration() {
        return twilioResponseDuration;
    }


    public  void invokeHttpProxy() {
//        HttpHost proxy = new HttpHost("10.17.10.42", 8080, "http");
        HttpHost proxy = new HttpHost("kpproxygsit", 8080, "http");
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

        //Set up Twilio user credentials
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope("api.twilio.com", 443),
                new UsernamePasswordCredentials(TwilioUtil.ACCOUNT_SID, TwilioUtil.AUTH_TOKEN));

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();

        Collection<Header> headers = Lists.newArrayList(
                new BasicHeader("X-Twilio-Client", "java-" + Twilio.VERSION),
                new BasicHeader(HttpHeaders.USER_AGENT, "twilio-java/" + Twilio.VERSION + " (" + Twilio.JAVA_VERSION + ")"),
                new BasicHeader(HttpHeaders.ACCEPT, "application/json"),
                new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "utf-8")
        );


        client = HttpClientBuilder.create().setRoutePlanner(routePlanner)
                .setDefaultCredentialsProvider(credsProvider)
                .setConnectionManager(new PoolingHttpClientConnectionManager())
                .setDefaultRequestConfig(config)
                .setDefaultHeaders(headers)
                .setMaxConnPerRoute(CONNECTIONS_PER_ROUTE)
                .build();
    }
}
