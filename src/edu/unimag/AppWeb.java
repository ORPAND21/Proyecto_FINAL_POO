package edu.unimag;

import edu.unimag.config.ConfiguracionDependenciasWeb;
import edu.unimag.vista.web.CursoPeriodoWeb;
import edu.unimag.vista.web.CursoProfesorWeb;
import edu.unimag.vista.web.EstudianteWeb;
import edu.unimag.vista.web.FacultadWeb;
import edu.unimag.vista.web.FormacionProfesorWeb;
import edu.unimag.vista.web.MateriaWeb;
import edu.unimag.vista.web.MatriculaWeb;
import edu.unimag.vista.web.PeriodoAcademicoWeb;
import edu.unimag.vista.web.PlanEstudioWeb;
import edu.unimag.vista.web.ProfesorWeb;
import edu.unimag.vista.web.ProgramaWeb;
import edu.unimag.vista.web.ServidorWeb;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Punto de arranque UNICO de la aplicacion.
 *
 * Toda la interfaz grafica se sirve como archivos web y se comunica con los
 * controladores a traves de la API HTTP.
 */
public class AppWeb {

    private static final int PUERTO = 8080;

    public static void main(String[] args) {
        try {
            iniciarAplicacion();
        } catch (Exception e) {
            System.err.println("ERROR: No se puede iniciar el servidor web");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void iniciarAplicacion() throws IOException {
        // 1. Raiz de composicion: aqui se crean y conectan todas las capas
        ConfiguracionDependenciasWeb config = new ConfiguracionDependenciasWeb();
        try {
            // 2. Una "vista web" por entidad, cada una con SU controlador
            ServidorWeb servidor = new ServidorWeb(
                    PUERTO,
                    Paths.get(System.getProperty("user.dir"), "web"),
                    List.of(
                            new ProfesorWeb(config.getProfesorControlador()),
                            new EstudianteWeb(config.getEstudianteControlador()),
                            new FacultadWeb(config.getFacultadControlador()),
                            new MateriaWeb(config.getMateriaControlador()),
                            new PeriodoAcademicoWeb(config.getPeriodoAcademicoControlador()),
                            new FormacionProfesorWeb(config.getFormacionProfesorControlador()),
                            new ProgramaWeb(config.getProgramaControlador()),
                            new CursoPeriodoWeb(config.getCursoPeriodoControlador()),
                            new PlanEstudioWeb(config.getPlanEstudioControlador()),
                            new CursoProfesorWeb(config.getCursoProfesorControlador()),
                            new MatriculaWeb(config.getMatriculaControlador())));
            servidor.iniciar();

            // 3. El servidor NO bloquea, por eso los recursos se liberan con un
            //    "shutdown hook" cuando el programa termina.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                servidor.detener();
                config.close();
            }));

            System.out.println("Servidor listo. Abre en el navegador: http://localhost:" + PUERTO);
            System.out.println("Para detenerlo: boton rojo (Stop) de NetBeans.");
        } catch (IOException | RuntimeException e) {
            config.close();
            throw e;
        }
    }
}
