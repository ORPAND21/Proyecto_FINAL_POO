package edu.unimag.dto.monitoria;

import edu.unimag.dto.cursoperiodo.CursoPeriodoDto;
import edu.unimag.dto.estudiante.EstudianteDto;
import edu.unimag.dto.profesor.ProfesorDto;
import edu.unimag.modelo.enumeracion.EstadoMonitoria;
import java.time.OffsetDateTime;

public record MonitoriaDto(
        EstudianteDto estudiante,
        ProfesorDto profesor,
        CursoPeriodoDto cursoPeriodo,
        Integer calificacion,
        String comentario,
        EstadoMonitoria estadoMonitoria,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaEvaluacion) {

}
