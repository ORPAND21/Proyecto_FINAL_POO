// Corre en el NAVEGADOR. MODULO DE INSCRIPCIONES DE ESTUDIANTES:
// pedir datos, llamar al controlador (via HTTP) y mostrar resultados o errores.

const RUTA = "/api/cursos-estudiantes";

const formAsignar = document.getElementById("formAsignar");
const selEstudiante = document.getElementById("selEstudiante");
const selCurso = document.getElementById("selCurso");
const msgAsignar = document.getElementById("msgAsignar");

const formFiltro = document.getElementById("formFiltro");
const filtroTipo = document.getElementById("filtroTipo");
const filtroEstudiante = document.getElementById("filtroEstudiante");
const filtroCurso = document.getElementById("filtroCurso");
const filtroMateria = document.getElementById("filtroMateria");
const filtroPeriodo = document.getElementById("filtroPeriodo");
const grupoEstudiante = document.getElementById("grupoEstudiante");
const grupoCurso = document.getElementById("grupoCurso");
const grupoMateria = document.getElementById("grupoMateria");
const grupoPeriodo = document.getElementById("grupoPeriodo");
const msgLista = document.getElementById("msgLista");

const tabla = document.getElementById("tabla");
const vacio = document.getElementById("vacio");

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

function textoCurso(c) {
    return c.nombre + " — " + c.materia.nombre + " (" + c.periodo.nombre + ")";
}

async function iniciar() {
    try {
        const [estudiantes, cursos] = await Promise.all([
            llamarApi("/api/estudiantes"),
            llamarApi("/api/cursos-periodos")
        ]);

        llenarSelect(selEstudiante, estudiantes, e => e.id, e => e.codigo + " - " + e.nombre, "Seleccione un estudiante");
        llenarSelect(selCurso, cursos, c => c.id, textoCurso, "Seleccione un curso");

        llenarSelect(filtroEstudiante, estudiantes, e => e.id, e => e.codigo + " - " + e.nombre, "Seleccione un estudiante");
        llenarSelect(filtroCurso, cursos, c => c.id, textoCurso, "Seleccione un curso");
        // Materias y periodos para filtrar: se sacan de los cursos existentes
        llenarSelect(filtroMateria, unicos(cursos.map(c => c.materia), m => m.id),
                m => m.id, m => m.nombre, "Seleccione una materia");
        llenarSelect(filtroPeriodo, unicos(cursos.map(c => c.periodo), p => p.id),
                p => p.id, p => p.nombre, "Seleccione un periodo");
    } catch (e) {
        mostrarMensaje(msgAsignar, e.message, true);
    }
}

// ---------------------------------------------------------------------
// 1. Inscribir  (POST /api/cursos-estudiantes)
// ---------------------------------------------------------------------
formAsignar.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const cuerpo = {
        idCursoPeriodo: selCurso.value,
        idEstudiante: selEstudiante.value
    };

    try {
        const r = await llamarApi(RUTA, opcionesJson("POST", cuerpo));
        mostrarMensaje(msgAsignar,
                "Inscrito: " + r.estudiante.nombre + " → " + r.cursoPeriodo.nombre
                + " (" + r.estado.descripcion + ")", false);
        await cargarLista();
    } catch (e) {
        mostrarMensaje(msgAsignar, e.message, true);
    }
});

// ---------------------------------------------------------------------
// 2-6. Consultar  (GET /api/cursos-estudiantes?...)
// ---------------------------------------------------------------------
function actualizarCamposFiltro() {
    const modo = filtroTipo.value;
    grupoEstudiante.hidden = modo !== "estudiante";
    grupoCurso.hidden = modo !== "curso";
    grupoMateria.hidden = modo !== "materia" && modo !== "materiaPeriodo";
    grupoPeriodo.hidden = modo !== "periodo" && modo !== "materiaPeriodo";
}
filtroTipo.addEventListener("change", actualizarCamposFiltro);

function construirConsulta() {
    const modo = filtroTipo.value;
    const partes = [];
    if (modo === "estudiante") {
        partes.push(["idEstudiante", filtroEstudiante.value]);
    }
    if (modo === "curso") {
        partes.push(["idCursoPeriodo", filtroCurso.value]);
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
// Tabla y acciones por fila (los botones dependen del estado)
//   PENDIENTE -> Admitir, Rechazar, Retirar
//   ADMITIDO  -> Aprobar, Reprobar, Retirar
//   RETIRADO  -> Reasignar curso
//   RECHAZADO, APROBADO, REPROBADO -> sin acciones (estados finales)
// ---------------------------------------------------------------------
function pintarTabla(inscripciones) {
    tabla.replaceChildren();
    vacio.hidden = inscripciones.length > 0;

    for (const i of inscripciones) {
        const fila = crearFila([
            i.estudiante.nombre,
            i.cursoPeriodo.nombre,
            i.cursoPeriodo.materia.nombre,
            i.cursoPeriodo.periodo.nombre,
            i.estado.descripcion,
            new Date(i.fechaInscripcion).toLocaleString("es-CO")
        ]);

        const botones = [];
        const estado = i.estado.nombre;
        if (estado === "PENDIENTE") {
            botones.push(crearBoton("Admitir", "secundario", () => cambiarEstado(i, "admitir")));
            botones.push(crearBoton("Rechazar", "peligro", () => cambiarEstado(i, "rechazar")));
            botones.push(crearBoton("Retirar", "peligro", () => cambiarEstado(i, "retirar")));
        } else if (estado === "ADMITIDO") {
            botones.push(crearBoton("Aprobar", "secundario", () => cambiarEstado(i, "aprobar")));
            botones.push(crearBoton("Reprobar", "peligro", () => cambiarEstado(i, "reprobar")));
            botones.push(crearBoton("Retirar", "peligro", () => cambiarEstado(i, "retirar")));
        } else if (estado === "RETIRADO") {
            botones.push(crearBoton("Reasignar curso", "secundario", () => cambiarEstado(i, "reasignar")));
        }
        agregarAcciones(fila, botones);
        tabla.appendChild(fila);
    }
}

// La PK es COMPUESTA: una inscripcion se identifica con curso-periodo Y estudiante
function rutaInscripcion(i) {
    return RUTA + "/" + encodeURIComponent(i.cursoPeriodo.id)
            + "/" + encodeURIComponent(i.estudiante.id);
}

const TEXTOS = {
    admitir: { pregunta: null, ok: "Inscripción admitida" },
    aprobar: { pregunta: "¿Aprobar a ", ok: "Inscripción aprobada" },
    reprobar: { pregunta: "¿Reprobar a ", ok: "Inscripción reprobada" },
    retirar: { pregunta: "¿Retirar a ", ok: "Estudiante retirado" },
    rechazar: { pregunta: "¿Rechazar la inscripción de ", ok: "Inscripción rechazada" },
    reasignar: { pregunta: null, ok: "Estudiante reasignado al curso" }
};

async function cambiarEstado(i, accion) {
    const textos = TEXTOS[accion];
    if (textos.pregunta !== null) {
        const texto = textos.pregunta + i.estudiante.nombre + " en el curso " + i.cursoPeriodo.nombre + "?";
        if (!window.confirm(texto)) {
            return;
        }
    }
    try {
        await llamarApi(rutaInscripcion(i) + "/" + accion, { method: "PUT" });
        mostrarMensaje(msgLista, textos.ok, false);
        await cargarLista();
    } catch (e) {
        mostrarMensaje(msgLista, e.message, true);
    }
}

iniciar();
