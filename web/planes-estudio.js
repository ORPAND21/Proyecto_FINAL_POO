// Corre en el NAVEGADOR. Hace el trabajo de PlanEstudioVista (consola).
// Un plan pertenece a un programa (cuya PK es UUID): el desplegable se llena con /api/programas.
// En edicion solo se puede cambiar la version.

const RUTA = "/api/planes-estudio";

const formulario = document.getElementById("formulario");
const tituloForm = document.getElementById("tituloForm");
const campoVersion = document.getElementById("version");
const campoPrograma = document.getElementById("programa");
const btnGuardar = document.getElementById("btnGuardar");
const btnCancelar = document.getElementById("btnCancelar");
const filtro = document.getElementById("filtro");
const mensaje = document.getElementById("mensaje");
const tabla = document.getElementById("tabla");
const vacio = document.getElementById("vacio");

let enEdicion = null; // el plan que se esta editando (null = modo "registrar")

// ---- Desplegables ----------------------------------------------------------
async function cargarProgramas() {
    try {
        const programas = await llamarApi("/api/programas");
        const texto = p => p.nombre + " (" + p.nivelPrograma.descripcion + ")";
        llenarSelect(campoPrograma, programas, p => p.id, texto, "Seleccione un programa");
        llenarSelect(filtro, programas, p => p.id, texto, "Todos los programas");
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

// ---- Listar  (GET /api/planes-estudio[?idPrograma={uuid}]) ----------------------
async function cargarLista() {
    const consulta = filtro.value ? "?idPrograma=" + encodeURIComponent(filtro.value) : "";
    try {
        pintarTabla(await llamarApi(RUTA + consulta));
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}
filtro.addEventListener("change", cargarLista);

function pintarTabla(planes) {
    tabla.replaceChildren();
    vacio.hidden = planes.length > 0;

    for (const pl of planes) {
        const fila = crearFila([pl.id, pl.version, pl.programa.nombre, pl.estado.descripcion,
            fechaCorta(pl.fechaCreacion)]);
        const activo = pl.estado.nombre === "ACTIVO";
        agregarAcciones(fila, [
            crearBoton("Editar", "secundario", () => iniciarEdicion(pl)),
            activo
                ? crearBoton("Inactivar", "peligro", () => cambiarEstado(pl, "inactivar"))
                : crearBoton("Activar", "secundario", () => cambiarEstado(pl, "activar"))
        ]);
        tabla.appendChild(fila);
    }
}

// ---- Modo edicion (el programa no se puede cambiar) ----------------------------
function iniciarEdicion(pl) {
    enEdicion = pl;
    campoVersion.value = pl.version;
    campoPrograma.value = pl.programa.id;
    campoPrograma.disabled = true;
    tituloForm.textContent = "Editar versión del plan";
    btnGuardar.textContent = "Guardar cambios";
    btnCancelar.hidden = false;
    mensaje.textContent = "";
    campoVersion.focus();
}

function terminarEdicion() {
    enEdicion = null;
    formulario.reset();
    campoPrograma.disabled = false;
    tituloForm.textContent = "Registrar plan de estudio";
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
            const cuerpo = { version: campoVersion.value, idPrograma: campoPrograma.value };
            const creado = await llamarApi(RUTA, opcionesJson("POST", cuerpo));
            mostrarMensaje(mensaje, "Plan registrado: versión " + creado.version, false);
        } else {
            if (campoVersion.value.trim() === enEdicion.version) {
                mostrarMensaje(mensaje, "No hay cambios para guardar", true);
                return;
            }
            await llamarApi(RUTA + "/" + enEdicion.id, opcionesJson("PUT", { nuevaVersion: campoVersion.value }));
            mostrarMensaje(mensaje, "Plan actualizado", false);
        }
        terminarEdicion();
        await cargarLista();
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
});

// ---- Activar / inactivar ----------------------------------------------------------
async function cambiarEstado(pl, accion) {
    if (accion === "inactivar" && !window.confirm("¿Inactivar el plan versión " + pl.version + "?")) {
        return;
    }
    try {
        await llamarApi(RUTA + "/" + pl.id + "/" + accion, { method: "PUT" });
        mostrarMensaje(mensaje, accion === "inactivar" ? "Plan inactivado" : "Plan activado", false);
        await cargarLista();
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

cargarProgramas();
cargarLista();
