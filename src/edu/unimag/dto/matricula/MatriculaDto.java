package edu.unimag.dto.matricula;

import edu.unimag.dto.cursoperiodo.CursoPeriodoDto;
import edu.unimag.dto.estudiante.EstudianteDto;
import edu.unimag.dto.materia.MateriaDto;
import edu.unimag.dto.periodoacademico.PeriodoAcademicoDto;
import edu.unimag.dto.profesor.ProfesorDto;
import edu.unimag.modelo.enumeracion.EstadoEntidad;
import java.time.OffsetDateTime;

public record MatriculaDto(
        EstudianteDto estudiante,
        CursoPeriodoDto cursoPeriodo,
        MateriaDto materia,
        PeriodoAcademicoDto periodoAcademico,
        ProfesorDto profesor,
        OffsetDateTime fechaMatricula,
        EstadoEntidad estadoMatricula
        ) {

}
