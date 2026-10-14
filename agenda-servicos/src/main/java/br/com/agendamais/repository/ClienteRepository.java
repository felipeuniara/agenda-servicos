package br.com.agendamais.repository;

import br.com.agendamais.entity.Cliente;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);

    Optional<Cliente> findByEnderecoId(Integer enderecoId);

    List<Cliente> findByEnderecoIsNullOrderByNomeAsc();

    List<Cliente> findAllByOrderByNomeAsc();

    @Query("""
            select c from Cliente c
            where :termo = ''
               or lower(c.nome) like lower(concat('%', :termo, '%'))
               or c.telefone like concat('%', :termo, '%')
               or lower(c.email) like lower(concat('%', :termo, '%'))
            """)
    List<Cliente> buscar(@Param("termo") String termo, Sort sort);
}
