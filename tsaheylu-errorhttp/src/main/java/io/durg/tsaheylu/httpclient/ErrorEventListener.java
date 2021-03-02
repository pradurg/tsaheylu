package io.durg.tsaheylu.httpclient;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Response;

import java.io.IOException;

public class ErrorEventListener extends EventListener {

    @Override
    public void callStart(Call call) {
        System.out.println(String.format("call start %s", call.request().url()));
    }

    @Override
    public void callFailed(Call call, IOException ioe) {
        System.out.println(String.format("call failed %s", call.request().url()));
    }

    @Override
    public void responseHeadersEnd(Call call, Response response) {
        System.out.println(String.format("response headers received %s : %s", call.request().url(), response.code()));
    }
}
