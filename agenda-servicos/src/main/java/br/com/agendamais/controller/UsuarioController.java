package br.com.agendamais.controller;

import br.com.agendamais.config.LoginInterceptor;
import br.com.agendamais.dto.UsuarioForm;
import br.com.agendamais.entity.Usuario;
import br.com.agendamais.service.BusinessException;
import br.com.agendamais.service.TipoAcessoService;
import br.com.agendamais.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final TipoAcessoService tipoAcessoService;

    public UsuarioController(UsuarioService usuarioService, TipoAcessoService tipoAcessoService) {
        this.usuarioService = usuarioService;
        this.tipoAcessoService = tipoAcessoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuarioForm", new UsuarioForm());
        model.addAttribute("tipos", tipoAcessoService.listar());
        return "usuarios/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("usuarioForm", UsuarioForm.de(usuarioService.buscar(id)));
            model.addAttribute("tipos", tipoAcessoService.listar());
            return "usuarios/form";
        } catch (BusinessException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/usuarios";
        }
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("usuarioForm") UsuarioForm form, BindingResult result,
                         Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                Usuario salvo = usuarioService.salvar(form);
                ra.addFlashAttribute("sucesso", "Usuário " + salvo.getUsuario() + " salvo com sucesso.");
                return "redirect:/usuarios";
            } catch (BusinessException e) {
                Erros.aplicar(result, e);
            }
        }
        form.setSenha(null);
        model.addAttribute("tipos", tipoAcessoService.listar());
        return "usuarios/form";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Integer id,
                          @SessionAttribute(LoginInterceptor.USUARIO_SESSAO) Integer usuarioConectado,
                          RedirectAttributes ra) {
        try {
            usuarioService.excluir(id, usuarioConectado);
            ra.addFlashAttribute("sucesso", "Usuário excluído.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/usuarios";
    }
}
