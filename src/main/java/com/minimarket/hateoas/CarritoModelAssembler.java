package com.minimarket.hateoas;

import com.minimarket.controller.CarritoController;
import com.minimarket.controller.ProductoController;
import com.minimarket.entity.Carrito;
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
public class CarritoModelAssembler implements RepresentationModelAssembler<Carrito, EntityModel<Carrito>> {

    private final HateoasSecurityHelper hateoasSecurityHelper;

    public CarritoModelAssembler(HateoasSecurityHelper hateoasSecurityHelper) {
        this.hateoasSecurityHelper = hateoasSecurityHelper;
    }

    @Override
    public EntityModel<Carrito> toModel(@NonNull Carrito carrito) {
        return toModelPropio(carrito);
    }

    public EntityModel<Carrito> toModelPropio(Carrito carrito) {
        EntityModel<Carrito> model = EntityModel.of(carrito)
                .add(linkToInvocation(methodOn(CarritoController.class).obtenerCarritoPropio()).withSelfRel())
                .add(linkToInvocation(methodOn(CarritoController.class).obtenerCarritoPropio()).withRel("carrito"))
                .add(linkToInvocation(methodOn(ProductoController.class).listarProductos()).withRel("productos"));

        agregarEnlacesDeOperacion(model);
        return model;
    }

    public EntityModel<Carrito> toModelPorId(Carrito carrito) {
        EntityModel<Carrito> model = EntityModel.of(carrito)
                .add(linkToInvocation(methodOn(CarritoController.class).obtenerCarritoPorId(carrito.getId())).withSelfRel())
                .add(linkToInvocation(methodOn(CarritoController.class).listarTodosLosCarritos()).withRel("carrito"))
                .add(linkToInvocation(methodOn(ProductoController.class).listarProductos()).withRel("productos"));

        if (hateoasSecurityHelper.puedeOperarCarrito()) {
            model.add(linkToInvocation(methodOn(CarritoController.class).agregarProductoAlCarrito(null))
                    .withRel("agregarProducto"));
        }

        return model;
    }

    public CollectionModel<EntityModel<Carrito>> toCollectionModel(List<Carrito> carritos) {
        List<EntityModel<Carrito>> models = carritos.stream().map(this::toModelPorId).toList();
        return CollectionModel.of(models)
                .add(linkToInvocation(methodOn(CarritoController.class).listarTodosLosCarritos()).withSelfRel())
                .add(linkToInvocation(methodOn(CarritoController.class).listarTodosLosCarritos()).withRel("carrito"));
    }

    private void agregarEnlacesDeOperacion(EntityModel<Carrito> model) {
        if (hateoasSecurityHelper.puedeOperarCarrito()) {
            model.add(linkToInvocation(methodOn(CarritoController.class).agregarProductoAlCarrito(null))
                    .withRel("agregarProducto"));
        }

        if (hateoasSecurityHelper.puedeGestionarCarritoPropio()) {
            model.add(linkToInvocation(methodOn(CarritoController.class).quitarProductoDelCarrito(null))
                            .withRel("eliminarProducto"))
                    .add(linkToInvocation(methodOn(CarritoController.class).vaciarCarrito()).withRel("vaciarCarrito"));
        }

        if (hateoasSecurityHelper.estaAutenticado()) {
            model.add(linkToInvocation(methodOn(CarritoController.class).checkout(null)).withRel("checkout"));
        }
    }
}
