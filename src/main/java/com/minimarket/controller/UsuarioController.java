package com.minimarket.controller;

import com.minimarket.dto.UsuarioRequestDto;
import com.minimarket.dto.UsuarioResponseDto;
import com.minimarket.hateoas.UsuarioModelAssembler;
import com.minimarket.openapi.HalExamples;
import com.minimarket.openapi.UsuarioCollectionDoc;
import com.minimarket.openapi.UsuarioResourceDoc;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
@Tag(name = "Usuarios", description = "Administración de usuarios del sistema")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioModelAssembler usuarioModelAssembler;

    @Operation(
            summary = "Listar usuarios",
            description = "Rol: ADMIN.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios en formato HAL (_embedded.usuarioResponseDtoList + _links)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioCollectionDoc.class),
                            examples = @ExampleObject(name = "usuariosHal", value = HalExamples.USUARIO_COLLECTION))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping
    public CollectionModel<EntityModel<UsuarioResponseDto>> listarUsuarios() {
        return usuarioModelAssembler.toCollectionModel(usuarioService.findAll());
    }

    @Operation(
            summary = "Obtener usuario por ID",
            description = "Rol: ADMIN.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado en formato HAL (campos + _links)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResourceDoc.class),
                            examples = @ExampleObject(name = "usuarioHal", value = HalExamples.USUARIO_RESOURCE))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioResponseDto>> obtenerUsuarioPorId(@PathVariable Long id) {
        return usuarioService.findById(id)
                .map(usuarioModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Crear usuario",
            description = "Rol: ADMIN.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario creado en formato HAL (campos + _links)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResourceDoc.class),
                            examples = @ExampleObject(name = "usuarioHal", value = HalExamples.USUARIO_RESOURCE))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping
    public EntityModel<UsuarioResponseDto> crearUsuario(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            name = "Nuevo empleado",
                            value = """
                                    {"username":"empleado1","password":"Empleado123!",\
                                    "roles":["EMPLEADO"]}\
                                    """)))
            @Valid @RequestBody UsuarioRequestDto usuario) {
        return usuarioModelAssembler.toModel(Objects.requireNonNull(usuarioService.create(usuario)));
    }

    @Operation(
            summary = "Actualizar usuario",
            description = "Rol: ADMIN.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado en formato HAL (campos + _links)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResourceDoc.class),
                            examples = @ExampleObject(name = "usuarioHal", value = HalExamples.USUARIO_RESOURCE))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioResponseDto>> actualizarUsuario(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = UsuarioRequestDto.class)))
            @Valid @RequestBody UsuarioRequestDto usuario) {
        return usuarioService.update(id, usuario)
                .map(usuarioModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Eliminar usuario",
            description = "Rol: ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        if (usuarioService.findById(id).isPresent()) {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
