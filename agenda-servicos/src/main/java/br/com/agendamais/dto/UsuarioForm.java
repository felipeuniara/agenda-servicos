package br.com.agendamais.dto;

import br.com.agendamais.entity.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class UsuarioForm {
    private Integer id;

    @NotBlank(message = "Informe o login do usuário.")
    @Pattern(regexp = "^$|^[A-Za-z0-9._-]{3,50}$",
            message = "Use de 3 a 50 caracteres: letras, números, ponto, hífen ou sublinhado.")
    private String usuario;

    private String senha;

    @NotNull(message = "Selecione o tipo de acesso.")
    private Integer tipoAcessoId;

    public static UsuarioForm de(Usuario u) {
        UsuarioForm f = new UsuarioForm();
        f.setId(u.getId());
        f.setUsuario(u.getUsuario());
        f.setTipoAcessoId(u.getTipoAcesso() != null ? u.getTipoAcesso().getId() : null);
        return f;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public Integer getTipoAcessoId() { return tipoAcessoId; }
    public void setTipoAcessoId(Integer tipoAcessoId) { this.tipoAcessoId = tipoAcessoId; }
}
