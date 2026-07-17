package com.minimarket.hateoas;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.lang.NonNull;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public final class HateoasLinkSupport {

    private HateoasLinkSupport() {
    }

    @NonNull
    public static WebMvcLinkBuilder linkToInvocation(@NonNull Object controllerMethodInvocation) {
        return linkTo(controllerMethodInvocation);
    }
}
