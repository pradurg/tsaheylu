package io.durg.tsaheylu.metered;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Response;

import java.io.IOException;

public class ErrorEventListener extends EventListener {
    private final ErrorRegistry errorRegistry;
    private final SuccessPredicate predicate;

    public ErrorEventListener(ErrorRegistry errorRegistry, SuccessPredicate predicate) {
        this.errorRegistry = errorRegistry;
        this.predicate = predicate;
    }

    @Override
    public void callFailed(Call call, IOException ioe) {
//        System.out.println(String.format("call failed %s", call.request().url()));
        errorRegistry.recordHttpCall(call.request().url());
        errorRegistry.recordError(call.request().url());
    }


    @Override
    public void responseHeadersEnd(Call call, Response response) {
        errorRegistry.recordHttpCall(call.request().url());
//        System.out.println(String.format("response headers received %s : %s", call.request().url(), code));
        if (!predicate.isSuccess(response))
            errorRegistry.recordError(call.request().url());
    }
}
