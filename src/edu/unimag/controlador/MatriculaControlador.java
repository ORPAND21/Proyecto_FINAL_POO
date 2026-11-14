package edu.unimag.controlador;

import edu.unimag.dto.cursoperiodo.CursoPeriodoDto;
import edu.unimag.dto.estudiante.EstudianteDto;
import edu.unimag.dto.matricula.MatriculaCrearDto;
import edu.unimag.dto.matricula.MatriculaDto;
import edu.unimag.dto.materia.MateriaDto;
import edu.unimag.dto.profesor.ProfesorDto;
import edu.unimag.servicio.CursoPeriodoServicio;
import edu.unimag.servicio.EstudianteServicio;
import edu.unimag.servicio.MateriaServicio;
import edu.unimag.servicio.MatriculaServicio;
import edu.unimag.servicio.ProfesorServicio;
import java.util.List;
import java.util.UUID;

public class MatriculaControlador {

    private final MatriculaServicio matriculaServicio;
    private final EstudianteServicio estudianteServicio;
    private final CursoPeriodoServicio cursoPeriodoServicio;
    private final MateriaServicio materiaServicio;
    private final ProfesorServicio profesorServicio;

    public MatriculaControlador(MatriculaServicio matriculaServicio,
            EstudianteServicio estudianteServicio,
            CursoPeriodoServicio cursoPeriodoServicio,
            MateriaServicio materiaServicio,
            ProfesorServicio profesorServicio) {
        if (matriculaServicio == null) {
            throw new IllegalArgumentException("El servicio de matricula no puede ser nulo");
        }
        if (estudianteServicio == null) {
            throw new IllegalArgumentException("El servicio de estudiante no puede ser nulo");
        }
        if (cursoPeriodoServicio == null) {
            throw new IllegalArgumentException("El servicio de curso periodo no puede ser nulo");
        }
        if (materiaServicio == null) {
            throw new IllegalArgumentException("El servicio de materia no puede ser nulo");
        }
        if (profesorServicio == null) {
            throw new IllegalArgumentException("El servicio de profesor no puede ser nulo");
        }
        this.matriculaServicio = matriculaServicio;
        this.estudianteServicio = estudianteServicio;
        this.cursoPeriodoServicio = cursoPeriodoServicio;
        this.materiaServicio = materiaServicio;
        this.profesorServicio = profesorServicio;
    }

    public MatriculaDto matricular(MatriculaCrearDto dto) {
        return matriculaServicio.matricular(dto);
    }

    public MatriculaDto retirar(UUID idEstudiante, UUID idCursoPeriodo,
            Integer idMateria, Integer idPeriodoAcademico, Long idProfesor) {
        return matriculaServicio.retirar(idEstudiante, idCursoPeriodo,
                idMateria, idPeriodoAcademico, idProfesor);
    }

    public MatriculaDto reactivar(UUID idEstudiante, UUID idCursoPeriodo,
            Integer idMateria, Integer idPeriodoAcademico, Long idProfesor) {
        return matriculaServicio.reactivar(idEstudiante, idCursoPeriodo,
                idMateria, idPeriodoAcademico, idProfesor);
    }

    public List<MatriculaDto> listarPorEstudiante(UUID idEstudiante) {
        return matriculaServicio.listarPorEstudiante(idEstudiante);
    }

    public List<MatriculaDto> listarPorCursoPeriodo(UUID idCursoPeriodo) {
        return matriculaServicio.listarPorCursoPeriodo(idCursoPeriodo);
    }

    // Acceso a otros servicios para mostrar datos al usuario
    public EstudianteDto buscarEstudiante(UUID idEstudiante) {
        return estudianteServicio.buscarPorId(idEstudiante);
    }

    public CursoPeriodoDto buscarCursoPeriodo(UUID idCursoPeriodo) {
        return cursoPeriodoServicio.buscarPorId(idCursoPeriodo);
    }

    public MateriaDto buscarMateria(Integer idMateria) {
        return materiaServicio.buscarPorId(idMateria);
    }

    public ProfesorDto buscarProfesor(Long idProfesor) {
        return profesorServicio.buscarPorId(idProfesor);
    }
}
