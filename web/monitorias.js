// Corre en el NAVEGADOR. MODULO DE MONITORIAS:
// pedir datos, llamar al controlador (via HTTP) y mostrar resultados o errores.

const RUTA = "/api/monitorias";

const formAsignar = document.getElementById("formAsignar");
const selCurso = document.getElementById("selCurso");
const selEstudiante = document.getElementById("selEstudiante");
const selProfesor = document.getElementById("selProfesor");
const msgAsignar = document.getElementById("msgAsignar");

const formFiltro = document.getElementById("formFiltro");
const filtroTipo = document.getElementById("filtroTipo");
const filtroEstudiante = document.getElementById("filtroEstudiante");
const filtroProfesor = document.getElementById("filtroProfesor");
const filtroCurso = document.getElementById("filtroCurso");
const grupoEstudiante = document.getElementById("grupoEstudiante");
const grupoProfesor = document.getElementById("grupoProfesor");
const grupoCurso = document.getElementById("grupoCurso");
const msgLista = document.getElementById("msgLista");

const tabla = document.getElementById("tabla");
const vacio = document.getElementById("vacio");

let ultimaConsulta = null;    // la consulta actual, para refrescar la tabla tras cada accion

// ---------------------------------------------------------------------
// Desplegables
// ---------------------------------------------------------------------
function textoCurso(c) {
    return c.nombre + " — " + c.materia.nombre + " (" + c.periodo.nombre + ")";
}

async function iniciar() {
    try {
        const [cursos, estudiantes, profesores] = await Promise.all([
            llamarApi("/api/cursos-periodos"),
            llamarApi("/api/estudiantes"),
            llamarApi("/api/profesores")
        ]);

        llenarSelect(selCurso, cursos, c => c.id, textoCurso, "Seleccione un curso");
        llenarSelect(selEstudiante, estudiantes, e => e.id, e => e.codigo + " - " + e.nombre, "Seleccione un estudiante");
        llenarSelect(selProfesor, profesores, p => p.id, p => p.nombre, "Seleccione un profesor");

        llenarSelect(filtroCurso, cursos, c => c.id, textoCurso, "Seleccione un curso");
        llenarSelect(filtroEstudiante, estudiantes, e => e.id, e => e.codigo + " - " + e.nombre, "Seleccione un estudiante");
        llenarSelect(filtroProfesor, profesores, p => p.id, p => p.nombre, "Seleccione un profesor");
    } catch (e) {
        mostrarMensaje(msgAsignar, e.message, true);
    }
}

// ---------------------------------------------------------------------
// 1. Crear monitoria  (POST /api/monitorias)
// ---------------------------------------------------------------------
formAsignar.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const cuerpo = {
        idCursoPeriodo: selCurso.value,
        idEstudiante: selEstudiante.value,
        idProfesor: selProfesor.value
    };

    try {
        const r = await llamarApi(RUTA, opcionesJson("POST", cuerpo));
        mostrarMensaje(msgAsignar,
                "Monitoría creada: " + r.estudiante.nombre + " con " + r.profesor.nombre
                + " en " + r.cursoPeriodo.nombre + " (" + r.estadoMonitoria.descripcion + ")", false);
        await cargarLista();
    } catch (e) {
        // Aqui llegan, por ejemplo, "El estudiante no esta inscrito en el curso"
        // o "El profesor no esta asignado al curso"
        mostrarMensaje(msgAsignar, e.message, true);
    }
});

// ---------------------------------------------------------------------
// 4. Listar  (GET /api/monitorias  |  ?idEstudiante= | ?idProfesor= | ?idCursoPeriodo=)
// ---------------------------------------------------------------------
function actualizarCamposFiltro() {
    const modo = filtroTipo.value;
    grupoEstudiante.hidden = modo !== "estudiante";
    grupoProfesor.hidden = modo !== "profesor";
    grupoCurso.hidden = modo !== "curso";
}
filtroTipo.addEventListener("change", actualizarCamposFiltro);

