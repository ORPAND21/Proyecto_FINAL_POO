package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import edu.unimag.controlador.PlanEstudioMateriaControlador;
import edu.unimag.dto.planestudiomateria.PlanEstudioMateriaActualizarDto;
import edu.unimag.dto.planestudiomateria.PlanEstudioMateriaCrearDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * MODULO DE ASIGNACIONES DE MATERIAS A PLANES (web).
 *
 * La PK de PlanEstudioMateria es COMPUESTA (plan + materia), asi que una
 * asignacion concreta se identifica con DOS valores en la ruta:
 *
 *   /api/planes-materias/{idPlanEstudio}/{idMateria}
 *
 * Rutas:
 *   POST   /api/planes-materias                       -> 1. Asignar materia a plan
 *   GET    /api/planes-materias?idPlanEstudio=1       -> 2. Listar materias de un plan
 *   GET    /api/planes-materias?idMateria=2           -> 3. Listar planes que incluyen una materia
 *   PUT    /api/planes-materias/{plan}/{materia}      -> 4. Actualizar semestre u orden
 *   DELETE /api/planes-materias/{plan}/{materia}      -> 5. Quitar materia del plan
 */
public class PlanEstudioMateriaWeb extends ManejadorWeb {

    private final PlanEstudioMateriaControlador controlador;

    public PlanEstudioMateriaWeb(PlanEstudioMateriaControlador controlador) {
        super("/api/planes-materias");
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de planes materias no puede ser nulo");
        }
        this.controlador = controlador;
    }

    @Override
    protected void atender(HttpExchange ex, String metodo, List<String> segmentos) throws IOException {
        switch (segmentos.size()) {
            case 0 -> coleccion(ex, metodo);
            case 2 -> asignacionConcreta(ex, metodo, segmentos);
            default -> noEncontrado(ex);
        }
    }

    // ---- /api/planes-materias --------------------------------------------
    private void coleccion(HttpExchange ex, String metodo) throws IOException {
        if (metodo.equals("GET")) {
            listar(ex);
        } else if (metodo.equals("POST")) {
            asignar(ex);
        } else {
            metodoNoPermitido(ex, "GET, POST");
        }
    }

    private void listar(HttpExchange ex) throws IOException {
        Map<String, String> consulta = leerConsulta(ex);
        if (consulta.containsKey("idPlanEstudio")) {
            Long idPlanEstudio = aLong(consulta.get("idPlanEstudio"), "idPlanEstudio");
            responderJson(ex, 200, controlador.listarPorPlan(idPlanEstudio));
        } else if (consulta.containsKey("idMateria")) {
            Integer idMateria = aInteger(consulta.get("idMateria"), "idMateria");
            responderJson(ex, 200, controlador.listarPorMateria(idMateria));
        } else {
            throw new IllegalArgumentException("Indique un filtro: idPlanEstudio o idMateria");
        }
    }

    private void asignar(HttpExchange ex) throws IOException {
        Map<String, String> campos = leerCuerpo(ex);
        Long idPlanEstudio = aLong(campos.get("idPlanEstudio"), "idPlanEstudio");
        Integer idMateria = aInteger(campos.get("idMateria"), "idMateria");
        Integer semestre = aInteger(campos.get("semestre"), "semestre");
        Integer orden = aInteger(campos.get("orden"), "orden");

        PlanEstudioMateriaCrearDto dto = new PlanEstudioMateriaCrearDto(idPlanEstudio, idMateria, semestre, orden);
        responderJson(ex, 201, controlador.asignar(dto));
    }

    // ---- /api/planes-materias/{plan}/{materia} ---------------------------
    private void asignacionConcreta(HttpExchange ex, String metodo, List<String> segmentos)
            throws IOException {
        Long idPlanEstudio = aLong(segmentos.get(0), "idPlanEstudio");
        Integer idMateria = aInteger(segmentos.get(1), "idMateria");

        if (metodo.equals("PUT")) {
            Map<String, String> campos = leerCuerpo(ex);
            Integer semestre = aInteger(campos.get("semestre"), "semestre");
            Integer orden = aInteger(campos.get("orden"), "orden");
            PlanEstudioMateriaActualizarDto dto = new PlanEstudioMateriaActualizarDto(
                    idPlanEstudio, idMateria, semestre, orden);
            responderJson(ex, 200, controlador.actualizar(dto));
        } else if (metodo.equals("DELETE")) {
            responderJson(ex, 200, Map.of("eliminado", controlador.quitar(idPlanEstudio, idMateria)));
        } else {
            metodoNoPermitido(ex, "PUT, DELETE");
        }
    }
}
