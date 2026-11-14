// Corre en el NAVEGADOR. Hace el trabajo de ProgramaVista (consola).
// Un programa tiene un nivel (enum con codigo) y una facultad: ambos desplegables
// se llenan con datos del servidor.

const RUTA = "/api/programas";

const formulario = document.getElementById("formulario");
const tituloForm = document.getElementById("tituloForm");
const campoNombre = document.getElementById("nombre");
const campoNivel = document.getElementById("nivel");
const campoFacultad = document.getElementById("facultad");
const btnGuardar = document.getElementById("btnGuardar");
const btnCancelar = document.getElementById("btnCancelar");
const filtro = document.getElementById("filtro");
const mensaje = document.getElementById("mensaje");
const tabla = document.getElementById("tabla");
const vacio = document.getElementById("vacio");

let enEdicion = null; // el programa que se esta editando (null = modo "registrar")

// ---- Desplegables ----------------------------------------------------------
async function cargarDesplegables() {
    try {
        const [facultades, niveles] = await Promise.all([
            llamarApi("/api/facultades"),
            llamarApi(RUTA + "/niveles")
        ]);
        llenarSelect(campoNivel, niveles, n => n.codigo, n => n.descripcion, "Seleccione un nivel");
        llenarSelect(campoFacultad, facultades, f => f.id, f => f.nombre, "Seleccione una facultad");
        llenarSelect(filtro, facultades, f => f.id, f => f.nombre, "Todas las facultades");
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

// ---- Listar  (GET /api/programas[?idFacultad=1]) --------------------------------
async function cargarLista() {
    const consulta = filtro.value ? "?idFacultad=" + encodeURIComponent(filtro.value) : "";
    try {
        pintarTabla(await llamarApi(RUTA + consulta));
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}
filtro.addEventListener("change", cargarLista);

function pintarTabla(programas) {
    tabla.replaceChildren();
    vacio.hidden = programas.length > 0;

    for (const p of programas) {
        const fila = crearFila([p.nombre, p.nivelPrograma.descripcion, p.facultad.nombre, p.estado.descripcion]);
        const activo = p.estado.nombre === "ACTIVO";
        agregarAcciones(fila, [
            crearBoton("Editar", "secundario", () => iniciarEdicion(p)),
            activo
                ? crearBoton("Suspender", "peligro", () => cambiarEstado(p, "suspender"))
                : crearBoton("Reactivar", "secundario", () => cambiarEstado(p, "reactivar"))
        ]);
        tabla.appendChild(fila);
    }
}

// ---- Modo edicion (aqui se puede cambiar todo, incluida la facultad) -----------
function iniciarEdicion(p) {
    enEdicion = p;
    campoNombre.value = p.nombre;
    campoNivel.value = p.nivelPrograma.codigo;
    campoFacultad.value = p.facultad.id;
    tituloForm.textContent = "Editar programa";
    btnGuardar.textContent = "Guardar cambios";
    btnCancelar.hidden = false;
    mensaje.textContent = "";
    campoNombre.focus();
}

function terminarEdicion() {
    enEdicion = null;
    formulario.reset();
    tituloForm.textContent = "Registrar programa";
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
                nivelPrograma: campoNivel.value, // codigo del enum (1 a 6)
                idFacultad: campoFacultad.value
            };
            const creado = await llamarApi(RUTA, opcionesJson("POST", cuerpo));
            mostrarMensaje(mensaje, "Programa registrado: " + creado.nombre, false);
        } else {
            const cambios = {};
            if (campoNombre.value.trim() !== enEdicion.nombre) {
                cambios.nuevoNombre = campoNombre.value;
            }
            if (Number(campoNivel.value) !== enEdicion.nivelPrograma.codigo) {
                cambios.nuevoNivel = campoNivel.value;
            }
            if (Number(campoFacultad.value) !== enEdicion.facultad.id) {
                cambios.nuevoIdFacultad = campoFacultad.value;
            }
            if (Object.keys(cambios).length === 0) {
                mostrarMensaje(mensaje, "No hay cambios para guardar", true);
                return;
            }
            await llamarApi(RUTA + "/" + enEdicion.id, opcionesJson("PUT", cambios));
            mostrarMensaje(mensaje, "Programa actualizado", false);
        }
        terminarEdicion();
        await cargarLista();
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
});

// ---- Suspender / reactivar (la PK del programa es un UUID) ---------------------
async function cambiarEstado(p, accion) {
    if (accion === "suspender" && !window.confirm("¿Suspender el programa " + p.nombre + "?")) {
        return;
    }
    try {
        await llamarApi(RUTA + "/" + p.id + "/" + accion, { method: "PUT" });
        mostrarMensaje(mensaje, accion === "suspender" ? "Programa suspendido" : "Programa reactivado", false);
        await cargarLista();
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

cargarDesplegables();
cargarLista();
