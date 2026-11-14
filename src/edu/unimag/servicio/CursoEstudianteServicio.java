package edu.unimag.servicio;

import edu.unimag.dto.cursoestudiante.CursoEstudianteCrearDto;
import edu.unimag.dto.cursoestudiante.CursoEstudianteDto;
import edu.unimag.mapeador.CursoEstudianteMapeador;
import edu.unimag.modelo.CursoEstudiante;
import edu.unimag.modelo.CursoPeriodo;
import edu.unimag.modelo.Estudiante;
import edu.unimag.modelo.Materia;
import edu.unimag.modelo.PeriodoAcademico;
import edu.unimag.modelo.pk.CursoEstudiantePk;
import edu.unimag.repositorio.CursoEstudianteRepositorio;
import edu.unimag.repositorio.CursoPeriodoRepositorio;
import edu.unimag.repositorio.EstudianteRepositorio;
import edu.unimag.repositorio.MateriaRepositorio;
import edu.unimag.repositorio.PeriodoAcademicoRepositorio;
import edu.unimag.repositorio.cargador.CursoPeriodoCargador;
import edu.unimag.repositorio.cargador.EstudianteCargador;
import edu.unimag.repositorio.cargador.MateriaCargador;
import edu.unimag.repositorio.cargador.PeriodoAcademicoCargador;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CursoEstudianteServicio {

    private final CursoEstudianteRepositorio cursoEstudianteRepositorio;
    private final EstudianteRepositorio estudianteRepositorio;
    private final CursoPeriodoRepositorio cursoPeriodoRepositorio;
    private final MateriaRepositorio materiaRepositorio;
    private final PeriodoAcademicoRepositorio periodoAcademicoRepositorio;
    private final CursoEstudianteMapeador mapeador;

    public CursoEstudianteServicio(CursoEstudianteRepositorio cursoEstudianteRepositorio,
            EstudianteRepositorio estudianteRepositorio,
            CursoPeriodoRepositorio cursoPeriodoRepositorio,
            MateriaRepositorio materiaRepositorio,
            PeriodoAcademicoRepositorio periodoAcademicoRepositorio,
            CursoEstudianteMapeador mapeador) {
        if (cursoEstudianteRepositorio == null) {
            throw new IllegalArgumentException("Repo para inscripciones no puede ser nulo");
        }
        if (estudianteRepositorio == null) {
            throw new IllegalArgumentException("Repo para estudiantes no puede ser nulo");
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
        if (mapeador == null) {
            throw new IllegalArgumentException("El mapeador no puede ser nulo");
        }
        this.cursoEstudianteRepositorio = cursoEstudianteRepositorio;
        this.estudianteRepositorio = estudianteRepositorio;
        this.cursoPeriodoRepositorio = cursoPeriodoRepositorio;
        this.materiaRepositorio = materiaRepositorio;
        this.periodoAcademicoRepositorio = periodoAcademicoRepositorio;
        this.mapeador = mapeador;
    }

    //logica del negocio
    //1. Inscribir estudiante a curso
    public CursoEstudianteDto inscribir(CursoEstudianteCrearDto dto) {
        Estudiante estudiante = estudianteRepositorio.buscarPorId(dto.idEstudiante()).orElseThrow(
                () -> new IllegalArgumentException("Estudiante no encontrado"));
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(dto.idCursoPeriodo()).orElseThrow(
                () -> new IllegalArgumentException("No existe curso"));

        CursoEstudiantePk pk = new CursoEstudiantePk(cursoPeriodo, estudiante);
        CursoEstudiante nuevaInscripcion = new CursoEstudiante(pk);
        boolean insertado = cursoEstudianteRepositorio.insertarSiNoExiste(nuevaInscripcion);
        if (!insertado) {
            throw new IllegalStateException("El estudiante ya esta inscrito en el curso o hay error al grabar");
        }
        return hidratarYmapearUnico(nuevaInscripcion, estudiante, cursoPeriodo);
    }

    //7. Admitir inscripcion
    public CursoEstudianteDto admitir(UUID idCursoPeriodo, UUID idEstudiante) {
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(idCursoPeriodo).
                orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
        Estudiante estudiante = estudianteRepositorio.buscarPorId(idEstudiante).
                orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
        CursoEstudiantePk pk = new CursoEstudiantePk(cursoPeriodo, estudiante);
        CursoEstudiante inscripcion = cursoEstudianteRepositorio.buscarPorId(pk)
                .orElseThrow(() -> new IllegalArgumentException("No existe la inscripcion a admitir"));
        inscripcion.admitir();
        CursoEstudiante actualizado = cursoEstudianteRepositorio.actualizar(inscripcion);

        return hidratarYmapearUnico(actualizado, estudiante, cursoPeriodo);
    }

    //8. Evaluar inscripcion: aprobar
    public CursoEstudianteDto aprobar(UUID idCursoPeriodo, UUID idEstudiante) {
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(idCursoPeriodo).
                orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
        Estudiante estudiante = estudianteRepositorio.buscarPorId(idEstudiante).
                orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
        CursoEstudiantePk pk = new CursoEstudiantePk(cursoPeriodo, estudiante);
        CursoEstudiante inscripcion = cursoEstudianteRepositorio.buscarPorId(pk)
                .orElseThrow(() -> new IllegalArgumentException("No existe la inscripcion a aprobar"));
        inscripcion.aprobar();
        CursoEstudiante actualizado = cursoEstudianteRepositorio.actualizar(inscripcion);

        return hidratarYmapearUnico(actualizado, estudiante, cursoPeriodo);
    }

    //8. Evaluar inscripcion: reprobar
    public CursoEstudianteDto reprobar(UUID idCursoPeriodo, UUID idEstudiante) {
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(idCursoPeriodo).
                orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
        Estudiante estudiante = estudianteRepositorio.buscarPorId(idEstudiante).
                orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
        CursoEstudiantePk pk = new CursoEstudiantePk(cursoPeriodo, estudiante);
        CursoEstudiante inscripcion = cursoEstudianteRepositorio.buscarPorId(pk)
                .orElseThrow(() -> new IllegalArgumentException("No existe la inscripcion a reprobar"));
        inscripcion.reprobar();
        CursoEstudiante actualizado = cursoEstudianteRepositorio.actualizar(inscripcion);

        return hidratarYmapearUnico(actualizado, estudiante, cursoPeriodo);
    }

    //9. Retirar estudiante
    public CursoEstudianteDto retirar(UUID idCursoPeriodo, UUID idEstudiante) {
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(idCursoPeriodo).
                orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
        Estudiante estudiante = estudianteRepositorio.buscarPorId(idEstudiante).
                orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
        CursoEstudiantePk pk = new CursoEstudiantePk(cursoPeriodo, estudiante);
        CursoEstudiante inscripcion = cursoEstudianteRepositorio.buscarPorId(pk)
                .orElseThrow(() -> new IllegalArgumentException("No existe la inscripcion a retirar"));
        inscripcion.retirar();
        CursoEstudiante actualizado = cursoEstudianteRepositorio.actualizar(inscripcion);

        return hidratarYmapearUnico(actualizado, estudiante, cursoPeriodo);
    }

    //reasignar curso (reactivar inscripcion retirada)
    public CursoEstudianteDto reAsignarCurso(UUID idCursoPeriodo, UUID idEstudiante) {
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(idCursoPeriodo).
                orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
        Estudiante estudiante = estudianteRepositorio.buscarPorId(idEstudiante).
                orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
        CursoEstudiantePk pk = new CursoEstudiantePk(cursoPeriodo, estudiante);
        CursoEstudiante inscripcion = cursoEstudianteRepositorio.buscarPorId(pk)
                .orElseThrow(() -> new IllegalArgumentException("No existe la inscripcion a reasignar"));
        inscripcion.reAsignarCurso();
        CursoEstudiante actualizado = cursoEstudianteRepositorio.actualizar(inscripcion);

        return hidratarYmapearUnico(actualizado, estudiante, cursoPeriodo);
    }

    //10. Rechazar inscripcion
    public CursoEstudianteDto rechazar(UUID idCursoPeriodo, UUID idEstudiante) {
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(idCursoPeriodo).
                orElseThrow(() -> new IllegalArgumentException("Curso no encontrado"));
        Estudiante estudiante = estudianteRepositorio.buscarPorId(idEstudiante).
                orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));
        CursoEstudiantePk pk = new CursoEstudiantePk(cursoPeriodo, estudiante);
        CursoEstudiante inscripcion = cursoEstudianteRepositorio.buscarPorId(pk)
                .orElseThrow(() -> new IllegalArgumentException("No existe la inscripcion a rechazar"));
        inscripcion.rechazar();
        CursoEstudiante actualizado = cursoEstudianteRepositorio.actualizar(inscripcion);

        return hidratarYmapearUnico(actualizado, estudiante, cursoPeriodo);
    }
    
    //2. Listar cursos por estudiante
    public List<CursoEstudianteDto> listarPorEstudiante(UUID idEstudiante) {
        Estudiante estudiante = estudianteRepositorio.buscarPorId(idEstudiante).
                orElseThrow(() -> new IllegalArgumentException("No existe el estudiante"));

        List<CursoEstudiante> delEstudiante = new ArrayList<>();
        for (CursoEstudiante inscripcion : cursoEstudianteRepositorio.listarTodos()) {
            if (inscripcion.getIdEstudiante().equals(idEstudiante)) {
                delEstudiante.add(inscripcion);
            }
        }
        if (delEstudiante.isEmpty()) {
            return new ArrayList<>();
        }
        delEstudiante.sort(Comparator.comparing(CursoEstudiante::getFechaInscripcion).reversed());
        return hidratarYmapearLista(delEstudiante, estudiante);
    }

    //3. Listar estudiantes por curso periodo
    public List<CursoEstudianteDto> listarPorCursoPeriodo(UUID idCursoPeriodo) {
        CursoPeriodo cursoPeriodo = cursoPeriodoRepositorio.buscarPorId(idCursoPeriodo).
                orElseThrow(() -> new IllegalArgumentException("No existe el curso"));

        List<CursoEstudiante> delCurso = new ArrayList<>();
        for (CursoEstudiante inscripcion : cursoEstudianteRepositorio.listarTodos()) {
            if (inscripcion.getIdCursoPeriodo().equals(idCursoPeriodo)) {
                delCurso.add(inscripcion);
            }
        }
        if (delCurso.isEmpty()) {
            return List.of();
        }
        delCurso.sort(Comparator.comparing(CursoEstudiante::getFechaInscripcion).reversed());
        return hidratarYmapearListaConCursoPeriodo(delCurso, cursoPeriodo);
    }

    //4. Listar estudiantes por materia
    public List<CursoEstudianteDto> listarPorMateria(Integer idMateria) {
        Materia materia = materiaRepositorio.buscarPorId(idMateria).
                orElseThrow(() -> new IllegalArgumentException("No existe la materia"));
        Set<UUID> idsCursosValidos = new HashSet<>();
        for (CursoPeriodo cp : cursoPeriodoRepositorio.listarTodos()) {
            if (cp.getIdMateria().equals(idMateria)) {
                idsCursosValidos.add(cp.getIdCursoPeriodo());
            }
        }

        List<CursoEstudiante> deLaMateria = new ArrayList<>();
        for (CursoEstudiante inscripcion : cursoEstudianteRepositorio.listarTodos()) {
            if (idsCursosValidos.contains(inscripcion.getIdCursoPeriodo())) {
                deLaMateria.add(inscripcion);
            }
        }
        if (deLaMateria.isEmpty()) {
            return List.of();
        }
        deLaMateria.sort(Comparator.comparing(CursoEstudiante::getFechaInscripcion).reversed());
        return hidratarYmapearListaConMateria(deLaMateria, materia);
    }

    //5. Listar estudiantes por materia y periodo
    public List<CursoEstudianteDto> listarPorMateriaYPorPeriodo(Integer idMateria, Integer idPeriodo) {
        Materia materia = materiaRepositorio.buscarPorId(idMateria).
                orElseThrow(() -> new IllegalArgumentException("No existe la materia"));
        PeriodoAcademico periodoAcademico = periodoAcademicoRepositorio.buscarPorId(idPeriodo)
                .orElseThrow(() -> new IllegalArgumentException("No existe el periodo"));
        Set<UUID> idsCursosValidos = new HashSet<>();
        for (CursoPeriodo cp : cursoPeriodoRepositorio.listarTodos()) {
            if (cp.getIdMateria().equals(idMateria) && cp.getIdPeriodoAcademico().equals(idPeriodo)) {
                idsCursosValidos.add(cp.getIdCursoPeriodo());
            }
        }
        List<CursoEstudiante> filtradas = new ArrayList<>();
        for (CursoEstudiante inscripcion : cursoEstudianteRepositorio.listarTodos()) {
            if (idsCursosValidos.contains(inscripcion.getIdCursoPeriodo())) {
                filtradas.add(inscripcion);
            }
        }
        if (filtradas.isEmpty()) {
            return List.of();
        }
        filtradas.sort(Comparator.comparing(CursoEstudiante::getFechaInscripcion).reversed());
        return hidratarYmapearListaConMateriaYperiodo(filtradas, periodoAcademico, materia);
    }

    //6. Listar estudiantes por periodo
    public List<CursoEstudianteDto> listarPorPeriodo(Integer idPeriodo) {
        PeriodoAcademico periodoAcademico = periodoAcademicoRepositorio.buscarPorId(idPeriodo)
                .orElseThrow(() -> new IllegalArgumentException("No existe el periodo"));
        Set<UUID> idsCursosValidos = new HashSet<>();
        for (CursoPeriodo cursoPeriodo : cursoPeriodoRepositorio.listarTodos()) {
            if (cursoPeriodo.getIdPeriodoAcademico().equals(idPeriodo)) {
                idsCursosValidos.add(cursoPeriodo.getIdCursoPeriodo());
            }
        }
        List<CursoEstudiante> delPeriodo = new ArrayList<>();
        for (CursoEstudiante inscripcion : cursoEstudianteRepositorio.listarTodos()) {
            if (idsCursosValidos.contains(inscripcion.getIdCursoPeriodo())) {
                delPeriodo.add(inscripcion);
            }
        }
        if (delPeriodo.isEmpty()) {
            return List.of();
        }
        delPeriodo.sort(Comparator.comparing(CursoEstudiante::getFechaInscripcion).reversed());
        return hidratarYmapearListaConPeriodo(delPeriodo, periodoAcademico);
    }

    //validacion para borrar un curso: no debe tener inscripciones
    //(cubre tambien monitorias, porque una monitoria exige una inscripcion admitida
    // y las inscripciones no se borran fisicamente)
    public boolean tieneInscripciones(UUID idCursoPeriodo) {
        for (CursoEstudiante inscripcion : cursoEstudianteRepositorio.listarTodos()) {
            if (inscripcion.getIdCursoPeriodo().equals(idCursoPeriodo)) {
                return true;
            }
        }
        return false;
    }

    //metodos auxiliares de hidratacion
    private List<CursoEstudianteDto> hidratarYmapearListaGenerico(
            List<CursoEstudiante> inscripciones,
            CursoPeriodoCargador cursoPeriodoCargadorInicial,
            MateriaCargador materiaCargadorInicial,
            PeriodoAcademicoCargador periodoCargadorInicial
    ) {
        Set<UUID> idsEstudiante = new HashSet<>();
        Set<UUID> idsCursoPeriodo = new HashSet<>();

        for (CursoEstudiante inscripcion : inscripciones) {
            idsEstudiante.add(inscripcion.getIdEstudiante());
            idsCursoPeriodo.add(inscripcion.getIdCursoPeriodo());
        }
        EstudianteCargador estudianteCargador = new EstudianteCargador(estudianteRepositorio);
        estudianteCargador.cargarPorIds(new ArrayList<>(idsEstudiante));

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
        MateriaCargador materiaCargador = materiaCargadorInicial != null
                ? materiaCargadorInicial
                : new MateriaCargador(materiaRepositorio);
        materiaCargador.cargarPorIds(new ArrayList<>(idsMateria));

        PeriodoAcademicoCargador periodoCargador = periodoCargadorInicial != null
                ? periodoCargadorInicial
                : new PeriodoAcademicoCargador(periodoAcademicoRepositorio);
        periodoCargador.cargarPorIds(new ArrayList<>(idsPeriodo));

        return mapeador.toDtoList(inscripciones, estudianteCargador, cursoPeriodoCargador, materiaCargador, periodoCargador);
    }

    private List<CursoEstudianteDto> hidratarYmapearListaConCursoPeriodo(List<CursoEstudiante> inscripciones, CursoPeriodo cursoPeriodo) {
        CursoPeriodoCargador cursoPeriodoCargador = new CursoPeriodoCargador(cursoPeriodoRepositorio);
        cursoPeriodoCargador.registrarEnCache(cursoPeriodo);
        return hidratarYmapearListaGenerico(inscripciones, cursoPeriodoCargador, null, null);
    }

    private List<CursoEstudianteDto> hidratarYmapearListaConMateria(List<CursoEstudiante> inscripciones, Materia materia) {
        MateriaCargador materiaCargador = new MateriaCargador(materiaRepositorio);
        materiaCargador.registrarEnCache(materia);
        return hidratarYmapearListaGenerico(inscripciones, null, materiaCargador, null);
    }

    private List<CursoEstudianteDto> hidratarYmapearListaConPeriodo(List<CursoEstudiante> inscripciones, PeriodoAcademico periodo) {
        PeriodoAcademicoCargador periodoCargador = new PeriodoAcademicoCargador(periodoAcademicoRepositorio);
        periodoCargador.registrarEnCache(periodo);
        return hidratarYmapearListaGenerico(inscripciones, null, null, periodoCargador);
    }

    private List<CursoEstudianteDto> hidratarYmapearListaConMateriaYperiodo(List<CursoEstudiante> inscripciones, PeriodoAcademico periodo, Materia materia) {
        PeriodoAcademicoCargador periodoCargador = new PeriodoAcademicoCargador(periodoAcademicoRepositorio);
        periodoCargador.registrarEnCache(periodo);

        MateriaCargador materiaCargador = new MateriaCargador(materiaRepositorio);
        materiaCargador.registrarEnCache(materia);
        return hidratarYmapearListaGenerico(inscripciones, null, materiaCargador, periodoCargador);
    }

    private CursoEstudianteDto hidratarYmapearUnico(CursoEstudiante inscripcion,
            Estudiante estudiante,
            CursoPeriodo cursoPeriodo) {
        Materia materia = materiaRepositorio.buscarPorId(cursoPeriodo.getIdMateria()).orElseThrow(
                () -> new IllegalStateException("Materia incorrecta"));

        PeriodoAcademico periodo = periodoAcademicoRepositorio.buscarPorId(cursoPeriodo.getIdPeriodoAcademico()).orElseThrow(
                () -> new IllegalStateException("Periodo Incorrecto"));

        EstudianteCargador estudianteCargador = new EstudianteCargador(estudianteRepositorio);
        estudianteCargador.registrarEnCache(estudiante);

        CursoPeriodoCargador cursoPeriodoCargador = new CursoPeriodoCargador(cursoPeriodoRepositorio);
        cursoPeriodoCargador.registrarEnCache(cursoPeriodo);

        MateriaCargador materiaCargador = new MateriaCargador(materiaRepositorio);
        materiaCargador.registrarEnCache(materia);

        PeriodoAcademicoCargador periodoAcademicoCargador = new PeriodoAcademicoCargador(periodoAcademicoRepositorio);
        periodoAcademicoCargador.registrarEnCache(periodo);

        return mapeador.toDto(inscripcion, estudianteCargador, cursoPeriodoCargador, materiaCargador, periodoAcademicoCargador);
    }

    private List<CursoEstudianteDto> hidratarYmapearLista(List<CursoEstudiante> inscripciones, Estudiante estudiante) {
        EstudianteCargador estudianteCargador = new EstudianteCargador(estudianteRepositorio);
        estudianteCargador.registrarEnCache(estudiante);

        Set<UUID> idsCursoPeriodo = new HashSet<>();
        for (CursoEstudiante inscripcion : inscripciones) {
            idsCursoPeriodo.add(inscripcion.getIdCursoPeriodo());
        }
        CursoPeriodoCargador cursoPeriodoCargador = new CursoPeriodoCargador(cursoPeriodoRepositorio);
        cursoPeriodoCargador.cargarPorIds(new ArrayList<>(idsCursoPeriodo));

        Set<Integer> idsMateria = new HashSet<>();
        Set<Integer> idsPeriodo = new HashSet<>();
        for (UUID idCursoPeriodo : idsCursoPeriodo) {
            cursoPeriodoCargador.obtener(idCursoPeriodo).ifPresent(
                    curso -> {
                        idsMateria.add(curso.getIdMateria());
                        idsPeriodo.add(curso.getIdPeriodoAcademico());
                    });
        }
        MateriaCargador materiaCargador = new MateriaCargador(materiaRepositorio);
        materiaCargador.cargarPorIds(new ArrayList<>(idsMateria));

        PeriodoAcademicoCargador periodoCargador = new PeriodoAcademicoCargador(periodoAcademicoRepositorio);
        periodoCargador.cargarPorIds(new ArrayList<>(idsPeriodo));

        return mapeador.toDtoList(inscripciones, estudianteCargador, cursoPeriodoCargador, materiaCargador, periodoCargador);
    }

}
