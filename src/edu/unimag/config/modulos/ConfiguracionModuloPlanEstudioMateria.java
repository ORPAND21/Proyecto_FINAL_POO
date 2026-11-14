package edu.unimag.config.modulos;

import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import edu.unimag.config.RutaPersistencia;
import edu.unimag.config.modulos.api.ModuloConfigurable;
import edu.unimag.controlador.PlanEstudioMateriaControlador;
import edu.unimag.mapeador.FacultadMapeador;
import edu.unimag.mapeador.MateriaMapeador;
import edu.unimag.mapeador.PlanEstudioMapeador;
import edu.unimag.mapeador.PlanEstudioMateriaMapeador;
import edu.unimag.mapeador.ProgramaMapeador;
import edu.unimag.modelo.PlanEstudioMateria;
import edu.unimag.modelo.pk.PlanEstudioMateriaPk;
import edu.unimag.persistencia.PlanEstudioMateriaRepositorioImpl;
import edu.unimag.repositorio.FacultadRepositorio;
import edu.unimag.repositorio.MateriaRepositorio;
import edu.unimag.repositorio.PlanEstudioMateriaRepositorio;
import edu.unimag.repositorio.PlanEstudioRepositorio;
import edu.unimag.repositorio.ProgramaRepositorio;
import edu.unimag.servicio.MateriaServicio;
import edu.unimag.servicio.PlanEstudioMateriaServicio;
import edu.unimag.servicio.PlanEstudioServicio;

public class ConfiguracionModuloPlanEstudioMateria implements ModuloConfigurable {

    private final PlanEstudioMateriaRepositorio planEstudioMateriaRepositorio;

    private final PlanEstudioRepositorio planEstudioRepositorio;
    private final PlanEstudioServicio planEstudioServicio;

    private final MateriaRepositorio materiaRepositorio;
    private final MateriaServicio materiaServicio;

    private final ProgramaRepositorio programaRepositorio;
    private final FacultadRepositorio facultadRepositorio;
    private final PlanEstudioMateriaServicio planEstudioMateriaServicio;

    public ConfiguracionModuloPlanEstudioMateria(
            PlanEstudioRepositorio planEstudioRepositorio,
            PlanEstudioServicio planEstudioServicio,
            MateriaRepositorio materiaRepositorio,
            MateriaServicio materiaServicio,
            ProgramaRepositorio programaRepositorio,
            FacultadRepositorio facultadRepositorio) {

        if (planEstudioRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de plan de estudio no puede ser nulo");
        }
        if (planEstudioServicio == null) {
            throw new IllegalArgumentException("El servicio de plan de estudio no puede ser nulo");
        }
        if (materiaRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de materia no puede ser nulo");
        }
        if (materiaServicio == null) {
            throw new IllegalArgumentException("El servicio de materia no puede ser nulo");
        }
        if (programaRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de programa no puede ser nulo");
        }
        if (facultadRepositorio == null) {
            throw new IllegalArgumentException("El repositorio de facultad no puede ser nulo");
        }

        this.planEstudioRepositorio = planEstudioRepositorio;
        this.planEstudioServicio = planEstudioServicio;
        this.materiaRepositorio = materiaRepositorio;
        this.materiaServicio = materiaServicio;
        this.programaRepositorio = programaRepositorio;
        this.facultadRepositorio = facultadRepositorio;
        boolean necesitaUUID = false;
        TpaRepository<PlanEstudioMateria, PlanEstudioMateriaPk> tpaEngine = TpaRepositoryFactory.create(PlanEstudioMateria.class, RutaPersistencia.PLANES_ESTUDIO_MATERIAS.obtenerRuta(), necesitaUUID);

        this.planEstudioMateriaRepositorio = new PlanEstudioMateriaRepositorioImpl(tpaEngine);

        FacultadMapeador facultadMapeador = new FacultadMapeador();
        ProgramaMapeador programaMapeador = new ProgramaMapeador(facultadMapeador);
        PlanEstudioMapeador planEstudioMapeador = new PlanEstudioMapeador(programaMapeador);
        MateriaMapeador materiaMapeador = new MateriaMapeador();
        PlanEstudioMateriaMapeador planEstudioMateriaMapeador = new PlanEstudioMateriaMapeador(planEstudioMapeador, materiaMapeador);
        this.planEstudioMateriaServicio = new PlanEstudioMateriaServicio(
                this.planEstudioMateriaRepositorio,
                this.planEstudioRepositorio,
                this.materiaRepositorio,
                this.programaRepositorio,
                this.facultadRepositorio,
                planEstudioMateriaMapeador);
    }

    public PlanEstudioMateriaServicio getPlanEstudioMateriaServicio() {
        return planEstudioMateriaServicio;
    }

    public PlanEstudioMateriaControlador construirControlador() {
        return new PlanEstudioMateriaControlador(
                this.planEstudioMateriaServicio,
                this.planEstudioServicio,
                this.materiaServicio);
    }


    @Override
    public void cerrarRecursos() {
        if (planEstudioMateriaRepositorio != null) {
            planEstudioMateriaRepositorio.cerrar();
        }
    }

}
