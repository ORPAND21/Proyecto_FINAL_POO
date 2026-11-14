
package edu.unimag.dto.cursoProfesor;

import edu.unimag.modelo.enumeracion.TipoAsignacion;
import java.util.UUID;


public record CursoProfesorActualizarDto(
        UUID idCursoPeriodo,
        Long idProfesor,
        TipoAsignacion tipoAsignacion) {

    public CursoProfesorActualizarDto {
        if (idCursoPeriodo == null) {
            throw new IllegalArgumentException("id curso null");

        }
        if (idProfesor == null) {
            throw new IllegalArgumentException("id profesor null");

        }
        if (tipoAsignacion == null) {
            throw new IllegalArgumentException("Tipo asignacion null");

        }

    }
    
    

}
