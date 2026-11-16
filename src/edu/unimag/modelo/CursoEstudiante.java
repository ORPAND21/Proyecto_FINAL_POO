package edu.unimag.modelo;

import com.cleandev.tpa.api.annotations.TpaConvert;
import com.cleandev.tpa.api.annotations.TpaId;
import edu.unimag.modelo.convertidor.EstadoInscripcionConverter;
import edu.unimag.modelo.enumeracion.EstadoInscripcion;
import edu.unimag.modelo.pk.CursoEstudiantePk;
import java.time.OffsetDateTime;
import java.util.UUID;

public class CursoEstudiante {

    @TpaId
    private CursoEstudiantePk idCursoEstudiante;
    private OffsetDateTime fechaInscripcion;
    @TpaConvert(converter = EstadoInscripcionConverter.class)
    private EstadoInscripcion estado;

    // Reflexión
    protected CursoEstudiante() {
    }

    // Creación | va la llave compuesta pues no se genera automáticamente
    public CursoEstudiante(CursoEstudiantePk idCursoEstudiante) {
        this.idCursoEstudiante = idCursoEstudiante;
        this.fechaInscripcion = OffsetDateTime.now();
        this.estado = EstadoInscripcion.PENDIENTE;
    }

    // Hidratación
    public CursoEstudiante(
            CursoEstudiantePk idCursoEstudiante,
            OffsetDateTime fechaInscripcion,
            EstadoInscripcion estado
    ) {
        this.idCursoEstudiante = idCursoEstudiante;
        this.fechaInscripcion = (fechaInscripcion != null) ? fechaInscripcion : OffsetDateTime.now();
        this.estado = (estado != null) ? estado : EstadoInscripcion.PENDIENTE;
    }

    // Métodos de comportamiento
    public void admitir() {
        if(!estaPendiente()){
            throw new IllegalArgumentException("Invalido | Solo se pueden admitir estudiantes en estado pendiente");
        }
        estado = estado.cambiarEstadoA(EstadoInscripcion.ADMITIDO);
    }

    public void rechazar() {
        estado = estado.cambiarEstadoA(EstadoInscripcion.RECHAZADO);
    }

    public void aprobar() {
        estado = estado.cambiarEstadoA(EstadoInscripcion.APROBADO);
    }

    public void reprobar() {
        estado = estado.cambiarEstadoA(EstadoInscripcion.REPROBADO);
    }

    public void retirar() {
        estado = estado.cambiarEstadoA(EstadoInscripcion.RETIRADO);
    }

    public void reAsignarCurso() {
        
        estado = estado.cambiarEstadoA(EstadoInscripcion.ADMITIDO);
    }

    // Métodos de consulta
    public boolean estaPendiente() {
        return estado == EstadoInscripcion.PENDIENTE;
    }

    public boolean estaAdmitido() {
        return estado == EstadoInscripcion.ADMITIDO;
    }

    // Getters
    public CursoEstudiantePk getIdCursoEstudiante() {
        return idCursoEstudiante;
    }

    public OffsetDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public EstadoInscripcion getEstado() {
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
