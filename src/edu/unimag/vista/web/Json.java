package edu.unimag.vista.web;

import com.cleandev.tpa.api.converter.CodedEnum;

import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traductor mínimo entre Java y JSON.
 *
 * El navegador y Java no se entienden directamente: se mandan TEXTO.
 * El formato de ese texto es JSON. Esta clase hace las dos traducciones:
 *
 *   Java  -> JSON : aJson(...)            (respuesta del servidor)
 *   JSON  -> Java : leerObjetoPlano(...)  (lo que envía el navegador)
 *
 * Limitación consciente: solo lee objetos planos como {"nombre":"Ana","celular":"3001234567"}.
 * En un proyecto real se agrega una librería (Gson o Jackson) como .jar en la carpeta
 * "librerias", igual que ya hiciste con cleandev-scanner-cli.jar y tpa-library-blaster.jar.
 */
public final class Json {

    private Json() {
    }

    // =====================================================================
    // JAVA -> JSON
    // =====================================================================
    public static String aJson(Object valor) {
        StringBuilder sb = new StringBuilder();
        escribir(valor, sb);
        return sb.toString();
    }

    private static void escribir(Object v, StringBuilder sb) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof Number || v instanceof Boolean) {
            sb.append(v);
        } else if (v instanceof Collection<?> coleccion) {
            sb.append('[');
            boolean primero = true;
            for (Object elemento : coleccion) {
                if (!primero) {
                    sb.append(',');
                }
                primero = false;
                escribir(elemento, sb);
            }
            sb.append(']');
        } else if (v instanceof Map<?, ?> mapa) {
            sb.append('{');
            boolean primero = true;
            for (Map.Entry<?, ?> entrada : mapa.entrySet()) {
                if (!primero) {
                    sb.append(',');
                }
                primero = false;
                texto(String.valueOf(entrada.getKey()), sb);
                sb.append(':');
                escribir(entrada.getValue(), sb);
            }
            sb.append('}');
        } else if (v.getClass().isRecord()) {
            // Tus DTOs son records: cada componente se vuelve una clave del JSON.
            sb.append('{');
            boolean primero = true;
            for (RecordComponent componente : v.getClass().getRecordComponents()) {
                if (!primero) {
                    sb.append(',');
                }
                primero = false;
                texto(componente.getName(), sb);
                sb.append(':');
                try {
                    escribir(componente.getAccessor().invoke(v), sb);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("No se pudo leer " + componente.getName(), e);
                }
            }
            sb.append('}');
        } else if (v instanceof Enum<?> enumeracion) {
            if (enumeracion instanceof CodedEnum codificado) {
                // Enum con codigo y descripcion (EstadoEntidad, TipoAsignacion...):
                // {"codigo":1,"nombre":"ACTIVO","descripcion":"Activo"}
                sb.append("{\"codigo\":").append(codificado.getCode()).append(",\"nombre\":");
                texto(enumeracion.name(), sb);
                sb.append(",\"descripcion\":");
                texto(codificado.getDescription(), sb);
                sb.append('}');
            } else {
                texto(enumeracion.name(), sb);
            }
        } else {
            texto(v.toString(), sb); // String, UUID, OffsetDateTime...
        }
    }

    private static void texto(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    // =====================================================================
    // JSON -> JAVA
    // =====================================================================
    public static Map<String, String> leerObjetoPlano(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("El cuerpo de la peticion esta vacio");
        }
        return new Lector(json).objeto();
    }

    private static final class Lector {

        private final String s;
        private int i = 0;

        Lector(String s) {
            this.s = s;
        }

        Map<String, String> objeto() {
            Map<String, String> mapa = new LinkedHashMap<>();
            saltar();
            esperar('{');
            saltar();
            if (mirar() == '}') {
                return mapa;
            }
            while (true) {
                saltar();
                String clave = cadena();
                saltar();
                esperar(':');
                saltar();
                mapa.put(clave, valor());
                saltar();
                char c = tomar();
                if (c == '}') {
                    return mapa;
                }
                if (c != ',') {
                    throw new IllegalArgumentException("JSON invalido: se esperaba ',' o '}'");
                }
            }
        }

        private String valor() {
            if (mirar() == '"') {
                return cadena();
            }
            // numero, true, false o null: se lee hasta la coma o la llave de cierre
            int inicio = i;
            while (i < s.length() && ",}".indexOf(s.charAt(i)) < 0) {
                i++;
            }
            String literal = s.substring(inicio, i).trim();
            return literal.equals("null") ? null : literal;
        }

        private String cadena() {
            esperar('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = tomar();
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                char e = tomar();
                switch (e) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        if (i + 4 > s.length()) {
                            throw new IllegalArgumentException("JSON invalido: escape \\u incompleto");
                        }
                        try {
                            sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        } catch (NumberFormatException ex) {
                            throw new IllegalArgumentException("JSON invalido: escape \\u incorrecto");
                        }
                        i += 4;
                    }
                    default -> sb.append(e); // \" \\ \/
                }
            }
        }

        private void saltar() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
        }

        private char mirar() {
            if (i >= s.length()) {
                throw new IllegalArgumentException("JSON incompleto");
            }
            return s.charAt(i);
        }

        private char tomar() {
            char c = mirar();
            i++;
            return c;
        }

        private void esperar(char esperado) {
            char c = tomar();
            if (c != esperado) {
                throw new IllegalArgumentException("JSON invalido: se esperaba '" + esperado + "'");
            }
        }
    }
}
