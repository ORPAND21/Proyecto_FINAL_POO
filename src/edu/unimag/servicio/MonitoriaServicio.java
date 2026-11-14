package edu.unimag.servicio;

import edu.unimag.dto.monitoria.MonitoriaActualizarDto;
import edu.unimag.dto.monitoria.MonitoriaCrearDto;
import edu.unimag.dto.monitoria.MonitoriaDto;
import edu.unimag.mapeador.MonitoriaMapeador;
import edu.unimag.modelo.CursoEstudiante;
import edu.unimag.modelo.CursoPeriodo;
import edu.unimag.modelo.CursoProfesor;
import edu.unimag.modelo.Estudiante;
import edu.unimag.modelo.Materia;
import edu.unimag.modelo.Monitoria;
import edu.unimag.modelo.PeriodoAcademico;
import edu.unimag.modelo.Profesor;
import edu.unimag.modelo.pk.CursoEstudiantePk;
import edu.unimag.modelo.pk.CursoProfesorPk;
import edu.unimag.modelo.pk.MonitoriaPk;
import edu.unimag.repositorio.CursoEstudianteRepositorio;
import edu.unimag.repositorio.CursoPeriodoRepositorio;
import edu.unimag.repositorio.CursoProfesorRepositorio;
import edu.unimag.repositorio.EstudianteRepositorio;
import edu.unimag.repositorio.MateriaRepositorio;
import edu.unimag.repositorio.MonitoriaRepositorio;
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

public class MonitoriaServicio {

    private final MonitoriaRepositorio monitoriaRepositorio;
    private final EstudianteRepositorio estudianteRepositorio;
    private final ProfesorRepositorio profesorRepositorio;
    private final CursoPeriodoRepositorio cursoPeriodoRepositorio;
    private final MateriaRepositorio materiaRepositorio;
    private final PeriodoAcademicoRepositorio periodoAcademicoRepositorio;
    private final CursoEstudianteRepositorio cursoEstudianteRepositorio;
    private final CursoProfesorRepositorio cursoProfesorRepositorio;
    private final MonitoriaMapeador mapeador;

    public MonitoriaServicio(MonitoriaRepositorio monitoriaRepositorio,
            EstudianteRepositorio estudianteRepositorio,
            ProfesorRepositorio profesorRepositorio,
            CursoPeriodoRepositorio cursoPeriodoRepositorio,
            MateriaRepositorio materiaRepositorio,
            PeriodoAcademicoRepositorio periodoAcademicoRepositorio,
            CursoEstudianteRepositorio cursoEstudianteRepositorio,
            CursoProfesorRepositorio cursoProfesorRepositorio,
            MonitoriaMapeador mapeador) {
        if (monitoriaRepositorio == null) {
            throw new IllegalArgumentException("Repo para monitorias no puede ser nulo");
        }
        if (estudianteRepositorio == null) {
            throw new IllegalArgumentException("Repo para estudiantes no puede ser nulo");
        }
        if (profesorRepositorio == null) {
            throw new IllegalArgumentException("Repo para profes no puede ser nulo");
        }
        if (cursoPeriodoRepositorio == null) {
            throw new IllegalArgumentException("Repo para curso Periodo no puede ser nulo");
        }
        if (materiaRepositorio == null) {
            throw new IllegalArgumentException("Repo para materia no puede ser nulo");
        }
        if (periodoAcademicoRepositorio == null) {
            throw new IllegalArgumentException("Repo para periodo academico no puede ser nulo");
        }
        if (cursoEstudianteRepositorio == null) {
            throw new IllegalArgumentException("Repo para inscripciones no puede ser nulo");
        }
        if (cursoProfesorRepositorio == null) {
            throw new IllegalArgumentException("Repo para asignar profes no puede ser nulo");
        }
        if (mapeador == null) {
            throw new IllegalArgumentException("El mapeador no puede ser nulo");
        }
        this.monitoriaRepositorio = monitoriaRepositorio;
        this.estudianteRepositorio = estudianteRepositorio;
        this.profesorRepositorio = profesorRepositorio;
        this.cursoPeriodoRepositorio = cursoPeriodoRepositorio;
        this.materiaRepositorio = materiaRepositorio;
        this.periodoAcademicoRepositorio = periodoAcademicoRepositorio;
        this.cursoEstudianteRepositorio = cursoEstudianteRepositorio;
        this.cursoProfesorRepositorio = cursoProfesorRepositorio;
        this.mapeador = mapeador;
    }

