package com.minimarket.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(name = "UsuarioResourceDoc", description = "Usuario individual en formato HAL (EntityModel)")
public class UsuarioResourceDoc {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "admin")
    private String username;

    @Schema(example = "[\"ADMIN\"]")
    private Set<String> roles;

    @JsonProperty("_links")
    @Schema(implementation = HalLinks.class)
    private HalLinks links;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public HalLinks getLinks() {
        return links;
    }

    public void setLinks(HalLinks links) {
        this.links = links;
    }
}
