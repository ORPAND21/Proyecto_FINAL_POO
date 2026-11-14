package edu.unimag.persistencia;

import com.cleandev.tpa.api.TpaRepository;
import edu.unimag.modelo.PlanEstudioMateria;
import edu.unimag.modelo.pk.PlanEstudioMateriaPk;
import edu.unimag.persistencia.abstracta.RepositorioBaseAbstracto;
import edu.unimag.repositorio.PlanEstudioMateriaRepositorio;

public class PlanEstudioMateriaRepositorioImpl
        extends RepositorioBaseAbstracto<PlanEstudioMateria, PlanEstudioMateriaPk>
        implements PlanEstudioMateriaRepositorio {

    public PlanEstudioMateriaRepositorioImpl(TpaRepository<PlanEstudioMateria, PlanEstudioMateriaPk> tpaRepository) {
        super(tpaRepository);
    }

    @Override
    protected boolean permiteBorradoFisico() {
        return true;
    }

    @Override
    public boolean insertarSiNoExiste(PlanEstudioMateria planEstudioMateria) {
        if (planEstudioMateria == null) {
            throw new IllegalArgumentException("La relacion plan-materia no puede ser nula");
        }
        if (planEstudioMateria.getIdPlanEstudioMateria() == null) {
            throw new IllegalArgumentException("Error en la PK");
        }
        return tpaRepository.executeAtomic(repo -> {
            PlanEstudioMateriaPk pk = planEstudioMateria.getIdPlanEstudioMateria();
            if (repo.findById(pk).isPresent()) { // si ya existe, evita duplicarla
                return false;
            }
            repo.save(planEstudioMateria); // si no existe, guarda
            return true;
        });
    }
}
