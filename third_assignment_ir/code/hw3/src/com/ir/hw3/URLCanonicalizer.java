package com.ir.hw3;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


public class URLCanonicalizer {

        public  String getCanonicalURL(String url) {
                URL canonicalURL = getCanonicalURL(url, null);
                if (canonicalURL != null) {
                        return canonicalURL.toExternalForm();
                }
                return null;
        }

        public  URL getCanonicalURL(String href, String context) {

                try {

                        URL canonicalURL;
                        if (context == null) {
                                canonicalURL = new URL(href);
                        } else {
                                canonicalURL = new URL(new URL(context), href);
                        }

                        String path = canonicalURL.getPath();

                        /*
                         * Normalize: no empty segments (i.e., "//"), no segments equal to
                         * ".", and no segments equal to ".." that are preceded by a segment
                         * not equal to "..".
                         */
                        path = new URI(path).normalize().toString();

                        /*
                         * Convert '//' -> '/'
                         */
                        int idx = path.indexOf("//");
                        while (idx >= 0) {
                                path = path.replace("//", "/");
                                idx = path.indexOf("//");
                        }

                        /*
                         * Drop starting '/../'
                         */
                        while (path.startsWith("/../")) {
                                path = path.substring(3);
                        }

                        /*
                         * Trim
                         */
                        path = path.trim();

                        final SortedMap<String, String> params = createParameterMap(canonicalURL.getQuery());
                        final String queryString;

                        if (params != null && params.size() > 0) {
                                String canonicalParams = canonicalize(params);
                                queryString = (canonicalParams.isEmpty() ? "" : "?" + canonicalParams);
                        } else {
                                queryString = "";
                        }

                        /*
                         * Add starting slash if needed
                         */
                        if (path.length() == 0) {
                                path = "/" + path;
                        }

                        /*
                         * Drop default port: example.com:80 -> example.com
                         */
                        int port = canonicalURL.getPort();
                        if (port == canonicalURL.getDefaultPort()) {
                                port = -1;
                        }

                        /*
                         * Lowercasing protocol and host
                         */
                        String protocol = canonicalURL.getProtocol().toLowerCase();
                        String host = canonicalURL.getHost().toLowerCase();
                        String pathAndQueryString = normalizePath(path) + queryString;

                        return new URL(protocol, host, port, pathAndQueryString);

                } catch (MalformedURLException ex) {
                        return null;
                } catch (URISyntaxException ex) {
                        return null;
                }
        }

        private  SortedMap<String, String> createParameterMap(final String queryString) {
                if (queryString == null || queryString.isEmpty()) {
                        return null;
                }

                final String[] pairs = queryString.split("&");
                final Map<String, String> params = new HashMap<String, String>(pairs.length);

                for (final String pair : pairs) {
                        if (pair.length() == 0) {
                                continue;
                        }

                        String[] tokens = pair.split("=", 2);
                        switch (tokens.length) {
                        case 1:
                                if (pair.charAt(0) == '=') {
                                        params.put("", tokens[0]);
                                } else {
                                        params.put(tokens[0], "");
                                }
                                break;
                        case 2: 
                                params.put(tokens[0], tokens[1]);
                                break;
                        }
                }
                return new TreeMap<String, String>(params);
        }

        private  String canonicalize(final SortedMap<String, String> sortedParamMap) {
                if (sortedParamMap == null || sortedParamMap.isEmpty()) {
                        return "";
                }

                final StringBuffer sb = new StringBuffer(100);
                for (Map.Entry<String, String> pair : sortedParamMap.entrySet()) {
                        final String key = pair.getKey().toLowerCase();
                        if (key.equals("jsessionid") || key.equals("phpsessid") || key.equals("aspsessionid")) {
                                continue;
                        }
                        if (sb.length() > 0) {
                                sb.append('&');
                        }
                        sb.append(percentEncodeRfc3986(pair.getKey()));
                        if (!pair.getValue().isEmpty()) {
                                sb.append('=');
                                sb.append(percentEncodeRfc3986(pair.getValue()));
                        }
                }
                return sb.toString();
        }

        private  String percentEncodeRfc3986(String string) {
                try {
                        string = string.replace("+", "%2B");
                        string = URLDecoder.decode(string, "UTF-8");
                        string = URLEncoder.encode(string, "UTF-8");
                        return string.replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
                } catch (Exception e) {
                        return string;
                }
        }

        private static String normalizePath(final String path) {
                return path.replace("%7E", "~").replace(" ", "%20");
        }
}
