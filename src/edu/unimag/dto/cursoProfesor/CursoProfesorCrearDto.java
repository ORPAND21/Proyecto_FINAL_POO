package edu.unimag.dto.cursoProfesor;

import edu.unimag.modelo.enumeracion.TipoAsignacion;
import java.util.UUID;

public record CursoProfesorCrearDto(
        UUID idCursoPeriodo,
        Long idProfesor,
        TipoAsignacion tipoAsignacion
        ) {

    public CursoProfesorCrearDto {
        if (idCursoPeriodo == null) {
            throw new IllegalArgumentException("id curso null");

        }
        if (idProfesor == null) {
            throw new IllegalArgumentException("id profesor null");

        }
        if (tipoAsignacion == null) {
            throw new IllegalArgumentException("Tipo asignacion null");

        }

    }

}
