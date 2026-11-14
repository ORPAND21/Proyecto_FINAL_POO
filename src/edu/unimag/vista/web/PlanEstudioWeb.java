package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import edu.unimag.controlador.PlanEstudioControlador;
import edu.unimag.dto.planestudio.PlanEstudioActualizarDto;
import edu.unimag.dto.planestudio.PlanEstudioCrearDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Equivale a PlanEstudioVista (consola), pero para la web.
 * La PK del plan es un Long y pertenece a un Programa (cuya PK es UUID).
 *
 *   GET  /api/planes-estudio                      -> 2. Listar todos
 *   GET  /api/planes-estudio?idPrograma={uuid}    -> 7. Listar por programa
 *   GET  /api/planes-estudio/{id}                 -> 3. Buscar por ID
 *   POST /api/planes-estudio                      -> 1. Registrar (version, idPrograma)
 *   PUT  /api/planes-estudio/{id}                 -> 4. Actualizar version (nuevaVersion)
 *   PUT  /api/planes-estudio/{id}/activar         -> 5. Activar
 *   PUT  /api/planes-estudio/{id}/inactivar       -> 6. Inactivar
 */
public class PlanEstudioWeb extends ManejadorWeb {

    private final PlanEstudioControlador controlador;

    public PlanEstudioWeb(PlanEstudioControlador controlador) {
        super("/api/planes-estudio");
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de planes de estudio no puede ser nulo");
        }
        this.controlador = controlador;
    }

    @Override
    protected void atender(HttpExchange ex, String metodo, List<String> segmentos) throws IOException {
        switch (segmentos.size()) {
            case 0 -> coleccion(ex, metodo);
            case 1 -> plan(ex, metodo, aLong(segmentos.get(0), "id"));
            case 2 -> cambiarEstado(ex, metodo, segmentos);
            default -> noEncontrado(ex);
        }
    }

    private void coleccion(HttpExchange ex, String metodo) throws IOException {
        if (metodo.equals("GET")) {
            Map<String, String> consulta = leerConsulta(ex);
            if (consulta.containsKey("idPrograma")) {
                responderJson(ex, 200, controlador.listarPorPrograma(aUuid(consulta.get("idPrograma"), "idPrograma")));
            } else {
                responderJson(ex, 200, controlador.listarTodos());
            }
        } else if (metodo.equals("POST")) {
            Map<String, String> campos = leerCuerpo(ex);
            PlanEstudioCrearDto dto = new PlanEstudioCrearDto(
                    campos.get("version"),
                    aUuid(campos.get("idPrograma"), "idPrograma"));
            responderJson(ex, 201, controlador.registrar(dto));
        } else {
            metodoNoPermitido(ex, "GET, POST");
        }
    }

    private void plan(HttpExchange ex, String metodo, Long id) throws IOException {
        if (metodo.equals("GET")) {
            responderJson(ex, 200, controlador.buscarPorId(id));
        } else if (metodo.equals("PUT")) {
            Map<String, String> campos = leerCuerpo(ex);
            responderJson(ex, 200, controlador.actualizar(new PlanEstudioActualizarDto(id, campos.get("nuevaVersion"))));
        } else {
            metodoNoPermitido(ex, "GET, PUT");
        }
    }

    private void cambiarEstado(HttpExchange ex, String metodo, List<String> segmentos) throws IOException {
        String accion = segmentos.get(1);
        if (!accion.equals("activar") && !accion.equals("inactivar")) {
            noEncontrado(ex);
            return;
        }
        if (!exigirMetodo(ex, metodo, "PUT")) {
            return;
        }
        Long id = aLong(segmentos.get(0), "id");
        responderJson(ex, 200, accion.equals("activar") ? controlador.activar(id) : controlador.inactivar(id));
    }
}
