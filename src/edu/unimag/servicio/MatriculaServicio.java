package edu.unimag.servicio;

import edu.unimag.dto.matricula.MatriculaCrearDto;
import edu.unimag.dto.matricula.MatriculaDto;
import edu.unimag.mapeador.MatriculaMapeador;
import edu.unimag.modelo.CursoPeriodo;
import edu.unimag.modelo.Estudiante;
import edu.unimag.modelo.Materia;
import edu.unimag.modelo.Matricula;
import edu.unimag.modelo.PeriodoAcademico;
import edu.unimag.modelo.Profesor;
import edu.unimag.modelo.pk.MatriculaPk;
import edu.unimag.repositorio.CursoPeriodoRepositorio;
import edu.unimag.repositorio.EstudianteRepositorio;
import edu.unimag.repositorio.MateriaRepositorio;
import edu.unimag.repositorio.MatriculaRepositorio;
import edu.unimag.repositorio.PeriodoAcademicoRepositorio;
import edu.unimag.repositorio.ProfesorRepositorio;
import edu.unimag.repositorio.cargador.CursoPeriodoCargador;
import edu.unimag.repositorio.cargador.EstudianteCargador;
import edu.unimag.repositorio.cargador.MateriaCargador;
import edu.unimag.repositorio.cargador.PeriodoAcademicoCargador;
import edu.unimag.repositorio.cargador.ProfesorCargador;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Logica de negocio de Matricula.
 *
 * La llave compuesta relaciona cinco modelos y el servicio valida que el
 * curso periodo corresponda realmente a la materia y al periodo indicados.
 * Cada operacion termina en un metodo auxiliar de hidratacion, igual que
 * CursoProfesorServicio.
 */
public class MatriculaServicio {

    private final MatriculaRepositorio matriculaRepositorio;
    private final EstudianteRepositorio estudianteRepositorio;
    private final CursoPeriodoRepositorio cursoPeriodoRepositorio;
    private final MateriaRepositorio materiaRepositorio;
    private final PeriodoAcademicoRepositorio periodoAcademicoRepositorio;
    private final ProfesorRepositorio profesorRepositorio;
    private final MatriculaMapeador mapeador;

    public MatriculaServicio(MatriculaRepositorio matriculaRepositorio,
            EstudianteRepositorio estudianteRepositorio,
            CursoPeriodoRepositorio cursoPeriodoRepositorio,
            MateriaRepositorio materiaRepositorio,
            PeriodoAcademicoRepositorio periodoAcademicoRepositorio,
            ProfesorRepositorio profesorRepositorio,
            MatriculaMapeador mapeador) {
        if (matriculaRepositorio == null) {
            throw new IllegalArgumentException("Repo de matricula no puede ser nulo");
        }
        if (estudianteRepositorio == null) {
            throw new IllegalArgumentException("Repo de estudiante no puede ser nulo");
        }
        if (cursoPeriodoRepositorio == null) {
            throw new IllegalArgumentException("Repo de curso periodo no puede ser nulo");
        }
        if (materiaRepositorio == null) {
            throw new IllegalArgumentException("Repo de materia no puede ser nulo");
        }
        if (periodoAcademicoRepositorio == null) {
            throw new IllegalArgumentException("Repo de periodo academico no puede ser nulo");
        }
        if (profesorRepositorio == null) {
            throw new IllegalArgumentException("Repo de profesor no puede ser nulo");
        }
        if (mapeador == null) {
            throw new IllegalArgumentException("El mapeador no puede ser nulo");
        }
        this.matriculaRepositorio = matriculaRepositorio;
        this.estudianteRepositorio = estudianteRepositorio;
        this.cursoPeriodoRepositorio = cursoPeriodoRepositorio;
        this.materiaRepositorio = materiaRepositorio;
        this.periodoAcademicoRepositorio = periodoAcademicoRepositorio;
        this.profesorRepositorio = profesorRepositorio;
        this.mapeador = mapeador;
    }

