package edu.unimag.modelo.pk;

import edu.unimag.modelo.CursoPeriodo;
import edu.unimag.modelo.Estudiante;
import edu.unimag.modelo.Profesor;
import java.util.Objects;
import java.util.UUID;

public class MonitoriaPk {

    private final CursoPeriodo cursoPeriodo;
    private final Estudiante estudiante;
    private final Profesor profesor;

    // Constructor para reflexión
    protected MonitoriaPk() {
        this.cursoPeriodo = null;
        this.estudiante = null;
        this.profesor = null;
    }

    // Constructor principal
    public MonitoriaPk(CursoPeriodo cursoPeriodo, Estudiante estudiante, Profesor profesor) {
        if (cursoPeriodo == null) {
            throw new IllegalArgumentException("Curso periodo es obligatorio");
        }
        if (estudiante == null) {
            throw new IllegalArgumentException("Estudiante es obligatorio");
        }
        if (profesor == null) {
            throw new IllegalArgumentException("Profesor es obligatorio");
        }
        this.cursoPeriodo = cursoPeriodo;
        this.estudiante = estudiante;
        this.profesor = profesor;
    }

    // Getters
    public CursoPeriodo getCursoPeriodo() {
        return cursoPeriodo;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    // Ley de Demeter (acceso a los IDs sin exponer la entidad completa)
    public UUID getIdCursoPeriodo() {
        return cursoPeriodo.getIdCursoPeriodo();
    }

    public UUID getIdEstudiante() {
        return estudiante.getIdEstudiante();
    }

    public Long getIdProfesor() {
        return profesor.getIdProfesor();
    }

    // Identidad
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(getIdCursoPeriodo());
        hash = 31 * hash + Objects.hashCode(getIdEstudiante());
        hash = 31 * hash + Objects.hashCode(getIdProfesor());
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
        return "MonitoriaPk{"
                + "idCursoPeriodo=" + getIdCursoPeriodo()
                + ", idEstudiante=" + getIdEstudiante()
                + ", idProfesor=" + getIdProfesor()
                + "}";
    }
}