// Corre en el NAVEGADOR. Hace el mismo trabajo que CursoProfesorVista (consola):
// pedir datos, llamar al controlador (via HTTP) y mostrar resultados o errores.

const RUTA = "/api/cursos-profesores";

const formAsignar = document.getElementById("formAsignar");
const selProfesor = document.getElementById("selProfesor");
const selCurso = document.getElementById("selCurso");
const selTipo = document.getElementById("selTipo");
const msgAsignar = document.getElementById("msgAsignar");

const formFiltro = document.getElementById("formFiltro");
const filtroTipo = document.getElementById("filtroTipo");
const filtroProfesor = document.getElementById("filtroProfesor");
const filtroMateria = document.getElementById("filtroMateria");
const filtroPeriodo = document.getElementById("filtroPeriodo");
const grupoProfesor = document.getElementById("grupoProfesor");
const grupoMateria = document.getElementById("grupoMateria");
const grupoPeriodo = document.getElementById("grupoPeriodo");
const msgLista = document.getElementById("msgLista");

const tabla = document.getElementById("tabla");
const vacio = document.getElementById("vacio");

let tipos = [];               // tipos de asignacion (vienen del servidor)
let ultimaConsulta = null;    // la consulta actual, para refrescar la tabla tras cada accion

// ---------------------------------------------------------------------
// Desplegables
// ---------------------------------------------------------------------
function unicos(items, clave) {
    const mapa = new Map();
    for (const item of items) {
        if (!mapa.has(clave(item))) {
            mapa.set(clave(item), item);
        }
    }
    return [...mapa.values()];
}

// Al abrir la pagina se piden 3 listas al servidor para llenar los desplegables.
// (En la consola el usuario tenia que digitar el ID y el UUID a mano.)
async function iniciar() {
    try {
        const [profesores, cursos, tiposApi] = await Promise.all([
            llamarApi("/api/profesores"),
            llamarApi("/api/cursos-periodos"),
            llamarApi(RUTA + "/tipos-asignacion")
        ]);
        tipos = tiposApi;

        llenarSelect(selProfesor, profesores, p => p.id, p => p.nombre, "Seleccione un profesor");
        llenarSelect(selCurso, cursos, c => c.id,
                c => c.nombre + " — " + c.materia.nombre + " (" + c.periodo.nombre + ")",
                "Seleccione un curso");
        llenarSelect(selTipo, tipos, t => t.codigo, t => t.descripcion, "Seleccione un tipo");

        // Materias y periodos para filtrar: se sacan de los cursos existentes
        llenarSelect(filtroProfesor, profesores, p => p.id, p => p.nombre, "Seleccione un profesor");
        llenarSelect(filtroMateria, unicos(cursos.map(c => c.materia), m => m.id),
                m => m.id, m => m.nombre, "Seleccione una materia");
        llenarSelect(filtroPeriodo, unicos(cursos.map(c => c.periodo), p => p.id),
                p => p.id, p => p.nombre, "Seleccione un periodo");
    } catch (e) {
        mostrarMensaje(msgAsignar, e.message, true);
    }
}

// ---------------------------------------------------------------------
// Asignar  (POST /api/cursos-profesores)
// ---------------------------------------------------------------------
formAsignar.addEventListener("submit", async (evento) => {
    evento.preventDefault(); // evita que el formulario recargue la pagina

    const cuerpo = {
        idCursoPeriodo: selCurso.value,
        idProfesor: selProfesor.value,
        tipoAsignacion: Number(selTipo.value)
    };

    try {
        const r = await llamarApi(RUTA, opcionesJson("POST", cuerpo));
        mostrarMensaje(msgAsignar,
                "Asignado: " + r.profesor.nombre + " → " + r.cursoPeriodoDto.nombre
                + " (" + r.tipoAsignacion.descripcion + ")", false);
        await cargarLista();
    } catch (e) {
        // Aqui llega, por ejemplo, "Ya existe la llave..." cuando la pareja curso-profesor esta repetida
        mostrarMensaje(msgAsignar, e.message, true);
    }
});

// ---------------------------------------------------------------------
// Consultar  (GET /api/cursos-profesores?idProfesor=...)
// ---------------------------------------------------------------------
function actualizarCamposFiltro() {
    const modo = filtroTipo.value;
    grupoProfesor.hidden = modo !== "profesor";
    grupoMateria.hidden = modo !== "materia" && modo !== "materiaPeriodo";
    grupoPeriodo.hidden = modo !== "periodo" && modo !== "materiaPeriodo";
}
filtroTipo.addEventListener("change", actualizarCamposFiltro);

