package br.com.agendamais.controller;

import br.com.agendamais.config.LoginInterceptor;
import br.com.agendamais.entity.Usuario;
import br.com.agendamais.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {
    private static final int SETE_DIAS = 7 * 24 * 60 * 60;

    private final UsuarioService usuarioService;

    public LoginController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute(LoginInterceptor.USUARIO_SESSAO) != null) {
            return "redirect:/agenda";
        }
        return "login";
    }

    @PostMapping("/login")
    public String entrar(@RequestParam(defaultValue = "") String usuario,
                         @RequestParam(defaultValue = "") String senha,
                         @RequestParam(required = false) String manterConectado,
                         HttpServletRequest request) {
        Optional<Usuario> autenticado = usuarioService.autenticar(usuario, senha);
        if (autenticado.isEmpty()) {
            return "redirect:/login?erro";
        }
        HttpSession session = request.getSession();
        request.changeSessionId();
        session.setAttribute(LoginInterceptor.USUARIO_SESSAO, autenticado.get().getId());
        if (manterConectado != null) {
            session.setMaxInactiveInterval(SETE_DIAS);
        }
        return "redirect:/agenda";
    }

    @PostMapping("/logout")
    public String sair(HttpSession session) {
        session.invalidate();
        return "redirect:/login?saiu";
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/agenda";
    }
}
