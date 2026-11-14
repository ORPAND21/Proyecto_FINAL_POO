package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import edu.unimag.controlador.CursoEstudianteControlador;
import edu.unimag.dto.cursoestudiante.CursoEstudianteCrearDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * MODULO DE INSCRIPCIONES DE ESTUDIANTES (web).
 *
 * La PK de CursoEstudiante es COMPUESTA (curso-periodo + estudiante), asi que una
 * inscripcion concreta se identifica con DOS valores en la ruta:
 *
 *   /api/cursos-estudiantes/{idCursoPeriodo}/{idEstudiante}
 *
 * Rutas:
 *   POST /api/cursos-estudiantes                                -> 1. Inscribir estudiante a curso
 *   GET  /api/cursos-estudiantes?idEstudiante={uuid}            -> 2. Listar cursos por estudiante
 *   GET  /api/cursos-estudiantes?idCursoPeriodo={uuid}          -> 3. Listar estudiantes por curso periodo
 *   GET  /api/cursos-estudiantes?idMateria=2                    -> 4. Listar estudiantes por materia
 *   GET  /api/cursos-estudiantes?idMateria=2&idPeriodo=3        -> 5. Listar por materia y periodo
 *   GET  /api/cursos-estudiantes?idPeriodo=3                    -> 6. Listar estudiantes por periodo
 *   PUT  /api/cursos-estudiantes/{curso}/{estudiante}/admitir   -> 7. Admitir inscripcion
 *   PUT  /api/cursos-estudiantes/{curso}/{estudiante}/aprobar   -> 8. Evaluar (aprobar)
 *   PUT  /api/cursos-estudiantes/{curso}/{estudiante}/reprobar  -> 8. Evaluar (reprobar)
 *   PUT  /api/cursos-estudiantes/{curso}/{estudiante}/retirar   -> 9. Retirar estudiante
 *   PUT  /api/cursos-estudiantes/{curso}/{estudiante}/rechazar  -> 10. Rechazar inscripcion
 *   PUT  /api/cursos-estudiantes/{curso}/{estudiante}/reasignar -> reasignar curso (reactivar retirado)
 */
public class CursoEstudianteWeb extends ManejadorWeb {

    private static final List<String> ACCIONES = List.of(
            "admitir", "aprobar", "reprobar", "retirar", "rechazar", "reasignar");

    private final CursoEstudianteControlador controlador;

    public CursoEstudianteWeb(CursoEstudianteControlador controlador) {
        super("/api/cursos-estudiantes");
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de cursos estudiantes no puede ser nulo");
        }
        this.controlador = controlador;
    }

    @Override
    protected void atender(HttpExchange ex, String metodo, List<String> segmentos) throws IOException {
        switch (segmentos.size()) {
            case 0 -> coleccion(ex, metodo);
            case 3 -> accionSobreInscripcion(ex, metodo, segmentos);
            default -> noEncontrado(ex);
        }
    }

    // ---- /api/cursos-estudiantes -----------------------------------------
    private void coleccion(HttpExchange ex, String metodo) throws IOException {
        if (metodo.equals("GET")) {
            listar(ex);
        } else if (metodo.equals("POST")) {
            inscribir(ex);
        } else {
            metodoNoPermitido(ex, "GET, POST");
        }
    }

    private void listar(HttpExchange ex) throws IOException {
        Map<String, String> consulta = leerConsulta(ex);
        boolean hayEstudiante = consulta.containsKey("idEstudiante");
        boolean hayCurso = consulta.containsKey("idCursoPeriodo");
        boolean hayMateria = consulta.containsKey("idMateria");
        boolean hayPeriodo = consulta.containsKey("idPeriodo");

        if (hayEstudiante) {
            UUID idEstudiante = aUuid(consulta.get("idEstudiante"), "idEstudiante");
            responderJson(ex, 200, controlador.listarPorEstudiante(idEstudiante));
        } else if (hayCurso) {
            UUID idCursoPeriodo = aUuid(consulta.get("idCursoPeriodo"), "idCursoPeriodo");
            responderJson(ex, 200, controlador.listarPorCursoPeriodo(idCursoPeriodo));
        } else if (hayMateria && hayPeriodo) {
            Integer idMateria = aInteger(consulta.get("idMateria"), "idMateria");
            Integer idPeriodo = aInteger(consulta.get("idPeriodo"), "idPeriodo");
            responderJson(ex, 200, controlador.listarPorMateriaPeriodo(idMateria, idPeriodo));
        } else if (hayMateria) {
            Integer idMateria = aInteger(consulta.get("idMateria"), "idMateria");
            responderJson(ex, 200, controlador.listarPorMateria(idMateria));
        } else if (hayPeriodo) {
            Integer idPeriodo = aInteger(consulta.get("idPeriodo"), "idPeriodo");
            responderJson(ex, 200, controlador.listarPorPeriodo(idPeriodo));
        } else {
            // Igual que en CursoProfesorWeb: no hay "listar todo", siempre se filtra por algo
            throw new IllegalArgumentException(
                    "Indique un filtro: idEstudiante, idCursoPeriodo, idMateria o idPeriodo");
        }
    }

    private void inscribir(HttpExchange ex) throws IOException {
        Map<String, String> campos = leerCuerpo(ex);
        UUID idCursoPeriodo = aUuid(campos.get("idCursoPeriodo"), "idCursoPeriodo");
        UUID idEstudiante = aUuid(campos.get("idEstudiante"), "idEstudiante");

        CursoEstudianteCrearDto dto = new CursoEstudianteCrearDto(idCursoPeriodo, idEstudiante);
        responderJson(ex, 201, controlador.inscribir(dto));
    }

    // ---- /api/cursos-estudiantes/{curso}/{estudiante}/{accion} -----------
    private void accionSobreInscripcion(HttpExchange ex, String metodo, List<String> segmentos)
            throws IOException {
        String accion = segmentos.get(2);
        if (!ACCIONES.contains(accion)) {
            noEncontrado(ex);
            return;
        }
        if (!metodo.equals("PUT")) {
            metodoNoPermitido(ex, "PUT");
            return;
        }
        UUID idCursoPeriodo = aUuid(segmentos.get(0), "idCursoPeriodo");
        UUID idEstudiante = aUuid(segmentos.get(1), "idEstudiante");

        switch (accion) {
            case "admitir" -> responderJson(ex, 200, controlador.admitir(idCursoPeriodo, idEstudiante));
            case "aprobar" -> responderJson(ex, 200, controlador.aprobar(idCursoPeriodo, idEstudiante));
            case "reprobar" -> responderJson(ex, 200, controlador.reprobar(idCursoPeriodo, idEstudiante));
            case "retirar" -> responderJson(ex, 200, controlador.retirar(idCursoPeriodo, idEstudiante));
            case "rechazar" -> responderJson(ex, 200, controlador.rechazar(idCursoPeriodo, idEstudiante));
            default -> responderJson(ex, 200, controlador.reAsignarCurso(idCursoPeriodo, idEstudiante));
        }
    }
}
