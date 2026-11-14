package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import edu.unimag.controlador.CursoProfesorControlador;
import edu.unimag.dto.cursoProfesor.CursoProfesorActualizarDto;
import edu.unimag.dto.cursoProfesor.CursoProfesorCrearDto;
import edu.unimag.modelo.enumeracion.TipoAsignacion;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Equivale a CursoProfesorVista (consola), pero para la web.
 *
 * La PK de CursoProfesor es COMPUESTA (curso-periodo + profesor), asi que una
 * asignacion concreta se identifica con DOS valores en la ruta:
 *
 *   /api/cursos-profesores/{idCursoPeriodo}/{idProfesor}
 *
 * Rutas:
 *   GET  /api/cursos-profesores?idProfesor=1                 -> opcion 2 de la consola
 *   GET  /api/cursos-profesores?idMateria=2                  -> opcion 3
 *   GET  /api/cursos-profesores?idMateria=2&idPeriodo=3      -> opcion 4
 *   GET  /api/cursos-profesores?idPeriodo=3                  -> opcion 5
 *   POST /api/cursos-profesores                              -> opcion 1 (asignar)
 *   PUT  /api/cursos-profesores/{curso}/{profesor}           -> opcion 6 (cambiar tipo)
 *   PUT  /api/cursos-profesores/{curso}/{profesor}/retirar   -> opcion 7
 *   PUT  /api/cursos-profesores/{curso}/{profesor}/reactivar -> opcion 8
 *   GET  /api/cursos-profesores/tipos-asignacion             -> opciones del desplegable
 */
public class CursoProfesorWeb extends ManejadorWeb {

    private final CursoProfesorControlador controlador;

    public CursoProfesorWeb(CursoProfesorControlador controlador) {
        super("/api/cursos-profesores");
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de cursos profesores no puede ser nulo");
        }
        this.controlador = controlador;
    }

    @Override
    protected void atender(HttpExchange ex, String metodo, List<String> segmentos) throws IOException {
        switch (segmentos.size()) {
            case 0 -> coleccion(ex, metodo);
            case 1 -> {
                if (segmentos.get(0).equals("tipos-asignacion")) {
                    tiposAsignacion(ex, metodo);
                } else {
                    noEncontrado(ex);
                }
            }
            case 2 -> asignacionConcreta(ex, metodo, segmentos);
            case 3 -> accionSobreAsignacion(ex, metodo, segmentos);
            default -> noEncontrado(ex);
        }
    }

    // ---- /api/cursos-profesores ------------------------------------------
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
        boolean hayProfesor = consulta.containsKey("idProfesor");
        boolean hayMateria = consulta.containsKey("idMateria");
        boolean hayPeriodo = consulta.containsKey("idPeriodo");

        if (hayProfesor) {
            Long idProfesor = aLong(consulta.get("idProfesor"), "idProfesor");
            responderJson(ex, 200, controlador.listarPorProfesor(idProfesor));
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
            // La consola tampoco tiene "listar todo": siempre se filtra por algo
            throw new IllegalArgumentException("Indique un filtro: idProfesor, idMateria o idPeriodo");
        }
    }

    private void asignar(HttpExchange ex) throws IOException {
        Map<String, String> campos = leerCuerpo(ex);
        UUID idCursoPeriodo = aUuid(campos.get("idCursoPeriodo"), "idCursoPeriodo");
        Long idProfesor = aLong(campos.get("idProfesor"), "idProfesor");
        TipoAsignacion tipo = tipoDesdeCodigo(campos.get("tipoAsignacion"));

        CursoProfesorCrearDto dto = new CursoProfesorCrearDto(idCursoPeriodo, idProfesor, tipo);
        responderJson(ex, 201, controlador.asignar(dto));
    }

    // ---- /api/cursos-profesores/tipos-asignacion ------------------------
    private void tiposAsignacion(HttpExchange ex, String metodo) throws IOException {
        if (metodo.equals("GET")) {
            responderJson(ex, 200, List.of(TipoAsignacion.values()));
        } else {
            metodoNoPermitido(ex, "GET");
        }
    }

    // ---- /api/cursos-profesores/{curso}/{profesor} ----------------------
    private void asignacionConcreta(HttpExchange ex, String metodo, List<String> segmentos)
            throws IOException {
        if (!metodo.equals("PUT")) {
            metodoNoPermitido(ex, "PUT");
            return;
        }
        UUID idCursoPeriodo = aUuid(segmentos.get(0), "idCursoPeriodo");
        Long idProfesor = aLong(segmentos.get(1), "idProfesor");
        TipoAsignacion tipo = tipoDesdeCodigo(leerCuerpo(ex).get("tipoAsignacion"));

        CursoProfesorActualizarDto dto = new CursoProfesorActualizarDto(idCursoPeriodo, idProfesor, tipo);
        responderJson(ex, 200, controlador.cambiarTipoAsignacion(dto));
    }

    // ---- /api/cursos-profesores/{curso}/{profesor}/retirar|reactivar ----
    private void accionSobreAsignacion(HttpExchange ex, String metodo, List<String> segmentos)
            throws IOException {
        String accion = segmentos.get(2);
        if (!accion.equals("retirar") && !accion.equals("reactivar")) {
            noEncontrado(ex);
            return;
        }
        if (!metodo.equals("PUT")) {
            metodoNoPermitido(ex, "PUT");
            return;
        }
        UUID idCursoPeriodo = aUuid(segmentos.get(0), "idCursoPeriodo");
        Long idProfesor = aLong(segmentos.get(1), "idProfesor");

        if (accion.equals("retirar")) {
            responderJson(ex, 200, controlador.retirar(idCursoPeriodo, idProfesor));
        } else {
            responderJson(ex, 200, controlador.reactivar(idCursoPeriodo, idProfesor));
        }
    }

    // ---- utilidades ------------------------------------------------------
    /** Igual que seleccionarTipoAsignacion de la consola: el usuario elige por CODIGO. */
    private TipoAsignacion tipoDesdeCodigo(String valor) {
        int codigo = aInteger(valor, "tipoAsignacion");
        for (TipoAsignacion tipo : TipoAsignacion.values()) {
            if (tipo.getCode() == codigo) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Codigo de tipo de asignacion invalido: " + codigo);
    }
}