    public MatriculaDto matricular(MatriculaCrearDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de matricula son obligatorios");
        }
        Estudiante estudiante = estudianteRepositorio.buscarPorId(dto.idEstudiante())
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(dto.idCursoPeriodo())
                .orElseThrow(() -> new IllegalArgumentException("No existe el curso periodo"));
        Materia materia = materiaRepositorio.buscarPorId(dto.idMateria())
                .orElseThrow(() -> new IllegalArgumentException("No existe la materia"));
        PeriodoAcademico periodoAcademico = periodoAcademicoRepositorio.buscarPorId(dto.idPeriodoAcademico())
                .orElseThrow(() -> new IllegalArgumentException("No existe el periodo academico"));
        Profesor profesor = profesorRepositorio.buscarPorId(dto.idProfesor())
                .orElseThrow(() -> new IllegalArgumentException("Profesor no encontrado"));

        validarCursoPeriodo(cursoPeriodo, materia, periodoAcademico);

        MatriculaPk pk = new MatriculaPk(estudiante, cursoPeriodo, materia, periodoAcademico, profesor);
        Matricula nuevaMatricula = new Matricula(pk);
        boolean insertado = matriculaRepositorio.insertarSiNoExiste(nuevaMatricula);
        if (!insertado) {
            throw new IllegalStateException("Ya existe la matricula o hay error al grabar");
        }
        return hidratarYmapearUnico(nuevaMatricula, estudiante, cursoPeriodo,
                materia, periodoAcademico, profesor);
    }

    public MatriculaDto retirar(UUID idEstudiante,
            UUID idCursoPeriodo,
            Integer idMateria,
            Integer idPeriodoAcademico,
            Long idProfesor) {
        DependenciasMatricula dependencias = buscarDependencias(idEstudiante, idCursoPeriodo,
                idMateria, idPeriodoAcademico, idProfesor);
        Matricula matricula = buscarMatricula(dependencias);
        matricula.retirar();
        Matricula actualizada = matriculaRepositorio.actualizar(matricula);
        return hidratarYmapearUnico(actualizada,
                dependencias.estudiante(),
                dependencias.cursoPeriodo(),
                dependencias.materia(),
                dependencias.periodoAcademico(),
                dependencias.profesor());
    }

    public MatriculaDto reactivar(UUID idEstudiante,
            UUID idCursoPeriodo,
            Integer idMateria,
            Integer idPeriodoAcademico,
            Long idProfesor) {
        DependenciasMatricula dependencias = buscarDependencias(idEstudiante, idCursoPeriodo,
                idMateria, idPeriodoAcademico, idProfesor);
        Matricula matricula = buscarMatricula(dependencias);
        matricula.reactivar();
        Matricula actualizada = matriculaRepositorio.actualizar(matricula);
        return hidratarYmapearUnico(actualizada,
                dependencias.estudiante(),
                dependencias.cursoPeriodo(),
                dependencias.materia(),
                dependencias.periodoAcademico(),
                dependencias.profesor());
    }

    public List<MatriculaDto> listarPorEstudiante(UUID idEstudiante) {
        if (idEstudiante == null) {
            throw new IllegalArgumentException("El ID del estudiante es obligatorio");
        }
        Estudiante estudiante = estudianteRepositorio.buscarPorId(idEstudiante)
                .orElseThrow(() -> new IllegalArgumentException("No existe el estudiante"));

        List<Matricula> matriculas = matriculaRepositorio.listarPorEstudiante(idEstudiante);
        if (matriculas.isEmpty()) {
            return new ArrayList<>();
        }
        matriculas.sort(Comparator.comparing(Matricula::getFechaMatricula).reversed());
        return hidratarYmapearListaConEstudiante(matriculas, estudiante);
    }

    public List<MatriculaDto> listarPorCursoPeriodo(UUID idCursoPeriodo) {
        if (idCursoPeriodo == null) {
            throw new IllegalArgumentException("El ID del curso periodo es obligatorio");
        }
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(idCursoPeriodo)
                .orElseThrow(() -> new IllegalArgumentException("No existe el curso periodo"));

        List<Matricula> matriculas = matriculaRepositorio.listarPorCursoPeriodo(idCursoPeriodo);
        if (matriculas.isEmpty()) {
            return List.of();
        }
        matriculas.sort(Comparator.comparing(Matricula::getFechaMatricula).reversed());
        return hidratarYmapearListaConCursoPeriodo(matriculas, cursoPeriodo);
    }

    // ---------------------------------------------------------------------
    // Busqueda de la entidad y sus cinco dependencias
    // ---------------------------------------------------------------------
    private DependenciasMatricula buscarDependencias(UUID idEstudiante,
            UUID idCursoPeriodo,
            Integer idMateria,
            Integer idPeriodoAcademico,
            Long idProfesor) {
        Estudiante estudiante = estudianteRepositorio.buscarPorId(idEstudiante)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(idCursoPeriodo)
                .orElseThrow(() -> new IllegalArgumentException("No existe el curso periodo"));
        Materia materia = materiaRepositorio.buscarPorId(idMateria)
                .orElseThrow(() -> new IllegalArgumentException("No existe la materia"));
        PeriodoAcademico periodoAcademico = periodoAcademicoRepositorio.buscarPorId(idPeriodoAcademico)
                .orElseThrow(() -> new IllegalArgumentException("No existe el periodo academico"));
        Profesor profesor = profesorRepositorio.buscarPorId(idProfesor)
                .orElseThrow(() -> new IllegalArgumentException("Profesor no encontrado"));

        validarCursoPeriodo(cursoPeriodo, materia, periodoAcademico);

        return new DependenciasMatricula(estudiante, cursoPeriodo, materia,
                periodoAcademico, profesor);
    }

    private Matricula buscarMatricula(DependenciasMatricula dependencias) {
        MatriculaPk pk = new MatriculaPk(
                dependencias.estudiante(),
                dependencias.cursoPeriodo(),
                dependencias.materia(),
                dependencias.periodoAcademico(),
                dependencias.profesor());
        return matriculaRepositorio.buscarPorId(pk)
                .orElseThrow(() -> new IllegalArgumentException("No existe la matricula indicada"));
    }

    private void validarCursoPeriodo(CursoPeriodo cursoPeriodo,
            Materia materia,
            PeriodoAcademico periodoAcademico) {
        if (!cursoPeriodo.getIdMateria().equals(materia.getIdMateria())) {
            throw new IllegalArgumentException("El curso periodo no pertenece a la materia indicada");
        }
        if (!cursoPeriodo.getIdPeriodoAcademico().equals(periodoAcademico.getIdPeriodoAcademico())) {
            throw new IllegalArgumentException("El curso periodo no pertenece al periodo academico indicado");
        }
    }

    // ---------------------------------------------------------------------
    // Metodos auxiliares de hidratacion
    // ---------------------------------------------------------------------
    private List<MatriculaDto> hidratarYmapearListaGenerico(
            List<Matricula> matriculas,
            EstudianteCargador estudianteCargadorInicial,
            CursoPeriodoCargador cursoPeriodoCargadorInicial,
            MateriaCargador materiaCargadorInicial,
            PeriodoAcademicoCargador periodoCargadorInicial,
            ProfesorCargador profesorCargadorInicial) {

        Set<UUID> idsEstudiante = new HashSet<>();
        Set<UUID> idsCursoPeriodo = new HashSet<>();
        Set<Integer> idsMateria = new HashSet<>();
        Set<Integer> idsPeriodo = new HashSet<>();
        Set<Long> idsProfesor = new HashSet<>();

        for (Matricula matricula : matriculas) {
            idsEstudiante.add(matricula.getIdEstudiante());
            idsCursoPeriodo.add(matricula.getIdCursoPeriodo());
            idsMateria.add(matricula.getIdMateria());
            idsPeriodo.add(matricula.getIdPeriodoAcademico());
            idsProfesor.add(matricula.getIdProfesor());
        }

        EstudianteCargador estudianteCargador = estudianteCargadorInicial != null
                ? estudianteCargadorInicial
                : new EstudianteCargador(estudianteRepositorio);
        estudianteCargador.cargarPorIds(new ArrayList<>(idsEstudiante));

        CursoPeriodoCargador cursoPeriodoCargador = cursoPeriodoCargadorInicial != null
                ? cursoPeriodoCargadorInicial
                : new CursoPeriodoCargador(cursoPeriodoRepositorio);
        cursoPeriodoCargador.cargarPorIds(new ArrayList<>(idsCursoPeriodo));

        MateriaCargador materiaCargador = materiaCargadorInicial != null
                ? materiaCargadorInicial
                : new MateriaCargador(materiaRepositorio);
        materiaCargador.cargarPorIds(new ArrayList<>(idsMateria));

        PeriodoAcademicoCargador periodoCargador = periodoCargadorInicial != null
                ? periodoCargadorInicial
                : new PeriodoAcademicoCargador(periodoAcademicoRepositorio);
        periodoCargador.cargarPorIds(new ArrayList<>(idsPeriodo));

        ProfesorCargador profesorCargador = profesorCargadorInicial != null
                ? profesorCargadorInicial
                : new ProfesorCargador(profesorRepositorio);
        profesorCargador.cargarPorIds(new ArrayList<>(idsProfesor));

        return mapeador.toDtoList(matriculas, estudianteCargador, cursoPeriodoCargador,
                materiaCargador, periodoCargador, profesorCargador);
    }

    private List<MatriculaDto> hidratarYmapearListaConEstudiante(
            List<Matricula> matriculas,
            Estudiante estudiante) {
        EstudianteCargador estudianteCargador = new EstudianteCargador(estudianteRepositorio);
        estudianteCargador.registrarEnCache(estudiante);
        return hidratarYmapearListaGenerico(matriculas, estudianteCargador, null, null, null, null);
    }

    private List<MatriculaDto> hidratarYmapearListaConCursoPeriodo(
            List<Matricula> matriculas,
            CursoPeriodo cursoPeriodo) {
        CursoPeriodoCargador cursoPeriodoCargador = new CursoPeriodoCargador(cursoPeriodoRepositorio);
        cursoPeriodoCargador.registrarEnCache(cursoPeriodo);
        return hidratarYmapearListaGenerico(matriculas, null, cursoPeriodoCargador, null, null, null);
    }

    private MatriculaDto hidratarYmapearUnico(Matricula matricula,
            Estudiante estudiante,
            CursoPeriodo cursoPeriodo,
            Materia materia,
            PeriodoAcademico periodoAcademico,
            Profesor profesor) {
        EstudianteCargador estudianteCargador = new EstudianteCargador(estudianteRepositorio);
        estudianteCargador.registrarEnCache(estudiante);

        CursoPeriodoCargador cursoPeriodoCargador = new CursoPeriodoCargador(cursoPeriodoRepositorio);
        cursoPeriodoCargador.registrarEnCache(cursoPeriodo);

        MateriaCargador materiaCargador = new MateriaCargador(materiaRepositorio);
        materiaCargador.registrarEnCache(materia);

        PeriodoAcademicoCargador periodoCargador = new PeriodoAcademicoCargador(periodoAcademicoRepositorio);
        periodoCargador.registrarEnCache(periodoAcademico);

        ProfesorCargador profesorCargador = new ProfesorCargador(profesorRepositorio);
        profesorCargador.registrarEnCache(profesor);

        return mapeador.toDto(matricula, estudianteCargador, cursoPeriodoCargador,
                materiaCargador, periodoCargador, profesorCargador);
    }

    private record DependenciasMatricula(
            Estudiante estudiante,
            CursoPeriodo cursoPeriodo,
            Materia materia,
            PeriodoAcademico periodoAcademico,
            Profesor profesor) {

    }
}
