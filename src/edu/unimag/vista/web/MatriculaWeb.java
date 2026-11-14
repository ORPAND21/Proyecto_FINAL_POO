package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import edu.unimag.controlador.MatriculaControlador;
import edu.unimag.dto.matricula.MatriculaCrearDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Vista web de Matricula.
 *
 * La PK de Matricula es COMPUESTA y relaciona CINCO modelos, asi que una
 * matricula concreta se identifica con cinco valores en la ruta:
 *
 *   /api/matriculas/{idEstudiante}/{idCursoPeriodo}/{idMateria}/{idPeriodoAcademico}/{idProfesor}
 *
 * Rutas:
 *   GET  /api/matriculas?idEstudiante=...     -> listar por estudiante
 *   GET  /api/matriculas?idCursoPeriodo=...   -> listar por curso periodo
 *   POST /api/matriculas                      -> matricular
 *   PUT  /api/matriculas/{...}/retirar        -> retirar (cambio de estado)
 *   PUT  /api/matriculas/{...}/reactivar      -> reactivar (cambio de estado)
 */
public class MatriculaWeb extends ManejadorWeb {

    private final MatriculaControlador controlador;

    public MatriculaWeb(MatriculaControlador controlador) {
        super("/api/matriculas");
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de matriculas no puede ser nulo");
        }
        this.controlador = controlador;
    }

    @Override
    protected void atender(HttpExchange ex, String metodo, List<String> segmentos) throws IOException {
        switch (segmentos.size()) {
            case 0 -> coleccion(ex, metodo);
            case 6 -> accionSobreMatricula(ex, metodo, segmentos);
            default -> noEncontrado(ex);
        }
    }

    // ---- /api/matriculas ------------------------------------------------
    private void coleccion(HttpExchange ex, String metodo) throws IOException {
        if (metodo.equals("GET")) {
            listar(ex);
        } else if (metodo.equals("POST")) {
            matricular(ex);
        } else {
            metodoNoPermitido(ex, "GET, POST");
        }
    }

    private void listar(HttpExchange ex) throws IOException {
        Map<String, String> consulta = leerConsulta(ex);
        boolean hayEstudiante = consulta.containsKey("idEstudiante");
        boolean hayCursoPeriodo = consulta.containsKey("idCursoPeriodo");

        if (hayEstudiante) {
            UUID idEstudiante = aUuid(consulta.get("idEstudiante"), "idEstudiante");
            responderJson(ex, 200, controlador.listarPorEstudiante(idEstudiante));
        } else if (hayCursoPeriodo) {
            UUID idCursoPeriodo = aUuid(consulta.get("idCursoPeriodo"), "idCursoPeriodo");
            responderJson(ex, 200, controlador.listarPorCursoPeriodo(idCursoPeriodo));
        } else {
            throw new IllegalArgumentException("Indique un filtro: idEstudiante o idCursoPeriodo");
        }
    }

    private void matricular(HttpExchange ex) throws IOException {
        Map<String, String> campos = leerCuerpo(ex);
        UUID idEstudiante = aUuid(campos.get("idEstudiante"), "idEstudiante");
        UUID idCursoPeriodo = aUuid(campos.get("idCursoPeriodo"), "idCursoPeriodo");
        Integer idMateria = aInteger(campos.get("idMateria"), "idMateria");
        Integer idPeriodoAcademico = aInteger(campos.get("idPeriodoAcademico"), "idPeriodoAcademico");
        Long idProfesor = aLong(campos.get("idProfesor"), "idProfesor");

        MatriculaCrearDto dto = new MatriculaCrearDto(
                idEstudiante,
                idCursoPeriodo,
                idMateria,
                idPeriodoAcademico,
                idProfesor);
        responderJson(ex, 201, controlador.matricular(dto));
    }

    // ---- /api/matriculas/{...}/retirar|reactivar ------------------------
    private void accionSobreMatricula(HttpExchange ex, String metodo, List<String> segmentos)
            throws IOException {
        String accion = segmentos.get(5);
        if (!accion.equals("retirar") && !accion.equals("reactivar")) {
            noEncontrado(ex);
            return;
        }
        if (!metodo.equals("PUT")) {
            metodoNoPermitido(ex, "PUT");
            return;
        }

        UUID idEstudiante = aUuid(segmentos.get(0), "idEstudiante");
        UUID idCursoPeriodo = aUuid(segmentos.get(1), "idCursoPeriodo");
        Integer idMateria = aInteger(segmentos.get(2), "idMateria");
        Integer idPeriodoAcademico = aInteger(segmentos.get(3), "idPeriodoAcademico");
        Long idProfesor = aLong(segmentos.get(4), "idProfesor");

        if (accion.equals("retirar")) {
            responderJson(ex, 200, controlador.retirar(idEstudiante, idCursoPeriodo,
                    idMateria, idPeriodoAcademico, idProfesor));
        } else {
            responderJson(ex, 200, controlador.reactivar(idEstudiante, idCursoPeriodo,
                    idMateria, idPeriodoAcademico, idProfesor));
        }
    }
}
