package edu.unimag.dto.cursoestudiante;

import edu.unimag.modelo.enumeracion.EstadoInscripcion;
import java.util.UUID;

// Identifica una inscripcion (curso-periodo + estudiante) y el estado de inscripcion
// al que se quiere llevar (ADMITIDO, RECHAZADO, APROBADO, REPROBADO, RETIRADO...)
public record CursoEstudianteActualizarDto(
        UUID idCursoPeriodo,
        UUID idEstudiante,
        EstadoInscripcion estado) {

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
