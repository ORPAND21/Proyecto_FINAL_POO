package edu.unimag.repositorio;

import edu.unimag.modelo.Matricula;
import edu.unimag.modelo.pk.MatriculaPk;
import edu.unimag.repositorio.api.RepositorioBase;
import java.util.List;
import java.util.UUID;

public interface MatriculaRepositorio extends RepositorioBase<Matricula, MatriculaPk> {

    /**
     * Inserta una matricula solo si su llave compuesta (los cinco modelos) no
     * existe previamente.
     *
     * @param matricula la matricula a insertar
     * @return true si se inserto, false si la llave ya existia
     */
    boolean insertarSiNoExiste(Matricula matricula);

    List<Matricula> listarPorEstudiante(UUID idEstudiante);

    List<Matricula> listarPorCursoPeriodo(UUID idCursoPeriodo);
}