function construirConsulta() {
    const modo = filtroTipo.value;
    if (modo === "todas") {
        return "";
    }
    let clave;
    let valor;
    if (modo === "estudiante") {
        clave = "idEstudiante";
        valor = filtroEstudiante.value;
    } else if (modo === "profesor") {
        clave = "idProfesor";
        valor = filtroProfesor.value;
    } else {
        clave = "idCursoPeriodo";
        valor = filtroCurso.value;
    }
    if (!valor) {
        throw new Error("Seleccione un valor para consultar");
    }
    return "?" + clave + "=" + encodeURIComponent(valor);
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
// Tabla y acciones por fila: 2. evaluar, 3. eliminar
// ---------------------------------------------------------------------
function pintarTabla(monitorias) {
    tabla.replaceChildren();
    vacio.hidden = monitorias.length > 0;

    for (const m of monitorias) {
        const fila = crearFila([
            m.estudiante.nombre,
            m.profesor.nombre,
            textoCurso(m.cursoPeriodo),
            m.estadoMonitoria.descripcion,
            m.calificacion === null ? "—" : m.calificacion,
            m.comentario === null ? "—" : m.comentario,
            new Date(m.fechaCreacion).toLocaleString("es-CO"),
            m.fechaEvaluacion === null ? "—" : new Date(m.fechaEvaluacion).toLocaleString("es-CO")
        ]);

        const botones = [];
        // Una monitoria REPROBADA ya no se puede reevaluar (ver EstadoMonitoria.cambiarEstadoA)
        if (m.estadoMonitoria.nombre !== "REPROBADO") {
            const campoNota = document.createElement("input");
            campoNota.type = "number";
            campoNota.min = "0";
            campoNota.max = "500";
            campoNota.placeholder = "Nota 0-500";
            campoNota.style.width = "7em";

            const campoComentario = document.createElement("input");
            campoComentario.type = "text";
            campoComentario.placeholder = "Comentario";

            botones.push(campoNota, campoComentario,
                    crearBoton("Evaluar", "secundario",
                            () => evaluar(m, campoNota.value, campoComentario.value)));
        }
        botones.push(crearBoton("Eliminar", "peligro", () => eliminar(m)));
        agregarAcciones(fila, botones);
        tabla.appendChild(fila);
    }
}

// La PK es COMPUESTA: una monitoria se identifica con curso-periodo, estudiante Y profesor
function rutaMonitoria(m) {
    return RUTA + "/" + encodeURIComponent(m.cursoPeriodo.id)
            + "/" + encodeURIComponent(m.estudiante.id)
            + "/" + encodeURIComponent(m.profesor.id);
}

// ---- 2. Evaluar  (PUT /api/monitorias/{curso}/{estudiante}/{profesor}) ----
async function evaluar(m, nota, comentario) {
    if (nota === "") {
        mostrarMensaje(msgLista, "Ingrese la calificación", true);
        return;
    }
    try {
        await llamarApi(rutaMonitoria(m),
                opcionesJson("PUT", { calificacion: Number(nota), comentario: comentario }));
        mostrarMensaje(msgLista, "Monitoría evaluada", false);
        await cargarLista();
    } catch (e) {
        mostrarMensaje(msgLista, e.message, true);
    }
}

// ---- 3. Eliminar  (DELETE /api/monitorias/{curso}/{estudiante}/{profesor}) ----
async function eliminar(m) {
    if (!window.confirm("¿Eliminar la monitoría de " + m.estudiante.nombre + " en " + m.cursoPeriodo.nombre + "?")) {
        return;
    }
    try {
        await llamarApi(rutaMonitoria(m), { method: "DELETE" });
        mostrarMensaje(msgLista, "Monitoría eliminada", false);
        await cargarLista();
    } catch (e) {
        mostrarMensaje(msgLista, e.message, true);
    }
}

iniciar();
