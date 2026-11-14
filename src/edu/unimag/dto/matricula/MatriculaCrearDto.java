package edu.unimag.dto.matricula;

import java.util.UUID;

/**
 * Datos necesarios para crear una matricula.
 * Los cinco identificadores corresponden a los cinco modelos de la PK.
 */
public record MatriculaCrearDto(
        UUID idEstudiante,
        UUID idCursoPeriodo,
        Integer idMateria,
        Integer idPeriodoAcademico,
        Long idProfesor
        ) {

    public MatriculaCrearDto {
        if (idEstudiante == null) {
            throw new IllegalArgumentException("id estudiante null");
        }
        if (idCursoPeriodo == null) {
            throw new IllegalArgumentException("id curso periodo null");
        }
        if (idMateria == null) {
            throw new IllegalArgumentException("id materia null");
        }
        if (idPeriodoAcademico == null) {
            throw new IllegalArgumentException("id periodo academico null");
        }
        if (idProfesor == null) {
            throw new IllegalArgumentException("id profesor null");
        }
    }
}
