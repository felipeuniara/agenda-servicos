package br.com.agendamais.controller;

import br.com.agendamais.entity.TipoAcesso;
import br.com.agendamais.service.BusinessException;
import br.com.agendamais.service.TipoAcessoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tipos-acesso")
public class TipoAcessoController {
    private final TipoAcessoService tipoAcessoService;

    public TipoAcessoController(TipoAcessoService tipoAcessoService) {
        this.tipoAcessoService = tipoAcessoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tipos", tipoAcessoService.listar());
        model.addAttribute("contagem", tipoAcessoService.usuariosPorTipo());
        return "tipos-acesso/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("tipoAcesso", new TipoAcesso());
        return "tipos-acesso/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            TipoAcesso t = tipoAcessoService.buscar(id);
            TipoAcesso copia = new TipoAcesso(t.getDescricao());
            copia.setId(t.getId());
            model.addAttribute("tipoAcesso", copia);
            return "tipos-acesso/form";
        } catch (BusinessException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/tipos-acesso";
        }
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("tipoAcesso") TipoAcesso tipoAcesso, BindingResult result,
                         RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                TipoAcesso salvo = tipoAcessoService.salvar(tipoAcesso);
                ra.addFlashAttribute("sucesso", "Tipo de acesso " + salvo.getDescricao() + " salvo com sucesso.");
                return "redirect:/tipos-acesso";
            } catch (BusinessException e) {
                Erros.aplicar(result, e);
            }
        }
        return "tipos-acesso/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            tipoAcessoService.excluir(id);
            ra.addFlashAttribute("sucesso", "Tipo de acesso excluído.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/tipos-acesso";
    }
}
