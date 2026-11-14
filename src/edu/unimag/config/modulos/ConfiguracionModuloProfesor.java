package edu.unimag.config.modulos;

import edu.unimag.config.modulos.api.ModuloConfigurable;
import com.cleandev.tpa.api.TpaRepository;
import com.cleandev.tpa.api.TpaRepositoryFactory;
import edu.unimag.config.RutaPersistencia;
import edu.unimag.controlador.ProfesorControlador;
import edu.unimag.mapeador.ProfesorMapeador;
import edu.unimag.modelo.Profesor;
import edu.unimag.persistencia.ProfesorRepositorioImpl;
import edu.unimag.repositorio.ProfesorRepositorio;
import edu.unimag.servicio.ProfesorServicio;

public class ConfiguracionModuloProfesor implements ModuloConfigurable {

    private final ProfesorRepositorio profesorRepositorio;
    private final ProfesorServicio profesorServicio;

    public ConfiguracionModuloProfesor() {
        boolean necesitaUUID = false;
        TpaRepository<Profesor, Long> tpaEngine = TpaRepositoryFactory.create(
                Profesor.class,
                RutaPersistencia.PROFESORES.obtenerRuta(),
                necesitaUUID
        );
        this.profesorRepositorio = new ProfesorRepositorioImpl(tpaEngine);

        // Crear el servicio en el constructor para poder exponerlo
        ProfesorMapeador profesorMapper = new ProfesorMapeador();
        this.profesorServicio = new ProfesorServicio(
                this.profesorRepositorio,
                profesorMapper
        );
    }

    public ProfesorRepositorio getProfesorRepositorio() {
        return profesorRepositorio;
    }

    public ProfesorServicio getProfesorServicio() {
        return profesorServicio;
    }

    public ProfesorControlador construirControlador() {
        ProfesorControlador controller = new ProfesorControlador(this.profesorServicio);
        return controller;
    }


    @Override
    public void cerrarRecursos() {
        if (profesorRepositorio != null) {
            profesorRepositorio.cerrar();
        }
    }
}
