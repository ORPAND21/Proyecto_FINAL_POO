
package edu.unimag.modelo.enumeracion;

import com.cleandev.tpa.api.converter.CodedEnum;


public enum EstadoMonitoria implements CodedEnum {
    
    ACTIVO(1, "Vigente"),
    INACTIVO(2,"Retirado");
    
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
    
    
    
}