    //logica del negocio
    //1. Crear monitoria
    public MonitoriaDto crear(MonitoriaCrearDto dto) {
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(dto.idCursoPeriodo()).orElseThrow(
                () -> new IllegalArgumentException("No existe curso"));
        Estudiante estudiante = estudianteRepositorio.buscarPorId(dto.idEstudiante()).orElseThrow(
                () -> new IllegalArgumentException("Estudiante no encontrado"));
        Profesor profesor = profesorRepositorio.buscarPorId(dto.idProfesor()).orElseThrow(
                () -> new IllegalArgumentException("Profe no encontrado"));

        //el estudiante debe estar inscrito y admitido en el curso
        CursoEstudiante inscripcion = cursoEstudianteRepositorio.buscarPorId(
                new CursoEstudiantePk(cursoPeriodo, estudiante)).orElseThrow(
                () -> new IllegalStateException("El estudiante no esta inscrito en el curso"));
        if (!inscripcion.estaAdmitido()) {
            throw new IllegalStateException("La inscripcion del estudiante no esta admitida en el curso");
        }

        //el profesor debe estar asignado y activo en el curso
        CursoProfesor asignacion = cursoProfesorRepositorio.buscarPorId(
                new CursoProfesorPk(cursoPeriodo, profesor)).orElseThrow(
                () -> new IllegalStateException("El profesor no esta asignado al curso"));
        if (!asignacion.estaActivo()) {
            throw new IllegalStateException("El profesor esta retirado del curso");
        }

        MonitoriaPk pk = new MonitoriaPk(cursoPeriodo, estudiante, profesor);
        Monitoria nuevaMonitoria = new Monitoria(pk);
        boolean insertado = monitoriaRepositorio.insertarSiNoExiste(nuevaMonitoria);
        if (!insertado) {
            throw new IllegalStateException("Ya existe la monitoria o hay error al grabar");
        }
        return hidratarYmapearUnico(nuevaMonitoria, estudiante, profesor, cursoPeriodo);
    }

    //2. Evaluar monitoria
    public MonitoriaDto evaluar(MonitoriaActualizarDto dto) {
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(dto.idCursoPeriodo()).
                orElseThrow(() -> new IllegalArgumentException("No existe este curso"));
        Estudiante estudiante = estudianteRepositorio.buscarPorId(dto.idEstudiante()).
                orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
        Profesor profesor = profesorRepositorio.buscarPorId(dto.idProfesor()).
                orElseThrow(() -> new IllegalArgumentException("Profesor no encontrado"));

        MonitoriaPk pk = new MonitoriaPk(cursoPeriodo, estudiante, profesor);
        Monitoria monitoria = monitoriaRepositorio.buscarPorId(pk)
                .orElseThrow(() -> new IllegalArgumentException("No existe la monitoria"));

        monitoria.evaluar(dto.calificacion(), dto.comentario());
        Monitoria actualizado = monitoriaRepositorio.actualizar(monitoria);

        return hidratarYmapearUnico(actualizado, estudiante, profesor, cursoPeriodo);
    }

    //3. Eliminar monitoria (borrado fisico)
    public boolean eliminar(UUID idCursoPeriodo, UUID idEstudiante, Long idProfesor) {
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(idCursoPeriodo).
                orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
        Estudiante estudiante = estudianteRepositorio.buscarPorId(idEstudiante).
                orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
        Profesor profesor = profesorRepositorio.buscarPorId(idProfesor).
                orElseThrow(() -> new IllegalArgumentException("Profesor no encontrado"));

        MonitoriaPk pk = new MonitoriaPk(cursoPeriodo, estudiante, profesor);
        if (!monitoriaRepositorio.buscarPorId(pk).isPresent()) {
            throw new IllegalArgumentException(
                    "No se puede eliminar: Monitoria no encontrada con " + pk
            );
        }
        return monitoriaRepositorio.eliminar(pk);
    }

