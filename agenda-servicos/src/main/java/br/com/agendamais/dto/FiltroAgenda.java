package br.com.agendamais.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class FiltroAgenda {
    private Integer clienteId;
    private Integer servicoId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate de;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ate;

    public boolean isAtivo() {
        return clienteId != null || servicoId != null || de != null || ate != null;
    }

    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }

    public Integer getServicoId() { return servicoId; }
    public void setServicoId(Integer servicoId) { this.servicoId = servicoId; }

    public LocalDate getDe() { return de; }
    public void setDe(LocalDate de) { this.de = de; }

    public LocalDate getAte() { return ate; }
    public void setAte(LocalDate ate) { this.ate = ate; }
}
