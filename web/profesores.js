// Corre en el NAVEGADOR. Hace el trabajo de ProfesorVista (consola):
// pedir datos, llamar al controlador (via HTTP) y mostrar resultados o errores.
// La direccion del servidor y el manejo de errores viven en api.js.

const RUTA = "/api/profesores";

const formulario = document.getElementById("formProfesor");
const campoNombre = document.getElementById("nombre");
const campoCelular = document.getElementById("celular");
const tabla = document.getElementById("tabla");
const mensaje = document.getElementById("mensaje");
const vacio = document.getElementById("vacio");

// GET /api/profesores  ->  el servidor Java responde con una lista en JSON
async function cargarProfesores() {
    try {
        pintarTabla(await llamarApi(RUTA));
    } catch (e) {
        mostrarMensaje(mensaje, e.message, true);
    }
}

function pintarTabla(profesores) {
    tabla.replaceChildren();
    vacio.hidden = profesores.length > 0;

    for (const p of profesores) {
        const fila = document.createElement("tr");
        const valores = [p.id, p.nombre, p.celular, p.activo ? "Activo" : "Inactivo"];
        for (const valor of valores) {
            const celda = document.createElement("td");
            celda.textContent = valor; // textContent (no innerHTML): evita inyectar HTML
            fila.appendChild(celda);
        }
        tabla.appendChild(fila);
    }
}

// POST /api/profesores  ->  se envia un JSON, el servidor arma el DTO y lo guarda
formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault(); // evita que el formulario recargue la pagina

    const cuerpo = { nombre: campoNombre.value, celular: campoCelular.value };

    try {
        const datos = await llamarApi(RUTA, opcionesJson("POST", cuerpo));
        mostrarMensaje(mensaje, "Profesor registrado: " + datos.nombre, false);
        formulario.reset();
        await cargarProfesores();
    } catch (e) {
        // Aqui llegan los IllegalArgumentException del DTO/servicio
        mostrarMensaje(mensaje, e.message, true);
    }
});

cargarProfesores(); // al abrir la pagina, se muestra la lista
