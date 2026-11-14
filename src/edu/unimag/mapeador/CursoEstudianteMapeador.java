package edu.unimag.mapeador;

import edu.unimag.dto.cursoestudiante.CursoEstudianteDto;
import edu.unimag.dto.cursoperiodo.CursoPeriodoDto;
import edu.unimag.dto.estudiante.EstudianteDto;
import edu.unimag.modelo.CursoEstudiante;
import edu.unimag.modelo.CursoPeriodo;
import edu.unimag.modelo.Estudiante;
import edu.unimag.repositorio.cargador.CursoPeriodoCargador;
import edu.unimag.repositorio.cargador.EstudianteCargador;
import edu.unimag.repositorio.cargador.MateriaCargador;
import edu.unimag.repositorio.cargador.PeriodoAcademicoCargador;
import java.util.ArrayList;
import java.util.List;

public class CursoEstudianteMapeador {

    private final EstudianteMapeador estudianteMapeador;
    private final CursoPeriodoMapeador cursoPeriodoMapeador;

    public CursoEstudianteMapeador(EstudianteMapeador estudianteMapeador,
            CursoPeriodoMapeador cursoPeriodoMapeador) {
        if (estudianteMapeador == null) {
            throw new IllegalArgumentException("Fallo en el mapeador estudiante");
        }
        if (cursoPeriodoMapeador == null) {
            throw new IllegalArgumentException("Fallo en el mapeador curso");
        }
        this.estudianteMapeador = estudianteMapeador;
        this.cursoPeriodoMapeador = cursoPeriodoMapeador;
    }

    public List<CursoEstudianteDto> toDtoList(
            List<CursoEstudiante> entidades,
            EstudianteCargador estudianteCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador) {
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
        List<CursoEstudianteDto> resultado = new ArrayList<>(entidades.size());
        for (CursoEstudiante miEntidad : entidades) {
            if (!validarIntegridad(miEntidad, estudianteCargador,
                    cursoPeriodoCargador,
                    materiaCargador,
                    periodoCargador)) {
                continue;
            }
            resultado.add(transformarDto(miEntidad, estudianteCargador,
                    cursoPeriodoCargador,
                    materiaCargador,
                    periodoCargador));
        }
        return resultado;
    }

    //metodos auxiliares
    private boolean validarIntegridad(CursoEstudiante miEntidad,
            EstudianteCargador estudianteCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador) {
        if (!estudianteCargador.existe(miEntidad.getIdEstudiante())) {
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

    private CursoEstudianteDto transformarDto(CursoEstudiante miEntidad,
            EstudianteCargador estudianteCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador) {
        Estudiante estudiante = estudianteCargador.obtener(miEntidad.getIdEstudiante())
                .orElseThrow(() -> new IllegalStateException("Error de integridad en estudiante"));
        EstudianteDto estudianteDto = estudianteMapeador.toDto(estudiante);

        CursoPeriodo cursoPeriodo = cursoPeriodoCargador
                .obtener(miEntidad.getIdCursoPeriodo())
                .orElseThrow(() -> new IllegalStateException("Error de integridad en curso"));
        CursoPeriodoDto cursoPeriodoDto = cursoPeriodoMapeador.toDto(
                cursoPeriodo,
                materiaCargador,
                periodoCargador);

        return new CursoEstudianteDto(
                estudianteDto,
                cursoPeriodoDto,
                miEntidad.getEstado(),
                miEntidad.getFechaInscripcion());
    }

    public CursoEstudianteDto toDto(
            CursoEstudiante entidad,
            EstudianteCargador estudianteCargador,
            CursoPeriodoCargador cursoPeriodoCargador,
            MateriaCargador materiaCargador,
            PeriodoAcademicoCargador periodoCargador) {
        if (entidad == null) {
            throw new IllegalArgumentException("La entidad CursoEstudiante no puede ser nula");
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
        if (!validarIntegridad(entidad, estudianteCargador, cursoPeriodoCargador,
                materiaCargador, periodoCargador)) {
            if (!estudianteCargador.existe(entidad.getIdEstudiante())) {
                throw new IllegalStateException("Estudiante NO encontrado en el cache");
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
            throw new IllegalStateException("Error con el curso estudiante");
        }
        return transformarDto(entidad, estudianteCargador, cursoPeriodoCargador, materiaCargador, periodoCargador);
    }

}
