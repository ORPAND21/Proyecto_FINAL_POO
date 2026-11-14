package edu.unimag.mapeador;

import edu.unimag.dto.cursoperiodo.CursoPeriodoDto;
import edu.unimag.dto.estudiante.EstudianteDto;
import edu.unimag.dto.monitoria.MonitoriaDto;
import edu.unimag.dto.profesor.ProfesorDto;
import edu.unimag.modelo.CursoPeriodo;
import edu.unimag.modelo.Estudiante;
import edu.unimag.modelo.Monitoria;
import edu.unimag.modelo.Profesor;
import edu.unimag.repositorio.cargador.CursoPeriodoCargador;
import edu.unimag.repositorio.cargador.EstudianteCargador;
import edu.unimag.repositorio.cargador.MateriaCargador;
import edu.unimag.repositorio.cargador.PeriodoAcademicoCargador;
import edu.unimag.repositorio.cargador.ProfesorCargador;
import java.util.ArrayList;
import java.util.List;

public class MonitoriaMapeador {

    private final EstudianteMapeador estudianteMapeador;
    private final ProfesorMapeador profesorMapeador;
    private final CursoPeriodoMapeador cursoPeriodoMapeador;

    public MonitoriaMapeador(EstudianteMapeador estudianteMapeador,
            ProfesorMapeador profesorMapeador,
            CursoPeriodoMapeador cursoPeriodoMapeador) {
        if (estudianteMapeador == null) {
            throw new IllegalArgumentException("Fallo en el mapeador estudiante");
        }
        if (profesorMapeador == null) {
            throw new IllegalArgumentException("Fallo en el mapeador profesor");
        }
        if (cursoPeriodoMapeador == null) {
            throw new IllegalArgumentException("Fallo en el mapeador curso");
        }
        this.estudianteMapeador = estudianteMapeador;
        this.profesorMapeador = profesorMapeador;
        this.cursoPeriodoMapeador = cursoPeriodoMapeador;
    }

    public List<MonitoriaDto> toDtoList(
            List<Monitoria> entidades,
            EstudianteCargador estudianteCargador,
            ProfesorCargador profesorCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador) {
        if (entidades == null || entidades.isEmpty()) {
            return List.of();
        }
        validarCargadores(estudianteCargador, profesorCargador, cursoPeriodoCargador,
                materiaCargador, periodoCargador);

        List<MonitoriaDto> resultado = new ArrayList<>(entidades.size());
        for (Monitoria miEntidad : entidades) {
            if (!validarIntegridad(miEntidad, estudianteCargador, profesorCargador,
                    cursoPeriodoCargador, materiaCargador, periodoCargador)) {
                continue;
            }
            resultado.add(transformarDto(miEntidad, estudianteCargador, profesorCargador,
                    cursoPeriodoCargador, materiaCargador, periodoCargador));
        }
        return resultado;
    }

    //metodos auxiliares
    private void validarCargadores(EstudianteCargador estudianteCargador,
            ProfesorCargador profesorCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador) {
        if (estudianteCargador == null) {
            throw new IllegalArgumentException("El cargador de estudiante no puede ser nulo");
        }
        if (profesorCargador == null) {
            throw new IllegalArgumentException("El cargador de profesor no puede ser nulo");
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
    }

    private boolean validarIntegridad(Monitoria miEntidad,
            EstudianteCargador estudianteCargador,
            ProfesorCargador profesorCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador) {
        if (!estudianteCargador.existe(miEntidad.getIdEstudiante())) {
            return false;
        }
        if (!profesorCargador.existe(miEntidad.getIdProfesor())) {
            return false;
        }
        if (!cursoPeriodoCargador.existe(miEntidad.getIdCursoPeriodo())) {
            return false;
        }
        CursoPeriodo cursoPeriodo = cursoPeriodoCargador.obtener(miEntidad.getIdCursoPeriodo()).orElse(null);
        if (cursoPeriodo == null) {
            return false;
        }
        if (!materiaCargador.existe(cursoPeriodo.getIdMateria())) {
            return false;
        }
        if (!periodoCargador.existe(cursoPeriodo.getIdPeriodoAcademico())) {
            return false;
        }
        return true;
    }

    private MonitoriaDto transformarDto(Monitoria miEntidad,
            EstudianteCargador estudianteCargador,
            ProfesorCargador profesorCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador) {
        Estudiante estudiante = estudianteCargador.obtener(miEntidad.getIdEstudiante())
                .orElseThrow(() -> new IllegalStateException("Error de integridad en estudiante"));
        EstudianteDto estudianteDto = estudianteMapeador.toDto(estudiante);

        Profesor profesor = profesorCargador.obtener(miEntidad.getIdProfesor())
                .orElseThrow(() -> new IllegalStateException("Error de integridad en profesor"));
        ProfesorDto profesorDto = profesorMapeador.toDto(profesor);

        CursoPeriodo cursoPeriodo = cursoPeriodoCargador
                .obtener(miEntidad.getIdCursoPeriodo())
                .orElseThrow(() -> new IllegalStateException("Error de integridad en curso"));
        CursoPeriodoDto cursoPeriodoDto = cursoPeriodoMapeador.toDto(
                cursoPeriodo,
                materiaCargador,
                periodoCargador);

        return new MonitoriaDto(
                estudianteDto,
                profesorDto,
                cursoPeriodoDto,
                miEntidad.getCalificacionMonitoria(),
                miEntidad.getComentarioMonitoria(),
                miEntidad.getEstadoMonitoria(),
                miEntidad.getFechaCreacionMonitoria(),
                miEntidad.getFechaEvaluacionMonitoria());
    }

    public MonitoriaDto toDto(
            Monitoria entidad,
            EstudianteCargador estudianteCargador,
            ProfesorCargador profesorCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador) {
        if (entidad == null) {
            throw new IllegalArgumentException("La entidad Monitoria no puede ser nula");
        }
        validarCargadores(estudianteCargador, profesorCargador, cursoPeriodoCargador,
                materiaCargador, periodoCargador);

        if (!validarIntegridad(entidad, estudianteCargador, profesorCargador,
                cursoPeriodoCargador, materiaCargador, periodoCargador)) {
            if (!estudianteCargador.existe(entidad.getIdEstudiante())) {
                throw new IllegalStateException("Estudiante NO encontrado en el cache");
            }
            if (!profesorCargador.existe(entidad.getIdProfesor())) {
                throw new IllegalStateException("Profesor NO encontrado en el cache");
            }
            if (!cursoPeriodoCargador.existe(entidad.getIdCursoPeriodo())) {
                throw new IllegalStateException("Curso periodo NO encontrado en el cache");
            }
            CursoPeriodo cursoPeriodo = cursoPeriodoCargador.obtener(entidad.getIdCursoPeriodo()).orElse(null);
            if (cursoPeriodo != null) {
                if (!materiaCargador.existe(cursoPeriodo.getIdMateria())) {
                    throw new IllegalStateException("Materia NO encontrada en el cache");
                }
                if (!periodoCargador.existe(cursoPeriodo.getIdPeriodoAcademico())) {
                    throw new IllegalStateException("Periodo NO encontrado en el cache");
                }
            }
            throw new IllegalStateException("Error con la monitoria");
        }
        return transformarDto(entidad, estudianteCargador, profesorCargador,
                cursoPeriodoCargador, materiaCargador, periodoCargador);
    }

}
