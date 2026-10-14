(function () {
    "use strict";

    document.querySelectorAll("form[data-confirm]").forEach(function (form) {
        form.addEventListener("submit", function (e) {
            if (!window.confirm(form.dataset.confirm)) {
                e.preventDefault();
            }
        });
    });

    document.querySelectorAll("select[data-autosubmit]").forEach(function (sel) {
        sel.addEventListener("change", function () { sel.form.submit(); });
    });

    document.querySelectorAll(".form-select:not(.select-pill):not(.select-plain)").forEach(function (sel) {
        var marcar = function () { sel.classList.toggle("vazio", sel.value === ""); };
        sel.addEventListener("change", marcar);
        marcar();
    });

    var moeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

    var servico = document.getElementById("servicoId");
    if (servico) {
        var valor = document.getElementById("valor");
        var total = document.getElementById("total");
        var data = document.getElementById("data");
        var horario = document.getElementById("horario");
        var profissional = document.getElementById("usuarioId");
        var resumo = document.getElementById("resumo");

        var atualizar = function () {
            var opt = servico.options[servico.selectedIndex];
            var v = opt && opt.dataset.valor ? parseFloat(opt.dataset.valor) : 0;
            valor.value = v ? moeda.format(v) : "";
            total.textContent = moeda.format(v);

            var partes = [];
            if (data.value) {
                var p = data.value.split("-");
                partes.push(p[2] + "/" + p[1] + "/" + p[0] + (horario.value ? " às " + horario.value : ""));
            }
            if (profissional.value) {
                partes.push("Profissional: " + profissional.options[profissional.selectedIndex].text);
            }
            resumo.textContent = partes.length ? partes.join(" • ") : "Selecione data, horário e profissional";
        };
        [servico, data, horario, profissional].forEach(function (el) {
            el.addEventListener("change", atualizar);
        });
        atualizar();
    }

    document.querySelectorAll("select[data-uf]").forEach(function (uf) {
        var estado = document.getElementById(uf.dataset.uf);
        var preencher = function () {
            var opt = uf.options[uf.selectedIndex];
            estado.value = opt && opt.value ? opt.dataset.estado : "";
        };
        uf.addEventListener("change", preencher);
        preencher();
    });

    var mascara = function (el, fn) {
        el.addEventListener("input", function () { el.value = fn(el.value.replace(/\D/g, "")); });
    };
    document.querySelectorAll("input[data-mask='cep']").forEach(function (el) {
        mascara(el, function (d) {
            d = d.slice(0, 8);
            return d.length > 5 ? d.slice(0, 5) + "-" + d.slice(5) : d;
        });
    });
    document.querySelectorAll("input[data-mask='telefone']").forEach(function (el) {
        mascara(el, function (d) {
            d = d.slice(0, 11);
            if (d.length <= 2) return d.length ? "(" + d : d;
            if (d.length <= 6) return "(" + d.slice(0, 2) + ") " + d.slice(2);
            if (d.length <= 10) return "(" + d.slice(0, 2) + ") " + d.slice(2, 6) + "-" + d.slice(6);
            return "(" + d.slice(0, 2) + ") " + d.slice(2, 7) + "-" + d.slice(7);
        });
    });
})();
