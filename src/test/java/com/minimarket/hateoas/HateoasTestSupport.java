package com.minimarket.hateoas;

import com.minimarket.dto.UsuarioResponseDto;
import com.minimarket.entity.Carrito;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public final class HateoasTestSupport {

    private HateoasTestSupport() {
    }

    public static <T> EntityModel<T> entityModelOf(@org.springframework.lang.NonNull T content) {
        return EntityModel.of(content);
    }

    @SuppressWarnings("null")
    public static void stubUsuarioModelAssembler(UsuarioModelAssembler assembler) {
        when(assembler.toModel(any(UsuarioResponseDto.class)))
                .thenAnswer(invocation -> entityModelOf(
                        Objects.requireNonNull(invocation.getArgument(0, UsuarioResponseDto.class))));
        when(assembler.toCollectionModel(any()))
                .thenAnswer(invocation -> collectionModelOf(invocation.getArgument(0)));
    }

    @SuppressWarnings("null")
    public static void stubInventarioModelAssembler(InventarioModelAssembler assembler) {
        when(assembler.toModel(any(Inventario.class)))
                .thenAnswer(invocation -> entityModelOf(
                        Objects.requireNonNull(invocation.getArgument(0, Inventario.class))));
        when(assembler.toCollectionModel(any()))
                .thenAnswer(invocation -> collectionModelOf(invocation.getArgument(0)));
    }

    @SuppressWarnings("null")
    public static void stubCarritoModelAssembler(CarritoModelAssembler assembler) {
        when(assembler.toModelPropio(any(Carrito.class)))
                .thenAnswer(invocation -> entityModelOf(
                        Objects.requireNonNull(invocation.getArgument(0, Carrito.class))));
        when(assembler.toModelPorId(any(Carrito.class)))
                .thenAnswer(invocation -> entityModelOf(
                        Objects.requireNonNull(invocation.getArgument(0, Carrito.class))));
        when(assembler.toCollectionModel(any()))
                .thenAnswer(invocation -> collectionModelOf(invocation.getArgument(0)));
    }

    @SuppressWarnings("null")
    public static void stubProductoToModel(ProductoModelAssembler assembler) {
        when(assembler.toModel(any(Producto.class)))
                .thenAnswer(invocation -> entityModelOf(
                        Objects.requireNonNull(invocation.getArgument(0, Producto.class))));
    }

    @SuppressWarnings("null")
    public static <T> CollectionModel<EntityModel<T>> collectionModelOf(List<T> content) {
        List<EntityModel<T>> models = content.stream()
                .map(EntityModel::of)
                .collect(Collectors.toList());
        return CollectionModel.of(List.copyOf(models));
    }

    @SuppressWarnings("null")
    public static <T> CollectionModel<EntityModel<T>> collectionModelOf(
            List<T> content,
            Function<T, EntityModel<T>> mapper) {
        List<EntityModel<T>> models = content.stream().map(mapper).collect(Collectors.toList());
        return CollectionModel.of(List.copyOf(models));
    }
}
