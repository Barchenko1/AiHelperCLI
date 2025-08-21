package com.helper.cli.rest;

import java.net.http.HttpResponse;
import java.util.Map;

public interface IRestClient {
    HttpResponse<String> postJson(String url,
                                  Object payload,
                                  String subPrompt,
                                  Map<String, String> headers);
    HttpResponse<String> postMultipartPng(String url,
                                          byte[] pngBytes,
                                          String subPrompt,
                                          Map<String, String> headers);

    HttpResponse<String> postMultipartWav(String url,
                                          byte[] wavBytes,
                                          String subPrompt,
                                          Map<String, String> headers);
}
