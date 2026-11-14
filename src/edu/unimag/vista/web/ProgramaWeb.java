package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import edu.unimag.controlador.ProgramaControlador;
import edu.unimag.dto.programa.ProgramaActualizarDto;
import edu.unimag.dto.programa.ProgramaCrearDto;
import edu.unimag.modelo.enumeracion.NivelPrograma;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Equivale a ProgramaVista (consola), pero para la web.
 * La PK de Programa es un UUID y tiene una Facultad (relacion mutable: se puede cambiar).
 *
 *   GET  /api/programas                       -> 2. Listar todos
 *   GET  /api/programas?idFacultad=1          -> 7. Listar por facultad
 *   GET  /api/programas/{uuid}                -> 3. Buscar por ID
 *   GET  /api/programas/niveles               -> opciones del desplegable de nivel
 *   POST /api/programas                       -> 1. Registrar (nombre, nivelPrograma, idFacultad)
 *   PUT  /api/programas/{uuid}                -> 4. Actualizar (nuevoNombre, nuevoNivel, nuevoIdFacultad)
 *   PUT  /api/programas/{uuid}/suspender      -> 5. Suspender
 *   PUT  /api/programas/{uuid}/reactivar      -> 6. Reactivar
 */
public class ProgramaWeb extends ManejadorWeb {

    private final ProgramaControlador controlador;

    public ProgramaWeb(ProgramaControlador controlador) {
        super("/api/programas");
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de programas no puede ser nulo");
        }
        this.controlador = controlador;
    }

    @Override
    protected void atender(HttpExchange ex, String metodo, List<String> segmentos) throws IOException {
        switch (segmentos.size()) {
            case 0 -> coleccion(ex, metodo);
            case 1 -> {
                if (segmentos.get(0).equals("niveles")) {
                    niveles(ex, metodo);
                } else {
                    programa(ex, metodo, aUuid(segmentos.get(0), "id"));
                }
            }
            case 2 -> cambiarEstado(ex, metodo, segmentos);
            default -> noEncontrado(ex);
        }
    }

    private void coleccion(HttpExchange ex, String metodo) throws IOException {
        if (metodo.equals("GET")) {
            Map<String, String> consulta = leerConsulta(ex);
            if (consulta.containsKey("idFacultad")) {
                responderJson(ex, 200, controlador.listarPorFacultad(aLong(consulta.get("idFacultad"), "idFacultad")));
            } else {
                responderJson(ex, 200, controlador.listarTodos());
            }
        } else if (metodo.equals("POST")) {
            Map<String, String> campos = leerCuerpo(ex);
            ProgramaCrearDto dto = new ProgramaCrearDto(
                    campos.get("nombre"),
                    enumDesdeCodigo(NivelPrograma.class, campos.get("nivelPrograma"), "nivelPrograma"),
                    aLong(campos.get("idFacultad"), "idFacultad"));
            responderJson(ex, 201, controlador.registrar(dto));
        } else {
            metodoNoPermitido(ex, "GET, POST");
        }
    }

    private void niveles(HttpExchange ex, String metodo) throws IOException {
        if (exigirMetodo(ex, metodo, "GET")) {
            responderJson(ex, 200, List.of(NivelPrograma.values()));
        }
    }

    private void programa(HttpExchange ex, String metodo, UUID id) throws IOException {
        if (metodo.equals("GET")) {
            responderJson(ex, 200, controlador.buscarPorId(id));
        } else if (metodo.equals("PUT")) {
            Map<String, String> campos = leerCuerpo(ex);
            ProgramaActualizarDto dto = new ProgramaActualizarDto(
                    id,
                    textoOpcional(campos, "nuevoNombre"),
                    enumOpcional(NivelPrograma.class, campos, "nuevoNivel"),
                    longOpcional(campos, "nuevoIdFacultad"));
            responderJson(ex, 200, controlador.actualizar(dto));
        } else {
            metodoNoPermitido(ex, "GET, PUT");
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
        UUID id = aUuid(segmentos.get(0), "id");
        responderJson(ex, 200, accion.equals("suspender") ? controlador.suspender(id) : controlador.reactivar(id));
    }
}
