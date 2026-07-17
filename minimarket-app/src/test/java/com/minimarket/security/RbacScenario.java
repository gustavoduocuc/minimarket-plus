package com.minimarket.security;

import org.springframework.http.HttpMethod;

record RbacScenario(
        HttpMethod method,
        String path,
        TestRole role,
        AccessOutcome outcome
) {
    static RbacScenario of(HttpMethod method, String path, TestRole role, AccessOutcome outcome) {
        return new RbacScenario(method, path, role, outcome);
    }

    @Override
    public String toString() {
        String roleLabel = role == null ? "anonymous" : role.name();
        return method + " " + path + " role=" + roleLabel + " expects=" + outcome;
    }
}
