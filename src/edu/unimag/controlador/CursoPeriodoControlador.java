package edu.unimag.controlador;

import edu.unimag.dto.cursoperiodo.CursoPeriodoActualizarDto;
import edu.unimag.dto.cursoperiodo.CursoPeriodoCrearDto;
import edu.unimag.dto.cursoperiodo.CursoPeriodoDto;
import edu.unimag.servicio.CursoEstudianteServicio;
import edu.unimag.servicio.CursoPeriodoServicio;
import edu.unimag.servicio.CursoProfesorServicio;

import java.util.List;
import java.util.UUID;

public class CursoPeriodoControlador {

    private final CursoPeriodoServicio servicio;
    private final CursoProfesorServicio validacion;
    private final CursoEstudianteServicio validacionInscripciones;

    public CursoPeriodoControlador(CursoPeriodoServicio servicio, CursoProfesorServicio validacion,
            CursoEstudianteServicio validacionInscripciones) {
        if (servicio == null) {
            throw new IllegalArgumentException("El servicio de cursos por periodo no puede ser nulo");
        }
        this.servicio = servicio;
        this.validacion=validacion;
        this.validacionInscripciones = validacionInscripciones;
    }

    public CursoPeriodoDto registrar(CursoPeriodoCrearDto request) {
        return servicio.registrar(request);
    }

    public List<CursoPeriodoDto> listarTodos() {
        return servicio.listarTodos();
    }

    public CursoPeriodoDto actualizar(CursoPeriodoActualizarDto request) {
        return servicio.actualizar(request);
    }

    public boolean eliminar(UUID id) {
        if(validacion!=null && validacion.tieneAsignaciones(id)){
            throw new IllegalArgumentException("No es posible eliminar un curso con docentes asignados");
        }
        if(validacionInscripciones!=null && validacionInscripciones.tieneInscripciones(id)){
            throw new IllegalArgumentException("No es posible eliminar un curso con estudiantes inscritos");
        }
        return servicio.eliminar(id);
    }
}
