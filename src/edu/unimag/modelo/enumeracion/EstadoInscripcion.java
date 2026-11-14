package edu.unimag.modelo.enumeracion;

import com.cleandev.tpa.api.converter.CodedEnum;

public enum EstadoInscripcion implements CodedEnum {

    PENDIENTE(1, "Pendiente"),
    ADMITIDO(2, "Admitido"),
    RECHAZADO(3, "Rechazado"),
    APROBADO(4, "Aprobado"),
    REPROBADO(5, "Reprobado"),
    RETIRADO(6, "Retirado");

    private final int code;
    private final String description;

    private EstadoInscripcion(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    // PENDIENTE -> ADMITIDO | RECHAZADO | RETIRADO
    // ADMITIDO  -> APROBADO | REPROBADO | RETIRADO
    // RETIRADO  -> ADMITIDO (reasignar curso)
    // RECHAZADO, APROBADO y REPROBADO son estados finales
    public EstadoInscripcion cambiarEstadoA(EstadoInscripcion nuevoEstado) {
        if (this == PENDIENTE && nuevoEstado == ADMITIDO) {
            return nuevoEstado;
        }
        if (this == PENDIENTE && nuevoEstado == RECHAZADO) {
            return nuevoEstado;
        }
        if (this == PENDIENTE && nuevoEstado == RETIRADO) {
            return nuevoEstado;
        }
        if (this == ADMITIDO && nuevoEstado == APROBADO) {
            return nuevoEstado;
        }
        if (this == ADMITIDO && nuevoEstado == REPROBADO) {
            return nuevoEstado;
        }
        if (this == ADMITIDO && nuevoEstado == RETIRADO) {
            return nuevoEstado;
        }
        if (this == RETIRADO && nuevoEstado == ADMITIDO) {
            return nuevoEstado;
        }
        throw new IllegalStateException(
                "Transicion invalida: " + this + " → " + nuevoEstado
        );
    }
}
