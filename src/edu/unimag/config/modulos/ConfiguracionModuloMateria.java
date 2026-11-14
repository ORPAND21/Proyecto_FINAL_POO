package edu.unimag.config.modulos;

import edu.unimag.config.modulos.api.ModuloConfigurable;
import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import edu.unimag.config.RutaPersistencia;
import edu.unimag.controlador.MateriaControlador;
import edu.unimag.mapeador.MateriaMapeador;
import edu.unimag.modelo.Materia;
import edu.unimag.persistencia.MateriaRepositorioImpl;
import edu.unimag.repositorio.MateriaRepositorio;
import edu.unimag.servicio.MateriaServicio;

public class ConfiguracionModuloMateria implements ModuloConfigurable {

    private final MateriaRepositorio materiaRepositorio;
    private final MateriaServicio materiaServicio;

    public ConfiguracionModuloMateria() {
        boolean necesitaUUID = false;
        TpaRepository<Materia, Integer> tpaEngine = TpaRepositoryFactory.create(
                Materia.class,
                RutaPersistencia.MATERIAS.obtenerRuta(),
                necesitaUUID
        );
        this.materiaRepositorio = new MateriaRepositorioImpl(tpaEngine);

        // Crear el servicio en el constructor para poder exponerlo
        MateriaMapeador materiaMapper = new MateriaMapeador();
        this.materiaServicio = new MateriaServicio(
                this.materiaRepositorio,
                materiaMapper
        );
    }

    public MateriaRepositorio getMateriaRepositorio() {
        return materiaRepositorio;
    }

    public MateriaServicio getMateriaServicio() {
        return materiaServicio;
    }
    public MateriaControlador construirControlador (){
        MateriaControlador controller = new MateriaControlador(this.materiaServicio);
        return controller;
    }


    @Override
    public void cerrarRecursos() {
        if (materiaRepositorio != null) {
            materiaRepositorio.cerrar();
        }
    }
}
