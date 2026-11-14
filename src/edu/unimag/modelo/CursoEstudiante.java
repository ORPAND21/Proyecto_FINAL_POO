package edu.unimag.modelo;

import com.cleandev.tpa.api.annotations.TpaConvert;
import com.cleandev.tpa.api.annotations.TpaId;
import edu.unimag.modelo.convertidor.EstadoEntidadConverter;
import edu.unimag.modelo.enumeracion.EstadoEntidad;
import edu.unimag.modelo.pk.CursoEstudiantePk;
import java.time.OffsetDateTime;
import java.util.UUID;

public class CursoEstudiante {

    @TpaId
    private CursoEstudiantePk idCursoEstudiante;
    private OffsetDateTime fechaInscripcion;
    @TpaConvert(converter = EstadoEntidadConverter.class)
    private EstadoEntidad estado;

    // Reflexión
    protected CursoEstudiante() {
    }

    // Creación | va la llave compuesta pues no se genera automáticamente
    public CursoEstudiante(CursoEstudiantePk idCursoEstudiante) {
        this.idCursoEstudiante = idCursoEstudiante;
        this.fechaInscripcion = OffsetDateTime.now();
        this.estado = EstadoEntidad.ACTIVO;
    }

    // Hidratación
    public CursoEstudiante(
            CursoEstudiantePk idCursoEstudiante,
            OffsetDateTime fechaInscripcion,
            EstadoEntidad estado
    ) {
        this.idCursoEstudiante = idCursoEstudiante;
        this.fechaInscripcion = (fechaInscripcion != null) ? fechaInscripcion : OffsetDateTime.now();
        this.estado = (estado != null) ? estado : EstadoEntidad.ACTIVO;
    }

    // Métodos de comportamiento
    public void retirar() {
        estado = estado.cambiarEstadoA(EstadoEntidad.INACTIVO);
    }

    public void reactivar() {
        estado = estado.cambiarEstadoA(EstadoEntidad.ACTIVO);
    }

    public boolean estaActivo() {
        return estado == EstadoEntidad.ACTIVO;
    }

    // Getters
    public CursoEstudiantePk getIdCursoEstudiante() {
        return idCursoEstudiante;
    }

    public OffsetDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public EstadoEntidad getEstado() {
        return estado;
    }

    // Getters de conveniencia (Ley de Demeter)
    public UUID getIdCursoPeriodo() {
        return (idCursoEstudiante == null) ? null : idCursoEstudiante.getIdCursoPeriodo();
    }

    public UUID getIdEstudiante() {
        return (idCursoEstudiante == null) ? null : idCursoEstudiante.getIdEstudiante();
    }


}
