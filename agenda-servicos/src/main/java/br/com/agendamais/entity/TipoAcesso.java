package br.com.agendamais.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tipo_acesso")
public class TipoAcesso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Informe a descrição do tipo de acesso.")
    @Size(max = 60, message = "A descrição deve ter no máximo 60 caracteres.")
    @Column(nullable = false, unique = true, length = 60)
    private String descricao;

    public TipoAcesso() {
    }

    public TipoAcesso(String descricao) {
        this.descricao = descricao;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
