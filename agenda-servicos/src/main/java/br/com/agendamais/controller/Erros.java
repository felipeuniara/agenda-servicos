package br.com.agendamais.controller;

import br.com.agendamais.service.BusinessException;
import org.springframework.validation.BindingResult;

final class Erros {
    private Erros() {
    }

    static void aplicar(BindingResult result, BusinessException e) {
        if (e.getCampo() != null) {
            try {
                result.rejectValue(e.getCampo(), "regra", e.getMessage());
                return;
            } catch (RuntimeException ignorada) {
            }
        }
        result.reject("regra", e.getMessage());
    }
}
