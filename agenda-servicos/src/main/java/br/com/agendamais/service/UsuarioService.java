package br.com.agendamais.service;

import br.com.agendamais.dto.UsuarioForm;
import br.com.agendamais.entity.TipoAcesso;
import br.com.agendamais.entity.Usuario;
import br.com.agendamais.repository.AgendaRepository;
import br.com.agendamais.repository.TipoAcessoRepository;
import br.com.agendamais.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final TipoAcessoRepository tipoAcessoRepository;
    private final AgendaRepository agendaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, TipoAcessoRepository tipoAcessoRepository,
                          AgendaRepository agendaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tipoAcessoRepository = tipoAcessoRepository;
        this.agendaRepository = agendaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByUsuarioAsc();
    }

    public Usuario buscar(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));
    }

    public Optional<Usuario> autenticar(String login, String senha) {
        if (login == null || login.isBlank() || senha == null || senha.isEmpty()) {
            return Optional.empty();
        }
        return usuarioRepository.findByUsuarioIgnoreCase(login.trim())
                .filter(u -> passwordEncoder.matches(senha, u.getSenha()));
    }

    @Transactional
    public Usuario salvar(UsuarioForm form) {
        String login = form.getUsuario().trim().toLowerCase();
        String senha = form.getSenha() == null ? "" : form.getSenha();
        boolean novo = form.getId() == null;

        boolean duplicado = novo
                ? usuarioRepository.existsByUsuarioIgnoreCase(login)
                : usuarioRepository.existsByUsuarioIgnoreCaseAndIdNot(login, form.getId());
        if (duplicado) {
            throw new BusinessException("usuario", "Já existe um usuário com este login.");
        }

        if (form.getTipoAcessoId() == null) {
            throw new BusinessException("tipoAcessoId", "Selecione um tipo de acesso.");
        }
        TipoAcesso tipo = tipoAcessoRepository.findById(form.getTipoAcessoId())
                .orElseThrow(() -> new BusinessException("tipoAcessoId",
                        "Tipo de acesso inválido. Selecione um tipo previamente cadastrado."));

        if (novo && senha.isBlank()) {
            throw new BusinessException("senha", "Informe a senha.");
        }
        if (!senha.isBlank() && senha.length() < 6) {
            throw new BusinessException("senha", "A senha deve ter pelo menos 6 caracteres.");
        }

        Usuario usuario = novo ? new Usuario() : buscar(form.getId());
        if (novo) {
            usuario.setDtCadastro(LocalDate.now());
        }
        usuario.setUsuario(login);
        usuario.setTipoAcesso(tipo);
        if (!senha.isBlank()) {
            usuario.setSenha(passwordEncoder.encode(senha));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void excluir(Integer id, Integer idUsuarioConectado) {
        Usuario usuario = buscar(id);
        if (usuario.getId().equals(idUsuarioConectado)) {
            throw new BusinessException("Você não pode excluir o próprio usuário enquanto está conectado.");
        }
        if (agendaRepository.existsByUsuarioId(id)) {
            throw new BusinessException("Não é possível excluir " + usuario.getNomeExibicao()
                    + ": o usuário é responsável por agendamentos.");
        }
        usuarioRepository.delete(usuario);
    }
}
