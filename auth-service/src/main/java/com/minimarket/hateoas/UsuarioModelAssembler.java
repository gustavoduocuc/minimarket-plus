package com.minimarket.hateoas;

import com.minimarket.controller.UsuarioController;
import com.minimarket.dto.UsuarioResponseDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.minimarket.hateoas.HateoasLinkSupport.linkToInvocation;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@SuppressWarnings("null")
@Component
public class UsuarioModelAssembler implements RepresentationModelAssembler<UsuarioResponseDto, EntityModel<UsuarioResponseDto>> {

    private final HateoasSecurityHelper hateoasSecurityHelper;

    public UsuarioModelAssembler(HateoasSecurityHelper hateoasSecurityHelper) {
        this.hateoasSecurityHelper = hateoasSecurityHelper;
    }

    @Override
    public EntityModel<UsuarioResponseDto> toModel(@NonNull UsuarioResponseDto usuario) {
        EntityModel<UsuarioResponseDto> model = EntityModel.of(usuario)
                .add(linkToInvocation(methodOn(UsuarioController.class).obtenerUsuarioPorId(usuario.getId())).withSelfRel())
                .add(linkToInvocation(methodOn(UsuarioController.class).listarUsuarios()).withRel("usuarios"));

        if (hateoasSecurityHelper.puedeGestionarUsuarios()) {
            model.add(linkToInvocation(methodOn(UsuarioController.class).crearUsuario(null)).withRel("crearUsuario"))
                    .add(linkToInvocation(methodOn(UsuarioController.class).actualizarUsuario(usuario.getId(), null))
                            .withRel("actualizarUsuario"))
                    .add(linkToInvocation(methodOn(UsuarioController.class).eliminarUsuario(usuario.getId()))
                            .withRel("eliminarUsuario"));
        }

        return model;
    }

    public CollectionModel<EntityModel<UsuarioResponseDto>> toCollectionModel(List<UsuarioResponseDto> usuarios) {
        List<EntityModel<UsuarioResponseDto>> models = usuarios.stream().map(this::toModel).toList();
        CollectionModel<EntityModel<UsuarioResponseDto>> collectionModel = CollectionModel.of(models)
                .add(linkToInvocation(methodOn(UsuarioController.class).listarUsuarios()).withSelfRel())
                .add(linkToInvocation(methodOn(UsuarioController.class).listarUsuarios()).withRel("usuarios"));

        if (hateoasSecurityHelper.puedeGestionarUsuarios()) {
            collectionModel.add(linkToInvocation(methodOn(UsuarioController.class).crearUsuario(null))
                    .withRel("crearUsuario"));
        }

        return collectionModel;
    }
}
