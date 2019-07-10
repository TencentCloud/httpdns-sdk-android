/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.httpdns.network;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.android.volley.toolbox.HttpStack;
import com.tencent.httpdns.network.httpclient.HttpClientHelper;
import kotlin.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

/**
 * An HttpStack that performs request over an {@link HttpClient}.
 *
 * @deprecated The Apache HTTP library on Android is deprecated. Use {@link com.android.volley.toolbox.HurlStack} or another
 *     {@link com.android.volley.toolbox.BaseHttpStack} implementation.
 */
@Deprecated
final class HttpClientStackWrapper implements HttpStack {
    protected final HttpClient mClient;

    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    public HttpClientStackWrapper(HttpClient client) {
        mClient = client;
    }

    private static void setHeaders(HttpUriRequest httpRequest, Map<String, String> headers) {
        for (String key : headers.keySet()) {
            httpRequest.setHeader(key, headers.get(key));
        }
    }

    @SuppressWarnings("unused")
    private static List<NameValuePair> getPostParameterPairs(Map<String, String> postParams) {
        List<NameValuePair> result = new ArrayList<>(postParams.size());
        for (String key : postParams.keySet()) {
            result.add(new BasicNameValuePair(key, postParams.get(key)));
        }
        return result;
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {
        HttpUriRequest httpRequest = createHttpRequest(request, additionalHeaders);
        setHeaders(httpRequest, additionalHeaders);
        // Request.getHeaders() takes precedence over the given additional (cache) headers) and any
        // headers set by createHttpRequest (like the Content-Type header).
        setHeaders(httpRequest, request.getHeaders());
        onPrepareRequest(httpRequest);
        HttpParams httpParams = httpRequest.getParams();
        int timeoutMs = request.getTimeoutMs();
        // TODO: Reevaluate this connection timeout based on more wide-scale
        // data collection and possibly different for wifi vs. 3G.
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, timeoutMs);
        return mClient.execute(httpRequest);
    }

    /** Creates the appropriate subclass of HttpUriRequest for passed in request. */
    @SuppressWarnings("deprecation")
    /* protected */ static HttpUriRequest createHttpRequest(
            Request<?> request, Map<String, String> additionalHeaders) throws AuthFailureError {
        // NOTE: BEGIN HTTPDNS-added
        Pair<String, String> url2HostPair = HttpClientHelper.INSTANCE.url2HostPair(request.getUrl());
        // NOTE: END HTTPDNS-added
        switch (request.getMethod()) {
            // NOTE: BEGIN HTTPDNS-changed
            case Method.DEPRECATED_GET_OR_POST:
            {
                // This is the deprecated way that needs to be handled for backwards
                // compatibility.
                // If the request's post body is null, then the assumption is that the request
                // is
                // GET.  Otherwise, it is assumed that the request is a POST.
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    HttpPost postRequest = new HttpPost(url2HostPair.getFirst());
                    postRequest.addHeader(HTTP.TARGET_HOST, url2HostPair.getSecond());
                    postRequest.addHeader(
                            HEADER_CONTENT_TYPE, request.getPostBodyContentType());
                    HttpEntity entity;
                    entity = new ByteArrayEntity(postBody);
                    postRequest.setEntity(entity);
                    return postRequest;
                } else {
                    HttpGet getRequest = new HttpGet(url2HostPair.getFirst());
                    getRequest.addHeader(HTTP.TARGET_HOST, url2HostPair.getSecond());
                    return getRequest;
                }
            }
            case Method.GET:
            {
                HttpGet getRequest = new HttpGet(url2HostPair.getFirst());
                getRequest.addHeader(HTTP.TARGET_HOST, url2HostPair.getSecond());
                return getRequest;
            }
            case Method.DELETE:
            {
                HttpDelete deleteRequest = new HttpDelete(url2HostPair.getFirst());
                deleteRequest.addHeader(HTTP.TARGET_HOST, url2HostPair.getSecond());
                return deleteRequest;
            }
            case Method.POST:
            {
                HttpPost postRequest = new HttpPost(url2HostPair.getFirst());
                postRequest.addHeader(HTTP.TARGET_HOST, url2HostPair.getSecond());
                postRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(postRequest, request);
                return postRequest;
            }
            case Method.PUT:
            {
                HttpPut putRequest = new HttpPut(url2HostPair.getFirst());
                putRequest.addHeader(HTTP.TARGET_HOST, url2HostPair.getSecond());
                putRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(putRequest, request);
                return putRequest;
            }
            case Method.HEAD:
            {
                HttpHead headRequest = new HttpHead(url2HostPair.getFirst());
                headRequest.addHeader(HTTP.TARGET_HOST, url2HostPair.getSecond());
                return headRequest;
            }
            case Method.OPTIONS:
            {
                HttpOptions optionsRequest = new HttpOptions(url2HostPair.getFirst());
                optionsRequest.addHeader(HTTP.TARGET_HOST, url2HostPair.getSecond());
                return optionsRequest;
            }
            case Method.TRACE:
            {
                HttpTrace traceRequest = new HttpTrace(url2HostPair.getFirst());
                traceRequest.addHeader(HTTP.TARGET_HOST, url2HostPair.getSecond());
                return traceRequest;
            }
            case Method.PATCH:
            {
                HttpPatch patchRequest = new HttpPatch(url2HostPair.getFirst());
                patchRequest.addHeader(HTTP.TARGET_HOST, url2HostPair.getSecond());
                patchRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(patchRequest, request);
                return patchRequest;
            }
            // NOTE: END HTTPDNS-changed
            default:
                throw new IllegalStateException("Unknown request method.");
        }
    }

    private static void setEntityIfNonEmptyBody(
            HttpEntityEnclosingRequestBase httpRequest, Request<?> request)
            throws AuthFailureError {
        byte[] body = request.getBody();
        if (body != null) {
            HttpEntity entity = new ByteArrayEntity(body);
            httpRequest.setEntity(entity);
        }
    }

    /**
     * Called before the request is executed using the underlying HttpClient.
     *
     * <p>Overwrite in subclasses to augment the request.
     */
    protected void onPrepareRequest(HttpUriRequest request) throws IOException {
        // Nothing.
    }

    /**
     * The HttpPatch class does not exist in the Android framework, so this has been defined here.
     */
    public static final class HttpPatch extends HttpEntityEnclosingRequestBase {

        public static final String METHOD_NAME = "PATCH";

        public HttpPatch() {
            super();
        }

        public HttpPatch(final URI uri) {
            super();
            setURI(uri);
        }

        /** @throws IllegalArgumentException if the uri is invalid. */
        public HttpPatch(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }
    }
}
