// Comportamiento SOLO visual del encabezado y del menú de opciones.
// No llama al servidor ni toca formularios: eso lo siguen haciendo api.js y el JS de cada página.

(function () {
    const menu = document.getElementById("menuPrincipal");
    const botonMenu = document.getElementById("botonMenu");
    const grupos = Array.from(document.querySelectorAll(".menu-grupo"));
    const escritorio = window.matchMedia("(min-width: 981px)");

    function cerrarGrupos(excepto) {
        for (const g of grupos) {
            if (g !== excepto) {
                g.setAttribute("aria-expanded", "false");
                g.nextElementSibling.hidden = true;
            }
        }
    }

    for (const grupo of grupos) {
        const submenu = grupo.nextElementSibling;
        grupo.addEventListener("click", () => {
            // En escritorio, si el mouse ya lo abrio, el clic no debe cerrarlo
            if (escritorio.matches && grupo.parentElement.matches(":hover")) {
                cerrarGrupos(grupo);
                grupo.setAttribute("aria-expanded", "true");
                submenu.hidden = false;
                return;
            }
            const abrir = grupo.getAttribute("aria-expanded") !== "true";
            cerrarGrupos(grupo);
            grupo.setAttribute("aria-expanded", String(abrir));
            submenu.hidden = !abrir;
        });
        // En escritorio el submenú también abre al pasar el mouse
        const item = grupo.parentElement;
        item.addEventListener("mouseenter", () => {
            if (!escritorio.matches) return;
            cerrarGrupos(grupo);
            grupo.setAttribute("aria-expanded", "true");
            submenu.hidden = false;
        });
        item.addEventListener("mouseleave", () => {
            if (!escritorio.matches) return;
            grupo.setAttribute("aria-expanded", "false");
            submenu.hidden = true;
        });
    }

    document.addEventListener("click", (e) => {
        if (!e.target.closest(".menu-principal > ul > li")) cerrarGrupos(null);
    });
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            const abierto = grupos.find(g => g.getAttribute("aria-expanded") === "true");
            cerrarGrupos(null);
            if (abierto) abierto.focus();
        }
    });

    if (botonMenu) {
        botonMenu.addEventListener("click", () => {
            const abrir = !menu.classList.contains("abierto");
            menu.classList.toggle("abierto", abrir);
            botonMenu.setAttribute("aria-expanded", String(abrir));
        });
    }

    // Menú de opciones de la entidad: marca la sección que se está viendo
    const enlaces = Array.from(document.querySelectorAll(".opciones a[href^='#']"));
    if (enlaces.length && "IntersectionObserver" in window) {
        const porId = new Map(enlaces.map(a => [a.getAttribute("href").slice(1), a]));
        const visibles = new Set();
        const observador = new IntersectionObserver((entradas) => {
            for (const e of entradas) {
                if (e.isIntersecting) visibles.add(e.target.id); else visibles.delete(e.target.id);
            }
            const primera = enlaces.find(a => visibles.has(a.getAttribute("href").slice(1)));
            for (const a of enlaces) a.classList.toggle("activa", a === (primera || enlaces[0]));
        }, { rootMargin: "-140px 0px -45% 0px" });
        for (const id of porId.keys()) {
            const seccion = document.getElementById(id);
            if (seccion) observador.observe(seccion);
        }
    }
})();
