package edu.unimag.modelo;

import com.cleandev.tpa.api.annotations.TpaConvert;
import com.cleandev.tpa.api.annotations.TpaId;
import edu.unimag.modelo.convertidor.EstadoEntidadConverter;
import edu.unimag.modelo.enumeracion.EstadoEntidad;
import edu.unimag.modelo.pk.MatriculaPk;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Matricula de un estudiante en un curso periodo, dictado por un profesor.
 *
 * Su llave primaria es compuesta y relaciona CINCO modelos:
 * Estudiante + CursoPeriodo + Materia + PeriodoAcademico + Profesor.
 */
public class Matricula {

    @TpaId
    private MatriculaPk idMatricula;
    private OffsetDateTime fechaMatricula;
    @TpaConvert(converter = EstadoEntidadConverter.class)
    private EstadoEntidad estadoMatricula;

    protected Matricula() {
    }

    // Creacion | la llave compuesta se construye antes de guardar
    public Matricula(MatriculaPk idMatricula) {
        if (idMatricula == null) {
            throw new IllegalArgumentException("La llave compuesta de matricula es obligatoria");
        }
        this.idMatricula = idMatricula;
        this.fechaMatricula = OffsetDateTime.now();
        this.estadoMatricula = EstadoEntidad.ACTIVO;
    }

    // Hidratacion
    public Matricula(MatriculaPk idMatricula,
            OffsetDateTime fechaMatricula,
            EstadoEntidad estadoMatricula) {
        if (idMatricula == null) {
            throw new IllegalArgumentException("La llave compuesta de matricula es obligatoria");
        }
        this.idMatricula = idMatricula;
        this.fechaMatricula = (fechaMatricula != null) ? fechaMatricula : OffsetDateTime.now();
        this.estadoMatricula = (estadoMatricula != null) ? estadoMatricula : EstadoEntidad.ACTIVO;
    }

    public void retirar() {
        estadoMatricula = estadoMatricula.cambiarEstadoA(EstadoEntidad.INACTIVO);
    }

    public void reactivar() {
        estadoMatricula = estadoMatricula.cambiarEstadoA(EstadoEntidad.ACTIVO);
    }

    public boolean estaActiva() {
        return estadoMatricula == EstadoEntidad.ACTIVO;
    }

    public MatriculaPk getIdMatricula() {
        return idMatricula;
    }

    public OffsetDateTime getFechaMatricula() {
        return fechaMatricula;
    }

    public EstadoEntidad getEstadoMatricula() {
        return estadoMatricula;
    }

    // Getters de conveniencia (Ley de Demeter)
    public UUID getIdEstudiante() {
        return (idMatricula == null) ? null : idMatricula.getIdEstudiante();
    }

    public UUID getIdCursoPeriodo() {
        return (idMatricula == null) ? null : idMatricula.getIdCursoPeriodo();
    }

    public Integer getIdMateria() {
        return (idMatricula == null) ? null : idMatricula.getIdMateria();
    }

    public Integer getIdPeriodoAcademico() {
        return (idMatricula == null) ? null : idMatricula.getIdPeriodoAcademico();
    }

    public Long getIdProfesor() {
        return (idMatricula == null) ? null : idMatricula.getIdProfesor();
    }
}
