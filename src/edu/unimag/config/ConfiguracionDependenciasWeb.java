package edu.unimag.config;

import edu.unimag.config.modulos.ConfiguracionModuloCursoPeriodo;
import edu.unimag.config.modulos.ConfiguracionModuloCursoProfesor;
import edu.unimag.config.modulos.ConfiguracionModuloEstudiante;
import edu.unimag.config.modulos.ConfiguracionModuloFacultad;
import edu.unimag.config.modulos.ConfiguracionModuloFormacionProfesor;
import edu.unimag.config.modulos.ConfiguracionModuloMateria;
import edu.unimag.config.modulos.ConfiguracionModuloPeriodoAcademico;
import edu.unimag.config.modulos.ConfiguracionModuloPlanEstudio;
import edu.unimag.config.modulos.ConfiguracionModuloPrograma;
import edu.unimag.config.modulos.ConfiguracionModuloProfesor;
import edu.unimag.config.modulos.api.ModuloConfigurable;
import edu.unimag.controlador.CursoPeriodoControlador;
import edu.unimag.controlador.CursoProfesorControlador;
import edu.unimag.controlador.EstudianteControlador;
import edu.unimag.controlador.FacultadControlador;
import edu.unimag.controlador.FormacionProfesorControlador;
import edu.unimag.controlador.MateriaControlador;
import edu.unimag.controlador.PeriodoAcademicoControlador;
import edu.unimag.controlador.PlanEstudioControlador;
import edu.unimag.controlador.ProfesorControlador;
import edu.unimag.controlador.ProgramaControlador;

import java.util.ArrayList;
import java.util.List;

/**
 * Raiz de composicion de la aplicacion WEB.
 *
 * Ensambla los modulos en el MISMO orden de dependencias (niveles 1 a 6) y
 * entrega CONTROLADORES, que es lo que usan los manejadores web.
 */
public class ConfiguracionDependenciasWeb implements AutoCloseable {

    private final List<ModuloConfigurable> modulosConfigurados = new ArrayList<>();

    private final ProfesorControlador profesorControlador;
    private final EstudianteControlador estudianteControlador;
    private final FacultadControlador facultadControlador;
    private final MateriaControlador materiaControlador;
    private final PeriodoAcademicoControlador periodoAcademicoControlador;
    private final FormacionProfesorControlador formacionProfesorControlador;
    private final ProgramaControlador programaControlador;
    private final CursoPeriodoControlador cursoPeriodoControlador;
    private final PlanEstudioControlador planEstudioControlador;
    private final CursoProfesorControlador cursoProfesorControlador;

