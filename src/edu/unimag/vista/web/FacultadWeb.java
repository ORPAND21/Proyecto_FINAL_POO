package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import edu.unimag.controlador.FacultadControlador;
import edu.unimag.dto.facultad.FacultadActualizarDto;
import edu.unimag.dto.facultad.FacultadCrearDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Equivale a FacultadVista (consola), pero para la web.
 *
 *   GET /api/facultades                      -> 2. Listar
 *   POST /api/facultades                     -> 1. Registrar
 *   PUT /api/facultades/{id}                 -> 3. Actualizar nombre
 *   PUT /api/facultades/{id}/suspender       -> 4. Suspender
 *   PUT /api/facultades/{id}/reactivar       -> 5. Reactivar
 */
public class FacultadWeb extends ManejadorWeb {

    private final FacultadControlador controlador;

    public FacultadWeb(FacultadControlador controlador) {
        super("/api/facultades");
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de facultades no puede ser nulo");
        }
        this.controlador = controlador;
    }

    @Override
    protected void atender(HttpExchange ex, String metodo, List<String> segmentos) throws IOException {
        switch (segmentos.size()) {
            case 0 -> coleccion(ex, metodo);
            case 1 -> {
                if (exigirMetodo(ex, metodo, "PUT")) {
                    Map<String, String> campos = leerCuerpo(ex);
                    Long id = aLong(segmentos.get(0), "id");
                    responderJson(ex, 200, controlador.actualizar(
                            new FacultadActualizarDto(id, textoOpcional(campos, "nuevoNombre"))));
                }
            }
            case 2 -> cambiarEstado(ex, metodo, segmentos);
            default -> noEncontrado(ex);
        }
    }

    private void coleccion(HttpExchange ex, String metodo) throws IOException {
        if (metodo.equals("GET")) {
            responderJson(ex, 200, controlador.listarTodas());
        } else if (metodo.equals("POST")) {
            Map<String, String> campos = leerCuerpo(ex);
            responderJson(ex, 201, controlador.registrar(new FacultadCrearDto(campos.get("nombre"))));
        } else {
            metodoNoPermitido(ex, "GET, POST");
        }
    }

    private void cambiarEstado(HttpExchange ex, String metodo, List<String> segmentos) throws IOException {
        String accion = segmentos.get(1);
        if (!accion.equals("suspender") && !accion.equals("reactivar")) {
            noEncontrado(ex);
            return;
        }
        if (!exigirMetodo(ex, metodo, "PUT")) {
            return;
        }
        Long id = aLong(segmentos.get(0), "id");
        responderJson(ex, 200, accion.equals("suspender") ? controlador.suspender(id) : controlador.reactivar(id));
    }
}
