package edu.unimag.controlador;

import edu.unimag.dto.cursoperiodo.CursoPeriodoDto;
import edu.unimag.dto.estudiante.EstudianteDto;
import edu.unimag.dto.monitoria.MonitoriaActualizarDto;
import edu.unimag.dto.monitoria.MonitoriaCrearDto;
import edu.unimag.dto.monitoria.MonitoriaDto;
import edu.unimag.dto.profesor.ProfesorDto;
import edu.unimag.servicio.CursoPeriodoServicio;
import edu.unimag.servicio.EstudianteServicio;
import edu.unimag.servicio.MonitoriaServicio;
import edu.unimag.servicio.ProfesorServicio;
import java.util.List;
import java.util.UUID;

public class MonitoriaControlador {

    private final MonitoriaServicio monitoriaServicio;
    private final EstudianteServicio estudianteServicio;
    private final ProfesorServicio profesorServicio;
    private final CursoPeriodoServicio cursoPeriodoServicio;

    public MonitoriaControlador(MonitoriaServicio monitoriaServicio,
            EstudianteServicio estudianteServicio,
            ProfesorServicio profesorServicio,
            CursoPeriodoServicio cursoPeriodoServicio) {
        if (monitoriaServicio == null) {
            throw new IllegalArgumentException("Error en configuracion");
        }
        if (estudianteServicio == null) {
            throw new IllegalArgumentException("Error en configuracion");
        }
        if (profesorServicio == null) {
            throw new IllegalArgumentException("Error en configuracion");
        }
        if (cursoPeriodoServicio == null) {
            throw new IllegalArgumentException("Error en configuracion");
        }

        this.monitoriaServicio = monitoriaServicio;
        this.estudianteServicio = estudianteServicio;
        this.profesorServicio = profesorServicio;
        this.cursoPeriodoServicio = cursoPeriodoServicio;
    }
    public MonitoriaDto crear(MonitoriaCrearDto dto){
        return monitoriaServicio.crear(dto);
    }
    public MonitoriaDto evaluar(MonitoriaActualizarDto dto){
        return monitoriaServicio.evaluar(dto);
    }
    public boolean eliminar(UUID idCursoPeriodo, UUID idEstudiante, Long idProfesor){
        return monitoriaServicio.eliminar(idCursoPeriodo, idEstudiante, idProfesor);
    }
    public List<MonitoriaDto> listarTodas(){
        return monitoriaServicio.listarTodas();
    }
    public List<MonitoriaDto> listarPorEstudiante(UUID idEstudiante){
        return monitoriaServicio.listarPorEstudiante(idEstudiante);
    }
    public List<MonitoriaDto> listarPorProfesor(Long idProfesor){
        return monitoriaServicio.listarPorProfesor(idProfesor);
    }
    public List<MonitoriaDto> listarPorCursoPeriodo(UUID idCursoPeriodo){
        return monitoriaServicio.listarPorCursoPeriodo(idCursoPeriodo);
    }
    //acceso a otros servicios
    public EstudianteDto buscarEstudiante(UUID idEstudiante){
        return estudianteServicio.buscarPorId(idEstudiante);
    }
    public ProfesorDto buscarProfesor(Long idProfesor){
        return profesorServicio.buscarPorId(idProfesor);
    }
    public CursoPeriodoDto buscarPorCursoPeriodo(UUID idCursoPeriodo){
        return cursoPeriodoServicio.buscarPorId(idCursoPeriodo);
    }

}
