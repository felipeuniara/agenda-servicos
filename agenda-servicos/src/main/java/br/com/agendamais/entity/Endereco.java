package br.com.agendamais.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "endereco")
public class Endereco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 150, message = "O logradouro deve ter no máximo 150 caracteres.")
    @Column(length = 150)
    private String logradouro;

    @Size(max = 2, message = "A UF deve ter 2 letras.")
    @Column(length = 2)
    private String uf;

    @Size(max = 40)
    @Column(length = 40)
    private String estado;

    @Size(max = 9, message = "O CEP deve estar no formato 00000-000.")
    @Column(length = 9)
    private String cep;

    @Size(max = 80)
    @Column(length = 80)
    private String cidade;

    private Integer numero;

    @Size(max = 80)
    @Column(length = 80)
    private String complemento;

    public Endereco() {
    }

    @Transient
    public boolean isCompleto() {
        return preenchido(logradouro) && numero != null && preenchido(cidade)
                && preenchido(uf) && preenchido(cep);
    }

    @Transient
    public boolean isVazio() {
        return !preenchido(logradouro) && numero == null && !preenchido(cidade) && !preenchido(uf)
                && !preenchido(estado) && !preenchido(cep) && !preenchido(complemento);
    }

    @Transient
    public String getResumo() {
        StringBuilder sb = new StringBuilder();
        if (preenchido(logradouro)) {
            sb.append(logradouro);
            if (numero != null) sb.append(", ").append(numero);
            if (preenchido(complemento)) sb.append(" (").append(complemento).append(")");
        }
        String local = preenchido(cidade) ? cidade + (preenchido(uf) ? "/" + uf : "") : (preenchido(uf) ? uf : "");
        if (!local.isEmpty()) {
            if (sb.length() > 0) sb.append(" — ");
            sb.append(local);
        }
        return sb.toString();
    }

    private static boolean preenchido(String s) {
        return s != null && !s.isBlank();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }
}
