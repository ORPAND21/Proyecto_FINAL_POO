package edu.unimag.persistencia;

import com.cleandev.tpa.api.TpaRepository;
import edu.unimag.modelo.Monitoria;
import edu.unimag.modelo.pk.MonitoriaPk;
import edu.unimag.persistencia.abstracta.RepositorioBaseAbstracto;
import edu.unimag.repositorio.MonitoriaRepositorio;

public class MonitoriaRepositorioImpl
        extends RepositorioBaseAbstracto<Monitoria, MonitoriaPk>
        implements MonitoriaRepositorio {

    public MonitoriaRepositorioImpl(TpaRepository<Monitoria, MonitoriaPk> tpaRepository) {
        super(tpaRepository);
    }

    @Override
    protected boolean permiteBorradoFisico() {
        return true;
    }

    @Override
    public boolean insertarSiNoExiste(Monitoria monitoria) {
        if (monitoria == null) {
            throw new IllegalArgumentException("La monitoria no puede ser nula");
        }
        if (monitoria.getIdMonitoria() == null) {
            throw new IllegalArgumentException("Error en la PK");
        }
        return tpaRepository.executeAtomic(repo -> {
            MonitoriaPk pk = monitoria.getIdMonitoria();
            if (repo.findById(pk).isPresent()) { // si ya existe, evita duplicarla
                return false;
            }
            repo.save(monitoria); // si no existe, guarda
            return true;
        });
    }
}