    //4. Listar monitorias
    public List<MonitoriaDto> listarTodas() {
        List<Monitoria> todas = new ArrayList<>(monitoriaRepositorio.listarTodos());
        if (todas.isEmpty()) {
            return List.of();
        }
        todas.sort(Comparator.comparing(Monitoria::getFechaCreacionMonitoria).reversed());
        return hidratarYmapearListaGenerico(todas, null, null, null);
    }

    public List<MonitoriaDto> listarPorEstudiante(UUID idEstudiante) {
        Estudiante estudiante = estudianteRepositorio.buscarPorId(idEstudiante).
                orElseThrow(() -> new IllegalArgumentException("No existe el estudiante"));

        List<Monitoria> delEstudiante = new ArrayList<>();
        for (Monitoria monitoria : monitoriaRepositorio.listarTodos()) {
            if (monitoria.getIdEstudiante().equals(idEstudiante)) {
                delEstudiante.add(monitoria);
            }
        }
        if (delEstudiante.isEmpty()) {
            return new ArrayList<>();
        }
        delEstudiante.sort(Comparator.comparing(Monitoria::getFechaCreacionMonitoria).reversed());
        return hidratarYmapearListaConEstudiante(delEstudiante, estudiante);
    }

    public List<MonitoriaDto> listarPorProfesor(Long idProfesor) {
        Profesor profesor = profesorRepositorio.buscarPorId(idProfesor).
                orElseThrow(() -> new IllegalArgumentException("No existe el profe"));

        List<Monitoria> delProfesor = new ArrayList<>();
        for (Monitoria monitoria : monitoriaRepositorio.listarTodos()) {
            if (monitoria.getIdProfesor().equals(idProfesor)) {
                delProfesor.add(monitoria);
            }
        }
        if (delProfesor.isEmpty()) {
            return new ArrayList<>();
        }
        delProfesor.sort(Comparator.comparing(Monitoria::getFechaCreacionMonitoria).reversed());
        return hidratarYmapearListaConProfesor(delProfesor, profesor);
    }

    public List<MonitoriaDto> listarPorCursoPeriodo(UUID idCursoPeriodo) {
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(idCursoPeriodo).
                orElseThrow(() -> new IllegalArgumentException("No existe el curso"));

        List<Monitoria> delCurso = new ArrayList<>();
        for (Monitoria monitoria : monitoriaRepositorio.listarTodos()) {
            if (monitoria.getIdCursoPeriodo().equals(idCursoPeriodo)) {
                delCurso.add(monitoria);
            }
        }
        if (delCurso.isEmpty()) {
            return List.of();
        }
        delCurso.sort(Comparator.comparing(Monitoria::getFechaCreacionMonitoria).reversed());
        return hidratarYmapearListaConCursoPeriodo(delCurso, cursoPeriodo);
    }

    //metodos auxiliares de hidratacion
    private List<MonitoriaDto> hidratarYmapearListaGenerico(
            List<Monitoria> monitorias,
            EstudianteCargador estudianteCargadorInicial,
            ProfesorCargador profesorCargadorInicial,
            CursoPeriodoCargador cursoPeriodoCargadorInicial
    ) {
        Set<UUID> idsEstudiante = new HashSet<>();
        Set<Long> idsProfesor = new HashSet<>();
        Set<UUID> idsCursoPeriodo = new HashSet<>();

        for (Monitoria monitoria : monitorias) {
            idsEstudiante.add(monitoria.getIdEstudiante());
            idsProfesor.add(monitoria.getIdProfesor());
            idsCursoPeriodo.add(monitoria.getIdCursoPeriodo());
        }
        EstudianteCargador estudianteCargador = estudianteCargadorInicial != null
                ? estudianteCargadorInicial
                : new EstudianteCargador(estudianteRepositorio);
        estudianteCargador.cargarPorIds(new ArrayList<>(idsEstudiante));

        ProfesorCargador profesorCargador = profesorCargadorInicial != null
                ? profesorCargadorInicial
                : new ProfesorCargador(profesorRepositorio);
        profesorCargador.cargarPorIds(new ArrayList<>(idsProfesor));

        CursoPeriodoCargador cursoPeriodoCargador = cursoPeriodoCargadorInicial != null
                ? cursoPeriodoCargadorInicial
                : new CursoPeriodoCargador(cursoPeriodoRepositorio);
        cursoPeriodoCargador.cargarPorIds(new ArrayList<>(idsCursoPeriodo));

        Set<Integer> idsMateria = new HashSet<>();
        Set<Integer> idsPeriodo = new HashSet<>();
        for (UUID idCursoPeriodo : idsCursoPeriodo) {
            cursoPeriodoCargador.obtener(idCursoPeriodo).ifPresent(
                    curso -> {
                        idsMateria.add(curso.getIdMateria());
                        idsPeriodo.add(curso.getIdPeriodoAcademico());
                    }
            );
        }
        MateriaCargador materiaCargador = new MateriaCargador(materiaRepositorio);
        materiaCargador.cargarPorIds(new ArrayList<>(idsMateria));

        PeriodoAcademicoCargador periodoCargador = new PeriodoAcademicoCargador(periodoAcademicoRepositorio);
        periodoCargador.cargarPorIds(new ArrayList<>(idsPeriodo));

        return mapeador.toDtoList(monitorias, estudianteCargador, profesorCargador,
                cursoPeriodoCargador, materiaCargador, periodoCargador);
    }

