package br.com.agendamais.dto;

import br.com.agendamais.entity.Agenda;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public class AgendamentoForm {
    private Integer id;

    @NotNull(message = "Selecione um cliente.")
    private Integer clienteId;

    @NotNull(message = "Selecione um serviço.")
    private Integer servicoId;

    @NotNull(message = "Informe a data.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data;

    @NotNull(message = "Selecione um horário.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime horario;

    @NotNull(message = "Selecione o profissional responsável.")
    private Integer usuarioId;

    @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres.")
    private String descricao;

    public static AgendamentoForm de(Agenda a) {
        AgendamentoForm f = new AgendamentoForm();
        f.setId(a.getId());
        f.setClienteId(a.getCliente().getId());
        f.setServicoId(a.getServico().getId());
        f.setUsuarioId(a.getUsuario().getId());
        f.setData(a.getData().toLocalDate());
        f.setHorario(a.getData().toLocalTime());
        f.setDescricao(a.getDescricao());
        return f;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }

    public Integer getServicoId() { return servicoId; }
    public void setServicoId(Integer servicoId) { this.servicoId = servicoId; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
