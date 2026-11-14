package edu.unimag.mapeador;

import edu.unimag.dto.materia.MateriaDto;
import edu.unimag.dto.planestudio.PlanEstudioDto;
import edu.unimag.dto.planestudiomateria.PlanEstudioMateriaDto;
import edu.unimag.modelo.Materia;
import edu.unimag.modelo.PlanEstudio;
import edu.unimag.modelo.PlanEstudioMateria;
import edu.unimag.modelo.Programa;
import edu.unimag.repositorio.cargador.FacultadCargador;
import edu.unimag.repositorio.cargador.MateriaCargador;
import edu.unimag.repositorio.cargador.PlanEstudioCargador;
import edu.unimag.repositorio.cargador.ProgramaCargador;
import java.util.ArrayList;
import java.util.List;

public class PlanEstudioMateriaMapeador {

    private final PlanEstudioMapeador planEstudioMapeador;
    private final MateriaMapeador materiaMapeador;

    public PlanEstudioMateriaMapeador(PlanEstudioMapeador planEstudioMapeador,
            MateriaMapeador materiaMapeador) {
        if (planEstudioMapeador == null) {
            throw new IllegalArgumentException("Fallo en el mapeador plan de estudio");
        }
        if (materiaMapeador == null) {
            throw new IllegalArgumentException("Fallo en el mapeador materia");
        }
        this.planEstudioMapeador = planEstudioMapeador;
        this.materiaMapeador = materiaMapeador;
    }

    public List<PlanEstudioMateriaDto> toDtoList(
            List<PlanEstudioMateria> entidades,
            PlanEstudioCargador planEstudioCargador,
            MateriaCargador materiaCargador,
            ProgramaCargador programaCargador,
            FacultadCargador facultadCargador) {
        if (entidades == null || entidades.isEmpty()) {
            return List.of();
        }
        if (planEstudioCargador == null) {
            throw new IllegalArgumentException("El cargador de plan de estudio no puede ser nulo");
        }
        if (materiaCargador == null) {
            throw new IllegalArgumentException("El cargador de materia no puede ser nulo");
        }
        if (programaCargador == null) {
            throw new IllegalArgumentException("El cargador de programa no puede ser nulo");
        }
        if (facultadCargador == null) {
            throw new IllegalArgumentException("El cargador de facultad no puede ser nulo");
        }
        List<PlanEstudioMateriaDto> resultado = new ArrayList<>(entidades.size());
        for (PlanEstudioMateria miEntidad : entidades) {
            if (!validarIntegridad(miEntidad, planEstudioCargador, materiaCargador,
                    programaCargador, facultadCargador)) {
                continue;
            }
            resultado.add(transformarDto(miEntidad, planEstudioCargador, materiaCargador,
                    programaCargador, facultadCargador));
        }
        return resultado;
    }

    //metodos auxiliares
    private boolean validarIntegridad(PlanEstudioMateria miEntidad,
            PlanEstudioCargador planEstudioCargador,
            MateriaCargador materiaCargador,
            ProgramaCargador programaCargador,
            FacultadCargador facultadCargador) {
        if (!materiaCargador.existe(miEntidad.getIdMateria())) {
            return false;
        }
        if (!planEstudioCargador.existe(miEntidad.getIdPlanEstudio())) {
            return false;
        }
        PlanEstudio planEstudio = planEstudioCargador.obtener(miEntidad.getIdPlanEstudio()).orElse(null);
        if (planEstudio == null) {
            return false;
        }
        if (!programaCargador.existe(planEstudio.getIdPrograma())) {
            return false;
        }
        Programa programa = programaCargador.obtener(planEstudio.getIdPrograma()).orElse(null);
        if (programa == null) {
            return false;
        }
        if (!facultadCargador.existe(programa.getIdFacultad())) {
            return false;
        }
        return true;
    }

    private PlanEstudioMateriaDto transformarDto(PlanEstudioMateria miEntidad,
            PlanEstudioCargador planEstudioCargador,
            MateriaCargador materiaCargador,
            ProgramaCargador programaCargador,
            FacultadCargador facultadCargador) {
        PlanEstudio planEstudio = planEstudioCargador.obtener(miEntidad.getIdPlanEstudio())
                .orElseThrow(() -> new IllegalStateException("Error de integridad en plan de estudio"));
        PlanEstudioDto planEstudioDto = planEstudioMapeador.toDto(
                planEstudio,
                programaCargador,
                facultadCargador);

        Materia materia = materiaCargador.obtener(miEntidad.getIdMateria())
                .orElseThrow(() -> new IllegalStateException("Error de integridad en materia"));
        MateriaDto materiaDto = materiaMapeador.toDto(materia);

        return new PlanEstudioMateriaDto(
                planEstudioDto,
                materiaDto,
                miEntidad.getSemestrePlanEstudioMateria(),
                miEntidad.getOrdenPlanEstudioMateria());
    }

    public PlanEstudioMateriaDto toDto(
            PlanEstudioMateria entidad,
            PlanEstudioCargador planEstudioCargador,
            MateriaCargador materiaCargador,
            ProgramaCargador programaCargador,
            FacultadCargador facultadCargador) {
        if (entidad == null) {
            throw new IllegalArgumentException("La entidad PlanEstudioMateria no puede ser nula");
        }
        if (planEstudioCargador == null) {
            throw new IllegalArgumentException("El cargador de plan de estudio no puede ser nulo");
        }
        if (materiaCargador == null) {
            throw new IllegalArgumentException("El cargador de materia no puede ser nulo");
        }
        if (programaCargador == null) {
            throw new IllegalArgumentException("El cargador de programa no puede ser nulo");
        }
        if (facultadCargador == null) {
            throw new IllegalArgumentException("El cargador de facultad no puede ser nulo");
        }
        if (!validarIntegridad(entidad, planEstudioCargador, materiaCargador,
                programaCargador, facultadCargador)) {
            if (!materiaCargador.existe(entidad.getIdMateria())) {
                throw new IllegalStateException("Materia NO encontrada en el cache");
            }
            if (!planEstudioCargador.existe(entidad.getIdPlanEstudio())) {
                throw new IllegalStateException("Plan de estudio NO encontrado en el cache");
            }
            PlanEstudio planEstudio = planEstudioCargador.obtener(entidad.getIdPlanEstudio()).orElse(null);
            if (planEstudio != null) {
                if (!programaCargador.existe(planEstudio.getIdPrograma())) {
                    throw new IllegalStateException("Programa NO encontrado en el cache");
                }
                Programa programa = programaCargador.obtener(planEstudio.getIdPrograma()).orElse(null);
                if (programa != null && !facultadCargador.existe(programa.getIdFacultad())) {
                    throw new IllegalStateException("Facultad NO encontrada en el cache");
                }
            }
            throw new IllegalStateException("Error con el plan estudio materia");
        }
        return transformarDto(entidad, planEstudioCargador, materiaCargador,
                programaCargador, facultadCargador);
    }

}
