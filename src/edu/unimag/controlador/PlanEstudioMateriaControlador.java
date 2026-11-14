package edu.unimag.controlador;

import edu.unimag.dto.materia.MateriaDto;
import edu.unimag.dto.planestudio.PlanEstudioDto;
import edu.unimag.dto.planestudiomateria.PlanEstudioMateriaActualizarDto;
import edu.unimag.dto.planestudiomateria.PlanEstudioMateriaCrearDto;
import edu.unimag.dto.planestudiomateria.PlanEstudioMateriaDto;
import edu.unimag.servicio.MateriaServicio;
import edu.unimag.servicio.PlanEstudioMateriaServicio;
import edu.unimag.servicio.PlanEstudioServicio;
import java.util.List;

public class PlanEstudioMateriaControlador {

    private final PlanEstudioMateriaServicio planEstudioMateriaServicio;
    private final PlanEstudioServicio planEstudioServicio;
    private final MateriaServicio materiaServicio;

    public PlanEstudioMateriaControlador(PlanEstudioMateriaServicio planEstudioMateriaServicio,
            PlanEstudioServicio planEstudioServicio,
            MateriaServicio materiaServicio) {
        if (planEstudioMateriaServicio == null) {
            throw new IllegalArgumentException("Error en configuracion");
        }
        if (planEstudioServicio == null) {
            throw new IllegalArgumentException("Error en configuracion");
        }
        if (materiaServicio == null) {
            throw new IllegalArgumentException("Error en configuracion");
        }

        this.planEstudioMateriaServicio = planEstudioMateriaServicio;
        this.planEstudioServicio = planEstudioServicio;
        this.materiaServicio = materiaServicio;
    }
    public PlanEstudioMateriaDto asignar(PlanEstudioMateriaCrearDto dto){
        return planEstudioMateriaServicio.asignar(dto);
    }
    public PlanEstudioMateriaDto actualizar(PlanEstudioMateriaActualizarDto dto){
        return planEstudioMateriaServicio.actualizar(dto);
    }
    public boolean quitar(Long idPlanEstudio, Integer idMateria){
        return planEstudioMateriaServicio.quitar(idPlanEstudio, idMateria);
    }
    public List<PlanEstudioMateriaDto> listarPorPlan(Long idPlanEstudio){
        return planEstudioMateriaServicio.listarPorPlan(idPlanEstudio);
    }
    public List<PlanEstudioMateriaDto> listarPorMateria(Integer idMateria){
        return planEstudioMateriaServicio.listarPorMateria(idMateria);
    }
    //acceso a otros servicios
    public PlanEstudioDto buscarPlanEstudio(Long idPlanEstudio){
        return planEstudioServicio.buscarPorId(idPlanEstudio);
    }
    public MateriaDto buscarMateria(Integer idMateria){
        return materiaServicio.buscarPorId(idMateria);
    }

}
