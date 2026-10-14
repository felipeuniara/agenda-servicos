package br.com.agendamais.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String usuario;

    @Column(nullable = false, length = 100)
    private String senha;

    @Column(name = "dt_cadastro", nullable = false)
    private LocalDate dtCadastro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo_acesso_id", nullable = false)
    private TipoAcesso tipoAcesso;

    public Usuario() {
    }

    @Transient
    public String getNomeExibicao() {
        if (usuario == null || usuario.isBlank()) {
            return "";
        }
        return Character.toUpperCase(usuario.charAt(0)) + usuario.substring(1);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public LocalDate getDtCadastro() { return dtCadastro; }
    public void setDtCadastro(LocalDate dtCadastro) { this.dtCadastro = dtCadastro; }

    public TipoAcesso getTipoAcesso() { return tipoAcesso; }
    public void setTipoAcesso(TipoAcesso tipoAcesso) { this.tipoAcesso = tipoAcesso; }
}
