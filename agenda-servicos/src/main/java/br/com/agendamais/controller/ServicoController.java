package br.com.agendamais.controller;

import br.com.agendamais.entity.Servico;
import br.com.agendamais.service.BusinessException;
import br.com.agendamais.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/servicos")
public class ServicoController {
    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("servicos", servicoService.listar());
        model.addAttribute("contagem", servicoService.agendamentosPorServico());
        return "servicos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("servico", new Servico());
        return "servicos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            Servico s = servicoService.buscar(id);
            Servico copia = new Servico(s.getNome(), s.getValor());
            copia.setId(s.getId());
            model.addAttribute("servico", copia);
            return "servicos/form";
        } catch (BusinessException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/servicos";
        }
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("servico") Servico servico, BindingResult result,
                         RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                Servico salvo = servicoService.salvar(servico);
                ra.addFlashAttribute("sucesso", "Serviço " + salvo.getNome() + " salvo com sucesso.");
                return "redirect:/servicos";
            } catch (BusinessException e) {
                Erros.aplicar(result, e);
            }
        }
        return "servicos/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            servicoService.excluir(id);
            ra.addFlashAttribute("sucesso", "Serviço excluído.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/servicos";
    }
}
