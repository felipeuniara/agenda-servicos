package br.com.agendamais.controller;

import br.com.agendamais.config.LoginInterceptor;
import br.com.agendamais.dto.AgendamentoForm;
import br.com.agendamais.dto.FiltroAgenda;
import br.com.agendamais.entity.Agenda;
import br.com.agendamais.service.AgendaService;
import br.com.agendamais.service.BusinessException;
import br.com.agendamais.service.ClienteService;
import br.com.agendamais.service.ServicoService;
import br.com.agendamais.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/agenda")
public class AgendaController {
    private final AgendaService agendaService;
    private final ClienteService clienteService;
    private final ServicoService servicoService;
    private final UsuarioService usuarioService;

    public AgendaController(AgendaService agendaService, ClienteService clienteService,
                            ServicoService servicoService, UsuarioService usuarioService) {
        this.agendaService = agendaService;
        this.clienteService = clienteService;
        this.servicoService = servicoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String agenda(@RequestParam(defaultValue = "hoje") String periodo,
                         @ModelAttribute("filtro") FiltroAgenda filtro,
                         BindingResult filtroResult,
                         Model model) {
        if (filtroResult.hasErrors()) {
            model.addAttribute("erro", "Filtro de pesquisa inválido.");
        }
        if (!List.of("hoje", "semana", "mes").contains(periodo)) {
            periodo = "hoje";
        }
        List<Agenda> agendamentos;
        String rotulo;
        if (filtro.isAtivo()) {
            try {
                agendamentos = agendaService.pesquisar(filtro);
            } catch (BusinessException e) {
                model.addAttribute("erro", e.getMessage());
                agendamentos = List.of();
            }
            rotulo = "encontrados";
        } else {
            agendamentos = agendaService.listarPorPeriodo(periodo);
            rotulo = switch (periodo) {
                case "semana" -> "nesta semana";
                case "mes" -> "neste mês";
                default -> "hoje";
            };
        }
        model.addAttribute("agendamentos", agendamentos);
        model.addAttribute("periodo", periodo);
        model.addAttribute("rotulo", rotulo);
        model.addAttribute("hoje", LocalDate.now());
        model.addAttribute("mostrarDia", filtro.isAtivo() || !"hoje".equals(periodo));
        model.addAttribute("clientes", clienteService.listarPorNome());
        model.addAttribute("servicos", servicoService.listar());
        return "agenda/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime horario,
                       @SessionAttribute(LoginInterceptor.USUARIO_SESSAO) Integer usuarioConectado,
                       Model model) {
        AgendamentoForm form = new AgendamentoForm();
        form.setData(data != null ? data : LocalDate.now());
        form.setHorario(horario);
        form.setUsuarioId(usuarioConectado);
        model.addAttribute("agendamento", form);
        prepararFormulario(model, form);
        return "agenda/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            AgendamentoForm form = AgendamentoForm.de(agendaService.buscar(id));
            model.addAttribute("agendamento", form);
            prepararFormulario(model, form);
            return "agenda/form";
        } catch (BusinessException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/agenda";
        }
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("agendamento") AgendamentoForm form, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                Agenda salva = agendaService.salvar(form);
                ra.addFlashAttribute("sucesso", "Agendamento de " + salva.getCliente().getNome() + " salvo com sucesso.");
                return "redirect:/agenda";
            } catch (BusinessException e) {
                Erros.aplicar(result, e);
            }
        }
        prepararFormulario(model, form);
        return "agenda/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            agendaService.excluir(id);
            ra.addFlashAttribute("sucesso", "Agendamento excluído.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/agenda";
    }

    private void prepararFormulario(Model model, AgendamentoForm form) {
        List<LocalTime> horarios = new ArrayList<>(agendaService.horariosDoDia());
        if (form.getHorario() != null && !horarios.contains(form.getHorario())) {
            horarios.add(form.getHorario());
            horarios.sort(null);
        }
        model.addAttribute("horarios", horarios);
        model.addAttribute("clientes", clienteService.listarPorNome());
        model.addAttribute("servicos", servicoService.listar());
        model.addAttribute("profissionais", usuarioService.listar());
    }
}
