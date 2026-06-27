package com.minimarket.security;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecurityRbacIntegrationTest {

    private static final String emptyJsonBody = "{}";

    @Autowired
    private MockMvc mockMvc;

    private Map<TestRole, String> tokensByRole;

    void authenticateTestUsers() throws Exception {
        tokensByRole = new EnumMap<>(TestRole.class);
        for (TestRole role : TestRole.values()) {
            tokensByRole.put(role, JwtTestHelper.loginAndGetToken(
                    mockMvc, role.username(), role.password()));
        }
    }

    @BeforeAll
    void setUp() throws Exception {
        authenticateTestUsers();
    }

    static Stream<RbacScenario> rbacScenarios() {
        return Stream.of(
                productScenarios(),
                categoryScenarios(),
                cartScenarios(),
                inventoryScenarios(),
                salesScenarios(),
                userScenarios(),
                publicScenarios()
        ).flatMap(stream -> stream);
    }

    private static Stream<RbacScenario> productScenarios() {
        return Stream.of(
                RbacScenario.of(HttpMethod.GET, "/api/productos", null, AccessOutcome.PUBLIC),
                RbacScenario.of(HttpMethod.GET, "/api/productos", TestRole.CLIENTE, AccessOutcome.PUBLIC),
                RbacScenario.of(HttpMethod.POST, "/api/productos", null, AccessOutcome.UNAUTHENTICATED),
                RbacScenario.of(HttpMethod.POST, "/api/productos", TestRole.CLIENTE, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.POST, "/api/productos", TestRole.EMPLEADO, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.POST, "/api/productos", TestRole.GERENTE, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.POST, "/api/productos", TestRole.ADMIN, AccessOutcome.AUTHORIZED)
        );
    }

    private static Stream<RbacScenario> categoryScenarios() {
        return Stream.of(
                RbacScenario.of(HttpMethod.GET, "/api/categorias", null, AccessOutcome.PUBLIC),
                RbacScenario.of(HttpMethod.POST, "/api/categorias", null, AccessOutcome.UNAUTHENTICATED),
                RbacScenario.of(HttpMethod.POST, "/api/categorias", TestRole.CLIENTE, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.POST, "/api/categorias", TestRole.GERENTE, AccessOutcome.AUTHORIZED)
        );
    }

    private static Stream<RbacScenario> cartScenarios() {
        return Stream.of(
                RbacScenario.of(HttpMethod.GET, "/api/carrito", null, AccessOutcome.UNAUTHENTICATED),
                RbacScenario.of(HttpMethod.GET, "/api/carrito", TestRole.CLIENTE, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.GET, "/api/carrito", TestRole.EMPLEADO, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.GET, "/api/carrito", TestRole.GERENTE, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.GET, "/api/carrito", TestRole.ADMIN, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.GET, "/api/carrito/todos", null, AccessOutcome.UNAUTHENTICATED),
                RbacScenario.of(HttpMethod.GET, "/api/carrito/todos", TestRole.CLIENTE, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.GET, "/api/carrito/todos", TestRole.EMPLEADO, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.GET, "/api/carrito/todos", TestRole.GERENTE, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.GET, "/api/carrito/todos", TestRole.ADMIN, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.GET, "/api/carrito/999", null, AccessOutcome.UNAUTHENTICATED),
                RbacScenario.of(HttpMethod.GET, "/api/carrito/999", TestRole.CLIENTE, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.GET, "/api/carrito/999", TestRole.EMPLEADO, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.GET, "/api/carrito/999", TestRole.GERENTE, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.GET, "/api/carrito/999", TestRole.ADMIN, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.POST, "/api/carrito/checkout", null, AccessOutcome.UNAUTHENTICATED),
                RbacScenario.of(HttpMethod.POST, "/api/carrito/checkout", TestRole.CLIENTE, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.POST, "/api/carrito/checkout", TestRole.EMPLEADO, AccessOutcome.AUTHORIZED)
        );
    }

    private static Stream<RbacScenario> inventoryScenarios() {
        return Stream.of(
                RbacScenario.of(HttpMethod.GET, "/api/inventario", null, AccessOutcome.UNAUTHENTICATED),
                RbacScenario.of(HttpMethod.GET, "/api/inventario", TestRole.CLIENTE, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.GET, "/api/inventario", TestRole.EMPLEADO, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.GET, "/api/inventario", TestRole.GERENTE, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.POST, "/api/inventario", null, AccessOutcome.UNAUTHENTICATED),
                RbacScenario.of(HttpMethod.POST, "/api/inventario", TestRole.CLIENTE, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.POST, "/api/inventario", TestRole.EMPLEADO, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.POST, "/api/inventario", TestRole.GERENTE, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.PUT, "/api/inventario/999", null, AccessOutcome.UNAUTHENTICATED),
                RbacScenario.of(HttpMethod.PUT, "/api/inventario/999", TestRole.CLIENTE, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.PUT, "/api/inventario/999", TestRole.EMPLEADO, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.PUT, "/api/inventario/999", TestRole.GERENTE, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.DELETE, "/api/inventario/999", null, AccessOutcome.UNAUTHENTICATED),
                RbacScenario.of(HttpMethod.DELETE, "/api/inventario/999", TestRole.CLIENTE, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.DELETE, "/api/inventario/999", TestRole.EMPLEADO, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.DELETE, "/api/inventario/999", TestRole.GERENTE, AccessOutcome.AUTHORIZED)
        );
    }

    private static Stream<RbacScenario> salesScenarios() {
        return Stream.of(
                RbacScenario.of(HttpMethod.GET, "/api/ventas", null, AccessOutcome.UNAUTHENTICATED),
                RbacScenario.of(HttpMethod.GET, "/api/ventas", TestRole.CLIENTE, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.GET, "/api/ventas", TestRole.EMPLEADO, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.GET, "/api/ventas", TestRole.GERENTE, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.POST, "/api/ventas", null, AccessOutcome.UNAUTHENTICATED),
                RbacScenario.of(HttpMethod.POST, "/api/ventas", TestRole.CLIENTE, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.POST, "/api/ventas", TestRole.EMPLEADO, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.POST, "/api/ventas", TestRole.GERENTE, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.POST, "/api/ventas", TestRole.ADMIN, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.POST, "/api/ventas/1/confirmar-pago", null, AccessOutcome.UNAUTHENTICATED),
                RbacScenario.of(HttpMethod.POST, "/api/ventas/1/confirmar-pago", TestRole.CLIENTE, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.POST, "/api/ventas/1/confirmar-pago", TestRole.EMPLEADO, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.POST, "/api/ventas/1/confirmar-pago", TestRole.GERENTE, AccessOutcome.AUTHORIZED),
                RbacScenario.of(HttpMethod.POST, "/api/ventas/1/confirmar-pago", TestRole.ADMIN, AccessOutcome.AUTHORIZED)
        );
    }

    private static Stream<RbacScenario> userScenarios() {
        return Stream.of(
                RbacScenario.of(HttpMethod.GET, "/api/usuarios", null, AccessOutcome.UNAUTHENTICATED),
                RbacScenario.of(HttpMethod.GET, "/api/usuarios", TestRole.CLIENTE, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.GET, "/api/usuarios", TestRole.GERENTE, AccessOutcome.FORBIDDEN),
                RbacScenario.of(HttpMethod.GET, "/api/usuarios", TestRole.ADMIN, AccessOutcome.AUTHORIZED)
        );
    }

    private static Stream<RbacScenario> publicScenarios() {
        return Stream.of(
                RbacScenario.of(HttpMethod.GET, "/public/hola", null, AccessOutcome.PUBLIC),
                RbacScenario.of(HttpMethod.GET, "/public/hola", TestRole.CLIENTE, AccessOutcome.PUBLIC)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rbacScenarios")
    void enforcesRoleBasedAccess(RbacScenario scenario) throws Exception {
        String token = resolveToken(scenario.role());

        JwtTestHelper.performAuthenticated(
                        mockMvc,
                        scenario.method(),
                        scenario.path(),
                        token,
                        requestBodyFor(scenario.method()))
                .andExpect(matcherFor(scenario.outcome()));
    }

    private String resolveToken(TestRole role) {
        if (role == null) {
            return null;
        }
        return tokensByRole.get(role);
    }

    private static String requestBodyFor(HttpMethod method) {
        if (method == HttpMethod.GET || method == HttpMethod.DELETE) {
            return null;
        }
        return emptyJsonBody;
    }

    private static ResultMatcher matcherFor(AccessOutcome outcome) {
        return switch (outcome) {
            case PUBLIC -> status().isOk();
            case UNAUTHENTICATED -> status().isUnauthorized();
            case FORBIDDEN -> status().isForbidden();
            case AUTHORIZED -> isAuthorized();
        };
    }

    private static ResultMatcher isAuthorized() {
        return result -> {
            int statusCode = result.getResponse().getStatus();
            if (statusCode == 401 || statusCode == 403) {
                throw new AssertionError("Expected authorized access but got HTTP " + statusCode);
            }
        };
    }
}
