package edu.unimag.controlador;

import edu.unimag.dto.cursoperiodo.CursoPeriodoActualizarDto;
import edu.unimag.dto.cursoperiodo.CursoPeriodoCrearDto;
import edu.unimag.dto.cursoperiodo.CursoPeriodoDto;
import edu.unimag.servicio.CursoPeriodoServicio;
import edu.unimag.servicio.CursoProfesorServicio;

import java.util.List;
import java.util.UUID;

public class CursoPeriodoControlador {

    private final CursoPeriodoServicio servicio;
    private final CursoProfesorServicio validacion;

    public CursoPeriodoControlador(CursoPeriodoServicio servicio,CursoProfesorServicio validacion) {
        if (servicio == null) {
            throw new IllegalArgumentException("El servicio de cursos por periodo no puede ser nulo");
        }
        this.servicio = servicio;
        this.validacion=validacion;
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
        return servicio.eliminar(id);
    }
}
