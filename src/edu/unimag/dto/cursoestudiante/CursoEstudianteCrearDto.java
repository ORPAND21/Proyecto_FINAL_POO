package edu.unimag.dto.cursoestudiante;

import java.util.UUID;

public record CursoEstudianteCrearDto(
        UUID idCursoPeriodo,
        UUID idEstudiante) {

    public CursoEstudianteCrearDto {
        if (idCursoPeriodo == null) {
            throw new IllegalArgumentException("id curso null");
        }
        if (idEstudiante == null) {
            throw new IllegalArgumentException("id estudiante null");
        }
    }

}
