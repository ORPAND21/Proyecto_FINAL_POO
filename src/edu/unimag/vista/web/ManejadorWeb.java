package edu.unimag.vista.web;

import com.cleandev.tpa.api.converter.CodedEnum;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Base de todas las "vistas web" (ProfesorWeb, CursoProfesorWeb...).
 *
 * Hace lo que TODAS tienen en comun, para no repetirlo en cada entidad:
 *   - CORS (permiso para que Live Server llame a la API)
 *   - responder con JSON
 *   - traducir excepciones a codigos HTTP (el mismo criterio de los catch de tus vistas de consola)
 *   - leer el cuerpo JSON, la consulta (?clave=valor) y convertir tipos
 *
 * Cada entidad solo implementa atender(...): "que hago con GET, POST, PUT...".
 */
public abstract class ManejadorWeb implements HttpHandler {

    // Origenes (pagina + puerto) autorizados a llamar a la API: los de Live Server
    private static final Set<String> ORIGENES_PERMITIDOS = Set.of(
            "http://127.0.0.1:5500",
            "http://localhost:5500");

    private final String rutaBase;

    protected ManejadorWeb(String rutaBase) {
        if (rutaBase == null || !rutaBase.startsWith("/")) {
            throw new IllegalArgumentException("La ruta base debe empezar con '/'");
        }
        this.rutaBase = rutaBase;
    }

    public String getRutaBase() {
        return rutaBase;
    }

    /**
     * Cada entidad decide que hacer segun el metodo HTTP y la ruta.
     *
     * @param metodo    GET, POST, PUT...
     * @param segmentos lo que viene DESPUES de la ruta base, separado por "/".
     *                  Ej: /api/cursos-profesores/abc/5/retirar -> [abc, 5, retirar]
     */
    protected abstract void atender(HttpExchange ex, String metodo, List<String> segmentos)
            throws IOException;

    @Override
    public final void handle(HttpExchange ex) throws IOException {
        try (ex) {
            try {
                aplicarCors(ex);
                String metodo = ex.getRequestMethod();
                if (metodo.equals("OPTIONS")) {
                    // "Pregunta previa" del navegador: solo confirma que SI esta permitido
                    ex.sendResponseHeaders(204, -1);
                    return;
                }
                List<String> segmentos = segmentosDeRuta(ex);
                if (segmentos == null) {
                    responderJson(ex, 404, error("No encontrado"));
                    return;
                }
                atender(ex, metodo, segmentos);
            } catch (IllegalArgumentException | IllegalStateException e) {
                // Igual que el primer catch de tus vistas: error de validacion
                responderJson(ex, 400, error(e.getMessage()));
            } catch (RuntimeException e) {
                // Igual que el segundo catch: error inesperado del sistema
                responderJson(ex, 500, error("Error del sistema: " + e.getMessage()));
            }
        }
    }

