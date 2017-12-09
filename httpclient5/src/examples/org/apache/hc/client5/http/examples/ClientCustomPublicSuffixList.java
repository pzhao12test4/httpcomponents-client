/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.hc.client5.http.examples;

import java.net.URL;

import org.apache.hc.client5.http.config.CookieSpecs;
import org.apache.hc.client5.http.cookie.CookieSpecProvider;
import org.apache.hc.client5.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.sync.CloseableHttpClient;
import org.apache.hc.client5.http.impl.sync.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.sync.HttpClients;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.psl.PublicSuffixMatcher;
import org.apache.hc.client5.http.psl.PublicSuffixMatcherLoader;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.sync.methods.HttpGet;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContexts;

/**
 * This example demonstrates how to use a custom public suffix list.
 */
public class ClientCustomPublicSuffixList {

    public final static void main(final String[] args) throws Exception {

        // Use PublicSuffixMatcherLoader to load public suffix list from a file,
        // resource or from an arbitrary URL
        final PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.load(
                new URL("https://publicsuffix.org/list/effective_tld_names.dat"));

        // Please use the publicsuffix.org URL to download the list no more than once per day !!!
        // Please consider making a local copy !!!

        final RFC6265CookieSpecProvider cookieSpecProvider = new RFC6265CookieSpecProvider(publicSuffixMatcher);
        final Lookup<CookieSpecProvider> cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT, cookieSpecProvider)
                .register(CookieSpecs.STANDARD, cookieSpecProvider)
                .register(CookieSpecs.STANDARD_STRICT, cookieSpecProvider)
                .build();
        final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                SSLContexts.createDefault(),
                new DefaultHostnameVerifier(publicSuffixMatcher));
        final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslsf)
                .build();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                .build()) {

            final HttpGet httpget = new HttpGet("https://httpbin.org/get");

            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());

            try (CloseableHttpResponse response = httpclient.execute(httpget)) {
                System.out.println("----------------------------------------");
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                System.out.println(EntityUtils.toString(response.getEntity()));
            }
        }
    }

}
