package com.helper.cli.rest;

import java.net.http.HttpResponse;
import java.util.Map;

public interface IRestClient {
    HttpResponse<String> postJson(String url, Object payload, Map<String, String> headers);
    HttpResponse<String> postMultipartPng(String url,
                                          byte[] pngBytes,
                                          String filename,
                                          String descriptionOrNull,
                                          Map<String, String> headers);
}
