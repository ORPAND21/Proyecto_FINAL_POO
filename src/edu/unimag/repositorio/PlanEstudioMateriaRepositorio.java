package edu.unimag.repositorio;

import edu.unimag.modelo.PlanEstudioMateria;
import edu.unimag.modelo.pk.PlanEstudioMateriaPk;
import edu.unimag.repositorio.api.RepositorioBase;

public interface PlanEstudioMateriaRepositorio extends RepositorioBase<PlanEstudioMateria, PlanEstudioMateriaPk> {

    boolean insertarSiNoExiste(PlanEstudioMateria planEstudioMateria);
}
