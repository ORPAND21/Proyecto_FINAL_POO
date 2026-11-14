package edu.unimag.servicio;

import edu.unimag.dto.planestudiomateria.PlanEstudioMateriaActualizarDto;
import edu.unimag.dto.planestudiomateria.PlanEstudioMateriaCrearDto;
import edu.unimag.dto.planestudiomateria.PlanEstudioMateriaDto;
import edu.unimag.mapeador.PlanEstudioMateriaMapeador;
import edu.unimag.modelo.Facultad;
import edu.unimag.modelo.Materia;
import edu.unimag.modelo.PlanEstudio;
import edu.unimag.modelo.PlanEstudioMateria;
import edu.unimag.modelo.Programa;
import edu.unimag.modelo.pk.PlanEstudioMateriaPk;
import edu.unimag.repositorio.FacultadRepositorio;
import edu.unimag.repositorio.MateriaRepositorio;
import edu.unimag.repositorio.PlanEstudioMateriaRepositorio;
import edu.unimag.repositorio.PlanEstudioRepositorio;
import edu.unimag.repositorio.ProgramaRepositorio;
import edu.unimag.repositorio.cargador.FacultadCargador;
import edu.unimag.repositorio.cargador.MateriaCargador;
import edu.unimag.repositorio.cargador.PlanEstudioCargador;
import edu.unimag.repositorio.cargador.ProgramaCargador;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlanEstudioMateriaServicio {

    private final PlanEstudioMateriaRepositorio planEstudioMateriaRepositorio;
    private final PlanEstudioRepositorio planEstudioRepositorio;
    private final MateriaRepositorio materiaRepositorio;
    private final ProgramaRepositorio programaRepositorio;
    private final FacultadRepositorio facultadRepositorio;
    private final PlanEstudioMateriaMapeador mapeador;

    public PlanEstudioMateriaServicio(PlanEstudioMateriaRepositorio planEstudioMateriaRepositorio,
            PlanEstudioRepositorio planEstudioRepositorio,
            MateriaRepositorio materiaRepositorio,
            ProgramaRepositorio programaRepositorio,
            FacultadRepositorio facultadRepositorio,
            PlanEstudioMateriaMapeador mapeador) {
        if (planEstudioMateriaRepositorio == null) {
            throw new IllegalArgumentException("Repo para materias del plan no puede ser nulo");
        }
        if (planEstudioRepositorio == null) {
            throw new IllegalArgumentException("Repo para planes de estudio no puede ser nulo");
        }
        if (materiaRepositorio == null) {
            throw new IllegalArgumentException("Repo para materia no puede ser nulo");
        }
        if (programaRepositorio == null) {
            throw new IllegalArgumentException("Repo para programa no puede ser nulo");
        }
        if (facultadRepositorio == null) {
            throw new IllegalArgumentException("Repo para facultad no puede ser nulo");
        }
        if (mapeador == null) {
            throw new IllegalArgumentException("El mapeador no puede ser nulo");
        }
        this.planEstudioMateriaRepositorio = planEstudioMateriaRepositorio;
        this.planEstudioRepositorio = planEstudioRepositorio;
        this.materiaRepositorio = materiaRepositorio;
        this.programaRepositorio = programaRepositorio;
        this.facultadRepositorio = facultadRepositorio;
        this.mapeador = mapeador;
    }

    //logica del negocio
    //1. Asignar materia a plan
    public PlanEstudioMateriaDto asignar(PlanEstudioMateriaCrearDto dto) {
        PlanEstudio planEstudio = planEstudioRepositorio.buscarPorId(dto.idPlanEstudio()).orElseThrow(
                () -> new IllegalArgumentException("Plan de estudio no encontrado"));
        Materia materia = materiaRepositorio.buscarPorId(dto.idMateria()).orElseThrow(
                () -> new IllegalArgumentException("Materia no encontrada"));

        PlanEstudioMateriaPk pk = new PlanEstudioMateriaPk(planEstudio, materia);
        PlanEstudioMateria nuevaAsignacion = new PlanEstudioMateria(pk, dto.semestre(), dto.orden());
        boolean insertado = planEstudioMateriaRepositorio.insertarSiNoExiste(nuevaAsignacion);
        if (!insertado) {
            throw new IllegalStateException("La materia ya esta en el plan o hay error al grabar");
        }
        return hidratarYmapearUnico(nuevaAsignacion, planEstudio, materia);
    }

    //4. Actualizar semestre u orden
    public PlanEstudioMateriaDto actualizar(PlanEstudioMateriaActualizarDto dto) {
        PlanEstudio planEstudio = planEstudioRepositorio.buscarPorId(dto.idPlanEstudio()).
                orElseThrow(() -> new IllegalArgumentException("Plan de estudio no encontrado"));
        Materia materia = materiaRepositorio.buscarPorId(dto.idMateria()).
                orElseThrow(() -> new IllegalArgumentException("Materia no encontrada"));

        PlanEstudioMateriaPk pk = new PlanEstudioMateriaPk(planEstudio, materia);
        PlanEstudioMateria asignacion = planEstudioMateriaRepositorio.buscarPorId(pk)
                .orElseThrow(() -> new IllegalArgumentException("La materia no esta asignada al plan"));

        boolean cambiaSemestre = !dto.semestre().equals(asignacion.getSemestrePlanEstudioMateria());
        boolean cambiaOrden = !dto.orden().equals(asignacion.getOrdenPlanEstudioMateria());
        if (!cambiaSemestre && !cambiaOrden) {
            throw new IllegalArgumentException("No hay cambios: el semestre y el orden son los mismos");
        }
        if (cambiaSemestre) {
            asignacion.reubicarSemestre(dto.semestre());
        }
        if (cambiaOrden) {
            asignacion.actualizarOrden(dto.orden());
        }
        PlanEstudioMateria actualizado = planEstudioMateriaRepositorio.actualizar(asignacion);

        return hidratarYmapearUnico(actualizado, planEstudio, materia);
    }

    //5. Quitar materia del plan (borrado fisico)
    public boolean quitar(Long idPlanEstudio, Integer idMateria) {
        PlanEstudio planEstudio = planEstudioRepositorio.buscarPorId(idPlanEstudio).
                orElseThrow(() -> new IllegalArgumentException("Plan de estudio no encontrado"));
        Materia materia = materiaRepositorio.buscarPorId(idMateria).
                orElseThrow(() -> new IllegalArgumentException("Materia no encontrada"));

        PlanEstudioMateriaPk pk = new PlanEstudioMateriaPk(planEstudio, materia);
        if (!planEstudioMateriaRepositorio.buscarPorId(pk).isPresent()) {
            throw new IllegalArgumentException(
                    "No se puede eliminar: La materia " + idMateria + " no esta asignada al plan " + idPlanEstudio
            );
        }
        return planEstudioMateriaRepositorio.eliminar(pk);
    }

    //2. Listar materias de un plan
    public List<PlanEstudioMateriaDto> listarPorPlan(Long idPlanEstudio) {
        PlanEstudio planEstudio = planEstudioRepositorio.buscarPorId(idPlanEstudio).
                orElseThrow(() -> new IllegalArgumentException("No existe el plan de estudio"));

        List<PlanEstudioMateria> delPlan = new ArrayList<>();
        for (PlanEstudioMateria asignacion : planEstudioMateriaRepositorio.listarTodos()) {
            if (asignacion.getIdPlanEstudio().equals(idPlanEstudio)) {
                delPlan.add(asignacion);
            }
        }
        if (delPlan.isEmpty()) {
            return new ArrayList<>();
        }
        delPlan.sort(Comparator.comparing(PlanEstudioMateria::getSemestrePlanEstudioMateria)
                .thenComparing(PlanEstudioMateria::getOrdenPlanEstudioMateria));
        return hidratarYmapearListaConPlan(delPlan, planEstudio);
    }

    //3. Listar planes que incluyen una materia
    public List<PlanEstudioMateriaDto> listarPorMateria(Integer idMateria) {
        Materia materia = materiaRepositorio.buscarPorId(idMateria).
                orElseThrow(() -> new IllegalArgumentException("No existe la materia"));

        List<PlanEstudioMateria> deLaMateria = new ArrayList<>();
        for (PlanEstudioMateria asignacion : planEstudioMateriaRepositorio.listarTodos()) {
            if (asignacion.getIdMateria().equals(idMateria)) {
                deLaMateria.add(asignacion);
            }
        }
        if (deLaMateria.isEmpty()) {
            return List.of();
        }
        deLaMateria.sort(Comparator.comparing(PlanEstudioMateria::getIdPlanEstudio));
        return hidratarYmapearListaConMateria(deLaMateria, materia);
    }

    //metodos auxiliares de hidratacion
    private List<PlanEstudioMateriaDto> hidratarYmapearListaGenerico(
            List<PlanEstudioMateria> asignaciones,
            PlanEstudioCargador planCargadorInicial,
            MateriaCargador materiaCargadorInicial
    ) {
        Set<Long> idsPlan = new HashSet<>();
        Set<Integer> idsMateria = new HashSet<>();

        for (PlanEstudioMateria asignacion : asignaciones) {
            idsPlan.add(asignacion.getIdPlanEstudio());
            idsMateria.add(asignacion.getIdMateria());
        }
        PlanEstudioCargador planCargador = planCargadorInicial != null
                ? planCargadorInicial
                : new PlanEstudioCargador(planEstudioRepositorio);
        planCargador.cargarPorIds(new ArrayList<>(idsPlan));

        MateriaCargador materiaCargador = materiaCargadorInicial != null
                ? materiaCargadorInicial
                : new MateriaCargador(materiaRepositorio);
        materiaCargador.cargarPorIds(new ArrayList<>(idsMateria));

        Set<UUID> idsPrograma = new HashSet<>();
        for (Long idPlan : idsPlan) {
            planCargador.obtener(idPlan).ifPresent(
                    plan -> idsPrograma.add(plan.getIdPrograma())
            );
        }
        ProgramaCargador programaCargador = new ProgramaCargador(programaRepositorio);
        programaCargador.cargarPorIds(new ArrayList<>(idsPrograma));

        Set<Long> idsFacultad = new HashSet<>();
        for (UUID idPrograma : idsPrograma) {
            programaCargador.obtener(idPrograma).ifPresent(
                    programa -> idsFacultad.add(programa.getIdFacultad())
            );
        }
        FacultadCargador facultadCargador = new FacultadCargador(facultadRepositorio);
        facultadCargador.cargarPorIds(new ArrayList<>(idsFacultad));

        return mapeador.toDtoList(asignaciones, planCargador, materiaCargador, programaCargador, facultadCargador);
    }

    private List<PlanEstudioMateriaDto> hidratarYmapearListaConPlan(List<PlanEstudioMateria> asignaciones, PlanEstudio planEstudio) {
        PlanEstudioCargador planCargador = new PlanEstudioCargador(planEstudioRepositorio);
        planCargador.registrarEnCache(planEstudio);
        return hidratarYmapearListaGenerico(asignaciones, planCargador, null);
    }

    private List<PlanEstudioMateriaDto> hidratarYmapearListaConMateria(List<PlanEstudioMateria> asignaciones, Materia materia) {
        MateriaCargador materiaCargador = new MateriaCargador(materiaRepositorio);
        materiaCargador.registrarEnCache(materia);
        return hidratarYmapearListaGenerico(asignaciones, null, materiaCargador);
    }

    private PlanEstudioMateriaDto hidratarYmapearUnico(PlanEstudioMateria asignacion,
            PlanEstudio planEstudio,
            Materia materia) {
        Programa programa = programaRepositorio.buscarPorId(planEstudio.getIdPrograma()).orElseThrow(
                () -> new IllegalStateException("Programa incorrecto"));

        Facultad facultad = facultadRepositorio.buscarPorId(programa.getIdFacultad()).orElseThrow(
                () -> new IllegalStateException("Facultad incorrecta"));

        PlanEstudioCargador planCargador = new PlanEstudioCargador(planEstudioRepositorio);
        planCargador.registrarEnCache(planEstudio);

        MateriaCargador materiaCargador = new MateriaCargador(materiaRepositorio);
        materiaCargador.registrarEnCache(materia);

        ProgramaCargador programaCargador = new ProgramaCargador(programaRepositorio);
        programaCargador.registrarEnCache(programa);

        FacultadCargador facultadCargador = new FacultadCargador(facultadRepositorio);
        facultadCargador.registrarEnCache(facultad);

        return mapeador.toDto(asignacion, planCargador, materiaCargador, programaCargador, facultadCargador);
    }

}
