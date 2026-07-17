package com.minimarket.exception;

public class InsufficientStockException extends RuntimeException {

    private final String producto;
    private final int disponible;
    private final int solicitado;
    private final String clientMessage;

    public InsufficientStockException(String producto, int disponible, int solicitado) {
        super(buildClientMessage(producto, disponible, solicitado));
        this.producto = producto;
        this.disponible = disponible;
        this.solicitado = solicitado;
        this.clientMessage = buildClientMessage(producto, disponible, solicitado);
    }

    public String getProducto() {
        return producto;
    }

    public int getDisponible() {
        return disponible;
    }

    public int getSolicitado() {
        return solicitado;
    }

    public String getClientMessage() {
        return clientMessage;
    }

    private static String buildClientMessage(String producto, int disponible, int solicitado) {
        return "Stock insuficiente para '" + producto + "'. Solo quedan " + disponible + " unidades.";
    }
}
