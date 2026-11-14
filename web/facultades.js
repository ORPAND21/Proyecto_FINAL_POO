// Corre en el NAVEGADOR. Hace el trabajo de FacultadVista (consola):
// pedir datos, llamar al controlador (via HTTP) y mostrar resultados o errores.
// El esqueleto es el mismo en todas las paginas de modulo.

const RUTA = "/api/facultades";

const formulario = document.getElementById("formulario");
const tituloForm = document.getElementById("tituloForm");
const campoNombre = document.getElementById("nombre");
const btnGuardar = document.getElementById("btnGuardar");
const btnCancelar = document.getElementById("btnCancelar");
const mensaje = document.getElementById("mensaje");
const tabla = document.getElementById("tabla");
const vacio = document.getElementById("vacio");

let enEdicion = null; // la facultad que se esta editando (null = modo "registrar")

// ---- Listar  (GET /api/facultades) ----------------------------------
async function cargarLista() {
    try {
        pintarTabla(await llamarApi(RUTA));
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

function pintarTabla(facultades) {
    tabla.replaceChildren();
    vacio.hidden = facultades.length > 0;

    for (const f of facultades) {
        const fila = crearFila([f.id, f.nombre, f.estado.descripcion]);
        const activa = f.estado.nombre === "ACTIVO";
        agregarAcciones(fila, [
            crearBoton("Editar", "secundario", () => iniciarEdicion(f)),
            activa
                ? crearBoton("Suspender", "peligro", () => cambiarEstado(f, "suspender"))
                : crearBoton("Reactivar", "secundario", () => cambiarEstado(f, "reactivar"))
        ]);
        tabla.appendChild(fila);
    }
}

// ---- Modo edicion: el mismo formulario sirve para registrar y para editar ----
function iniciarEdicion(f) {
    enEdicion = f;
    campoNombre.value = f.nombre;
    tituloForm.textContent = "Editar facultad";
    btnGuardar.textContent = "Guardar cambios";
    btnCancelar.hidden = false;
    mensaje.textContent = "";
    campoNombre.focus();
}

function terminarEdicion() {
    enEdicion = null;
    formulario.reset();
    tituloForm.textContent = "Registrar facultad";
    btnGuardar.textContent = "Guardar";
    btnCancelar.hidden = true;
}

btnCancelar.addEventListener("click", () => {
    terminarEdicion();
    mensaje.textContent = "";
});

// ---- Guardar: POST si es nueva, PUT si se esta editando ----------------
formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault(); // evita que el formulario recargue la pagina

    try {
        if (enEdicion === null) {
            const creada = await llamarApi(RUTA, opcionesJson("POST", { nombre: campoNombre.value }));
            mostrarMensaje(mensaje, "Facultad registrada: " + creada.nombre, false);
        } else {
            // Solo se envia lo que cambio (como el [Enter] para no cambiar de la consola)
            const cambios = {};
            if (campoNombre.value.trim() !== enEdicion.nombre) {
                cambios.nuevoNombre = campoNombre.value;
            }
            if (Object.keys(cambios).length === 0) {
                mostrarMensaje(mensaje, "No hay cambios para guardar", true);
                return;
            }
            await llamarApi(RUTA + "/" + enEdicion.id, opcionesJson("PUT", cambios));
            mostrarMensaje(mensaje, "Facultad actualizada", false);
        }
        terminarEdicion();
        await cargarLista();
    } catch (e) {
        // Aqui llegan los IllegalArgumentException / IllegalStateException de Java
        mostrarMensaje(mensaje, e.message, true);
    }
});

// ---- Suspender / reactivar (PUT /api/facultades/{id}/suspender) -----------
async function cambiarEstado(f, accion) {
    if (accion === "suspender" && !window.confirm("¿Suspender la facultad " + f.nombre + "?")) {
        return;
    }
    try {
        await llamarApi(RUTA + "/" + f.id + "/" + accion, { method: "PUT" });
        mostrarMensaje(mensaje, accion === "suspender" ? "Facultad suspendida" : "Facultad reactivada", false);
        await cargarLista();
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

cargarLista(); // al abrir la pagina, se muestra la lista