    private List<MonitoriaDto> hidratarYmapearListaConEstudiante(List<Monitoria> monitorias, Estudiante estudiante) {
        EstudianteCargador estudianteCargador = new EstudianteCargador(estudianteRepositorio);
        estudianteCargador.registrarEnCache(estudiante);
        return hidratarYmapearListaGenerico(monitorias, estudianteCargador, null, null);
    }

    private List<MonitoriaDto> hidratarYmapearListaConProfesor(List<Monitoria> monitorias, Profesor profesor) {
        ProfesorCargador profesorCargador = new ProfesorCargador(profesorRepositorio);
        profesorCargador.registrarEnCache(profesor);
        return hidratarYmapearListaGenerico(monitorias, null, profesorCargador, null);
    }

    private List<MonitoriaDto> hidratarYmapearListaConCursoPeriodo(List<Monitoria> monitorias, CursoPeriodo cursoPeriodo) {
        CursoPeriodoCargador cursoPeriodoCargador = new CursoPeriodoCargador(cursoPeriodoRepositorio);
        cursoPeriodoCargador.registrarEnCache(cursoPeriodo);
        return hidratarYmapearListaGenerico(monitorias, null, null, cursoPeriodoCargador);
    }

    private MonitoriaDto hidratarYmapearUnico(Monitoria monitoria,
            Estudiante estudiante,
            Profesor profesor,
            CursoPeriodo cursoPeriodo) {
        Materia materia = materiaRepositorio.buscarPorId(cursoPeriodo.getIdMateria()).orElseThrow(
                () -> new IllegalStateException("Materia incorrecta"));

        PeriodoAcademico periodo = periodoAcademicoRepositorio.buscarPorId(cursoPeriodo.getIdPeriodoAcademico()).orElseThrow(
                () -> new IllegalStateException("Periodo Incorrecto"));

        EstudianteCargador estudianteCargador = new EstudianteCargador(estudianteRepositorio);
        estudianteCargador.registrarEnCache(estudiante);

        ProfesorCargador profesorCargador = new ProfesorCargador(profesorRepositorio);
        profesorCargador.registrarEnCache(profesor);

        CursoPeriodoCargador cursoPeriodoCargador = new CursoPeriodoCargador(cursoPeriodoRepositorio);
        cursoPeriodoCargador.registrarEnCache(cursoPeriodo);

        MateriaCargador materiaCargador = new MateriaCargador(materiaRepositorio);
        materiaCargador.registrarEnCache(materia);

        PeriodoAcademicoCargador periodoAcademicoCargador = new PeriodoAcademicoCargador(periodoAcademicoRepositorio);
        periodoAcademicoCargador.registrarEnCache(periodo);

        return mapeador.toDto(monitoria, estudianteCargador, profesorCargador,
                cursoPeriodoCargador, materiaCargador, periodoAcademicoCargador);
    }

}
