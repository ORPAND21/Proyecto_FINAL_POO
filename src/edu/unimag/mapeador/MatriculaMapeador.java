package edu.unimag.mapeador;

import edu.unimag.dto.cursoperiodo.CursoPeriodoDto;
import edu.unimag.dto.matricula.MatriculaDto;
import edu.unimag.modelo.CursoPeriodo;
import edu.unimag.modelo.Estudiante;
import edu.unimag.modelo.Materia;
import edu.unimag.modelo.Matricula;
import edu.unimag.modelo.PeriodoAcademico;
import edu.unimag.modelo.Profesor;
import edu.unimag.repositorio.cargador.CursoPeriodoCargador;
import edu.unimag.repositorio.cargador.EstudianteCargador;
import edu.unimag.repositorio.cargador.MateriaCargador;
import edu.unimag.repositorio.cargador.PeriodoAcademicoCargador;
import edu.unimag.repositorio.cargador.ProfesorCargador;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Convierte Matricula a DTO usando los cinco cargadores de su llave compuesta.
 *
 * Se apoya en el mismo patron de CursoProfesorMapeador: primero valida que
 * todas las referencias esten en cache y despues transforma la entidad.
 */
public class MatriculaMapeador {

    private final EstudianteMapeador estudianteMapeador;
    private final CursoPeriodoMapeador cursoPeriodoMapeador;
    private final MateriaMapeador materiaMapeador;
    private final PeriodoAcademicoMapeador periodoAcademicoMapeador;
    private final ProfesorMapeador profesorMapeador;

    public MatriculaMapeador(EstudianteMapeador estudianteMapeador,
            CursoPeriodoMapeador cursoPeriodoMapeador,
            MateriaMapeador materiaMapeador,
            PeriodoAcademicoMapeador periodoAcademicoMapeador,
            ProfesorMapeador profesorMapeador) {
        if (estudianteMapeador == null) {
            throw new IllegalArgumentException("El mapeador de estudiante no puede ser nulo");
        }
        if (cursoPeriodoMapeador == null) {
            throw new IllegalArgumentException("El mapeador de curso periodo no puede ser nulo");
        }
        if (materiaMapeador == null) {
            throw new IllegalArgumentException("El mapeador de materia no puede ser nulo");
        }
        if (periodoAcademicoMapeador == null) {
            throw new IllegalArgumentException("El mapeador de periodo academico no puede ser nulo");
        }
        if (profesorMapeador == null) {
            throw new IllegalArgumentException("El mapeador de profesor no puede ser nulo");
        }
        this.estudianteMapeador = estudianteMapeador;
        this.cursoPeriodoMapeador = cursoPeriodoMapeador;
        this.materiaMapeador = materiaMapeador;
        this.periodoAcademicoMapeador = periodoAcademicoMapeador;
        this.profesorMapeador = profesorMapeador;
    }

    public List<MatriculaDto> toDtoList(
            List<Matricula> entidades,
            EstudianteCargador estudianteCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador,
            ProfesorCargador profesorCargador) {
        if (entidades == null || entidades.isEmpty()) {
            return List.of();
        }
        if (estudianteCargador == null) {
            throw new IllegalArgumentException("El cargador de estudiante no puede ser nulo");
        }
        if (cursoPeriodoCargador == null) {
            throw new IllegalArgumentException("El cargador de curso periodo no puede ser nulo");
        }
        if (materiaCargador == null) {
            throw new IllegalArgumentException("El cargador de materia no puede ser nulo");
        }
        if (periodoCargador == null) {
            throw new IllegalArgumentException("El cargador de periodo academico no puede ser nulo");
        }
        if (profesorCargador == null) {
            throw new IllegalArgumentException("El cargador de profesor no puede ser nulo");
        }

        List<MatriculaDto> resultado = new ArrayList<>(entidades.size());
        for (Matricula miEntidad : entidades) {
            if (!validarIntegridad(miEntidad, estudianteCargador, cursoPeriodoCargador,
                    materiaCargador, periodoCargador, profesorCargador)) {
                continue;
            }
            resultado.add(transformarDto(miEntidad, estudianteCargador, cursoPeriodoCargador,
                    materiaCargador, periodoCargador, profesorCargador));
        }
        return resultado;
    }