    public ConfiguracionDependenciasWeb() {
        try {
            // NIVEL 1: modulos independientes
            ConfiguracionModuloProfesor configProfesor = new ConfiguracionModuloProfesor();
            modulosConfigurados.add(configProfesor);

            ConfiguracionModuloEstudiante configEstudiante = new ConfiguracionModuloEstudiante();
            modulosConfigurados.add(configEstudiante);

            ConfiguracionModuloFacultad configFacultad = new ConfiguracionModuloFacultad();
            modulosConfigurados.add(configFacultad);

            ConfiguracionModuloMateria configMateria = new ConfiguracionModuloMateria();
            modulosConfigurados.add(configMateria);

            ConfiguracionModuloPeriodoAcademico configPeriodo = new ConfiguracionModuloPeriodoAcademico();
            modulosConfigurados.add(configPeriodo);

            // NIVEL 2: una dependencia
            ConfiguracionModuloFormacionProfesor configFormacion = new ConfiguracionModuloFormacionProfesor(
                    configProfesor.getProfesorRepositorio());
            modulosConfigurados.add(configFormacion);

            ConfiguracionModuloPrograma configPrograma = new ConfiguracionModuloPrograma(
                    configFacultad.getFacultadRepositorio());
            modulosConfigurados.add(configPrograma);

            // NIVEL 3: CursoPeriodo tiene Materia y PeriodoAcademico
            ConfiguracionModuloCursoPeriodo configCursoPeriodo = new ConfiguracionModuloCursoPeriodo(
                    configMateria.getMateriaRepositorio(),
                    configPeriodo.getPeriodoAcademicoRepositorio());
            modulosConfigurados.add(configCursoPeriodo);

            // NIVEL 4: PlanEstudio tiene Programa y Facultad
            ConfiguracionModuloPlanEstudio configPlanEstudio = new ConfiguracionModuloPlanEstudio(
                    configPrograma.getProgramaRepositorio(),
                    configPrograma.getProgramaServicio(),
                    configFacultad.getFacultadRepositorio());
            modulosConfigurados.add(configPlanEstudio);

            // NIVEL 5: CursoProfesor (PK compuesta) tiene Profesor y CursoPeriodo
            ConfiguracionModuloCursoProfesor configCursoProfesor = new ConfiguracionModuloCursoProfesor(
                    configProfesor.getProfesorRepositorio(),
                    configProfesor.getProfesorServicio(),
                    configCursoPeriodo.getCursoPeriodoRepositorio(),
                    configCursoPeriodo.getCursoPeriodoServicio(),
                    configMateria.getMateriaRepositorio(),
                    configPeriodo.getPeriodoAcademicoRepositorio());
            modulosConfigurados.add(configCursoProfesor);

            // Controladores que usara la web
            // (los modulos que exponen su servicio se envuelven aqui; los que no, usan construirControlador())
            this.profesorControlador = new ProfesorControlador(configProfesor.getProfesorServicio());
            this.estudianteControlador = new EstudianteControlador(configEstudiante.getEstudianteServicio());
            this.facultadControlador = configFacultad.construirControlador();
            this.materiaControlador = new MateriaControlador(configMateria.getMateriaServicio());
            this.periodoAcademicoControlador = new PeriodoAcademicoControlador(configPeriodo.getPeriodoAcademicoServicio());
            this.formacionProfesorControlador = configFormacion.construirControlador();
            this.programaControlador = new ProgramaControlador(configPrograma.getProgramaServicio());
            this.cursoPeriodoControlador = new CursoPeriodoControlador(
                    configCursoPeriodo.getCursoPeriodoServicio(),
                    configCursoProfesor.getCursoProfesorServicio());
            this.planEstudioControlador = new PlanEstudioControlador(
                    configPlanEstudio.getPlanEstudioServicio(),
                    configPrograma.getProgramaServicio());
            this.cursoProfesorControlador = configCursoProfesor.construirControlador();
        } catch (RuntimeException e) {
            close(); // si algo falla a medias, liberar lo ya abierto
            throw e;
        }
    }

    public ProfesorControlador getProfesorControlador() {
        return profesorControlador;
    }

    public EstudianteControlador getEstudianteControlador() {
        return estudianteControlador;
    }

    public FacultadControlador getFacultadControlador() {
        return facultadControlador;
    }

    public MateriaControlador getMateriaControlador() {
        return materiaControlador;
    }

    public PeriodoAcademicoControlador getPeriodoAcademicoControlador() {
        return periodoAcademicoControlador;
    }

    public FormacionProfesorControlador getFormacionProfesorControlador() {
        return formacionProfesorControlador;
    }

    public ProgramaControlador getProgramaControlador() {
        return programaControlador;
    }

    public CursoPeriodoControlador getCursoPeriodoControlador() {
        return cursoPeriodoControlador;
    }

    public PlanEstudioControlador getPlanEstudioControlador() {
        return planEstudioControlador;
    }

    public CursoProfesorControlador getCursoProfesorControlador() {
        return cursoProfesorControlador;
    }

    @Override
    public void close() {
        for (int i = modulosConfigurados.size() - 1; i >= 0; i--) {
            ModuloConfigurable modulo = modulosConfigurados.get(i);
            try {
                modulo.cerrarRecursos();
            } catch (Exception e) {
                System.err.println("ADVERTENCIA: Error cerrando modulo "
                        + modulo.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        modulosConfigurados.clear();
    }
}
