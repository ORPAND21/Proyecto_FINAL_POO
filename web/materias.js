// Corre en el NAVEGADOR. Hace el trabajo de MateriaVista (consola).

const RUTA = "/api/materias";

const formulario = document.getElementById("formulario");
const tituloForm = document.getElementById("tituloForm");
const campoNombre = document.getElementById("nombre");
const campoCreditos = document.getElementById("creditos");
const campoCupo = document.getElementById("cupo");
const btnGuardar = document.getElementById("btnGuardar");
const btnCancelar = document.getElementById("btnCancelar");
const mensaje = document.getElementById("mensaje");
const tabla = document.getElementById("tabla");
const vacio = document.getElementById("vacio");

let enEdicion = null; // la materia que se esta editando (null = modo "registrar")

// ---- Listar  (GET /api/materias) ------------------------------------
async function cargarLista() {
    try {
        pintarTabla(await llamarApi(RUTA));
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

function pintarTabla(materias) {
    tabla.replaceChildren();
    vacio.hidden = materias.length > 0;

    for (const m of materias) {
        const fila = crearFila([m.id, m.nombre, m.creditos, m.cupoMaximo, m.estado.descripcion]);
        const activa = m.estado.nombre === "ACTIVO";
        agregarAcciones(fila, [
            crearBoton("Editar", "secundario", () => iniciarEdicion(m)),
            activa
                ? crearBoton("Suspender", "peligro", () => cambiarEstado(m, "suspender"))
                : crearBoton("Reactivar", "secundario", () => cambiarEstado(m, "reactivar"))
        ]);
        tabla.appendChild(fila);
    }
}

// ---- Modo edicion -----------------------------------------------------
function iniciarEdicion(m) {
    enEdicion = m;
    campoNombre.value = m.nombre;
    campoCreditos.value = m.creditos;
    campoCupo.value = m.cupoMaximo;
    tituloForm.textContent = "Editar materia";
    btnGuardar.textContent = "Guardar cambios";
    btnCancelar.hidden = false;
    mensaje.textContent = "";
    campoNombre.focus();
}

function terminarEdicion() {
    enEdicion = null;
    formulario.reset();
    tituloForm.textContent = "Registrar materia";
    btnGuardar.textContent = "Guardar";
    btnCancelar.hidden = true;
}

btnCancelar.addEventListener("click", () => {
    terminarEdicion();
    mensaje.textContent = "";
});

// ---- Guardar: POST si es nueva, PUT si se esta editando ----------------
formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    try {
        if (enEdicion === null) {
            const cuerpo = {
                nombre: campoNombre.value,
                creditos: campoCreditos.value,
                cupoMaximo: campoCupo.value
            };
            const creada = await llamarApi(RUTA, opcionesJson("POST", cuerpo));
            mostrarMensaje(mensaje, "Materia registrada: " + creada.nombre, false);
        } else {
            // Solo se envia lo que cambio
            const cambios = {};
            if (campoNombre.value.trim() !== enEdicion.nombre) {
                cambios.nuevoNombre = campoNombre.value;
            }
            if (Number(campoCreditos.value) !== enEdicion.creditos) {
                cambios.nuevosCreditos = campoCreditos.value;
            }
            if (Number(campoCupo.value) !== enEdicion.cupoMaximo) {
                cambios.nuevoCupo = campoCupo.value;
            }
            if (Object.keys(cambios).length === 0) {
                mostrarMensaje(mensaje, "No hay cambios para guardar", true);
                return;
            }
            await llamarApi(RUTA + "/" + enEdicion.id, opcionesJson("PUT", cambios));
            mostrarMensaje(mensaje, "Materia actualizada", false);
        }
        terminarEdicion();
        await cargarLista();
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
});

// ---- Suspender / reactivar -----------------------------------------------
async function cambiarEstado(m, accion) {
    if (accion === "suspender" && !window.confirm("¿Suspender la materia " + m.nombre + "?")) {
        return;
    }
    try {
        await llamarApi(RUTA + "/" + m.id + "/" + accion, { method: "PUT" });
        mostrarMensaje(mensaje, accion === "suspender" ? "Materia suspendida" : "Materia reactivada", false);
        await cargarLista();
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

cargarLista();
