package edu.unimag.dto.planestudiomateria;

public record PlanEstudioMateriaCrearDto(
        Long idPlanEstudio,
        Integer idMateria,
        Integer semestre,
        Integer orden) {

    public PlanEstudioMateriaCrearDto {
        if (idPlanEstudio == null) {
            throw new IllegalArgumentException("id plan estudio null");
        }
        if (idMateria == null) {
            throw new IllegalArgumentException("id materia null");
        }
        if (semestre == null || semestre <= 0) {
            throw new IllegalArgumentException("El semestre debe ser mayor a 0");
        }
        if (orden == null || orden <= 0) {
            throw new IllegalArgumentException("El orden debe ser mayor a 0");
        }
    }

}
