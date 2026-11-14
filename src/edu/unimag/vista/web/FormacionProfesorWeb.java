package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import edu.unimag.controlador.FormacionProfesorControlador;
import edu.unimag.dto.formacion.FormacionProfesorActualizarDto;
import edu.unimag.dto.formacion.FormacionProfesorCrearDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Equivale a FormacionProfesorVista (consola), pero para la web.
 * Una formacion pertenece a UN profesor (relacion 1:N), por eso se registra con idProfesor.
 *
 *   GET    /api/formaciones-profesores                  -> 2. Listar todas
 *   GET    /api/formaciones-profesores?idProfesor=1     -> 3. Listar por profesor
 *   POST   /api/formaciones-profesores                  -> 1. Registrar (idProfesor, titulo, fecha)
 *   PUT    /api/formaciones-profesores/{id}             -> 4. Actualizar (nuevoTitulo, nuevaFecha)
 *   DELETE /api/formaciones-profesores/{id}             -> 5. Eliminar
 *
 * Las fechas viajan como "aaaa-mm-dd".
 */
public class FormacionProfesorWeb extends ManejadorWeb {

    private final FormacionProfesorControlador controlador;

    public FormacionProfesorWeb(FormacionProfesorControlador controlador) {
        super("/api/formaciones-profesores");
        if (controlador == null) {
            throw new IllegalArgumentException("El controlador de formaciones no puede ser nulo");
        }
        this.controlador = controlador;
    }

    @Override
    protected void atender(HttpExchange ex, String metodo, List<String> segmentos) throws IOException {
        switch (segmentos.size()) {
            case 0 -> coleccion(ex, metodo);
            case 1 -> formacion(ex, metodo, aLong(segmentos.get(0), "id"));
            default -> noEncontrado(ex);
        }
    }

    private void coleccion(HttpExchange ex, String metodo) throws IOException {
        if (metodo.equals("GET")) {
            Map<String, String> consulta = leerConsulta(ex);
            if (consulta.containsKey("idProfesor")) {
                responderJson(ex, 200, controlador.listarPorProfesor(aLong(consulta.get("idProfesor"), "idProfesor")));
            } else {
                responderJson(ex, 200, controlador.listarTodos());
            }
        } else if (metodo.equals("POST")) {
            Map<String, String> campos = leerCuerpo(ex);
            FormacionProfesorCrearDto dto = new FormacionProfesorCrearDto(
                    aLong(campos.get("idProfesor"), "idProfesor"),
                    campos.get("titulo"),
                    aFecha(campos.get("fecha"), "fecha"));
            responderJson(ex, 201, controlador.registrar(dto));
        } else {
            metodoNoPermitido(ex, "GET, POST");
        }
    }

    private void formacion(HttpExchange ex, String metodo, Long id) throws IOException {
        if (metodo.equals("PUT")) {
            Map<String, String> campos = leerCuerpo(ex);
            FormacionProfesorActualizarDto dto = new FormacionProfesorActualizarDto(
                    id,
                    textoOpcional(campos, "nuevoTitulo"),
                    fechaOpcional(campos, "nuevaFecha"));
            responderJson(ex, 200, controlador.actualizar(dto));
        } else if (metodo.equals("DELETE")) {
            responderJson(ex, 200, Map.of("eliminado", controlador.eliminar(id)));
        } else {
            metodoNoPermitido(ex, "PUT, DELETE");
        }
    }
}
