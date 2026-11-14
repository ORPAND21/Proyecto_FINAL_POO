package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import edu.unimag.controlador.ProfesorControlador;
import edu.unimag.dto.profesor.ProfesorCrearDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Equivale a ProfesorVista (consola), pero para la web.
 *
 *   GET  /api/profesores   -> listar todos
 *   POST /api/profesores   -> registrar
 */
public class ProfesorWeb extends ManejadorWeb {

    private final ProfesorControlador controlador;

    public ProfesorWeb(ProfesorControlador controlador) {
        super("/api/profesores");
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de profesores no puede ser nulo");
        }
        this.controlador = controlador;
    }

    @Override
    protected void atender(HttpExchange ex, String metodo, List<String> segmentos) throws IOException {
        if (!segmentos.isEmpty()) {
            noEncontrado(ex);
            return;
        }
        if (metodo.equals("GET")) {
            responderJson(ex, 200, controlador.listarTodos());
        } else if (metodo.equals("POST")) {
            Map<String, String> campos = leerCuerpo(ex);
            ProfesorCrearDto dto = new ProfesorCrearDto(campos.get("nombre"), campos.get("celular"));
            responderJson(ex, 201, controlador.registrar(dto));
        } else {
            metodoNoPermitido(ex, "GET, POST");
        }
    }
}