function construirConsulta() {
    const modo = filtroTipo.value;
    const partes = [];
    if (modo === "profesor") {
        partes.push(["idProfesor", filtroProfesor.value]);
    }
    if (modo === "materia" || modo === "materiaPeriodo") {
        partes.push(["idMateria", filtroMateria.value]);
    }
    if (modo === "periodo" || modo === "materiaPeriodo") {
        partes.push(["idPeriodo", filtroPeriodo.value]);
    }
    for (const [, valor] of partes) {
        if (!valor) {
            throw new Error("Seleccione un valor para consultar");
        }
    }
    return "?" + partes.map(([clave, valor]) => clave + "=" + encodeURIComponent(valor)).join("&");
}

formFiltro.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    msgLista.textContent = "";
    try {
        ultimaConsulta = construirConsulta();
    } catch (e) {
        mostrarMensaje(msgLista, e.message, true);
        return;
    }
    await cargarLista();
});

async function cargarLista() {
    if (ultimaConsulta === null) {
        return; // todavia no se ha consultado nada
    }
    try {
        pintarTabla(await llamarApi(RUTA + ultimaConsulta));
    } catch (e) {
        mostrarMensaje(msgLista, e.message, true);
    }
}

// ---------------------------------------------------------------------
// Tabla y acciones por fila
// ---------------------------------------------------------------------
function pintarTabla(asignaciones) {
    tabla.replaceChildren();
    vacio.hidden = asignaciones.length > 0;

    for (const a of asignaciones) {
        const fila = document.createElement("tr");
        const valores = [
            a.profesor.nombre,
            a.cursoPeriodoDto.nombre,
            a.cursoPeriodoDto.materia.nombre,
            a.cursoPeriodoDto.periodo.nombre,
            a.tipoAsignacion.descripcion,
            a.estadoProfesorCurso.descripcion,
            new Date(a.fechaAsignacion).toLocaleString("es-CO")
        ];
        for (const valor of valores) {
            const celda = document.createElement("td");
            celda.textContent = valor;
            fila.appendChild(celda);
        }

        // Acciones: cambiar tipo + retirar/reactivar
        const acciones = document.createElement("td");
        acciones.className = "acciones";

        const selTipoFila = document.createElement("select");
        llenarSelect(selTipoFila, tipos, t => t.codigo, t => t.descripcion);
        selTipoFila.value = a.tipoAsignacion.codigo;
        acciones.appendChild(selTipoFila);
        acciones.appendChild(crearBoton("Cambiar tipo", "secundario",
                () => cambiarTipo(a, selTipoFila.value)));

        const activa = a.estadoProfesorCurso.nombre === "ACTIVO";
        acciones.appendChild(activa
                ? crearBoton("Retirar", "peligro", () => cambiarEstado(a, "retirar"))
                : crearBoton("Reactivar", "secundario", () => cambiarEstado(a, "reactivar")));

        fila.appendChild(acciones);
        tabla.appendChild(fila);
    }
}

// La PK es COMPUESTA: una asignacion se identifica con curso-periodo Y profesor
function rutaAsignacion(a) {
    return RUTA + "/" + encodeURIComponent(a.cursoPeriodoDto.id)
            + "/" + encodeURIComponent(a.profesor.id);
}

async function cambiarTipo(a, codigo) {
    try {
        await llamarApi(rutaAsignacion(a), opcionesJson("PUT", { tipoAsignacion: Number(codigo) }));
        mostrarMensaje(msgLista, "Tipo de asignación actualizado", false);
        await cargarLista();
    } catch (e) {
        mostrarMensaje(msgLista, e.message, true);
    }
}

async function cambiarEstado(a, accion) {
    if (accion === "retirar") {
        const texto = "¿Retirar a " + a.profesor.nombre + " del curso " + a.cursoPeriodoDto.nombre + "?";
        if (!window.confirm(texto)) {
            return;
        }
    }
    try {
        await llamarApi(rutaAsignacion(a) + "/" + accion, { method: "PUT" });
        mostrarMensaje(msgLista, accion === "retirar" ? "Profesor retirado" : "Profesor reactivado", false);
        await cargarLista();
    } catch (e) {
        mostrarMensaje(msgLista, e.message, true);
    }
}

iniciar();
