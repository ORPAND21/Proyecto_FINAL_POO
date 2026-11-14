package edu.unimag.repositorio;

import edu.unimag.modelo.Monitoria;
import edu.unimag.modelo.pk.MonitoriaPk;
import edu.unimag.repositorio.api.RepositorioBase;

public interface MonitoriaRepositorio extends RepositorioBase<Monitoria, MonitoriaPk> {

    boolean insertarSiNoExiste(Monitoria monitoria);
}
