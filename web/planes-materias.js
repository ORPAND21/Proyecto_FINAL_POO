// Corre en el NAVEGADOR. MODULO DE ASIGNACIONES DE MATERIAS A PLANES:
// pedir datos, llamar al controlador (via HTTP) y mostrar resultados o errores.

const RUTA = "/api/planes-materias";

const formAsignar = document.getElementById("formAsignar");
const selPlan = document.getElementById("selPlan");
const selMateria = document.getElementById("selMateria");
const campoSemestre = document.getElementById("semestre");
const campoOrden = document.getElementById("orden");
const msgAsignar = document.getElementById("msgAsignar");

const formFiltro = document.getElementById("formFiltro");
const filtroTipo = document.getElementById("filtroTipo");
const filtroPlan = document.getElementById("filtroPlan");
const filtroMateria = document.getElementById("filtroMateria");
const grupoPlan = document.getElementById("grupoPlan");
const grupoMateria = document.getElementById("grupoMateria");
const msgLista = document.getElementById("msgLista");

const tabla = document.getElementById("tabla");
const vacio = document.getElementById("vacio");

let ultimaConsulta = null;    // la consulta actual, para refrescar la tabla tras cada accion

// ---------------------------------------------------------------------
// Desplegables
// ---------------------------------------------------------------------
function textoPlan(p) {
    return p.version + " — " + p.programa.nombre;
}

async function iniciar() {
    try {
        const [planes, materias] = await Promise.all([
            llamarApi("/api/planes-estudio"),
            llamarApi("/api/materias")
        ]);

        llenarSelect(selPlan, planes, p => p.id, textoPlan, "Seleccione un plan");
        llenarSelect(selMateria, materias, m => m.id, m => m.nombre, "Seleccione una materia");

        llenarSelect(filtroPlan, planes, p => p.id, textoPlan, "Seleccione un plan");
        llenarSelect(filtroMateria, materias, m => m.id, m => m.nombre, "Seleccione una materia");
    } catch (e) {
        mostrarMensaje(msgAsignar, e.message, true);
    }
}

// ---------------------------------------------------------------------
// 1. Asignar materia a plan  (POST /api/planes-materias)
// ---------------------------------------------------------------------
formAsignar.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const cuerpo = {
        idPlanEstudio: selPlan.value,
        idMateria: selMateria.value,
        semestre: Number(campoSemestre.value),
        orden: Number(campoOrden.value)
    };

    try {
        const r = await llamarApi(RUTA, opcionesJson("POST", cuerpo));
        mostrarMensaje(msgAsignar,
                "Asignada: " + r.materia.nombre + " → plan " + r.planEstudio.version
                + " (semestre " + r.semestre + ", orden " + r.orden + ")", false);
        formAsignar.reset();
        await cargarLista();
    } catch (e) {
        mostrarMensaje(msgAsignar, e.message, true);
    }
});

// ---------------------------------------------------------------------
// 2-3. Consultar  (GET /api/planes-materias?idPlanEstudio=... | ?idMateria=...)
// ---------------------------------------------------------------------
function actualizarCamposFiltro() {
    const modo = filtroTipo.value;
    grupoPlan.hidden = modo !== "plan";
    grupoMateria.hidden = modo !== "materia";
}
filtroTipo.addEventListener("change", actualizarCamposFiltro);

function construirConsulta() {
    const modo = filtroTipo.value;
    const clave = modo === "plan" ? "idPlanEstudio" : "idMateria";
    const valor = modo === "plan" ? filtroPlan.value : filtroMateria.value;
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
// Tabla y acciones por fila: 4. actualizar semestre u orden, 5. quitar
// ---------------------------------------------------------------------
function crearCampoNumero(valor) {
    const campo = document.createElement("input");
    campo.type = "number";
    campo.min = "1";
    campo.value = valor;
    campo.style.width = "5em";
    return campo;
}

function pintarTabla(asignaciones) {
    tabla.replaceChildren();
    vacio.hidden = asignaciones.length > 0;

    for (const a of asignaciones) {
        const fila = crearFila([
            a.planEstudio.version,
            a.planEstudio.programa.nombre,
            a.materia.nombre,
            a.materia.creditos
        ]);

        // Semestre y orden se editan en la misma fila
        const campoSemestreFila = crearCampoNumero(a.semestre);
        const campoOrdenFila = crearCampoNumero(a.orden);
        for (const campo of [campoSemestreFila, campoOrdenFila]) {
            const celda = document.createElement("td");
            celda.appendChild(campo);
            fila.appendChild(celda);
        }

        agregarAcciones(fila, [
            crearBoton("Guardar cambios", "secundario",
                    () => actualizar(a, campoSemestreFila.value, campoOrdenFila.value)),
            crearBoton("Quitar", "peligro", () => quitar(a))
        ]);
        tabla.appendChild(fila);
    }
}

// La PK es COMPUESTA: una asignacion se identifica con plan Y materia
function rutaAsignacion(a) {
    return RUTA + "/" + encodeURIComponent(a.planEstudio.id)
            + "/" + encodeURIComponent(a.materia.id);
}

// ---- 4. Actualizar semestre u orden  (PUT /api/planes-materias/{plan}/{materia}) ----
async function actualizar(a, semestre, orden) {
    try {
        await llamarApi(rutaAsignacion(a),
                opcionesJson("PUT", { semestre: Number(semestre), orden: Number(orden) }));
        mostrarMensaje(msgLista, "Semestre/orden actualizado", false);
        await cargarLista();
    } catch (e) {
        mostrarMensaje(msgLista, e.message, true);
    }
}

// ---- 5. Quitar materia del plan  (DELETE /api/planes-materias/{plan}/{materia}) ----
async function quitar(a) {
    if (!window.confirm("¿Quitar la materia " + a.materia.nombre + " del plan " + a.planEstudio.version + "?")) {
        return;
    }
    try {
        await llamarApi(rutaAsignacion(a), { method: "DELETE" });
        mostrarMensaje(msgLista, "Materia quitada del plan", false);
        await cargarLista();
    } catch (e) {
        mostrarMensaje(msgLista, e.message, true);
    }
}

iniciar();
