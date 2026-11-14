package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import edu.unimag.controlador.PeriodoAcademicoControlador;
import edu.unimag.dto.periodoacademico.PeriodoAcademicoActualizarDto;
import edu.unimag.dto.periodoacademico.PeriodoAcademicoCrearDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Equivale a PeriodoAcademicoVista (consola), pero para la web.
 *
 *   GET  /api/periodos-academicos                  -> 2. Listar
 *   POST /api/periodos-academicos                  -> 1. Registrar
 *   PUT  /api/periodos-academicos/{id}             -> 3. Actualizar nombre
 *   PUT  /api/periodos-academicos/{id}/suspender   -> 4. Suspender
 *   PUT  /api/periodos-academicos/{id}/reactivar   -> 5. Reactivar
 */
public class PeriodoAcademicoWeb extends ManejadorWeb {

    private final PeriodoAcademicoControlador controlador;

    public PeriodoAcademicoWeb(PeriodoAcademicoControlador controlador) {
        super("/api/periodos-academicos");
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de periodos academicos no puede ser nulo");
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
                    Integer id = aInteger(segmentos.get(0), "id");
                    responderJson(ex, 200, controlador.actualizar(
                            new PeriodoAcademicoActualizarDto(id, textoOpcional(campos, "nuevoNombre"))));
                }
            }
            case 2 -> cambiarEstado(ex, metodo, segmentos);
            default -> noEncontrado(ex);
        }
    }

    private void coleccion(HttpExchange ex, String metodo) throws IOException {
        if (metodo.equals("GET")) {
            responderJson(ex, 200, controlador.listarTodos());
        } else if (metodo.equals("POST")) {
            Map<String, String> campos = leerCuerpo(ex);
            responderJson(ex, 201, controlador.registrar(new PeriodoAcademicoCrearDto(campos.get("nombre"))));
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
        Integer id = aInteger(segmentos.get(0), "id");
        responderJson(ex, 200, accion.equals("suspender") ? controlador.suspender(id) : controlador.reactivar(id));
    }
}
