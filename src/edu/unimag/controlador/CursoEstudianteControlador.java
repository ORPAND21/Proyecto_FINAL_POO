package edu.unimag.controlador;

import edu.unimag.dto.cursoestudiante.CursoEstudianteCrearDto;
import edu.unimag.dto.cursoestudiante.CursoEstudianteDto;
import edu.unimag.dto.cursoperiodo.CursoPeriodoDto;
import edu.unimag.dto.estudiante.EstudianteDto;
import edu.unimag.servicio.CursoEstudianteServicio;
import edu.unimag.servicio.CursoPeriodoServicio;
import edu.unimag.servicio.EstudianteServicio;
import java.util.List;
import java.util.UUID;

public class CursoEstudianteControlador {

    private final CursoEstudianteServicio cursoEstudianteServicio;
    private final EstudianteServicio estudianteServicio;
    private final CursoPeriodoServicio cursoPeriodoServicio;

    public CursoEstudianteControlador(CursoEstudianteServicio cursoEstudianteServicio,
            EstudianteServicio estudianteServicio,
            CursoPeriodoServicio cursoPeriodoServicio) {
        if (cursoEstudianteServicio == null) {
            throw new IllegalArgumentException("Error en configuracion");
        }
        if (estudianteServicio == null) {
            throw new IllegalArgumentException("Error en configuracion");
        }
        if (cursoPeriodoServicio == null) {
            throw new IllegalArgumentException("Error en configuracion");
        }

        this.cursoEstudianteServicio = cursoEstudianteServicio;
        this.estudianteServicio = estudianteServicio;
        this.cursoPeriodoServicio = cursoPeriodoServicio;
    }
    public CursoEstudianteDto inscribir(CursoEstudianteCrearDto dto){
        return cursoEstudianteServicio.inscribir(dto);
    }
    public CursoEstudianteDto admitir(UUID idCursoPeriodo, UUID idEstudiante){
        return cursoEstudianteServicio.admitir(idCursoPeriodo, idEstudiante);
    }
    public CursoEstudianteDto aprobar(UUID idCursoPeriodo, UUID idEstudiante){
        return cursoEstudianteServicio.aprobar(idCursoPeriodo, idEstudiante);
    }
    public CursoEstudianteDto reprobar(UUID idCursoPeriodo, UUID idEstudiante){
        return cursoEstudianteServicio.reprobar(idCursoPeriodo, idEstudiante);
    }
    public CursoEstudianteDto retirar(UUID idCursoPeriodo, UUID idEstudiante){
        return cursoEstudianteServicio.retirar(idCursoPeriodo, idEstudiante);
    }
    public CursoEstudianteDto reAsignarCurso(UUID idCursoPeriodo, UUID idEstudiante){
        return cursoEstudianteServicio.reAsignarCurso(idCursoPeriodo, idEstudiante);
    }
    public CursoEstudianteDto rechazar(UUID idCursoPeriodo, UUID idEstudiante){
        return cursoEstudianteServicio.rechazar(idCursoPeriodo, idEstudiante);
    }
    public List<CursoEstudianteDto> listarPorEstudiante(UUID idEstudiante){
        return cursoEstudianteServicio.listarPorEstudiante(idEstudiante);
    }
    public List<CursoEstudianteDto> listarPorCursoPeriodo(UUID idCursoPeriodo){
        return cursoEstudianteServicio.listarPorCursoPeriodo(idCursoPeriodo);
    }
    public List<CursoEstudianteDto> listarPorMateria(Integer idMateria){
        return cursoEstudianteServicio.listarPorMateria(idMateria);
    }
    public List<CursoEstudianteDto> listarPorMateriaPeriodo(Integer idMateria, Integer idPeriodo){
        return cursoEstudianteServicio.listarPorMateriaYPorPeriodo(idMateria, idPeriodo);
    }
    public List<CursoEstudianteDto> listarPorPeriodo(Integer idPeriodo){
        return cursoEstudianteServicio.listarPorPeriodo(idPeriodo);
    }
    //acceso a otros servicios
    public EstudianteDto buscarEstudiante(UUID idEstudiante){
        return estudianteServicio.buscarPorId(idEstudiante);
    }
    public CursoPeriodoDto buscarPorCursoPeriodo(UUID idCursoPeriodo){
        return cursoPeriodoServicio.buscarPorId(idCursoPeriodo);
    }

}
