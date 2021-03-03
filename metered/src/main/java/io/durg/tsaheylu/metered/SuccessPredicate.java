package io.durg.tsaheylu.metered;

import okhttp3.Response;

public interface SuccessPredicate {

    boolean isSuccess(Response response);
}


