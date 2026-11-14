package edu.unimag.vista.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Arranca el servidor HTTP que viene dentro del JDK (com.sun.net.httpserver).
 *
 * Su unico trabajo: registrar los manejadores (uno por entidad: ProfesorWeb,
 * CursoProfesorWeb...) y, opcionalmente, servir los archivos de la carpeta "web".
 * No sabe nada de profesores ni de cursos: eso lo saben los manejadores.
 */
public class ServidorWeb {

    private final HttpServer servidor;
    private final Path carpetaWeb;

    public ServidorWeb(int puerto, Path carpetaWeb, List<ManejadorWeb> manejadores) throws IOException {
        if (carpetaWeb == null) {
            throw new IllegalArgumentException("La carpeta web no puede ser nula");
        }
        if (manejadores == null || manejadores.isEmpty()) {
            throw new IllegalArgumentException("Debe registrar al menos un manejador");
        }
        this.carpetaWeb = carpetaWeb.toAbsolutePath().normalize();

        // Solo acepta conexiones de TU computador (loopback), no de la red.
        this.servidor = HttpServer.create(
                new InetSocketAddress(InetAddress.getLoopbackAddress(), puerto), 0);

        // Cada manejador atiende su ruta base, ej: "/api/profesores"
        for (ManejadorWeb manejador : manejadores) {
            this.servidor.createContext(manejador.getRutaBase(), manejador);
        }
        // Todo lo demas ("/") son archivos: index.html, estilos.css, app.js...
        this.servidor.createContext("/", this::manejarArchivos);
    }

    public void iniciar() {
        servidor.start();
    }

    public void detener() {
        servidor.stop(0);
    }

    private void manejarArchivos(HttpExchange ex) throws IOException {
        try (ex) {
            if (!ex.getRequestMethod().equals("GET")) {
                responderError(ex, 405, "Metodo no permitido");
                return;
            }
            String ruta = ex.getRequestURI().getPath();
            if (ruta.equals("/")) {
                ruta = "/index.html";
            }
            Path archivo;
            try {
                archivo = carpetaWeb.resolve(ruta.substring(1)).normalize();
            } catch (InvalidPathException e) {
                responderError(ex, 404, "No encontrado");
                return;
            }
            // Seguridad: nadie puede pedir "../" para salirse de la carpeta web
            if (!archivo.startsWith(carpetaWeb) || !Files.isRegularFile(archivo)) {
                responderError(ex, 404, "No encontrado");
                return;
            }
            byte[] bytes = Files.readAllBytes(archivo);
            ex.getResponseHeaders().set("Content-Type", tipoContenido(archivo.getFileName().toString()));
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream salida = ex.getResponseBody()) {
                salida.write(bytes);
            }
        }
    }

    private void responderError(HttpExchange ex, int codigo, String mensaje) throws IOException {
        byte[] bytes = Json.aJson(Map.of("error", mensaje)).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream salida = ex.getResponseBody()) {
            salida.write(bytes);
        }
    }

    private String tipoContenido(String nombre) {
        String n = nombre.toLowerCase();
        if (n.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (n.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (n.endsWith(".js")) {
            return "text/javascript; charset=utf-8";
        }
        if (n.endsWith(".json")) {
            return "application/json; charset=utf-8";
        }
        if (n.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (n.endsWith(".png")) {
            return "image/png";
        }
        if (n.endsWith(".ico")) {
            return "image/x-icon";
        }
        return "application/octet-stream";
    }
}
