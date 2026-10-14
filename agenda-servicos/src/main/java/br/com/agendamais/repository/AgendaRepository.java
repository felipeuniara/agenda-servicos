package br.com.agendamais.repository;

import br.com.agendamais.entity.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendaRepository extends JpaRepository<Agenda, Integer>, JpaSpecificationExecutor<Agenda> {
    boolean existsByUsuarioIdAndData(Integer usuarioId, LocalDateTime data);

    boolean existsByUsuarioIdAndDataAndIdNot(Integer usuarioId, LocalDateTime data, Integer id);

    boolean existsByServicoId(Integer servicoId);

    boolean existsByClienteId(Integer clienteId);

    boolean existsByUsuarioId(Integer usuarioId);

    long countByServicoId(Integer servicoId);

    long countByClienteId(Integer clienteId);

    List<Agenda> findByDataBetweenOrderByDataAsc(LocalDateTime inicio, LocalDateTime fim);
}
