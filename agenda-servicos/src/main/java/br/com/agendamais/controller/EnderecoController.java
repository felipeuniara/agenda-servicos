package br.com.agendamais.controller;

import br.com.agendamais.entity.Endereco;
import br.com.agendamais.service.BusinessException;
import br.com.agendamais.service.EnderecoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/enderecos")
public class EnderecoController {
    private final EnderecoService enderecoService;

    public EnderecoController(EnderecoService enderecoService) {
        this.enderecoService = enderecoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("enderecos", enderecoService.listar());
        model.addAttribute("donos", enderecoService.clientesPorEndereco());
        return "enderecos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("endereco", new Endereco());
        model.addAttribute("clientesSemEndereco", enderecoService.clientesSemEndereco());
        return "enderecos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            Endereco o = enderecoService.buscar(id);
            Endereco e = new Endereco();
            e.setId(o.getId());
            e.setLogradouro(o.getLogradouro());
            e.setNumero(o.getNumero());
            e.setComplemento(o.getComplemento());
            e.setCep(o.getCep());
            e.setCidade(o.getCidade());
            e.setUf(o.getUf());
            e.setEstado(o.getEstado());
            model.addAttribute("endereco", e);
            model.addAttribute("dono", enderecoService.clienteDoEndereco(id).orElse(null));
            return "enderecos/form";
        } catch (BusinessException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/enderecos";
        }
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("endereco") Endereco endereco, BindingResult result,
                         @RequestParam(required = false) Integer clienteId,
                         Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                enderecoService.salvar(endereco, clienteId);
                ra.addFlashAttribute("sucesso", "Endereço salvo com sucesso.");
                return "redirect:/enderecos";
            } catch (BusinessException e) {
                Erros.aplicar(result, e);
            }
        }
        model.addAttribute("clienteId", clienteId);
        if (endereco.getId() == null) {
            model.addAttribute("clientesSemEndereco", enderecoService.clientesSemEndereco());
        } else {
            model.addAttribute("dono", enderecoService.clienteDoEndereco(endereco.getId()).orElse(null));
        }
        return "enderecos/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            enderecoService.excluir(id);
            ra.addFlashAttribute("sucesso", "Endereço excluído.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/enderecos";
    }
}
