package edu.unimag.repositorio;

import edu.unimag.modelo.CursoEstudiante;
import edu.unimag.modelo.pk.CursoEstudiantePk;
import edu.unimag.repositorio.api.RepositorioBase;

public interface CursoEstudianteRepositorio extends RepositorioBase<CursoEstudiante, CursoEstudiantePk> {

    boolean insertarSiNoExiste(CursoEstudiante cursoEstudiante);
}
