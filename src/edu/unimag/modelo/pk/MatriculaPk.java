package edu.unimag.modelo.pk;

import edu.unimag.modelo.CursoPeriodo;
import edu.unimag.modelo.Estudiante;
import edu.unimag.modelo.Materia;
import edu.unimag.modelo.PeriodoAcademico;
import edu.unimag.modelo.Profesor;
import java.util.Objects;
import java.util.UUID;

/**
 * Llave primaria compuesta de Matricula.
 *
 * A diferencia de CursoProfesorPk (2 modelos), esta PK relaciona CINCO modelos:
 * Estudiante, CursoPeriodo, Materia, PeriodoAcademico y Profesor.
 *
 * El orden de los campos es el mismo que usa TPA Blaster para reconstruir la
 * llave desde el archivo de persistencia, por lo que no debe alterarse.
 */
public class MatriculaPk {

    private final Estudiante estudiante;
    private final CursoPeriodo cursoPeriodo;
    private final Materia materia;
    private final PeriodoAcademico periodoAcademico;
    private final Profesor profesor;

    protected MatriculaPk() {
        this.estudiante = null;
        this.cursoPeriodo = null;
        this.materia = null;
        this.periodoAcademico = null;
        this.profesor = null;
    }

    public MatriculaPk(Estudiante estudiante,
            CursoPeriodo cursoPeriodo,
            Materia materia,
            PeriodoAcademico periodoAcademico,
            Profesor profesor) {
        if (estudiante == null) {
            throw new IllegalArgumentException("Estudiante es obligatorio");
        }
        if (cursoPeriodo == null) {
            throw new IllegalArgumentException("Curso periodo es obligatorio");
        }
        if (materia == null) {
            throw new IllegalArgumentException("Materia es obligatoria");
        }
        if (periodoAcademico == null) {
            throw new IllegalArgumentException("Periodo academico es obligatorio");
        }
        if (profesor == null) {
            throw new IllegalArgumentException("Profesor es obligatorio");
        }
        this.estudiante = estudiante;
        this.cursoPeriodo = cursoPeriodo;
        this.materia = materia;
        this.periodoAcademico = periodoAcademico;
        this.profesor = profesor;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public CursoPeriodo getCursoPeriodo() {
        return cursoPeriodo;
    }

    public Materia getMateria() {
        return materia;
    }

    public PeriodoAcademico getPeriodoAcademico() {
        return periodoAcademico;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    // Ley de Demeter: exponer solo los identificadores necesarios
    public UUID getIdEstudiante() {
        return (estudiante == null) ? null : estudiante.getIdEstudiante();
    }

    public UUID getIdCursoPeriodo() {
        return (cursoPeriodo == null) ? null : cursoPeriodo.getIdCursoPeriodo();
    }

    public Integer getIdMateria() {
        return (materia == null) ? null : materia.getIdMateria();
    }

    public Integer getIdPeriodoAcademico() {
        return (periodoAcademico == null) ? null : periodoAcademico.getIdPeriodoAcademico();
    }

    public Long getIdProfesor() {
        return (profesor == null) ? null : profesor.getIdProfesor();
    }

    // Identidad
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(getIdEstudiante());
        hash = 31 * hash + Objects.hashCode(getIdCursoPeriodo());
        hash = 31 * hash + Objects.hashCode(getIdMateria());
        hash = 31 * hash + Objects.hashCode(getIdPeriodoAcademico());
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
        final MatriculaPk other = (MatriculaPk) obj;
        return Objects.equals(this.getIdEstudiante(), other.getIdEstudiante())
                && Objects.equals(this.getIdCursoPeriodo(), other.getIdCursoPeriodo())
                && Objects.equals(this.getIdMateria(), other.getIdMateria())
                && Objects.equals(this.getIdPeriodoAcademico(), other.getIdPeriodoAcademico())
                && Objects.equals(this.getIdProfesor(), other.getIdProfesor());
    }

    @Override
    public String toString() {
        return "MatriculaPk{"
                + "idEstudiante=" + getIdEstudiante()
                + ", idCursoPeriodo=" + getIdCursoPeriodo()
                + ", idMateria=" + getIdMateria()
                + ", idPeriodoAcademico=" + getIdPeriodoAcademico()
                + ", idProfesor=" + getIdProfesor() + "}";
    }
}
