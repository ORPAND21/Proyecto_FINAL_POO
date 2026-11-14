package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import edu.unimag.controlador.MonitoriaControlador;
import edu.unimag.dto.monitoria.MonitoriaActualizarDto;
import edu.unimag.dto.monitoria.MonitoriaCrearDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * MODULO DE MONITORIAS (web).
 *
 * La PK de Monitoria es COMPUESTA (curso-periodo + estudiante + profesor), asi que
 * una monitoria concreta se identifica con TRES valores en la ruta:
 *
 *   /api/monitorias/{idCursoPeriodo}/{idEstudiante}/{idProfesor}
 *
 * Rutas:
 *   POST   /api/monitorias                                 -> 1. Crear monitoria
 *   PUT    /api/monitorias/{curso}/{estudiante}/{profesor} -> 2. Evaluar monitoria
 *   DELETE /api/monitorias/{curso}/{estudiante}/{profesor} -> 3. Eliminar monitoria
 *   GET    /api/monitorias                                 -> 4. Listar todas
 *   GET    /api/monitorias?idEstudiante={uuid}             -> 4. Listar por estudiante
 *   GET    /api/monitorias?idProfesor=1                    -> 4. Listar por profesor
 *   GET    /api/monitorias?idCursoPeriodo={uuid}           -> 4. Listar por curso periodo
 */
public class MonitoriaWeb extends ManejadorWeb {

    private final MonitoriaControlador controlador;

    public MonitoriaWeb(MonitoriaControlador controlador) {
        super("/api/monitorias");
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de monitorias no puede ser nulo");
        }
        this.controlador = controlador;
    }

    @Override
    protected void atender(HttpExchange ex, String metodo, List<String> segmentos) throws IOException {
        switch (segmentos.size()) {
            case 0 -> coleccion(ex, metodo);
            case 3 -> monitoriaConcreta(ex, metodo, segmentos);
            default -> noEncontrado(ex);
        }
    }

    // ---- /api/monitorias -------------------------------------------------
    private void coleccion(HttpExchange ex, String metodo) throws IOException {
        if (metodo.equals("GET")) {
            listar(ex);
        } else if (metodo.equals("POST")) {
            crear(ex);
        } else {
            metodoNoPermitido(ex, "GET, POST");
        }
    }

    private void listar(HttpExchange ex) throws IOException {
        Map<String, String> consulta = leerConsulta(ex);
        if (consulta.containsKey("idEstudiante")) {
            UUID idEstudiante = aUuid(consulta.get("idEstudiante"), "idEstudiante");
            responderJson(ex, 200, controlador.listarPorEstudiante(idEstudiante));
        } else if (consulta.containsKey("idProfesor")) {
            Long idProfesor = aLong(consulta.get("idProfesor"), "idProfesor");
            responderJson(ex, 200, controlador.listarPorProfesor(idProfesor));
        } else if (consulta.containsKey("idCursoPeriodo")) {
            UUID idCursoPeriodo = aUuid(consulta.get("idCursoPeriodo"), "idCursoPeriodo");
            responderJson(ex, 200, controlador.listarPorCursoPeriodo(idCursoPeriodo));
        } else {
            responderJson(ex, 200, controlador.listarTodas());
        }
    }

    private void crear(HttpExchange ex) throws IOException {
        Map<String, String> campos = leerCuerpo(ex);
        UUID idCursoPeriodo = aUuid(campos.get("idCursoPeriodo"), "idCursoPeriodo");
        UUID idEstudiante = aUuid(campos.get("idEstudiante"), "idEstudiante");
        Long idProfesor = aLong(campos.get("idProfesor"), "idProfesor");

        MonitoriaCrearDto dto = new MonitoriaCrearDto(idCursoPeriodo, idEstudiante, idProfesor);
        responderJson(ex, 201, controlador.crear(dto));
    }

    // ---- /api/monitorias/{curso}/{estudiante}/{profesor} -----------------
    private void monitoriaConcreta(HttpExchange ex, String metodo, List<String> segmentos)
            throws IOException {
        UUID idCursoPeriodo = aUuid(segmentos.get(0), "idCursoPeriodo");
        UUID idEstudiante = aUuid(segmentos.get(1), "idEstudiante");
        Long idProfesor = aLong(segmentos.get(2), "idProfesor");

        if (metodo.equals("PUT")) {
            Map<String, String> campos = leerCuerpo(ex);
            Integer calificacion = aInteger(campos.get("calificacion"), "calificacion");
            MonitoriaActualizarDto dto = new MonitoriaActualizarDto(
                    idCursoPeriodo, idEstudiante, idProfesor, calificacion, campos.get("comentario"));
            responderJson(ex, 200, controlador.evaluar(dto));
        } else if (metodo.equals("DELETE")) {
            responderJson(ex, 200, Map.of("eliminado",
                    controlador.eliminar(idCursoPeriodo, idEstudiante, idProfesor)));
        } else {
            metodoNoPermitido(ex, "PUT, DELETE");
        }
    }
}
