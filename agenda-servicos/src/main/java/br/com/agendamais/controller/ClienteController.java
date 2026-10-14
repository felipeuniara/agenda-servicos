package br.com.agendamais.controller;

import br.com.agendamais.entity.Cliente;
import br.com.agendamais.entity.Endereco;
import br.com.agendamais.service.BusinessException;
import br.com.agendamais.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "todos") String filtro,
                         @RequestParam(defaultValue = "recentes") String ordem,
                         Model model) {
        model.addAttribute("clientes", clienteService.pesquisar(q, filtro, ordem));
        model.addAttribute("q", q);
        model.addAttribute("filtro", filtro);
        model.addAttribute("ordem", ordem);
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        Cliente cliente = new Cliente();
        cliente.setEndereco(new Endereco());
        model.addAttribute("cliente", cliente);
        return "clientes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("cliente", copiaParaFormulario(clienteService.buscar(id)));
            model.addAttribute("qtdAgendamentos", clienteService.quantidadeAgendamentos(id));
            return "clientes/form";
        } catch (BusinessException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/clientes";
        }
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("cliente") Cliente cliente, BindingResult result,
                         RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                Cliente salvo = clienteService.salvar(cliente);
                ra.addFlashAttribute("sucesso", "Cliente " + salvo.getNome() + " salvo com sucesso.");
                return "redirect:/clientes";
            } catch (BusinessException e) {
                Erros.aplicar(result, e);
            }
        }
        if (cliente.getEndereco() == null) {
            cliente.setEndereco(new Endereco());
        }
        return "clientes/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            clienteService.excluir(id);
            ra.addFlashAttribute("sucesso", "Cliente excluído.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clientes";
    }

    private Cliente copiaParaFormulario(Cliente origem) {
        Cliente c = new Cliente();
        c.setId(origem.getId());
        c.setNome(origem.getNome());
        c.setTelefone(origem.getTelefone());
        c.setEmail(origem.getEmail());
        Endereco e = new Endereco();
        Endereco o = origem.getEndereco();
        if (o != null) {
            e.setId(o.getId());
            e.setLogradouro(o.getLogradouro());
            e.setNumero(o.getNumero());
            e.setComplemento(o.getComplemento());
            e.setCep(o.getCep());
            e.setCidade(o.getCidade());
            e.setUf(o.getUf());
            e.setEstado(o.getEstado());
        }
        c.setEndereco(e);
        return c;
    }
}
