package io.durg.tsaheylu.httpclient.localtesting;

import io.durg.tsaheylu.httpclient.ErrorRegistry;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Response;

import java.io.IOException;

public class LoggingEventListener extends EventListener {
    final ErrorRegistry errorRegistry;

    public LoggingEventListener(ErrorRegistry errorRegistry) {
        this.errorRegistry = errorRegistry;
    }

    @Override
    public void callStart(Call call) {
        final double errorPercentage = errorRegistry.getErrorPercentage(call.request().url());
        System.out.println(errorPercentage);
//        System.out.println(String.format("call start %s", call.request().url()));
    }

    @Override
    public void callEnd(Call call) {
        System.out.println(String.format("call end %s", call.request().url()));
    }

    @Override
    public void callFailed(Call call, IOException ioe) {
//        System.out.println(String.format("call failed %s", call.request().url()));
        errorRegistry.recordHttpCall(call.request().url());
        errorRegistry.recordError(call.request().url());
    }


    @Override
    public void responseHeadersEnd(Call call, Response response) {
        final int code = response.code();
        errorRegistry.recordHttpCall(call.request().url());
//        System.out.println(String.format("response headers received %s : %s", call.request().url(), code));
        if (code >= 300)
            errorRegistry.recordError(call.request().url());
    }
}
