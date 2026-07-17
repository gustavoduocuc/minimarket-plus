package com.minimarket.hateoas;

import com.minimarket.controller.InventarioController;
import com.minimarket.controller.ProductoController;
import com.minimarket.entity.Inventario;
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
public class InventarioModelAssembler implements RepresentationModelAssembler<Inventario, EntityModel<Inventario>> {

    private final HateoasSecurityHelper hateoasSecurityHelper;

    public InventarioModelAssembler(HateoasSecurityHelper hateoasSecurityHelper) {
        this.hateoasSecurityHelper = hateoasSecurityHelper;
    }

    @Override
    public EntityModel<Inventario> toModel(@NonNull Inventario inventario) {
        EntityModel<Inventario> model = EntityModel.of(inventario)
                .add(linkToInvocation(methodOn(InventarioController.class).obtenerMovimientoPorId(inventario.getId()))
                        .withSelfRel())
                .add(linkToInvocation(methodOn(InventarioController.class).listarMovimientosDeInventario())
                        .withRel("inventario"));

        if (inventario.getProducto() != null && inventario.getProducto().getId() != null) {
            model.add(linkToInvocation(methodOn(ProductoController.class)
                    .obtenerProductoPorId(inventario.getProducto().getId())).withRel("producto"));
        }

        if (hateoasSecurityHelper.puedeGestionarInventario()) {
            model.add(linkToInvocation(methodOn(InventarioController.class).registrarMovimiento(null))
                            .withRel("registrarMovimiento"))
                    .add(linkToInvocation(methodOn(InventarioController.class)
                            .actualizarMovimiento(inventario.getId(), null))
                            .withRel("actualizarMovimiento"))
                    .add(linkToInvocation(methodOn(InventarioController.class).eliminarMovimiento(inventario.getId()))
                            .withRel("eliminarMovimiento"));
        }

        return model;
    }

    public CollectionModel<EntityModel<Inventario>> toCollectionModel(List<Inventario> movimientos) {
        List<EntityModel<Inventario>> models = movimientos.stream().map(this::toModel).toList();
        CollectionModel<EntityModel<Inventario>> collectionModel = CollectionModel.of(models)
                .add(linkToInvocation(methodOn(InventarioController.class).listarMovimientosDeInventario())
                        .withSelfRel())
                .add(linkToInvocation(methodOn(InventarioController.class).listarMovimientosDeInventario())
                        .withRel("inventario"));

        if (hateoasSecurityHelper.puedeGestionarInventario()) {
            collectionModel.add(linkToInvocation(methodOn(InventarioController.class).registrarMovimiento(null))
                    .withRel("registrarMovimiento"));
        }

        return collectionModel;
    }
}
