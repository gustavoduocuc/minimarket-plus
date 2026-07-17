package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.client.VentasUsuarioClient;
import com.minimarket.dto.UsuarioRequestDto;
import com.minimarket.dto.UsuarioResponseDto;
import com.minimarket.hateoas.HateoasTestSupport;
import com.minimarket.hateoas.UsuarioModelAssembler;
import com.minimarket.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioModelAssembler usuarioModelAssembler;

    @Mock
    private VentasUsuarioClient ventasUsuarioClient;

    @InjectMocks
    private UsuarioController usuarioController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UsuarioResponseDto usuarioResponse;
    private UsuarioRequestDto usuarioRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
        objectMapper = new ObjectMapper();

        usuarioResponse = new UsuarioResponseDto(1L, "empleado", Set.of("EMPLEADO"));

        usuarioRequest = new UsuarioRequestDto();
        usuarioRequest.setUsername("empleado1");
        usuarioRequest.setPassword("Empleado123!");
        usuarioRequest.setRoles(Set.of("EMPLEADO"));

        HateoasTestSupport.stubUsuarioModelAssembler(usuarioModelAssembler);
    }

    @Test
    void listarUsuarios_retorna200ConLista() throws Exception {
        when(usuarioService.findAll()).thenReturn(List.of(usuarioResponse));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerUsuarioPorId_encontrado_retorna200() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuarioResponse));

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("empleado"));
    }

    @Test
    void obtenerUsuarioPorId_noEncontrado_retorna404() throws Exception {
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearUsuario_retorna200() throws Exception {
        when(usuarioService.create(any(UsuarioRequestDto.class))).thenReturn(usuarioResponse);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(objectMapper.writeValueAsString(usuarioRequest))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("empleado"));

        verify(ventasUsuarioClient).ensureUsuario("empleado");
    }

    @Test
    void actualizarUsuario_encontrado_retorna200() throws Exception {
        when(usuarioService.update(eq(1L), any(UsuarioRequestDto.class)))
                .thenReturn(Optional.of(usuarioResponse));

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(objectMapper.writeValueAsString(usuarioRequest))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void actualizarUsuario_noEncontrado_retorna404() throws Exception {
        when(usuarioService.update(eq(99L), any(UsuarioRequestDto.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/usuarios/99")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(objectMapper.writeValueAsString(usuarioRequest))))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarUsuario_encontrado_retorna204() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuarioResponse));

        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(usuarioService).deleteById(1L);
    }

    @Test
    void eliminarUsuario_noEncontrado_retorna404() throws Exception {
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/usuarios/99"))
                .andExpect(status().isNotFound());
    }
}
