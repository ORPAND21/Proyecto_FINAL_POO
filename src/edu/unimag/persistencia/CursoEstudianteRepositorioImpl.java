package edu.unimag.persistencia;

import com.cleandev.tpa.api.TpaRepository;
import edu.unimag.modelo.CursoEstudiante;
import edu.unimag.modelo.pk.CursoEstudiantePk;
import edu.unimag.persistencia.abstracta.RepositorioBaseAbstracto;
import edu.unimag.repositorio.CursoEstudianteRepositorio;

public class CursoEstudianteRepositorioImpl
        extends RepositorioBaseAbstracto<CursoEstudiante, CursoEstudiantePk>
        implements CursoEstudianteRepositorio {

    public CursoEstudianteRepositorioImpl(TpaRepository<CursoEstudiante, CursoEstudiantePk> tpaRepository) {
        super(tpaRepository);
    }

    @Override
    public boolean insertarSiNoExiste(CursoEstudiante cursoEstudiante) {
        if (cursoEstudiante == null) {
            throw new IllegalArgumentException("La inscripcion no puede ser nula");
        }
        if (cursoEstudiante.getIdCursoEstudiante() == null) {
            throw new IllegalArgumentException("Error en la PK");
        }
        return tpaRepository.executeAtomic(repo -> {
            CursoEstudiantePk pk = cursoEstudiante.getIdCursoEstudiante();
            if (repo.findById(pk).isPresent()) { // si ya esta inscrito, evita duplicarlo
                return false;
            }
            repo.save(cursoEstudiante); // si no existe, guarda
            return true;
        });
    }
}
