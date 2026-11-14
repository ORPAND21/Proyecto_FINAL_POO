package edu.unimag.config.modulos;

import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import edu.unimag.config.RutaPersistencia;
import edu.unimag.config.modulos.api.ModuloConfigurable;
import edu.unimag.controlador.CursoProfesorControlador;
import edu.unimag.mapeador.CursoPeriodoMapeador;
import edu.unimag.mapeador.CursoProfesorMapeador;
import edu.unimag.mapeador.MateriaMapeador;
import edu.unimag.mapeador.PeriodoAcademicoMapeador;
import edu.unimag.mapeador.ProfesorMapeador;
import edu.unimag.modelo.CursoProfesor;
import edu.unimag.modelo.pk.CursoProfesorPk;
import edu.unimag.persistencia.CursoProfesorRepositorioImpl;
import edu.unimag.repositorio.CursoPeriodoRepositorio;
import edu.unimag.repositorio.CursoProfesorRepositorio;
import edu.unimag.repositorio.MateriaRepositorio;
import edu.unimag.repositorio.PeriodoAcademicoRepositorio;
import edu.unimag.repositorio.ProfesorRepositorio;
import edu.unimag.servicio.CursoPeriodoServicio;
import edu.unimag.servicio.CursoProfesorServicio;
import edu.unimag.servicio.ProfesorServicio;

public class ConfiguracionModuloCursoProfesor implements ModuloConfigurable {

    private final CursoProfesorRepositorio cursoProfesorRepositorio;

    private final ProfesorRepositorio profesorRepositorio;
    private final ProfesorServicio profesorServicio;

    private final CursoPeriodoRepositorio cursoPeriodoRepositorio;
    private final CursoPeriodoServicio cursoPeriodoServicio;

    private final MateriaRepositorio materiaRepositorio;
    private final PeriodoAcademicoRepositorio periodoAcademicoRepositorio;
    private final CursoProfesorServicio cursoProfesorServicio;

    public ConfiguracionModuloCursoProfesor(
            ProfesorRepositorio profesorRepositorio,
            ProfesorServicio profesorServicio,
            CursoPeriodoRepositorio cursoPeriodoRepositorio,
            CursoPeriodoServicio cursoPeriodoServicio,
            MateriaRepositorio materiaRepositorio,
            PeriodoAcademicoRepositorio periodoAcademicoRepositorio) {

        
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

        this.profesorRepositorio = profesorRepositorio;
        this.profesorServicio = profesorServicio;
        this.cursoPeriodoRepositorio = cursoPeriodoRepositorio;
        this.cursoPeriodoServicio = cursoPeriodoServicio;
        this.materiaRepositorio = materiaRepositorio;
        this.periodoAcademicoRepositorio = periodoAcademicoRepositorio;
        boolean necesitaUUID = false;
        TpaRepository<CursoProfesor, CursoProfesorPk> tpaEngine = TpaRepositoryFactory.create(CursoProfesor.class, RutaPersistencia.CURSOS_PROFESORES.obtenerRuta(), necesitaUUID);

        this.cursoProfesorRepositorio = new CursoProfesorRepositorioImpl(tpaEngine);

        ProfesorMapeador profesorMapeador = new ProfesorMapeador();
        MateriaMapeador materiaMapeador = new MateriaMapeador();
        PeriodoAcademicoMapeador periodoAcademicoMapeador = new PeriodoAcademicoMapeador();
        CursoPeriodoMapeador cursoPeriodoMapeador = new CursoPeriodoMapeador(materiaMapeador, periodoAcademicoMapeador);
        CursoProfesorMapeador cursoProfesorMapeador = new CursoProfesorMapeador(profesorMapeador, cursoPeriodoMapeador);
        this.cursoProfesorServicio = new CursoProfesorServicio(
                this.cursoProfesorRepositorio,
                this.profesorRepositorio,
                this.cursoPeriodoRepositorio,
                this.materiaRepositorio,
                this.periodoAcademicoRepositorio,
                cursoProfesorMapeador);
    }

    public CursoProfesorServicio getCursoProfesorServicio() {
        return cursoProfesorServicio;
    }

    public CursoProfesorControlador construirControlador() {
        return new CursoProfesorControlador(
                this.cursoProfesorServicio,
                this.profesorServicio,
                this.cursoPeriodoServicio);
    }


    @Override
    public void cerrarRecursos() {
        if (cursoProfesorRepositorio != null) {
            cursoProfesorRepositorio.cerrar();
        }
    }

}
