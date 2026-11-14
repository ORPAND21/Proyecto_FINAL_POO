package edu.unimag.controlador;

import edu.unimag.dto.cursoProfesor.CursoProfesorActualizarDto;
import edu.unimag.dto.cursoProfesor.CursoProfesorCrearDto;
import edu.unimag.dto.cursoProfesor.CursoProfesorDto;
import edu.unimag.dto.cursoperiodo.CursoPeriodoDto;
import edu.unimag.dto.profesor.ProfesorDto;
import edu.unimag.servicio.CursoPeriodoServicio;
import edu.unimag.servicio.CursoProfesorServicio;
import edu.unimag.servicio.ProfesorServicio;
import java.util.List;
import java.util.UUID;

public class CursoProfesorControlador {

    private final CursoProfesorServicio cursoProfesorServicio;
    private final ProfesorServicio profesorServicio;
    private final CursoPeriodoServicio cursoPeriodoServicio;

    public CursoProfesorControlador(CursoProfesorServicio cursoProfesorServicio,
            ProfesorServicio profesorServicio,
            CursoPeriodoServicio cursoPeriodoServicio) {
        if (cursoProfesorServicio == null) {
            throw new IllegalArgumentException("Error en configuracion");
        }
        if (cursoProfesorServicio == null) {
            throw new IllegalArgumentException("Error en configuracion");
        }
        if (cursoProfesorServicio == null) {
            throw new IllegalArgumentException("Error en configuracion");
        }

        this.cursoProfesorServicio = cursoProfesorServicio;
        this.profesorServicio = profesorServicio;
        this.cursoPeriodoServicio = cursoPeriodoServicio;
    }
    public CursoProfesorDto asignar (CursoProfesorCrearDto dto){
        return cursoProfesorServicio.asignar(dto);
    }
    public CursoProfesorDto cambiarTipoAsignacion(CursoProfesorActualizarDto dto){
        return cursoProfesorServicio.cambiarTipoAsignacion(dto);
    }
    public CursoProfesorDto retirar(UUID idCursoPeriodo, Long idProfesor){
        return cursoProfesorServicio.retirar(idCursoPeriodo, idProfesor); 
    }
    public CursoProfesorDto reactivar(UUID idCursoPeriodo, Long idProfesor){
        return cursoProfesorServicio.reActivar(idCursoPeriodo, idProfesor);
    }
    public List<CursoProfesorDto> listarPorProfesor(Long idProfesor){
        return cursoProfesorServicio.listarPorProfesor(idProfesor);
    }
    public List<CursoProfesorDto> listarPorMateria (Integer idMateria){
        return cursoProfesorServicio.listarPorMateria(idMateria);
    }
    public List<CursoProfesorDto> listarPorMateriaPeriodo(Integer idMateria,Integer idPeriodo){
        return cursoProfesorServicio.listarPorMateriaYPorPeriodo(idMateria, idPeriodo);
    }
    public List<CursoProfesorDto> listarPorPeriodo(Integer idPeriodo){
        return cursoProfesorServicio.listarPorPeriodo(idPeriodo);
    }
    //acceso a otros servicios
    public ProfesorDto buscarProfesor(Long idProfesor){
        return profesorServicio.buscarPorId(idProfesor);
    }
    public CursoPeriodoDto buscarPorCursoPeriodo (UUID idCursoPeriodo){
        return cursoPeriodoServicio.buscarPorId(idCursoPeriodo);
    }

}
