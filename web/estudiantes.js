// Corre en el NAVEGADOR. Hace el trabajo de EstudianteVista (consola).
// En edicion solo se pueden cambiar los datos de contacto (igual que en la consola).

const RUTA = "/api/estudiantes";

const formulario = document.getElementById("formulario");
const tituloForm = document.getElementById("tituloForm");
const campoCodigo = document.getElementById("codigo");
const campoNombre = document.getElementById("nombre");
const campoCorreo = document.getElementById("correo");
const campoCelular = document.getElementById("celular");
const campoDireccion = document.getElementById("direccion");
const btnGuardar = document.getElementById("btnGuardar");
const btnCancelar = document.getElementById("btnCancelar");
const filtro = document.getElementById("filtro");
const mensaje = document.getElementById("mensaje");
const tabla = document.getElementById("tabla");
const vacio = document.getElementById("vacio");

let enEdicion = null; // el estudiante que se esta editando (null = modo "registrar")

// ---- Listar  (GET /api/estudiantes?estado=todos|activos|inactivos) ---------
async function cargarLista() {
    try {
        pintarTabla(await llamarApi(RUTA + "?estado=" + encodeURIComponent(filtro.value)));
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}
filtro.addEventListener("change", cargarLista);

function pintarTabla(estudiantes) {
    tabla.replaceChildren();
    vacio.hidden = estudiantes.length > 0;

    for (const est of estudiantes) {
        const fila = crearFila([est.codigo, est.nombre, est.correo, est.celular,
            est.direccion, est.estado.descripcion]);
        agregarAcciones(fila, [
            crearBoton("Editar", "secundario", () => iniciarEdicion(est)),
            est.activo
                ? crearBoton("Inactivar", "peligro", () => cambiarEstado(est, "inactivar"))
                : crearBoton("Activar", "secundario", () => cambiarEstado(est, "activar"))
        ]);
        tabla.appendChild(fila);
    }
}

// ---- Modo edicion ------------------------------------------------------
function iniciarEdicion(est) {
    enEdicion = est;
    campoCodigo.value = est.codigo;
    campoNombre.value = est.nombre;
    campoCorreo.value = est.correo;
    campoCelular.value = est.celular;
    campoDireccion.value = est.direccion;
    campoCodigo.disabled = true; // el codigo y el nombre no se pueden cambiar
    campoNombre.disabled = true;
    tituloForm.textContent = "Editar datos de contacto";
    btnGuardar.textContent = "Guardar cambios";
    btnCancelar.hidden = false;
    mensaje.textContent = "";
    campoCorreo.focus();
}

function terminarEdicion() {
    enEdicion = null;
    formulario.reset();
    campoCodigo.disabled = false;
    campoNombre.disabled = false;
    tituloForm.textContent = "Registrar estudiante";
    btnGuardar.textContent = "Guardar";
    btnCancelar.hidden = true;
}

btnCancelar.addEventListener("click", () => {
    terminarEdicion();
    mensaje.textContent = "";
});

// ---- Guardar: POST si es nuevo, PUT si se esta editando ------------------
formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    try {
        if (enEdicion === null) {
            const cuerpo = {
                codigo: campoCodigo.value,
                nombre: campoNombre.value,
                correo: campoCorreo.value,
                celular: campoCelular.value,
                direccion: campoDireccion.value
            };
            const creado = await llamarApi(RUTA, opcionesJson("POST", cuerpo));
            mostrarMensaje(mensaje, "Estudiante registrado: " + creado.nombre, false);
        } else {
            const cambios = {};
            if (campoCorreo.value.trim() !== enEdicion.correo) {
                cambios.correo = campoCorreo.value;
            }
            if (campoCelular.value.trim() !== enEdicion.celular) {
                cambios.celular = campoCelular.value;
            }
            if (campoDireccion.value.trim() !== enEdicion.direccion) {
                cambios.direccion = campoDireccion.value;
            }
            if (Object.keys(cambios).length === 0) {
                mostrarMensaje(mensaje, "No hay cambios para guardar", true);
                return;
            }
            await llamarApi(RUTA + "/" + enEdicion.id, opcionesJson("PUT", cambios));
            mostrarMensaje(mensaje, "Datos del estudiante actualizados", false);
        }
        terminarEdicion();
        await cargarLista();
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
});

// ---- Activar / inactivar -------------------------------------------------
async function cambiarEstado(est, accion) {
    if (accion === "inactivar" && !window.confirm("¿Inactivar al estudiante " + est.nombre + "?")) {
        return;
    }
    try {
        await llamarApi(RUTA + "/" + est.id + "/" + accion, { method: "PUT" });
        mostrarMensaje(mensaje, accion === "inactivar" ? "Estudiante inactivado" : "Estudiante activado", false);
        await cargarLista();
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

cargarLista();