    private boolean validarIntegridad(Matricula miEntidad,
            EstudianteCargador estudianteCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador,
            ProfesorCargador profesorCargador) {
        if (miEntidad == null) {
            return false;
        }
        if (!estudianteCargador.existe(miEntidad.getIdEstudiante())) {
            return false;
        }
        if (!cursoPeriodoCargador.existe(miEntidad.getIdCursoPeriodo())) {
            return false;
        }
        if (!materiaCargador.existe(miEntidad.getIdMateria())) {
            return false;
        }
        if (!periodoCargador.existe(miEntidad.getIdPeriodoAcademico())) {
            return false;
        }
        if (!profesorCargador.existe(miEntidad.getIdProfesor())) {
            return false;
        }

        CursoPeriodo cursoPeriodo = cursoPeriodoCargador
                .obtener(miEntidad.getIdCursoPeriodo())
                .orElse(null);
        if (cursoPeriodo == null) {
            return false;
        }
        // La llave de matricula debe coincidir con la materia y el periodo del curso.
        if (!Objects.equals(cursoPeriodo.getIdMateria(), miEntidad.getIdMateria())) {
            return false;
        }
        if (!Objects.equals(cursoPeriodo.getIdPeriodoAcademico(), miEntidad.getIdPeriodoAcademico())) {
            return false;
        }
        return true;
    }

    private MatriculaDto transformarDto(Matricula miEntidad,
            EstudianteCargador estudianteCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador,
            ProfesorCargador profesorCargador) {
        Estudiante estudiante = estudianteCargador.obtener(miEntidad.getIdEstudiante())
                .orElseThrow(() -> new IllegalStateException("Error de integridad en estudiante"));

        CursoPeriodo cursoPeriodo = cursoPeriodoCargador.obtener(miEntidad.getIdCursoPeriodo())
                .orElseThrow(() -> new IllegalStateException("Error de integridad en curso periodo"));

        Materia materia = materiaCargador.obtener(miEntidad.getIdMateria())
                .orElseThrow(() -> new IllegalStateException("Error de integridad en materia"));

        PeriodoAcademico periodoAcademico = periodoCargador.obtener(miEntidad.getIdPeriodoAcademico())
                .orElseThrow(() -> new IllegalStateException("Error de integridad en periodo academico"));

        Profesor profesor = profesorCargador.obtener(miEntidad.getIdProfesor())
                .orElseThrow(() -> new IllegalStateException("Error de integridad en profesor"));

        CursoPeriodoDto cursoPeriodoDto = cursoPeriodoMapeador.toDto(
                cursoPeriodo,
                materiaCargador,
                periodoCargador);

        return new MatriculaDto(
                estudianteMapeador.toDto(estudiante),
                cursoPeriodoDto,
                materiaMapeador.toDto(materia),
                periodoAcademicoMapeador.toDto(periodoAcademico),
                profesorMapeador.toDto(profesor),
                miEntidad.getFechaMatricula(),
                miEntidad.getEstadoMatricula());
    }

    public MatriculaDto toDto(
            Matricula entidad,
            EstudianteCargador estudianteCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador,
            ProfesorCargador profesorCargador) {
        if (entidad == null) {
            throw new IllegalArgumentException("La entidad Matricula no puede ser nula");
        }
        if (estudianteCargador == null) {
            throw new IllegalArgumentException("El cargador de estudiante no puede ser nulo");
        }
        if (cursoPeriodoCargador == null) {
            throw new IllegalArgumentException("El cargador de curso periodo no puede ser nulo");
        }
        if (materiaCargador == null) {
            throw new IllegalArgumentException("El cargador de materia no puede ser nulo");
        }
        if (periodoCargador == null) {
            throw new IllegalArgumentException("El cargador de periodo academico no puede ser nulo");
        }
        if (profesorCargador == null) {
            throw new IllegalArgumentException("El cargador de profesor no puede ser nulo");
        }

        if (!validarIntegridad(entidad, estudianteCargador, cursoPeriodoCargador,
                materiaCargador, periodoCargador, profesorCargador)) {
            if (!estudianteCargador.existe(entidad.getIdEstudiante())) {
                throw new IllegalStateException("Estudiante NO encontrado en el cache");
            }
            if (!cursoPeriodoCargador.existe(entidad.getIdCursoPeriodo())) {
                throw new IllegalStateException("Curso periodo NO encontrado en el cache");
            }
            if (!materiaCargador.existe(entidad.getIdMateria())) {
                throw new IllegalStateException("Materia NO encontrada en el cache");
            }
            if (!periodoCargador.existe(entidad.getIdPeriodoAcademico())) {
                throw new IllegalStateException("Periodo academico NO encontrado en el cache");
            }
            if (!profesorCargador.existe(entidad.getIdProfesor())) {
                throw new IllegalStateException("Profesor NO encontrado en el cache");
            }
            throw new IllegalStateException(
                    "La materia o el periodo de la matricula no coinciden con el curso periodo");
        }

        return transformarDto(entidad, estudianteCargador, cursoPeriodoCargador,
                materiaCargador, periodoCargador, profesorCargador);
    }
}
