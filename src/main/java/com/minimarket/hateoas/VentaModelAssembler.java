package com.minimarket.hateoas;

import com.minimarket.controller.VentaController;
import com.minimarket.entity.EstadoPago;
import com.minimarket.entity.Venta;
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
public class VentaModelAssembler implements RepresentationModelAssembler<Venta, EntityModel<Venta>> {

    private final HateoasSecurityHelper hateoasSecurityHelper;

    public VentaModelAssembler(HateoasSecurityHelper hateoasSecurityHelper) {
        this.hateoasSecurityHelper = hateoasSecurityHelper;
    }

    @Override
    public EntityModel<Venta> toModel(@NonNull Venta venta) {
        EntityModel<Venta> model = EntityModel.of(venta)
                .add(linkToInvocation(methodOn(VentaController.class).obtenerVentaPorId(venta.getId())).withSelfRel())
                .add(linkToInvocation(methodOn(VentaController.class).listarVentas()).withRel("ventas"))
                .add(linkToInvocation(methodOn(VentaController.class).listarVentasPendientes()).withRel("ventasPendientes"));

        if (hateoasSecurityHelper.puedeGestionarVentas()) {
            model.add(linkToInvocation(methodOn(VentaController.class).guardarVenta(null)).withRel("crearVenta"));

            if (venta.getEstadoPago() == EstadoPago.PENDIENTE_PAGO) {
                model.add(linkToInvocation(methodOn(VentaController.class).confirmarPago(venta.getId()))
                        .withRel("confirmarPago"));
            }
        }

        return model;
    }

    public CollectionModel<EntityModel<Venta>> toCollectionModel(List<Venta> ventas) {
        List<EntityModel<Venta>> models = ventas.stream().map(this::toModel).toList();
        CollectionModel<EntityModel<Venta>> collectionModel = CollectionModel.of(models)
                .add(linkToInvocation(methodOn(VentaController.class).listarVentas()).withSelfRel())
                .add(linkToInvocation(methodOn(VentaController.class).listarVentas()).withRel("ventas"))
                .add(linkToInvocation(methodOn(VentaController.class).listarVentasPendientes()).withRel("ventasPendientes"));

        if (hateoasSecurityHelper.puedeGestionarVentas()) {
            collectionModel.add(linkToInvocation(methodOn(VentaController.class).guardarVenta(null)).withRel("crearVenta"));
        }

        return collectionModel;
    }

    public CollectionModel<EntityModel<Venta>> toPendientesCollectionModel(List<Venta> ventas) {
        List<EntityModel<Venta>> models = ventas.stream().map(this::toModel).toList();
        CollectionModel<EntityModel<Venta>> collectionModel = CollectionModel.of(models)
                .add(linkToInvocation(methodOn(VentaController.class).listarVentasPendientes()).withSelfRel())
                .add(linkToInvocation(methodOn(VentaController.class).listarVentas()).withRel("ventas"))
                .add(linkToInvocation(methodOn(VentaController.class).listarVentasPendientes()).withRel("ventasPendientes"));

        if (hateoasSecurityHelper.puedeGestionarVentas()) {
            collectionModel.add(linkToInvocation(methodOn(VentaController.class).guardarVenta(null)).withRel("crearVenta"));
        }

        return collectionModel;
    }
}
