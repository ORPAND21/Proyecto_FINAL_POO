
package edu.unimag.modelo.enumeracion;

import com.cleandev.tpa.api.converter.CodedEnum;


public enum EstadoMonitoria implements CodedEnum {
    
    PENDIENTE(1, "Pendiente"),
    APROBADO(2,"Aprobado"),
    REPROBADO(3,"Reprobado");
    
    private final int code;
    private final String description;

    private EstadoMonitoria(int code, String description) {
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
    public EstadoMonitoria desdeCalificacion (EstadoMonitoria calificacion){
        
    }
    
    
    
}
