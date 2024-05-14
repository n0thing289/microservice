package com.hmall.api.interceptor;

import com.hmall.common.utils.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class UserInfoRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        template.header("user-info", UserContext.getUser().toString());
    }
}
