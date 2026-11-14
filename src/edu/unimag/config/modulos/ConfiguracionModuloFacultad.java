package edu.unimag.config.modulos;

import edu.unimag.config.modulos.api.ModuloConfigurable;
import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import edu.unimag.config.RutaPersistencia;
import edu.unimag.controlador.FacultadControlador;
import edu.unimag.mapeador.FacultadMapeador;
import edu.unimag.modelo.Facultad;
import edu.unimag.persistencia.FacultadRepositorioImpl;
import edu.unimag.repositorio.FacultadRepositorio;
import edu.unimag.servicio.FacultadServicio;

public class ConfiguracionModuloFacultad implements ModuloConfigurable {

    private final FacultadRepositorio facultadRepositorio;

    public ConfiguracionModuloFacultad() {
        boolean necesitaUUID = false;
        TpaRepository<Facultad, Long> tpaEngine = TpaRepositoryFactory.create(
                Facultad.class,
                RutaPersistencia.FACULTADES.obtenerRuta(),
                necesitaUUID
        );
        this.facultadRepositorio = new FacultadRepositorioImpl(tpaEngine);
    }

    public FacultadRepositorio getFacultadRepositorio() {
        return facultadRepositorio;
    }
    public FacultadControlador construirControlador(){
        FacultadMapeador mapper = new FacultadMapeador();
        FacultadServicio service = new FacultadServicio(this.facultadRepositorio, mapper);
        FacultadControlador controller = new FacultadControlador(service);
        return controller;
        
    }


    @Override
    public void cerrarRecursos() {
        if (facultadRepositorio != null) {
            facultadRepositorio.cerrar();
        }
    }
}
