package br.com.agendamais.config;

import br.com.agendamais.entity.Usuario;
import br.com.agendamais.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttribute;

@ControllerAdvice
public class ModeloGlobal {
    private final UsuarioRepository usuarioRepository;

    public ModeloGlobal(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute("usuarioLogado")
    public Usuario usuarioLogado(@SessionAttribute(name = LoginInterceptor.USUARIO_SESSAO, required = false)
                                 Integer usuarioId) {
        if (usuarioId == null) return null;
        return usuarioRepository.findById(usuarioId).orElse(null);
    }
}
