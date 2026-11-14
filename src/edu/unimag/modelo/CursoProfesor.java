
package edu.unimag.modelo;

import com.cleandev.tpa.api.annotations.TpaConvert;
import com.cleandev.tpa.api.annotations.TpaId;
import edu.unimag.modelo.convertidor.EstadoProfesorCursoConverter;
import edu.unimag.modelo.convertidor.TipoAsignacionConverter;
import edu.unimag.modelo.enumeracion.EstadoProfesorCurso;
import edu.unimag.modelo.enumeracion.TipoAsignacion;
import edu.unimag.modelo.pk.CursoProfesorPk;
import java.time.OffsetDateTime;
import java.util.UUID;


public class CursoProfesor {
    
    @TpaId
    private CursoProfesorPk idCursoProfesorPeriodo;
    private OffsetDateTime fechaAsignacion;
    @TpaConvert (converter = TipoAsignacionConverter.class)
    private TipoAsignacion tipoAsignacion;
    @TpaConvert(converter = EstadoProfesorCursoConverter.class)
    private EstadoProfesorCurso estado;

    protected CursoProfesor() {
    }
    //creacion | va la llave compuesta pues no se genera automaticamente
    public CursoProfesor(CursoProfesorPk idCursoProfesorPeriodo, TipoAsignacion tipoAsignacion) {
        this.idCursoProfesorPeriodo = idCursoProfesorPeriodo;
        this.tipoAsignacion = tipoAsignacion;
        fechaAsignacion = OffsetDateTime.now();
        estado = EstadoProfesorCurso.ACTIVO;
    }
    //hidratacion

    public CursoProfesor(CursoProfesorPk idCursoProfesorPeriodo, OffsetDateTime fechaAsignacion, TipoAsignacion tipoAsignacion, EstadoProfesorCurso estado) {
        this.idCursoProfesorPeriodo = idCursoProfesorPeriodo;
        this.fechaAsignacion = (fechaAsignacion != null)? fechaAsignacion: OffsetDateTime.now();
        this.tipoAsignacion = tipoAsignacion;
        this.estado = (estado == null)? EstadoProfesorCurso.ACTIVO: estado  ;
    }
    
    public void cambiarTipoAsignacion(TipoAsignacion nuevo){
        if(nuevo == tipoAsignacion){
            throw new IllegalArgumentException("Son el mismo tipo");
        }
        tipoAsignacion = nuevo;
        
        
    }
    public void retirar (){
        estado = estado.cambiarEstado(EstadoProfesorCurso.RETIRADO);
    }
    public void reactivar (){
        estado = estado.cambiarEstado(EstadoProfesorCurso.ACTIVO);
    }
    public boolean estaActivo(){
        return estado == EstadoProfesorCurso.ACTIVO;
    }

    public CursoProfesorPk getIdCursoProfesorPeriodo() {
        return idCursoProfesorPeriodo;
    }

    public OffsetDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public TipoAsignacion getTipoAsignacion() {
        return tipoAsignacion;
    }

    public EstadoProfesorCurso getEstado() {
        return estado;
    }
    
    public Long getIdProfesor (){
        return (idCursoProfesorPeriodo == null) ? null : idCursoProfesorPeriodo.getIdProfesor();
    }
    public UUID getIdCursoPeriodo (){
        return (idCursoProfesorPeriodo == null) ? null : idCursoProfesorPeriodo.getIdCursoPeriodo();
    }

    
    
    
    
    
}