    // =====================================================================
    // Respuestas
    // =====================================================================
    protected void responderJson(HttpExchange ex, int codigo, Object cuerpo) throws IOException {
        String json = (cuerpo instanceof String texto) ? texto : Json.aJson(cuerpo);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream salida = ex.getResponseBody()) {
            salida.write(bytes);
        }
    }

    protected String error(String mensaje) {
        return Json.aJson(Map.of("error", mensaje == null ? "Error desconocido" : mensaje));
    }

    protected void metodoNoPermitido(HttpExchange ex, String permitidos) throws IOException {
        ex.getResponseHeaders().set("Allow", permitidos + ", OPTIONS");
        responderJson(ex, 405, error("Metodo no permitido"));
    }

    /** Devuelve true si el metodo es el esperado; si no, responde 405 y devuelve false. */
    protected boolean exigirMetodo(HttpExchange ex, String metodo, String esperado) throws IOException {
        if (metodo.equals(esperado)) {
            return true;
        }
        metodoNoPermitido(ex, esperado);
        return false;
    }

    protected void noEncontrado(HttpExchange ex) throws IOException {
        responderJson(ex, 404, error("No encontrado"));
    }

    // =====================================================================
    // Lectura de la peticion
    // =====================================================================
    protected Map<String, String> leerCuerpo(HttpExchange ex) throws IOException {
        String cuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return Json.leerObjetoPlano(cuerpo);
    }

    /** Lee ?clave=valor&otra=valor de la direccion. */
    protected Map<String, String> leerConsulta(HttpExchange ex) {
        Map<String, String> mapa = new LinkedHashMap<>();
        String consulta = ex.getRequestURI().getRawQuery();
        if (consulta == null || consulta.isBlank()) {
            return mapa;
        }
        for (String par : consulta.split("&")) {
            int igual = par.indexOf('=');
            String clave = igual < 0 ? par : par.substring(0, igual);
            String valor = igual < 0 ? "" : par.substring(igual + 1);
            mapa.put(URLDecoder.decode(clave, StandardCharsets.UTF_8),
                    URLDecoder.decode(valor, StandardCharsets.UTF_8));
        }
        return mapa;
    }

    // =====================================================================
    // Conversion de tipos: el JSON trae TEXTO, los DTOs piden Long, Integer, UUID...
    // =====================================================================
    protected static Long aLong(String valor, String campo) {
        try {
            return Long.valueOf(requerido(valor, campo));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser un numero entero");
        }
    }

    protected static Integer aInteger(String valor, String campo) {
        try {
            return Integer.valueOf(requerido(valor, campo));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser un numero entero");
        }
    }

    protected static UUID aUuid(String valor, String campo) {
        try {
            return UUID.fromString(requerido(valor, campo));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El campo " + campo + " no es un UUID valido");
        }
    }

    // =====================================================================
    // Campos OPCIONALES: en las actualizaciones, "no enviado" o "vacio" significa
    // "no cambiar" (igual que el [Enter] de tus vistas de consola) y se entrega null al DTO.
    // =====================================================================
    protected static String textoOpcional(Map<String, String> campos, String clave) {
        String valor = campos.get(clave);
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    protected static Integer integerOpcional(Map<String, String> campos, String clave) {
        String valor = textoOpcional(campos, clave);
        return valor == null ? null : aInteger(valor, clave);
    }

    protected static Long longOpcional(Map<String, String> campos, String clave) {
        String valor = textoOpcional(campos, clave);
        return valor == null ? null : aLong(valor, clave);
    }

    // =====================================================================
    // Enums con codigo (NivelPrograma, TipoAsignacion...): el usuario elige por CODIGO,
    // igual que en las vistas de consola ("1 - Tecnico, 2 - Tecnologico...").
    // =====================================================================
    protected static <E extends Enum<E> & CodedEnum> E enumDesdeCodigo(Class<E> tipo, String valor, String campo) {
        int codigo = aInteger(valor, campo);
        for (E constante : tipo.getEnumConstants()) {
            if (constante.getCode() == codigo) {
                return constante;
            }
        }
        throw new IllegalArgumentException("Codigo invalido para " + campo + ": " + codigo);
    }

    protected static <E extends Enum<E> & CodedEnum> E enumOpcional(Class<E> tipo, Map<String, String> campos, String clave) {
        String valor = textoOpcional(campos, clave);
        return valor == null ? null : enumDesdeCodigo(tipo, valor, clave);
    }

    // =====================================================================
    // Fechas: el navegador envia "aaaa-mm-dd" (<input type="date">); los DTOs piden OffsetDateTime.
    // Se guarda a medianoche UTC, asi la parte de la fecha no cambia al mostrarla.
    // =====================================================================
    protected static OffsetDateTime aFecha(String valor, String campo) {
        try {
            return LocalDate.parse(requerido(valor, campo)).atStartOfDay().atOffset(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe tener formato aaaa-mm-dd");
        }
    }

    protected static OffsetDateTime fechaOpcional(Map<String, String> campos, String clave) {
        String valor = textoOpcional(campos, clave);
        return valor == null ? null : aFecha(valor, clave);
    }

    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
        return valor.trim();
    }

    // =====================================================================
    // Internos
    // =====================================================================
    private void aplicarCors(HttpExchange ex) {
        String origen = ex.getRequestHeaders().getFirst("Origin");
        if (origen != null && ORIGENES_PERMITIDOS.contains(origen)) {
            ex.getResponseHeaders().set("Access-Control-Allow-Origin", origen);
            ex.getResponseHeaders().set("Vary", "Origin");
            ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        }
    }

    /** Devuelve los trozos de ruta tras la base, o null si la ruta no corresponde a este manejador. */
    private List<String> segmentosDeRuta(HttpExchange ex) {
        String ruta = ex.getRequestURI().getPath();
        if (!ruta.startsWith(rutaBase)) {
            return null;
        }
        String resto = ruta.substring(rutaBase.length());
        if (!resto.isEmpty() && !resto.startsWith("/")) {
            return null; // "/api/profesoresXYZ" no es "/api/profesores"
        }
        List<String> segmentos = new ArrayList<>();
        for (String trozo : resto.split("/")) {
            if (!trozo.isEmpty()) {
                segmentos.add(trozo);
            }
        }
        return segmentos;
    }
}
