// Corre en el NAVEGADOR. Hace el trabajo de CursoPeriodoVista (consola).
// Un curso por periodo es una materia dictada en un periodo: ambos desplegables
// se llenan con datos del servidor. En edicion solo se cambian el nombre y el cupo.

const RUTA = "/api/cursos-periodos";

const formulario = document.getElementById("formulario");
const tituloForm = document.getElementById("tituloForm");
const campoNombre = document.getElementById("nombre");
const campoMateria = document.getElementById("materia");
const campoPeriodo = document.getElementById("periodo");
const campoCupo = document.getElementById("cupo");
const btnGuardar = document.getElementById("btnGuardar");
const btnCancelar = document.getElementById("btnCancelar");
const mensaje = document.getElementById("mensaje");
const tabla = document.getElementById("tabla");
const vacio = document.getElementById("vacio");

let enEdicion = null; // el curso que se esta editando (null = modo "registrar")

// ---- Desplegables ----------------------------------------------------------
async function cargarDesplegables() {
    try {
        const [materias, periodos] = await Promise.all([
            llamarApi("/api/materias"),
            llamarApi("/api/periodos-academicos")
        ]);
        llenarSelect(campoMateria, materias, m => m.id, m => m.nombre, "Seleccione una materia");
        llenarSelect(campoPeriodo, periodos, p => p.id, p => p.nombre, "Seleccione un periodo");
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

// ---- Listar  (GET /api/cursos-periodos) ---------------------------------------
async function cargarLista() {
    try {
        pintarTabla(await llamarApi(RUTA));
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

function pintarTabla(cursos) {
    tabla.replaceChildren();
    vacio.hidden = cursos.length > 0;

    for (const c of cursos) {
        const fila = crearFila([c.nombre, c.materia.nombre, c.periodo.nombre, c.cupo]);
        agregarAcciones(fila, [
            crearBoton("Editar", "secundario", () => iniciarEdicion(c)),
            crearBoton("Eliminar", "peligro", () => eliminar(c))
        ]);
        tabla.appendChild(fila);
    }
}

// ---- Modo edicion (la materia y el periodo no se pueden cambiar) ---------------
function iniciarEdicion(c) {
    enEdicion = c;
    campoNombre.value = c.nombre;
    campoMateria.value = c.materia.id;
    campoPeriodo.value = c.periodo.id;
    campoMateria.disabled = true;
    campoPeriodo.disabled = true;
    campoCupo.value = c.cupo;
    tituloForm.textContent = "Editar curso";
    btnGuardar.textContent = "Guardar cambios";
    btnCancelar.hidden = false;
    mensaje.textContent = "";
    campoNombre.focus();
}

function terminarEdicion() {
    enEdicion = null;
    formulario.reset();
    campoMateria.disabled = false;
    campoPeriodo.disabled = false;
    tituloForm.textContent = "Registrar curso";
    btnGuardar.textContent = "Guardar";
    btnCancelar.hidden = true;
}

btnCancelar.addEventListener("click", () => {
    terminarEdicion();
    mensaje.textContent = "";
});

// ---- Guardar: POST si es nuevo, PUT si se esta editando ------------------------
formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    try {
        if (enEdicion === null) {
            const cuerpo = {
                nombre: campoNombre.value,
                idMateria: campoMateria.value,
                idPeriodoAcademico: campoPeriodo.value,
                cupo: campoCupo.value
            };
            const creado = await llamarApi(RUTA, opcionesJson("POST", cuerpo));
            mostrarMensaje(mensaje, "Curso registrado: " + creado.nombre, false);
        } else {
            const cambios = {};
            if (campoNombre.value.trim() !== enEdicion.nombre) {
                cambios.nuevoNombre = campoNombre.value;
            }
            if (Number(campoCupo.value) !== enEdicion.cupo) {
                cambios.nuevoCupo = campoCupo.value;
            }
            if (Object.keys(cambios).length === 0) {
                mostrarMensaje(mensaje, "No hay cambios para guardar", true);
                return;
            }
            await llamarApi(RUTA + "/" + enEdicion.id, opcionesJson("PUT", cambios));
            mostrarMensaje(mensaje, "Curso actualizado", false);
        }
        terminarEdicion();
        await cargarLista();
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
});

// ---- Eliminar  (DELETE /api/cursos-periodos/{uuid}) ------------------------------
async function eliminar(c) {
    if (!window.confirm("¿Eliminar el curso " + c.nombre + " (" + c.materia.nombre + ", " + c.periodo.nombre + ")?")) {
        return;
    }
    try {
        await llamarApi(RUTA + "/" + c.id, { method: "DELETE" });
        mostrarMensaje(mensaje, "Curso eliminado", false);
        if (enEdicion !== null && enEdicion.id === c.id) {
            terminarEdicion();
        }
        await cargarLista();
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

cargarDesplegables();
cargarLista();
