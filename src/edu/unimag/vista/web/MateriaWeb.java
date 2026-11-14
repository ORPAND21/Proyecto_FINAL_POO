package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import edu.unimag.controlador.MateriaControlador;
import edu.unimag.dto.materia.MateriaActualizarDto;
import edu.unimag.dto.materia.MateriaCrearDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Equivale a MateriaVista (consola), pero para la web.
 *
 *   GET  /api/materias                   -> 2. Listar
 *   POST /api/materias                   -> 1. Registrar (nombre, creditos, cupoMaximo)
 *   PUT  /api/materias/{id}              -> 3. Actualizar (nuevoNombre, nuevosCreditos, nuevoCupo)
 *   PUT  /api/materias/{id}/suspender    -> 4. Suspender
 *   PUT  /api/materias/{id}/reactivar    -> 5. Reactivar
 */
public class MateriaWeb extends ManejadorWeb {

    private final MateriaControlador controlador;

    public MateriaWeb(MateriaControlador controlador) {
        super("/api/materias");
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de materias no puede ser nulo");
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
                    responderJson(ex, 200, controlador.actualizar(new MateriaActualizarDto(
                            id,
                            textoOpcional(campos, "nuevoNombre"),
                            integerOpcional(campos, "nuevosCreditos"),
                            integerOpcional(campos, "nuevoCupo"))));
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
            MateriaCrearDto dto = new MateriaCrearDto(
                    campos.get("nombre"),
                    aInteger(campos.get("creditos"), "creditos"),
                    aInteger(campos.get("cupoMaximo"), "cupoMaximo"));
            responderJson(ex, 201, controlador.registrar(dto));
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
