package io.durg.tsaheylu.httpclient;

import okhttp3.Response;

public class DefaultSuccessPredicate implements SuccessPredicate {
    @Override
    public boolean isSuccess(Response response) {
        return response.isSuccessful();
    }
}