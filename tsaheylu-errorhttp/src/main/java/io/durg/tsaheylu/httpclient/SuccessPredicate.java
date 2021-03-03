package io.durg.tsaheylu.httpclient;

import okhttp3.Response;

public interface SuccessPredicate {

    boolean isSuccess(Response response);
}


