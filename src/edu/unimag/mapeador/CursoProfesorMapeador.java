package edu.unimag.mapeador;

import edu.unimag.dto.cursoProfesor.CursoProfesorDto;
import edu.unimag.dto.cursoperiodo.CursoPeriodoDto;
import edu.unimag.dto.profesor.ProfesorDto;
import edu.unimag.modelo.CursoPeriodo;
import edu.unimag.modelo.CursoProfesor;
import edu.unimag.modelo.Profesor;
import edu.unimag.repositorio.cargador.CursoPeriodoCargador;
import edu.unimag.repositorio.cargador.MateriaCargador;
import edu.unimag.repositorio.cargador.PeriodoAcademicoCargador;
import edu.unimag.repositorio.cargador.ProfesorCargador;
import java.util.ArrayList;
import java.util.List;

public class CursoProfesorMapeador {

    private final ProfesorMapeador profesorMapeador;
    private final CursoPeriodoMapeador cursoPeriodoMapeador;

    public CursoProfesorMapeador(ProfesorMapeador profesorMapeador,
            CursoPeriodoMapeador cursoPeriodoMapeador) {
        if (profesorMapeador == null) {
            throw new IllegalArgumentException("Fallo en el mapeador profesor");

        }
        if (cursoPeriodoMapeador == null) {
            throw new IllegalArgumentException("Fallo en el mapeador curso");
        }
        this.profesorMapeador = profesorMapeador;
        this.cursoPeriodoMapeador = cursoPeriodoMapeador;
    }

    public List<CursoProfesorDto> toDtoList(
            List<CursoProfesor> entidades,
            ProfesorCargador profesorCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador) {
        if (entidades == null || entidades.isEmpty()) {
            return List.of();
        }
        if (profesorCargador == null) {
            throw new IllegalArgumentException("Error en el cargador profesor");
        }
        if (cursoPeriodoCargador == null) {
            throw new IllegalArgumentException("El cargador cursos no puede ser nulo");
        }
        if (materiaCargador == null) {
            throw new IllegalArgumentException("Cargador de materia no puede ser null");
        }
        if (periodoCargador == null) {
            throw new IllegalArgumentException("cargador de materia no puede ser nulo");
        }
        List<CursoProfesorDto> resultado = new ArrayList<>(entidades.size());
        for (CursoProfesor miEntidad : entidades) {
            if (!validarIntegridad(miEntidad, profesorCargador,
                    cursoPeriodoCargador,
                    materiaCargador,
                    periodoCargador)) {
                continue;
            }
            resultado.add(transformarDto(miEntidad, profesorCargador,
                    cursoPeriodoCargador,
                    materiaCargador,
                    periodoCargador));
        }
        return resultado;
    }

    private boolean validarIntegridad(CursoProfesor miEntidad,
            ProfesorCargador profesorCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador) {
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

    private CursoProfesorDto transformarDto(CursoProfesor miEntidad,
            ProfesorCargador profesorCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador) {
        Profesor profesor = profesorCargador.obtener(miEntidad.getIdProfesor()).orElseThrow(() -> new IllegalStateException("Error de integridad"));
        ProfesorDto profesorDto = profesorMapeador.toDto(profesor);

        CursoPeriodo cursoPeriodo = cursoPeriodoCargador
                .obtener(miEntidad.getIdCursoPeriodo())
                .orElseThrow(() -> new IllegalStateException("Error de integridad en curso"));
        CursoPeriodoDto cursoPeriodoDto = cursoPeriodoMapeador.toDto(
                cursoPeriodo,
                materiaCargador,
                periodoCargador);
        return new CursoProfesorDto(
                profesorDto,
                cursoPeriodoDto,
                miEntidad.getTipoAsignacion(),
                miEntidad.getEstado(),
                miEntidad.getFechaAsignacion());
    }

    public CursoProfesorDto toDto(
            CursoProfesor entidad,
            ProfesorCargador profesorCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador) {
        if (entidad == null) {
            throw new IllegalArgumentException("La entidad CursoProfesor no puede ser nula");
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
        if (!validarIntegridad(entidad, profesorCargador, cursoPeriodoCargador,
                materiaCargador, periodoCargador)) {
            if (!profesorCargador.existe(entidad.getIdProfesor())) {
                throw new IllegalStateException("Profesor NO encontrado en el cache");
            }
            if (!cursoPeriodoCargador.existe(entidad.getIdCursoPeriodo())) {
                throw new IllegalStateException("Curso periodo NO encontrado en el cache");
            }
            CursoPeriodo cursoPeriodo = cursoPeriodoCargador.obtener(entidad.getIdCursoPeriodo()).orElse(null);
            if (cursoPeriodo != null) {
                if (!materiaCargador.existe(cursoPeriodo.getIdMateria())) {
                    throw new IllegalStateException("materia NO encontrada en el cache");
                }
                if (!periodoCargador.existe(cursoPeriodo.getIdPeriodoAcademico())) {
                    throw new IllegalStateException("periodo NO encontrado en el cache");
                }
                
            }
            throw new IllegalStateException("Error con el curso profesor");
        }
        return transformarDto(entidad, profesorCargador, cursoPeriodoCargador, materiaCargador, periodoCargador);

    }

}
