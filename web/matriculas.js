// Corre en el NAVEGADOR. Es la vista web de Matricula: pide datos, llama al
// controlador por HTTP y muestra resultados o errores.

const RUTA = "/api/matriculas";

const formMatricular = document.getElementById("formMatricular");
const selEstudiante = document.getElementById("selEstudiante");
const selCurso = document.getElementById("selCurso");
const selProfesor = document.getElementById("selProfesor");
const resumenCurso = document.getElementById("resumenCurso");
const msgMatricular = document.getElementById("msgMatricular");

const formFiltro = document.getElementById("formFiltro");
const filtroTipo = document.getElementById("filtroTipo");
const filtroEstudiante = document.getElementById("filtroEstudiante");
const filtroCursoPeriodo = document.getElementById("filtroCursoPeriodo");
const grupoEstudiante = document.getElementById("grupoEstudiante");
const grupoCursoPeriodo = document.getElementById("grupoCursoPeriodo");
const msgLista = document.getElementById("msgLista");

const tabla = document.getElementById("tabla");
const vacio = document.getElementById("vacio");

let cursos = [];              // cursos del periodo cargados del servidor
let ultimaConsulta = null;    // consulta actual, para refrescar tras cada accion

// ---------------------------------------------------------------------
// Textos de los desplegables
// ---------------------------------------------------------------------
function textoEstudiante(estudiante) {
    return estudiante.nombre + " (" + estudiante.codigo + ")";
}

function textoCurso(curso) {
    return curso.nombre + " — " + curso.materia.nombre + " (" + curso.periodo.nombre + ")";
}

// ---------------------------------------------------------------------
// Desplegables iniciales
// ---------------------------------------------------------------------
async function iniciar() {
    try {
        const [estudiantes, cursosApi, profesores] = await Promise.all([
            llamarApi("/api/estudiantes"),
            llamarApi("/api/cursos-periodos"),
            llamarApi("/api/profesores")
        ]);
        cursos = cursosApi;

        llenarSelect(selEstudiante, estudiantes, e => e.id, textoEstudiante,
                "Seleccione un estudiante");
        llenarSelect(selCurso, cursos, c => c.id, textoCurso,
                "Seleccione un curso");
        llenarSelect(selProfesor, profesores, p => p.id, p => p.nombre,
                "Seleccione un profesor");

        llenarSelect(filtroEstudiante, estudiantes, e => e.id, textoEstudiante,
                "Seleccione un estudiante");
        llenarSelect(filtroCursoPeriodo, cursos, c => c.id, textoCurso,
                "Seleccione un curso");

        actualizarResumenCurso();
    } catch (e) {
        mostrarMensaje(msgMatricular, e.message, true);
    }
}

// ---------------------------------------------------------------------
// Matricular  (POST /api/matriculas)
// ---------------------------------------------------------------------
function cursoSeleccionado() {
    return cursos.find(c => c.id === selCurso.value) || null;
}

function actualizarResumenCurso() {
    const curso = cursoSeleccionado();
    if (!curso) {
        resumenCurso.textContent = "";
        return;
    }
    resumenCurso.textContent = "Materia: " + curso.materia.nombre
            + " (id " + curso.materia.id + ") · Periodo: "
            + curso.periodo.nombre + " (id " + curso.periodo.id + ")";
}
selCurso.addEventListener("change", actualizarResumenCurso);

