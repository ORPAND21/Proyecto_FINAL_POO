// Corre en el NAVEGADOR. Hace el trabajo de FormacionProfesorVista (consola).
// Una formacion pertenece a un profesor: el desplegable se llena con /api/profesores.

const RUTA = "/api/formaciones-profesores";

const formulario = document.getElementById("formulario");
const tituloForm = document.getElementById("tituloForm");
const campoProfesor = document.getElementById("profesor");
const campoTitulo = document.getElementById("titulo");
const campoFecha = document.getElementById("fecha");
const btnGuardar = document.getElementById("btnGuardar");
const btnCancelar = document.getElementById("btnCancelar");
const filtro = document.getElementById("filtro");
const mensaje = document.getElementById("mensaje");
const tabla = document.getElementById("tabla");
const vacio = document.getElementById("vacio");

let enEdicion = null; // la formacion que se esta editando (null = modo "registrar")

// ---- Desplegables: se piden los profesores al servidor ----------------------
async function cargarProfesores() {
    try {
        const profesores = await llamarApi("/api/profesores");
        llenarSelect(campoProfesor, profesores, p => p.id, p => p.nombre, "Seleccione un profesor");
        llenarSelect(filtro, profesores, p => p.id, p => p.nombre, "Todos los profesores");
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

// ---- Listar  (GET /api/formaciones-profesores[?idProfesor=1]) ---------------
async function cargarLista() {
    const consulta = filtro.value ? "?idProfesor=" + encodeURIComponent(filtro.value) : "";
    try {
        pintarTabla(await llamarApi(RUTA + consulta));
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}
filtro.addEventListener("change", cargarLista);

function pintarTabla(formaciones) {
    tabla.replaceChildren();
    vacio.hidden = formaciones.length > 0;

    for (const f of formaciones) {
        const fila = crearFila([f.id, f.profesor.nombre, f.titulo, fechaCorta(f.fecha)]);
        agregarAcciones(fila, [
            crearBoton("Editar", "secundario", () => iniciarEdicion(f)),
            crearBoton("Eliminar", "peligro", () => eliminar(f))
        ]);
        tabla.appendChild(fila);
    }
}

// ---- Modo edicion (el profesor no se puede cambiar) ---------------------------
function iniciarEdicion(f) {
    enEdicion = f;
    campoProfesor.value = f.profesor.id;
    campoProfesor.disabled = true;
    campoTitulo.value = f.titulo;
    campoFecha.value = fechaCorta(f.fecha);
    tituloForm.textContent = "Editar formación";
    btnGuardar.textContent = "Guardar cambios";
    btnCancelar.hidden = false;
    mensaje.textContent = "";
    campoTitulo.focus();
}

function terminarEdicion() {
    enEdicion = null;
    formulario.reset();
    campoProfesor.disabled = false;
    tituloForm.textContent = "Registrar formación";
    btnGuardar.textContent = "Guardar";
    btnCancelar.hidden = true;
}

btnCancelar.addEventListener("click", () => {
    terminarEdicion();
    mensaje.textContent = "";
});

// ---- Guardar: POST si es nueva, PUT si se esta editando ------------------------
formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    try {
        if (enEdicion === null) {
            const cuerpo = {
                idProfesor: campoProfesor.value,
                titulo: campoTitulo.value,
                fecha: campoFecha.value // "aaaa-mm-dd"
            };
            const creada = await llamarApi(RUTA, opcionesJson("POST", cuerpo));
            mostrarMensaje(mensaje, "Formación registrada: " + creada.titulo, false);
        } else {
            const cambios = {};
            if (campoTitulo.value.trim() !== enEdicion.titulo) {
                cambios.nuevoTitulo = campoTitulo.value;
            }
            if (campoFecha.value !== fechaCorta(enEdicion.fecha)) {
                cambios.nuevaFecha = campoFecha.value;
            }
            if (Object.keys(cambios).length === 0) {
                mostrarMensaje(mensaje, "No hay cambios para guardar", true);
                return;
            }
            await llamarApi(RUTA + "/" + enEdicion.id, opcionesJson("PUT", cambios));
            mostrarMensaje(mensaje, "Formación actualizada", false);
        }
        terminarEdicion();
        await cargarLista();
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
});

// ---- Eliminar  (DELETE /api/formaciones-profesores/{id}) -----------------------
async function eliminar(f) {
    if (!window.confirm("¿Eliminar la formación “" + f.titulo + "” de " + f.profesor.nombre + "?")) {
        return;
    }
    try {
        await llamarApi(RUTA + "/" + f.id, { method: "DELETE" });
        mostrarMensaje(mensaje, "Formación eliminada", false);
        if (enEdicion !== null && enEdicion.id === f.id) {
            terminarEdicion();
        }
        await cargarLista();
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

cargarProfesores();
cargarLista();
