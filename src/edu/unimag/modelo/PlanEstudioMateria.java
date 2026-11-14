package edu.unimag.modelo;

import com.cleandev.tpa.api.annotations.TpaId;
import edu.unimag.modelo.pk.PlanEstudioMateriaPk;

public class PlanEstudioMateria {

    @TpaId
    private PlanEstudioMateriaPk idPlanEstudioMateria;
    private Integer semestrePlanEstudioMateria;
    private Integer ordenPlanEstudioMateria;

    // Reflexión
    protected PlanEstudioMateria() {
    }

    // Creación de hidratacion en un solo constructor
    public PlanEstudioMateria(PlanEstudioMateriaPk id, Integer semestre, Integer orden) {
        if (id == null) {
            throw new IllegalArgumentException("La clave del plan-materia es obligatoria");
        }

        this.idPlanEstudioMateria = id;
        this.semestrePlanEstudioMateria = semestre;
        this.ordenPlanEstudioMateria = orden;
    }

    // Métodos de comportamiento
    public void reubicarSemestre(Integer nuevoSemestre) {
        if (nuevoSemestre.equals(this.semestrePlanEstudioMateria)) {
            throw new IllegalArgumentException(
                    "La materia ya se encuentra en el semestre " + nuevoSemestre
            );
        }
        this.semestrePlanEstudioMateria = nuevoSemestre;
    }

    public void actualizarOrden(Integer nuevoOrden) {
        if (nuevoOrden.equals(this.ordenPlanEstudioMateria)) {
            throw new IllegalArgumentException(
                    "La materia ya tiene el orden " + nuevoOrden
            );
        }
        this.ordenPlanEstudioMateria = nuevoOrden;
    }

    // Getters
    public PlanEstudioMateriaPk getIdPlanEstudioMateria() {
        return idPlanEstudioMateria;
    }

    public Integer getSemestrePlanEstudioMateria() {
        return semestrePlanEstudioMateria;
    }

    public Integer getOrdenPlanEstudioMateria() {
        return ordenPlanEstudioMateria;
    }

    // Getters de conveniencia (Ley de Demeter)
    public Long getIdPlanEstudio() {
        return this.idPlanEstudioMateria.getIdPlanEstudio();
    }

    public Integer getIdMateria() {
        return this.idPlanEstudioMateria.getIdMateria();
    }

    public PlanEstudio getPlanEstudio() {
        return this.idPlanEstudioMateria.getPlanEstudio();
    }

    public Materia getMateria() {
        return this.idPlanEstudioMateria.getMateria();
    }
}
