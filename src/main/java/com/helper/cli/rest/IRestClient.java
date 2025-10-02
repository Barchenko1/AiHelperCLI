package com.helper.cli.rest;

import java.io.File;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public interface IRestClient {
    HttpResponse<String> postJson(String url,
                                  Object payload,
                                  String prompt,
                                  Map<String, String> headers);
    HttpResponse<String> postMultipartPng(String url,
                                          byte[] pngBytes,
                                          String prompt,
                                          Map<String, String> headers);

    HttpResponse<String> postMultipartPngs(String url,
                                           List<File> files,
                                            String prompt,
                                            Map<String, String> headers);

    HttpResponse<String> postMultipartWav(String url,
                                          byte[] wavBytes,
                                          String prompt,
                                          Map<String, String> headers);
}
