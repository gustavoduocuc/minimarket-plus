package com.minimarket.hateoas;

import com.minimarket.controller.InventarioController;
import com.minimarket.controller.ProductoController;
import com.minimarket.entity.Producto;
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
public class ProductoModelAssembler implements RepresentationModelAssembler<Producto, EntityModel<Producto>> {

    private final HateoasSecurityHelper hateoasSecurityHelper;

    public ProductoModelAssembler(HateoasSecurityHelper hateoasSecurityHelper) {
        this.hateoasSecurityHelper = hateoasSecurityHelper;
    }

    @Override
    public EntityModel<Producto> toModel(@NonNull Producto producto) {
        EntityModel<Producto> model = EntityModel.of(producto)
                .add(linkToInvocation(methodOn(ProductoController.class).obtenerProductoPorId(producto.getId()))
                        .withSelfRel())
                .add(linkToInvocation(methodOn(ProductoController.class).listarProductos()).withRel("productos"))
                .add(linkToInvocation(methodOn(ProductoController.class).consultarStockDisponible(producto.getId()))
                        .withRel("stock"));

        if (hateoasSecurityHelper.puedeConsultarInventario()) {
            model.add(linkToInvocation(methodOn(InventarioController.class).listarMovimientosDeInventario())
                    .withRel("inventario"));
        }

        if (hateoasSecurityHelper.puedeGestionarProductos()) {
            model.add(linkToInvocation(methodOn(ProductoController.class).guardarProducto(null)).withRel("crearProducto"))
                    .add(linkToInvocation(methodOn(ProductoController.class).actualizarProducto(producto.getId(), null))
                            .withRel("actualizarProducto"))
                    .add(linkToInvocation(methodOn(ProductoController.class).eliminarProducto(producto.getId()))
                            .withRel("eliminarProducto"));
        }

        return model;
    }

    public CollectionModel<EntityModel<Producto>> toCollectionModel(List<Producto> productos) {
        List<EntityModel<Producto>> models = productos.stream().map(this::toModel).toList();
        CollectionModel<EntityModel<Producto>> collectionModel = CollectionModel.of(models)
                .add(linkToInvocation(methodOn(ProductoController.class).listarProductos()).withSelfRel());

        if (hateoasSecurityHelper.puedeGestionarProductos()) {
            collectionModel.add(linkToInvocation(methodOn(ProductoController.class).guardarProducto(null))
                    .withRel("crearProducto"));
        }

        return collectionModel;
    }
}
