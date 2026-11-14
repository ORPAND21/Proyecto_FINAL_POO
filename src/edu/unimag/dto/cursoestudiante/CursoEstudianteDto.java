package edu.unimag.dto.cursoestudiante;

import edu.unimag.dto.cursoperiodo.CursoPeriodoDto;
import edu.unimag.dto.estudiante.EstudianteDto;
import edu.unimag.modelo.enumeracion.EstadoEntidad;
import java.time.OffsetDateTime;

public record CursoEstudianteDto(
        EstudianteDto estudiante,
        CursoPeriodoDto cursoPeriodo,
        EstadoEntidad estado,
        OffsetDateTime fechaInscripcion) {

}
