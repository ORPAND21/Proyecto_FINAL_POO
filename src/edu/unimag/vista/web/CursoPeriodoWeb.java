package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import edu.unimag.controlador.CursoPeriodoControlador;
import edu.unimag.dto.cursoperiodo.CursoPeriodoActualizarDto;
import edu.unimag.dto.cursoperiodo.CursoPeriodoCrearDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Equivale a CursoPeriodoVista (consola), pero para la web.
 * Un curso por periodo es una materia dictada en un periodo (PK: UUID).
 *
 *   GET    /api/cursos-periodos            -> 2. Listar
 *   POST   /api/cursos-periodos            -> 1. Registrar (nombre, idMateria, idPeriodoAcademico, cupo)
 *   PUT    /api/cursos-periodos/{uuid}     -> 3. Actualizar (nuevoNombre, nuevoCupo)
 *   DELETE /api/cursos-periodos/{uuid}     -> 4. Eliminar
 *
 * Tambien alimenta el desplegable de cursos de la pagina de asignaciones.
 */
public class CursoPeriodoWeb extends ManejadorWeb {

    private final CursoPeriodoControlador controlador;

    public CursoPeriodoWeb(CursoPeriodoControlador controlador) {
        super("/api/cursos-periodos");
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de cursos por periodo no puede ser nulo");
        }
        this.controlador = controlador;
    }

    @Override
    protected void atender(HttpExchange ex, String metodo, List<String> segmentos) throws IOException {
        switch (segmentos.size()) {
            case 0 -> coleccion(ex, metodo);
            case 1 -> curso(ex, metodo, aUuid(segmentos.get(0), "id"));
            default -> noEncontrado(ex);
        }
    }

    private void coleccion(HttpExchange ex, String metodo) throws IOException {
        if (metodo.equals("GET")) {
            responderJson(ex, 200, controlador.listarTodos());
        } else if (metodo.equals("POST")) {
            Map<String, String> campos = leerCuerpo(ex);
            CursoPeriodoCrearDto dto = new CursoPeriodoCrearDto(
                    campos.get("nombre"),
                    aInteger(campos.get("idMateria"), "idMateria"),
                    aInteger(campos.get("idPeriodoAcademico"), "idPeriodoAcademico"),
                    aInteger(campos.get("cupo"), "cupo"));
            responderJson(ex, 201, controlador.registrar(dto));
        } else {
            metodoNoPermitido(ex, "GET, POST");
        }
    }

    private void curso(HttpExchange ex, String metodo, UUID id) throws IOException {
        if (metodo.equals("PUT")) {
            Map<String, String> campos = leerCuerpo(ex);
            CursoPeriodoActualizarDto dto = new CursoPeriodoActualizarDto(
                    id,
                    textoOpcional(campos, "nuevoNombre"),
                    integerOpcional(campos, "nuevoCupo"));
            responderJson(ex, 200, controlador.actualizar(dto));
        } else if (metodo.equals("DELETE")) {
            responderJson(ex, 200, Map.of("eliminado", controlador.eliminar(id)));
        } else {
            metodoNoPermitido(ex, "PUT, DELETE");
        }
    }
}
