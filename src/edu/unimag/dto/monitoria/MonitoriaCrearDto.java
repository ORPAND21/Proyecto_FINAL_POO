package edu.unimag.dto.monitoria;

import java.util.UUID;

public record MonitoriaCrearDto(
        UUID idCursoPeriodo,
        UUID idEstudiante,
        Long idProfesor) {

    public MonitoriaCrearDto {
        if (idCursoPeriodo == null) {
            throw new IllegalArgumentException("id curso null");
        }
        if (idEstudiante == null) {
            throw new IllegalArgumentException("id estudiante null");
        }
        if (idProfesor == null) {
            throw new IllegalArgumentException("id profesor null");
        }
    }

}
