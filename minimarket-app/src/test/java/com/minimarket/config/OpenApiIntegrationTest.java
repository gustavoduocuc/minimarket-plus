package com.minimarket.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void apiDocsEndpointIsPublicAndContainsCarritoCheckout() throws Exception {
        String apiDocs = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(apiDocs.contains("/api/carrito/checkout"));
        assertTrue(apiDocs.contains("/api/productos/{id}/stock"));
    }

    @Test
    void swaggerUiIsPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void apiDocsIncludesHalSchemasForHateoasResources() throws Exception {
        JsonNode schemas = loadApiDocs().path("components").path("schemas");

        assertSchemaPresent(schemas, "ProductoResourceDoc");
        assertSchemaPresent(schemas, "ProductoCollectionDoc");
        assertSchemaPresent(schemas, "CarritoResourceDoc");
        assertSchemaPresent(schemas, "CarritoCollectionDoc");
        assertSchemaPresent(schemas, "InventarioResourceDoc");
        assertSchemaPresent(schemas, "InventarioCollectionDoc");
        assertSchemaPresent(schemas, "VentaResourceDoc");
        assertSchemaPresent(schemas, "VentaCollectionDoc");
        assertSchemaPresent(schemas, "HalLink");
        assertSchemaPresent(schemas, "HalLinks");
    }

    @Test
    void hateoasListAndGetResponsesReferenceHalSchemas() throws Exception {
        JsonNode paths = loadApiDocs().path("paths");

        assertResponseSchemaRef(paths, "/api/productos", "get", "ProductoCollectionDoc");
        assertResponseSchemaRef(paths, "/api/productos/{id}", "get", "ProductoResourceDoc");
        assertResponseSchemaRef(paths, "/api/carrito", "get", "CarritoResourceDoc");
        assertResponseSchemaRef(paths, "/api/carrito/todos", "get", "CarritoCollectionDoc");
        assertResponseSchemaRef(paths, "/api/inventario", "get", "InventarioCollectionDoc");
        assertResponseSchemaRef(paths, "/api/inventario/{id}", "get", "InventarioResourceDoc");
        assertResponseSchemaRef(paths, "/api/ventas", "get", "VentaCollectionDoc");
        assertResponseSchemaRef(paths, "/api/ventas/{id}", "get", "VentaResourceDoc");
    }

    @Test
    void apiDocsDescriptionMentionsHal() throws Exception {
        String description = loadApiDocs().path("info").path("description").asText();
        assertTrue(description.contains("HAL"));
        assertTrue(description.contains("_links"));
        assertTrue(description.contains("_embedded"));
    }

    private JsonNode loadApiDocs() throws Exception {
        String apiDocs = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(apiDocs);
    }

    private void assertSchemaPresent(JsonNode schemas, String schemaName) {
        assertTrue(schemas.has(schemaName), "Missing schema: " + schemaName);
    }

    private void assertResponseSchemaRef(JsonNode paths, String path, String method, String schemaName) {
        JsonNode schema = paths.path(path).path(method).path("responses").path("200")
                .path("content").path("application/json").path("schema");
        assertNotNull(schema, "Missing 200 application/json schema for " + method.toUpperCase() + " " + path);

        String ref = schema.path("$ref").asText("");
        String composedRef = schema.path("allOf").isArray() && schema.path("allOf").size() > 0
                ? schema.path("allOf").get(0).path("$ref").asText("")
                : "";

        boolean matches = ref.endsWith("/" + schemaName) || composedRef.endsWith("/" + schemaName)
                || schema.toString().contains(schemaName);
        assertTrue(matches, "Expected schema " + schemaName + " for " + method.toUpperCase() + " " + path
                + " but was: " + schema);
    }
}