formMatricular.addEventListener("submit", async (evento) => {
    evento.preventDefault(); // evita que el formulario recargue la pagina
    msgMatricular.textContent = "";

    const curso = cursoSeleccionado();
    if (!curso) {
        mostrarMensaje(msgMatricular, "Seleccione un curso del periodo", true);
        return;
    }
    if (!selEstudiante.value || !selProfesor.value) {
        mostrarMensaje(msgMatricular, "Seleccione el estudiante y el profesor", true);
        return;
    }

    // La materia y el periodo salen del curso, igual que en la vista de consola.
    const cuerpo = {
        idEstudiante: selEstudiante.value,
        idCursoPeriodo: selCurso.value,
        idMateria: Number(curso.materia.id),
        idPeriodoAcademico: Number(curso.periodo.id),
        idProfesor: selProfesor.value
    };

    try {
        const r = await llamarApi(RUTA, opcionesJson("POST", cuerpo));
        mostrarMensaje(msgMatricular,
                "Matriculado: " + r.estudiante.nombre + " → " + r.cursoPeriodo.nombre,
                false);
        await cargarLista();
    } catch (e) {
        // Aqui llega, por ejemplo, "Ya existe la matricula..." si la PK esta repetida
        mostrarMensaje(msgMatricular, e.message, true);
    }
});

// ---------------------------------------------------------------------
// Consultar  (GET /api/matriculas?idEstudiante=... | ?idCursoPeriodo=...)
// ---------------------------------------------------------------------
function actualizarCamposFiltro() {
    const modo = filtroTipo.value;
    grupoEstudiante.hidden = modo !== "estudiante";
    grupoCursoPeriodo.hidden = modo !== "cursoPeriodo";
}
filtroTipo.addEventListener("change", actualizarCamposFiltro);

function construirConsulta() {
    const modo = filtroTipo.value;
    let clave;
    let valor;
    if (modo === "estudiante") {
        clave = "idEstudiante";
        valor = filtroEstudiante.value;
    } else {
        clave = "idCursoPeriodo";
        valor = filtroCursoPeriodo.value;
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
// Tabla y acciones por fila
// ---------------------------------------------------------------------
function pintarTabla(matriculas) {
    tabla.replaceChildren();
    vacio.hidden = matriculas.length > 0;

    for (const m of matriculas) {
        const fila = document.createElement("tr");
        const fecha = m.fechaMatricula
                ? new Date(m.fechaMatricula).toLocaleString("es-CO")
                : "";
        const valores = [
            m.estudiante.nombre + " (" + m.estudiante.codigo + ")",
            m.cursoPeriodo.nombre,
            m.materia.nombre,
            m.periodoAcademico.nombre,
            m.profesor.nombre,
            m.estadoMatricula.descripcion,
            fecha
        ];
        for (const valor of valores) {
            const celda = document.createElement("td");
            celda.textContent = valor;
            fila.appendChild(celda);
        }

        const acciones = document.createElement("td");
        acciones.className = "acciones";
        const activa = m.estadoMatricula.nombre === "ACTIVO";
        acciones.appendChild(activa
                ? crearBoton("Retirar", "peligro", () => cambiarEstado(m, "retirar"))
                : crearBoton("Reactivar", "secundario", () => cambiarEstado(m, "reactivar")));

        fila.appendChild(acciones);
        tabla.appendChild(fila);
    }
}

// La PK es COMPUESTA: una matricula se identifica con sus CINCO modelos.
function rutaMatricula(m) {
    return RUTA + "/" + encodeURIComponent(m.estudiante.id)
            + "/" + encodeURIComponent(m.cursoPeriodo.id)
            + "/" + encodeURIComponent(m.materia.id)
            + "/" + encodeURIComponent(m.periodoAcademico.id)
            + "/" + encodeURIComponent(m.profesor.id);
}

async function cambiarEstado(m, accion) {
    if (accion === "retirar") {
        const texto = "¿Retirar la matrícula de " + m.estudiante.nombre
                + " en " + m.cursoPeriodo.nombre + "?";
        if (!window.confirm(texto)) {
            return;
        }
    }
    try {
        await llamarApi(rutaMatricula(m) + "/" + accion, { method: "PUT" });
        mostrarMensaje(msgLista,
                accion === "retirar" ? "Matrícula retirada" : "Matrícula reactivada",
                false);
        await cargarLista();
    } catch (e) {
        mostrarMensaje(msgLista, e.message, true);
    }
}

iniciar();
