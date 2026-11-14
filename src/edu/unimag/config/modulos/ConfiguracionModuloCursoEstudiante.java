package edu.unimag.config.modulos;

import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import edu.unimag.config.RutaPersistencia;
import edu.unimag.config.modulos.api.ModuloConfigurable;
import edu.unimag.controlador.CursoEstudianteControlador;
import edu.unimag.mapeador.CursoEstudianteMapeador;
import edu.unimag.mapeador.CursoPeriodoMapeador;
import edu.unimag.mapeador.EstudianteMapeador;
import edu.unimag.mapeador.MateriaMapeador;
import edu.unimag.mapeador.PeriodoAcademicoMapeador;
import edu.unimag.modelo.CursoEstudiante;
import edu.unimag.modelo.pk.CursoEstudiantePk;
import edu.unimag.persistencia.CursoEstudianteRepositorioImpl;
import edu.unimag.repositorio.CursoEstudianteRepositorio;
import edu.unimag.repositorio.CursoPeriodoRepositorio;
import edu.unimag.repositorio.EstudianteRepositorio;
import edu.unimag.repositorio.MateriaRepositorio;
import edu.unimag.repositorio.PeriodoAcademicoRepositorio;
import edu.unimag.servicio.CursoEstudianteServicio;
import edu.unimag.servicio.CursoPeriodoServicio;
import edu.unimag.servicio.EstudianteServicio;

public class ConfiguracionModuloCursoEstudiante implements ModuloConfigurable {

    private final CursoEstudianteRepositorio cursoEstudianteRepositorio;

    private final EstudianteRepositorio estudianteRepositorio;
    private final EstudianteServicio estudianteServicio;

    private final CursoPeriodoRepositorio cursoPeriodoRepositorio;
    private final CursoPeriodoServicio cursoPeriodoServicio;

    private final MateriaRepositorio materiaRepositorio;
    private final PeriodoAcademicoRepositorio periodoAcademicoRepositorio;
    private final CursoEstudianteServicio cursoEstudianteServicio;

    public ConfiguracionModuloCursoEstudiante(
            EstudianteRepositorio estudianteRepositorio,
            EstudianteServicio estudianteServicio,
            CursoPeriodoRepositorio cursoPeriodoRepositorio,
            CursoPeriodoServicio cursoPeriodoServicio,
            MateriaRepositorio materiaRepositorio,
            PeriodoAcademicoRepositorio periodoAcademicoRepositorio) {

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
        if (periodoAcademicoRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de periodo academico no puede ser nulo");
        }

        this.estudianteRepositorio = estudianteRepositorio;
        this.estudianteServicio = estudianteServicio;
        this.cursoPeriodoRepositorio = cursoPeriodoRepositorio;
        this.cursoPeriodoServicio = cursoPeriodoServicio;
        this.materiaRepositorio = materiaRepositorio;
        this.periodoAcademicoRepositorio = periodoAcademicoRepositorio;
        boolean necesitaUUID = false;
        TpaRepository<CursoEstudiante, CursoEstudiantePk> tpaEngine = TpaRepositoryFactory.create(CursoEstudiante.class, RutaPersistencia.CURSOS_ESTUDIANTES.obtenerRuta(), necesitaUUID);

        this.cursoEstudianteRepositorio = new CursoEstudianteRepositorioImpl(tpaEngine);

        EstudianteMapeador estudianteMapeador = new EstudianteMapeador();
        MateriaMapeador materiaMapeador = new MateriaMapeador();
        PeriodoAcademicoMapeador periodoAcademicoMapeador = new PeriodoAcademicoMapeador();
        CursoPeriodoMapeador cursoPeriodoMapeador = new CursoPeriodoMapeador(materiaMapeador, periodoAcademicoMapeador);
        CursoEstudianteMapeador cursoEstudianteMapeador = new CursoEstudianteMapeador(estudianteMapeador, cursoPeriodoMapeador);
        this.cursoEstudianteServicio = new CursoEstudianteServicio(
                this.cursoEstudianteRepositorio,
                this.estudianteRepositorio,
                this.cursoPeriodoRepositorio,
                this.materiaRepositorio,
                this.periodoAcademicoRepositorio,
                cursoEstudianteMapeador);
    }

    public CursoEstudianteRepositorio getCursoEstudianteRepositorio() {
        return cursoEstudianteRepositorio;
    }

    public CursoEstudianteServicio getCursoEstudianteServicio() {
        return cursoEstudianteServicio;
    }

    public CursoEstudianteControlador construirControlador() {
        return new CursoEstudianteControlador(
                this.cursoEstudianteServicio,
                this.estudianteServicio,
                this.cursoPeriodoServicio);
    }


    @Override
    public void cerrarRecursos() {
        if (cursoEstudianteRepositorio != null) {
            cursoEstudianteRepositorio.cerrar();
        }
    }

}
