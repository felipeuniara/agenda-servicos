package br.com.agendamais.repository;

import br.com.agendamais.entity.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, Integer> {
    List<Servico> findAllByOrderByNomeAsc();
}
