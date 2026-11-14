package edu.unimag.persistencia;

import com.cleandev.tpa.api.TpaRepository;
import edu.unimag.modelo.Matricula;
import edu.unimag.modelo.pk.MatriculaPk;
import edu.unimag.persistencia.abstracta.RepositorioBaseAbstracto;
import edu.unimag.repositorio.MatriculaRepositorio;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatriculaRepositorioImpl extends RepositorioBaseAbstracto<Matricula, MatriculaPk>
        implements MatriculaRepositorio {

    public MatriculaRepositorioImpl(TpaRepository<Matricula, MatriculaPk> tpaRepository) {
        super(tpaRepository);
    }

    @Override
    public boolean insertarSiNoExiste(Matricula matricula) {
        if (matricula == null) {
            throw new IllegalArgumentException("Matricula incorrecta");
        }
        if (matricula.getIdMatricula() == null) {
            throw new IllegalArgumentException("Error en la PK de matricula");
        }
        return tpaRepository.executeAtomic(repo -> {
            MatriculaPk pk = matricula.getIdMatricula();
            if (repo.findById(pk).isPresent()) {
                return false;
            }
            repo.save(matricula);
            return true;
        });
    }

    @Override
    public List<Matricula> listarPorEstudiante(UUID idEstudiante) {
        if (idEstudiante == null) {
            throw new IllegalArgumentException("El ID del estudiante es nulo");
        }
        List<Matricula> resultado = new ArrayList<>();
        for (Matricula matricula : tpaRepository.findAll()) {
            if (idEstudiante.equals(matricula.getIdEstudiante())) {
                resultado.add(matricula);
            }
        }
        return resultado;
    }

    @Override
    public List<Matricula> listarPorCursoPeriodo(UUID idCursoPeriodo) {
        if (idCursoPeriodo == null) {
            throw new IllegalArgumentException("El ID del curso periodo es nulo");
        }
        List<Matricula> resultado = new ArrayList<>();
        for (Matricula matricula : tpaRepository.findAll()) {
            if (idCursoPeriodo.equals(matricula.getIdCursoPeriodo())) {
                resultado.add(matricula);
            }
        }
        return resultado;
    }
}
