package edu.unimag.config.modulos;

import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import edu.unimag.config.RutaPersistencia;
import edu.unimag.config.modulos.api.ModuloConfigurable;
import edu.unimag.controlador.MatriculaControlador;
import edu.unimag.mapeador.CursoPeriodoMapeador;
import edu.unimag.mapeador.EstudianteMapeador;
import edu.unimag.mapeador.MateriaMapeador;
import edu.unimag.mapeador.MatriculaMapeador;
import edu.unimag.mapeador.PeriodoAcademicoMapeador;
import edu.unimag.mapeador.ProfesorMapeador;
import edu.unimag.modelo.Matricula;
import edu.unimag.modelo.pk.MatriculaPk;
import edu.unimag.persistencia.MatriculaRepositorioImpl;
import edu.unimag.repositorio.CursoPeriodoRepositorio;
import edu.unimag.repositorio.EstudianteRepositorio;
import edu.unimag.repositorio.MateriaRepositorio;
import edu.unimag.repositorio.MatriculaRepositorio;
import edu.unimag.repositorio.PeriodoAcademicoRepositorio;
import edu.unimag.repositorio.ProfesorRepositorio;
import edu.unimag.servicio.CursoPeriodoServicio;
import edu.unimag.servicio.EstudianteServicio;
import edu.unimag.servicio.MateriaServicio;
import edu.unimag.servicio.MatriculaServicio;
import edu.unimag.servicio.ProfesorServicio;

public class ConfiguracionModuloMatricula implements ModuloConfigurable {

    private final MatriculaRepositorio matriculaRepositorio;

    private final EstudianteRepositorio estudianteRepositorio;
    private final EstudianteServicio estudianteServicio;

    private final CursoPeriodoRepositorio cursoPeriodoRepositorio;
    private final CursoPeriodoServicio cursoPeriodoServicio;

    private final MateriaRepositorio materiaRepositorio;
    private final MateriaServicio materiaServicio;

    private final PeriodoAcademicoRepositorio periodoAcademicoRepositorio;

    private final ProfesorRepositorio profesorRepositorio;
    private final ProfesorServicio profesorServicio;

    public ConfiguracionModuloMatricula(
            EstudianteRepositorio estudianteRepositorio,
            EstudianteServicio estudianteServicio,
            CursoPeriodoRepositorio cursoPeriodoRepositorio,
            CursoPeriodoServicio cursoPeriodoServicio,
            MateriaRepositorio materiaRepositorio,
            MateriaServicio materiaServicio,
            PeriodoAcademicoRepositorio periodoAcademicoRepositorio,
            ProfesorRepositorio profesorRepositorio,
            ProfesorServicio profesorServicio) {

        if (estudianteRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de estudiante no puede ser nulo");
        }
        if (estudianteServicio == null) {
            throw new IllegalArgumentException("El servicio de estudiante no puede ser nulo");
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
        if (materiaServicio == null) {
            throw new IllegalArgumentException("El servicio de materia no puede ser nulo");
        }
        if (periodoAcademicoRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de periodo academico no puede ser nulo");
        }
        if (profesorRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de profesor no puede ser nulo");
        }
        if (profesorServicio == null) {
            throw new IllegalArgumentException("El servicio de profesor no puede ser nulo");
        }

        this.estudianteRepositorio = estudianteRepositorio;
        this.estudianteServicio = estudianteServicio;
        this.cursoPeriodoRepositorio = cursoPeriodoRepositorio;
        this.cursoPeriodoServicio = cursoPeriodoServicio;
        this.materiaRepositorio = materiaRepositorio;
        this.materiaServicio = materiaServicio;
        this.periodoAcademicoRepositorio = periodoAcademicoRepositorio;
        this.profesorRepositorio = profesorRepositorio;
        this.profesorServicio = profesorServicio;

        boolean necesitaUUID = false;
        TpaRepository<Matricula, MatriculaPk> tpaEngine = TpaRepositoryFactory.create(
                Matricula.class,
                RutaPersistencia.MATRICULAS.obtenerRuta(),
                necesitaUUID);
        this.matriculaRepositorio = new MatriculaRepositorioImpl(tpaEngine);
    }

    public MatriculaControlador construirControlador() {
        EstudianteMapeador estudianteMapeador = new EstudianteMapeador();
        MateriaMapeador materiaMapeador = new MateriaMapeador();
        PeriodoAcademicoMapeador periodoAcademicoMapeador = new PeriodoAcademicoMapeador();
        ProfesorMapeador profesorMapeador = new ProfesorMapeador();
        CursoPeriodoMapeador cursoPeriodoMapeador = new CursoPeriodoMapeador(
                materiaMapeador, periodoAcademicoMapeador);
        MatriculaMapeador matriculaMapeador = new MatriculaMapeador(
                estudianteMapeador,
                cursoPeriodoMapeador,
                materiaMapeador,
                periodoAcademicoMapeador,
                profesorMapeador);

        MatriculaServicio matriculaServicio = new MatriculaServicio(
                matriculaRepositorio,
                estudianteRepositorio,
                cursoPeriodoRepositorio,
                materiaRepositorio,
                periodoAcademicoRepositorio,
                profesorRepositorio,
                matriculaMapeador);

        return new MatriculaControlador(
                matriculaServicio,
                estudianteServicio,
                cursoPeriodoServicio,
                materiaServicio,
                profesorServicio);
    }


    @Override
    public void cerrarRecursos() {
        if (matriculaRepositorio != null) {
            matriculaRepositorio.cerrar();
        }
    }
}
