package edu.unimag.config.modulos;

import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import edu.unimag.config.RutaPersistencia;
import edu.unimag.config.modulos.api.ModuloConfigurable;
import edu.unimag.controlador.MonitoriaControlador;
import edu.unimag.mapeador.CursoPeriodoMapeador;
import edu.unimag.mapeador.EstudianteMapeador;
import edu.unimag.mapeador.MateriaMapeador;
import edu.unimag.mapeador.MonitoriaMapeador;
import edu.unimag.mapeador.PeriodoAcademicoMapeador;
import edu.unimag.mapeador.ProfesorMapeador;
import edu.unimag.modelo.Monitoria;
import edu.unimag.modelo.pk.MonitoriaPk;
import edu.unimag.persistencia.MonitoriaRepositorioImpl;
import edu.unimag.repositorio.CursoEstudianteRepositorio;
import edu.unimag.repositorio.CursoPeriodoRepositorio;
import edu.unimag.repositorio.CursoProfesorRepositorio;
import edu.unimag.repositorio.EstudianteRepositorio;
import edu.unimag.repositorio.MateriaRepositorio;
import edu.unimag.repositorio.MonitoriaRepositorio;
import edu.unimag.repositorio.PeriodoAcademicoRepositorio;
import edu.unimag.repositorio.ProfesorRepositorio;
import edu.unimag.servicio.CursoPeriodoServicio;
import edu.unimag.servicio.EstudianteServicio;
import edu.unimag.servicio.MonitoriaServicio;
import edu.unimag.servicio.ProfesorServicio;

public class ConfiguracionModuloMonitoria implements ModuloConfigurable {

    private final MonitoriaRepositorio monitoriaRepositorio;

    private final EstudianteRepositorio estudianteRepositorio;
    private final EstudianteServicio estudianteServicio;

    private final ProfesorRepositorio profesorRepositorio;
    private final ProfesorServicio profesorServicio;

    private final CursoPeriodoRepositorio cursoPeriodoRepositorio;
    private final CursoPeriodoServicio cursoPeriodoServicio;

    private final MateriaRepositorio materiaRepositorio;
    private final PeriodoAcademicoRepositorio periodoAcademicoRepositorio;

    private final CursoEstudianteRepositorio cursoEstudianteRepositorio;
    private final CursoProfesorRepositorio cursoProfesorRepositorio;
    private final MonitoriaServicio monitoriaServicio;

    public ConfiguracionModuloMonitoria(
            EstudianteRepositorio estudianteRepositorio,
            EstudianteServicio estudianteServicio,
            ProfesorRepositorio profesorRepositorio,
            ProfesorServicio profesorServicio,
            CursoPeriodoRepositorio cursoPeriodoRepositorio,
            CursoPeriodoServicio cursoPeriodoServicio,
            MateriaRepositorio materiaRepositorio,
            PeriodoAcademicoRepositorio periodoAcademicoRepositorio,
            CursoEstudianteRepositorio cursoEstudianteRepositorio,
            CursoProfesorRepositorio cursoProfesorRepositorio) {

        if (estudianteRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de estudiante no puede ser nulo");
        }
        if (estudianteServicio == null) {
            throw new IllegalArgumentException("El servicio de estudiante no puede ser nulo");
        }
        if (profesorRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de profesor no puede ser nulo");
        }
        if (profesorServicio == null) {
            throw new IllegalArgumentException("El servicio de profesor no puede ser nulo");
        }
        if (cursoPeriodoRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de curso periodo no puede ser nulo");
        }
        if (cursoPeriodoServicio == null) {
            throw new IllegalArgumentException("El servicio de curso periodo no puede ser nulo");
        }
        if (materiaRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de materia no puede ser nulo");
        }
        if (periodoAcademicoRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de periodo academico no puede ser nulo");
        }
        if (cursoEstudianteRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de inscripciones no puede ser nulo");
        }
        if (cursoProfesorRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de curso profesor no puede ser nulo");
        }

        this.estudianteRepositorio = estudianteRepositorio;
        this.estudianteServicio = estudianteServicio;
        this.profesorRepositorio = profesorRepositorio;
        this.profesorServicio = profesorServicio;
        this.cursoPeriodoRepositorio = cursoPeriodoRepositorio;
        this.cursoPeriodoServicio = cursoPeriodoServicio;
        this.materiaRepositorio = materiaRepositorio;
        this.periodoAcademicoRepositorio = periodoAcademicoRepositorio;
        this.cursoEstudianteRepositorio = cursoEstudianteRepositorio;
        this.cursoProfesorRepositorio = cursoProfesorRepositorio;
        boolean necesitaUUID = false;
        TpaRepository<Monitoria, MonitoriaPk> tpaEngine = TpaRepositoryFactory.create(Monitoria.class, RutaPersistencia.MONITORIAS.obtenerRuta(), necesitaUUID);

        this.monitoriaRepositorio = new MonitoriaRepositorioImpl(tpaEngine);

        EstudianteMapeador estudianteMapeador = new EstudianteMapeador();
        ProfesorMapeador profesorMapeador = new ProfesorMapeador();
        MateriaMapeador materiaMapeador = new MateriaMapeador();
        PeriodoAcademicoMapeador periodoAcademicoMapeador = new PeriodoAcademicoMapeador();
        CursoPeriodoMapeador cursoPeriodoMapeador = new CursoPeriodoMapeador(materiaMapeador, periodoAcademicoMapeador);
        MonitoriaMapeador monitoriaMapeador = new MonitoriaMapeador(estudianteMapeador, profesorMapeador, cursoPeriodoMapeador);
        this.monitoriaServicio = new MonitoriaServicio(
                this.monitoriaRepositorio,
                this.estudianteRepositorio,
                this.profesorRepositorio,
                this.cursoPeriodoRepositorio,
                this.materiaRepositorio,
                this.periodoAcademicoRepositorio,
                this.cursoEstudianteRepositorio,
                this.cursoProfesorRepositorio,
                monitoriaMapeador);
    }

    public MonitoriaServicio getMonitoriaServicio() {
        return monitoriaServicio;
    }

    public MonitoriaControlador construirControlador() {
        return new MonitoriaControlador(
                this.monitoriaServicio,
                this.estudianteServicio,
                this.profesorServicio,
                this.cursoPeriodoServicio);
    }


    @Override
    public void cerrarRecursos() {
        if (monitoriaRepositorio != null) {
            monitoriaRepositorio.cerrar();
        }
    }

}
