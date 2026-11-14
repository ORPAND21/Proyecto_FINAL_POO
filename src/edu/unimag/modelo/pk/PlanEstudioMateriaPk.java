package edu.unimag.modelo.pk;

import edu.unimag.modelo.Materia;
import edu.unimag.modelo.PlanEstudio;
import java.util.Objects;

public class PlanEstudioMateriaPk {

    private final PlanEstudio planEstudio;
    private final Materia materia;

    // Constructor para reflexión
    protected PlanEstudioMateriaPk() {
        this.planEstudio = null;
        this.materia = null;
    }

    // Constructor principal
    public PlanEstudioMateriaPk(PlanEstudio planEstudio, Materia materia) {
        if (planEstudio == null) {
            throw new IllegalArgumentException("Plan de estudio es obligatorio");
        }
        if (materia == null) {
            throw new IllegalArgumentException("Materia es obligatoria");
        }
        this.planEstudio = planEstudio;
        this.materia = materia;
    }

    // Getters
    public PlanEstudio getPlanEstudio() {
        return planEstudio;
    }

    public Materia getMateria() {
        return materia;
    }

    // Ley de Demeter (acceso a los IDs sin exponer la entidad completa)
    public Long getIdPlanEstudio() {
        return planEstudio.getIdPlanEstudio();
    }

    public Integer getIdMateria() {
        return materia.getIdMateria();
    }

    // Identidad
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(getIdPlanEstudio());
        hash = 31 * hash + Objects.hashCode(getIdMateria());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlanEstudioMateriaPk other = (PlanEstudioMateriaPk) obj;
        if (!Objects.equals(this.planEstudio, other.planEstudio)) {
            return false;
        }
        return Objects.equals(this.materia, other.materia);
    }

    @Override
    public String toString() {
        return "PlanEstudioMateriaPk{"
                + "idPlanEstudio=" + getIdPlanEstudio()
                + ", idMateria=" + getIdMateria() + "}";
    }
}