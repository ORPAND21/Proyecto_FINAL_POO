package edu.unimag.dto.cursoestudiante;

import edu.unimag.modelo.enumeracion.EstadoEntidad;
import java.util.UUID;

// Se usa para retirar (INACTIVO) o reactivar (ACTIVO) la inscripcion
public record CursoEstudianteActualizarDto(
        UUID idCursoPeriodo,
        UUID idEstudiante,
        EstadoEntidad estado) {

    public CursoEstudianteActualizarDto {
        if (idCursoPeriodo == null) {
            throw new IllegalArgumentException("id curso null");
        }
        if (idEstudiante == null) {
            throw new IllegalArgumentException("id estudiante null");
        }
        if (estado == null) {
            throw new IllegalArgumentException("estado null");
        }
    }

}
