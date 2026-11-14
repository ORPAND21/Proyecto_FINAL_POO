package edu.unimag.modelo.pk;

import edu.unimag.modelo.CursoPeriodo;
import edu.unimag.modelo.Estudiante;
import java.util.Objects;
import java.util.UUID;

public class CursoEstudiantePk {

    private final CursoPeriodo cursoPeriodo;
    private final Estudiante estudiante;

    // Constructor para reflexión
    protected CursoEstudiantePk() {
        this.cursoPeriodo = null;
        this.estudiante = null;
    }

    // Constructor principal
    public CursoEstudiantePk(CursoPeriodo cursoPeriodo, Estudiante estudiante) {
        if (cursoPeriodo == null) {
            throw new IllegalArgumentException("Curso periodo es obligatorio");
        }
        if (estudiante == null) {
            throw new IllegalArgumentException("Estudiante es obligatorio");
        }
        this.cursoPeriodo = cursoPeriodo;
        this.estudiante = estudiante;
    }

    // Getters
    public CursoPeriodo getCursoPeriodo() {
        return cursoPeriodo;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    // Ley de Demeter (acceso a los IDs sin exponer la entidad completa)
    public UUID getIdCursoPeriodo() {
        return cursoPeriodo.getIdCursoPeriodo();
    }

    public UUID getIdEstudiante() {
        return estudiante.getIdEstudiante();
    }

    // Identidad
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(getIdCursoPeriodo());
        hash = 31 * hash + Objects.hashCode(getIdEstudiante());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CursoEstudiantePk other = (CursoEstudiantePk) obj;
        if (!Objects.equals(this.getIdCursoPeriodo(), other.getIdCursoPeriodo())) {
            return false;
        }
        return Objects.equals(this.getIdEstudiante(), other.getIdEstudiante());
    }

    @Override
    public String toString() {
        return "CursoEstudiantePk{"
                + "idCursoPeriodo=" + getIdCursoPeriodo()
                + ", idEstudiante=" + getIdEstudiante() + "}";
    }
}