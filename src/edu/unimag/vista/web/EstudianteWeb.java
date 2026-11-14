package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import edu.unimag.controlador.EstudianteControlador;
import edu.unimag.dto.estudiante.EstudianteActualizarDto;
import edu.unimag.dto.estudiante.EstudianteCrearDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Equivale a EstudianteVista (consola), pero para la web.
 * La PK de Estudiante es un UUID.
 *
 *   GET  /api/estudiantes                    -> 2. Listar (todos)
 *   GET  /api/estudiantes?estado=activos     -> listarActivos   (o "inactivos")
 *   GET  /api/estudiantes/{uuid}             -> buscar por ID
 *   POST /api/estudiantes                    -> 1. Registrar
 *   PUT  /api/estudiantes/{uuid}             -> 3. Actualizar datos de contacto
 *   PUT  /api/estudiantes/{uuid}/activar     -> activar
 *   PUT  /api/estudiantes/{uuid}/inactivar   -> inactivar
 */
public class EstudianteWeb extends ManejadorWeb {

    private final EstudianteControlador controlador;

    public EstudianteWeb(EstudianteControlador controlador) {
        super("/api/estudiantes");
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de estudiantes no puede ser nulo");
        }
        this.controlador = controlador;
    }

    @Override
    protected void atender(HttpExchange ex, String metodo, List<String> segmentos) throws IOException {
        switch (segmentos.size()) {
            case 0 -> coleccion(ex, metodo);
            case 1 -> estudiante(ex, metodo, aUuid(segmentos.get(0), "id"));
            case 2 -> cambiarEstado(ex, metodo, segmentos);
            default -> noEncontrado(ex);
        }
    }

    private void coleccion(HttpExchange ex, String metodo) throws IOException {
        if (metodo.equals("GET")) {
            String estado = leerConsulta(ex).getOrDefault("estado", "todos");
            switch (estado) {
                case "todos" -> responderJson(ex, 200, controlador.listarTodos());
                case "activos" -> responderJson(ex, 200, controlador.listarActivos());
                case "inactivos" -> responderJson(ex, 200, controlador.listarInactivos());
                default -> throw new IllegalArgumentException("El estado debe ser: todos, activos o inactivos");
            }
        } else if (metodo.equals("POST")) {
            Map<String, String> campos = leerCuerpo(ex);
            EstudianteCrearDto dto = new EstudianteCrearDto(
                    campos.get("codigo"),
                    campos.get("nombre"),
                    campos.get("correo"),
                    campos.get("celular"),
                    campos.get("direccion"));
            responderJson(ex, 201, controlador.registrar(dto));
        } else {
            metodoNoPermitido(ex, "GET, POST");
        }
    }

    private void estudiante(HttpExchange ex, String metodo, UUID id) throws IOException {
        if (metodo.equals("GET")) {
            responderJson(ex, 200, controlador.buscarPorId(id));
        } else if (metodo.equals("PUT")) {
            Map<String, String> campos = leerCuerpo(ex);
            // El estado se cambia con /activar e /inactivar, por eso aqui va null
            EstudianteActualizarDto dto = new EstudianteActualizarDto(
                    id,
                    textoOpcional(campos, "correo"),
                    textoOpcional(campos, "celular"),
                    textoOpcional(campos, "direccion"),
                    null);
            responderJson(ex, 200, controlador.actualizar(dto));
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
        UUID id = aUuid(segmentos.get(0), "id");
        responderJson(ex, 200, accion.equals("activar") ? controlador.activar(id) : controlador.inactivar(id));
    }
}
