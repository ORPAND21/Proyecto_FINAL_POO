// Funciones compartidas por todas las paginas. Se carga ANTES que el JS de cada pagina.
// Si algun dia cambia el puerto o la direccion del servidor Java, se cambia SOLO aqui.

const API_BASE = "http://localhost:8080";

// Llama al servidor Java y devuelve los datos ya convertidos de JSON.
// Si algo falla, lanza un Error con un mensaje listo para mostrar al usuario:
//   - sin conexion        -> "No se pudo conectar con el servidor"
//   - error de validacion -> el mensaje que mando Java (los IllegalArgumentException de tus DTOs/servicios)
async function llamarApi(ruta, opciones = {}) {
    let respuesta;
    try {
        respuesta = await fetch(API_BASE + ruta, opciones);
    } catch (e) {
        throw new Error("No se pudo conectar con el servidor");
    }

    let datos = null;
    try {
        datos = await respuesta.json();
    } catch (e) {
        // la respuesta no traia JSON
    }

    if (!respuesta.ok) {
        throw new Error(datos && datos.error ? datos.error : "Error " + respuesta.status);
    }
    return datos;
}

// Arma las opciones de fetch para enviar un cuerpo JSON (POST, PUT).
function opcionesJson(metodo, cuerpo) {
    return {
        method: metodo,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(cuerpo)
    };
}

function mostrarMensaje(elemento, texto, esError) {
    elemento.textContent = texto;
    elemento.className = esError ? "error" : "ok";
}

// ---------------------------------------------------------------------
// Ayudas para construir pantallas (las usan TODAS las paginas)
// ---------------------------------------------------------------------

// Llena un <select>. valor(item) = lo que se envia al servidor; texto(item) = lo que ve el usuario.
// Si se da "placeholder", se agrega primero una opcion vacia (sirve de "todos" o "seleccione...").
function llenarSelect(select, items, valor, texto, placeholder) {
    select.replaceChildren();
    if (placeholder) {
        const opcion = document.createElement("option");
        opcion.value = "";
        opcion.textContent = placeholder;
        select.appendChild(opcion);
    }
    for (const item of items) {
        const opcion = document.createElement("option");
        opcion.value = valor(item);
        opcion.textContent = texto(item); // textContent (no innerHTML): evita inyectar HTML
        select.appendChild(opcion);
    }
}

function crearBoton(texto, clase, accion) {
    const boton = document.createElement("button");
    boton.type = "button";
    boton.className = clase;
    boton.textContent = texto;
    boton.addEventListener("click", accion);
    return boton;
}

// Fila de tabla con una celda de texto por cada valor.
function crearFila(valores) {
    const fila = document.createElement("tr");
    for (const valor of valores) {
        const celda = document.createElement("td");
        celda.textContent = valor;
        fila.appendChild(celda);
    }
    return fila;
}

// Agrega al final de la fila una celda con botones.
function agregarAcciones(fila, botones) {
    const celda = document.createElement("td");
    celda.className = "acciones";
    for (const boton of botones) {
        celda.appendChild(boton);
    }
    fila.appendChild(celda);
}

// El servidor envia fechas como "2024-01-15T00:00Z"; para mostrar solo "2024-01-15".
function fechaCorta(iso) {
    return String(iso).slice(0, 10);
}
