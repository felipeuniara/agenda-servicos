package br.com.agendamais.repository;

import br.com.agendamais.entity.TipoAcesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoAcessoRepository extends JpaRepository<TipoAcesso, Integer> {
    List<TipoAcesso> findAllByOrderByDescricaoAsc();

    boolean existsByDescricaoIgnoreCase(String descricao);

    boolean existsByDescricaoIgnoreCaseAndIdNot(String descricao, Integer id);
}
