package edu.unimag.dto.monitoria;

import edu.unimag.modelo.constante.PoliticasAcademicas;
import java.util.UUID;

// Se usa para evaluar la monitoria: Monitoria.evaluar(calificacion, comentario)
public record MonitoriaActualizarDto(
        UUID idCursoPeriodo,
        UUID idEstudiante,
        Long idProfesor,
        Integer calificacion,
        String comentario) {

    public MonitoriaActualizarDto {
        if (idCursoPeriodo == null) {
            throw new IllegalArgumentException("id curso null");
        }
        if (idEstudiante == null) {
            throw new IllegalArgumentException("id estudiante null");
        }
        if (idProfesor == null) {
            throw new IllegalArgumentException("id profesor null");
        }
        if (calificacion == null) {
            throw new IllegalArgumentException("calificacion null");
        }
        if (calificacion < PoliticasAcademicas.NOTA_MINIMA || calificacion > PoliticasAcademicas.NOTA_MAXIMA) {
            throw new IllegalArgumentException("La calificacion debe estar entre "
                    + PoliticasAcademicas.NOTA_MINIMA + " y " + PoliticasAcademicas.NOTA_MAXIMA);
        }
        if (comentario == null || comentario.isBlank()) {
            throw new IllegalArgumentException("comentario null");
        }
        comentario = comentario.trim();
    }

}
